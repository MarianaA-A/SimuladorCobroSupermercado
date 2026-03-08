const Media = require('../models/Media');
const Genero = require('../models/Genero');
const Director = require('../models/Director');
const Productora = require('../models/Productora');
const Tipo = require('../models/Tipo');
const { request, response } = require('express');

const getMedias = async (req = request, res = response) => {
    try {
        const medias = await Media.find()
            .populate('genero')
            .populate('director')
            .populate('productora')
            .populate('tipo');

        return res.status(200).json(medias);
    } catch (error) {
        console.error('Error al obtener las medias:', error);
        return res.status(500).json({ message: 'Error al obtener las medias' });
    }
};

const createMedia = async (req = request, res = response) => {
    try {
        const {
            serial,
            titulo,
            sinopsis,
            url,
            imagenPortada,
            anioEstreno,
            genero,
            director,
            productora,
            tipo
        } = req.body;

        const mediaBySerial = await Media.findOne({ serial });
        if (mediaBySerial) {
            return res.status(400).json({ message: 'Ya existe una media con ese serial' });
        }

        const mediaByUrl = await Media.findOne({ url });
        if (mediaByUrl) {
            return res.status(400).json({ message: 'Ya existe una media con esa URL' });
        }

        const generoDB = await Genero.findOne({ _id: genero, estado: 'Activo' });
        if (!generoDB) {
            return res.status(400).json({ message: 'El género no existe o está inactivo' });
        }

        const directorDB = await Director.findOne({ _id: director, estado: 'Activo' });
        if (!directorDB) {
            return res.status(400).json({ message: 'El director no existe o está inactivo' });
        }

        const productoraDB = await Productora.findOne({ _id: productora, estado: 'Activo' });
        if (!productoraDB) {
            return res.status(400).json({ message: 'La productora no existe o está inactiva' });
        }

        const tipoDB = await Tipo.findById(tipo);
        if (!tipoDB) {
            return res.status(400).json({ message: 'El tipo no existe' });
        }

        const media = new Media({
            serial,
            titulo,
            sinopsis,
            url,
            imagenPortada,
            anioEstreno,
            genero,
            director,
            productora,
            tipo
        });

        await media.save();

        return res.status(201).json(media);
    } catch (error) {
        console.error('Error al crear la media:', error);
        return res.status(500).json({ message: 'Error al crear la media' });
    }
};

const updateMedia = async (req = request, res = response) => {
    try {
        const { serial } = req.params;
        const data = {
            ...req.body,
            fechaActualizacion: Date.now()
        };

        if (data.url) {
            const mediaUrlExistente = await Media.findOne({
                url: data.url,
                serial: { $ne: serial }
            });

            if (mediaUrlExistente) {
                return res.status(400).json({ message: 'Ya existe una media con esa URL' });
            }
        }

        if (data.genero) {
            const generoDB = await Genero.findOne({ _id: data.genero, estado: 'Activo' });
            if (!generoDB) {
                return res.status(400).json({ message: 'El género no existe o está inactivo' });
            }
        }

        if (data.director) {
            const directorDB = await Director.findOne({ _id: data.director, estado: 'Activo' });
            if (!directorDB) {
                return res.status(400).json({ message: 'El director no existe o está inactivo' });
            }
        }

        if (data.productora) {
            const productoraDB = await Productora.findOne({ _id: data.productora, estado: 'Activo' });
            if (!productoraDB) {
                return res.status(400).json({ message: 'La productora no existe o está inactiva' });
            }
        }

        if (data.tipo) {
            const tipoDB = await Tipo.findById(data.tipo);
            if (!tipoDB) {
                return res.status(400).json({ message: 'El tipo no existe' });
            }
        }

        const mediaActualizada = await Media.findOneAndUpdate(
            { serial },
            data,
            { new: true }
        )
            .populate('genero')
            .populate('director')
            .populate('productora')
            .populate('tipo');

        if (!mediaActualizada) {
            return res.status(404).json({ message: 'La media no existe' });
        }

        return res.status(200).json({
            message: 'Media actualizada correctamente',
            media: mediaActualizada
        });
    } catch (error) {
        console.error('Error al actualizar la media:', error);
        return res.status(500).json({ message: 'Error al actualizar la media' });
    }
};

const deleteMedia = async (req = request, res = response) => {
    try {
        const { serial } = req.params;

        const mediaEliminada = await Media.findOneAndDelete({ serial });

        if (!mediaEliminada) {
            return res.status(404).json({ message: 'La media no existe' });
        }

        return res.status(200).json({
            message: 'Media eliminada correctamente',
            media: mediaEliminada
        });
    } catch (error) {
        console.error('Error al eliminar la media:', error);
        return res.status(500).json({ message: 'Error al eliminar la media' });
    }
};

module.exports = {
    getMedias,
    createMedia,
    updateMedia,
    deleteMedia
};
