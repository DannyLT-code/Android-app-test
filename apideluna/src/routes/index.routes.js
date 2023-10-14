import {Router} from 'express';
import {ola} from '../controllers/index.controller.js';

const router = Router();

router.get('/ola', ola);

export default router;