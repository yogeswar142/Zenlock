const mongoose = require('mongoose');

let isConnected = false;

/**
 * Connect to MongoDB and cache the connection instance across Cloudflare Worker invocations.
 */
async function connectToDatabase(uri) {
    if (isConnected && mongoose.connection.readyState === 1) {
        return;
    }

    if (!uri) {
        throw new Error('MONGODB_URI environment variable is not defined.');
    }

    // Set bufferCommands to false for serverless environments
    await mongoose.connect(uri, {
        bufferCommands: false,
        serverSelectionTimeoutMS: 5000,
    });

    isConnected = true;
}

module.exports = { connectToDatabase };
