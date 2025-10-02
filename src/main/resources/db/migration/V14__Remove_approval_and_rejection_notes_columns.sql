-- Remove approval_notes and rejection_reason columns from leave_requests table
-- As per new requirement: supervisors only click approve/reject without adding notes

ALTER TABLE leave_requests
DROP COLUMN IF EXISTS approval_notes;

ALTER TABLE leave_requests
DROP COLUMN IF EXISTS rejection_reason;