CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    request_id UUID REFERENCES dsr_requests(id),
    action_taken VARCHAR(255) NOT NULL,
    performed_by VARCHAR(100), -- User email or ID
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);