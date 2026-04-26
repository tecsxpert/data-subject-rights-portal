-- Core table for Data Subject Requests
CREATE TABLE dsr_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    request_type VARCHAR(50) NOT NULL, -- e.g., ACCESS, DELETION, PORTABILITY
    status VARCHAR(50) DEFAULT 'PENDING',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    description TEXT,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexing for performance on common search fields
CREATE INDEX idx_dsr_email ON dsr_requests(email);
CREATE INDEX idx_dsr_status ON dsr_requests(status);

-- Adding soft delete capability to the main table
ALTER TABLE dsr_requests ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE;