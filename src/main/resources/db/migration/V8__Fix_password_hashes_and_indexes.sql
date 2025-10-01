-- Fix password hashes for all users - use proper BCrypt hashes for "admin123"
UPDATE employees
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email IN ('admin@hris.com', 'hr@hris.com', 'supervisor@hris.com', 'employee@hris.com');

-- Create indexes for better performance

-- Employees table indexes
CREATE INDEX IF NOT EXISTS idx_employees_email ON employees(email);
CREATE INDEX IF NOT EXISTS idx_employees_employee_id ON employees(employee_id);
CREATE INDEX IF NOT EXISTS idx_employees_supervisor_id ON employees(supervisor_id);
CREATE INDEX IF NOT EXISTS idx_employees_department_id ON employees(department_id);
CREATE INDEX IF NOT EXISTS idx_employees_position_id ON employees(position_id);
CREATE INDEX IF NOT EXISTS idx_employment_status ON employees(employment_status);
CREATE INDEX IF NOT EXISTS idx_employees_active ON employees(is_active);

-- Attendances table indexes
CREATE INDEX IF NOT EXISTS idx_attendances_employee_id ON attendances(employee_id);
CREATE INDEX IF NOT EXISTS idx_attendances_clock_in_time ON attendances(clock_in_time);
CREATE INDEX IF NOT EXISTS idx_attendances_clock_out_time ON attendances(clock_out_time);
CREATE INDEX IF NOT EXISTS idx_attendances_created_at ON attendances(created_at);
CREATE INDEX IF NOT EXISTS idx_attendances_attendance_type ON attendances(attendance_type);
CREATE INDEX IF NOT EXISTS idx_attendances_geofence ON attendances(is_within_geofence);
CREATE INDEX IF NOT EXISTS idx_attendances_employee_date ON attendances(employee_id, DATE(clock_in_time));

-- Leave requests table indexes
CREATE INDEX IF NOT EXISTS idx_leave_requests_employee_id ON leave_requests(employee_id);
CREATE INDEX IF NOT EXISTS idx_leave_requests_status ON leave_requests(status);
CREATE INDEX IF NOT EXISTS idx_leave_requests_leave_type ON leave_requests(leave_type);
CREATE INDEX IF NOT EXISTS idx_leave_requests_dates ON leave_requests(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_leave_requests_created_at ON leave_requests(created_at);
CREATE INDEX IF NOT EXISTS idx_leave_requests_approved_by ON leave_requests(approved_by);

-- Business travel requests table indexes
CREATE INDEX IF NOT EXISTS idx_business_travel_employee_id ON business_travel_requests(employee_id);
CREATE INDEX IF NOT EXISTS idx_business_travel_status ON business_travel_requests(status);
CREATE INDEX IF NOT EXISTS idx_business_travel_dates ON business_travel_requests(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_business_travel_destination ON business_travel_requests(destination);
CREATE INDEX IF NOT EXISTS idx_business_travel_created_at ON business_travel_requests(created_at);
CREATE INDEX IF NOT EXISTS idx_business_travel_approved_by ON business_travel_requests(approved_by);

-- User roles table indexes
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

-- Composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_attendances_employee_date_type ON attendances(employee_id, DATE(clock_in_time), attendance_type);
CREATE INDEX IF NOT EXISTS idx_leave_requests_employee_status_type ON leave_requests(employee_id, status, leave_type);
CREATE INDEX IF NOT EXISTS idx_business_travel_employee_status_dates ON business_travel_requests(employee_id, status, start_date, end_date);

-- Index for supervisor queries
CREATE INDEX IF NOT EXISTS idx_leave_requests_supervisor_pending ON leave_requests(approved_by)
WHERE status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_business_travel_supervisor_pending ON business_travel_requests(approved_by)
WHERE status = 'PENDING';