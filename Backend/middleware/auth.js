const jwt = require('jsonwebtoken');

/**
 * JWT authentication middleware.
 * Extracts and verifies the Bearer token from the Authorization header.
 * On success, attaches the decoded payload (id, email) to req.user.
 */
const authMiddleware = (req, res, next) => {
    const authHeader = req.header('Authorization');
    if (!authHeader) {
        return res.status(401).json({ error: 'No token provided, authorization denied' });
    }

    const token = authHeader.startsWith('Bearer ')
        ? authHeader.slice(7)
        : authHeader;

    if (!token) {
        return res.status(401).json({ error: 'Malformed authorization header' });
    }

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        req.user = decoded;
        next();
    } catch (err) {
        if (err.name === 'TokenExpiredError') {
            return res.status(401).json({ error: 'Token has expired, please login again' });
        }
        res.status(401).json({ error: 'Token is not valid' });
    }
};

module.exports = authMiddleware;
