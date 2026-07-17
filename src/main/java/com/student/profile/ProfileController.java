package com.student.profile;

import com.student.profile.entity.EnrollmentEntity;
import com.student.profile.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * MVC controller for the home/search page at {@code /}.
 *
 * <p>On a plain GET with no keyword the search form is shown with no results.
 * When the user submits a keyword, matching enrollment records are fetched
 * and displayed in the results table below the form.</p>
 */
@Controller
public class ProfileController {

    @Autowired
    private EnrollmentService enrollmentService;

    /**
     * Handles {@code GET /}.
     *
     * <ul>
     *   <li>No keyword → shows search form only; {@code enrollments} is null
     *       so the results section stays hidden.</li>
     *   <li>Keyword present → runs the search and populates the results
     *       table; forwards matching records to the Enrollments page.</li>
     * </ul>
     *
     * @param keyword optional search term (student name or course name)
     * @param model   Spring MVC model passed to the Thymeleaf template
     * @return logical view name {@code index}
     */
    @GetMapping("/")
    public String index(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            Model model) {

        model.addAttribute("keyword", keyword);

        if (!keyword.isBlank()) {
            List<EnrollmentEntity> results = enrollmentService.search(keyword);
            model.addAttribute("enrollments", results);
            model.addAttribute("totalCount", results.size());
        }
        // enrollments stays null when keyword is blank → results section hidden in template

        return "index";
    }
}
