package com.student.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Student Profile Spring Boot application.
 *
 * <p>Spring Boot requires a standard static {@code main} method as the JVM
 * entry point.  The {@code @SpringBootApplication} annotation enables:</p>
 * <ul>
 *   <li>Auto-configuration of Spring MVC, Thymeleaf, DataSource, and MyBatis</li>
 *   <li>Component scan of all classes under {@code com.student.profile}</li>
 *   <li>Detection of all {@code @Mapper} interfaces via MyBatis Spring Boot Starter</li>
 * </ul>
 */
@SpringBootApplication
public class ProfileApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfileApplication.class, args);
    }
}
