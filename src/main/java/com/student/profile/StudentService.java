package com.student.profile;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Service responsible for loading the student's display name and major
 * from {@code src/main/resources/student.csv}.
 *
 * <p>Expected CSV format (header row + one data row):</p>
 * <pre>
 * name,major
 * Jane Doe,Computer Science
 * </pre>
 *
 * <p>The first row (header) is skipped; the first data row is returned as a
 * {@link Student} record.</p>
 */
@Service
public class StudentService {

    /**
     * Reads the first student entry from the classpath resource
     * {@code student.csv}.
     *
     * @return a {@link Student} record; never {@code null}
     */
    public Student loadStudent() {
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(
                        new ClassPathResource("student.csv").getInputStream()))) {

            List<String[]> rows = reader.readAll();

            // rows.get(0) is the header; rows.get(1) is the first data row
            if (rows.size() < 2) {
                return new Student("Unknown", "Unknown");
            }

            String[] data = rows.get(1);
            String name  = data.length > 0 ? data[0].trim() : "Unknown";
            String major = data.length > 1 ? data[1].trim() : "Unknown";
            return new Student(name, major);

        } catch (IOException | CsvException ex) {
            return new Student("Error reading CSV", "Error reading CSV");
        }
    }
}
