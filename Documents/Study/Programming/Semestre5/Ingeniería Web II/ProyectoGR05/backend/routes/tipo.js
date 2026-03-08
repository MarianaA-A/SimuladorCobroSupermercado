const { Router } = require('express');

const {
    getTipos,
    createTipo,
    updateTipo,
    deleteTipo
} = require('../controllers/tipoController');

const router = Router();

router.get('/', getTipos);
router.post('/', createTipo);
router.put('/:nombre', updateTipo);
router.delete('/:nombre', deleteTipo);

module.exports = router;
