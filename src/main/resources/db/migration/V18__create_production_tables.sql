-- ============================================================
-- V18: Production Planning, Execution, and Tracking Module
-- ============================================================

CREATE TABLE IF NOT EXISTS machines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    company_id UUID NOT NULL,
    company_code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_machine_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE RESTRICT,
    CONSTRAINT uq_machine_code UNIQUE (company_id, code),
    CONSTRAINT chk_machine_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'UNDER_MAINTENANCE'))
);

CREATE TABLE IF NOT EXISTS job_cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    company_id UUID NOT NULL,
    company_code VARCHAR(20) NOT NULL,
    job_card_no VARCHAR(30) NOT NULL UNIQUE,
    work_order_id UUID NOT NULL,
    work_order_item_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    priority INTEGER NOT NULL DEFAULT 1,
    total_quantity INTEGER NOT NULL CHECK (total_quantity > 0),
    remarks TEXT,
    is_rework BOOLEAN NOT NULL DEFAULT FALSE,
    rework_parent_job_card_id UUID,

    CONSTRAINT fk_job_card_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE RESTRICT,
    CONSTRAINT fk_job_card_work_order FOREIGN KEY (work_order_id) REFERENCES work_orders(id) ON DELETE RESTRICT,
    CONSTRAINT fk_job_card_work_order_item FOREIGN KEY (work_order_item_id) REFERENCES work_order_items(id) ON DELETE RESTRICT,
    CONSTRAINT fk_job_card_rework_parent FOREIGN KEY (rework_parent_job_card_id) REFERENCES job_cards(id) ON DELETE SET NULL,
    CONSTRAINT chk_job_card_status CHECK (status IN ('CREATED', 'PLANNED', 'ASSIGNED', 'STARTED', 'PAUSED', 'COMPLETED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS production_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    company_id UUID NOT NULL,
    company_code VARCHAR(20) NOT NULL,
    job_card_id UUID NOT NULL UNIQUE,
    machine_id UUID NOT NULL,
    operator_id BIGINT NOT NULL,
    shift VARCHAR(20) NOT NULL DEFAULT 'MORNING',
    planned_start_date DATE NOT NULL,
    planned_end_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    CONSTRAINT fk_schedule_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE RESTRICT,
    CONSTRAINT fk_schedule_job_card FOREIGN KEY (job_card_id) REFERENCES job_cards(id) ON DELETE RESTRICT,
    CONSTRAINT fk_schedule_machine FOREIGN KEY (machine_id) REFERENCES machines(id) ON DELETE RESTRICT,
    CONSTRAINT fk_schedule_operator FOREIGN KEY (operator_id) REFERENCES laborers(id) ON DELETE RESTRICT,
    CONSTRAINT chk_schedule_shift CHECK (shift IN ('MORNING', 'EVENING', 'NIGHT')),
    CONSTRAINT chk_schedule_status CHECK (status IN ('PENDING', 'RUNNING', 'PAUSED', 'COMPLETED'))
);

CREATE TABLE IF NOT EXISTS execution_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    job_card_id UUID NOT NULL,
    operator_id BIGINT NOT NULL,
    machine_id UUID NOT NULL,
    shift VARCHAR(20) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    target_quantity INTEGER NOT NULL CHECK (target_quantity >= 0),
    produced_quantity INTEGER NOT NULL DEFAULT 0,
    rejected_quantity INTEGER NOT NULL DEFAULT 0,
    rework_quantity INTEGER NOT NULL DEFAULT 0,
    pending_quantity INTEGER NOT NULL DEFAULT 0,
    machine_downtime_minutes INTEGER NOT NULL DEFAULT 0,
    downtime_reason VARCHAR(255),
    remarks TEXT,

    CONSTRAINT fk_log_job_card FOREIGN KEY (job_card_id) REFERENCES job_cards(id) ON DELETE CASCADE,
    CONSTRAINT fk_log_operator FOREIGN KEY (operator_id) REFERENCES laborers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_log_machine FOREIGN KEY (machine_id) REFERENCES machines(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS quality_inspections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    company_id UUID NOT NULL,
    company_code VARCHAR(20) NOT NULL,
    job_card_id UUID NOT NULL UNIQUE,
    accepted_quantity INTEGER NOT NULL CHECK (accepted_quantity >= 0),
    rejected_quantity INTEGER NOT NULL DEFAULT 0,
    rework_quantity INTEGER NOT NULL DEFAULT 0,
    inspector VARCHAR(255) NOT NULL,
    inspection_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    result VARCHAR(20) NOT NULL,
    remarks TEXT,

    CONSTRAINT fk_qc_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE RESTRICT,
    CONSTRAINT fk_qc_job_card FOREIGN KEY (job_card_id) REFERENCES job_cards(id) ON DELETE RESTRICT,
    CONSTRAINT chk_qc_result CHECK (result IN ('PASS', 'REJECT'))
);

CREATE INDEX IF NOT EXISTS idx_machine_company ON machines(company_id);
CREATE INDEX IF NOT EXISTS idx_job_card_company ON job_cards(company_id);
CREATE INDEX IF NOT EXISTS idx_job_card_work_order ON job_cards(work_order_id);
CREATE INDEX IF NOT EXISTS idx_job_card_wo_item ON job_cards(work_order_item_id);
CREATE INDEX IF NOT EXISTS idx_schedule_company ON production_schedules(company_id);
CREATE INDEX IF NOT EXISTS idx_schedule_job_card ON production_schedules(job_card_id);
CREATE INDEX IF NOT EXISTS idx_execution_log_job_card ON execution_logs(job_card_id);
CREATE INDEX IF NOT EXISTS idx_quality_inspection_job_card ON quality_inspections(job_card_id);
