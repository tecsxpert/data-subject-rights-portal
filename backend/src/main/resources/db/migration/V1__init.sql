-- ============================================================
-- V1__init.sql  — Data Subject Rights Portal — Core Schema
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ── USERS ────────────────────────────────────────────────────
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    uuid        UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    username    VARCHAR(100) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL DEFAULT 'USER'
                    CHECK (role IN ('ADMIN','MANAGER','USER')),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email    ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role     ON users(role);

-- ── DATA SUBJECT REQUESTS ─────────────────────────────────────
CREATE TABLE dsr_requests (
    id                  BIGSERIAL PRIMARY KEY,
    uuid                UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    subject_name        VARCHAR(255) NOT NULL,
    subject_email       VARCHAR(255) NOT NULL,
    request_type        VARCHAR(100) NOT NULL
                            CHECK (request_type IN (
                                'ACCESS','ERASURE','RECTIFICATION',
                                'PORTABILITY','RESTRICTION','OBJECTION')),
    status              VARCHAR(50)  NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN (
                                'PENDING','IN_PROGRESS','COMPLETED',
                                'REJECTED','CANCELLED')),
    description         TEXT,
    ai_description      TEXT,
    ai_recommendations  JSONB,
    ai_report           JSONB,
    is_ai_fallback      BOOLEAN      NOT NULL DEFAULT FALSE,
    priority            VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM'
                            CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    assigned_to_id      BIGINT REFERENCES users(id),
    deadline_date       DATE,
    resolved_at         TIMESTAMPTZ,
    file_path           VARCHAR(512),
    file_original_name  VARCHAR(255),
    file_mime_type      VARCHAR(100),
    created_by_id       BIGINT REFERENCES users(id),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ  -- soft-delete
);

CREATE INDEX idx_dsr_status       ON dsr_requests(status)     WHERE deleted_at IS NULL;
CREATE INDEX idx_dsr_type         ON dsr_requests(request_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_dsr_priority     ON dsr_requests(priority)   WHERE deleted_at IS NULL;
CREATE INDEX idx_dsr_email        ON dsr_requests(subject_email);
CREATE INDEX idx_dsr_assigned     ON dsr_requests(assigned_to_id);
CREATE INDEX idx_dsr_created_at   ON dsr_requests(created_at DESC);
CREATE INDEX idx_dsr_deadline     ON dsr_requests(deadline_date) WHERE deleted_at IS NULL;
CREATE INDEX idx_dsr_deleted      ON dsr_requests(deleted_at);

-- Full-text search index
CREATE INDEX idx_dsr_fts ON dsr_requests
    USING GIN (to_tsvector('english',
        COALESCE(subject_name,'') || ' ' ||
        COALESCE(subject_email,'') || ' ' ||
        COALESCE(description,'')));
