// Login.js
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { User, Lock } from 'react-feather';
import './Login.css';

function Login() {
    const navigate = useNavigate();
    const [user, setUser] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const handleLogin = async () => {
        try {
            if (!user || !password) {
                setError('Por favor, complete todos los campos.');
                return;
            }

            const response = await fetch('https://apideluna-production.up.railway.app/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ user, password }),
            });

            if (response.status === 200) {
                navigate('/dashboard');
            } else {
                setError('Credenciales incorrectas. Por favor, inténtelo de nuevo.');
            }
        } catch (error) {
            console.error('Error al iniciar sesión:', error);
            setError('Error al iniciar sesión. Por favor, inténtelo de nuevo más tarde.');
        }
    };

    return (
        <div className="login-container">

            <div className="circle-container">
                <img src="/Ellipse.png" alt="Circle" className="circle" />
                <img src="/Ellipse2.png" alt="Circle" className="circle-2" />
                <img src="/Ellipse3.png" alt="Circle" className="circle-3" />
            </div>
            <div className="vector-container">
                <img src="/Vector.png" alt="Vector" className="vector" />
            </div>
            <img src="/100natural.png" alt="Logo" className="logo" />

            <div className="input-container">
                <div className="input-icon">
                    <User size={20} />
                </div>
                <input
                    type="text"
                    placeholder="Username"
                    className="input-field with-icon"
                    value={user}
                    onChange={(e) => setUser(e.target.value)}
                />
            </div>

            <div className="input-container">
                <div className="input-icon">
                    <Lock size={20} />
                </div>
                <input
                    type="password"
                    placeholder="Password"
                    className="input-field with-icon"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
            </div>

            <button className="login-button" onClick={handleLogin}>
                Log In
            </button>

            {error && <p className="error-message">{error}</p>}
        </div>
    );
}

export default Login;
