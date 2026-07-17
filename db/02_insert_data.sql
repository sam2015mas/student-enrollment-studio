-- ════════════════════════════════════════════════════════════════════
-- Student Course Registration System — Seed / Dummy Data
-- Target: PostgreSQL 18  |  Database: itc475
-- Run AFTER 01_create_tables.sql:
--   psql -U postgres -d itc475 -f 02_insert_data.sql
-- ════════════════════════════════════════════════════════════════════

-- ── Students (10 rows) ───────────────────────────────────────────────
INSERT INTO students (first_name, last_name, email, major) VALUES
    ('Alice',   'Johnson',    'alice.johnson@university.edu',    'Computer Science'),
    ('Bob',     'Smith',      'bob.smith@university.edu',        'Information Technology'),
    ('Carol',   'Williams',   'carol.williams@university.edu',   'Computer Science'),
    ('David',   'Brown',      'david.brown@university.edu',      'Cybersecurity'),
    ('Emily',   'Davis',      'emily.davis@university.edu',      'Data Science'),
    ('Frank',   'Miller',     'frank.miller@university.edu',     'Information Technology'),
    ('Grace',   'Wilson',     'grace.wilson@university.edu',     'Computer Science'),
    ('Henry',   'Moore',      'henry.moore@university.edu',      'Network Engineering'),
    ('Isabella','Taylor',     'isabella.taylor@university.edu',  'Data Science'),
    ('James',   'Anderson',   'james.anderson@university.edu',   'Cybersecurity');

-- ── Courses (8 rows) ────────────────────────────────────────────────
INSERT INTO courses (course_code, course_name, credits, instructor) VALUES
    ('CS101',  'Introduction to Programming',         3, 'Dr. Rebecca Chen'),
    ('CS201',  'Data Structures and Algorithms',      3, 'Dr. Michael Torres'),
    ('CS301',  'Database Systems',                    3, 'Dr. Rebecca Chen'),
    ('CS401',  'Software Engineering',                3, 'Prof. Sandra Lee'),
    ('ITC310', 'Network Administration',              3, 'Prof. Daniel Kim'),
    ('ITC475', 'Web Application Development',         3, 'Prof. Daniel Kim'),
    ('DS201',  'Introduction to Data Science',        3, 'Dr. Olivia Nguyen'),
    ('CY301',  'Ethical Hacking and Penetration Test',3, 'Dr. Samuel Okafor');

-- ── Enrollments (30 rows) ───────────────────────────────────────────
INSERT INTO enrollments (student_id, course_id, semester, grade, enrollment_date) VALUES
    -- Alice Johnson
    (1, 1, 'Fall 2023',   'A',  '2023-08-20'),
    (1, 2, 'Spring 2024', 'A-', '2024-01-15'),
    (1, 3, 'Fall 2024',   'B+', '2024-08-19'),
    (1, 6, 'Spring 2025', NULL, '2025-01-13'),
    -- Bob Smith
    (2, 1, 'Fall 2023',   'B+', '2023-08-21'),
    (2, 5, 'Spring 2024', 'A',  '2024-01-16'),
    (2, 6, 'Fall 2024',   'A-', '2024-08-20'),
    -- Carol Williams
    (3, 1, 'Fall 2023',   'A',  '2023-08-20'),
    (3, 2, 'Spring 2024', 'B+', '2024-01-15'),
    (3, 4, 'Fall 2024',   'A',  '2024-08-18'),
    -- David Brown
    (4, 1, 'Fall 2023',   'B',  '2023-08-22'),
    (4, 8, 'Spring 2024', 'A-', '2024-01-17'),
    (4, 5, 'Fall 2024',   'B+', '2024-08-19'),
    -- Emily Davis
    (5, 1, 'Fall 2023',   'A-', '2023-08-20'),
    (5, 7, 'Spring 2024', 'A',  '2024-01-14'),
    (5, 3, 'Fall 2024',   'A',  '2024-08-18'),
    (5, 6, 'Spring 2025', NULL, '2025-01-13'),
    -- Frank Miller
    (6, 1, 'Fall 2023',   'C+', '2023-08-23'),
    (6, 5, 'Spring 2024', 'B',  '2024-01-18'),
    (6, 6, 'Fall 2024',   'B-', '2024-08-21'),
    -- Grace Wilson
    (7, 2, 'Fall 2023',   'A',  '2023-08-20'),
    (7, 3, 'Spring 2024', 'A-', '2024-01-15'),
    (7, 4, 'Fall 2024',   'A',  '2024-08-19'),
    -- Henry Moore
    (8, 1, 'Fall 2023',   'B+', '2023-08-21'),
    (8, 5, 'Spring 2024', 'A',  '2024-01-16'),
    (8, 6, 'Fall 2024',   'B+', '2024-08-20'),
    -- Isabella Taylor
    (9, 1, 'Fall 2023',   'A-', '2023-08-20'),
    (9, 7, 'Spring 2024', 'A',  '2024-01-14'),
    -- James Anderson
    (10, 1, 'Fall 2023',  'B',  '2023-08-22'),
    (10, 8, 'Spring 2024','A',  '2024-01-17');

-- ── Verification queries ─────────────────────────────────────────────
SELECT COUNT(*) AS student_count  FROM students;
SELECT COUNT(*) AS course_count   FROM courses;
SELECT COUNT(*) AS enrollment_cnt FROM enrollments;
