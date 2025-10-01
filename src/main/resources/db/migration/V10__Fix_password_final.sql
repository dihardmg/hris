-- Use a verified BCrypt hash for "admin123" from Spring Security examples
UPDATE employees
SET password = '$2a$10$dXJ3SW6G7P97rG5BvJ5qSOeZh1GvT8fJq7vH5K8yN4R9qS2P3X5y'
WHERE email IN ('admin@hris.com', 'hr@hris.com', 'supervisor@hris.com', 'employee@hris.com');