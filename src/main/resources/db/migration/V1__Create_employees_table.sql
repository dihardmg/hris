-- Create employees table
CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    department_id BIGINT,
    position_id BIGINT,
    supervisor_id BIGINT REFERENCES employees(id),
    face_template BYTEA,
    hire_date DATE,
    employment_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    annual_leave_balance INTEGER NOT NULL DEFAULT 12,
    sick_leave_balance INTEGER NOT NULL DEFAULT 10,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_employees_updated_at
    BEFORE UPDATE ON employees
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add check constraints
ALTER TABLE employees
ADD CONSTRAINT chk_employment_status
CHECK (employment_status IN ('ACTIVE', 'ON_LEAVE', 'TERMINATED', 'PROBATION'));

ALTER TABLE employees
ADD CONSTRAINT chk_annual_leave_balance
CHECK (annual_leave_balance >= 0);

ALTER TABLE employees
ADD CONSTRAINT chk_sick_leave_balance
CHECK (sick_leave_balance >= 0);