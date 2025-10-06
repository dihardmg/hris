-- Add remaining_balance column to leave_requests table
ALTER TABLE leave_requests ADD COLUMN remaining_balance INT;

-- Set default value for existing records (they will be calculated when created/approved)
UPDATE leave_requests SET remaining_balance = 0;