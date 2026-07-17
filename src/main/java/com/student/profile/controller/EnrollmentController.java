package com.student.profile.controller;

import com.student.profile.entity.EnrollmentEntity;
import com.student.profile.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * MVC controller for the enrollments display page at {@code /enrollments}.
 *
 * <p>This page has no search form of its own. It receives an optional
 * {@code keyword} parameter forwarded from the home page search, or
 * displays all enrollment records when accessed directly with no keyword.</p>
 */
@Controller
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    /**
     * Handles {@code GET /enrollments}.
     *
     * <ul>
     *   <li>With {@code keyword} → displays only the matching records and
     *       shows the keyword in the page heading.</li>
     *   <li>Without {@code keyword} → displays all enrollment records.</li>
     * </ul>
     *
     * @param keyword optional search term passed from the home search page
     * @param model   Spring MVC model passed to the Thymeleaf template
     * @return logical view name {@code enrollments}
     */
    @GetMapping("/enrollments")
    public String enrollments(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            Model model) {

        List<EnrollmentEntity> results = enrollmentService.search(keyword);

        model.addAttribute("enrollments", results);
        model.addAttribute("keyword", keyword);
        model.addAttribute("totalCount", results.size());

        return "enrollments";
    }
}
