-- Migration: Make updated_at column nullable in business_travel_requests table
-- This migration allows updated_at to be null on initial submit and only set during approval/rejection workflow

-- Drop the NOT NULL constraint from updated_at column
ALTER TABLE business_travel_requests
ALTER COLUMN updated_at DROP NOT NULL;