
# GSJ HRIS and Attendance System - Backend REST API

A comprehensive **HRIS and Attendance Management System** backend built with **Spring Boot** and **Java 25**, featuring **JWT authentication**, **Face Recognition**, **Geofencing**, and complete **HR management** capabilities.

## 📋 Quick Overview

This system provides a complete HR management solution with the following key features:

- **Employee Management**: Registration, activation, and profile management
- **Attendance Tracking**: Clock in/out with face recognition and geofencing
- **Leave Management**: Request and approval workflows for various leave types
- **Business Travel**: Travel request management with approval workflows
- **Password Reset**: Secure, token-based password reset with email notifications
- **Role-Based Access**: Multi-level security (ADMIN, HR, SUPERVISOR, EMPLOYEE)
- **Database Migrations**: Flyway-based schema versioning
- **Rate Limiting**: Protection against brute force attacks
- **Real-time Monitoring**: Health checks and debugging endpoints

## 🚀 Getting Started

### Quick Start (Recommended)
```bash
# 1. Clone repository
git clone <repository-url>
cd hris

# 2. Setup development environment (secure setup)
./scripts/setup-dev.sh

# 3. Run application
 set -a && source .env && set +a && mvn clean spring-boot:run -Dspring-boot.run.profiles=dev

# 4. Initialize default data
curl -X POST http://localhost:8081/api/migration/initialize \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json"

# 5. Login with admin credentials
# Email: admin@hris.com, Password: admin123
```

### 🔒 Security First Setup
For secure environment configuration, see **[Environment Setup Guide](docs/ENVIRONMENT_SETUP.md)**

**Important Security Notes:**
- ⚠️ Never commit sensitive data to version control
- 🔐 Use environment-specific configuration files
- 🛡️ Generate strong secrets for production
- 📋 Follow the security checklist in the documentation

## 🔧 Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Backend** | Java, Spring Boot | 25, 3.5.6 |
| **Database** | PostgreSQL | 15+ |
| **Security** | JWT, Spring Security | Latest |
| **Caching** | Redis | 7+ |
| **Email** | SendGrid SMTP | - |
| **Migration** | Flyway | Latest |
| **Testing** | JUnit, Mockito | 5+ |

## 🚀 Features Implemented

### Authentication & Security
- ✅ **JWT-based authentication** with secure token management (WIB timezone support)
- ✅ **Password reset functionality** with secure token-based workflow
- ✅ **Advanced rate limiting** with multi-layer protection:
  - Failed login limiting: 5 attempts per 5 minutes
  - Success login limiting: 5 per account, 20 per IP per 5 minutes
  - Password reset limiting: 3 requests per hour per email
- ✅ **Password encryption** using BCrypt
- ✅ **Password history tracking** (prevents reuse of last 5 passwords)
- ✅ **Role-based access control** (ADMIN, HR, SUPERVISOR, EMPLOYEE)
- ✅ **Credential stuffing protection** through success login rate limiting

### Attendance Management
- ✅ **Clock In/Out** with GPS location tracking
- ✅ **Face Recognition** verification (mock implementation with configurable confidence)
- ✅ **Geofencing** validation for office location
- ✅ **Race condition handling** with pessimistic locking
- ✅ **Real-time attendance status** tracking

### Leave Management
- ✅ **Leave request submission** (Annual, Sick, Maternity, Paternity, etc.)
- ✅ **Leave balance tracking** and automatic deduction
- ✅ **Supervisor approval workflow**
- ✅ **Overlap validation** to prevent conflicting leave periods

### Business Travel Management
- ✅ **Travel request submission** with cost estimation
- ✅ **Transportation and accommodation** management
- ✅ **Supervisor approval workflow**
- ✅ **Current travel tracking**

### HR Admin Features
- ✅ **Employee registration** with face template upload
- ✅ **Employee management** (activate/deactivate/update)
- ✅ **Data migration** via CSV import
- ✅ **Reporting capabilities** for attendance, leave, and travel
- ✅ **Face template management** for biometric authentication

## 🛠 Technology Stack

| Component | Technology |
|-----------|------------|
| **Backend** | Java 25, Spring Boot 3.5.6 |
| **Database** | PostgreSQL |
| **Authentication** | JWT (JSON Web Tokens) |
| **Security** | Spring Security, Rate Limiting |
| **Face Recognition** | OpenCV (Mock Implementation) |
| **Caching** | Redis (for rate limiting) |
| **Containerization** | Docker & Docker Compose |
| **Build Tool** | Maven |

## 📋 API Endpoints Documentation

### Base URL
```
Development: http://localhost:8081
Production: https://your-domain.com
```

### Authentication Headers
All API endpoints (except login and password reset) require JWT authentication:
```http
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

### Standard Response Format
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2025-10-04T22:00:00.000Z"
}
```

### Error Response Format
```json
{
  "success": false,
  "message": "Error description",
  "error": "ERROR_TYPE",
  "timestamp": "2025-10-04T22:00:00.000Z"
}
```

---

## 🔐 Authentication Endpoints

### POST /api/auth/login
Login user and return JWT token.

**Rate Limits:**
- **Failed Login**: 5 attempts per 5 minutes per email/IP
- **Success Login**: 5 successful logins per 5 minutes per account, 20 per IP
- **Lockout**: 5 minutes for failed attempts, separate windows for success limits

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "expiresAt": "2025-01-05T22:00:00.000Z"
  }
}
```

**Rate Limit Exceeded Response (429 Too Many Requests):**
```json
{
  "timestamp": "2025-01-04T22:00:00.000Z",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Too many successful login attempts for this account. Please try again later.",
  "path": "/api/auth/login",
  "retryAfter": 180,
  "email": "user@example.com",
  "rateLimitType": "LOGIN_SUCCESS"
}
```

**Failed Authentication Response (401 Unauthorized):**
```json
{
  "timestamp": "2025-01-04T22:00:00.000Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/auth/login"
}
```

### GET /api/auth/me
Get current authenticated user information.

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "employeeId": "EMP001",
    "role": "EMPLOYEE",
    "isActive": true
  }
}
```

