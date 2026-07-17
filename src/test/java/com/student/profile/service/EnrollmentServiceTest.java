package com.student.profile.service;

import com.student.profile.entity.EnrollmentEntity;
import com.student.profile.mapper.EnrollmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EnrollmentService}.
 *
 * <p>The mapper is mocked with Mockito, so these tests run in
 * milliseconds with no database, network, or Spring context involved.
 * They validate the branching/business logic inside the service layer
 * in isolation from the data-access layer.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService — unit tests (mocked mapper)")
class EnrollmentServiceTest {

    @Mock
    private EnrollmentMapper enrollmentMapper;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private EnrollmentEntity sampleEnrollment;

    @BeforeEach
    void setUp() {
        sampleEnrollment = new EnrollmentEntity();
        sampleEnrollment.setEnrollmentId(1L);
        sampleEnrollment.setFirstName("Alice");
        sampleEnrollment.setLastName("Johnson");
        sampleEnrollment.setCourseName("Introduction to Programming");
    }

    @Nested
    @DisplayName("when keyword is blank or absent")
    class BlankKeyword {

        @ParameterizedTest(name = "keyword=\"{0}\" falls back to findAllEnrollments()")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t"})
        void search_withBlankKeyword_returnsAllEnrollments(String keyword) {
            when(enrollmentMapper.findAllEnrollments()).thenReturn(List.of(sampleEnrollment));

            List<EnrollmentEntity> result = enrollmentService.search(keyword);

            assertThat(result).containsExactly(sampleEnrollment);
            verify(enrollmentMapper, times(1)).findAllEnrollments();
            verify(enrollmentMapper, never()).searchEnrollments(anyString());
        }
    }

    @Nested
    @DisplayName("when keyword has content")
    class NonBlankKeyword {

        @Test
        @DisplayName("delegates to searchEnrollments() with the keyword")
        void search_withKeyword_delegatesToSearchEnrollments() {
            when(enrollmentMapper.searchEnrollments("Alice")).thenReturn(List.of(sampleEnrollment));

            List<EnrollmentEntity> result = enrollmentService.search("Alice");

            assertThat(result).containsExactly(sampleEnrollment);
            verify(enrollmentMapper).searchEnrollments("Alice");
            verify(enrollmentMapper, never()).findAllEnrollments();
        }

        @Test
        @DisplayName("trims leading/trailing whitespace before querying")
        void search_trimsWhitespaceFromKeyword() {
            when(enrollmentMapper.searchEnrollments("Alice")).thenReturn(List.of(sampleEnrollment));

            enrollmentService.search("  Alice  ");

            verify(enrollmentMapper).searchEnrollments("Alice");
        }

        @Test
        @DisplayName("returns an empty (non-null) list when nothing matches")
        void search_noMatches_returnsEmptyList() {
            when(enrollmentMapper.searchEnrollments("zzzz")).thenReturn(List.of());

            List<EnrollmentEntity> result = enrollmentService.search("zzzz");

            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("keyword matching by major is passed through unchanged")
        void search_byMajorKeyword_passesThrough() {
            when(enrollmentMapper.searchEnrollments("Cybersecurity")).thenReturn(List.of(sampleEnrollment));

            enrollmentService.search("Cybersecurity");

            verify(enrollmentMapper).searchEnrollments("Cybersecurity");
        }
    }
}
