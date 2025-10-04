-- Add date column to attendances table for easier reporting and filtering
-- Check if column already exists to avoid errors
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'attendances'
        AND column_name = 'date'
    ) THEN
        ALTER TABLE attendances ADD COLUMN date DATE;
    END IF;
END $$;

-- Create indexes only if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_indexes
        WHERE tablename = 'attendances'
        AND indexname = 'idx_attendances_date'
    ) THEN
        CREATE INDEX idx_attendances_date ON attendances(date);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_indexes
        WHERE tablename = 'attendances'
        AND indexname = 'idx_attendances_employee_date'
    ) THEN
        CREATE INDEX idx_attendances_employee_date ON attendances(employee_id, date);
    END IF;
END $$;

-- Update existing records to populate date from clock_in_time
UPDATE attendances
SET date = DATE(clock_in_time)
WHERE clock_in_time IS NOT NULL AND date IS NULL;

-- For records with only clock_out_time, use that date
UPDATE attendances
SET date = DATE(clock_out_time)
WHERE date IS NULL AND clock_out_time IS NOT NULL;

-- Add NOT NULL constraint after updating existing records
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'attendances'
        AND column_name = 'date'
        AND is_nullable = 'YES'
    ) THEN
        ALTER TABLE attendances ALTER COLUMN date SET NOT NULL;
    END IF;
END $$;

-- Add unique constraint only if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_name = 'attendances'
        AND constraint_name = 'uk_employee_daily_attendance'
    ) THEN
        ALTER TABLE attendances
        ADD CONSTRAINT uk_employee_daily_attendance
            UNIQUE (employee_id, date);
    END IF;
END $$;

-- Add check constraint only if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.check_constraints
        WHERE constraint_name = 'chk_attendance_date_consistency'
    ) THEN
        ALTER TABLE attendances
        ADD CONSTRAINT chk_attendance_date_consistency
        CHECK (
            (clock_in_time IS NOT NULL AND date = DATE(clock_in_time)) OR
            (clock_in_time IS NULL AND clock_out_time IS NOT NULL AND date = DATE(clock_out_time))
        );
    END IF;
END $$;