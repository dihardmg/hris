-- Migration: Remove approval and audit fields from business_travel_requests table
-- This migration simplifies the table by removing approval and audit tracking fields

-- Remove indexes that reference the columns being dropped
DROP INDEX IF EXISTS idx_business_travel_requests_approved_by;
DROP INDEX IF EXISTS idx_business_travel_requests_created_by;
DROP INDEX IF EXISTS idx_business_travel_requests_updated_by;

-- Remove foreign key constraints for the columns being dropped
ALTER TABLE business_travel_requests
DROP CONSTRAINT IF EXISTS fk_business_travel_requests_approved_by;

ALTER TABLE business_travel_requests
DROP CONSTRAINT IF EXISTS fk_business_travel_requests_created_by;

ALTER TABLE business_travel_requests
DROP CONSTRAINT IF EXISTS fk_business_travel_requests_updated_by;

-- Drop the specified columns
ALTER TABLE business_travel_requests
DROP COLUMN IF EXISTS approval_date;

ALTER TABLE business_travel_requests
DROP COLUMN IF EXISTS approved_by_id;

ALTER TABLE business_travel_requests
DROP COLUMN IF EXISTS created_by_id;

ALTER TABLE business_travel_requests
DROP COLUMN IF EXISTS rejection_reason;

ALTER TABLE business_travel_requests
DROP COLUMN IF EXISTS updated_by_id;

ALTER TABLE business_travel_requests
DROP COLUMN IF EXISTS approved_by;

-- Recreate indexes for performance (keeping only essential ones)
CREATE INDEX IF NOT EXISTS idx_business_travel_requests_employee_id ON business_travel_requests (employee_id);
CREATE INDEX IF NOT EXISTS idx_business_travel_requests_status ON business_travel_requests (status);
CREATE INDEX IF NOT EXISTS idx_business_travel_requests_start_date ON business_travel_requests (start_date);
CREATE INDEX IF NOT EXISTS idx_business_travel_requests_created_at ON business_travel_requests (created_at);
CREATE INDEX IF NOT EXISTS idx_business_travel_requests_uuid ON business_travel_requests ("uuid");