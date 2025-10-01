-- Insert default roles
INSERT INTO roles (name) VALUES
('ROLE_ADMIN'),
('ROLE_HR'),
('ROLE_SUPERVISOR'),
('ROLE_EMPLOYEE')
ON CONFLICT (name) DO NOTHING;

-- Insert default admin user
INSERT INTO employees (
    employee_id,
    first_name,
    last_name,
    email,
    password,
    phone_number,
    department_id,
    position_id,
    supervisor_id,
    hire_date,
    employment_status,
    annual_leave_balance,
    sick_leave_balance,
    is_active
) VALUES (
    'ADMIN001',
    'System',
    'Administrator',
    'admin@hris.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.2L.4f9i8H8u', -- password: admin123
    '0000000000',
    1,
    1,
    NULL,
    CURRENT_DATE,
    'ACTIVE',
    30,
    15,
    true
) ON CONFLICT (email) DO NOTHING;

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT e.id, r.id
FROM employees e, roles r
WHERE e.email = 'admin@hris.com' AND r.name = 'ROLE_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Also assign HR role to admin user for full access
INSERT INTO user_roles (user_id, role_id)
SELECT e.id, r.id
FROM employees e, roles r
WHERE e.email = 'admin@hris.com' AND r.name = 'ROLE_HR'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Also assign supervisor role to admin user for full access
INSERT INTO user_roles (user_id, role_id)
SELECT e.id, r.id
FROM employees e, roles r
WHERE e.email = 'admin@hris.com' AND r.name = 'ROLE_SUPERVISOR'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Also assign employee role to admin user for full access
INSERT INTO user_roles (user_id, role_id)
SELECT e.id, r.id
FROM employees e, roles r
WHERE e.email = 'admin@hris.com' AND r.name = 'ROLE_EMPLOYEE'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Insert sample HR user
INSERT INTO employees (
    employee_id,
    first_name,
    last_name,
    email,
    password,
    phone_number,
    department_id,
    position_id,
    supervisor_id,
    hire_date,
    employment_status,
    annual_leave_balance,
    sick_leave_balance,
    is_active
) VALUES (
    'HR001',
    'HR',
    'Manager',
    'hr@hris.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.2L.4f9i8H8u', -- password: admin123
    '0000000001',
    2,
    2,
    (SELECT id FROM employees WHERE email = 'admin@hris.com'),
    CURRENT_DATE - INTERVAL '30 days',
    'ACTIVE',
    25,
    12,
    true
) ON CONFLICT (email) DO NOTHING;

-- Assign HR role to HR user
INSERT INTO user_roles (user_id, role_id)
SELECT e.id, r.id
FROM employees e, roles r
WHERE e.email = 'hr@hris.com' AND r.name = 'ROLE_HR'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Also assign employee role to HR user
INSERT INTO user_roles (user_id, role_id)
SELECT e.id, r.id
FROM employees e, roles r
WHERE e.email = 'hr@hris.com' AND r.name = 'ROLE_EMPLOYEE'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Insert sample supervisor
INSERT INTO employees (
    employee_id,
    first_name,
    last_name,
    email,
    password,
    phone_number,
    department_id,
    position_id,
    supervisor_id,
    hire_date,
    employment_status,
    annual_leave_balance,
    sick_leave_balance,
    is_active
) VALUES (
    'SUP001',
    'Team',
    'Supervisor',
    'supervisor@hris.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.2L.4f9i8H8u', -- password: admin123
    '0000000002',
    1,
    3,
    (SELECT id FROM employees WHERE email = 'hr@hris.com'),
    CURRENT_DATE - INTERVAL '60 days',
    'ACTIVE',
    18,
    10,
    true
) ON CONFLICT (email) DO NOTHING;

-- Assign supervisor role to supervisor user
INSERT INTO user_roles (user_id, role_id)
SELECT e.id, r.id
FROM employees e, roles r
WHERE e.email = 'supervisor@hris.com' AND r.name = 'ROLE_SUPERVISOR'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Also assign employee role to supervisor user
INSERT INTO user_roles (user_id, role_id)
SELECT e.id, r.id
FROM employees e, roles r
WHERE e.email = 'supervisor@hris.com' AND r.name = 'ROLE_EMPLOYEE'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Insert sample regular employee
INSERT INTO employees (
    employee_id,
    first_name,
    last_name,
    email,
    password,
    phone_number,
    department_id,
    position_id,
    supervisor_id,
    hire_date,
    employment_status,
    annual_leave_balance,
    sick_leave_balance,
    is_active
) VALUES (
    'EMP001',
    'Regular',
    'Employee',
    'employee@hris.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.2L.4f9i8H8u', -- password: admin123
    '0000000003',
    1,
    4,
    (SELECT id FROM employees WHERE email = 'supervisor@hris.com'),
    CURRENT_DATE - INTERVAL '90 days',
    'ACTIVE',
    12,
    10,
    true
) ON CONFLICT (email) DO NOTHING;

-- Assign employee role to regular employee
INSERT INTO user_roles (user_id, role_id)
SELECT e.id, r.id
FROM employees e, roles r
WHERE e.email = 'employee@hris.com' AND r.name = 'ROLE_EMPLOYEE'
ON CONFLICT (user_id, role_id) DO NOTHING;