---

## 🔑 Password Reset Endpoints

### POST /api/auth/password-reset/forgot
Request password reset link.

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "If an account with this email exists, a password reset link has been sent.",
  "expiresIn": "1 hour"
}
```

### POST /api/auth/password-reset/reset
Reset password with token.

**Request:**
```json
{
  "token": "uuid-token-here",
  "newPassword": "NewSecurePass123",
  "confirmPassword": "NewSecurePass123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Password has been successfully reset."
}
```

### GET /api/auth/password-reset/verify-token
Verify reset token validity.

**Query Parameters:** `?token=uuid-token-here`

**Response:**
```json
{
  "valid": true,
  "message": "Token is valid"
}
```

---

## ⏰ Attendance Management Endpoints

### POST /api/attendance/clock-in
Clock in with face verification and location tracking.

**Request:**
```json
{
  "latitude": -6.2088,
  "longitude": 106.8456,
  "faceImage": "base64-encoded-face-image"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Clock in successful",
  "data": {
    "id": 123,
    "clockInTime": "2025-10-04T08:00:00",
    "location": {
      "latitude": -6.2088,
      "longitude": 106.8456
    },
    "faceRecognitionConfidence": 0.95
  }
}
```

### POST /api/attendance/clock-out
Clock out from current attendance session.

**Response:**
```json
{
  "success": true,
  "message": "Clock out successful",
  "data": {
    "id": 123,
    "clockInTime": "2025-10-04T08:00:00",
    "clockOutTime": "2025-10-04T17:00:00",
    "totalHours": 9.0
  }
}
```

### GET /api/attendance/today
Get today's attendance record.

**Response:**
```json
{
  "success": true,
  "data": {
    "clockInTime": "2025-10-04T08:00:00",
    "clockOutTime": null,
    "status": "CLOCKED_IN"
  }
}
```

### GET /api/attendance/status
Get current attendance status.

**Response:**
```json
{
  "success": true,
  "data": {
    "status": "CLOCKED_IN",
    "canClockIn": false,
    "canClockOut": true
  }
}
```

### GET /api/attendance/history
Get attendance history with pagination.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Items per page
- `startDate` (optional) - Filter by start date (yyyy-MM-dd)
- `endDate` (optional) - Filter by end date (yyyy-MM-dd)

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 123,
        "clockInTime": "2025-10-04T08:00:00",
        "clockOutTime": "2025-10-04T17:00:00",
        "totalHours": 9.0,
        "location": "Office"
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "currentPage": 1
  }
}
```

---

## 🏖️ Leave Management Endpoints

### POST /api/leave/request
Submit new leave request.

**Request:**
```json
{
  "leaveType": "ANNUAL",
  "startDate": "2025-12-01",
  "endDate": "2025-12-05",
  "reason": "Family vacation"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Leave request submitted successfully",
  "data": {
    "uuid": "b350343f-ab94-4e37-8dd8-c25604942d52",
    "leaveType": "ANNUAL",
    "startDate": "2025-12-01",
    "endDate": "2025-12-05",
    "totalDays": 5,
    "reason": "Family vacation",
    "status": "PENDING",
    "remainingBalance": 10,
    "createdAt": "2025-10-04T22:00:00",
    "createdById": {
      "id": 1,
      "employeeCode": "EMP001",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com"
    }
  }
}
```

### GET /api/leave/my-requests
Get user's leave requests with pagination.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Items per page
- `days` (default: 30) - Filter by last N days

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "uuid": "b350343f-ab94-4e37-8dd8-c25604942d52",
        "leaveType": "ANNUAL",
        "startDate": "2025-12-01",
        "endDate": "2025-12-05",
        "status": "PENDING",
        "remainingBalance": 10
      }
    ],
    "totalElements": 15,
    "totalPages": 2,
    "currentPage": 1
  }
}
```

### GET /api/leave/request/{uuid}
Get specific leave request by UUID.

**Response:**
```json
{
  "data": {
    "uuid": "b350343f-ab94-4e37-8dd8-c25604942d52",
    "leaveType": "ANNUAL",
    "startDate": "2025-12-01",
    "endDate": "2025-12-05",
    "status": "APPROVED",
    "remainingBalance": 10,
    "createdAt": "2025-10-04T22:00:00",
    "createdById": {
      "id": 1,
      "employeeCode": "EMP001",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com"
    },
    "updatedById": {
      "id": 2,
      "employeeCode": "SUP001",
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane@example.com"
    }
  },
  "message": "Leave record retrieved successfully"
}
```

### GET /api/leave/current
Get current active leave.

**Response:**
```json
{
  "success": true,
  "message": "Current leave found",
  "data": {
    "uuid": "b350343f-ab94-4e37-8dd8-c25604942d52",
    "leaveType": "ANNUAL",
    "startDate": "2025-12-01",
    "endDate": "2025-12-05",
    "status": "APPROVED"
  }
}
```

### POST /api/leave/supervisor/approve/{uuid}
Approve leave request (SUPERVISOR role only).

**Response:**
```json
{
  "success": true,
  "message": "Leave request approved successfully",
  "data": {
    "uuid": "b350343f-ab94-4e37-8dd8-c25604942d52",
    "status": "APPROVED",
    "updatedById": {
      "id": 2,
      "employeeCode": "SUP001",
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane@example.com"
    }
  }
}
```

### POST /api/leave/supervisor/reject/{uuid}
Reject leave request (SUPERVISOR role only).

**Response:**
```json
{
  "success": true,
  "message": "Leave request rejected successfully",
  "data": {
    "uuid": "b350343f-ab94-4e37-8dd8-c25604942d52",
    "status": "REJECTED",
    "updatedById": {
      "id": 2,
      "employeeCode": "SUP001",
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane@example.com"
    }
  }
}
```

### GET /api/leave/balance
Get leave balance information.

**Response:**
```json
{
  "success": true,
  "data": {
    "annual": 12,
    "sick": 6,
    "maternity": 90,
    "paternity": 7
  }
}
```

---

## ✈️ Business Travel Management Endpoints

### POST /api/business-travel/request
Submit new business travel request.

**Request:**
```json
{
  "city": "Singapore",
  "startDate": "2025-11-10",
  "endDate": "2025-11-12",
  "reason": "Client meeting and project discussion"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Business travel request submitted successfully",
  "data": {
    "uuid": "46bcf50a-cfc5-4f53-910d-5f8f96670d6f",
    "employeeId": 4146,
    "employeeName": "Citra Ningsih",
    "city": "Singapore",
    "startDate": "2025-11-10",
    "endDate": "2025-11-12",
    "totalDays": 3,
    "reason": "Client meeting and project discussion",
    "status": "PENDING",
    "createdAt": "2025-10-04T20:28:24",
    "createdById": {
      "id": 4146,
      "employeeCode": "EMP67139",
      "firstName": "Citra",
      "lastName": "Ningsih",
      "email": "mcrdik@gmail.com"
    }
  }
}
```

### GET /api/business-travel/my-requests
Get user's business travel requests with pagination.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Items per page

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "uuid": "46bcf50a-cfc5-4f53-910d-5f8f96670d6f",
        "city": "Singapore",
        "startDate": "2025-11-10",
        "endDate": "2025-11-12",
        "status": "PENDING",
        "totalDays": 3
      }
    ],
    "totalElements": 8,
    "totalPages": 1,
    "currentPage": 1
  }
}
```

