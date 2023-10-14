import React, { useState, useEffect } from 'react';
import { LogOut, Check, HelpCircle, Edit, Trash2, Image } from 'react-feather';
import { useNavigate } from 'react-router-dom';
import './Dashboard.css';

function Tooltip({ content }) {
    return <div className="tooltip">{content}</div>;
}

function Dashboard() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        logo: null,
        name: '',
        disponible: 'Abierto',
        distancia: '2km',
        imagenCategoria: null,
        description: '',
        checkbox: '',
        tipo: '',
        address: '',
        imagenReal: null,
        dropdown: '',
        openingTime: '',
        closingTime: '',
        latitude: '',
        longitude: '',
    });

    const [uploadStatus, setUploadStatus] = useState({
        logo: false,
        imagenReal: false,
    });

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Check if all required fields are filled
        const requiredFields = ['logo', 'imagenReal', 'name', 'description', 'address', 'openingTime', 'closingTime', 'latitude', 'longitude'];

        const isFormValid = requiredFields.every((field) => formData[field] !== '');

        if (!isFormValid) {
            alert('Favor de llenar todas las casillas');
            return;
        }

        // Concatenate opening and closing times
        const concatenatedTimes = `${formData.openingTime} - ${formData.closingTime}`;

        console.log(formData); // Log the entire form data
        console.log(concatenatedTimes); // Log the concatenated opening and closing times

        try {
            const formDataToSend = new FormData();

            formDataToSend.append('imagenNegocio', formData.logo);
            formDataToSend.append('tituloNegocio', formData.name);
            formDataToSend.append('disponible', formData.disponible);
            formDataToSend.append('distancia', formData.distancia);
            formDataToSend.append('imagenCategoria', formData.imagenCategoria);
            formDataToSend.append('descripcion', formData.description);
            formDataToSend.append('insignia', formData.checkbox);
            formDataToSend.append('tipoNegocio', formData.tipo);
            formDataToSend.append('direccion', formData.address);
            formDataToSend.append('imagenRealNegocio', formData.imagenReal);
            formDataToSend.append('nombreCategoria', formData.dropdown);
            formDataToSend.append('horario', concatenatedTimes);
            formDataToSend.append('latitud', formData.latitude);
            formDataToSend.append('longitud', formData.longitude);

            const response = await fetch('https://apideluna-production.up.railway.app/api/negocios', {
                method: 'POST',
                body: formDataToSend,
            });

            // Handle the response as needed
            if (response.ok) {
                // Handle success
                console.log('Form submitted successfully');
            } else {
                // Handle error
                console.error('Error submitting form:', response.statusText);
            }
        } catch (error) {
            console.error('Error submitting form:', error.message);
        }
    };

    const handleImageUpload = (name, image) => {
        // Validate image dimensions
        const maxWidth = name === 'logo' ? 80 : 360;
        const maxHeight = name === 'logo' ? 80 : 180;

        if (image.width > maxWidth || image.height > maxHeight) {
            alert(`Favor de usar una imagen de dimensiones ${maxWidth}x${maxHeight}.`);
            return;
        }

        setFormData({
            ...formData,
            [name]: image,
        });

        setUploadStatus({
            ...uploadStatus,
            [name]: true,
        });
    };

    const handleInputChange = (e) => {
        const { name, type, files, checked, value } = e.target;

        if (type === 'file') {
            const image = files[0];
            if (!image) return; // Handle case where no file is selected

            handleImageUpload(name, image);
        } else if (type === 'checkbox') {
            setFormData({
                ...formData,
                [name]: checked ? 1 : 0,
            });
        } else if (type === 'time') {
            const timeRegex = /^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$/;
            if (!value.match(timeRegex)) {
                alert('Please enter a valid time in the format HH:mm');
                return;
            }

            setFormData({
                ...formData,
                [name]: value,
            });
        } else {
            const inputValue = type === 'file' ? files[0] : value;

            setFormData({
                ...formData,
                [name]: inputValue,
            });
        }
    };

    const maxDescriptionLength = 300;
    const maxTipoLength = 20;
    const [negocios, setNegocios] = useState([]);
    const [tooltipVisible, setTooltipVisible] = useState(false);
    const [tooltipContent, setTooltipContent] = useState('');

    useEffect(() => {
        fetch('https://apideluna-production.up.railway.app/api/negocios')
            .then((response) => response.json())
            .then((data) => {

                const negociosData = data.map((negocio) => ({
                    id: negocio.id,
                    name: negocio.tituloNegocio,
                    image: negocio.imagenNegocio,
                }));
                setNegocios(negociosData);
            })
            .catch((error) => console.error('Error fetching data:', error));
    }, []);


    const handleHelpCircleHover = (content) => {
        setTooltipContent(content);
        setTooltipVisible(true);
    };

    const handleHelpCircleLeave = () => {
        setTooltipVisible(false);
    };

    const handleEdit = (user) => {

        navigate(`/dashboard/${user.id}`);
    };

    const handleDelete = async (negocio) => {
        try {
            const response = await fetch(`https://apideluna-production.up.railway.app/api/negocios/${negocio.id}`, {
                method: 'DELETE',
            });

            if (response.ok) {
                setNegocios((prevNegocios) => prevNegocios.filter((n) => n.id !== negocio.id));
                console.log('Negocio eliminado:', negocio);
            } else {
                console.error('Error al eliminar el negocio:', response.statusText);
            }
        } catch (error) {
            console.error('Error al eliminar el negocio:', error.message);
        }
    };



    const handleLogout = () => {
        document.cookie = 'yourCookieName=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        window.location.href = '/';
    };

    return (
        <div className="dashboard-container">
            <div className="dashboard-header">
                <img src="/100natural.png" alt="Logo" className="dashboard-logo" />
                <button className="logout-button" onClick={handleLogout}>
                    <LogOut size={20} />
                </button>
            </div>
            <div className="dashboard-content">
                <div className="form-container">
                    <form onSubmit={handleSubmit}>
                        <div className="image-input-container">

                            <div className="image-form-field">
                                <div className="label-container">
                                    <label className="form-label">Logo:</label>
                                    <HelpCircle
                                        size={24}
                                        color="#3498db"
                                        style={{ cursor: 'pointer', fontWeight: 'bold', marginRight: '10px' }}
                                        onMouseEnter={() => handleHelpCircleHover('Information about Logo')}
                                        onMouseLeave={handleHelpCircleLeave}
                                    />
                                </div>

                                <div className="file-input-container">
                                    <input type="file" name="logo" onChange={handleInputChange} className="form-input" accept="image/*" />
                                    {uploadStatus.logo && <Check color="green" size={20} className="form-input-checkmark" />}
                                </div>
                            </div>

                            <div className="image-form-field">
                                <div className="label-container">
                                    <label className="form-label">Imagen:</label>
                                    <HelpCircle
                                        size={24}
                                        color="#3498db"
                                        style={{ cursor: 'pointer', fontWeight: 'bold', marginRight: '10px' }}
                                        onMouseEnter={() => handleHelpCircleHover('Information about Imagen')}
                                        onMouseLeave={handleHelpCircleLeave}
                                    />
                                </div>

                                <div className="file-input-container">
                                    <input type="file" name="imagenReal" onChange={handleInputChange} className="form-input" accept="image/*" />
                                    {uploadStatus.imagenReal && <Check color="green" size={20} className="form-input-checkmark" />}
                                </div>
                            </div>

                        </div>
                        <div className="form-field">
                            <label className="form-label">Nombre:</label>
                            <input type="text" name="name" value={formData.name} onChange={handleInputChange} className="form-input" />
                        </div>
                        <div className="form-field">
                            <label className="form-label">Descripcion:</label>
                            <textarea
                                name="description"
                                value={formData.description}
                                onChange={handleInputChange}
                                className="form-textarea"
                                maxLength={maxDescriptionLength}
                                placeholder={`Max ${maxDescriptionLength} characters`}
                            ></textarea>
                        </div>

                        <div className="form-field">
                            <label className="form-label">Tipo de negocio:</label>
                            <input type="text" name="tipo" value={formData.tipo} onChange={handleInputChange} className="form-input"
                                   maxLength={maxTipoLength} />
                        </div>

                        <div className="form-field">
                            <label className="form-label">Direccion:</label>
                            <input type="text" name="address" value={formData.address} onChange={handleInputChange} className="form-input" />
                        </div>

                        <div className="form-field">
                            <label className="form-label">Categoria:</label>
                            <select name="dropdown" value={formData.dropdown} onChange={handleInputChange} className="form-input form-select">
                                <option value="Turismo Consciente">Turismo Consciente</option>
                                <option value="Agricultura Regenerativa">Agricultura Regenerativa</option>
                                <option value="Medicina Tradicional">Medicina Tradicional</option>
                                <option value="Bioconstruccion">Bioconstruccion</option>
                            </select>
                        </div>

                        <div className="form-field">
                            <label className="form-label">Apertura:</label>
                            <input type="time" name="openingTime" value={formData.openingTime} onChange={handleInputChange} className="form-input" />
                        </div>
                        <div className="form-field">
                            <label className="form-label">Cierre:</label>
                            <input type="time" name="closingTime" value={formData.closingTime} onChange={handleInputChange} className="form-input" />
                        </div>

                        <div className="form-field">
                            <label className="form-label">Latitud:</label>
                            <input type="text" name="latitude" value={formData.latitude} onChange={handleInputChange} className="form-input" />
                        </div>
                        <div className="form-field">
                            <label className="form-label">Longitud:</label>
                            <input type="text" name="longitude" value={formData.longitude} onChange={handleInputChange} className="form-input" />
                        </div>
                        <div className="form-field checkbox">
                            <label className="form-checkbox-label">Insignia:</label>
                            <input type="checkbox" name="checkbox" checked={formData.checkbox} onChange={handleInputChange} className="form-checkbox" />
                        </div>
                        <button type="submit" className="form-submit">Guardar</button>
                    </form>
                </div>

                <div className="list-view">
                    <ul>
                        {negocios.map((negocio) => (
                            <li key={negocio.id}>
                                <img
                                    src={negocio.image}
                                    alt={negocio.name}
                                    style={{ marginRight: '10px' }}
                                />
                                <span>{negocio.name}</span>
                                <button className="edit" onClick={() => handleEdit(negocio)}>
                                    <Edit size={26} />
                                </button>
                                <button className="delete" onClick={() => handleDelete(negocio)}>
                                    <Trash2 size={26} />
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>


            </div>
            {tooltipVisible && <Tooltip content={tooltipContent} />}
        </div>
    );
}

export default Dashboard;
