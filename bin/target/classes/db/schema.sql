-- HMS prototype schema — MySQL 8 (idempotent CREATE)
-- Soft delete: deleted_at IS NULL means active row.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(128) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(32) NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    suspended TINYINT(1) NOT NULL DEFAULT 0,
    revoked_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS patient_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    mrn VARCHAR(32) NOT NULL UNIQUE,
    blood_group VARCHAR(8) NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_patient_profile_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    bed_type VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS beds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ward_id BIGINT NOT NULL,
    bed_code VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_ward_bed (ward_id, bed_code),
    CONSTRAINT fk_bed_ward FOREIGN KEY (ward_id) REFERENCES wards (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_user_id BIGINT NOT NULL,
    booked_by_user_id BIGINT NOT NULL,
    scheduled_at DATETIME(6) NOT NULL,
    booking_channel VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_PAYMENT',
    consultation_fee DECIMAL(12,2) NOT NULL DEFAULT 0,
    notes VARCHAR(512) NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_user_id) REFERENCES users (id),
    CONSTRAINT fk_appt_booker FOREIGN KEY (booked_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_user_id BIGINT NOT NULL,
    reference_type VARCHAR(64) NOT NULL,
    reference_id BIGINT NULL,
    amount DECIMAL(12,2) NOT NULL,
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,
    gst_invoice TINYINT(1) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_bill_patient FOREIGN KEY (patient_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    mode VARCHAR(32) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    external_ref VARCHAR(255) NULL,
    received_by_user_id BIGINT NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_payment_bill FOREIGN KEY (bill_id) REFERENCES bills (id),
    CONSTRAINT fk_payment_receiver FOREIGN KEY (received_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS visits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE,
    doctor_user_id BIGINT NOT NULL,
    assistant_user_id BIGINT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'CHECKED_IN',
    checked_in_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_visit_appt FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    CONSTRAINT fk_visit_doctor FOREIGN KEY (doctor_user_id) REFERENCES users (id),
    CONSTRAINT fk_visit_assistant FOREIGN KEY (assistant_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS prescriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visit_id BIGINT NOT NULL UNIQUE,
    instructions VARCHAR(1024) NULL,
    printed_at DATETIME(6) NULL,
    pharmacy_opt_in TINYINT(1) NOT NULL DEFAULT 0,
    pharmacy_opt_in_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_rx_visit FOREIGN KEY (visit_id) REFERENCES visits (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS prescription_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    medicine_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(128) NOT NULL,
    frequency VARCHAR(128) NOT NULL,
    duration_days INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL DEFAULT 0,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_rxline_rx FOREIGN KEY (prescription_id) REFERENCES prescriptions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pharmacy_line_fulfillments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_line_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    bill_id BIGINT NULL,
    dispensed_at DATETIME(6) NULL,
    pharmacist_user_id BIGINT NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_pharma_line FOREIGN KEY (prescription_line_id) REFERENCES prescription_lines (id),
    CONSTRAINT fk_pharma_bill FOREIGN KEY (bill_id) REFERENCES bills (id),
    CONSTRAINT fk_pharma_pharmacist FOREIGN KEY (pharmacist_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS admissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_user_id BIGINT NOT NULL,
    bed_id BIGINT NOT NULL,
    admission_type VARCHAR(64) NOT NULL,
    admitted_at DATETIME(6) NOT NULL,
    discharged_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_admission_patient FOREIGN KEY (patient_user_id) REFERENCES users (id),
    CONSTRAINT fk_admission_bed FOREIGN KEY (bed_id) REFERENCES beds (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS attendance_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_user_id BIGINT NOT NULL,
    log_date DATE NOT NULL,
    check_in DATETIME(6) NULL,
    check_out DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_staff_day (staff_user_id, log_date),
    CONSTRAINT fk_att_staff FOREIGN KEY (staff_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_user_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(512) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    decided_by_user_id BIGINT NULL,
    decided_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_leave_staff FOREIGN KEY (staff_user_id) REFERENCES users (id),
    CONSTRAINT fk_leave_decider FOREIGN KEY (decided_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS canteen_menu_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(512) NULL,
    price DECIMAL(12,2) NOT NULL,
    available TINYINT(1) NOT NULL DEFAULT 1,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS canteen_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    placed_by_user_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PLACED',
    payment_status VARCHAR(32) NOT NULL DEFAULT 'UNPAID',
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_canteen_user FOREIGN KEY (placed_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS canteen_order_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_col_order FOREIGN KEY (order_id) REFERENCES canteen_orders (id),
    CONSTRAINT fk_col_item FOREIGN KEY (menu_item_id) REFERENCES canteen_menu_items (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS patient_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_user_id BIGINT NOT NULL,
    doc_type VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    storage_uri VARCHAR(1024) NOT NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_doc_patient FOREIGN KEY (patient_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS medical_waste_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_code VARCHAR(64) NOT NULL,
    description VARCHAR(512) NULL,
    quantity_value DECIMAL(12,4) NOT NULL,
    quantity_unit VARCHAR(32) NOT NULL,
    segregation_status VARCHAR(64) NOT NULL,
    storage_location VARCHAR(255) NULL,
    handed_over_to_authorised_agent TINYINT(1) NOT NULL DEFAULT 0,
    cbwtf_manifest_number VARCHAR(128) NULL,
    recorded_by_user_id BIGINT NOT NULL,
    incident_notes VARCHAR(1024) NULL,
    recorded_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_waste_recorder FOREIGN KEY (recorded_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Supplier / vendor master (procurement)
CREATE TABLE IF NOT EXISTS vendors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    party_type VARCHAR(32) NOT NULL,
    legal_name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255) NULL,
    gstin VARCHAR(32) NULL,
    pan VARCHAR(20) NULL,
    contact_person VARCHAR(255) NULL,
    phone VARCHAR(32) NULL,
    email VARCHAR(255) NULL,
    address VARCHAR(1024) NULL,
    payment_terms_days INT NOT NULL DEFAULT 0,
    active TINYINT(1) NOT NULL DEFAULT 1,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payroll_components (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    component_kind VARCHAR(32) NOT NULL,
    taxable TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS staff_salary_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_user_id BIGINT NOT NULL,
    payroll_component_id BIGINT NOT NULL,
    amount_monthly DECIMAL(12,2) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_ssa_staff FOREIGN KEY (staff_user_id) REFERENCES users (id),
    CONSTRAINT fk_ssa_comp FOREIGN KEY (payroll_component_id) REFERENCES payroll_components (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payroll_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_year INT NOT NULL,
    period_month INT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    finalized_at DATETIME(6) NULL,
    created_by_user_id BIGINT NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_payroll_period (period_year, period_month),
    CONSTRAINT fk_payroll_creator FOREIGN KEY (created_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payroll_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payroll_run_id BIGINT NOT NULL,
    staff_user_id BIGINT NOT NULL,
    payroll_component_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_pl_run FOREIGN KEY (payroll_run_id) REFERENCES payroll_runs (id),
    CONSTRAINT fk_pl_staff FOREIGN KEY (staff_user_id) REFERENCES users (id),
    CONSTRAINT fk_pl_comp FOREIGN KEY (payroll_component_id) REFERENCES payroll_components (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS misc_expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(128) NOT NULL,
    description VARCHAR(1024) NULL,
    amount DECIMAL(12,2) NOT NULL,
    expense_date DATE NOT NULL,
    vendor_id BIGINT NULL,
    recorded_by_user_id BIGINT NOT NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_misc_vendor FOREIGN KEY (vendor_id) REFERENCES vendors (id),
    CONSTRAINT fk_misc_recorder FOREIGN KEY (recorded_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Extend staff daily attendance (ignore errors if columns already exist — continue-on-error)
ALTER TABLE attendance_logs ADD COLUMN shift_code VARCHAR(32) NULL;
ALTER TABLE attendance_logs ADD COLUMN work_location VARCHAR(255) NULL;
ALTER TABLE attendance_logs ADD COLUMN remarks VARCHAR(512) NULL;
ALTER TABLE attendance_logs ADD COLUMN day_status VARCHAR(32) NOT NULL DEFAULT 'PRESENT';
ALTER TABLE attendance_logs ADD COLUMN approved_by_user_id BIGINT NULL;
ALTER TABLE attendance_logs ADD COLUMN approved_at DATETIME(6) NULL;
ALTER TABLE attendance_logs ADD CONSTRAINT fk_att_approver FOREIGN KEY (approved_by_user_id) REFERENCES users (id);

-- Alphabetic bucket + normalized name for patient search; IST calendar date for appointment/revenue pruning (errors ignored if duplicate)
ALTER TABLE users ADD COLUMN sort_name_norm VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE users ADD COLUMN name_bucket CHAR(1) NOT NULL DEFAULT '#';
ALTER TABLE appointments ADD COLUMN scheduled_local_date DATE NULL;
ALTER TABLE bills ADD COLUMN business_date DATE NULL;
ALTER TABLE payments ADD COLUMN business_date DATE NULL;
CREATE INDEX idx_users_bucket_sort ON users (name_bucket, sort_name_norm);
CREATE INDEX idx_appts_day_status ON appointments (scheduled_local_date, status);
CREATE INDEX idx_bills_business_date ON bills (business_date);
CREATE INDEX idx_payments_business_date ON payments (business_date);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    user_id BIGINT NULL,
    username VARCHAR(128) NULL,
    http_method VARCHAR(16) NOT NULL,
    request_uri VARCHAR(512) NOT NULL,
    response_status INT NOT NULL,
    client_ip VARCHAR(64) NULL,
    detail VARCHAR(512) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
