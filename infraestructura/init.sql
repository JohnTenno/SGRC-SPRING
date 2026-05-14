CREATE TABLE faculties (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE buildings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255)
);

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    faculty_id INT NOT NULL,
    enrollment VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255),
    role ENUM('STUDENT', 'TEACHER', 'ADMIN') DEFAULT 'STUDENT',
    FOREIGN KEY (faculty_id) REFERENCES faculties(id) ON DELETE RESTRICT
);

CREATE TABLE cubicles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    building_id INT NOT NULL,
    identifier VARCHAR(20) NOT NULL,
    capacity INT NOT NULL,
    status ENUM('AVAILABLE', 'MAINTENANCE', 'OUT_OF_SERVICE') DEFAULT 'AVAILABLE',
    FOREIGN KEY (building_id) REFERENCES buildings(id) ON DELETE CASCADE
);

CREATE TABLE reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    cubicle_id INT NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'NO_SHOW') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (cubicle_id) REFERENCES cubicles(id)
);

CREATE TABLE sanctions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO faculties (name) VALUES
('Engineering'), ('Accounting and Administration'), ('Law');

INSERT INTO buildings (name, location) VALUES
('Central Library', 'Campus 1'),
('Computer Center', 'Campus 2');

INSERT INTO users (faculty_id, enrollment, full_name, email, role) VALUES
(1, '367886', 'Admin Nico',        'a367886@uach.mx', 'ADMIN'),
(1, '374357', 'Admin JohnT',       'a374357@uach.mx', 'ADMIN'),
(1, '367651', 'Admin Samuel',      'a367651@uach.mx', 'ADMIN'),
(1, '231010', 'Student Juanito',   'a231010@uach.mx', 'STUDENT'),
(1, '102030', 'Teacher DeLira',    'a102030@uach.mx', 'TEACHER');

INSERT INTO cubicles (building_id, identifier, capacity) VALUES
(1, 'CB-01', 4), (1, 'CB-02', 6), (2, 'LAB-A', 3);
