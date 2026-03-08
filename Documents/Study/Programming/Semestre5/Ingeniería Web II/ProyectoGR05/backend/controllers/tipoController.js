const Tipo = require('../models/Tipo');
const { request, response } = require('express');

const getTipos = async (req = request, res = response) => {
    try {
        const tipos = await Tipo.find();
        return res.status(200).json(tipos);
    } catch (error) {
        console.error('Error al obtener los tipos:', error);
        return res.status(500).json({ message: 'Error al obtener los tipos' });
    }
};

const createTipo = async (req = request, res = response) => {
    try {
        const { nombre } = req.body;

        const tipoDB = await Tipo.findOne({ nombre });
        if (tipoDB) {
            return res.status(400).json({ message: 'El tipo ya existe' });
        }

        const tipo = new Tipo(req.body);
        await tipo.save();

        return res.status(201).json(tipo);
    } catch (error) {
        console.error('Error al crear el tipo:', error);
        return res.status(500).json({ message: 'Error al crear el tipo' });
    }
};

const updateTipo = async (req = request, res = response) => {
    try {
        const { nombre } = req.params;
        const data = {
            ...req.body,
            fechaActualizacion: Date.now()
        };

        const tipoActualizado = await Tipo.findOneAndUpdate(
            { nombre },
            data,
            { new: true }
        );

        if (!tipoActualizado) {
            return res.status(404).json({ message: 'El tipo no existe' });
        }

        return res.status(200).json({
            message: 'Tipo actualizado correctamente',
            tipo: tipoActualizado
        });
    } catch (error) {
        console.error('Error al actualizar el tipo:', error);
        return res.status(500).json({ message: 'Error al actualizar el tipo' });
    }
};

const deleteTipo = async (req = request, res = response) => {
    try {
        const { nombre } = req.params;

        const tipoEliminado = await Tipo.findOneAndDelete({ nombre });

        if (!tipoEliminado) {
            return res.status(404).json({ message: 'El tipo no existe' });
        }

        return res.status(200).json({
            message: 'Tipo eliminado correctamente',
            tipo: tipoEliminado
        });
    } catch (error) {
        console.error('Error al eliminar el tipo:', error);
        return res.status(500).json({ message: 'Error al eliminar el tipo' });
    }
};

module.exports = {
    getTipos,
    createTipo,
    updateTipo,
    deleteTipo
};
