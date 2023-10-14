import {Router} from 'express';
import {getNegocios, getNegocio, crearNegocio, actualizarNegocio, eliminarNegocio} from '../controllers/negocios.controller.js';

const router = Router();

router.get('/negocioss', getNegocios);

router.get('/negocioss/:id', getNegocio);

router.post('/negocioss', crearNegocio);

router.delete('/negocioss/:id', eliminarNegocio);

router.patch('/negocioss/:id', actualizarNegocio);

export default router;
