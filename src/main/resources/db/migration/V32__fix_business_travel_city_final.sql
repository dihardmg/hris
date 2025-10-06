-- Fix business travel requests schema final
-- This migration resolves the conflict between the old 'city' column and new 'city_id' column

-- First, ensure any existing records with city names have corresponding city_id values
-- For this migration, we'll map any existing city names to city IDs if possible
UPDATE business_travel_requests
SET city_id = (
    SELECT id FROM cities
    WHERE LOWER(cities.city_name) = LOWER(business_travel_requests.city)
    LIMIT 1
)
WHERE city_id IS NULL AND city IS NOT NULL;

-- For any remaining records that couldn't be matched, set a default city_id
-- (assuming there's at least one city in the cities table)
UPDATE business_travel_requests
SET city_id = (SELECT MIN(id) FROM cities)
WHERE city_id IS NULL;

-- Now make city_id column NOT NULL
ALTER TABLE business_travel_requests
ALTER COLUMN city_id SET NOT NULL;

-- The foreign key constraint already ensures city_id references a valid city
-- No additional check constraint needed

-- Finally, drop the old city column as it's no longer needed
ALTER TABLE business_travel_requests
DROP COLUMN city;