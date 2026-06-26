DROP TABLE IF EXISTS audit_log CASCADE;

CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    username VARCHAR(255),
    action VARCHAR(50) NOT NULL,
    action_description VARCHAR(500),
    entity_type VARCHAR(100),
    entity_id UUID,
    entity_name VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message TEXT,
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    duration_ms BIGINT,

    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes as defined in AuditLog entity annotations
CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_username ON audit_log(username);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_log_entity_type ON audit_log(entity_type);
CREATE INDEX idx_audit_log_entity_id ON audit_log(entity_id);
CREATE INDEX idx_audit_log_status ON audit_log(status);
