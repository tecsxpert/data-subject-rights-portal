-- ============================================================
-- V2__audit_log.sql  — Audit Log + Stats View
-- ============================================================

CREATE TABLE audit_log (
    id           BIGSERIAL PRIMARY KEY,
    entity_type  VARCHAR(100) NOT NULL,
    entity_id    BIGINT,
    entity_uuid  UUID,
    action       VARCHAR(50)  NOT NULL
                     CHECK (action IN ('CREATE','UPDATE','DELETE','VIEW','EXPORT','LOGIN','LOGOUT')),
    performed_by BIGINT REFERENCES users(id),
    username     VARCHAR(100),
    ip_address   VARCHAR(50),
    user_agent   VARCHAR(512),
    old_value    JSONB,
    new_value    JSONB,
    description  VARCHAR(1000),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_entity     ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_user       ON audit_log(performed_by);
CREATE INDEX idx_audit_action     ON audit_log(action);
CREATE INDEX idx_audit_created_at ON audit_log(created_at DESC);

-- ── STATS MATERIALIZED VIEW (refresh on demand) ──────────────
CREATE MATERIALIZED VIEW dsr_stats AS
SELECT
    COUNT(*)                                              AS total_requests,
    COUNT(*) FILTER (WHERE status = 'PENDING')            AS pending,
    COUNT(*) FILTER (WHERE status = 'IN_PROGRESS')        AS in_progress,
    COUNT(*) FILTER (WHERE status = 'COMPLETED')          AS completed,
    COUNT(*) FILTER (WHERE status = 'REJECTED')           AS rejected,
    COUNT(*) FILTER (WHERE deadline_date < CURRENT_DATE
                       AND status NOT IN ('COMPLETED','REJECTED','CANCELLED'))
                                                          AS overdue,
    COUNT(*) FILTER (WHERE created_at >= NOW() - INTERVAL '7 days')
                                                          AS new_this_week,
    COUNT(*) FILTER (WHERE created_at >= NOW() - INTERVAL '30 days')
                                                          AS new_this_month,
    ROUND(AVG(
        EXTRACT(EPOCH FROM (resolved_at - created_at))/3600
    ) FILTER (WHERE resolved_at IS NOT NULL), 2)          AS avg_resolution_hours,
    NOW()                                                 AS refreshed_at
FROM dsr_requests
WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ON dsr_stats (refreshed_at);
