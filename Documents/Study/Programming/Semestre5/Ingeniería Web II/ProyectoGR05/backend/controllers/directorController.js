const Director = require('../models/Director');
const { request, response } = require('express');

const getDirectores = async (req = request, res = response) => {
    try {
        const directores = await Director.find();
        return res.status(200).json(directores);
    } catch (error) {
        console.error('Error al obtener los directores:', error);
        return res.status(500).json({ message: 'Error al obtener los directores' });
    }
};

const createDirector = async (req = request, res = response) => {
    try {
        const { nombres } = req.body;

        const directorDB = await Director.findOne({ nombres });
        if (directorDB) {
            return res.status(400).json({ message: 'El director ya existe' });
        }

        const director = new Director(req.body);
        await director.save();

        return res.status(201).json(director);
    } catch (error) {
        console.error('Error al crear el director:', error);
        return res.status(500).json({ message: 'Error al crear el director' });
    }
};

const updateDirector = async (req = request, res = response) => {
    try {
        const { nombres } = req.params;
        const data = {
            ...req.body,
            fechaActualizacion: Date.now()
        };

        const directorActualizado = await Director.findOneAndUpdate(
            { nombres },
            data,
            { new: true }
        );

        if (!directorActualizado) {
            return res.status(404).json({ message: 'El director no existe' });
        }

        return res.status(200).json({
            message: 'Director actualizado correctamente',
            director: directorActualizado
        });
    } catch (error) {
        console.error('Error al actualizar el director:', error);
        return res.status(500).json({ message: 'Error al actualizar el director' });
    }
};

const deleteDirector = async (req = request, res = response) => {
    try {
        const { nombres } = req.params;

        const directorEliminado = await Director.findOneAndDelete({ nombres });

        if (!directorEliminado) {
            return res.status(404).json({ message: 'El director no existe' });
        }

        return res.status(200).json({
            message: 'Director eliminado correctamente',
            director: directorEliminado
        });
    } catch (error) {
        console.error('Error al eliminar el director:', error);
        return res.status(500).json({ message: 'Error al eliminar el director' });
    }
};

module.exports = {
    getDirectores,
    createDirector,
    updateDirector,
    deleteDirector
};
