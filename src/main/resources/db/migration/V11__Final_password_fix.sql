-- Update all user passwords with the correct BCrypt hash for "admin123"
UPDATE employees
SET password = '$2a$10$QiLa8cZm9p/K1jYp99aGEeD.cvXJJRYJjiFUwxBWDyxycOB0lSmde'
WHERE email IN ('admin@hris.com', 'hr@hris.com', 'supervisor@hris.com', 'employee@hris.com');