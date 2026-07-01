-- ============================================================
-- V11: Reusable Email Module - Production Grade
-- Polymorphic owner: QUOTATION, INVOICE, DELIVERY_CHALLAN, ...
-- ============================================================

CREATE TABLE IF NOT EXISTS email_log (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),

    company_id UUID,
    company_code VARCHAR(20),

    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,

    from_address VARCHAR(255) NOT NULL,
    to_addresses TEXT NOT NULL,
    cc_addresses TEXT,
    bcc_addresses TEXT,

    subject VARCHAR(1000) NOT NULL,
    body_text TEXT,
    body_html TEXT,
    charset VARCHAR(20) DEFAULT 'UTF-8',

    has_attachments BOOLEAN NOT NULL DEFAULT FALSE,
    attachment_count INTEGER NOT NULL DEFAULT 0,
    attachment_total_size_bytes BIGINT NOT NULL DEFAULT 0,
    attachment_summary VARCHAR(500),

    mail_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    provider_type VARCHAR(20) NOT NULL DEFAULT 'SMTP',
    priority VARCHAR(10) NOT NULL DEFAULT 'NORMAL',

    smtp_message_id VARCHAR(255),
    smtp_response_code VARCHAR(10),
    smtp_response_message TEXT,

    queued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,

    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    last_retry_at TIMESTAMP,
    next_retry_at TIMESTAMP,

    failure_reason TEXT,
    failure_stack_trace TEXT,

    is_test BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT chk_mail_status CHECK (mail_status IN
('PENDING','PROCESSING','SENT','DELIVERED','FAILED','BOUNCED','REJECTED')),
    CONSTRAINT chk_priority CHECK (priority IN ('LOW','NORMAL','HIGH','URGENT'))
    );

CREATE TABLE IF NOT EXISTS email_recipient (
                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    email_log_id UUID NOT NULL,
    recipient_address VARCHAR(255) NOT NULL,
    recipient_type VARCHAR(10) NOT NULL,

    delivery_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    delivered_at TIMESTAMP,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,
    bounced_at TIMESTAMP,
    bounce_reason TEXT,

    CONSTRAINT fk_recipient_log FOREIGN KEY (email_log_id) REFERENCES email_log(id) ON DELETE CASCADE,
    CONSTRAINT chk_recipient_type CHECK (recipient_type IN ('TO','CC','BCC'))
    );

CREATE TABLE IF NOT EXISTS email_attachment_log (
                                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    email_log_id UUID NOT NULL,
    filename VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    size_bytes BIGINT NOT NULL,
    storage_path VARCHAR(1000),
    inline BOOLEAN NOT NULL DEFAULT FALSE,
    content_id VARCHAR(255),

    CONSTRAINT fk_attachment_log FOREIGN KEY (email_log_id) REFERENCES email_log(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_email_entity ON email_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_email_owner ON email_log(entity_id);
CREATE INDEX IF NOT EXISTS idx_email_company ON email_log(company_id);
CREATE INDEX IF NOT EXISTS idx_email_status ON email_log(mail_status);
CREATE INDEX IF NOT EXISTS idx_email_pending ON email_log(mail_status, queued_at DESC) WHERE mail_status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_email_failed ON email_log(mail_status, retry_count) WHERE mail_status = 'FAILED';
CREATE INDEX IF NOT EXISTS idx_email_priority ON email_log(priority, queued_at);
CREATE INDEX IF NOT EXISTS idx_email_message_id ON email_log(smtp_message_id);

CREATE INDEX IF NOT EXISTS idx_email_recipient_log ON email_recipient(email_log_id);
CREATE INDEX IF NOT EXISTS idx_email_recipient_address ON email_recipient(recipient_address);
CREATE INDEX IF NOT EXISTS idx_email_recipient_bounced ON email_recipient(bounced_at) WHERE bounced_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_attachment_log ON email_attachment_log(email_log_id);