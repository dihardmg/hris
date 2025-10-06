-- Update all leave types to have minimum 1 day and maximum 90 days duration limits
-- This ensures consistency across all leave types and allows requests from 1 to 90 days

-- Update ANNUAL_LEAVE to use 1-90 days range
UPDATE leave_types
SET
    min_duration_days = 1,
    max_duration_days = 90,
    description = 'Cuti tahunan berbayar (bisa diajukan 1-90 hari)'
WHERE code = 'ANNUAL_LEAVE';

-- Update SICK_LEAVE to use 1-90 days range
UPDATE leave_types
SET
    min_duration_days = 1,
    max_duration_days = 90,
    description = 'Cuti sakit berbayar (bisa diajukan 1-90 hari)'
WHERE code = 'SICK_LEAVE';

-- Update MATERNITY_LEAVE to use 1-90 days range
UPDATE leave_types
SET
    min_duration_days = 1,
    max_duration_days = 90,
    description = 'Cuti melahirkan (bisa diajukan 1-90 hari)'
WHERE code = 'MATERNITY_LEAVE';

-- Update PATERNITY_LEAVE to use 1-90 days range
UPDATE leave_types
SET
    min_duration_days = 1,
    max_duration_days = 90,
    description = 'Cuti ayah (bisa diajukan 1-90 hari)'
WHERE code = 'PATERNITY_LEAVE';

-- Update UNPAID_LEAVE to use 1-90 days range
UPDATE leave_types
SET
    min_duration_days = 1,
    max_duration_days = 90,
    description = 'Cuti tidak berbayar (bisa diajukan 1-90 hari)'
WHERE code = 'UNPAID_LEAVE';

-- Update COMPASSIONATE_LEAVE to use 1-90 days range
UPDATE leave_types
SET
    min_duration_days = 1,
    max_duration_days = 90,
    description = 'Cuti berdukacita (bisa diajukan 1-90 hari)'
WHERE code = 'COMPASSIONATE_LEAVE';