const mongoose = require('mongoose');

const getDbConnection = async () => {
    try {
        const url = process.env.MONGODB_URI || process.env.MONGO_URI;
        await mongoose.connect(url);
        console.log('Conectado a MongoDB');
    } catch (error) {
        console.error('Error conectando a MongoDB:', error);
    }
}

module.exports = {
    getDbConnection
}