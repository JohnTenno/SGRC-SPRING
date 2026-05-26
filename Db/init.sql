SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS sgrc_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sgrc_db;

-- =============================================================================
-- DDL — Definicion de tablas
-- Orden de eliminacion respetando restricciones de llaves foraneas
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS import_log;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS penalty;
DROP TABLE IF EXISTS loan;
DROP TABLE IF EXISTS equipment_rental_request_item;
DROP TABLE IF EXISTS equipment_rental_request;
DROP TABLE IF EXISTS equipment_category;
DROP TABLE IF EXISTS equipment_type;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS cubicle;
DROP TABLE IF EXISTS faculty;

SET FOREIGN_KEY_CHECKS = 1;


-- =============================================================================
-- Institucion raiz del sistema. Permite escalar a otras facultades en el futuro.
-- =============================================================================
CREATE TABLE faculty (
    faculty_id  INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(120) NOT NULL,
    CONSTRAINT pk_faculty PRIMARY KEY (faculty_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================================================
-- Espacios fisicos reservables dentro de la biblioteca.
-- El campo qr_token identifica de forma unica al cubiculo para el check-in
-- mediante el dispositivo RECEPTOR (iPad) que se instalara en cada espacio.
-- Estados posibles: AVAILABLE | OCCUPIED | MAINTENANCE
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
-- Usuarios del sistema. Un mismo modelo cubre todos los roles:
--   STUDENT  : alumno regular, sujeto a restricciones de tiempo y sanciones.
--   TEACHER  : docente, con extension de tiempo e inmunidad a sanciones automaticas.
--   ADMIN    : administrador con acceso total al sistema.
--   RECEPTOR : usuario de sistema asignado al iPad de cada cubiculo;
--              solo puede renderizar el codigo QR, sin acceso a ningun otro modulo.
--
-- is_tutor     : cuando vale 1, el usuario (alumno o docente) obtiene privilegios
--                extendidos de duracion y prioridad en conflictos de reserva.
-- is_blocked   : bloqueo activo por sancion; impide crear reservas y prestamos.
-- must_change_password : forzado en el primer inicio de sesion.
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
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_user        PRIMARY KEY (user_id),
    CONSTRAINT uq_email       UNIQUE (email),
    CONSTRAINT uq_enrollment  UNIQUE (enrollment),
    CONSTRAINT fk_usr_faculty FOREIGN KEY (faculty_id)
        REFERENCES faculty(faculty_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_role    ON `user`(role);
CREATE INDEX idx_user_blocked ON `user`(is_blocked);


-- =============================================================================
-- Transaccion central del sistema. Registra cada solicitud de espacio.
-- Ciclo de vida: PENDING -> APPROVED -> ACTIVE -> COMPLETED
--                PENDING -> CANCELLED  |  APPROVED -> CANCELLED
--
-- cancellation_penalty : marca si la cancelacion genero una sancion automatica
--                        (LATE_CANCELLATION o NO_SHOW).
-- =============================================================================
CREATE TABLE reservation (
    reservation_id     INT  NOT NULL AUTO_INCREMENT,
    user_id            INT  NOT NULL,
    cubicle_id         INT  NOT NULL,
    reservation_date   DATE NOT NULL,
    start_time         TIME NOT NULL,
    end_time           TIME NOT NULL,
    status             ENUM('PENDING','APPROVED','ACTIVE','COMPLETED','CANCELLED')
                            NOT NULL DEFAULT 'PENDING',
    cancellation_penalty TINYINT(1) NOT NULL DEFAULT 0,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
-- Catalogo de categorias de equipo. Controla los valores validos de clasificacion
-- y sirve como fuente de datos para los filtros del frontend.
-- El campo slug es la clave que viaja en los parametros de la URL (categoria=computo).
-- =============================================================================
CREATE TABLE equipment_category (
    category_id INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(80)  NOT NULL,
    slug        VARCHAR(40)  NOT NULL,
    logo_url    VARCHAR(255) NOT NULL DEFAULT '',
    CONSTRAINT pk_equipment_category PRIMARY KEY (category_id),
    CONSTRAINT uq_category_slug      UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================================================
-- Tipos de equipo prestable. Maneja inventario por cantidad, no por unidad individual.
-- Los campos total_stock y available_stock se actualizan transaccionalmente en cada
-- prestamo y devolucion.
-- Estados posibles: AVAILABLE | DAMAGED | OUT_OF_SERVICE
-- Un tipo en estado DAMAGED o OUT_OF_SERVICE no aparece disponible para prestamo.
-- =============================================================================
CREATE TABLE equipment_type (
    equipment_type_id INT          NOT NULL AUTO_INCREMENT,
    category_id       INT          NOT NULL,
    name              VARCHAR(80)  NOT NULL,
    description       VARCHAR(255),
    logo_url          VARCHAR(255) NOT NULL DEFAULT '',
    status            ENUM('AVAILABLE','DAMAGED','OUT_OF_SERVICE')
                                   NOT NULL DEFAULT 'AVAILABLE',
    total_stock       INT          NOT NULL DEFAULT 0,
    available_stock   INT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_equipment_type  PRIMARY KEY (equipment_type_id),
    CONSTRAINT fk_equip_category  FOREIGN KEY (category_id)
        REFERENCES equipment_category(category_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_total_stock    CHECK (total_stock >= 0),
    CONSTRAINT chk_avail_stock    CHECK (available_stock >= 0),
    CONSTRAINT chk_stock_max      CHECK (available_stock <= total_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_equip_category ON equipment_type(category_id);


-- =============================================================================
-- Solicitud digital de prestamo iniciada por el alumno desde su dispositivo.
-- Un administrador o LAB_ADMIN la convierte en un prestamo formal al confirmar
-- la entrega fisica en mostrador.
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


-- =============================================================================
-- Lineas de la solicitud de prestamo. Una solicitud puede incluir
-- multiples tipos de equipo en una sola transaccion.
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
-- Registro formal del prestamo confirmado en mostrador por el administrador.
-- Toda entrega de equipo requiere una reservacion activa (APPROVED o ACTIVE)
-- del mismo usuario. Para proyectores, id_card_handed debe ser 1.
-- Estados posibles: PENDING_PICKUP | ACTIVE | RETURNED | DAMAGED
-- =============================================================================
CREATE TABLE loan (
    loan_id              INT          NOT NULL AUTO_INCREMENT,
    user_id              INT          NOT NULL,
    reservation_id       INT          NOT NULL,
    equipment_type_id    INT          NOT NULL,
    quantity             INT          NOT NULL DEFAULT 1,
    loan_date            TIMESTAMP    NULL,
    expected_return_date TIMESTAMP    NULL,
    actual_return_date   TIMESTAMP    NULL,
    status               ENUM('PENDING_PICKUP','ACTIVE','RETURNED','DAMAGED')
                                      NOT NULL DEFAULT 'PENDING_PICKUP',
    id_card_handed       TINYINT(1)   NOT NULL DEFAULT 0,
    notes                VARCHAR(255),
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_loan             PRIMARY KEY (loan_id),
    CONSTRAINT fk_loan_user        FOREIGN KEY (user_id)
        REFERENCES `user`(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_loan_reservation FOREIGN KEY (reservation_id)
        REFERENCES reservation(reservation_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_loan_equip_type  FOREIGN KEY (equipment_type_id)
        REFERENCES equipment_type(equipment_type_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_loan_qty        CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_loan_user   ON loan(user_id);
CREATE INDEX idx_loan_status ON loan(status);


-- =============================================================================
-- Sanciones aplicadas a los usuarios. Pueden ser automaticas o manuales.
--   NO_SHOW          : el alumno no hizo check-in antes del minuto 16 (automatica).
--   LATE_CANCELLATION: el alumno cancelo con menos de 60 minutos de anticipacion (automatica).
--   DISORDER         : el administrador la aplica manualmente por mal uso del espacio.
--   LATE_RETURN      : el administrador la aplica manualmente por devolucion tardia de equipo.
--
-- manager_admin_id puede ser NULL en sanciones generadas de forma automatica por el sistema.
-- Una sancion activa bloquea al usuario para crear reservas y solicitar prestamos.
-- =============================================================================
CREATE TABLE penalty (
    penalty_id       INT          NOT NULL AUTO_INCREMENT,
    user_id          INT          NOT NULL,
    loan_id          INT,
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
    CONSTRAINT fk_penal_loan     FOREIGN KEY (loan_id)
        REFERENCES loan(loan_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_penal_admin    FOREIGN KEY (manager_admin_id)
        REFERENCES `user`(user_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_penalty_dates CHECK (end_date > start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_penal_user ON penalty(user_id, is_active);


-- =============================================================================
-- Alertas generadas automaticamente ante cada cambio de estado relevante
-- en reservaciones y prestamos. Se eliminan automaticamente a los 60 dias
-- mediante un evento programado en la base de datos y un job en Spring.
-- =============================================================================
CREATE TABLE notification (
    notification_id INT          NOT NULL AUTO_INCREMENT,
    user_id         INT          NOT NULL,
    reservation_id  INT,
    message         VARCHAR(500) NOT NULL,
    is_read         TINYINT(1)   NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiration_date TIMESTAMP    NOT NULL,
    CONSTRAINT pk_notification      PRIMARY KEY (notification_id),
    CONSTRAINT fk_notif_user        FOREIGN KEY (user_id)
        REFERENCES `user`(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_notif_reservation FOREIGN KEY (reservation_id)
        REFERENCES reservation(reservation_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notif_user       ON notification(user_id, is_read);
CREATE INDEX idx_notif_expiration ON notification(expiration_date);


-- =============================================================================
-- Registro de cada ejecucion del proceso de importacion masiva de alumnos
-- desde archivo CSV. Permite auditoria y trazabilidad de las altas masivas.
-- =============================================================================
CREATE TABLE import_log (
    import_id          INT          NOT NULL AUTO_INCREMENT,
    manager_admin_id   INT          NOT NULL,
    file_name          VARCHAR(255) NOT NULL,
    records_processed  INT          NOT NULL DEFAULT 0,
    status             ENUM('SUCCESS','FAILED','PARTIAL') NOT NULL DEFAULT 'SUCCESS',
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_import_log   PRIMARY KEY (import_id),
    CONSTRAINT fk_import_admin FOREIGN KEY (manager_admin_id)
        REFERENCES `user`(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_import_status ON import_log(status);


-- =============================================================================
-- Evento programado para eliminar notificaciones vencidas.
-- Funciona como respaldo a nivel de base de datos del job @Scheduled de Spring.
-- =============================================================================
SET GLOBAL event_scheduler = ON;

DROP EVENT IF EXISTS evt_limpiar_notificaciones;
CREATE EVENT evt_limpiar_notificaciones
    ON SCHEDULE EVERY 1 DAY
    STARTS CURRENT_TIMESTAMP
    DO
        DELETE FROM notification
        WHERE expiration_date <= NOW();


-- =============================================================================
-- DML — Datos iniciales
-- Contrasena de todos los usuarios: password123
-- Hash BCrypt (costo 10): $2a$10$KRAAi6/KflfRkNZwF8hh4u.cdNqXcie2MUgXNRnRYK1l5Qg1yVKc2
-- =============================================================================

INSERT INTO faculty (faculty_id, name) VALUES
    (1, 'Facultad de Ingenieria');


-- 6 cubiculos de la biblioteca (RF-01). Todos inician disponibles.
INSERT INTO cubicle (cubicle_id, name, max_capacity, status, logo_url) VALUES
    (1, 'Cubiculo Biblioteca 01', 6, 'AVAILABLE',   'https://media.tenor.com/ZY16_IxQsMQAAAAM/arknights-endfield.gif'),
    (2, 'Cubiculo Biblioteca 02', 4, 'AVAILABLE',   'https://media.tenor.com/IwcY6lQD0_YAAAAM/fluorite-endfield.gif'),
    (3, 'Cubiculo Biblioteca 03', 6, 'AVAILABLE',   'https://i.pinimg.com/originals/87/72/3b/87723b4da84a23daa24f5a573d4f54c3.gif'),
    (4, 'Cubiculo Biblioteca 04', 4, 'AVAILABLE',   'https://i.pinimg.com/originals/63/32/46/633246a5ded88d206fb996a1b82126c4.gif'),
    (5, 'Cubiculo Biblioteca 05', 6, 'AVAILABLE',   'https://i.makeagif.com/media/12-23-2024/obnhZt.gif'),
    (6, 'Cubiculo Biblioteca 06', 4, 'MAINTENANCE', 'https://upload-os-bbs.hoyolab.com/upload/2024/10/16/317695012/38ee29b69f1283b9b9fb81c704b4346a_4827335672541980155.gif');


-- Usuarios de prueba. Cada combinacion de rol e indicadores cubre un caso
-- de uso distinto para la demostracion del sistema:
--
--   id  matricula  rol       tutor  bloqueado  cambio_pwd
--   1   ADM001     ADMIN     no     no         no   -> administrador principal
--   2   367886     STUDENT   no     no         no   -> alumno activo sin restricciones
--   3   374357     STUDENT   si     no         no   -> alumno con privilegios de tutor
--   4   367651     STUDENT   no     si         no   -> alumno bloqueado por sancion activa
--   5   EMP001     TEACHER   no     no         no   -> docente activo
--   6   RCP001     RECEPTOR  no     no         no   -> iPad del cubiculo 01
INSERT INTO `user`
    (user_id, faculty_id, first_name, last_name, email, enrollment,
     password_hash, logo_url, role, is_tutor, is_blocked, must_change_password)
VALUES
    (1, 1, 'Laura',    'Mendoza Rios',      'admin@uach.mx',       'ADM001',
     '$2a$10$KRAAi6/KflfRkNZwF8hh4u.cdNqXcie2MUgXNRnRYK1l5Qg1yVKc2',
     'https://cdn.pixabay.com/photo/2018/11/13/21/43/avatar-3814049_960_720.png',
     'ADMIN',    0, 0, 0),

    (2, 1, 'Nicolas',  'Nevarez Loera',     'a367886@uach.mx',     '367886',
     '$2a$10$KRAAi6/KflfRkNZwF8hh4u.cdNqXcie2MUgXNRnRYK1l5Qg1yVKc2',
     'https://cdn.pixabay.com/photo/2018/11/13/21/43/avatar-3814049_960_720.png',
     'STUDENT',  0, 0, 0),

    (3, 1, 'Jonathan', 'Gandara Salazar',   'a374357@uach.mx',     '374357',
     '$2a$10$KRAAi6/KflfRkNZwF8hh4u.cdNqXcie2MUgXNRnRYK1l5Qg1yVKc2',
     'https://cdn.pixabay.com/photo/2018/11/13/21/43/avatar-3814049_960_720.png',
     'STUDENT',  1, 0, 0),

    (4, 1, 'Samuel',   'Garcia Gomez',      'a367651@uach.mx',     '367651',
     '$2a$10$KRAAi6/KflfRkNZwF8hh4u.cdNqXcie2MUgXNRnRYK1l5Qg1yVKc2',
     'https://cdn.pixabay.com/photo/2018/11/13/21/43/avatar-3814049_960_720.png',
     'STUDENT',  0, 1, 0),

    (5, 1, 'Marco',    'Herrera Bustamante','m.herrera@uach.mx',   'EMP001',
     '$2a$10$KRAAi6/KflfRkNZwF8hh4u.cdNqXcie2MUgXNRnRYK1l5Qg1yVKc2',
     'https://cdn.pixabay.com/photo/2018/11/13/21/43/avatar-3814049_960_720.png',
     'TEACHER',  0, 0, 0),

    (9, 1, 'iPad',     'Cubiculo 01',       'receptor.c01@uach.mx','RCP001',
     '$2a$10$KRAAi6/KflfRkNZwF8hh4u.cdNqXcie2MUgXNRnRYK1l5Qg1yVKc2',
     'https://cdn.pixabay.com/photo/2018/11/13/21/43/avatar-3814049_960_720.png',
     'RECEPTOR', 0, 0, 0);


-- Categorias de equipo. El slug es el valor que usa el frontend en los filtros.
INSERT INTO equipment_category (category_id, name, slug, logo_url) VALUES
    (1, 'Computo',     'computo',     ''),
    (2, 'Audiovisual', 'audiovisual', ''),
    (3, 'Material',    'material',    ''),
    (4, 'Electrónica de Laboratorio', 'electronica', '');

-- Inventario inicial de equipo prestable (RF-06).
-- Los stocks se actualizan automaticamente en cada prestamo y devolucion.
INSERT INTO equipment_type
    (equipment_type_id, category_id, name, description, logo_url, total_stock, available_stock)
VALUES
    (1, 1, 'Laptop',                 'Laptop para uso academico',                                '', 4,  4),
    (2, 2, 'Proyector',              'Proyector HDMI para presentaciones, requiere credencial',  '', 3,  3),
    (3, 3, 'Marcadores',             'Marcadores para pizarron, juego de 4 colores',             '', 15, 15),
    (4, 3, 'Borrador para pizarron', 'Borrador estandar para pizarron blanco',                   '', 15, 15),
    (5, 2, 'Webcam',          'Camara web HD para videoconferencias',                     '', 3,  3),
    (6, 1, 'Calculadora cientifica', 'Calculadora cientifica para matematicas e ingenieria',     '', 6,  6),
    (7, 4, 'Multímetro Digital',     'Multímetro TruRMS para prácticas de circuitos',            '', 10, 10),
    (8, 4, 'Estación de Soldadura',  'Cautín de temperatura regulable (incluye base y estaño)',  '', 8,  8);

-- =============================================================================
-- Verificacion rapida al final de la carga
-- =============================================================================

SELECT '=== RESUMEN DE CARGA ===' AS '';

SELECT 'Cubiculos por estado' AS tabla, status AS valor, COUNT(*) AS total
FROM cubicle GROUP BY status;

SELECT 'Usuarios por rol' AS tabla, role AS valor,
       SUM(is_tutor) AS tutores, SUM(is_blocked) AS bloqueados, COUNT(*) AS total
FROM `user` GROUP BY role;

SELECT 'Inventario de equipo' AS tabla, et.name AS equipo,
       ec.name AS categoria, et.total_stock, et.available_stock
FROM equipment_type et
JOIN equipment_category ec ON et.category_id = ec.category_id;