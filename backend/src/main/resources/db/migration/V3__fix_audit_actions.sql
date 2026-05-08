-- ============================================================
-- V3__fix_audit_actions.sql  — Extend audit_log action constraint
-- Adds FILE_UPLOAD action used by DsrRequestService.attachFile()
-- ============================================================

ALTER TABLE audit_log DROP CONSTRAINT IF EXISTS audit_log_action_check;

ALTER TABLE audit_log
    ADD CONSTRAINT audit_log_action_check
        CHECK (action IN (
            'CREATE','UPDATE','DELETE',
            'VIEW','EXPORT',
            'LOGIN','LOGOUT',
            'FILE_UPLOAD'
        ));
