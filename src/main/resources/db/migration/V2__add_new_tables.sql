DROP TABLE IF EXISTS care_plan_tasks CASCADE;
DROP TABLE IF EXISTS care_plans CASCADE;
DROP TABLE IF EXISTS appointments CASCADE;
DROP TABLE IF EXISTS clinician_availability CASCADE;
DROP TABLE IF EXISTS clinicians CASCADE;
DROP TABLE IF EXISTS patients CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 2. Drop existing custom types so they can recreate safely
DROP TYPE IF EXISTS task_status CASCADE;
DROP TYPE IF EXISTS care_plan_status CASCADE;
DROP TYPE IF EXISTS appointment_status CASCADE;
DROP TYPE IF EXISTS user_role CASCADE;

-- Enable extension for time-range overlap checking
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- 1. ENUMS (Enforces state machines at the DB level)
CREATE TYPE user_role AS ENUM ('ADMIN', 'CLINICIAN', 'PATIENT');
CREATE TYPE appointment_status AS ENUM ('BOOKED', 'RESCHEDULED', 'CANCELLED', 'COMPLETED');
CREATE TYPE care_plan_status AS ENUM ('ACTIVE', 'PAUSED', 'COMPLETED');
CREATE TYPE task_status AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED');

-- 2. USERS (Base authentication table)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ -- Soft delete for auditability
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

-- 4. CLINICIANS
CREATE TABLE clinicians (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) UNIQUE NOT NULL
);

-- 5. CLINICIAN AVAILABILITY (Working hours)
CREATE TABLE clinician_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinician_id UUID NOT NULL REFERENCES clinicians(id) ON DELETE CASCADE,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7), -- 1=Mon, 7=Sun
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    CHECK (end_time > start_time)
);

-- 6. APPOINTMENTS (The core scheduling table)
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

    -- SENIOR MOVE: This constraint physically prevents double-booking at the DB level.
    -- It ensures no two appointments for the SAME clinician overlap in time.
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
CREATE TABLE care_plan_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    care_plan_id UUID NOT NULL REFERENCES care_plans(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATE NOT NULL,
    weight INTEGER DEFAULT 1, -- Used for calculating weighted progress
    status task_status DEFAULT 'PENDING',
    completed_at TIMESTAMPTZ
);

-- Indexes for performance
CREATE INDEX idx_appointments_clinician_time ON appointments(clinician_id, starts_at);
CREATE INDEX idx_appointments_patient ON appointments(patient_id);
CREATE INDEX idx_care_plans_patient ON care_plans(patient_id);