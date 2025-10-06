-- Add leave_type_id column to leave_requests table
ALTER TABLE leave_requests ADD COLUMN leave_type_id BIGINT;

-- Add foreign key constraint
ALTER TABLE leave_requests ADD CONSTRAINT fk_leave_requests_leave_type
FOREIGN KEY (leave_type_id) REFERENCES leave_types(id);

-- Add leave_type_enum column for backward compatibility
ALTER TABLE leave_requests ADD COLUMN leave_type_enum VARCHAR(50);

-- Update existing records to use new leave type relationship
UPDATE leave_requests
SET leave_type_id = (SELECT lt.id FROM leave_types lt WHERE leave_requests.leave_type = lt.code),
    leave_type_enum = leave_type;

-- Make leave_type_id not nullable after data migration
-- PostgreSQL syntax
ALTER TABLE leave_requests ALTER COLUMN leave_type_id SET NOT NULL;

-- Add index for better performance
CREATE INDEX idx_leave_requests_leave_type_id ON leave_requests(leave_type_id);
CREATE INDEX idx_leave_requests_leave_type_enum ON leave_requests(leave_type_enum);