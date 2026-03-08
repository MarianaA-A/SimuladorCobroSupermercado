const Productora = require('../models/Productora');
const { request, response } = require('express');

const getProductoras = async (req = request, res = response) => {
    try {
        const productoras = await Productora.find();
        return res.status(200).json(productoras);
    } catch (error) {
        console.error('Error al obtener las productoras:', error);
        return res.status(500).json({ message: 'Error al obtener las productoras' });
    }
};

const createProductora = async (req = request, res = response) => {
    try {
        const { nombre } = req.body;

        const productoraDB = await Productora.findOne({ nombre });
        if (productoraDB) {
            return res.status(400).json({ message: 'La productora ya existe' });
        }

        const productora = new Productora(req.body);
        await productora.save();

        return res.status(201).json(productora);
    } catch (error) {
        console.error('Error al crear la productora:', error);
        return res.status(500).json({ message: 'Error al crear la productora' });
    }
};

const updateProductora = async (req = request, res = response) => {
    try {
        const { nombre } = req.params;
        const data = {
            ...req.body,
            fechaActualizacion: Date.now()
        };

        const productoraActualizada = await Productora.findOneAndUpdate(
            { nombre },
            data,
            { new: true }
        );

        if (!productoraActualizada) {
            return res.status(404).json({ message: 'La productora no existe' });
        }

        return res.status(200).json({
            message: 'Productora actualizada correctamente',
            productora: productoraActualizada
        });
    } catch (error) {
        console.error('Error al actualizar la productora:', error);
        return res.status(500).json({ message: 'Error al actualizar la productora' });
    }
};

const deleteProductora = async (req = request, res = response) => {
    try {
        const { nombre } = req.params;

        const productoraEliminada = await Productora.findOneAndDelete({ nombre });

        if (!productoraEliminada) {
            return res.status(404).json({ message: 'La productora no existe' });
        }

        return res.status(200).json({
            message: 'Productora eliminada correctamente',
            productora: productoraEliminada
        });
    } catch (error) {
        console.error('Error al eliminar la productora:', error);
        return res.status(500).json({ message: 'Error al eliminar la productora' });
    }
};

module.exports = {
    getProductoras,
    createProductora,
    updateProductora,
    deleteProductora
};
