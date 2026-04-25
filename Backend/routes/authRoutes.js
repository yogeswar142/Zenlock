const express = require('express');
const router = express.Router();
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { OAuth2Client } = require('google-auth-library');
const User = require('../models/User');
const authMiddleware = require('../middleware/auth');

const client = new OAuth2Client();

// --- Helper to generate JWT ---
const generateToken = (user) => {
    return jwt.sign(
        { id: user._id, email: user.email },
        process.env.JWT_SECRET,
        { expiresIn: '30d' }
    );
};

/**
 * Sanitize user object before sending to client.
 * Strips password and internal Mongoose fields.
 */
const sanitizeUser = (user) => {
    const obj = user.toObject();
    delete obj.password;
    delete obj.__v;
    return obj;
};

// ─── Google Auth ────────────────────────────────────────────────────
router.post('/google', async (req, res) => {
    try {
        const { idToken } = req.body;

        if (!idToken) {
            return res.status(400).json({ error: 'Google ID token is required' });
        }

        // Verify the Google ID token
        const ticket = await client.verifyIdToken({
            idToken: idToken,
            audience: process.env.GOOGLE_CLIENT_ID,
        });
        const payload = ticket.getPayload();
        const email = payload.email;
        const googleId = payload.sub;

        // Check if user exists
        let user = await User.findOne({ email });

        if (user) {
            // Update googleId if not set (user previously signed up via email)
            if (!user.googleId) {
                user.googleId = googleId;
                await user.save();
            }
            const token = generateToken(user);
            return res.json({
                user: sanitizeUser(user),
                token,
                isNewUser: false
            });
        }

        // New user – create account
        user = new User({ email, googleId });
        await user.save();
        const token = generateToken(user);

        return res.json({
            user: sanitizeUser(user),
            token,
            isNewUser: true
        });

    } catch (error) {
        console.error('Google Auth Error:', error);
        res.status(401).json({ error: 'Invalid Google Token' });
    }
});

// ─── Standard Signup ────────────────────────────────────────────────
router.post('/signup', async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({ error: 'Email and password are required' });
        }

        if (password.length < 6) {
            return res.status(400).json({ error: 'Password must be at least 6 characters' });
        }

        // Check for existing user
        let user = await User.findOne({ email });
        if (user) {
            return res.status(400).json({ error: 'An account with this email already exists' });
        }

        const hashedPassword = await bcrypt.hash(password, 12);
        user = new User({ email, password: hashedPassword });
        await user.save();

        const token = generateToken(user);
        res.status(201).json({
            user: sanitizeUser(user),
            token,
            isNewUser: true
        });
    } catch (error) {
        console.error('Signup Error:', error);
        res.status(500).json({ error: 'Server error during signup' });
    }
});

// ─── Standard Login ─────────────────────────────────────────────────
router.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({ error: 'Email and password are required' });
        }

        // Allow login by email or username
        const user = await User.findOne({
            $or: [{ email }, { username: email }]
        });

        if (!user) {
            return res.status(400).json({ error: 'Invalid credentials' });
        }

        if (!user.password) {
            return res.status(400).json({ error: 'This account uses Google Sign-In. Please login with Google.' });
        }

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(400).json({ error: 'Invalid credentials' });
        }

        const token = generateToken(user);
        res.json({
            user: sanitizeUser(user),
            token,
            isNewUser: false
        });
    } catch (error) {
        console.error('Login Error:', error);
        res.status(500).json({ error: 'Server error during login' });
    }
});

// ─── Profile Setup ──────────────────────────────────────────────────
router.post('/profile-setup', authMiddleware, async (req, res) => {
    try {
        const { username, dob, gender, country, state, city } = req.body;

        if (!username) {
            return res.status(400).json({ error: 'Username is required' });
        }

        // Check if username is taken by another user
        const existingUser = await User.findOne({
            username: username.toLowerCase(),
            _id: { $ne: req.user.id }
        });
        if (existingUser) {
            return res.status(400).json({ error: 'Username is already taken' });
        }

        const user = await User.findByIdAndUpdate(
            req.user.id,
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
            return res.status(404).json({ error: 'User not found' });
        }

        res.json({ user: sanitizeUser(user) });
    } catch (error) {
        console.error('Profile Setup Error:', error);
        res.status(500).json({ error: 'Server error during profile setup' });
    }
});

// ─── Get Profile ────────────────────────────────────────────────────
router.get('/profile', authMiddleware, async (req, res) => {
    try {
        const user = await User.findById(req.user.id).select('-password -__v');
        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }
        res.json({ user });
    } catch (error) {
        console.error('Get Profile Error:', error);
        res.status(500).json({ error: 'Server error fetching profile' });
    }
});

// ─── Update Profile ─────────────────────────────────────────────────
router.put('/profile', authMiddleware, async (req, res) => {
    try {
        const { username, dob, gender, country, state, city } = req.body;

        const updateFields = {};
        if (username !== undefined) {
            // Check if username is taken
            const existing = await User.findOne({
                username: username.toLowerCase(),
                _id: { $ne: req.user.id }
            });
            if (existing) {
                return res.status(400).json({ error: 'Username is already taken' });
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
            req.user.id,
            updateFields,
            { new: true }
        ).select('-password -__v');

        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }

        res.json({ user });
    } catch (error) {
        console.error('Update Profile Error:', error);
        res.status(500).json({ error: 'Server error updating profile' });
    }
});

// ─── Delete Account ─────────────────────────────────────────────────
router.delete('/account', authMiddleware, async (req, res) => {
    try {
        const user = await User.findByIdAndDelete(req.user.id);
        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }
        res.json({ message: 'Account deleted successfully' });
    } catch (error) {
        console.error('Delete Account Error:', error);
        res.status(500).json({ error: 'Server error deleting account' });
    }
});

module.exports = router;
