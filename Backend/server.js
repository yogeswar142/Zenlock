require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');

const authRoutes = require('./routes/authRoutes');

const app = express();

// ── Middleware ──
app.use(express.json({ limit: '10mb' }));
app.use(cors());

// ── Routes ──
app.use('/api/auth', authRoutes);

// ── Health check ──
app.get('/api/health', (req, res) => {
    res.json({
        status: 'ok',
        timestamp: new Date().toISOString(),
        uptime: process.uptime()
    });
});

// ── Global error handler ──
app.use((err, req, res, next) => {
    console.error('Unhandled Error:', err);
    res.status(500).json({ error: 'Internal server error' });
});

// ── 404 handler ──
app.use((req, res) => {
    res.status(404).json({ error: 'Route not found' });
});

// ── MongoDB Connection ──
mongoose.connect(process.env.MONGODB_URI)
    .then(() => console.log('✅ MongoDB Connected Successfully'))
    .catch(err => {
        console.error('❌ MongoDB Connection Error:', err.message);
        process.exit(1);
    });

// ── Start Server ──
const PORT = process.env.PORT || 5000;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Zenlock API running on port ${PORT}`);
});
