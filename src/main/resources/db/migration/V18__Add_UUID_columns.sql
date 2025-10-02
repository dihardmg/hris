-- Add UUID columns to attendances and leave_requests tables for API security
-- This prevents IDOR (Insecure Direct Object Reference) attacks

-- Add UUID column to attendances table
ALTER TABLE attendances
ADD COLUMN uuid UUID NOT NULL DEFAULT gen_random_uuid();

-- Create unique index on UUID for attendances
CREATE UNIQUE INDEX idx_attendances_uuid ON attendances(uuid);

-- Add UUID column to leave_requests table
ALTER TABLE leave_requests
ADD COLUMN uuid UUID NOT NULL DEFAULT gen_random_uuid();

-- Create unique index on UUID for leave_requests
CREATE UNIQUE INDEX idx_leave_requests_uuid ON leave_requests(uuid);

-- Add comments to document the purpose of UUID columns
COMMENT ON COLUMN attendances.uuid IS 'Public UUID identifier for API endpoints to prevent IDOR attacks';
COMMENT ON COLUMN leave_requests.uuid IS 'Public UUID identifier for API endpoints to prevent IDOR attacks';