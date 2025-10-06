-- Configure leave types with specific quota rules and duration limits
-- Based on requirements:
-- 1. All leave types: minimum 1 day
-- 2. Only ANNUAL_LEAVE has quota (12 days) and will reduce balance
-- 3. Other leave types: no quota, but have maximum duration limits
-- 4. MATERNITY_LEAVE: maximum 90 days, no quota deduction

-- Update ANNUAL_LEAVE - has quota, max 12 days
UPDATE leave_types
SET
    min_duration_days = 1,
    max_duration_days = 12,
    has_balance_quota = TRUE,
    description = 'Cuti tahunan berbayar dengan quota (bisa diajukan 1-12 hari, akan mengurangi quota)'
WHERE code = 'ANNUAL_LEAVE';

-- Update SICK_LEAVE - no quota, reasonable max duration
UPDATE leave_types
SET
    min_duration_days = 1,
    max_duration_days = 30,
    has_balance_quota = FALSE,
    description = 'Cuti sakit berbayar (bisa diajukan 1-30 hari, tidak mengurangi quota)'
WHERE code = 'SICK_LEAVE';

-- Update MATERNITY_LEAVE - no quota, max 90 days
UPDATE leave_types
SET
    min_duration_days = 1,
    max_duration_days = 90,
    has_balance_quota = FALSE,
    description = 'Cuti melahirkan (bisa diajukan 1-90 hari, tidak mengurangi quota)'
WHERE code = 'MATERNITY_LEAVE';

-- Update PATERNITY_LEAVE - no quota, reasonable max duration
UPDATE leave_types
SET
    min_duration_days = 1,
    max_duration_days = 7,
    has_balance_quota = FALSE,
    description = 'Cuti ayah (bisa diajukan 1-7 hari, tidak mengurangi quota)'
WHERE code = 'PATERNITY_LEAVE';

-- Update UNPAID_LEAVE - no quota, reasonable max duration
UPDATE leave_types
SET
    min_duration_days = 1,
    max_duration_days = 30,
    has_balance_quota = FALSE,
    description = 'Cuti tidak berbayar (bisa diajukan 1-30 hari, tidak mengurangi quota)'
WHERE code = 'UNPAID_LEAVE';

-- Update COMPASSIONATE_LEAVE - no quota, reasonable max duration
UPDATE leave_types
SET
    min_duration_days = 1,
    max_duration_days = 7,
    has_balance_quota = FALSE,
    description = 'Cuti berdukacita (bisa diajukan 1-7 hari, tidak mengurangi quota)'
WHERE code = 'COMPASSIONATE_LEAVE';