### GET /api/business-travel/request/{uuid}
Get specific business travel request by UUID.

**Response:**
```json
{
  "data": {
    "uuid": "46bcf50a-cfc5-4f53-910d-5f8f96670d6f",
    "employeeId": 4146,
    "employeeName": "Citra Ningsih",
    "city": "Singapore",
    "startDate": "2025-11-10",
    "endDate": "2025-11-12",
    "totalDays": 3,
    "reason": "Client meeting and project discussion",
    "status": "PENDING",
    "createdAt": "2025-10-04T20:28:24",
    "createdById": {
      "id": 4146,
      "employeeCode": "EMP67139",
      "firstName": "Citra",
      "lastName": "Ningsih",
      "email": "mcrdik@gmail.com"
    }
  },
  "message": "Travel record retrieved successfully"
}
```

### GET /api/business-travel/current
Get current active business travel.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "uuid": "46bcf50a-cfc5-4f53-910d-5f8f96670d6f",
      "city": "Singapore",
      "startDate": "2025-11-10",
      "endDate": "2025-11-12",
      "status": "APPROVED"
    }
  ]
}
```

### POST /api/business-travel/supervisor/approve/{uuid}
Approve business travel request (SUPERVISOR role only).

**Response:**
```json
{
  "success": true,
  "message": "Business travel request approved successfully",
  "data": {
    "uuid": "46bcf50a-cfc5-4f53-910d-5f8f96670d6f",
    "status": "APPROVED",
    "updatedById": {
      "id": 2,
      "employeeCode": "SUP001",
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane@example.com"
    }
  }
}
```

### POST /api/business-travel/supervisor/reject/{uuid}
Reject business travel request (SUPERVISOR role only).

**Response:**
```json
{
  "success": true,
  "message": "Business travel request rejected successfully",
  "data": {
    "uuid": "46bcf50a-cfc5-4f53-910d-5f8f96670d6f",
    "status": "REJECTED",
    "updatedById": {
      "id": 2,
      "employeeCode": "SUP001",
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane@example.com"
    }
  }
}
```

### GET /api/business-travel/supervisor/pending
Get pending business travel requests for supervisor (SUPERVISOR role only).

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "uuid": "46bcf50a-cfc5-4f53-910d-5f8f96670d6f",
      "employeeName": "Citra Ningsih",
      "city": "Singapore",
      "startDate": "2025-11-10",
      "endDate": "2025-11-12",
      "status": "PENDING"
    }
  ]
}
```

---

## 👥 HR Admin Endpoints (ADMIN/HR roles only)

### POST /api/admin/register-employee
Register new employee.

**Request:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@company.com",
  "password": "TempPass123",
  "employeeId": "EMP123",
  "role": "EMPLOYEE",
  "supervisorId": 2
}
```

**Response:**
```json
{
  "success": true,
  "message": "Employee registered successfully",
  "data": {
    "id": 123,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@company.com",
    "employeeId": "EMP123",
    "role": "EMPLOYEE",
    "isActive": true
  }
}
```

### GET /api/admin/employees
Get all employees with pagination and filtering.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Items per page
- `search` (optional) - Search by name or email
- `role` (optional) - Filter by role
- `active` (optional) - Filter by active status

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@company.com",
        "employeeId": "EMP001",
        "role": "EMPLOYEE",
        "isActive": true
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "currentPage": 1
  }
}
```

### PUT /api/admin/employees/{id}
Update employee information.

