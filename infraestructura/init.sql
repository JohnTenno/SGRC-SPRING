-- 1 tabla de Facultades
CREATE TABLE facultades (
    id_facultad INT AUTO_INCREMENT PRIMARY KEY,
    nombre_facultad VARCHAR(100) NOT NULL UNIQUE
);

-- 2 tabla de Edificios
CREATE TABLE edificios (
    id_edificio INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(255)
);

-- 3 tabla de usuarios
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    id_facultad INT NOT NULL,
    matricula VARCHAR(20) NOT NULL UNIQUE,
    nombre_completo VARCHAR(150) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    rol ENUM('ESTUDIANTE', 'DOCENTE', 'ADMIN') DEFAULT 'ESTUDIANTE',
    FOREIGN KEY (id_facultad) REFERENCES facultades(id_facultad) ON DELETE RESTRICT
);

-- 4 tabla de cubículos
CREATE TABLE cubiculos (
    id_cubiculo INT AUTO_INCREMENT PRIMARY KEY,
    id_edificio INT NOT NULL,
    identificador VARCHAR(20) NOT NULL,
    capacidad INT NOT NULL,
    estado ENUM('DISPONIBLE', 'MANTENIMIENTO', 'FUERA_DE_SERVICIO') DEFAULT 'DISPONIBLE',
    FOREIGN KEY (id_edificio) REFERENCES edificios(id_edificio) ON DELETE CASCADE
);

-- 5 tabla de reservaciones 
CREATE TABLE reservaciones (
    id_reservacion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_cubiculo INT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado_reservacion ENUM('PENDIENTE', 'ACTIVA', 'COMPLETADA', 'CANCELADA', 'NO_ASISTIO') DEFAULT 'PENDIENTE',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_cubiculo) REFERENCES cubiculos(id_cubiculo)
);

-- 6 tabla de sanciones
CREATE TABLE sanciones (
    id_sancion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

-----------------------------------inserts-----------------------------------

-- insertar facultades
INSERT INTO facultades (nombre_facultad) VALUES 
('Ingeniería'), ('Contaduría y Administración'), ('Derecho');

-- insertar edificios
INSERT INTO edificios (nombre, ubicacion) VALUES 
('Biblioteca Central', 'Campus 1'), 
('Centro de Cómputo', 'Campus 2');

-- insertar usuarios
INSERT INTO usuarios (id_facultad, matricula, nombre_completo, correo, rol) VALUES 
(1, '367886', 'Admin Nico', 'a367886@uach.mx', 'ADMIN'),
(1, '374357', 'Admin JohnT', 'a374357@uach.mx', 'ADMIN'),
(1, '367651', 'Admin Samuel', 'a367651@uach.mx', 'ADMIN'),
(1, '231010', 'ESTUDIANTE Juanito ', 'a374357@uach.mx', 'ESTUDIANTE'),
(1, '102030', 'DOCENTE DeLira', 'a102030@uach.mx', 'DOCENTE');

-- insertar cubículos
INSERT INTO cubiculos (id_edificio, identificador, capacidad) VALUES 
(1, 'CB-01', 4), (1, 'CB-02', 6), (2, 'LAB-A', 3);