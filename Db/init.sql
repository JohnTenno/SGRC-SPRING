CREATE DATABASE IF NOT EXISTS sgrc_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sgrc_db;

DROP USER IF EXISTS 'sgrc_user'@'%';
CREATE USER 'sgrc_user'@'%' IDENTIFIED BY 'sgrc_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON sgrc_db.* TO 'sgrc_user'@'%';
FLUSH PRIVILEGES;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS penalty;
DROP TABLE IF EXISTS loan;
DROP TABLE IF EXISTS equipment_type;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS cubicle;
DROP TABLE IF EXISTS building;
DROP TABLE IF EXISTS faculty;

SET FOREIGN_KEY_CHECKS = 1;

-- ===========================================================================
CREATE TABLE faculty (
    faculty_id    INT            NOT NULL AUTO_INCREMENT,
    name          VARCHAR(120)   NOT NULL,
    CONSTRAINT pk_faculty PRIMARY KEY (faculty_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
CREATE TABLE building (
    building_id   INT            NOT NULL AUTO_INCREMENT,
    faculty_id    INT            NOT NULL,
    name          VARCHAR(120)   NOT NULL,
    CONSTRAINT pk_building      PRIMARY KEY (building_id),
    CONSTRAINT fk_bldg_faculty  FOREIGN KEY (faculty_id)
        REFERENCES faculty(faculty_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
--    status: AVAILABLE | OCCUPIED | MAINTENANCE
-- ---------------------------------------------------------------------------
CREATE TABLE cubicle (
    cubicle_id    INT            NOT NULL AUTO_INCREMENT,
    building_id   INT            NOT NULL,
    name          VARCHAR(80)    NOT NULL,
    max_capacity  INT            NOT NULL DEFAULT 6,
    status        ENUM('AVAILABLE','OCCUPIED','MAINTENANCE')
                                 NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT pk_cubicle       PRIMARY KEY (cubicle_id),
    CONSTRAINT fk_cub_building  FOREIGN KEY (building_id)
        REFERENCES building(building_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_capacity     CHECK (max_capacity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
--    role: STUDENT | TEACHER | ADMIN
-- ---------------------------------------------------------------------------
CREATE TABLE `user` (
    user_id       INT            NOT NULL AUTO_INCREMENT,
    faculty_id    INT            NOT NULL,
    first_name    VARCHAR(80)    NOT NULL,
    last_name     VARCHAR(80)    NOT NULL,
    email         VARCHAR(120)   NOT NULL,
    enrollment    VARCHAR(20)    NOT NULL,
    password_hash VARCHAR(255)   NOT NULL,
    role          ENUM('STUDENT','TEACHER','ADMIN')
                                 NOT NULL DEFAULT 'STUDENT',
    is_tutor      TINYINT(1)     NOT NULL DEFAULT 0,
    is_blocked    TINYINT(1)     NOT NULL DEFAULT 0,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_user           PRIMARY KEY (user_id),
    CONSTRAINT uq_email          UNIQUE (email),
    CONSTRAINT uq_enrollment     UNIQUE (enrollment),
    CONSTRAINT fk_usr_faculty    FOREIGN KEY (faculty_id)
        REFERENCES faculty(faculty_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_role       ON `user`(role);
CREATE INDEX idx_user_blocked    ON `user`(is_blocked);

-- ---------------------------------------------------------------------------
--    status: PENDING | APPROVED | COMPLETED | CANCELLED
-- ---------------------------------------------------------------------------
CREATE TABLE reservation (
    reservation_id INT            NOT NULL AUTO_INCREMENT,
    user_id        INT            NOT NULL,
    cubicle_id     INT            NOT NULL,
    reservation_date DATE         NOT NULL,
    start_time     TIME           NOT NULL,
    end_time       TIME           NOT NULL,
    status         ENUM('PENDING','APPROVED','COMPLETED','CANCELLED')
                                  NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_reservation     PRIMARY KEY (reservation_id),
    CONSTRAINT fk_res_user        FOREIGN KEY (user_id)
        REFERENCES `user`(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_res_cubicle     FOREIGN KEY (cubicle_id)
        REFERENCES cubicle(cubicle_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_times          CHECK (end_time > start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_res_user     ON reservation(user_id);
CREATE INDEX idx_res_cubicle  ON reservation(cubicle_id, reservation_date, start_time, end_time);
CREATE INDEX idx_res_status   ON reservation(status);

-- ---------------------------------------------------------------------------
CREATE TABLE equipment_type (
    equipment_type_id  INT            NOT NULL AUTO_INCREMENT,
    name               VARCHAR(80)    NOT NULL,
    description        VARCHAR(255),
    total_stock        INT            NOT NULL DEFAULT 0,
    available_stock    INT            NOT NULL DEFAULT 0,
    CONSTRAINT pk_equipment_type  PRIMARY KEY (equipment_type_id),
    CONSTRAINT chk_total_stock    CHECK (total_stock >= 0),
    CONSTRAINT chk_avail_stock    CHECK (available_stock >= 0),
    CONSTRAINT chk_stock_max      CHECK (available_stock <= total_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
--    status: PENDING_PICKUP | ACTIVE | RETURNED | DAMAGED
-- ---------------------------------------------------------------------------
CREATE TABLE loan (
    loan_id              INT            NOT NULL AUTO_INCREMENT,
    user_id              INT            NOT NULL,
    reservation_id       INT            NOT NULL,
    equipment_type_id    INT            NOT NULL,
    quantity             INT            NOT NULL DEFAULT 1,
    loan_date            TIMESTAMP      NULL,
    expected_return_date TIMESTAMP      NULL,
    actual_return_date   TIMESTAMP      NULL,
    status               ENUM('PENDING_PICKUP','ACTIVE','RETURNED','DAMAGED')
                                        NOT NULL DEFAULT 'PENDING_PICKUP',
    id_card_handed       TINYINT(1)     NOT NULL DEFAULT 0,
    notes                VARCHAR(255),
    created_at           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_loan                  PRIMARY KEY (loan_id),
    CONSTRAINT fk_loan_user             FOREIGN KEY (user_id)
        REFERENCES `user`(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_loan_reservation      FOREIGN KEY (reservation_id)
        REFERENCES reservation(reservation_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_loan_equip_type       FOREIGN KEY (equipment_type_id)
        REFERENCES equipment_type(equipment_type_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_loan_qty             CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_loan_user   ON loan(user_id);
CREATE INDEX idx_loan_status ON loan(status);

-- ---------------------------------------------------------------------------
--    type: LATE_RETURN | DISORDER
--    manager_admin_id: FK to the admin user who applies/lifts the penalty.
-- ---------------------------------------------------------------------------
CREATE TABLE penalty (
    penalty_id       INT            NOT NULL AUTO_INCREMENT,
    user_id          INT            NOT NULL,
    loan_id          INT,
    manager_admin_id INT            NOT NULL,
    type             ENUM('LATE_RETURN','DISORDER')
                                    NOT NULL,
    reason           VARCHAR(255)   NOT NULL,
    start_date       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date         TIMESTAMP      NOT NULL,
    is_active        TINYINT(1)     NOT NULL DEFAULT 1,
    CONSTRAINT pk_penalty           PRIMARY KEY (penalty_id),
    CONSTRAINT fk_penal_user        FOREIGN KEY (user_id)
        REFERENCES `user`(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_penal_loan        FOREIGN KEY (loan_id)
        REFERENCES loan(loan_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_penal_admin       FOREIGN KEY (manager_admin_id)
        REFERENCES `user`(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_penalty_dates    CHECK (end_date > start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_penal_user ON penalty(user_id, is_active);

-- ---------------------------------------------------------------------------
--    expiration_date = created_at + 60 days (calculated on insert).
-- ---------------------------------------------------------------------------
CREATE TABLE notification (
    notification_id  INT            NOT NULL AUTO_INCREMENT,
    user_id          INT            NOT NULL,
    reservation_id   INT,
    message          VARCHAR(500)   NOT NULL,
    is_read          TINYINT(1)     NOT NULL DEFAULT 0,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiration_date  TIMESTAMP      NOT NULL,
    CONSTRAINT pk_notification      PRIMARY KEY (notification_id),
    CONSTRAINT fk_notif_user        FOREIGN KEY (user_id)
        REFERENCES `user`(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_notif_reservation FOREIGN KEY (reservation_id)
        REFERENCES reservation(reservation_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notif_user        ON notification(user_id, is_read);
CREATE INDEX idx_notif_expiration  ON notification(expiration_date);

-- ---------------------------------------------------------------------------
--     Runs every 24 hours and deletes notifications whose expiration_date
--     has passed, equivalent to Spring's @Scheduled as DB backup.
-- ---------------------------------------------------------------------------
SET GLOBAL event_scheduler = ON;

DROP EVENT IF EXISTS evt_wipe_notifications;
CREATE EVENT evt_wipe_notifications
    ON SCHEDULE EVERY 1 DAY
    STARTS CURRENT_TIMESTAMP
    DO
        DELETE FROM notification
        WHERE expiration_date <= NOW();


-- ===========================================================================
--  DML — TEST DATA
--  Password for all users: password123
--  BCrypt cost-10 Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh72
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- FACULTY & BUILDING
-- ---------------------------------------------------------------------------
INSERT INTO faculty (faculty_id, name) VALUES
    (1, 'Faculty of Engineering');

INSERT INTO building (building_id, faculty_id, name) VALUES
    (1, 1, 'Central Library');

-- ---------------------------------------------------------------------------
-- CUBICLES (6 cubicles — RF-01)
--   C1: AVAILABLE
--   C2: AVAILABLE
--   C3: AVAILABLE
--   C4: AVAILABLE
--   C5: OCCUPIED      ← active reservation on it
--   C6: MAINTENANCE
-- ---------------------------------------------------------------------------
INSERT INTO cubicle (cubicle_id, building_id, name, max_capacity, status) VALUES
    (1, 1, 'Library Cubicle 01', 6, 'AVAILABLE'),
    (2, 1, 'Library Cubicle 02', 4, 'AVAILABLE'),
    (3, 1, 'Library Cubicle 03', 6, 'AVAILABLE'),
    (4, 1, 'Library Cubicle 04', 4, 'AVAILABLE'),
    (5, 1, 'Library Cubicle 05', 6, 'OCCUPIED'),
    (6, 1, 'Library Cubicle 06', 4, 'MAINTENANCE');

-- ---------------------------------------------------------------------------
-- USERS
--   id  enrollment  role       is_tutor is_blocked description
--   1   ADM001      ADMIN      0        0          Main Administrator
--   2   367886      STUDENT    0        0          Active normal student
--   3   374357      STUDENT    1        0          Tutor (can make 4 res/day)
--   4   367651      STUDENT    0        1          BLOCKED by active penalty
--   5   EMP001      TEACHER    0        0          Active teacher
--   6   EMP002      TEACHER    0        0          Teacher with clean history
--   7   STU004      STUDENT    0        0          Student with active loan
--   8   STU005      STUDENT    0        0          Student with PENDING res
-- ---------------------------------------------------------------------------
INSERT INTO `user`
    (user_id, faculty_id, first_name, last_name, email, enrollment, password_hash, role, is_tutor, is_blocked)
VALUES
    -- Administrator
    (1, 1, 'Laura',    'Mendoza Ríos',      'admin@uach.mx',          'ADM001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh72', 'ADMIN',   0, 0),

    -- Normal student
    (2, 1, 'Nicolás',  'Nevárez Loera',     'a367886@uach.mx',        '367886', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh72', 'STUDENT', 0, 0),

    -- Tutor (is_tutor = 1, up to 4 active reservations per day)
    (3, 1, 'Jonathan', 'Gandara Salazar',   'a374357@uach.mx',        '374357', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh72', 'STUDENT', 1, 0),

    -- BLOCKED student by active penalty — cannot reserve or request loan
    (4, 1, 'Samuel',   'García Gómez',      'a367651@uach.mx',        '367651', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh72', 'STUDENT', 0, 1),

    -- Active teacher
    (5, 1, 'Marco',    'Herrera Bustamante','m.herrera@uach.mx',      'EMP001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh72', 'TEACHER', 0, 0),

    -- Teacher clean history
    (6, 1, 'Diana',    'Ramos Ortega',      'd.ramos@uach.mx',        'EMP002', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh72', 'TEACHER', 0, 0),

    -- Student with active loan (projector — handed ID)
    (7, 1, 'Emilio',   'Castillo Vega',     'a370001@uach.mx',        'STU004', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh72', 'STUDENT', 0, 0),

    -- Student with PENDING reservation (schedule conflict under review)
    (8, 1, 'Valeria',  'Torres Montoya',    'a370002@uach.mx',        'STU005', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh72', 'STUDENT', 0, 0);

-- ---------------------------------------------------------------------------
-- EQUIPMENT_TYPE (RF-06)
-- ---------------------------------------------------------------------------
INSERT INTO equipment_type (equipment_type_id, name, description, total_stock, available_stock) VALUES
    (1, 'PROJECTOR', 'HDMI Projector for presentations — requires ID upon request', 3, 2),
    (2, 'BOOK',      'Reference collection textbooks',                              20, 18),
    (3, 'MARKER',    'Whiteboard markers (set of 4 colors)',                        15, 13);

-- ---------------------------------------------------------------------------
-- RESERVATIONS
--
--  id  user      cubicle  date        start   end     status     scenario
--  1   Nicolás   C1       2026-05-18  10:00   12:00   APPROVED   normal active res
--  2   Jonathan  C2       2026-05-18  09:00   11:00   APPROVED   tutor — res 1/4
--  3   Jonathan  C3       2026-05-18  09:00   11:00   APPROVED   tutor — res 2/4
--  4   Jonathan  C4       2026-05-18  09:00   11:00   APPROVED   tutor — res 3/4
--  5   Jonathan  C1       2026-05-18  14:00   16:00   APPROVED   tutor — res 4/4 (limit)
--  6   Samuel    C2       2026-05-16  08:00   10:00   COMPLETED  blocked history
--  7   Marco     C5       2026-05-16  09:00   15:00   APPROVED   teacher 6h, cub OCCUPIED
--  8   Valeria   C2       2026-05-18  09:00   11:00   PENDING    CONFLICT with res 2
--  9   Nicolás   C3       2026-05-15  10:00   12:00   COMPLETED  historical res
--  10  Diana     C4       2026-05-14  08:00   14:00   CANCELLED  cancelled by teacher
--  11  Emilio    C3       2026-05-16  10:00   12:00   APPROVED   base of active loan
-- ---------------------------------------------------------------------------
INSERT INTO reservation
    (reservation_id, user_id, cubicle_id, reservation_date, start_time, end_time, status, created_at)
VALUES
    -- Nicolás: active reservation today, cubicle 1
    (1,  2, 1, '2026-05-18', '10:00:00', '12:00:00', 'APPROVED',   '2026-05-16 00:40:00'),

    -- Jonathan (tutor): 4 active reservations same day — tutor limit edge case
    (2,  3, 2, '2026-05-18', '09:00:00', '11:00:00', 'APPROVED',   '2026-05-16 08:00:00'),
    (3,  3, 3, '2026-05-18', '09:00:00', '11:00:00', 'APPROVED',   '2026-05-16 08:01:00'),
    (4,  3, 4, '2026-05-18', '09:00:00', '11:00:00', 'APPROVED',   '2026-05-16 08:02:00'),
    (5,  3, 1, '2026-05-18', '14:00:00', '16:00:00', 'APPROVED',   '2026-05-16 08:03:00'),

    -- Samuel (blocked): already completed reservation, penalty applied after
    (6,  4, 2, '2026-05-16', '08:00:00', '10:00:00', 'COMPLETED',  '2026-05-15 07:50:00'),

    -- Marco (teacher): 6h active reservation, cubicle 5 shows OCCUPIED
    (7,  5, 5, '2026-05-16', '09:00:00', '15:00:00', 'APPROVED',   '2026-05-16 08:30:00'),

    -- Valeria: reservation in PENDING due to schedule conflict with reservation 2 (same cubicle 2, same time)
    (8,  8, 2, '2026-05-18', '09:00:00', '11:00:00', 'PENDING',    '2026-05-16 09:15:00'),

    -- Nicolás: completed historical reservation
    (9,  2, 3, '2026-05-15', '10:00:00', '12:00:00', 'COMPLETED',  '2026-05-14 22:00:00'),

    -- Diana: cancelled reservation
    (10, 6, 4, '2026-05-14', '08:00:00', '14:00:00', 'CANCELLED',  '2026-05-13 15:00:00'),

    -- Emilio: active reservation base for projector loan
    (11, 7, 3, '2026-05-16', '10:00:00', '12:00:00', 'APPROVED',   '2026-05-16 09:50:00');

-- ---------------------------------------------------------------------------
-- LOANS (RF-06)
--
--  id  user     res  equipment   scenario
--  1   Emilio   11   PROJECTOR   ACTIVE — ID handed, main edge case
--  2   Nicolás  9    BOOK        RETURNED — returned on time, clean history
--  3   Samuel   6    MARKER      penalty base (returned late)
--  4   Marco    7    PROJECTOR   PENDING_PICKUP — admin has not confirmed yet
--  5   Nicolás  1    BOOK        PENDING_PICKUP — newly created request
-- ---------------------------------------------------------------------------
INSERT INTO loan
    (loan_id, user_id, reservation_id, equipment_type_id, quantity,
     loan_date, expected_return_date, actual_return_date,
     status, id_card_handed, notes, created_at)
VALUES
    -- ACTIVE projector loan with ID handed (RF-06 edge case)
    (1, 7, 11, 1, 1,
     '2026-05-16 10:01:00', '2026-05-16 12:00:00', NULL,
     'ACTIVE', 1, 'ID Card: Emilio Castillo Vega — verified by admin', '2026-05-16 10:00:00'),

    -- RETURNED book loan — returned correctly, no penalty
    (2, 2, 9, 2, 1,
     '2026-05-15 10:02:00', '2026-05-15 12:00:00', '2026-05-15 11:50:00',
     'RETURNED', 0, NULL, '2026-05-15 10:01:00'),

    -- Samuel's penalty base loan (marker returned late — triggered LATE_RETURN)
    (3, 4, 6, 3, 1,
     '2026-05-16 08:02:00', '2026-05-16 10:00:00', '2026-05-16 14:30:00',
     'RETURNED', 0, 'Returned 4.5 hours late — penalty applied automatically', '2026-05-16 08:01:00'),

    -- PENDING_PICKUP Loan: Marco requested projector, admin has not confirmed
    (4, 5, 7, 1, 1,
     NULL, '2026-05-16 15:00:00', NULL,
     'PENDING_PICKUP', 0, 'Pending ID card verification at front desk', '2026-05-16 09:00:00'),

    -- PENDING_PICKUP Loan: Nicolás requested book for 18th reservation
    (5, 2, 1, 2, 1,
     NULL, '2026-05-18 12:00:00', NULL,
     'PENDING_PICKUP', 0, NULL, '2026-05-16 00:42:00');

-- ---------------------------------------------------------------------------
-- PENALTIES (RF-07)
--
--  id  penalized user  type          active  scenario
--  1   Samuel  (id=4)  LATE_RETURN   1       BLOCKS the user — main edge case
--  2   Nicolás (id=2)  DISORDER      0       Historical already lifted
-- ---------------------------------------------------------------------------
INSERT INTO penalty
    (penalty_id, user_id, loan_id, manager_admin_id, type, reason,
     start_date, end_date, is_active)
VALUES
    -- ACTIVE Penalty on Samuel: did not return on time → is_blocked = 1 in user (student = 7 days)
    (1, 4, 3, 1, 'LATE_RETURN',
     'Markers returned with a 4.5 hour delay over agreed time.',
     '2026-05-16 14:30:00', '2026-05-23 14:30:00', 1),

    -- LIFTED HISTORICAL Penalty: Nicolás left a mess two weeks ago (2 days, already expired)
    (2, 2, NULL, 1, 'DISORDER',
     'Cubicle found with food residue and disorganized furniture.',
     '2026-05-01 11:00:00', '2026-05-03 11:00:00', 0);

-- ---------------------------------------------------------------------------
-- NOTIFICATIONS (RF-04)
--
--  Covers: PENDING→APPROVED, PENDING→CANCELLED, expiration reminder,
--          notification close to expiring to test the wipe job.
-- ---------------------------------------------------------------------------
INSERT INTO notification
    (notification_id, user_id, reservation_id, message, is_read, created_at, expiration_date)
VALUES
    -- Nicolás: his reservation 1 was automatically approved
    (1,  2, 1,
     'Your reservation for Library Cubicle 01 on 2026-05-18 from 10:00 to 12:00 has been approved.',
     0, '2026-05-16 00:40:05', DATE_ADD('2026-05-16 00:40:05', INTERVAL 60 DAY)),

    -- Valeria: her reservation 8 stayed under review (PENDING due to conflict)
    (2,  8, 8,
     'Your request for Library Cubicle 02 on 2026-05-18 is under review due to a schedule conflict. The administrator will resolve it shortly.',
     0, '2026-05-16 09:15:10', DATE_ADD('2026-05-16 09:15:10', INTERVAL 60 DAY)),

    -- Samuel: applied penalty notification
    (3,  4, NULL,
     'A penalty has been registered on your account for late equipment return. Your account will be blocked until 2026-05-23.',
     0, '2026-05-16 14:31:00', DATE_ADD('2026-05-16 14:31:00', INTERVAL 60 DAY)),

    -- Emilio: confirmation that his projector loan was activated
    (4,  7, 11,
     'Your projector loan has been confirmed by the administrator. Remember to return the equipment before 12:00.',
     0, '2026-05-16 10:02:00', DATE_ADD('2026-05-16 10:02:00', INTERVAL 60 DAY)),

    -- Nicolás: his historical book loan was correctly closed
    (5,  2, 9,
     'Your book loan has been successfully closed. Thank you for returning on time!',
     1, '2026-05-15 11:51:00', DATE_ADD('2026-05-15 11:51:00', INTERVAL 60 DAY)),

    -- Jonathan: tutor, reservation 2 approved
    (6,  3, 2,
     'Your reservation for Library Cubicle 02 on 2026-05-18 from 09:00 to 11:00 has been approved.',
     1, '2026-05-16 08:00:10', DATE_ADD('2026-05-16 08:00:10', INTERVAL 60 DAY)),

    -- Diana: cancellation notification for her reservation
    (7,  6, 10,
     'Your reservation for Library Cubicle 04 on 2026-05-14 has been cancelled.',
     1, '2026-05-13 15:00:30', DATE_ADD('2026-05-13 15:00:30', INTERVAL 60 DAY)),

    -- Marco: pending loan confirmation notification at front desk
    (8,  5, 7,
     'Your projector loan request has been registered. Please go to the library front desk so the administrator can confirm the delivery and present your ID card.',
     0, '2026-05-16 09:00:10', DATE_ADD('2026-05-16 09:00:10', INTERVAL 60 DAY)),

    -- *** EDGE CASE: Notification close to expiring (expiration_date = today + 1 day)
    --     Used to test that the wipe job deletes it on the next execution.
    (9,  2, 9,
     '[TEST-WIPE] This notification expires in less than 24 hours to verify the automatic cleanup job.',
     0, DATE_SUB(NOW(), INTERVAL 59 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY)),

    -- *** EDGE CASE: ALREADY EXPIRED Notification (expiration_date in the past)
    --     Should be deleted in the next execution of the scheduled event.
    (10, 3, 3,
     '[TEST-WIPE] This notification has already expired and must be deleted by the scheduler.',
     0, DATE_SUB(NOW(), INTERVAL 61 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));


-- ===========================================================================
--  QUICK VERIFICATION
--  Run after the script to confirm data was loaded correctly.
-- ===========================================================================

SELECT '=== TEST DATA SUMMARY ===' AS '';

SELECT 'CUBICLES BY STATUS' AS category,
       status, COUNT(*) AS total
FROM cubicle
GROUP BY status;

SELECT 'USERS BY ROLE' AS category,
       role,
       SUM(is_tutor) AS tutors,
       SUM(is_blocked) AS blocked_users,
       COUNT(*) AS total
FROM `user`
GROUP BY role;

SELECT 'RESERVATIONS BY STATUS' AS category,
       status, COUNT(*) AS total
FROM reservation
GROUP BY status;

SELECT 'LOANS BY STATUS' AS category,
       status, COUNT(*) AS total
FROM loan
GROUP BY status;

SELECT 'PENALTIES' AS category,
       type,
       is_active,
       COUNT(*) AS total
FROM penalty
GROUP BY type, is_active;

SELECT 'EQUIPMENT STOCK' AS category,
       name,
       total_stock,
       available_stock,
       (total_stock - available_stock) AS on_loan
FROM equipment_type;

SELECT 'NOTIFICATIONS' AS category,
       is_read,
       SUM(CASE WHEN expiration_date < NOW() THEN 1 ELSE 0 END) AS expired,
       SUM(CASE WHEN expiration_date >= NOW() THEN 1 ELSE 0 END) AS valid,
       COUNT(*) AS total
FROM notification
GROUP BY is_read;