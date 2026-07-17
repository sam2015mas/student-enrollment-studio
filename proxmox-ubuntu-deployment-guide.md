# Student Enrollment Studio — Proxmox VE / Ubuntu 22.04 Deployment Guide

Target: Ubuntu 22.04.4 LTS (Jammy) VM on Proxmox VE, IP `192.168.4.19`, non-admin user (sudo required for every privileged command), app on host port `8081` (8080 is taken by `mwccdc_c`), no PostgreSQL installed on the host.

## 1. What actually needs to change for Ubuntu

Short answer: almost nothing in the application/Docker files themselves.

The `Dockerfile` already builds from Linux base images (`maven:3.9-eclipse-temurin-21` for the build stage, `eclipse-temurin:21-jre-jammy` for the runtime stage). Docker containers are always Linux under the hood — even when they ran on your Windows machine, Docker Desktop was running them inside a Linux VM (WSL2). So the exact same image builds and runs identically on your Proxmox Ubuntu VM. `docker-compose.yml`'s named volumes and bridge network are also OS-agnostic.

The only real changes are host/environment-level, already reflected in the package:

- App's host port mapping changed to `8081:8080` (container still listens on 8080 internally; only the host-side mapping changes) to avoid the conflict with `mwccdc_c` on 8080.
- PostgreSQL is containerized (`postgres:18` image), so the host never needs PostgreSQL installed — this already satisfies that constraint.
- Your host's OpenJDK 25.0.3 / Maven 3.6.3 are **not used** to build the application — the Docker build stage pulls its own pinned Maven 3.9 + JDK 21 image and builds inside the container, completely isolated from whatever is installed on the host. You only need Docker Engine on the Proxmox VM for the deployment path.
- All commands below are prefixed with `sudo` since you're a non-admin user, per your setup.

## 2. Docker networking: custom bridge (already configured)

`docker-compose.yml` defines a **user-defined bridge network** (`enrollment-net`, driver `bridge`) that both the `app` and `postgres` containers join. This is the right choice for a single-VM, two-container deployment, and here's why, compared to the alternatives:

- **Default bridge network** — Docker's built-in default network does *not* provide automatic DNS resolution between containers; you'd have to hardcode container IPs or use deprecated `--link` flags. A user-defined bridge gives every container on it automatic DNS-based service discovery by container/service name, which is exactly how the app finds the database (`SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/itc475` — `postgres` resolves automatically).
- **Host networking** — would put both containers directly on the VM's network stack with no isolation, defeating the purpose of containerization and creating exactly the kind of port collisions you already have with `mwccdc_c`.
- **Overlay networking** — only relevant for multi-host Docker Swarm/Kubernetes clusters. You have a single VM, so overlay adds complexity with no benefit.
- A **custom** bridge (vs. the default one) also isolates this app's containers from any other unrelated containers that might run on the same Proxmox VM later, which matters on a shared host.

## 3. Step-by-step deployment

### 3.1 Install Docker Engine + Compose plugin on the Ubuntu VM

SSH into the VM, then check if Docker is already present:

```bash
ssh <your-user>@192.168.4.19
docker --version
```

If that fails (not installed), install Docker from the official repository:

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg

sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

Verify:

```bash
sudo docker --version
sudo docker compose version
```

(Optional but recommended) add yourself to the `docker` group so you don't need `sudo` for every Docker command, and so `mvn` (run as your normal user, not root) can reach the Docker socket for the optional integration-test step in section 5:

```bash
sudo usermod -aG docker $USER
```

Log out and back in (or run `newgrp docker`) for the group change to take effect.

### 3.2 Transfer the package to the VM

From your Windows machine, using the included `student-enrollment-studio-proxmox.tar.gz`:

```powershell
scp student-enrollment-studio-proxmox.tar.gz <your-user>@192.168.4.19:~/
```

