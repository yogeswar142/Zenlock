const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    email: {
        type: String,
        required: true,
        unique: true,
        lowercase: true,
        trim: true
    },
    password: {
        type: String, // Null for Google-only accounts
    },
    googleId: {
        type: String,
        default: null
    },
    username: {
        type: String,
        default: null,
        sparse: true,
        unique: true,
        lowercase: true,
        trim: true
    },
    displayName: {
        type: String,
        default: null,
        trim: true
    },
    dob: {
        type: String,
        default: null
    },
    gender: {
        type: String,
        enum: ['Male', 'Female', 'Other', null],
        default: null
    },
    country: {
        type: String,
        default: null
    },
    state: {
        type: String,
        default: null
    },
    city: {
        type: String,
        default: null
    },
    isProfileComplete: {
        type: Boolean,
        default: false
    }
}, { timestamps: true });

module.exports = mongoose.model('User', userSchema);
