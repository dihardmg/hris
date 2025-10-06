# Leave Management API Documentation

## Overview
Sistem Leave Management yang telah diperbarui dengan fitur:
- Dynamic Leave Types (database-driven)
- Balance Quota Management
- Leave Type sesuai regulasi Indonesia
- API endpoints untuk monitoring balance

## API Endpoints

### Leave Types Management

#### Get All Active Leave Types
```
GET /api/v1/leave-types
```
Response:
```json
[
  {
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "code": "ANNUAL_LEAVE",
    "name": "Cuti Tahunan",
    "description": "Cuti tahunan berbayar minimal 12 hari kerja",
    "minDurationDays": 12,
    "hasBalanceQuota": true,
    "isPaidLeave": true,
    "requiresDocument": false,
    "isActive": true
  }
]
```

#### Create Leave Type
```
POST /api/v1/leave-types
Content-Type: application/json

{
  "code": "CUSTOM_LEAVE",
  "name": "Cuti Kustom",
  "description": "Deskripsi cuti",
  "minDurationDays": 1,
  "hasBalanceQuota": false,
  "isPaidLeave": false,
  "requiresDocument": false
}
```

### Leave Balance Management

#### Get My Leave Balances
```
GET /api/v1/leave-balance/my-balances
Authorization: Bearer <token>
```
Response:
```json
[
  {
    "employeeId": 1,
    "employeeName": "John Doe",
    "employeeEmail": "john@company.com",
    "leaveTypeUuid": "550e8400-e29b-41d4-a716-446655440000",
    "leaveTypeCode": "ANNUAL_LEAVE",
    "leaveTypeName": "Cuti Tahunan",
    "totalQuota": 12,
    "usedQuota": 5,
    "remainingQuota": 7,
    "hasBalanceQuota": true,
    "percentageUsed": 41.67
  }
]
```

#### Get Specific Leave Balance
```
GET /api/v1/leave-balance/my-balances/leave-type/{leaveTypeUuid}
Authorization: Bearer <token>
```

#### Get Subordinate Leave Balances (Supervisor)
```
GET /api/v1/leave-balance/subordinates
Authorization: Bearer <token>
```

#### Get All Leave Balances (Manager/HR)
```
GET /api/v1/leave-balance/all
Authorization: Bearer <token>
```

### Leave Request (Updated)

#### Create Leave Request
```
POST /api/leave/request
Authorization: Bearer <token>
Content-Type: application/json

{
  "leaveTypeUuid": "550e8400-e29b-41d4-a716-446655440000",
  "startDate": "2024-12-01",
  "endDate": "2024-12-03",
  "reason": "Family gathering"
}
```

#### Backward Compatibility (Enum-based)
```
POST /api/leave/request
Authorization: Bearer <token>
Content-Type: application/json

{
  "leaveTypeEnum": "ANNUAL_LEAVE",
  "startDate": "2024-12-01",
  "endDate": "2024-12-03",
  "reason": "Family gathering"
}
```

## Jenis Cuti Tersedia

### Cuti dengan Balance Quota
1. **ANNUAL_LEAVE** - Cuti Tahunan (12 hari minimal)
2. **SICK_LEAVE** - Cuti Sakit (dengan dokumen)

### Cuti Tanpa Balance Quota (Pencatatan Transaksi Saja)
1. **MATERNITY_LEAVE** - Cuti Melahirkan (90 hari)
2. **MISCARRIAGE_LEAVE** - Cuti Keguguran (45 hari)
3. **IMPORTANT_REASON_LEAVE** - Cuti Alasan Penting (1-3 hari)
4. **MENSTRUAL_LEAVE** - Cuti Haid (1-2 hari)
5. **SABBATICAL_LEAVE** - Cuti Besar (60 hari minimal)
6. **UNPAID_LEAVE** - Cuti Tidak Berbayar
7. **PATERNITY_LEAVE** - Cuti Ayah (2 hari)
8. **COMPASSIONATE_LEAVE** - Cuti Belasungkawa (3 hari)

## Business Logic

### Balance Quota Validation
- Hanya jenis cuti dengan `hasBalanceQuota: true` yang akan mengurangi balance
- Cuti Tahunan dan Cuti Sakit mengurangi balance quota
- Jenis cuti lainnya hanya untuk pencatatan transaksi

### Duration Validation
- `minDurationDays`: Validasi durasi minimal
- `maxDurationDays`: Validasi durasi maksimal
- Automatic calculation of total days

### Document Requirements
- `requiresDocument: true` untuk cuti yang memerlukan dokumen pendukung

## Database Schema

### Leave Types Table
```sql
CREATE TABLE leave_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid BINARY(16) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    min_duration_days INT,
    has_balance_quota BOOLEAN NOT NULL DEFAULT FALSE,
    is_paid_leave BOOLEAN NOT NULL DEFAULT FALSE,
    max_duration_days INT,
    requires_document BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
);
```

### Updated Leave Requests Table
```sql
ALTER TABLE leave_requests ADD COLUMN leave_type_id BIGINT;
ALTER TABLE leave_requests ADD COLUMN leave_type_enum VARCHAR(50);
ALTER TABLE leave_requests ADD CONSTRAINT fk_leave_requests_leave_type
FOREIGN KEY (leave_type_id) REFERENCES leave_types(id);
```

## Migration Notes

### Backward Compatibility
- Existing enum-based requests still supported
- Gradual migration to UUID-based leave types
- Dual storage of leave type information

### Data Migration
1. Create leave types table with Indonesian regulation-based types
2. Update existing leave requests with new relationships
3. Maintain enum data for compatibility

## Security

### Role-based Access
- Employee: Access own balances and requests
- Supervisor: Access subordinate balances
- Manager/HR: Access all balances

### Authentication
- JWT token-based authentication
- Role-based authorization

## Error Handling

### Common Error Codes
- `403`: Forbidden access (insufficient permissions)
- `404`: Leave type not found
- `400`: Invalid date range or insufficient balance

### Error Response Format
```json
{
  "status": "error",
  "message": "Insufficient leave balance for Cuti Tahunan. Available: 5 days, Requested: 7 days",
  "timestamp": "2024-01-01T12:00:00Z"
}
```