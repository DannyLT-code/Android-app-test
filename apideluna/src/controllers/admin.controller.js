import { pool } from '../db.js';
import crypto from 'crypto';

export const login = async (req, res) => {
    const { user, password } = req.body;

    try {
        const [rows] = await pool.query('SELECT * FROM admin WHERE user = ?', [user]);

        if (rows.length === 1) {
            const storedPasswordHash = rows[0].password; 
            const userPasswordHash = crypto.createHash('sha1').update(password).digest('hex'); 

            if (userPasswordHash === storedPasswordHash) {
                res.status(200).json({ message: 'Inicio de sesión exitoso' });
            } else {
              
                res.status(401).json({ message: 'Credenciales incorrectas' });
            }
        } else {
        
            res.status(401).json({ message: 'Usuario no encontrado' });
        }
    } catch (error) {
        console.error(error);
        res.status(500).json({
            message: 'Error al autenticar al administrador'
        });
    }
};

export const getAdmin = async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM admin')
        res.json(rows)
    } catch (error) {        
        res.status(500).json({
            message: 'Error al obtener el admin'
        })
    }
}
