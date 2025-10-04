-- Migration: Update business_travel_requests table to match new simplified structure
-- This migration creates the final table structure as specified

-- Drop existing table if it exists to start fresh
DROP TABLE IF EXISTS business_travel_requests CASCADE;

-- Create the new business_travel_requests table with the specified structure
CREATE TABLE business_travel_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    city VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INTEGER NOT NULL,
    reason VARCHAR(255),
    status VARCHAR(255) DEFAULT 'PENDING' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    "uuid" UUID DEFAULT gen_random_uuid() NOT NULL,

    -- Foreign key constraints
    CONSTRAINT fk_business_travel_requests_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_business_travel_requests_created_by
        FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE SET NULL,
    CONSTRAINT fk_business_travel_requests_updated_by
        FOREIGN KEY (updated_by) REFERENCES employees(id) ON DELETE SET NULL
);

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_business_travel_requests_updated_at
    BEFORE UPDATE ON business_travel_requests
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add unique constraint for UUID
ALTER TABLE business_travel_requests
ADD CONSTRAINT uk_business_travel_requests_uuid UNIQUE ("uuid");

-- Add indexes for better performance
CREATE INDEX idx_business_travel_requests_employee_id ON business_travel_requests (employee_id);
CREATE INDEX idx_business_travel_requests_status ON business_travel_requests (status);
CREATE INDEX idx_business_travel_requests_start_date ON business_travel_requests (start_date);
CREATE INDEX idx_business_travel_requests_created_at ON business_travel_requests (created_at);
CREATE INDEX idx_business_travel_requests_uuid ON business_travel_requests ("uuid");

-- Add check constraints
ALTER TABLE business_travel_requests
ADD CONSTRAINT chk_business_travel_status
CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'COMPLETED'));

ALTER TABLE business_travel_requests
ADD CONSTRAINT chk_travel_dates
CHECK (end_date >= start_date);

ALTER TABLE business_travel_requests
ADD CONSTRAINT chk_total_days_positive
CHECK (total_days > 0);