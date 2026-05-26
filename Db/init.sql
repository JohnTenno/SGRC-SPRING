SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS sgrc_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sgrc_db;

-- =============================================================================
-- SGRC — Sistema de Gestión de Recursos del Centro de Cómputo
-- Script de inicialización de base de datos
--
-- Tablas activas (9):
--   faculty                       — institución raíz, escala a múltiples facultades
--   user                          — todos los roles del sistema (STUDENT, TEACHER, ADMIN, RECEPTOR)
--   cubicle                       — espacios físicos reservables con check-in por QR
--   reservation                   — reservaciones de cubículos (ciclo de vida completo)
--   equipment_type                — catálogo de equipo prestable con control de inventario
--   equipment_rental_request      — solicitudes digitales de préstamo de equipo
--   equipment_rental_request_item — ítems por solicitud (relación N:M desnormalizada)
--   penalty                       — sanciones automáticas y manuales por incumplimiento
--   tutoring_*  (4 tablas)        — módulo de tutorías: profesores, materias, ofertas y solicitudes
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS tutoring_request;
DROP TABLE IF EXISTS tutoring_offering;
DROP TABLE IF EXISTS tutoring_professor;
DROP TABLE IF EXISTS tutoring_subject;
DROP TABLE IF EXISTS penalty;
DROP TABLE IF EXISTS equipment_rental_request_item;
DROP TABLE IF EXISTS equipment_rental_request;
DROP TABLE IF EXISTS equipment_type;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS cubicle;
DROP TABLE IF EXISTS faculty;

SET FOREIGN_KEY_CHECKS = 1;


