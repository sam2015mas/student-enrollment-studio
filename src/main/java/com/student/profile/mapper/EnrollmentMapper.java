package com.student.profile.mapper;

import com.student.profile.entity.EnrollmentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MyBatis mapper interface for the enrollment search feature.
 *
 * <p>At runtime MyBatis generates a dynamic proxy that performs the
 * following steps for every method call:</p>
 * <ol>
 *   <li><strong>Obtain a connection</strong> from the HikariCP connection pool
 *       configured in {@code application.properties}.</li>
 *   <li><strong>Create a PreparedStatement</strong> using the SQL in the
 *       {@code @Select} annotation (parameters are bound as {@code ?}
 *       placeholders to prevent SQL injection).</li>
 *   <li><strong>Execute the query</strong> and receive a {@code ResultSet}.</li>
 *   <li><strong>Iterate the ResultSet</strong> with an internal {@code while}
 *       loop, creating one {@link EnrollmentEntity} per row.</li>
 *   <li><strong>Map columns → fields</strong> using the {@code @Results}/
 *       {@code @Result} annotations below (camelCase mapping is also
 *       enabled globally via {@code mybatis.configuration
 *       .map-underscore-to-camel-case=true}).</li>
 *   <li><strong>Release the connection</strong> back to the pool when the
 *       method returns (or if an exception is thrown).</li>
 * </ol>
 *
 * <p>The {@code @Mapper} annotation causes Spring Boot to auto-detect this
 * interface and register the generated proxy as a Spring bean — no XML
 * configuration required.</p>
 */
@Mapper
public interface EnrollmentMapper {

    /**
     * Searches enrollment records by student name (first or last) OR course name.
     *
     * <p>The {@code ILIKE} operator performs a case-insensitive pattern match
     * in PostgreSQL.  The {@code %keyword%} wildcard matches any substring.</p>
     *
     * <p>MyBatis internal flow for this method:</p>
     * <ol>
     *   <li>Pool → {@code Connection conn = dataSource.getConnection()}</li>
     *   <li>{@code PreparedStatement ps = conn.prepareStatement(sql)}</li>
     *   <li>{@code ps.setString(1, keyword); ... ps.execute()}</li>
     *   <li>{@code ResultSet rs = ps.getResultSet()}</li>
     *   <li>{@code while (rs.next()) \{ EnrollmentEntity e = new EnrollmentEntity(); ... \}}</li>
     *   <li>{@code conn.close()} → returns to pool</li>
     * </ol>
     *
     * @param keyword search term; matched case-insensitively against student
     *                first name, last name, and course name
     * @return list of matching enrollment records (may be empty, never null)
     */
    @Select("""
            SELECT
                e.enrollment_id,
                e.student_id,
                e.course_id,
                e.semester,
                e.grade,
                e.enrollment_date,
                s.first_name,
                s.last_name,
                s.email,
                s.major,
                c.course_code,
                c.course_name,
                c.credits,
                c.instructor
            FROM enrollments e
            JOIN students  s ON s.student_id = e.student_id
            JOIN courses   c ON c.course_id  = e.course_id
    		WHERE s.first_name ILIKE '%' || #{keyword} || '%'
    		OR s.last_name  ILIKE '%' || #{keyword} || '%'
    		OR (s.first_name || ' ' || s.last_name) ILIKE '%' || #{keyword} || '%'
    		OR s.major       ILIKE '%' || #{keyword} || '%'
    		OR c.course_name ILIKE '%' || #{keyword} || '%'
            ORDER BY e.enrollment_id
            """)
    @Results(id = "enrollmentResultMap", value = {
            // ── Enrollment table columns ──────────────────────────────────
            @Result(property = "enrollmentId",   column = "enrollment_id"),
            @Result(property = "studentId",      column = "student_id"),
            @Result(property = "courseId",       column = "course_id"),
            @Result(property = "semester",       column = "semester"),
            @Result(property = "grade",          column = "grade"),
            @Result(property = "enrollmentDate", column = "enrollment_date"),
            // ── Joined student columns ────────────────────────────────────
            @Result(property = "firstName",      column = "first_name"),
            @Result(property = "lastName",       column = "last_name"),
            @Result(property = "email",          column = "email"),
            @Result(property = "major",          column = "major"),
            // ── Joined course columns ─────────────────────────────────────
            @Result(property = "courseCode",     column = "course_code"),
            @Result(property = "courseName",     column = "course_name"),
            @Result(property = "credits",        column = "credits"),
            @Result(property = "instructor",     column = "instructor")
    })
    List<EnrollmentEntity> searchEnrollments(@Param("keyword") String keyword);

    /**
     * Returns all enrollment records (used to populate the default enrollment page).
     *
     * <p>Uses the same {@code @Results} mapping defined on
     * {@link #searchEnrollments(String)} via {@code @ResultMap}.</p>
     */
    @Select("""
            SELECT
                e.enrollment_id,
                e.student_id,
                e.course_id,
                e.semester,
                e.grade,
                e.enrollment_date,
                s.first_name,
                s.last_name,
                s.email,
                s.major,
                c.course_code,
                c.course_name,
                c.credits,
                c.instructor
            FROM enrollments e
            JOIN students  s ON s.student_id = e.student_id
            JOIN courses   c ON c.course_id  = e.course_id
            ORDER BY e.enrollment_id
            """)
    @Results(value = {
            @Result(property = "enrollmentId",   column = "enrollment_id"),
            @Result(property = "studentId",      column = "student_id"),
            @Result(property = "courseId",       column = "course_id"),
            @Result(property = "semester",       column = "semester"),
            @Result(property = "grade",          column = "grade"),
            @Result(property = "enrollmentDate", column = "enrollment_date"),
            @Result(property = "firstName",      column = "first_name"),
            @Result(property = "lastName",       column = "last_name"),
            @Result(property = "email",          column = "email"),
            @Result(property = "major",          column = "major"),
            @Result(property = "courseCode",     column = "course_code"),
            @Result(property = "courseName",     column = "course_name"),
            @Result(property = "credits",        column = "credits"),
            @Result(property = "instructor",     column = "instructor")
    })
    List<EnrollmentEntity> findAllEnrollments();
}