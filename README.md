# CareSlot Backend

Enterprise healthcare platform built with **Spring Boot 3**, **jOOQ**, and **JWT Authentication**. Streamlines the healthcare lifecycle with role-based access control, clinician availability management, real-time double-booking prevention, and comprehensive care-plan task tracking.

---

##  Tech Stack

- **Java 21**
- **Spring Boot 3.4.2**
- **jOOQ 3.19** (Type-safe SQL & Complex Joins)
- **PostgreSQL 18**
- **Flyway** (Version-controlled DB Migrations)
- **Spring Security + JWT** (Stateless Authentication)
- **Lombok**
- **Jakarta Validation**
- **Maven Wrapper (mvnw)**

---

##  Quick Start

### Prerequisites

- Java 21 or higher
- PostgreSQL running on `localhost:5432`
- Terminal or PowerShell

### Setup & Run

#### 1. Clone the Repository

```bash
git clone https://github.com/Vishnu-dk/CareSlot_Backend.git
cd CareSlot_Backend
```

#### 2. Configure Database

Open `src/main/resources/application.properties` and update your PostgreSQL credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/careslot_db
spring.datasource.username=your_postgres_username
spring.datasource.password=your_postgres_password
spring.flyway.password=your_postgres_password
```

#### 3. Run the Application

**macOS/Linux**

```bash
./mvnw spring-boot:run
```

**Windows (PowerShell/CMD)**

```bash
.\mvnw spring-boot:run
```

Maven will automatically:

- Download dependencies
- Run Flyway migrations to create the database schema
- Seed demo users (Admin, Clinician, Patient) on first startup

#### 4. Verify Startup

Application URL:

```text
http://localhost:8080
```

##  API Reference

### Base URL

```text
http://localhost:8080/api
```

### Authentication

Add the following header to all protected endpoints:

```http
Authorization: Bearer <your_jwt_token>
```

### Get JWT Token

```http
POST /auth/login
```

Request Body:

```json
{
  "email": "your_email",
  "password": "your_password"
}
```

---

##  Appointments API (`/appointments`)

| Method | Endpoint | Role | Description |
|----------|----------|----------|----------|
| POST | /appointments | Patient | Book a new appointment (validates slot availability & prevents double-booking) |
| PATCH | /appointments/{id}/cancel | Patient/Admin | Cancel an appointment (24-hour advance policy enforced) |
| PATCH | /appointments/{id}/complete | Clinician | Mark appointment as completed (requires active care plan) |
| GET | /appointments/my-history | Patient | View patient's appointment history |
| GET | /appointments/my-schedule?date= | Clinician | View clinician's daily schedule |
| GET | /appointments/all | Admin | View all appointments |

### Appointment Status Flow

```text
BOOKED → CANCELLED / COMPLETED
```

---

##  Scheduling & Availability API (`/scheduling`)

| Method | Endpoint | Role | Description |
|----------|----------|----------|----------|
| POST | /scheduling/availability | Clinician/Admin | Set weekly working hours |
| PUT | /scheduling/availability | Clinician/Admin | Update existing availability |
| DELETE | /scheduling/availability/{dayOfWeek} | Clinician/Admin | Remove availability for a specific day |
| GET | /scheduling/clinicians/{userId}/slots?date= | All Authenticated | View available 30-minute slots for a clinician on a specific date |
| GET | /scheduling/clinicians/{userId}/weekly-availability | All Authenticated | View clinician's weekly schedule |

---

##  Care Plans API (`/care-plan`)

| Method | Endpoint | Role | Description |
|----------|----------|----------|----------|
| POST | /care-plan | Clinician | Create a new care plan with tasks (must be within appointment window) |
| GET | /care-plan/my-plans | Patient | View assigned care plans |
| PATCH | /care-plan/tasks/{taskId}/status?status= | Patient | Mark a task as completed (auto-calculates progress) |
| GET | /care-plan/my-issued-plans | Clinician | View all care plans issued by this clinician |

### Care Plan Status Flow

```text
ACTIVE → COMPLETED
```

(Automatically transitions when progress reaches 100%)

---

##  Patient Management API (`/patients`)

| Method | Endpoint | Role | Description |
|----------|----------|----------|----------|
| POST | /patients/profile | Patient/Admin | Create patient profile |
| GET | /patients | Admin/Clinician | View all patients |
| GET | /patients/my-profile | Patient | View own profile |

---

##  Clinician Management API (`/clinicians`)

| Method | Endpoint | Role | Description |
|----------|----------|----------|----------|
| POST | /clinicians/profile | Clinician/Admin | Create clinician profile (validates unique license number) |
| GET | /clinicians | All Authenticated | View all clinicians |
| GET | /clinicians/weekly-availability/me | Clinician | View own weekly availability |
| GET | /clinicians/my-patients | Clinician | View assigned patients with appointment status |
| GET | /clinicians/my-profile | Clinician | View own profile |

---

##  Authentication API (`/auth`)

| Method | Endpoint | Role | Description |
|----------|----------|----------|----------|
| POST | /auth/register | Public | Register new user (requires email, password, role) |
| POST | /auth/login | Public | Login and receive JWT token |

---

##  Admin API (`/admin`)

| Method | Endpoint | Role | Description |
|----------|----------|----------|----------|
| GET | /admin/users | Admin | View all active users |
| PATCH | /admin/users/{userId}/deactivate | Admin | Soft-delete user account |
| PATCH | /admin/users/{userId}/activate | Admin | Reactivate deactivated user |

---

##  Architecture & Key Features

### Layered Architecture

```text
Controller → Service → Repository (jOOQ)
```

Ensures:

- Strict separation of concerns
- High maintainability and testability
- Type-safe database interactions

### Key Features

 Role-Based Access Control (Admin, Clinician, Patient) via Spring Security  
 JWT Stateless Authentication with custom claims (userId, role)  
 Real-time Double-Booking Prevention (Validates clinician schedule overlaps via exclusion constraints)  
 Patient Overlap Validation (Prevents patients from booking conflicting appointments)  
 Strict Date/Time Validation (Prevents past-date bookings)  
 24-Hour Cancellation Policy (Enforced business rule)  
 Care-Plan Lifecycle & Task Progress Tracking (Weighted progress calculation)  
 Appointment Window Validation (Care plans can only be created within ±5 minutes of appointment)  
 Flyway Database Versioning  
 Type-Safe SQL with jOOQ  
 Centralized Exception Handling & Standardized Error Responses  
 Soft-Delete User Management  
 CORS Configuration for Frontend Integration

---

##  Business Rules

### Appointment Scheduling Rules

- Patients cannot book appointments in the past
- Appointments must fall strictly within the clinician's configured working hours
- Double-booking is strictly prevented. The system checks for time overlaps before confirming any `BOOKED` appointment
- Patients cannot have multiple active appointments with the same clinician
- Patients cannot book if they have an incomplete active care plan with the clinician

### Cancellation Rules

- Appointments can only be cancelled at least **24 hours in advance**
- Completed appointments cannot be cancelled
- Only the patient or admin can cancel appointments

### Care Plan Rules

- Care plans can only be created within a **5-minute window** before or after the appointment time
- A care plan must contain at least one task upon creation
- Progress percentage is automatically calculated based on weighted task completion
- Tasks cannot be marked as completed after their due date has passed
- When progress reaches 100%, care plan status automatically transitions to `COMPLETED`

### Clinician Availability Rules

- Clinicians can set availability for each day of the week (1–7, Monday–Sunday)
- Available slots are calculated in **30-minute increments**
- Existing appointments are excluded from available slot calculations

---

##  Testing & Frontend Integration

### Authentication Flow

```text
Login
 ↓
Receive JWT Token (contains userId, email, role)
 ↓
Attach Bearer Token to Headers
 ↓
Access Protected APIs
```

### Validation Errors

Example response for invalid input:

```json
{
  "timestamp": "2024-05-20T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "details": {
    "startTime": "Appointment start time must be in the future",
    "clinicianId": "Clinician is not available at this time"
  }
}
```

### Logs

Run:

```bash
.\mvnw spring-boot:run
```

View console output for:

- jOOQ generated SQL execution logs
- JWT validation and Spring Security filter chain logs
- Flyway migration success messages

---

## License

MIT License © Vishnu Divakar
