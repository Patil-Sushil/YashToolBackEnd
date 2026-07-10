-- Drop the Hibernate-generated check constraint on the action column of the audit_log table
-- to prevent insertions of new AuditAction enum values from failing.
ALTER TABLE audit_log DROP CONSTRAINT IF EXISTS audit_log_action_check;
