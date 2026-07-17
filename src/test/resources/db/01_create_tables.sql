-- ════════════════════════════════════════════════════════════════════
-- Student Course Registration System — DDL
-- Target: PostgreSQL 18  |  Database: itc475
-- Run as: psql -U postgres -d itc475 -f 01_create_tables.sql
-- ════════════════════════════════════════════════════════════════════

-- Drop in reverse-dependency order (safe to re-run)
DROP TABLE IF EXISTS enrollments CASCADE;
DROP TABLE IF EXISTS courses     CASCADE;
DROP TABLE IF EXISTS students    CASCADE;

-- ── STUDENTS ────────────────────────────────────────────────────────
-- One student can enroll in many courses (1 : N via enrollments).
CREATE TABLE students (
    student_id  BIGSERIAL    PRIMARY KEY,
    first_name  VARCHAR(80)  NOT NULL,
    last_name   VARCHAR(80)  NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    major       VARCHAR(120) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  students            IS 'Registered students in the system.';
COMMENT ON COLUMN students.student_id IS 'Surrogate primary key (auto-increment).';
COMMENT ON COLUMN students.email      IS 'Unique institutional e-mail.';

-- ── COURSES ─────────────────────────────────────────────────────────
-- One course can have many enrolled students (1 : N via enrollments).
CREATE TABLE courses (
    course_id   BIGSERIAL    PRIMARY KEY,
    course_code VARCHAR(20)  NOT NULL UNIQUE,
    course_name VARCHAR(200) NOT NULL,
    credits     SMALLINT     NOT NULL CHECK (credits BETWEEN 1 AND 6),
    instructor  VARCHAR(120) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  courses             IS 'Offered academic courses.';
COMMENT ON COLUMN courses.course_code IS 'Short identifier, e.g. CS101.';
COMMENT ON COLUMN courses.credits     IS 'Credit hours (1–6).';

-- ── ENROLLMENTS ─────────────────────────────────────────────────────
-- Associative (junction) table between students and courses.
-- Each row = one student enrolled in one course for one semester.
CREATE TABLE enrollments (
    enrollment_id   BIGSERIAL    PRIMARY KEY,
    student_id      BIGINT       NOT NULL
                        REFERENCES students(student_id) ON DELETE CASCADE,
    course_id       BIGINT       NOT NULL
                        REFERENCES courses(course_id)  ON DELETE CASCADE,
    semester        VARCHAR(20)  NOT NULL,          -- e.g. 'Fall 2024'
    grade           CHAR(2),                        -- e.g. 'A', 'B+', NULL if in-progress
    enrollment_date DATE         NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT uq_enrollment UNIQUE (student_id, course_id, semester)
);

COMMENT ON TABLE  enrollments                  IS 'Junction table: student ↔ course per semester.';
COMMENT ON COLUMN enrollments.grade            IS 'Letter grade; NULL while course is in progress.';
COMMENT ON COLUMN enrollments.enrollment_date  IS 'Date the student registered for the course.';

-- ── Indexes for common search patterns ──────────────────────────────
CREATE INDEX idx_students_last_name  ON students(lower(last_name));
CREATE INDEX idx_students_first_name ON students(lower(first_name));
CREATE INDEX idx_courses_name        ON courses(lower(course_name));
CREATE INDEX idx_enrollments_student ON enrollments(student_id);
CREATE INDEX idx_enrollments_course  ON enrollments(course_id);
