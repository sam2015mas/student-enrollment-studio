# ═══════════════════════════════════════════════════════════════
# Student Enrollment Studio — Application Image
# Multi-stage build: compile with Maven, run on a slim JRE
# ═══════════════════════════════════════════════════════════════

# ── Stage 1: Build ────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy only the POM first so Maven can cache dependency downloads
# as a separate Docker layer (dependencies rarely change as often
# as source code, so this avoids re-downloading on every code edit).
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Now copy the rest of the source and build the jar.
# Unit tests (Mockito-based, no external services) run here;
# tests that require a live database are tagged and excluded
# from this build-time run (see Testing section of the guide).
COPY src ./src
RUN mvn -B clean package -DskipITs -Dgroups='!requiresDatabase'

# ── Stage 2: Runtime ──────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Run as a non-root user for defense-in-depth
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

# Container-level health endpoint (requires spring-boot-starter-actuator)
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
