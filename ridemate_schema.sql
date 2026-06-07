-- ============================================================
-- RideMate Database Schema
-- MySQL 8.x
-- Run: mysql -u root -p < ridemate_schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS ridemate CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ridemate;

-- ── Users ─────────────────────────────────────────────────
CREATE TABLE users (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(120)    NOT NULL,
    email               VARCHAR(180)    NOT NULL UNIQUE,
    phone               VARCHAR(20)     NOT NULL UNIQUE,
    password_hash       VARCHAR(255)    NOT NULL,
    photo_path          VARCHAR(512),
    gender              ENUM('MALE','FEMALE','OTHER') NOT NULL,
    dob                 DATE            NOT NULL,
    role                ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    status              ENUM('PENDING_VERIFICATION','VERIFIED','REJECTED','SUSPENDED')
                                        NOT NULL DEFAULT 'PENDING_VERIFICATION',
    rejection_reason    TEXT,
    aadhaar_number      VARCHAR(20),
    aadhaar_doc_path    VARCHAR(512),
    dl_number           VARCHAR(30),
    dl_doc_path         VARCHAR(512),
    phone_verified      BOOLEAN         NOT NULL DEFAULT FALSE,
    avg_rating          DECIMAL(3,2)    NOT NULL DEFAULT 0.00,
    total_rides         INT             NOT NULL DEFAULT 0,
    total_parcels_delivered INT         NOT NULL DEFAULT 0,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ── Emergency Contacts ────────────────────────────────────
CREATE TABLE emergency_contacts (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    name        VARCHAR(120) NOT NULL,
    phone       VARCHAR(20)  NOT NULL,
    relation    VARCHAR(60)  NOT NULL,
    CONSTRAINT fk_ec_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ── Vehicles ─────────────────────────────────────────────
CREATE TABLE vehicles (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    model           VARCHAR(120) NOT NULL,
    number_plate    VARCHAR(30)  NOT NULL,
    color           VARCHAR(50)  NOT NULL,
    seats           INT          NOT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_veh_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ── Rides ────────────────────────────────────────────────
CREATE TABLE rides (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    driver_id           BIGINT          NOT NULL,
    vehicle_id          BIGINT,
    from_location       VARCHAR(255)    NOT NULL,
    to_location         VARCHAR(255)    NOT NULL,
    from_lat            DECIMAL(10,7)   NOT NULL,
    from_lng            DECIMAL(10,7)   NOT NULL,
    to_lat              DECIMAL(10,7)   NOT NULL,
    to_lng              DECIMAL(10,7)   NOT NULL,
    departure_time      DATETIME        NOT NULL,
    seats_total         INT             NOT NULL,
    seats_available     INT             NOT NULL,
    price_per_seat      DECIMAL(10,2)   NOT NULL,
    accepts_passengers  BOOLEAN         NOT NULL DEFAULT TRUE,
    accepts_parcels     BOOLEAN         NOT NULL DEFAULT FALSE,
    max_parcel_size     ENUM('SMALL','MEDIUM','LARGE') DEFAULT 'SMALL',
    women_only          BOOLEAN         NOT NULL DEFAULT FALSE,
    status              ENUM('SCHEDULED','STARTED','COMPLETED','CANCELLED')
                                        NOT NULL DEFAULT 'SCHEDULED',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ride_driver  FOREIGN KEY (driver_id)  REFERENCES users(id),
    CONSTRAINT fk_ride_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
) ENGINE=InnoDB;

-- ── Ride Stops ───────────────────────────────────────────
CREATE TABLE ride_stops (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    ride_id     BIGINT          NOT NULL,
    stop_name   VARCHAR(255)    NOT NULL,
    lat         DECIMAL(10,7)   NOT NULL,
    lng         DECIMAL(10,7)   NOT NULL,
    sequence    INT             NOT NULL,
    CONSTRAINT fk_stop_ride FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ── Bookings ─────────────────────────────────────────────
CREATE TABLE bookings (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    ride_id             BIGINT          NOT NULL,
    passenger_id        BIGINT          NOT NULL,
    seats_booked        INT             NOT NULL DEFAULT 1,
    status              ENUM('PENDING','APPROVED','REJECTED','STARTED','COMPLETED','CANCELLED')
                                        NOT NULL DEFAULT 'PENDING',
    trip_otp            VARCHAR(10),
    otp_verified        BOOLEAN         NOT NULL DEFAULT FALSE,
    trip_share_token    VARCHAR(64)     UNIQUE,
    amount              DECIMAL(10,2),
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bk_ride      FOREIGN KEY (ride_id)      REFERENCES rides(id),
    CONSTRAINT fk_bk_passenger FOREIGN KEY (passenger_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- ── Parcels ──────────────────────────────────────────────
CREATE TABLE parcels (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id               BIGINT          NOT NULL,
    driver_id               BIGINT,
    ride_id                 BIGINT,
    from_location           VARCHAR(255)    NOT NULL,
    to_location             VARCHAR(255)    NOT NULL,
    from_lat                DECIMAL(10,7)   NOT NULL,
    from_lng                DECIMAL(10,7)   NOT NULL,
    to_lat                  DECIMAL(10,7)   NOT NULL,
    to_lng                  DECIMAL(10,7)   NOT NULL,
    receiver_name           VARCHAR(120)    NOT NULL,
    receiver_phone          VARCHAR(20)     NOT NULL,
    category                ENUM('DOCUMENTS','FOOD','ELECTRONICS','CLOTHES','OTHER') NOT NULL,
    weight_kg               DECIMAL(6,2)    NOT NULL,
    size                    ENUM('SMALL','MEDIUM','LARGE') NOT NULL,
    description             TEXT,
    declared_value          DECIMAL(10,2),
    parcel_photo_path       VARCHAR(512)    NOT NULL,
    pickup_photo_path       VARCHAR(512),
    delivery_photo_path     VARCHAR(512),
    receiver_signature_path VARCHAR(512),
    pickup_otp              VARCHAR(10),
    pickup_otp_verified     BOOLEAN         NOT NULL DEFAULT FALSE,
    delivery_otp            VARCHAR(10),
    delivery_otp_verified   BOOLEAN         NOT NULL DEFAULT FALSE,
    trip_share_token        VARCHAR(64)     UNIQUE,
    status                  ENUM('PENDING','ACCEPTED','PICKED_UP','IN_TRANSIT','DELIVERED','CANCELLED','DISPUTED')
                                            NOT NULL DEFAULT 'PENDING',
    amount                  DECIMAL(10,2),
    preferred_time          DATETIME,
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    picked_up_at            DATETIME,
    delivered_at            DATETIME,
    CONSTRAINT fk_parcel_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT fk_parcel_driver FOREIGN KEY (driver_id) REFERENCES users(id),
    CONSTRAINT fk_parcel_ride   FOREIGN KEY (ride_id)   REFERENCES rides(id)
) ENGINE=InnoDB;

-- ── Messages ─────────────────────────────────────────────
CREATE TABLE messages (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id  BIGINT,
    parcel_id   BIGINT,
    sender_id   BIGINT      NOT NULL,
    text        TEXT        NOT NULL,
    sent_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read     BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_msg_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_msg_parcel  FOREIGN KEY (parcel_id)  REFERENCES parcels(id),
    CONSTRAINT fk_msg_sender  FOREIGN KEY (sender_id)  REFERENCES users(id)
) ENGINE=InnoDB;

-- ── Reviews ──────────────────────────────────────────────
CREATE TABLE reviews (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    ride_id         BIGINT,
    parcel_id       BIGINT,
    from_user_id    BIGINT      NOT NULL,
    to_user_id      BIGINT      NOT NULL,
    rating          TINYINT     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment         TEXT,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rev_ride      FOREIGN KEY (ride_id)      REFERENCES rides(id),
    CONSTRAINT fk_rev_parcel    FOREIGN KEY (parcel_id)    REFERENCES parcels(id),
    CONSTRAINT fk_rev_from_user FOREIGN KEY (from_user_id) REFERENCES users(id),
    CONSTRAINT fk_rev_to_user   FOREIGN KEY (to_user_id)   REFERENCES users(id),
    -- Prevent duplicate reviews for the same ride/parcel pair
    UNIQUE KEY uq_review_ride   (ride_id, from_user_id, to_user_id),
    UNIQUE KEY uq_review_parcel (parcel_id, from_user_id, to_user_id)
) ENGINE=InnoDB;

-- ── Reports ──────────────────────────────────────────────
CREATE TABLE reports (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id     BIGINT      NOT NULL,
    reported_id     BIGINT      NOT NULL,
    ride_id         BIGINT,
    parcel_id       BIGINT,
    reason          TEXT        NOT NULL,
    status          ENUM('OPEN','REVIEWED','ACTIONED') NOT NULL DEFAULT 'OPEN',
    admin_notes     TEXT,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
    CONSTRAINT fk_report_reported FOREIGN KEY (reported_id) REFERENCES users(id),
    CONSTRAINT fk_report_ride     FOREIGN KEY (ride_id)     REFERENCES rides(id),
    CONSTRAINT fk_report_parcel   FOREIGN KEY (parcel_id)   REFERENCES parcels(id)
) ENGINE=InnoDB;

-- ── Parcel Complaints ────────────────────────────────────
CREATE TABLE parcel_complaints (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    parcel_id           BIGINT      NOT NULL,
    complainant_id      BIGINT      NOT NULL,
    reason              TEXT        NOT NULL,
    evidence_photo_path VARCHAR(512),
    status              ENUM('OPEN','REVIEWING','RESOLVED','REJECTED')
                                    NOT NULL DEFAULT 'OPEN',
    admin_notes         TEXT,
    created_at          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at         DATETIME,
    CONSTRAINT fk_complaint_parcel      FOREIGN KEY (parcel_id)      REFERENCES parcels(id),
    CONSTRAINT fk_complaint_complainant FOREIGN KEY (complainant_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- ── SOS Alerts ───────────────────────────────────────────
CREATE TABLE sos_alerts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    ride_id         BIGINT,
    parcel_id       BIGINT,
    lat             DECIMAL(10,7),
    lng             DECIMAL(10,7),
    triggered_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved        BOOLEAN         NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_sos_user   FOREIGN KEY (user_id)   REFERENCES users(id),
    CONSTRAINT fk_sos_ride   FOREIGN KEY (ride_id)   REFERENCES rides(id),
    CONSTRAINT fk_sos_parcel FOREIGN KEY (parcel_id) REFERENCES parcels(id)
) ENGINE=InnoDB;

-- ── Blocked Users ────────────────────────────────────────
CREATE TABLE blocked_users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    blocker_id  BIGINT      NOT NULL,
    blocked_id  BIGINT      NOT NULL,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_block (blocker_id, blocked_id),
    CONSTRAINT fk_block_blocker FOREIGN KEY (blocker_id) REFERENCES users(id),
    CONSTRAINT fk_block_blocked FOREIGN KEY (blocked_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- ── OTP Codes ────────────────────────────────────────────
CREATE TABLE otp_codes (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    code        VARCHAR(10) NOT NULL,
    type        ENUM('PHONE','PASSWORD_RESET','TRIP','PICKUP','DELIVERY') NOT NULL,
    related_id  BIGINT,                 -- booking_id or parcel_id depending on type
    expires_at  DATETIME    NOT NULL,
    used        BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_otp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ── Seed: Admin user (password = Admin@123) ──────────────
-- BCrypt hash of "Admin@123" — regenerate in prod
INSERT INTO users (name, email, phone, password_hash, gender, dob, role, status, phone_verified)
VALUES (
    'Super Admin',
    'admin@ridemate.com',
    '9000000000',
    '$2a$12$KIXtbCf2EqLKD2/UO.i0xuQEHOJ0YpRsFZKHTBnxqZ2BFMg1bJj1u',
    'MALE',
    '1990-01-01',
    'ADMIN',
    'VERIFIED',
    TRUE
);
