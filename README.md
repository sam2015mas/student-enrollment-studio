# Student Enrollment Studio

A cloud-native, full-stack student enrollment search application built with Spring Boot MVC, Thymeleaf, MyBatis, and PostgreSQL — containerized with Docker and deployable to a local machine or a Proxmox VE private cloud VM.

## Architecture

- **Presentation layer**: Thymeleaf server-rendered views (`index.html`, `enrollments.html`)
- **Web layer**: Spring MVC controllers (`ProfileController` at `/`, `EnrollmentController` at `/enrollments`)
- **Business layer**: `EnrollmentService`, `StudentService`
- **Data access layer**: MyBatis (`EnrollmentMapper`, annotation-based)
- **Database**: PostgreSQL 18 (database name `itc475`), schema/seed data in `db/01_create_tables.sql` and `db/02_insert_data.sql`
- **Developer reference**: springdoc-openapi / Swagger UI at `/swagger-ui.html` (shows no operations by design — this app has no JSON REST endpoints, only server-rendered pages)
- **Health/monitoring**: Spring Boot Actuator at `/actuator/health`

Both the app and the database run as separate Docker containers on a custom bridge network (`enrollment-net`), giving the app container automatic DNS resolution of the database container by name (`postgres`), without exposing either to the host network unnecessarily.

## Prerequisites

You do **not** need Java or Maven installed on the deployment host — the Docker build stage pulls its own pinned Maven 3.9 + JDK 21 image and builds the application entirely inside the container, isolated from whatever Java/Maven version the host has.

Required on the deployment host:
- Docker Engine (Linux) or Docker Desktop (Windows/Mac), with the Compose plugin (`docker compose version` should return a version)
- Git (only if pulling from a repository rather than copying files directly)

Optional, only if you want to build/test outside Docker on the host directly:
- JDK 21+ (JDK 25 also works — the `pom.xml` includes a fix for a known Byte Buddy/Mockito compatibility gap on newer JDKs)
- Maven 3.6.3+

## Installation

### Option A — Local machine (Windows/Mac with Docker Desktop)

```powershell
git clone https://github.com/sam2015mas/student-enrollment-studio.git
cd student-enrollment-studio
docker compose up --build -d
```

