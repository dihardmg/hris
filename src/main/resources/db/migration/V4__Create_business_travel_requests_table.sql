-- Create business_travel_requests table
CREATE TABLE IF NOT EXISTS business_travel_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    travel_purpose VARCHAR(500) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    estimated_cost DECIMAL(12, 2),
    actual_cost DECIMAL(12, 2),
    transportation_type VARCHAR(20) NOT NULL,
    accommodation_required BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT,
    approval_date TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_business_travel_requests_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_business_travel_requests_approved_by
        FOREIGN KEY (approved_by) REFERENCES employees(id) ON DELETE SET NULL
);

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_business_travel_requests_updated_at
    BEFORE UPDATE ON business_travel_requests
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add check constraints
ALTER TABLE business_travel_requests
ADD CONSTRAINT chk_transportation_type
CHECK (transportation_type IN ('FLIGHT', 'TRAIN', 'BUS', 'CAR', 'OTHER'));

ALTER TABLE business_travel_requests
ADD CONSTRAINT chk_business_travel_status
CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'COMPLETED'));

ALTER TABLE business_travel_requests
ADD CONSTRAINT chk_travel_dates
CHECK (end_date >= start_date);

ALTER TABLE business_travel_requests
ADD CONSTRAINT chk_travel_costs
CHECK (estimated_cost IS NULL OR estimated_cost >= 0);

ALTER TABLE business_travel_requests
ADD CONSTRAINT chk_actual_cost
CHECK (actual_cost IS NULL OR actual_cost >= 0);

ALTER TABLE business_travel_requests
ADD CONSTRAINT chk_travel_approval_consistency
CHECK (
    (status = 'APPROVED' AND approved_by IS NOT NULL AND approval_date IS NOT NULL) OR
    (status = 'REJECTED' AND approved_by IS NOT NULL AND approval_date IS NOT NULL) OR
    (status IN ('PENDING', 'CANCELLED') AND approved_by IS NULL AND approval_date IS NULL) OR
    (status = 'COMPLETED')
);