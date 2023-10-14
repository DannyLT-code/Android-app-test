import {Router} from 'express';
import {login} from '../controllers/admin.controller.js';
import {getAdmin} from '../controllers/admin.controller.js';

const router = Router();

router.post('/login', login); 
router.get('/login', getAdmin); 

export default router;