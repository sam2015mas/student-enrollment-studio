package com.student.profile.entity;

/**
 * ORM entity representing the {@code enrollments} table.
 *
 * <p>This entity is also used as a <em>read model</em> (view object) when
 * the mapper JOINs students and courses so that the Thymeleaf template
 * can display all columns in a single flat object.</p>
 *
 * <p>ER relationship: Enrollment is the associative (junction) entity
 * linking one Student to one Course with additional attributes
 * (semester, grade, enrollmentDate).</p>
 */
public class EnrollmentEntity {

    // ── Enrollment-table columns ──────────────────────────────────────────

    /** Primary key. */
    private Long enrollmentId;

    /** FK → students.student_id */
    private Long studentId;

    /** FK → courses.course_id */
    private Long courseId;

    /** Academic semester (e.g. "Fall 2024"). */
    private String semester;

    /** Letter grade assigned at the end of the semester (may be null). */
    private String grade;

    /** ISO date the student enrolled (DB column: {@code enrollment_date}). */
    private String enrollmentDate;

    // ── Joined columns from students ──────────────────────────────────────

    private String firstName;
    private String lastName;
    private String email;
    private String major;

    // ── Joined columns from courses ───────────────────────────────────────

    private String courseCode;
    private String courseName;
    private Integer credits;
    private String instructor;

    // ── Getters ───────────────────────────────────────────────────────────

    public Long   getEnrollmentId()   { return enrollmentId; }
    public Long   getStudentId()      { return studentId; }
    public Long   getCourseId()       { return courseId; }
    public String getSemester()       { return semester; }
    public String getGrade()          { return grade; }
    public String getEnrollmentDate() { return enrollmentDate; }
    public String getFirstName()      { return firstName; }
    public String getLastName()       { return lastName; }
    public String getEmail()          { return email; }
    public String getMajor()          { return major; }
    public String getCourseCode()     { return courseCode; }
    public String getCourseName()     { return courseName; }
    public Integer getCredits()       { return credits; }
    public String getInstructor()     { return instructor; }

    // ── Setters ───────────────────────────────────────────────────────────

    public void setEnrollmentId(Long enrollmentId)     { this.enrollmentId = enrollmentId; }
    public void setStudentId(Long studentId)           { this.studentId = studentId; }
    public void setCourseId(Long courseId)             { this.courseId = courseId; }
    public void setSemester(String semester)           { this.semester = semester; }
    public void setGrade(String grade)                 { this.grade = grade; }
    public void setEnrollmentDate(String enrollmentDate){ this.enrollmentDate = enrollmentDate; }
    public void setFirstName(String firstName)         { this.firstName = firstName; }
    public void setLastName(String lastName)           { this.lastName = lastName; }
    public void setEmail(String email)                 { this.email = email; }
    public void setMajor(String major)                 { this.major = major; }
    public void setCourseCode(String courseCode)       { this.courseCode = courseCode; }
    public void setCourseName(String courseName)       { this.courseName = courseName; }
    public void setCredits(Integer credits)            { this.credits = credits; }
    public void setInstructor(String instructor)       { this.instructor = instructor; }

    /** Convenience: full name for display. */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
