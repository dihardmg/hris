-- Revert to the original BCrypt hash from V6 migration for "admin123"
UPDATE employees
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.2L.4f9i8H8u'
WHERE email IN ('admin@hris.com', 'hr@hris.com', 'supervisor@hris.com', 'employee@hris.com');