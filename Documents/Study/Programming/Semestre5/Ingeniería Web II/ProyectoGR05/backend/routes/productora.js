const { Router } = require('express');

const {
    getProductoras,
    createProductora,
    updateProductora,
    deleteProductora
} = require('../controllers/productoraController');

const router = Router();

router.get('/', getProductoras);
router.post('/', createProductora);
router.put('/:nombre', updateProductora);
router.delete('/:nombre', deleteProductora);

module.exports = router;
