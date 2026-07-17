package com.student.profile;

import com.student.profile.entity.EnrollmentEntity;
import com.student.profile.mapper.EnrollmentMapper;
import com.student.profile.service.EnrollmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end smoke test: boots the full Spring context on a random port,
 * against a real (Testcontainers) PostgreSQL instance, and exercises the
 * stack the same way a browser would — HTTP request in, rendered
 * Thymeleaf page out, backed by a real query against a real database.
 *
 * <p>This is the automated equivalent of the manual end-to-end
 * verification described in the deployment guide's Proxmox section,
 * and is what CI runs before an image is considered release-ready.</p>
 */
@Testcontainers
@Tag("requiresDatabase")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Application — full end-to-end smoke test")
class ProfileApplicationIT {

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

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("application context loads successfully with a live datasource")
    void contextLoads() {
        assertThat(enrollmentService).isNotNull();
        assertThat(enrollmentMapper).isNotNull();
    }

    @Test
    @DisplayName("live database connectivity: raw JDBC round-trip against the container")
    void liveDatabaseConnectivity_rawJdbcRoundTrip() {
        Integer studentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Integer.class);
        Integer courseCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM courses", Integer.class);
        Integer enrollmentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM enrollments", Integer.class);

        assertThat(studentCount).isEqualTo(10);
        assertThat(courseCount).isEqualTo(8);
        assertThat(enrollmentCount).isEqualTo(30);
    }

    @Test
    @DisplayName("GET / renders HTTP 200 and the home page HTML")
    void homePage_returnsOk() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("<html");
    }

    @Test
    @DisplayName("GET /?keyword=Alice returns matching enrollment rows end-to-end (HTTP -> service -> mapper -> Postgres)")
    void searchEndToEnd_viaHomePage() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("http://localhost:" + port + "/?keyword=Alice", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Alice");
    }

    @Test
    @DisplayName("GET /enrollments with no keyword lists all 30 seeded rows")
    void enrollmentsPage_noKeyword_listsAllRows() {
        List<EnrollmentEntity> all = enrollmentService.search("");
        assertThat(all).hasSize(30);

        ResponseEntity<String> response =
                restTemplate.getForEntity("http://localhost:" + port + "/enrollments", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
