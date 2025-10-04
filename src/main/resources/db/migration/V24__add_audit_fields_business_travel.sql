-- Migration: Add created_by and updated_by columns to business_travel_requests table
-- This migration adds back audit tracking fields with proper foreign key relationships

-- Add created_by column
ALTER TABLE business_travel_requests
ADD COLUMN created_by BIGINT;

-- Add updated_by column
ALTER TABLE business_travel_requests
ADD COLUMN updated_by BIGINT;

-- Add foreign key constraints
ALTER TABLE business_travel_requests
ADD CONSTRAINT fk_business_travel_requests_created_by
FOREIGN KEY (created_by) REFERENCES employees(id);

ALTER TABLE business_travel_requests
ADD CONSTRAINT fk_business_travel_requests_updated_by
FOREIGN KEY (updated_by) REFERENCES employees(id);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_business_travel_requests_created_by ON business_travel_requests (created_by);
CREATE INDEX IF NOT EXISTS idx_business_travel_requests_updated_by ON business_travel_requests (updated_by);