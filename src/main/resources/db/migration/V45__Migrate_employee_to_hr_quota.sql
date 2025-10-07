-- Migrate data from employees table to hr_quota table
-- This migration copies existing annual leave data from employees to hr_quota for the current year
-- Only creates records for employees who don't already have hr_quota records for 2025

-- Insert into hr_quota for all active employees who don't have quota records for 2025
INSERT INTO hr_quota (id_employee, cuti_tahunan, cuti_tahunan_terpakai, tahun, created_at, updated_at)
SELECT
    e.id as id_employee,
    COALESCE(e.annual_leave_balance, 12) as cuti_tahunan,
    0 as cuti_tahunan_terpakai, -- Start with 0 used days for new quota system
    2025 as tahun,
    CURRENT_TIMESTAMP as created_at,
    CURRENT_TIMESTAMP as updated_at
FROM employees e
WHERE e.is_active = true
AND NOT EXISTS (
    SELECT 1 FROM hr_quota h
    WHERE h.id_employee = e.id AND h.tahun = 2025
);

-- Log migration results for verification
DO $$
DECLARE
    employee_count INTEGER;
    migrated_count INTEGER;
BEGIN
    -- Count total active employees
    SELECT COUNT(*) INTO employee_count FROM employees WHERE is_active = true;

    -- Count how many were migrated (should be same as above if no existing quotas)
    SELECT COUNT(*) INTO migrated_count FROM hr_quota WHERE tahun = 2025;

    RAISE NOTICE 'Migration completed: % active employees, % hr_quota records for 2025',
                 employee_count, migrated_count;
END $$;

-- Create index for performance if not exists
CREATE INDEX IF NOT EXISTS idx_hr_quota_employee_year_2025 ON hr_quota(id_employee, tahun) WHERE tahun = 2025;