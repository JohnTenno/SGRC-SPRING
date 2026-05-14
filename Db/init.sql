
-- Desactivar checks para limpieza profunda si es necesario
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS penalties, chat_messages, tutoring_sessions, equipment_loans, 
                 space_reservations, equipments, spaces, tutor_requests, 
                 subjects, users, buildings, faculties, campuses;

-- Ubicación
CREATE TABLE campuses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255)
);

CREATE TABLE faculties (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    campus_id INT,
    FOREIGN KEY (campus_id) REFERENCES campuses(id)
);

CREATE TABLE buildings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    faculty_id INT,
    FOREIGN KEY (faculty_id) REFERENCES faculties(id)
);

-- Usuarios y Roles
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'TEACHER', 'STAFF', 'ADMIN') NOT NULL,
    is_tutor BOOLEAN DEFAULT FALSE,
    faculty_id INT,
    FOREIGN KEY (faculty_id) REFERENCES faculties(id)
);

-- Gestión Académica
CREATE TABLE subjects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    faculty_id INT,
    FOREIGN KEY (faculty_id) REFERENCES faculties(id)
);

CREATE TABLE tutor_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT,
    subject_id INT,
    gpa FLOAT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    admin_id INT,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (admin_id) REFERENCES users(id)
);

-- Recursos
CREATE TABLE spaces (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    `type` ENUM('CUBICLE', 'ROOM', 'LABORATORY') NOT NULL,
    `status` ENUM('FREE', 'OCCUPIED', 'MAINTENANCE') DEFAULT 'FREE',
    building_id INT,
    teacher_qr TEXT,
    FOREIGN KEY (building_id) REFERENCES buildings(id)
);

CREATE TABLE equipments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    `type` VARCHAR(255) NOT NULL,
    model VARCHAR(255),
    total_stock INT NOT NULL,
    available_stock INT NOT NULL,
    faculty_id INT,
    `status` ENUM('ACTIVE', 'MAINTENANCE', 'RETIRED') DEFAULT 'ACTIVE',
    FOREIGN KEY (faculty_id) REFERENCES faculties(id)
);

-- Transacciones
CREATE TABLE space_reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    space_id INT,
    reservation_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    purpose TEXT,
    status ENUM('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (space_id) REFERENCES spaces(id)
);

CREATE TABLE equipment_loans (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    equipment_id INT,
    collection_folio VARCHAR(100) UNIQUE,
    loan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estimated_return TIMESTAMP,
    actual_return TIMESTAMP NULL,
    staff_id INT,
    status ENUM('REQUESTED', 'DELIVERED', 'RETURNED', 'OVERDUE') DEFAULT 'REQUESTED',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (equipment_id) REFERENCES equipments(id),
    FOREIGN KEY (staff_id) REFERENCES users(id)
);

