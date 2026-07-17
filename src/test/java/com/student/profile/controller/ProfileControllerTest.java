package com.student.profile.controller;

import com.student.profile.ProfileController;
import com.student.profile.entity.EnrollmentEntity;
import com.student.profile.service.EnrollmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests for {@link ProfileController} (the {@code /} home + search page).
 */
@WebMvcTest(ProfileController.class)
@DisplayName("ProfileController — home/search page web layer tests")
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnrollmentService enrollmentService;

    @Test
    @DisplayName("GET / with no keyword shows the bare search form (no query executed)")
    void index_noKeyword_doesNotQueryAndHidesResults() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("keyword", ""))
                .andExpect(model().attributeDoesNotExist("enrollments"));

        // Business rule under test: a blank keyword on the home page must
        // NOT trigger a full-table search (that only happens on /enrollments).
        verifyNoInteractions(enrollmentService);
    }

    @Test
    @DisplayName("GET /?keyword=Programming runs the search and populates results + totalCount")
    void index_withKeyword_runsSearchAndPopulatesModel() throws Exception {
        EnrollmentEntity e = new EnrollmentEntity();
        e.setCourseName("Introduction to Programming");
        when(enrollmentService.search("Programming")).thenReturn(List.of(e));

        mockMvc.perform(get("/").param("keyword", "Programming"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("keyword", "Programming"))
                .andExpect(model().attribute("totalCount", 1))
                .andExpect(model().attributeExists("enrollments"));
    }
}
