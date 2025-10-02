-- Remove approved_by column from leave_requests table
-- This column is redundant with updated_by column which already tracks who approved/rejected requests

-- Drop foreign key constraint first
ALTER TABLE leave_requests
DROP CONSTRAINT IF EXISTS fk_leave_requests_approved_by;

-- Drop the column
ALTER TABLE leave_requests
DROP COLUMN IF EXISTS approved_by;

-- Remove index if it exists
DROP INDEX IF EXISTS idx_leave_requests_approved_by;