-- Create hr_quota table for leave management
CREATE TABLE hr_quota (
    id BIGSERIAL PRIMARY KEY,
    id_employee BIGINT NOT NULL,
    cuti_tahunan INT NOT NULL DEFAULT 12,
    cuti_tahunan_terpakai INT NOT NULL DEFAULT 0,
    tahun INTEGER NOT NULL, -- YEAR is not a standard PostgreSQL type
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hr_quota_employee FOREIGN KEY (id_employee) REFERENCES employees(id),
    CONSTRAINT unique_employee_year UNIQUE (id_employee, tahun)
);

-- Add index for performance
CREATE INDEX idx_hr_quota_employee_year ON hr_quota(id_employee, tahun);
CREATE INDEX idx_hr_quota_tahun ON hr_quota(tahun);

-- Create trigger to update updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_hr_quota_updated_at
    BEFORE UPDATE ON hr_quota
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();