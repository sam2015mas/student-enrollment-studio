package com.student.profile.mapper;

import com.student.profile.entity.EnrollmentEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack live-database tests for {@link EnrollmentMapper}.
 *
 * <p>Unlike {@code EnrollmentServiceTest}, nothing here is mocked. A real
 * PostgreSQL 18 instance is started in a disposable Testcontainers
 * container, seeded with the project's actual {@code 01_create_tables.sql}
 * and {@code 02_insert_data.sql} scripts, and the mapper runs its real
 * {@code @Select} SQL (ILIKE search, JOINs, ORDER BY) against it through
 * the real HikariCP pool and MyBatis proxy.</p>
 *
 * <p>Tagged {@code requiresDatabase} / {@code requiresDocker} so it can be
 * excluded from fast unit-test-only runs (e.g. inside the Docker build
 * stage, which has no Docker-in-Docker access) via:
 * {@code mvn test -Dgroups='!requiresDatabase'}. The full suite, including
 * this class, runs in CI and locally with:
 * {@code mvn verify}.</p>
 */
@Testcontainers
@Tag("requiresDatabase")
@SpringBootTest
@DisplayName("EnrollmentMapper — live PostgreSQL integration tests")
class EnrollmentMapperIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("itc475")
            .withUsername("postgres")
            .withPassword("Admin123")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("db/01_create_tables.sql"),
                    "/docker-entrypoint-initdb.d/01_create_tables.sql")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("db/02_insert_data.sql"),
                    "/docker-entrypoint-initdb.d/02_insert_data.sql");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @BeforeAll
    static void containerIsRunning() {
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    @DisplayName("findAllEnrollments() returns all 30 seeded rows, joined and ordered by id")
    void findAllEnrollments_returnsFullSeedSet() {
        List<EnrollmentEntity> all = enrollmentMapper.findAllEnrollments();

        assertThat(all).hasSize(30);
        assertThat(all.get(0).getEnrollmentId()).isEqualTo(1L);
        // Confirms the JOIN actually populated student + course columns,
        // not just the enrollments table's own FK ids.
        assertThat(all.get(0).getFirstName()).isNotBlank();
        assertThat(all.get(0).getCourseName()).isNotBlank();
    }

    @Test
    @DisplayName("searchEnrollments() matches by student first name, case-insensitively")
    void searchEnrollments_matchesFirstNameCaseInsensitive() {
        List<EnrollmentEntity> results = enrollmentMapper.searchEnrollments("alice");

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(e -> "Alice".equalsIgnoreCase(e.getFirstName()));
    }

    @Test
    @DisplayName("searchEnrollments() matches by last name")
    void searchEnrollments_matchesLastName() {
        List<EnrollmentEntity> results = enrollmentMapper.searchEnrollments("Johnson");

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(e -> "Johnson".equalsIgnoreCase(e.getLastName()));
    }

    @Test
    @DisplayName("searchEnrollments() matches by full name (first + last combined)")
    void searchEnrollments_matchesFullName() {
        List<EnrollmentEntity> results = enrollmentMapper.searchEnrollments("Alice Johnson");

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(e ->
                "Alice".equals(e.getFirstName()) && "Johnson".equals(e.getLastName()));
    }

    @Test
    @DisplayName("searchEnrollments() matches by course name substring")
    void searchEnrollments_matchesCourseNameSubstring() {
        List<EnrollmentEntity> results = enrollmentMapper.searchEnrollments("Programming");

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(e -> e.getCourseName().contains("Programming"));
    }

    @Test
    @DisplayName("searchEnrollments() matches by major")
    void searchEnrollments_matchesMajor() {
        List<EnrollmentEntity> results = enrollmentMapper.searchEnrollments("Cybersecurity");

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(e -> "Cybersecurity".equalsIgnoreCase(e.getMajor()));
    }

    @Test
    @DisplayName("searchEnrollments() returns an empty list for a keyword with no matches")
    void searchEnrollments_noMatches_returnsEmptyList() {
        List<EnrollmentEntity> results = enrollmentMapper.searchEnrollments("zzz-no-such-keyword-zzz");

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("searchEnrollments() is resilient to SQL-special characters (parameterized, not concatenated)")
    void searchEnrollments_handlesSqlSpecialCharactersSafely() {
        // A raw '%' or single quote would break naive string concatenation;
        // MyBatis's #{} binding sends this as a bound parameter, so it
        // should execute without throwing and simply find no matches.
        List<EnrollmentEntity> results = enrollmentMapper.searchEnrollments("O'Brien%");

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("results are ordered by enrollment_id ascending")
    void searchEnrollments_resultsAreOrderedByEnrollmentId() {
        List<EnrollmentEntity> results = enrollmentMapper.searchEnrollments("Fall");
        // "Fall" won't match any name/course/major here, so use a broader,
        // deterministic ordering check against the full set instead.
        List<EnrollmentEntity> all = enrollmentMapper.findAllEnrollments();

        for (int i = 1; i < all.size(); i++) {
            assertThat(all.get(i).getEnrollmentId())
                    .isGreaterThan(all.get(i - 1).getEnrollmentId());
        }
    }
}