**Request:**
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.smith@company.com",
  "role": "SUPERVISOR"
}
```

### POST /api/admin/employees/{id}/deactivate
Deactivate employee account.

**Response:**
```json
{
  "success": true,
  "message": "Employee deactivated successfully"
}
```

### POST /api/admin/employees/{id}/face-template
Update employee face template.

**Request:** `multipart/form-data` with face image file.

**Response:**
```json
{
  "success": true,
  "message": "Face template updated successfully"
}
```

---

## 📊 Data Migration Endpoints (ADMIN role only)

### POST /api/migration/initialize
Initialize default system data.

**Response:**
```json
{
  "success": true,
  "message": "Default data initialized successfully",
  "data": {
    "rolesCreated": 4,
    "adminUserCreated": 1
  }
}
```

### POST /api/migration/import-employees
Import employees from CSV file.

**Request:** `multipart/form-data` with CSV file.

**CSV Format:**
```csv
firstName,lastName,email,employeeId,role,supervisorId
John,Doe,john.doe@company.com,EMP001,EMPLOYEE,2
Jane,Smith,jane.smith@company.com,EMP002,SUPERVISOR,
```

### GET /api/migration/csv-template
Download CSV template for employee import.

**Response:** CSV file download

---

## 🏥 System Health & Monitoring Endpoints

### GET /api/test/email
Test email service configuration.

**Query Parameters:** `?to=test@example.com`

**Response:**
```json
{
  "success": true,
  "message": "Test email sent successfully"
}
```

### GET /actuator/health
Application health status (Spring Boot Actuator).

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 250685575168,
        "free": 125342787584,
        "threshold": 10485760
      }
    }
  }
}
```

---

## 🐛 Development & Debugging Endpoints

### GET /api/auth/debug/hash
Debug password hashing (development only).

**Query Parameters:** `?password=test123`

**Response:**
```json
{
  "password": "test123",
  "hashedPassword": "$2a$10$..."
}
```

### POST /api/auth/debug/verify
Debug password verification (development only).

**Request:**
```json
{
  "password": "test123",
  "hashedPassword": "$2a$10$..."
}
```

**Response:**
```json
{
  "valid": true,
  "matches": true
}
```

---

## 📝 Role-Based Access Control

### Role Hierarchy
1. **ADMIN** - Full system access
2. **HR** - Employee management and reporting
3. **SUPERVISOR** - Approve/reject leave and travel requests for team
4. **EMPLOYEE** - Personal data and requests only

### Endpoint Access Matrix

