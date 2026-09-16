-- Enable extension for time-range overlap checking
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- 1. ENUMS (JOOQ needs these to generate the Java Enums)
CREATE TYPE user_role AS ENUM ('ADMIN', 'CLINICIAN', 'PATIENT');
CREATE TYPE appointment_status AS ENUM ('BOOKED', 'RESCHEDULED', 'CANCELLED', 'COMPLETED');
CREATE TYPE care_plan_status AS ENUM ('ACTIVE', 'PAUSED', 'COMPLETED');
CREATE TYPE task_status AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED');

-- 2. USERS
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ
);

-- 3. PATIENTS
CREATE TABLE patients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    phone_number VARCHAR(20)
);

-- 4. CLINICIANS (Shared Primary Key)
CREATE TABLE clinicians (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) UNIQUE NOT NULL
);

-- 5. CLINICIAN AVAILABILITY
CREATE TABLE clinician_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinician_id UUID NOT NULL REFERENCES clinicians(id) ON DELETE CASCADE,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    CHECK (end_time > start_time)
);

-- 6. APPOINTMENTS
CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES patients(id),
    clinician_id UUID NOT NULL REFERENCES clinicians(id),
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    status appointment_status DEFAULT 'BOOKED',
    reason_for_visit TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CHECK (ends_at > starts_at),
    EXCLUDE USING gist (clinician_id WITH =, tstzrange(starts_at, ends_at) WITH &&)
);

-- 7. CARE PLANS
CREATE TABLE care_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES patients(id),
    clinician_id UUID NOT NULL REFERENCES clinicians(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status care_plan_status DEFAULT 'ACTIVE',
    progress_percentage DECIMAL(5,2) DEFAULT 0.00,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ
);

-- 8. CARE PLAN TASKS
CREATE TABLE