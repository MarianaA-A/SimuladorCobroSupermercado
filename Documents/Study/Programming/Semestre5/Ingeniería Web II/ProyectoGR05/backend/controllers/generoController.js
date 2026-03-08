const Genero = require('../models/Genero');
const { request, response } = require('express');

const getGeneros = async (req = request, res = response) => {
    try {
        const generos = await Genero.find();
        return res.status(200).json(generos);
    } catch (error) {
        console.error('Error al obtener los géneros:', error);
        return res.status(500).json({ message: 'Error al obtener los géneros' });
    }
};

const createGenero = async (req = request, res = response) => {
    try {
        const { nombre, descripcion } = req.body;

        const generoDB = await Genero.findOne({ nombre });
        if (generoDB) {
            return res.status(400).json({ message: 'El género ya existe' });
        }

        const genero = new Genero({ nombre, descripcion });
        await genero.save();

        return res.status(201).json(genero);
    } catch (error) {
        console.error('Error al crear el género:', error);
        return res.status(500).json({ message: 'Error al crear el género' });
    }
};

const updateGenero = async (req = request, res = response) => {
    try {
        const nombreParam = (req.params.nombre || '').trim();
        const { nombre, descripcion, estado } = req.body;

        if (!nombreParam) {
            return res.status(400).json({ message: 'Debe indicar el nombre del género a actualizar' });
        }

        const generoExistente = await Genero.findOne({ nombre: nombreParam });
        if (!generoExistente) {
            return res.status(404).json({ message: 'El género no existe' });
        }

        const updates = {
            fechaActualizacion: Date.now()
        };

        if (typeof descripcion !== 'undefined') updates.descripcion = descripcion;
        if (typeof estado !== 'undefined') updates.estado = estado;

        if (typeof nombre !== 'undefined' && nombre.trim() !== '' && nombre.trim() !== nombreParam) {
            const nombreNuevo = nombre.trim();
            const nombreDuplicado = await Genero.findOne({ nombre: nombreNuevo });
            if (nombreDuplicado) {
                return res.status(400).json({ message: 'Ya existe un género con ese nombre' });
            }
            updates.nombre = nombreNuevo;
        }

        const generoActualizado = await Genero.findOneAndUpdate(
            { nombre: nombreParam },
            updates,
            { new: true }
        );

        return res.status(200).json({
            message: 'Género actualizado correctamente',
            genero: generoActualizado
        });
    } catch (error) {
        console.error('Error al actualizar el género:', error);
        return res.status(500).json({ message: 'Error al actualizar el género' });
    }
};

const deleteGenero = async (req = request, res = response) => {
    try {
        const { nombre } = req.params;

        const generoEliminado = await Genero.findOneAndDelete({ nombre });

        if (!generoEliminado) {
            return res.status(404).json({ message: 'El género no existe' });
        }

        return res.status(200).json({
            message: 'Género eliminado correctamente',
            genero: generoEliminado
        });
    } catch (error) {
        console.error('Error al eliminar el género:', error);
        return res.status(500).json({ message: 'Error al eliminar el género' });
    }
};

module.exports = {
    getGeneros,
    createGenero,
    updateGenero,
    deleteGenero
};