| Endpoint | EMPLOYEE | SUPERVISOR | HR | ADMIN |
|----------|----------|------------|----|-------|
| /api/auth/* | ✅ | ✅ | ✅ | ✅ |
| /api/attendance/* | ✅ | ✅ | ✅ | ✅ |
| /api/leave/request | ✅ | ✅ | ✅ | ✅ |
| /api/leave/my-requests | ✅ | ✅ | ✅ | ✅ |
| /api/leave/supervisor/* | ❌ | ✅ | ✅ | ✅ |
| /api/business-travel/request | ✅ | ✅ | ✅ | ✅ |
| /api/business-travel/my-requests | ✅ | ✅ | ✅ | ✅ |
| /api/business-travel/supervisor/* | ❌ | ✅ | ✅ | ✅ |
| /api/admin/* | ❌ | ❌ | ✅ | ✅ |
| /api/migration/* | ❌ | ❌ | ❌ | ✅ |

### Error Codes
- `UNAUTHORIZED` (401) - Invalid or missing JWT token
- `FORBIDDEN` (403) - Insufficient role permissions
- `NOT_FOUND` (404) - Resource not found
- `BAD_REQUEST` (400) - Validation errors
- `CONFLICT` (409) - Business rule violations
- `TOO_MANY_REQUESTS` (429) - Rate limit exceeded
- `INTERNAL_SERVER_ERROR` (500) - System errors

---

## 🔄 Rate Limiting

The HRIS system implements comprehensive rate limiting to protect against brute force attacks, credential stuffing, and abuse. Rate limiting is enforced at multiple levels using Redis for distributed tracking.

### Authentication Rate Limiting

#### Failed Login Rate Limiting
- **Failed Attempts**: 5 attempts per 5 minutes per email/IP
- **Lockout Duration**: 5 minutes after exceeding limit
- **Reset Condition**: Successful login resets failed attempt counter

#### Success Login Rate Limiting ⭐ **NEW**
- **Per Account**: 5 successful logins per 5 minutes per email
- **Per IP Address**: 20 successful logins per 5 minutes per IP
- **Purpose**: Prevents credential stuffing and automated attacks
- **Independent Tracking**: Account and IP limits are enforced separately

#### Password Reset Rate Limiting
- **Reset Request**: 3 requests per hour per email
- **Reset Confirmation**: 5 attempts per hour per token
- **Token Expiration**: 1 hour validity period

### Rate Limiting Implementation Details

#### Redis Storage Structure
```
Failed Login Attempts:
Key: rate_limit:{email}
Value: JSON object with attempt count, lock status, and lock expiry

Success Login Attempts:
Key: login_success:{email}      # Account-based tracking
Key: login_success_ip:{ip}      # IP-based tracking
Value: Atomic counter with TTL
```

#### Rate Limit Headers
When rate limits are exceeded, the API returns comprehensive HTTP 429 responses:

```json
HTTP/1.1 429 Too Many Requests
Content-Type: application/json
Retry-After: 180
X-RateLimit-Limit: 5
X-RateLimit-Remaining: 0
X-RateLimit-Used: 6
X-RateLimit-Reset: 1698765432
X-RateLimit-Resource: login_success
X-RateLimit-Window: 5 minutes
X-RateLimit-Error-Code: LOGIN_SUCCESS_EXCEEDED
X-Request-ID: req-123456789

{
  "timestamp": "2025-01-04T22:00:00.000Z",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Too many successful login attempts for this account. Please try again later.",
  "path": "/api/auth/login",
  "retryAfter": 180,
  "email": "user@example.com",
  "clientIP": "192.168.1.100",
  "rateLimitType": "LOGIN_SUCCESS"
}
```

### Business Rule Rate Limiting

#### Attendance System
- **Clock In**: Only once per day per employee
- **Clock Out**: Requires active clock-in session
- **Face Recognition**: Configurable confidence threshold (default: 0.7)
- **Geofencing**: 100-meter radius from office coordinates

#### Leave Management
- **Leave Overlap**: Prevents conflicting leave periods
- **Balance Validation**: Ensures sufficient leave balance
- **Approval Workflow**: Supervisor hierarchy validation

#### Business Travel
- **Travel Overlap**: Prevents travel during approved leave
- **Approval Constraints**: Only supervisors can approve subordinate requests

### Rate Limiting Configuration

```properties
# Failed Login Rate Limiting
rate.limit.login.failed.max-attempts=5
rate.limit.login.failed.window-duration=300000 # 5 minutes

# Success Login Rate Limiting
rate.limit.login.success.per-account=5
rate.limit.login.success.per-ip=20
rate.limit.login.success.window-duration=300000 # 5 minutes

# Password Reset Rate Limiting
resilience4j.ratelimiter.instances.password-reset-request.limit-for-period=3
resilience4j.ratelimiter.instances.password-reset-request.limit-refresh-period=1h
resilience4j.ratelimiter.instances.password-reset-confirm.limit-for-period=5
resilience4j.ratelimiter.instances.password-reset-confirm.limit-refresh-period=1h
```

### Security Benefits

#### 🛡️ Attack Prevention
- **Brute Force**: Failed login limiting prevents password guessing
- **Credential Stuffing**: Success login limiting prevents automated credential testing
- **Password Spraying**: IP-based limiting prevents widespread credential attempts
- **Account Enumeration**: Consistent responses prevent email discovery

#### 📊 Monitoring & Auditing
- **Comprehensive Logging**: All rate limit violations are logged with context
- **IP Tracking**: Source IP addresses are monitored for patterns
- **Request IDs**: Unique identifiers for tracing and debugging
- **TTL Management**: Automatic cleanup of expired rate limit data

### Rate Limiting Behavior

#### Error Responses
- **429 Too Many Requests**: Rate limit exceeded
- **401 Unauthorized**: Invalid credentials (subject to failed login limits)
- **400 Bad Request**: Invalid input format
- **500 Internal Server Error**: System errors (not subject to user limits)

#### Rate Limit Reset
- **Successful Authentication**: Resets failed login counter only
- **Manual Reset**: Available through admin endpoints
- **Automatic Expiry**: TTL-based cleanup of all rate limit data
- **Window Reset**: Counters reset after time window expires

### Testing Rate Limiting

#### Development Testing
```bash
# Test failed login rate limiting
for i in {1..6}; do
  curl -X POST http://localhost:8081/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"wrong"}'
done

# Test success login rate limiting (requires valid credentials)
for i in {1..6}; do
  curl -X POST http://localhost:8081/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"correct"}'
done
```

#### Rate Limit Bypass (Development)
Rate limiting can be disabled for testing by setting:
```properties
rate.limit.enabled=false
```

### Production Considerations

#### Redis Configuration
- **High Availability**: Configure Redis clustering for production
- **Persistence**: Enable Redis persistence for rate limit data
- **Memory Management**: Monitor Redis memory usage for large deployments
- **Network Security**: Secure Redis access with authentication and firewalls

#### Monitoring
- **Rate Limit Metrics**: Monitor rate limit violations
- **Performance Impact**: Track Redis performance
- **Security Alerts**: Alert on suspicious rate limit patterns
- **Capacity Planning**: Scale Redis based on user base

## 🏗 Project Structure

```
src/main/java/hris/hris/
├── controller/          # REST API Controllers
├── service/            # Business Logic Services
├── repository/         # Data Access Layer
├── model/              # JPA Entities
├── dto/                # Data Transfer Objects
├── security/           # Security Configuration
├── exception/          # Exception Handlers
└── HrisApplication.java # Main Application Class
```

## 🚀 Quick Start

### Prerequisites
- **Java 25** or higher
- **Maven** 3.6+
- **Docker** and **Docker Compose**
- **PostgreSQL** (if not using Docker)
- **Redis** (if not using Docker)

### Setup Instructions

#### Option 1: Docker Development (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd hris
   ```

2. **Create Docker Compose file** (if not exists)
   ```yaml
   # docker-compose.yml
   version: '3.8'
   services:
     postgres:
       image: postgres:15-alpine
       environment:
         POSTGRES_DB: hris
         POSTGRES_USER: hris_user
         POSTGRES_PASSWORD: hris_password
       ports:
         - "5432:5432"
       volumes:
         - postgres_data:/var/lib/postgresql/data

     redis:
       image: redis:7-alpine
       ports:
         - "6379:6379"
       command: redis-server --appendonly yes
       volumes:
         - redis_data:/data

   volumes:
     postgres_data:
     redis_data:
   ```

3. **Start Services**
   ```bash
   docker-compose up -d
   ```

4. **Build and Run Application**
   ```bash
   docker build -t hris-app .
   docker run -p 8081:8081 --env-file .env hris-app
   ```

#### Option 2: Local Development

1. **Install and start PostgreSQL**
   ```bash
   # Create database
   createdb hris
   ```

2. **Install and start Redis**
   ```bash
   redis-server
   ```

3. **Set Environment Variables**
   ```bash
   export DB_USERNAME=hris_user
   export DB_PASSWORD=hris_password
   export JWT_SECRET=your_jwt_secret_key
   export MAIL_USERNAME=your_email@example.com
   export MAIL_PASSWORD=your_app_password
   ```

4. **Run the Application**
   ```bash
   mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
   ```

### Initialize Default Data
After starting the application, initialize default data:
```bash
curl -X POST http://localhost:8081/api/migration/initialize \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json"
```

### Default Admin User
- **Email**: admin@hris.com
- **Password**: admin123

## 📊 Database Schema & Migrations

### Database Migration with Flyway
The system uses **Flyway** for database version control and migrations. All schema changes are managed through versioned SQL migration files.

#### Migration Files
- **V1__Create_employees_table.sql** - Employee master data table
- **V2__Create_attendances_table.sql** - Attendance tracking with geofencing
- **V3__Create_leave_requests_table.sql** - Leave request management
- **V4__Create_business_travel_requests_table.sql** - Business travel requests
- **V5__Create_roles_table.sql** - User roles and permissions
- **V6__Insert_default_data.sql** - Default users and roles
- **V7__Add_indexes.sql** - Performance optimization indexes
- **V8__Fix_password_hashes_and_indexes.sql** - Password hash fixes and comprehensive indexing
- **V9__Fix_password_hashes_again.sql** - Additional password hash improvements
- **V10__Fix_password_final.sql** - Final password hash corrections
- **V11__Final_password_fix.sql** - Final password hash standardization
- **V12__Fix_face_template_column.sql** - Face template column corrections
- **V13__Add_approval_notes_column.sql** - Add approval notes feature
- **V14__Remove_approval_and_rejection_notes_columns.sql** - Clean up approval columns
- **V15__Remove_approval_date_and_approved_by_id_columns.sql** - Streamline approval workflow
- **V16__Add_created_by_and_updated_by_columns.sql** - Audit trail improvements
- **V17__Remove_approved_by_column.sql** - Final approval schema cleanup
- **V18__Add_UUID_columns.sql** - Add UUID support for entities

### Core Tables
- **employees** - Employee information and credentials
- **attendances** - Attendance records with location and face verification
- **leave_requests** - Leave request records with approval workflow
- **business_travel_requests** - Business travel requests
- **password_reset_tokens** - Password reset token management (Entity: `PasswordResetToken`)
- **password_history** - Password history for reuse prevention (Entity: `PasswordHistory`)
- **roles** - User role definitions
- **user_roles** - Many-to-many user-role mapping

### 🚨 Missing Database Migrations
The following entities are defined but lack corresponding Flyway migrations:

#### Password Reset Tables (Required)
```sql
-- Password Reset Tokens Table
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Password History Table
CREATE TABLE password_history (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- Add indexes for password reset tables
CREATE INDEX idx_password_reset_tokens_email ON password_reset_tokens(email);
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_expiry ON password_reset_tokens(expiry_date);
CREATE INDEX idx_password_history_employee_id ON password_history(employee_id);
```

**Next Migration Version**: `V19__Create_password_reset_tables.sql`

### Flyway Management APIs
- `POST /api/flyway/migrate` - Run pending migrations
- `GET /api/flyway/status` - Get migration status
- `GET /api/flyway/pending` - List pending migrations
- `GET /api/flyway/applied` - List applied migrations
- `POST /api/flyway/validate` - Validate migrations
- `POST /api/flyway/repair` - Repair migration state
- `POST /api/flyway/clean` - Drop all objects (⚠️ Destructive)

### Key Features
- **Pessimistic Locking** on critical operations to prevent race conditions
- **Automatic Timestamps** for created_at and updated_at
- **Soft Deletes** through isActive flag
- **Blob Storage** for face templates
- **Comprehensive Indexing** for optimal query performance
- **Data Integrity** with proper foreign key constraints and check constraints

## 🔧 Configuration

### Application Properties
The application is configured via `application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/hris?timezone=Asia/Jakarta
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.table=flyway_schema_history
spring.flyway.locations=classpath:db/migration
spring.flyway.validate-on-migrate=true
spring.flyway.out-of-order=false
spring.flyway.ignore-missing-migrations=false

# JWT Configuration
jwt.secret=hris_jwt_secret_key_2024_very_long_secure_key_for_jwt_tokens
jwt.expiration=86400000 # 24 hours

# Password Reset Configuration
app.password-reset.token-expiration=3600000 # 1 hour
app.password-reset.max-attempts=3
app.password-reset.rate-limit-duration=3600000 # 1 hour
app.password-check.history-limit=5
app.frontend.url=http://localhost:3000

# Mail Configuration (SendGrid)
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.sendgrid.net
spring.mail.properties.mail.smtp.connectiontimeout=10000
spring.mail.properties.mail.smtp.timeout=10000

# Rate Limiting Configuration
# Failed Login Rate Limiting
rate.limit.login.failed.max-attempts=5
rate.limit.login.failed.window-duration=300000 # 5 minutes

# Success Login Rate Limiting (NEW)
rate.limit.login.success.per-account=5
rate.limit.login.success.per-ip=20
rate.limit.login.success.window-duration=300000 # 5 minutes

# Redis Configuration (for rate limiting and caching)
spring.redis.host=localhost
spring.redis.port=6379

# Logging Configuration
logging.level.hris=DEBUG
logging.level.org.springframework.security=DEBUG

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Server Configuration
server.port=8081

# Timezone Configuration
spring.jpa.properties.hibernate.jdbc.time_zone=Asia/Jakarta

# Application Information
app.name=GSJ HRIS
app.version=1.0.0

# Resilience4j Configuration (Password Reset Rate Limiting)
resilience4j.ratelimiter.instances.password-reset-request.limit-for-period=3
resilience4j.ratelimiter.instances.password-reset-request.limit-refresh-period=1h
resilience4j.ratelimiter.instances.password-reset-request.timeout-duration=5s

resilience4j.ratelimiter.instances.password-reset-confirm.limit-for-period=5
resilience4j.ratelimiter.instances.password-reset-confirm.limit-refresh-period=1h
resilience4j.ratelimiter.instances.password-reset-confirm.timeout-duration=5s

# Resilience4j Retry Configuration
resilience4j.retry.instances.password-reset-request.max-attempts=3
resilience4j.retry.instances.password-reset-request.wait-duration=1s

resilience4j.retry.instances.password-reset-confirm.max-attempts=3
resilience4j.retry.instances.password-reset-confirm.wait-duration=1s

# Face Recognition
face.recognition.threshold=0.7

# Geofencing
office.latitude=-6.2088
office.longitude=106.8456
geofence.radius=100.0
```

## 🔒 Security Features

### Authentication
- **JWT tokens** with configurable expiration (WIB timezone support)
- **Password reset workflow** with secure token management
- **Password encryption** using BCrypt
- **Password history tracking** (prevents reuse of last 5 passwords)
- **Rate limiting** to prevent brute force attacks
- **Email notifications** for password reset requests

### Authorization
- **Role-based access control** with method-level security
- **Supervisor hierarchy validation** for approvals
- **Employee data isolation** based on roles

### Data Protection
- **Input validation** on all API endpoints
- **SQL injection prevention** through JPA/Hibernate
- **XSS protection** through proper response handling

## 🧪 Testing

### Running Tests
```bash
./mvnw test
```

### Key Test Areas
- **Authentication endpoints** with rate limiting
- **Attendance operations** with face recognition
- **Leave and travel workflows** with approval logic
- **HR admin functions** with proper authorization

## 📈 Performance

### Response Times
- **Clock In/Out**: < 1 second (requirement met)
- **Authentication**: < 500ms
- **Leave Requests**: < 2 seconds
- **Reporting**: < 5 seconds

### Optimization Features
- **Database connection pooling**
- **Redis caching** for rate limiting
- **Pessimistic locking** only when necessary
- **Efficient queries** with proper indexing


## 🚨 Error Handling

The API provides comprehensive error handling:
- **Validation errors** with detailed field information
- **Authentication errors** with clear messages
- **Business logic errors** with actionable responses
- **System errors** with appropriate logging

## 📝 Logging

- **Security events** (login attempts, failures)
- **Business operations** (clock in/out, approvals)
- **Performance metrics** (response times)
- **Error tracking** with stack traces

## 🛡 Security Considerations

### Implemented Security Measures
- ✅ **Rate limiting** prevents brute force attacks
- ✅ **Input validation** prevents injection attacks
- ✅ **Role-based access** ensures proper authorization
- ✅ **Password encryption** protects credentials
- ✅ **JWT validation** prevents token tampering
- ✅ **Password reset security** with token expiration and rate limiting
- ✅ **Email enumeration protection** in password reset flow
- ✅ **Password history enforcement** prevents password reuse
- ✅ **Secure email delivery** with HTML templates

### Best Practices Followed
- ✅ **Principle of least privilege** for role assignments
- ✅ **Secure defaults** for all configurations
- ✅ **Comprehensive logging** for audit trails
- ✅ **Error handling** prevents information leakage
- ✅ **Password reset flow** prevents email enumeration attacks
- ✅ **Token-based password reset** with proper expiration handling
- ✅ **Password strength validation** with comprehensive requirements

## 🔄 Future Enhancements

### Planned Features
- **Advanced email notifications** for approvals and system events
- **Advanced reporting** with charts and analytics
- **Mobile app integration** for biometric capture
- **Integration with HR systems** (payroll, etc.)
- **Advanced scheduling** and shift management
- **Multi-factor authentication** for enhanced security
- **Self-service password management** portal

### Scalability Considerations
- **Horizontal scaling** support
- **Database optimization** for large datasets
- **Caching strategies** for improved performance
- **Load balancing** ready architecture

## 🔐 Password Reset System

### Overview
The GSJ HRIS implements a secure, token-based password reset system with comprehensive security features including rate limiting, email enumeration protection, and password history tracking.

### Password Reset Flow Diagram

```
┌─────────────────┐    1. Forgot Password    ┌──────────────────┐    2. Generate Token     ┌─────────────────┐
│   User/Frontend │ ──────────────────────→ │   Password Reset │ ─────────────────────→ │   Database      │
│   Interface     │                         │   Controller     │                         │   (Tokens)      │
└─────────────────┘                         └──────────────────┘                         └─────────────────┘
        │                                           │                                           │
        │ 3. Email with Reset Link                  │ 4. Store Token                          │
        │                                           │                                           │
        ↓                                           ↓                                           ↓
┌─────────────────┐    5. Click Link          ┌──────────────────┐    6. Validate Token     ┌─────────────────┐
│   Email Service │ ◄──────────────────────  │   Password Reset │ ◄────────────────────── │   Database      │
│   (SendGrid)    │                         │   Controller     │                         │   (Validation)  │
└─────────────────┘                         └──────────────────┘                         └─────────────────┘
                                                                                                           │
                                                                                                           │ 7. Check History & Update
                                                                                                           │
                                                                                                           ↓
                                                                                                  ┌─────────────────┐
                                                                                                  │   Database      │
                                                                                                  │   (Employees)   │
                                                                                                  └─────────────────┘
```

### Detailed Workflow

#### Step 1: Request Password Reset
**Endpoint:** `POST /api/auth/password-reset/forgot`

1. **Input Validation:**
   - Validate email format
   - Check rate limiting (3 requests per hour per email)

2. **Security Measures:**
   - Email enumeration protection (always returns success)
   - Rate limiting enforcement
   - Audit logging for all attempts

3. **Token Generation:**
   - Generate UUID token
   - Set expiration (1 hour)
   - Invalidate existing tokens for the email

4. **Email Delivery:**
   - Send professional HTML email with reset link
   - Fallback: Display token in logs for development
   - Security notifications in email template

#### Step 2: Token Validation
**Endpoint:** `GET /api/auth/password-reset/verify-token`

1. **Token Checks:**
   - Token exists and not used
   - Token not expired
   - Token format validation

2. **Security:**
   - Prevent timing attacks
   - Consistent error messages

#### Step 3: Password Reset
**Endpoint:** `POST /api/auth/password-reset/reset`

1. **Input Validation:**
   - Token validation
   - Password confirmation match
   - Password strength requirements

2. **Password Requirements:**
   - Minimum 6 characters
   - Password history check (last 5 passwords)
   - No current password reuse

3. **Security Actions:**
   - Save current password to history
   - Update employee password with BCrypt encryption
   - Mark token as used
   - Invalidate all other tokens for the email
   - Send confirmation email

4. **Audit:**
   - Log successful password reset
   - Track timestamp and IP address

### API Usage Examples

#### Request Password Reset
```bash
curl -X POST http://localhost:8081/api/auth/password-reset/forgot \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'
```

**Response:**
```json
{
  "message": "If an account with this email exists, a password reset link has been sent.",
  "expiresIn": "1 hour",
  "success": true
}
```

#### Reset Password with Token
```bash
curl -X POST http://localhost:8081/api/auth/password-reset/reset \
  -H "Content-Type: application/json" \
  -d '{
    "token": "a4988afb-48fd-4471-b92b-499c2ea05ac6",
    "newPassword": "NewSecurePass123",
    "confirmPassword": "NewSecurePass123"
  }'
```

**Response:**
```json
{
  "message": "Password has been successfully reset.",
  "success": true
}
```

#### Verify Reset Token
```bash
curl -X GET "http://localhost:8081/api/auth/password-reset/verify-token?token=a4988afb-48fd-4471-b92b-499c2ea05ac6"
```

**Response:**
```json
{
  "valid": true,
  "message": "Token is valid"
}
```

### Email Templates

#### Password Reset Email Features:
- **Professional HTML Design**: Company branded with modern styling
- **Security Notices**: Clear instructions and warnings
- **Multiple Action Options**: Button and direct link
- **Expiration Information**: 1-hour validity notice
- **Password Requirements**: Security best practices guidance
- **Responsive Design**: Mobile-friendly layout

#### Email Content Sections:
1. **Header**: GSJ HRIS branding
2. **Security Notice**: Warning about link expiration and privacy
3. **Action Button**: Primary reset password action
4. **Fallback Link**: Copy-paste option for compatibility
5. **Password Guidelines**: Security requirements explanation
6. **Footer**: Company information and automated message notice

### Security Features

#### 🔒 Authentication & Authorization
- **Rate Limiting**:
  - 3 reset requests per hour per email
  - 5 reset confirmations per hour
- **Token Security**: UUID-based, single-use tokens
- **Encryption**: BCrypt for password storage
- **JWT Integration**: Seamless with existing auth system

#### 🛡️ Protection Against Attacks
- **Email Enumeration**: Generic responses prevent email discovery
- **Brute Force**: Rate limiting and attempt tracking
- **Timing Attacks**: Consistent response times
- **Token Theft**: Short expiration and single-use tokens
- **Password Reuse**: History tracking prevents reuse

#### 📊 Audit & Monitoring
- **Comprehensive Logging**: All reset attempts logged
- **IP Tracking**: Monitor source of requests
- **Failure Analysis**: Track patterns and potential attacks
- **Success Notifications**: Email confirmations for successful resets

### Database Schema

#### Password Reset Tokens Table
```sql
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Password History Table
```sql
CREATE TABLE password_history (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);
```


### Testing & Development

#### Development Features
- **Token Display**: Tokens shown in logs for easy testing
- **Test Endpoints**: Dedicated email testing endpoints
- **Bypass Email**: Direct token usage for development
- **Debug Logging**: Detailed logging for troubleshooting

#### Testing Scenarios
1. **Valid Flow**: Complete password reset workflow
2. **Invalid Token**: Expired or used token handling
3. **Rate Limiting**: Exceeding request limits
4. **Password Validation**: Weak password rejection
5. **Email Enumeration**: Non-existent email handling
6. **Password History**: Reuse prevention testing

### Production Deployment

#### Security Considerations
- **SSL/TLS**: Enforce HTTPS for all endpoints
- **Environment Variables**: Secure credential management
- **Monitoring**: Track reset patterns and anomalies
- **Backup**: Regular database backups with encrypted data
- **Compliance**: GDPR and data protection considerations

#### Best Practices
- **Regular Cleanup**: Automated expired token cleanup
- **Monitoring**: Alert on suspicious reset patterns
- **User Education**: Clear password security guidelines
- **Incident Response**: Procedures for security events
- **Testing**: Regular security testing and validation

### Troubleshooting

#### Common Issues
1. **Email Not Delivered**: Check SendGrid configuration
2. **Token Invalid**: Verify token expiration and usage
3. **Rate Limited**: Wait for rate limit window to reset
4. **Password Rejected**: Check password requirements and history
5. **Database Errors**: Verify database connectivity and schema

#### Debug Steps
1. Check application logs for detailed error messages
2. Verify email service configuration and credentials
3. Test database connectivity and token storage
4. Validate environment variables and configuration
5. Monitor rate limiting and security logs

### Integration with Frontend

#### Expected Frontend Flow
1. User enters email in forgot password form
2. Frontend shows success message (regardless of email existence)
3. User receives email with reset link
4. User clicks link and is redirected to reset form
5. Frontend validates token before showing form
6. User enters new password and confirmation
7. Frontend submits reset request
8. Show success message and redirect to login

#### Error Handling
- Display user-friendly error messages
- Handle network errors gracefully
- Provide clear next steps for each error type
- Maintain security through consistent error responses

---

## 📞 Support

For support and questions regarding the HRIS system:
- **Documentation**: Refer to this README and API documentation
- **Issues**: Create tickets in the project repository
- **Security**: Report security concerns through proper channels

---

**Built with ❤️ using Spring Boot and modern Java technologies**