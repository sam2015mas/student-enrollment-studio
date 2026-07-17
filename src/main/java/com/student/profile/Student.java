package com.student.profile;

/**
 * Immutable data carrier for a student's name and major (read from CSV).
 *
 * <p>Uses a Java <strong>record</strong> (permanent since Java 16) which
 * auto-generates {@code name()} and {@code major()} accessors,
 * {@code equals()}, {@code hashCode()}, and {@code toString()}.
 * No boilerplate getters or setters needed.</p>
 *
 * <p>This record is used exclusively by {@link StudentService} and
 * {@link ProfileController} to display the profile on the home page.
 * It is <em>separate</em> from {@link com.student.profile.entity.StudentEntity},
 * which is the MyBatis ORM entity mapped to the {@code students} database table.</p>
 */
public record Student(String name, String major) {}