-- Tutorías y Comunicación
CREATE TABLE tutoring_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    requester_id INT,
    tutor_id INT,
    subject_id INT,
    session_datetime TIMESTAMP NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'ABSENT') DEFAULT 'PENDING',
    initial_doubt TEXT,
    FOREIGN KEY (requester_id) REFERENCES users(id),
    FOREIGN KEY (tutor_id) REFERENCES users(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

CREATE TABLE chat_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tutoring_id INT,
    sender_id INT,
    content TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tutoring_id) REFERENCES tutoring_sessions(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- Control
CREATE TABLE penalties (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    type ENUM('STUDENT_ABSENCE', 'TEACHER_ABSENCE', 'LATE_EQUIPMENT') NOT NULL,
    strike_points INT DEFAULT 1,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

SET FOREIGN_KEY_CHECKS = 1;



-- Inserción de datos

-- Campus
INSERT INTO campuses (name, location) VALUES 
('Campus 1', 'Chihuahua, Chih.'),
('Campus 2', 'Chihuahua, Chih.'),
('Campus Parral', 'Parral, Chih.'),
('Campus Delicias', 'Delicias, Chih.');

-- Facultades
INSERT INTO faculties (name, campus_id) VALUES 
('Facultad de Ingenieria', 2),
('Facultad de Ciencias Quimicas', 2),
('Facultad de Contaduría y Administracion', 2),
('Facultad de Derecho', 1),
('Facultad de Filosofia y Letras', 1),
('Facultad de Artes', 1),
('Facultad de Medicina y Ciencias Biomedicas', 2),
('Facultad de Odontología', 1),
('Facultad de Ciencias Politicas y Sociales', 2),
('Facultad de Ciencias de la Cultura Fisica', 2),
('Facultad de Enfermeria y Nutriologia', 2),
('Facultad de Zootecnia y Ecologia', 2),
('Facultad de Ciencias Agricolas y Forestales', 4),
('Facultad de Ciencias Agrotecnologicas', 1),
('Facultad de Economía Internacional', 3);

-- Usuarios Iniciales
INSERT INTO users (enrollment, name, email, password, role, is_tutor, faculty_id) VALUES 
('367886', 'Nicolas Nevarez', 'a367886@uach.mx', 'password123', 'ADMIN', false, 1),
('374357', 'Jonathan Gandara', 'a374357@uach.mx', 'password123', 'STUDENT', true, 1),
('367651', 'Samuel García ', 'a367651@uach.mx', 'password123', 'STUDENT', false, 1),
('123122', 'Maestro Juan ', 'a123122@uach.mx', 'password123', 'TEACHER', true, 1),
('100100', 'Encargado Biblioteca', 'staff@uach.mx', 'password123', 'STAFF', false, 1);


-- Infraestructura
INSERT INTO buildings (name, faculty_id) VALUES 
('Biblioteca Compartida (ING-QUI)', 1);

INSERT INTO spaces (name, capacity, `type`, `status`, building_id) VALUES 
('Cubículo Biblioteca 01', 4, 'CUBICLE', 'FREE', 1),
('Cubículo Biblioteca 02', 4, 'CUBICLE', 'FREE', 1),
('Cubículo Biblioteca 03', 4, 'CUBICLE', 'FREE', 1),
('Cubículo Biblioteca 04', 6, 'CUBICLE', 'FREE', 1),
('Cubículo Biblioteca 05', 6, 'CUBICLE', 'FREE', 1),
('Cubículo Biblioteca 06', 2, 'CUBICLE', 'FREE', 1);

-- Catálogo Académico y Equipo
INSERT INTO subjects (name, faculty_id) VALUES 
('Bases de Datos', 1),
('Programación Orientada a Objetos', 1),
('Química Orgánica', 2);

INSERT INTO equipments (type, model, total_stock, available_stock, faculty_id, `status`) VALUES 
('Computer', 'Generic', 10, 10, 1, 'ACTIVE'),
('Proyector', 'Epson PowerLite', 5, 5, 1, 'ACTIVE');

-- Ejemplos Transaccionales (Usando subconsultas para mayor seguridad)
INSERT INTO space_reservations (user_id, space_id, reservation_date, start_time, end_time, purpose, status) 
VALUES (
    (SELECT id FROM users WHERE enrollment = '374357' LIMIT 1),
    (SELECT id FROM spaces WHERE name = 'Cubículo Biblioteca 01' LIMIT 1),
    CURDATE() + INTERVAL 1 DAY, '10:00:00', '12:00:00', 'Proyecto de Bases de Datos', 'ACTIVE'
);

INSERT INTO equipment_loans (user_id, equipment_id, collection_folio, staff_id, status) 
VALUES (
    (SELECT id FROM users WHERE enrollment = '374357' LIMIT 1),
    (SELECT id FROM equipments WHERE type = 'Laptop' LIMIT 1),
    'FOLIO-UACH-2026',
    (SELECT id FROM users WHERE role = 'STAFF' LIMIT 1),
    'DELIVERED'
);

INSERT INTO penalties (user_id, type, strike_points, is_active) 
VALUES (
    (SELECT id FROM users WHERE enrollment = '374357' LIMIT 1),
    'LATE_EQUIPMENT', 1, true
);
