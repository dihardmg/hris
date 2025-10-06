-- Update business_travel_requests table to handle city relationship properly
-- First, create the city_id column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'business_travel_requests'
        AND column_name = 'city_id'
    ) THEN
        ALTER TABLE business_travel_requests ADD COLUMN city_id BIGINT;
    END IF;
END $$;

-- Remove the old city column if it exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'business_travel_requests'
        AND column_name = 'city'
    ) THEN
        ALTER TABLE business_travel_requests DROP COLUMN city;
    END IF;
END $$;

-- Add foreign key constraint
ALTER TABLE business_travel_requests
ADD CONSTRAINT fk_business_travel_requests_city
FOREIGN KEY (city_id) REFERENCES cities(id);

-- Add index for better performance
CREATE INDEX IF NOT EXISTS idx_business_travel_requests_city_id
ON business_travel_requests(city_id);

-- Ensure city_id is not nullable for new records
ALTER TABLE business_travel_requests
ALTER COLUMN city_id SET NOT NULL;