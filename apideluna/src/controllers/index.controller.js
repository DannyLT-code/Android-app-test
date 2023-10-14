import {pool} from '../db.js';

export const ola = async (req, res) => {
    const [negocio] = await pool.query('SELECT * FROM negocios')
    res.json(negocio[0])
}