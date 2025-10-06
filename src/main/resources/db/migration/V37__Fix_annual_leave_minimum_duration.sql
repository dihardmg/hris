-- Fix minimum duration for ANNUAL_LEAVE to ensure it's set to 1 day
UPDATE leave_types
SET min_duration_days = 1, description = 'Cuti tahunan berbayar (bisa diajukan 1-12 hari)'
WHERE code = 'ANNUAL_LEAVE' AND (min_duration_days != 1 OR description IS NULL OR description NOT LIKE '%1-12 hari%');