-- =============================================================================
-- Institución raíz del sistema.
-- Diseñada para escalar a múltiples facultades sin cambios de esquema.
-- En esta versión el sistema opera únicamente con la Facultad de Ingeniería.
-- =============================================================================
CREATE TABLE faculty (
    faculty_id  INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(120) NOT NULL,
    CONSTRAINT pk_faculty PRIMARY KEY (faculty_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================================================
-- Usuarios del sistema. Un único modelo cubre todos los roles:
--
--   STUDENT  — alumno regular; sujeto a restricciones de tiempo y sanciones.
--   TEACHER  — docente; tiempo extendido e inmune a sanciones automáticas de NO_SHOW.
--   ADMIN    — acceso total al sistema y al panel de administración.
--   RECEPTOR — usuario de sistema asignado al iPad de cada cubículo; solo puede
--              renderizar el QR de check-in, sin acceso a ningún otro módulo.
--
-- is_tutor             — cuando es 1 el alumno aparece en el directorio de tutores
--                        y puede recibir solicitudes de tutoría de otros alumnos.
-- is_blocked           — bloqueo activo por sanción; impide crear reservaciones
--                        y solicitudes de equipo. Se levanta automáticamente cuando
--                        todas las sanciones activas del usuario expiran.
-- must_change_password — marcador para forzar cambio de contraseña en el siguiente
--                        inicio de sesión. Actualmente persistido en DB pero no
--                        evaluado en la capa de aplicación; está preparado para
--                        ser implementado en un commit futuro.
-- =============================================================================
CREATE TABLE `user` (
    user_id              INT          NOT NULL AUTO_INCREMENT,
    faculty_id           INT          NOT NULL,
    first_name           VARCHAR(80)  NOT NULL,
    last_name            VARCHAR(80)  NOT NULL,
    email                VARCHAR(120) NOT NULL,
    enrollment           VARCHAR(20)  NOT NULL,
    password_hash        VARCHAR(255) NOT NULL,
    logo_url             VARCHAR(255) NOT NULL DEFAULT '',
    role                 ENUM('STUDENT','TEACHER','ADMIN','RECEPTOR')
                                      NOT NULL DEFAULT 'STUDENT',
    is_tutor             TINYINT(1)   NOT NULL DEFAULT 0,
    is_blocked           TINYINT(1)   NOT NULL DEFAULT 0,
    must_change_password TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_user        PRIMARY KEY (user_id),
    CONSTRAINT uq_email       UNIQUE (email),
    CONSTRAINT uq_enrollment  UNIQUE (enrollment),
    CONSTRAINT fk_usr_faculty FOREIGN KEY (faculty_id)
        REFERENCES faculty(faculty_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_role    ON `user`(role);
CREATE INDEX idx_user_blocked ON `user`(is_blocked);


-- =============================================================================
-- Espacios físicos reservables dentro del centro de cómputo.
--
-- qr_token   — identificador único del cubículo para el proceso de check-in.
--              El iPad (rol RECEPTOR) de cada cubículo despliega este token
--              en forma de código QR; el alumno lo escanea desde su dispositivo.
-- status     — refleja el estado operativo actual del cubículo en tiempo real.
--              Se actualiza automáticamente por el scheduler y el check-in.
--              AVAILABLE  → cubículo libre, aceptando reservaciones.
--              OCCUPIED   → check-in confirmado, cubículo en uso.
--              MAINTENANCE → fuera de servicio, no aparece para reservar.
-- =============================================================================
CREATE TABLE cubicle (
    cubicle_id   INT          NOT NULL AUTO_INCREMENT,
    name         VARCHAR(80)  NOT NULL,
    max_capacity INT          NOT NULL DEFAULT 6,
    status       ENUM('AVAILABLE','OCCUPIED','MAINTENANCE')
                              NOT NULL DEFAULT 'AVAILABLE',
    logo_url     VARCHAR(255) NOT NULL DEFAULT '',
    qr_token     VARCHAR(100) NOT NULL UNIQUE DEFAULT (UUID()),
    CONSTRAINT pk_cubicle   PRIMARY KEY (cubicle_id),
    CONSTRAINT chk_capacity CHECK (max_capacity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================================================
-- Transacción central del sistema. Registra cada solicitud de espacio físico.
--
-- Ciclo de vida del status:
--   PENDING  → creada por el alumno, pendiente de confirmación del sistema.
--   APPROVED → aprobada automáticamente al no haber conflicto de horario.
--              El scheduler programa el check de no-show 15 min después del inicio.
--   ACTIVE   → check-in realizado por el alumno; el cubículo pasa a OCCUPIED.
--              El scheduler programa el checkout automático al llegar end_time.
--   COMPLETED → tiempo de reservación agotado; el cubículo vuelve a AVAILABLE.
--   CANCELLED → cancelada por el alumno (con ≥1 hora de anticipación) o por el
--              sistema ante un NO_SHOW (sin check-in en los primeros 15 minutos).
-- =============================================================================
CREATE TABLE reservation (
    reservation_id   INT  NOT NULL AUTO_INCREMENT,
    user_id          INT  NOT NULL,
    cubicle_id       INT  NOT NULL,
    reservation_date DATE NOT NULL,
    start_time       TIME NOT NULL,
    end_time         TIME NOT NULL,
    status           ENUM('PENDING','APPROVED','ACTIVE','COMPLETED','CANCELLED')
                          NOT NULL DEFAULT 'PENDING',
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_reservation PRIMARY KEY (reservation_id),
    CONSTRAINT fk_res_user    FOREIGN KEY (user_id)
        REFERENCES `user`(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_res_cubicle FOREIGN KEY (cubicle_id)
        REFERENCES cubicle(cubicle_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_times      CHECK (end_time > start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_res_user    ON reservation(user_id);
CREATE INDEX idx_res_cubicle ON reservation(cubicle_id, reservation_date, start_time, end_time);
CREATE INDEX idx_res_status  ON reservation(status);


-- =============================================================================
-- Catálogo de tipos de equipo prestable.
-- Maneja inventario por cantidad (tipo), no por unidad individual.
--
-- total_stock — unidades disponibles en tiempo real. Se decrementa cuando una
--              solicitud pasa a ACTIVE y se restaura al pasar a RETURNED o CANCELLED.
--              El constraint CHECK garantiza que nunca sea negativo a nivel de DB.
-- =============================================================================
CREATE TABLE equipment_type (
    equipment_type_id INT          NOT NULL AUTO_INCREMENT,
    name              VARCHAR(80)  NOT NULL,
    description       VARCHAR(255),
    logo_url          VARCHAR(255) NOT NULL DEFAULT '',
    total_stock       INT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_equipment_type PRIMARY KEY (equipment_type_id),
    CONSTRAINT chk_total_stock   CHECK (total_stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================================================
-- Solicitud digital de préstamo de equipo iniciada por el alumno.
--
-- Ciclo de vida del status:
--   PENDING_PICKUP → solicitud creada, esperando que el alumno recoja en mostrador.
--   ACTIVE         → equipo entregado físicamente por el administrador en mostrador.
--   RETURNED       → equipo devuelto; el stock se restaura automáticamente.
--   CANCELLED      → cancelada por el alumno o por el administrador.
-- =============================================================================
CREATE TABLE equipment_rental_request (
    request_id INT       NOT NULL AUTO_INCREMENT,
    user_id    INT       NOT NULL,
    status     ENUM('PENDING_PICKUP','ACTIVE','RETURNED','CANCELLED')
                         NOT NULL DEFAULT 'PENDING_PICKUP',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_equipment_rental_request PRIMARY KEY (request_id),
    CONSTRAINT fk_erq_user FOREIGN KEY (user_id)
        REFERENCES `user`(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_erq_user   ON equipment_rental_request(user_id);
CREATE INDEX idx_erq_status ON equipment_rental_request(status);


-- =============================================================================
-- Ítems de la solicitud de préstamo.
-- Una solicitud puede incluir múltiples tipos de equipo en una sola transacción.
-- Ejemplo: 1 laptop + 2 marcadores = 2 filas en esta tabla con el mismo request_id.
-- =============================================================================
CREATE TABLE equipment_rental_request_item (
    item_id           INT NOT NULL AUTO_INCREMENT,
    request_id        INT NOT NULL,
    equipment_type_id INT NOT NULL,
    quantity          INT NOT NULL DEFAULT 1,
    CONSTRAINT pk_erq_item   PRIMARY KEY (item_id),
    CONSTRAINT fk_erqi_req   FOREIGN KEY (request_id)
        REFERENCES equipment_rental_request(request_id) ON DELETE CASCADE,
    CONSTRAINT fk_erqi_equip FOREIGN KEY (equipment_type_id)
        REFERENCES equipment_type(equipment_type_id) ON DELETE RESTRICT,
    CONSTRAINT chk_erqi_qty  CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================================================
-- Sanciones aplicadas a los usuarios por incumplimiento de las reglas del sistema.
--
-- Tipos y su origen:
--   NO_SHOW          — automática. El scheduler la genera cuando el alumno no realiza
--                      check-in dentro de los 15 minutos de tolerancia tras el inicio
--                      de su reservación. No aplica a usuarios con rol TEACHER.
--   LATE_CANCELLATION — automática. Se genera cuando el alumno cancela con menos de
--                      60 minutos de anticipación al inicio de la reservación.
--                      (Nota: actualmente el sistema rechaza cancelaciones tardías
--                      con error; este tipo queda reservado para implementación futura.)
--   DISORDER         — manual. El administrador la aplica por mal uso del espacio.
--   LATE_RETURN      — manual. El administrador la aplica por devolución tardía de equipo.
--
-- manager_admin_id — es NULL en sanciones automáticas generadas por el scheduler.
-- is_active        — cuando es 1, el usuario tiene is_blocked = 1 en la tabla user.
--                    El scheduler de Spring evalúa las sanciones expiradas y
--                    desbloquea al usuario cuando todas sus penaltys activas
--                    han superado su end_date.
-- =============================================================================
CREATE TABLE penalty (
    penalty_id       INT          NOT NULL AUTO_INCREMENT,
    user_id          INT          NOT NULL,
    manager_admin_id INT          NULL,
    type             ENUM('LATE_RETURN','DISORDER','NO_SHOW','LATE_CANCELLATION')
                                  NOT NULL,
    reason           VARCHAR(255) NOT NULL,
    start_date       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date         TIMESTAMP    NOT NULL,
    is_active        TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_penalty        PRIMARY KEY (penalty_id),
    CONSTRAINT fk_penal_user     FOREIGN KEY (user_id)
        REFERENCES `user`(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_penal_admin    FOREIGN KEY (manager_admin_id)
        REFERENCES `user`(user_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_penalty_dates CHECK (end_date > start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_penal_user ON penalty(user_id, is_active);


-- =============================================================================
-- MÓDULO DE TUTORÍAS
-- Cuatro tablas independientes que gestionan el directorio de tutores,
-- el catálogo de materias, las ofertas de horario y las solicitudes de alumnos.
-- =============================================================================

-- Materias disponibles para tutoría. Catálogo maestro mantenido por el admin.
CREATE TABLE tutoring_subject (
    subject_id  INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(120) NOT NULL,
    description TEXT,
    CONSTRAINT pk_tutoring_subject PRIMARY KEY (subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Profesores y alumnos-tutores registrados en el módulo de tutorías.
-- employee_number es el PK natural; para alumnos-tutores corresponde a su matrícula.
CREATE TABLE tutoring_professor (
    employee_number VARCHAR(20)  NOT NULL,
    first_name      VARCHAR(80)  NOT NULL,
    last_name       VARCHAR(80)  NOT NULL,
    bio             TEXT,
    CONSTRAINT pk_tutoring_professor PRIMARY KEY (employee_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Configuración de disponibilidad de cada tutor.
-- Relación 1:1 con tutoring_professor (se crea automáticamente al registrar al tutor).
--
-- available_weekdays — lista serializada como JSON de días disponibles
--                      Ej: '["MONDAY","WEDNESDAY","FRIDAY"]'
-- subject_ids        — lista serializada de IDs de tutoring_subject que imparte
--                      Ej: '[1,3,5]'
-- Nota: La serialización en TEXT con conversor JPA es intencional para mantener
-- la simplicidad del modelo sin tablas pivot adicionales.
CREATE TABLE tutoring_offering (
    employee_number   VARCHAR(20)  NOT NULL,
    schedule_summary  VARCHAR(50),
    tutoring_location VARCHAR(120),
    available_weekdays TEXT,
    subject_ids        TEXT,
    CONSTRAINT pk_tutoring_offering  PRIMARY KEY (employee_number),
    CONSTRAINT fk_offering_professor FOREIGN KEY (employee_number)
        REFERENCES tutoring_professor(employee_number) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Solicitudes de tutoría enviadas por alumnos a un tutor específico.
--
-- Ciclo de vida del status:
--   PENDING   → solicitud enviada, esperando respuesta del tutor.
--   CONFIRMED → tutor aceptó la sesión.
--   REJECTED  → tutor rechazó la solicitud.
--   COMPLETED → sesión realizada.
--   CANCELLED → cancelada por el alumno antes de la fecha.
CREATE TABLE tutoring_request (
    tutoring_request_id      INT         NOT NULL AUTO_INCREMENT,
    student_enrollment       VARCHAR(20) NOT NULL,
    professor_employee_number VARCHAR(20) NOT NULL,
    subject                  VARCHAR(120) NOT NULL,
    reservation_date         DATE        NOT NULL,
    start_time               VARCHAR(5)  NOT NULL,
    end_time                 VARCHAR(5)  NOT NULL,
    topic                    TEXT,
    status                   ENUM('PENDING','CONFIRMED','REJECTED','COMPLETED','CANCELLED')
                                         NOT NULL DEFAULT 'PENDING',
    created_at               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_tutoring_request PRIMARY KEY (tutoring_request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_treq_student   ON tutoring_request(student_enrollment);
CREATE INDEX idx_treq_professor ON tutoring_request(professor_employee_number);
CREATE INDEX idx_treq_status    ON tutoring_request(status);


-- =============================================================================
-- DML — Datos iniciales del sistema
-- Contraseña de todos los usuarios de prueba: password123
-- Hash BCrypt costo 10: $2a$10$KRAAi6/KflfRkNZwF8hh4u.cdNqXcie2MUgXNRnRYK1l5Qg1yVKc2
-- =============================================================================

INSERT INTO faculty (faculty_id, name) VALUES
    (1, 'Facultad de Ingeniería');


-- 6 cubículos de la biblioteca (RF-01).
-- El cubículo 6 inicia en MAINTENANCE para demostrar ese estado en la UI.
INSERT INTO cubicle (cubicle_id, name, max_capacity, status, logo_url) VALUES
    (1, 'Cubículo Biblioteca 01', 6, 'AVAILABLE',   'https://media.tenor.com/ZY16_IxQsMQAAAAM/arknights-endfield.gif'),
    (2, 'Cubículo Biblioteca 02', 4, 'AVAILABLE',   'https://media.tenor.com/IwcY6lQD0_YAAAAM/fluorite-endfield.gif'),
    (3, 'Cubículo Biblioteca 03', 6, 'AVAILABLE',   'https://i.pinimg.com/originals/87/72/3b/87723b4da84a23daa24f5a573d4f54c3.gif'),
    (4, 'Cubículo Biblioteca 04', 4, 'AVAILABLE',   'https://i.pinimg.com/originals/63/32/46/633246a5ded88d206fb996a1b82126c4.gif'),
    (5, 'Cubículo Biblioteca 05', 6, 'AVAILABLE',   'https://i.makeagif.com/media/12-23-2024/obnhZt.gif'),
    (6, 'Cubículo Biblioteca 06', 4, 'MAINTENANCE', 'https://upload-os-bbs.hoyolab.com/upload/2024/10/16/317695012/38ee29b69f1283b9b9fb81c704b4346a_4827335672541980155.gif');


-- Usuarios de prueba. Cada combinación de rol e indicadores cubre un caso
-- de uso distinto para la demostración del sistema:
--
--   id  matrícula  rol       tutor  bloq.  change_pwd  descripción
--   1   ADM001     ADMIN     no     no     no          administrador principal
--   2   367886     STUDENT   no     no     no          alumno activo sin restricciones
--   3   374357     STUDENT   sí     no     no          alumno con privilegios de tutor
--   4   367651     STUDENT   no     sí     no          alumno bloqueado por sanción activa
--   5   EMP001     TEACHER   no     no     no          docente activo
--   9   RCP001     RECEPTOR  no     no     no          iPad del cubículo 01
INSERT INTO `user`
    (user_id, faculty_id, first_name, last_name, email, enrollment,
     password_hash, logo_url, role, is_tutor, is_blocked, must_change_password)
VALUES
    (1, 1, 'Laura',    'Mendoza Rios',       'admin@uach.mx',        'ADM001',
     '$2a$10$KRAAi6/KflfRkNZwF8hh4u.cdNqXcie2MUgXNRnRYK1l5Qg1yVKc2',
     'https://cdn.pixabay.com/photo/2018/11/13/21/43/avatar-3814049_960_720.png',
     'ADMIN',    0, 0, 0),

    (2, 1, 'Nicolas',  'Nevarez Loera',      'a367886@uach.mx',      '367886',
     '$2a$10$KRAAi6/KflfRkNZwF8hh4u.cdNqXcie2MUgXNRnRYK1l5Qg1yVKc2',
     'https://cdn.pixabay.com/photo/2018/11/13/21/43/avatar-3814049_960_720.png',
     'STUDENT',  0, 0, 0),

    (3, 1, 'Jonathan', 'Gandara Salazar',    'a374357@uach.mx',      '374357',
     '$2a$10$KRAAi6/KflfRkNZwF8hh4u.cdNqXcie2MUgXNRnRYK1l5Qg1yVKc2',
     'https://cdn.pixabay.com/photo/2018/11/13/21/43/avatar-3814049_960_720.png',
     'STUDENT',  1, 0, 0),

    (4, 1, 'Samuel',   'Garcia Gomez',       'a367651@uach.mx',      '367651',
     '$2a$10$KRAAi6/KflfRkNZwF8hh4u.cdNqXcie2MUgXNRnRYK1l5Qg1yVKc2',
     'https://cdn.pixabay.com/photo/2018/11/13/21/43/avatar-3814049_960_720.png',
     'STUDENT',  0, 1, 0),

    (5, 1, 'Marco',    'Herrera Bustamante', 'm.herrera@uach.mx',    'EMP001',
     '$2a$10$KRAAi6/KflfRkNZwF8hh4u.cdNqXcie2MUgXNRnRYK1l5Qg1yVKc2',
     'https://cdn.pixabay.com/photo/2018/11/13/21/43/avatar-3814049_960_720.png',
     'TEACHER',  0, 0, 0),

    (9, 1, 'iPad',     'Cubículo 01',        'receptor.c01@uach.mx', 'RCP001',
     '$2a$10$KRAAi6/KflfRkNZwF8hh4u.cdNqXcie2MUgXNRnRYK1l5Qg1yVKc2',
     'https://cdn.pixabay.com/photo/2018/11/13/21/43/avatar-3814049_960_720.png',
     'RECEPTOR', 0, 0, 0);


-- Sanción de demo activa para el alumno bloqueado (user_id = 4).
-- Simula un NO_SHOW ocurrido ayer; el bloqueo expira en 2 días.
-- El administrador es NULL porque las sanciones de NO_SHOW son automáticas.
INSERT INTO penalty
    (user_id, manager_admin_id, type, reason, start_date, end_date, is_active)
VALUES
    (4, NULL, 'NO_SHOW',
     'No se presentó al cubículo dentro de los 15 minutos de tolerancia. Reservación de demostración.',
     DATE_SUB(NOW(), INTERVAL 1 DAY),
     DATE_ADD(NOW(),  INTERVAL 2 DAY),
     1);


-- Inventario inicial de equipo prestable.
INSERT INTO equipment_type
    (equipment_type_id, name, description, logo_url, total_stock)
VALUES
    (1, 'Laptop',                  'Laptop para uso académico',                                '', 4),
    (2, 'Proyector',               'Proyector HDMI para presentaciones',                       '', 3),
    (3, 'Marcadores',              'Marcadores para pizarrón, juego de 4 colores',             '', 15),
    (4, 'Borrador para pizarrón',  'Borrador estándar para pizarrón blanco',                   '', 15),
    (5, 'Webcam',                  'Cámara web HD para videoconferencias',                     '', 3),
    (6, 'Calculadora científica',  'Calculadora científica para matemáticas e ingeniería',     '', 6),
    (7, 'Multímetro Digital',      'Multímetro TruRMS para prácticas de circuitos',            '', 10),
    (8, 'Estación de Soldadura',   'Cautín de temperatura regulable (incluye base y estaño)',  '', 8);


-- Tutor de demostración para el módulo de tutorías.
-- Vinculado al alumno user_id=3 (Jonathan Gandara, is_tutor=1).
INSERT INTO tutoring_professor
    (employee_number, first_name, last_name, bio)
VALUES
    ('374357', 'Jonathan', 'Gandara Salazar',
     'Alumno de 8vo semestre de Ingeniería en Sistemas. Disponible para tutorías en Cálculo, Programación y Bases de Datos.');

INSERT INTO tutoring_offering
    (employee_number, schedule_summary, tutoring_location, available_weekdays, subject_ids)
VALUES
    ('374357', 'Lunes y Miércoles 14:00–16:00', 'Cubículo Biblioteca 03',
     '["MONDAY","WEDNESDAY"]', '[1,2]');


-- Materias de demostración para el catálogo de tutorías.
INSERT INTO tutoring_subject (subject_id, name, description) VALUES
    (1, 'Cálculo Diferencial',    'Límites, derivadas y sus aplicaciones en ingeniería.'),
    (2, 'Bases de Datos',         'Modelado relacional, SQL, normalización y transacciones.'),
    (3, 'Programación Orientada a Objetos', 'Clases, herencia, polimorfismo y patrones de diseño.'),
    (4, 'Álgebra Lineal',         'Vectores, matrices, transformaciones y espacios vectoriales.'),
    (5, 'Redes de Computadoras',  'Modelos OSI/TCP-IP, protocolos y configuración de redes.');


-- =============================================================================
-- Verificación rápida al final de la carga
-- =============================================================================
SELECT '=== RESUMEN DE CARGA ===' AS '';

SELECT 'Cubículos por estado'    AS tabla,
       status                    AS valor,
       COUNT(*)                  AS total
FROM cubicle GROUP BY status;

SELECT 'Usuarios por rol'        AS tabla,
       role                      AS valor,
       SUM(is_tutor)             AS tutores,
       SUM(is_blocked)           AS bloqueados,
       COUNT(*)                  AS total
FROM `user` GROUP BY role;

SELECT 'Inventario de equipo'    AS tabla,
       name                      AS equipo,
       total_stock
FROM equipment_type;

SELECT 'Tutores registrados'     AS tabla,
       tp.employee_number,
       tp.first_name,
       tp.last_name,
       COUNT(tr.tutoring_request_id) AS solicitudes
FROM tutoring_professor tp
LEFT JOIN tutoring_request tr ON tr.professor_employee_number = tp.employee_number
GROUP BY tp.employee_number, tp.first_name, tp.last_name;

SELECT 'Materias disponibles'    AS tabla,
       name                      AS materia
FROM tutoring_subject;