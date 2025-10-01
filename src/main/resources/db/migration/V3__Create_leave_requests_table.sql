-- Create leave_requests table
CREATE TABLE IF NOT EXISTS leave_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INTEGER NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT,
    approval_date TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_leave_requests_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_requests_approved_by
        FOREIGN KEY (approved_by) REFERENCES employees(id) ON DELETE SET NULL
);

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_leave_requests_updated_at
    BEFORE UPDATE ON leave_requests
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add check constraints
ALTER TABLE leave_requests
ADD CONSTRAINT chk_leave_type
CHECK (leave_type IN ('ANNUAL_LEAVE', 'SICK_LEAVE', 'MATERNITY_LEAVE', 'PATERNITY_LEAVE', 'UNPAID_LEAVE', 'COMPASSIONATE_LEAVE'));

ALTER TABLE leave_requests
ADD CONSTRAINT chk_leave_status
CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'));

ALTER TABLE leave_requests
ADD CONSTRAINT chk_leave_dates
CHECK (end_date >= start_date);

ALTER TABLE leave_requests
ADD CONSTRAINT chk_total_days
CHECK (total_days > 0);

ALTER TABLE leave_requests
ADD CONSTRAINT chk_approval_consistency
CHECK (
    (status = 'APPROVED' AND approved_by IS NOT NULL AND approval_date IS NOT NULL) OR
    (status = 'REJECTED' AND approved_by IS NOT NULL AND approval_date IS NOT NULL) OR
    (status IN ('PENDING', 'CANCELLED') AND approved_by IS NULL AND approval_date IS NULL)
);