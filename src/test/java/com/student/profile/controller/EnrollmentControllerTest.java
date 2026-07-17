package com.student.profile.controller;

import com.student.profile.entity.EnrollmentEntity;
import com.student.profile.service.EnrollmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests for {@link EnrollmentController} using {@code @WebMvcTest}.
 *
 * <p>Only the web layer (dispatcher servlet, controller, Thymeleaf view
 * resolution) is loaded — the real {@link EnrollmentService} is replaced
 * with a Mockito bean, so these tests are fast and don't touch the
 * database.</p>
 */
@WebMvcTest(EnrollmentController.class)
@DisplayName("EnrollmentController — web layer tests")
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnrollmentService enrollmentService;

    @Test
    @DisplayName("GET /enrollments with no keyword shows all records")
    void enrollments_noKeyword_showsAllRecords() throws Exception {
        EnrollmentEntity e = new EnrollmentEntity();
        e.setEnrollmentId(1L);
        when(enrollmentService.search("")).thenReturn(List.of(e));

        mockMvc.perform(get("/enrollments"))
                .andExpect(status().isOk())
                .andExpect(view().name("enrollments"))
                .andExpect(model().attribute("keyword", ""))
                .andExpect(model().attribute("totalCount", 1))
                .andExpect(model().attributeExists("enrollments"));

        verify(enrollmentService).search("");
    }

    @Test
    @DisplayName("GET /enrollments?keyword=Alice forwards the keyword and shows matching count")
    void enrollments_withKeyword_showsFilteredRecordsAndEchoesKeyword() throws Exception {
        EnrollmentEntity e = new EnrollmentEntity();
        e.setEnrollmentId(1L);
        e.setFirstName("Alice");
        when(enrollmentService.search("Alice")).thenReturn(List.of(e));

        mockMvc.perform(get("/enrollments").param("keyword", "Alice"))
                .andExpect(status().isOk())
                .andExpect(view().name("enrollments"))
                .andExpect(model().attribute("keyword", "Alice"))
                .andExpect(model().attribute("totalCount", 1));

        verify(enrollmentService).search(eq("Alice"));
    }

    @Test
    @DisplayName("GET /enrollments?keyword=zzz with no matches shows totalCount=0, not an error")
    void enrollments_noMatches_returnsEmptyResultsGracefully() throws Exception {
        when(enrollmentService.search("zzz")).thenReturn(List.of());

        mockMvc.perform(get("/enrollments").param("keyword", "zzz"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalCount", 0));
    }
}
