import mongoose from 'mongoose';

let isConnected = false;

/**
 * Connect to MongoDB and cache the connection instance across Cloudflare Worker invocations.
 */
export async function connectToDatabase(uri) {
    if (isConnected && mongoose.connection.readyState === 1) {
        return;
    }

    if (!uri) {
        throw new Error('MONGODB_URI environment secret is not defined in Cloudflare Workers.');
    }

    await mongoose.connect(uri, {
        serverSelectionTimeoutMS: 10000,
    });

    isConnected = true;
}
