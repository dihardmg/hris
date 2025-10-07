-- Create annual_leave_balance table for better annual leave management
CREATE TABLE annual_leave_balance (
    id BIGSERIAL PRIMARY KEY,
    id_employee BIGINT NOT NULL,
    cuti_tahunan INT DEFAULT 12,          -- Annual leave quota (default 12 days)
    cuti_tahunan_terpakai INT DEFAULT 0,   -- Used annual leave days
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraint to employees table
    CONSTRAINT fk_annual_leave_employee
        FOREIGN KEY (id_employee) REFERENCES employees(id) ON DELETE CASCADE,

    -- Unique constraint to ensure one record per employee
    CONSTRAINT uc_employee_annual_leave
        UNIQUE (id_employee)
);

-- Create index for faster queries
CREATE INDEX idx_annual_leave_balance_employee_id ON annual_leave_balance(id_employee);

-- Migrate existing annual leave data from employees table
INSERT INTO annual_leave_balance (id_employee, cuti_tahunan, cuti_tahunan_terpakai, created_at, updated_at)
SELECT
    id,
    COALESCE(annual_leave_balance, 12) as cuti_tahunan,
    0 as cuti_tahunan_terpakai,  -- Reset to 0, will be calculated from approved leaves
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM employees
WHERE annual_leave_balance IS NOT NULL OR annual_leave_balance IS NULL;

-- Calculate used annual leave from existing approved leave requests
UPDATE annual_leave_balance alb
SET cuti_tahunan_terpakai = (
    SELECT COALESCE(SUM(lr.total_days), 0)
    FROM leave_requests lr
    JOIN leave_types lt ON lr.leave_type_id = lt.id
    WHERE lr.employee_id = alb.id_employee
      AND lr.status = 'APPROVED'
      AND lt.code = 'ANNUAL_LEAVE'
      AND EXTRACT(YEAR FROM lr.created_at) = EXTRACT(YEAR FROM CURRENT_DATE)
);