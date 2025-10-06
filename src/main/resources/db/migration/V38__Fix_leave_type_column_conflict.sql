-- Drop old leave_type column and clean up constraints to fix the null constraint issue
-- The old leave_type column is conflicting with the new leave_type_id column

-- First, drop any constraints that reference the old leave_type column
ALTER TABLE leave_requests DROP CONSTRAINT IF EXISTS chk_leave_type;

-- Drop the old leave_type column as it's been replaced by leave_type_id
ALTER TABLE leave_requests DROP COLUMN IF EXISTS leave_type;

-- Ensure leave_type_id is properly set and not nullable
-- Double-check data migration
UPDATE leave_requests lr
SET leave_type_id = (SELECT lt.id FROM leave_types lt WHERE lt.code = lr.leave_type_enum)
WHERE lr.leave_type_id IS NULL AND lr.leave_type_enum IS NOT NULL;

-- For any remaining null leave_type_id, set to ANNUAL_LEAVE as default
UPDATE leave_requests
SET leave_type_id = (SELECT id FROM leave_types WHERE code = 'ANNUAL_LEAVE')
WHERE leave_type_id IS NULL;

-- Ensure leave_type_id is not null (in case the previous migration didn't complete properly)
ALTER TABLE leave_requests ALTER COLUMN leave_type_id SET NOT NULL;