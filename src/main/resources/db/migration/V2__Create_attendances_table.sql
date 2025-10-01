-- Create attendances table
CREATE TABLE IF NOT EXISTS attendances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    clock_in_time TIMESTAMP,
    clock_out_time TIMESTAMP,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    location_address VARCHAR(500),
    is_within_geofence BOOLEAN,
    face_recognition_confidence DECIMAL(3, 2),
    attendance_type VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attendances_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_attendances_updated_at
    BEFORE UPDATE ON attendances
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add check constraints
ALTER TABLE attendances
ADD CONSTRAINT chk_attendance_type
CHECK (attendance_type IN ('REGULAR', 'OVERTIME', 'HOLIDAY', 'WEEKEND'));

ALTER TABLE attendances
ADD CONSTRAINT chk_clock_in_out_time
CHECK (clock_in_time IS NULL OR clock_out_time IS NULL OR clock_out_time > clock_in_time);

ALTER TABLE attendances
ADD CONSTRAINT chk_face_recognition_confidence
CHECK (face_recognition_confidence IS NULL OR (face_recognition_confidence >= 0 AND face_recognition_confidence <= 1));

ALTER TABLE attendances
ADD CONSTRAINT chk_latitude
CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90));

ALTER TABLE attendances
ADD CONSTRAINT chk_longitude
CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180));

-- Note: Unique constraint for daily attendance per employee is commented out due to PostgreSQL syntax issues
-- ALTER TABLE attendances
-- ADD CONSTRAINT uk_employee_daily_attendance
--     UNIQUE (employee_id, DATE(clock_in_time));