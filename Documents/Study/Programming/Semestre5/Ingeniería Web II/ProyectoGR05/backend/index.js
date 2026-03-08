require('dotenv').config({ path: __dirname + '/.env' });
const express = require('express');
const cors = require('cors');

const { getDbConnection } = require('./db/db-connection-mongo');

const app = express();
const port = process.env.PORT || 4000;

app.use(cors());
app.use(express.json());

// Rutas
app.use('/api/generos', require('./routes/genero'));
app.use('/api/directores', require('./routes/director'));
app.use('/api/productoras', require('./routes/productora'));
app.use('/api/tipos', require('./routes/tipo'));
app.use('/api/medias', require('./routes/media'));

getDbConnection();

app.listen(port, () => {
  console.log(`Server is running on port ${port}`);
});
