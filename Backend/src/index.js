import { Hono } from 'hono';
import { cors } from 'hono/cors';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { OAuth2Client } from 'google-auth-library';
import { connectToDatabase } from './db.js';
import User from '../models/User.js';

const app = new Hono();

// Global CORS & DB Middleware
app.use('*', cors());
app.use('*', async (c, next) => {
    const mongoUri = c.env?.MONGODB_URI || process.env?.MONGODB_URI;
    if (mongoUri) {
        try {
            await connectToDatabase(mongoUri);
        } catch (err) {
            console.error('Database connection failed:', err.message);
        }
    }
    await next();
});

// Helper for JWT
const generateToken = (user, jwtSecret) => {
    return jwt.sign(
        { id: user._id, email: user.email },
        jwtSecret,
        { expiresIn: '30d' }
    );
};

// Sanitize user object
const sanitizeUser = (user) => {
    const obj = user.toObject ? user.toObject() : { ...user };
    delete obj.password;
    delete obj.__v;
    return obj;
};

// Auth Middleware
const authMiddleware = async (c, next) => {
    const authHeader = c.req.header('Authorization');
    if (!authHeader) {
        return c.json({ error: 'No token provided, authorization denied' }, 401);
    }

    const token = authHeader.startsWith('Bearer ') ? authHeader.slice(7) : authHeader;
    if (!token) {
        return c.json({ error: 'Malformed authorization header' }, 401);
    }

    try {
        const secret = c.env?.JWT_SECRET || process.env?.JWT_SECRET;
        const decoded = jwt.verify(token, secret);
        c.set('user', decoded);
        await next();
    } catch (err) {
        if (err.name === 'TokenExpiredError') {
            return c.json({ error: 'Token has expired, please login again' }, 401);
        }
        return c.json({ error: 'Token is not valid' }, 401);
    }
};

// ── Health Check ──
app.get('/api/health', (c) => {
    return c.json({
        status: 'ok',
        timestamp: new Date().toISOString(),
        platform: 'Cloudflare Workers (Hono)'
    });
});

// ── Google Auth ──
app.post('/api/auth/google', async (c) => {
    try {
        const body = await c.req.json();
        const { idToken } = body;

        if (!idToken) {
            return c.json({ error: 'Google ID token is required' }, 400);
        }

        const clientId = c.env?.GOOGLE_CLIENT_ID || process.env?.GOOGLE_CLIENT_ID;
        const client = new OAuth2Client();
        const ticket = await client.verifyIdToken({
            idToken: idToken,
            audience: clientId,
        });
        const payload = ticket.getPayload();
        const email = payload.email;
        const googleId = payload.sub;

        let user = await User.findOne({ email });
        const jwtSecret = c.env?.JWT_SECRET || process.env?.JWT_SECRET;

        if (user) {
            if (!user.googleId) {
                user.googleId = googleId;
                await user.save();
            }
            const token = generateToken(user, jwtSecret);
            return c.json({
                user: sanitizeUser(user),
                token,
                isNewUser: false
            });
        }

        user = new User({ email, googleId });
        await user.save();
        const token = generateToken(user, jwtSecret);

        return c.json({
            user: sanitizeUser(user),
            token,
            isNewUser: true
        });

    } catch (error) {
        console.error('Google Auth Error:', error);
        return c.json({ error: 'Invalid Google Token' }, 401);
    }
});

// ── Standard Signup ──
app.post('/api/auth/signup', async (c) => {
    try {
        const body = await c.req.json();
        const { email, password } = body;

        if (!email || !password) {
            return c.json({ error: 'Email and password are required' }, 400);
        }

        if (password.length < 6) {
            return c.json({ error: 'Password must be at least 6 characters' }, 400);
        }

        let user = await User.findOne({ email });
        if (user) {
            return c.json({ error: 'An account with this email already exists' }, 400);
        }

        const hashedPassword = await bcrypt.hash(password, 12);
        user = new User({ email, password: hashedPassword });
        await user.save();

        const jwtSecret = c.env?.JWT_SECRET || process.env?.JWT_SECRET;
        const token = generateToken(user, jwtSecret);

        return c.json({
            user: sanitizeUser(user),
            token,
            isNewUser: true
        }, 201);
    } catch (error) {
        console.error('Signup Error:', error);
        return c.json({ error: 'Server error during signup' }, 500);
    }
});

