package com.student.profile.service;

import com.student.profile.entity.EnrollmentEntity;
import com.student.profile.mapper.EnrollmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Service layer for enrollment queries.
 *
 * <p>Sits between the web controller and the MyBatis mapper to keep
 * business logic out of both the controller and the data-access layer.</p>
 */
@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    /**
     * Returns enrollments that match the given keyword, or all enrollments
     * when the keyword is blank.
     *
     * @param keyword search term (student name or course name); may be null/blank
     * @return non-null list of matching enrollments
     */
    public List<EnrollmentEntity> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return enrollmentMapper.findAllEnrollments();
        }
        return enrollmentMapper.searchEnrollments(keyword.trim());
    }
}
