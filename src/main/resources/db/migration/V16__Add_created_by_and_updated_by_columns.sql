-- Add created_by and updated_by columns to leave_requests table for better audit trail
-- created_by: ID of user who submitted the leave request
-- updated_by: ID of user who approved/rejected the leave request

ALTER TABLE leave_requests
ADD COLUMN created_by BIGINT;

ALTER TABLE leave_requests
ADD COLUMN updated_by BIGINT;

-- Add foreign key constraints
ALTER TABLE leave_requests
ADD CONSTRAINT fk_leave_requests_created_by
FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE SET NULL;

ALTER TABLE leave_requests
ADD CONSTRAINT fk_leave_requests_updated_by
FOREIGN KEY (updated_by) REFERENCES employees(id) ON DELETE SET NULL;

-- Add indexes for better query performance
CREATE INDEX idx_leave_requests_created_by ON leave_requests(created_by);
CREATE INDEX idx_leave_requests_updated_by ON leave_requests(updated_by);

-- Update existing records to set created_by from employee_id
UPDATE leave_requests
SET created_by = employee_id
WHERE created_by IS NULL AND employee_id IS NOT NULL;