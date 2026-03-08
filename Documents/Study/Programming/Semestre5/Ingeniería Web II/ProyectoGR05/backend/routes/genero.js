const { Router } = require('express');

const { 
    getGeneros, 
    createGenero, 
    updateGenero, 
    deleteGenero 
} = require('../controllers/generoController');

const router = Router();

router.get('/', getGeneros);
router.post('/', createGenero);
router.put('/:nombre', updateGenero);
router.delete('/:nombre', deleteGenero);

module.exports = router;