The app is available at `http://localhost:8081/` (see [Configuration reference](#configuration-reference) for why 8081, not 8080).

### Option B — Proxmox VE / Ubuntu VM (private cloud deployment)

On the Ubuntu VM (22.04 LTS or later), via SSH or the Proxmox web console:

```bash
# Confirm Docker is available
docker --version
docker compose version

# If your user isn't in the docker group yet, add yourself (avoids needing sudo每次)
sudo usermod -aG docker $USER    # avoids needing sudo each time; log out/in after, or run: newgrp docker

# Install git if needed
sudo apt-get update && sudo apt-get install -y git

# Clone and deploy
git clone https://github.com/sam2015mas/student-enrollment-studio.git
cd student-enrollment-studio
sudo docker compose up --build -d
```

If the host's port 8080 is already in use by another service, this project is already configured to map the app to host port **8081** instead (see below) — no changes needed.

If `ufw` is active on the VM, allow the app port so it's reachable from other machines on the same network:

```bash
sudo ufw allow 8081/tcp
```

Note: if the VM sits on a private/internal network (e.g. a university-managed Proxmox host), it will only be reachable from machines on that same network or via VPN — this is a network topology consideration, not an application issue. See [Testing without external network access](#testing-without-external-network-access) below.

## Configuration reference

| Setting | Value | Notes |
|---|---|---|
| App host port | `8081` | Container listens on 8080 internally; host mapping is 8081 to avoid conflicts with other services |
| Database host port | `5432` | Exposed for local `psql`/debugging only |
| Database name | `itc475` | |
| Database user/password | `postgres` / `Admin123` (default) | Override via `DB_PASSWORD` environment variable before running `docker compose up` |
| Docker network | `enrollment-net` (custom bridge) | Provides DNS-based service discovery between containers; isolates from unrelated containers on a shared host |
| Data volume | `enrollment-pgdata` (named volume) | Persists across container restarts; `docker compose down -v` wipes it |

## Running the JUnit 5 test suite

### Automatically, during every build (already wired in)

The Docker build stage runs the fast unit test suite as a build gate — if any test fails, the image build fails and nothing deploys:

```
mvn -B clean package -DskipITs -Dgroups='!requiresDatabase'
```

This covers:
- **Service layer & search query logic**: `EnrollmentServiceTest` (keyword matching, blank-keyword handling)
- **Controller layer**: `EnrollmentControllerTest`, `ProfileControllerTest`

14 tests total, all Mockito-based (no live database required).

### Manually, on demand

```bash
cd student-enrollment-studio
mvn test              # unit tests only
mvn verify             # unit tests + jar packaging; integration tests skipped by default
```

`mvn test` prints results live in the terminal per test class, ending with a summary block:

```
[INFO] Results:
[INFO]
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

On a headless host (e.g. the Proxmox web console), if the output scrolls by too fast, capture it to a file while still watching it live:

```bash
mvn test | tee test-results.log
grep -A5 "Results:" test-results.log      # pull just the summary back out anytime
```

For per-test detail (including stack traces on any failure), Maven writes plain-text reports to `target/surefire-reports/` after the run:

```bash
cat target/surefire-reports/*.txt
```

### Full suite, including live-database integration tests

`EnrollmentMapperIT` (MyBatis data access against a live database) and `ProfileApplicationIT` (full application context + live database) use Testcontainers to spin up a disposable PostgreSQL container per test run. They're skipped by default (`skip.integration.tests=true` in `pom.xml`) because Testcontainers' Docker client library doesn't reliably negotiate with every Docker Desktop version on Windows. On a native Linux Docker host (e.g. the Proxmox VM), they're more likely to work correctly:

```bash
mvn clean verify -Dskip.integration.tests=false | tee verify-results.log
cat target/failsafe-reports/*.txt
```

Run this as yourself, not with `sudo` — Testcontainers needs to reach the Docker socket as your user, which is exactly why adding yourself to the `docker` group matters (see Installation, Option B). If you get a permission error reaching Docker, that group membership hasn't taken effect yet — log out/back in, or run `newgrp docker` first.

This exercises all four required coverage areas: service layer, MyBatis data access, search query logic, and live database connectivity.

## Verifying a deployment

```bash
docker compose ps                                    # both containers should show "Up (healthy)"
curl http://localhost:8081/actuator/health            # expect {"status":"UP"}
```

## End-to-end testing

### With browser access to the host

Open `http://<host>:8081/` (or `http://localhost:8081/` if testing locally) and search by student name or course name (e.g. a name/term you know exists in `db/02_insert_data.sql`). Matching results appearing on `http://<host>:8081/enrollments` confirms the full stack: controller → service → MyBatis → PostgreSQL.

### Testing without external network access

If the deployment host is on a network you can't reach directly from your laptop (no VPN, different subnet — common with university-managed infrastructure), run the same verification via `curl` directly on the host itself (e.g. through the Proxmox web console's terminal):

```bash
# See what data exists
curl -s "http://localhost:8081/enrollments" | grep -o '<td>[^<]*</td>' | head -20

# Search for a known value (URL-encode multi-word terms with -G --data-urlencode)
curl -s -G "http://localhost:8081/" --data-urlencode "keyword=Alice Johnson" | grep -c "Alice Johnson"

# Confirm HTTP status without scrolling through the full response
curl -s -o /dev/null -w "%{http_code}\n" -G "http://localhost:8081/" --data-urlencode "keyword=Alice Johnson"
```

A `200` status and a non-zero grep count confirms the search worked end-to-end, equivalent to what you'd see in a browser.

## Shutdown / reset

```bash
docker compose down          # stop containers, keep database data
docker compose down -v       # stop containers, wipe database data (full reset)
```

## Troubleshooting

| Symptom | Cause / Fix |
|---|---|
| Postgres container fails to start after first `up` | Postgres 18+ images require the data volume mounted at `/var/lib/postgresql`, not `/var/lib/postgresql/data`. Already configured correctly in `docker-compose.yml`; if you changed it, run `docker compose down -v` and fix the mount path. |
| App container fails to bind its port | Another service is already using the host port. Change the left-hand side of the app's port mapping in `docker-compo