(Windows 10/11 ships OpenSSH's `scp` by default — this runs directly in PowerShell.)

### 3.3 Extract and build on the VM

```bash
tar -xzf student-enrollment-studio-proxmox.tar.gz
cd student-enrollment-studio
sudo docker compose up --build -d
```

This builds the app image (JDK 21 Maven build stage, running the fast unit-test gate — see section 5), starts `postgres:18` seeded from `db/*.sql`, and starts the app once Postgres reports healthy, all on the `enrollment-net` bridge network. First run takes a few minutes to pull base images; subsequent runs are cached and fast.

### 3.4 Verify

```bash
sudo docker compose ps
```

Both `enrollment-db` and `enrollment-app` should show `Up (healthy)`.

```bash
sudo docker compose logs app --tail=50
curl http://localhost:8081/actuator/health
```

Expect `{"status":"UP"}`.

### 3.5 Open the firewall port (if `ufw` is active)

```bash
sudo ufw status
```

If it shows `active`:

```bash
sudo ufw allow 8081/tcp
```

### 3.6 Full end-to-end test from another machine

From your Windows machine (or any machine on the same network as the Proxmox VM):

```powershell
curl http://192.168.4.19:8081/actuator/health
```

Then open `http://192.168.4.19:8081/` in a browser and run a search (e.g. a student name or course name like "Database Systems"). A result rendering correctly confirms the full stack live on Proxmox: controller → service → MyBatis mapper → PostgreSQL, over the container network, reachable from outside the VM.

## 4. Automated JUnit 5 testing during the Maven build

This requirement is already built into the deployment path — no extra step needed. Stage 1 of the `Dockerfile` runs:

```
mvn -B clean package -DskipITs -Dgroups='!requiresDatabase'
```

as part of `docker compose up --build`. If any of the 14 unit tests fail (`EnrollmentServiceTest` — service layer + search query logic, `EnrollmentControllerTest` / `ProfileControllerTest` — controller layer), the image build fails and the deployment stops before anything runs. This is your "validate code quality during Maven builds" gate, and it already ran successfully in your Windows testing (`Tests run: 14, Failures: 0, Errors: 0`).

### Optional: full suite including live-database integration tests

`EnrollmentMapperIT` (MyBatis data access, live DB) and `ProfileApplicationIT` (full application context + live DB) use Testcontainers and are skipped by default (`skip.integration.tests=true` in `pom.xml`) because they couldn't reliably reach Docker Desktop's API from Windows. On the Proxmox Ubuntu VM, Testcontainers talks to a native Linux Docker Engine over the standard `/var/run/docker.sock` — the same setup Testcontainers is built and tested against — so this should work cleanly here where it didn't on Windows.

To run the full suite, including these live-DB tests, directly on the VM (after the docker group step in 3.1, using your normal user — not `sudo` — so `mvn` can reach the Docker socket):

```bash
cd student-enrollment-studio
mvn clean verify -Dskip.integration.tests=false
```

This exercises all four required coverage areas: service layer (`EnrollmentServiceTest`), MyBatis data access (`EnrollmentMapperIT`), search query logic (`EnrollmentServiceTest`, controller tests), and live database connectivity (`EnrollmentMapperIT`, `ProfileApplicationIT`). Your host's Maven 3.6.3 / JDK 25.0.3 are sufficient for this — Spring Boot 3.3.5 requires Maven 3.6.3+ as a minimum, and the `pom.xml` already includes the `-Dnet.bytebuddy.experimental=true` fix needed for Mockito to work on newer JDKs.

## 5. Shutdown / reset

```bash
sudo docker compose down          # stop containers, keep data
sudo docker compose down -v       # stop containers, wipe Postgres data (full reset)
```

## Troubleshooting

| Symptom | Fix |
|---|---|
| `docker compose up` fails with permission denied on the socket | You're not in the `docker` group yet, or haven't re-logged in after `usermod -aG docker`. Use `sudo docker compose ...` in the meantime. |
| Port 8081 unreachable from another machine | Check `sudo ufw status` and allow `8081/tcp`; also confirm the Proxmox VM's virtual NIC/firewall (if any, at the Proxmox host level) allows inbound traffic on 8081. |
| `enrollment-app` restarts repeatedly | `sudo docker compose logs app` — usually a datasource config issue; `depends_on: condition: service_healthy` should prevent starting before Postgres is ready. |
| `mvn clean verify -Dskip.integration.tests=false` still can't reach Docker | Confirm `docker version` works as your normal user (not just via sudo) — if not, re-check the `usermod -aG docker` + re-login step. |
