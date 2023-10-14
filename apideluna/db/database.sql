CREATE DATABASE trenmayadb;

USE trenmayadb;

-- DROP DATABASE trenmayadb;

CREATE TABLE negocios (
	id INT AUTO_INCREMENT PRIMARY KEY,
	imagenNegocio VARCHAR(255),
    tituloNegocio VARCHAR(255) NOT NULL,
	disponible VARCHAR(255),
    distancia VARCHAR(255),
    imagenCategoria VARCHAR(255),
    descripcion VARCHAR(300),
    insignia BOOLEAN,
    tipoNegocio VARCHAR(255),
    direccion VARCHAR(255),
    imagenRealNegocio VARCHAR(255),
    nombreCategoria VARCHAR(255),
    horario VARCHAR(255),
    latitud DOUBLE,
    longitud DOUBLE
);


CREATE TABLE admin (
    user VARCHAR(255) NOT NULL PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

INSERT INTO admin (user, password)
VALUES ('admin', SHA1('deluna123'));

SELECT * FROM admin;
SELECT * FROM negocios;