// ── Standard Login ──
app.post('/api/auth/login', async (c) => {
    try {
        const body = await c.req.json();
        const { email, password } = body;

        if (!email || !password) {
            return c.json({ error: 'Email and password are required' }, 400);
        }

        const user = await User.findOne({
            $or: [{ email }, { username: email }]
        });

        if (!user) {
            return c.json({ error: 'Invalid credentials' }, 400);
        }

        if (!user.password) {
            return c.json({ error: 'This account uses Google Sign-In. Please login with Google.' }, 400);
        }

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return c.json({ error: 'Invalid credentials' }, 400);
        }

        const jwtSecret = c.env?.JWT_SECRET || process.env?.JWT_SECRET;
        const token = generateToken(user, jwtSecret);

        return c.json({
            user: sanitizeUser(user),
            token,
            isNewUser: false
        });
    } catch (error) {
        console.error('Login Error:', error);
        return c.json({ error: 'Server error during login' }, 500);
    }
});

// ── Profile Setup ──
app.post('/api/auth/profile-setup', authMiddleware, async (c) => {
    try {
        const reqUser = c.get('user');
        const body = await c.req.json();
        const { username, dob, gender, country, state, city } = body;

        if (!username) {
            return c.json({ error: 'Username is required' }, 400);
        }

        const existingUser = await User.findOne({
            username: username.toLowerCase(),
            _id: { $ne: reqUser.id }
        });

        if (existingUser) {
            return c.json({ error: 'Username is already taken' }, 400);
        }

        const user = await User.findByIdAndUpdate(
            reqUser.id,
            {
                username: username.toLowerCase(),
                displayName: username,
                dob,
                gender,
                country,
                state,
                city,
                isProfileComplete: true
            },
            { new: true }
        );

        if (!user) {
            return c.json({ error: 'User not found' }, 404);
        }

        return c.json({ user: sanitizeUser(user) });
    } catch (error) {
        console.error('Profile Setup Error:', error);
        return c.json({ error: 'Server error during profile setup' }, 500);
    }
});

// ── Get Profile ──
app.get('/api/auth/profile', authMiddleware, async (c) => {
    try {
        const reqUser = c.get('user');
        const user = await User.findById(reqUser.id).select('-password -__v');
        if (!user) {
            return c.json({ error: 'User not found' }, 404);
        }
        return c.json({ user });
    } catch (error) {
        console.error('Get Profile Error:', error);
        return c.json({ error: 'Server error fetching profile' }, 500);
    }
});

// ── Update Profile ──
app.put('/api/auth/profile', authMiddleware, async (c) => {
    try {
        const reqUser = c.get('user');
        const body = await c.req.json();
        const { username, dob, gender, country, state, city } = body;

        const updateFields = {};
        if (username !== undefined) {
            const existing = await User.findOne({
                username: username.toLowerCase(),
                _id: { $ne: reqUser.id }
            });
            if (existing) {
                return c.json({ error: 'Username is already taken' }, 400);
            }
            updateFields.username = username.toLowerCase();
            updateFields.displayName = username;
        }
        if (dob !== undefined) updateFields.dob = dob;
        if (gender !== undefined) updateFields.gender = gender;
        if (country !== undefined) updateFields.country = country;
        if (state !== undefined) updateFields.state = state;
        if (city !== undefined) updateFields.city = city;

        const user = await User.findByIdAndUpdate(
            reqUser.id,
            updateFields,
            { new: true }
        ).select('-password -__v');

        if (!user) {
            return c.json({ error: 'User not found' }, 404);
        }

        return c.json({ user });
    } catch (error) {
        console.error('Update Profile Error:', error);
        return c.json({ error: 'Server error updating profile' }, 500);
    }
});

// ── Delete Account ──
app.delete('/api/auth/account', authMiddleware, async (c) => {
    try {
        const reqUser = c.get('user');
        const user = await User.findByIdAndDelete(reqUser.id);
        if (!user) {
            return c.json({ error: 'User not found' }, 404);
        }
        return c.json({ message: 'Account deleted successfully' });
    } catch (error) {
        console.error('Delete Account Error:', error);
        return c.json({ error: 'Server error deleting account' }, 500);
    }
});

export default app;
