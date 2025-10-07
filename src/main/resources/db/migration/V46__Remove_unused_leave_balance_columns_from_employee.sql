-- Remove unused leave balance columns from employee table
-- These columns are no longer needed since we're using hr_quota table for leave tracking

-- Drop check constraints first
ALTER TABLE employees DROP CONSTRAINT IF EXISTS chk_annual_leave_balance;

ALTER TABLE employees DROP CONSTRAINT IF EXISTS chk_sick_leave_balance;

-- Remove the columns
ALTER TABLE employees DROP COLUMN IF EXISTS annual_leave_balance;

ALTER TABLE employees DROP COLUMN IF EXISTS sick_leave_balance;

-- Add comment to document the change
COMMENT ON TABLE employees IS 'Employee table - leave balance tracking moved to hr_quota table';