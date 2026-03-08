const { Router } = require('express');

const {
    getMedias,
    createMedia,
    updateMedia,
    deleteMedia
} = require('../controllers/mediaController');

const router = Router();

router.get('/', getMedias);
router.post('/', createMedia);
router.put('/:serial', updateMedia);
router.delete('/:serial', deleteMedia);

module.exports = router;
