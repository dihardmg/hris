-- Remove approval_date and approved_by_id columns from leave_requests table
-- These are redundant with updated_at field which will track approval/rejection timestamps

ALTER TABLE leave_requests
DROP COLUMN IF EXISTS approval_date;

ALTER TABLE leave_requests
DROP COLUMN IF EXISTS approved_by_id;