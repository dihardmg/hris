-- Remove legacy columns from leave_requests table
-- These columns are no longer needed since we now use leave_type_id and calculate remaining balance in real-time

-- Drop index for leave_type_enum if it exists
DROP INDEX IF EXISTS idx_leave_requests_leave_type_enum;

-- Remove the legacy leave_type_enum column (used for backward compatibility during migration)
ALTER TABLE leave_requests DROP COLUMN IF EXISTS leave_type_enum;

-- Remove the remaining_balance column (now calculated in real-time via service layer)
ALTER TABLE leave_requests DROP COLUMN IF EXISTS remaining_balance;