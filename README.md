# Tool-48 — Data Subject Rights Portal

> A production-grade, AI-powered web application for managing GDPR/DPDP Data Subject Rights requests.
> Built as a 5-member team capstone project over a 20-day sprint.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Browser                             │
│                  React 18 + Vite + Tailwind                 │
│                        Port 80                              │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTPS / Axios
                      ▼
┌─────────────────────────────────────────────────────────────┐
│            Spring Boot 3.x  (Java 17)                       │
│  REST API · JWT Auth · Redis Cache · Flyway · Swagger       │
│                        Port 8080                            │
└────────────┬─────────────────────────┬──────────────────────┘
             │                         │
             ▼                         ▼
  ┌──────────────────┐    ┌────────────────────────┐
  │   PostgreSQL 15  │    │   Flask AI Service      │
  │   Primary DB     │    │   (Python 3.11 / Groq)  │
  │   Port 5432      │    │   Port 5000             │
  └──────────────────┘    └────────────────────────┘
             │
             ▼
  ┌──────────────────┐
  │    Redis 7       │
  │    Cache Layer   │
  │    Port 6379     │
  └──────────────────┘
```

---

## Prerequisites

| Tool              | Version   | Install                              |
|-------------------|-----------|--------------------------------------|
| Docker            | 24+       | [docs.docker.com](https://docs.docker.com) |
| Docker Compose    | 2.x       | Bundled with Docker Desktop          |
| Git               | 2.x       | [git-scm.com](https://git-scm.com)   |

No Java, Node.js, or Python installation required — Docker handles everything.

---

## How to Run the Project (Step-by-Step Guide)

Follow these exact steps to start the complete DSR portal on your local machine.

### 1. Prerequisites
- Install **Docker Desktop** and ensure it is currently running.
- Clone the repository and open your terminal.

### 2. Configure Environment Variables
Copy the example environment file and add your AI API key:
```bash
cp .env.example .env
```
*(Open the `.env` file in your editor and add your free `GROQ_API_KEY` from console.groq.com)*

### 3. Build and Start the Application
Navigate to the root directory containing `docker-compose.yml` and run:
```bash
docker compose up --build -d
```
*(This command orchestrates 5 separate containers. It will download the necessary images, compile the Java backend, and start the React frontend, Postgres database, Redis cache, and Python AI service. The `-d` flag runs them in the background.)*

### 4. Verify Services
To check that all 5 containers are running properly, type:
```bash
docker ps
```

### 5. Stop the Application
When you are finished using the application, cleanly shut down the containers by running:
```bash
docker compose down
```

Access the app:
- **Frontend**: http://localhost
- **API Swagger**: http://localhost:8080/swagger-ui.html
- **AI Health**: http://localhost:5000/health

---

## Demo Credentials

| Username  | Password     | Role    |
|-----------|-------------|---------|
| `admin`   | `Admin@123`  | ADMIN   |
| `manager` | `Manager@123`| MANAGER |
| `analyst` | `Analyst@123`| USER    |

---

## Environment Variables (`.env`)

| Variable            | Description                        | Example                      |
|---------------------|------------------------------------|------------------------------|
| `DB_URL`            | PostgreSQL JDBC URL                | `jdbc:postgresql://db:5432/dsr` |
| `DB_USERNAME`       | Database username                  | `dsr_user`                   |
| `DB_PASSWORD`       | Database password                  | `strong_password`            |
| `JWT_SECRET`        | JWT signing secret (min 32 chars)  | `your-256-bit-secret`        |
| `JWT_EXPIRY_MS`     | Token expiry in ms                 | `86400000`                   |
| `REDIS_HOST`        | Redis hostname                     | `redis`                      |
| `REDIS_PORT`        | Redis port                         | `6379`                       |
| `GROQ_API_KEY`      | Groq API key (free tier)           | `gsk_...`                    |
| `MAIL_HOST`         | SMTP host                          | `smtp.gmail.com`             |
| `MAIL_PORT`         | SMTP port                          | `587`                        |
| `MAIL_USERNAME`     | SMTP username                      | `you@gmail.com`              |
| `MAIL_PASSWORD`     | SMTP app password                  | `xxxx xxxx xxxx xxxx`        |

---

## Project Structure

```
.
├── backend/          # Spring Boot 3 Java API
│   ├── src/main/java/com/internship/tool/
│   │   ├── controller/     REST endpoints
│   │   ├── service/        Business logic
│   │   ├── repository/     JPA queries
│   │   ├── entity/         JPA models
│   │   ├── config/         Security, Redis, Seeder
│   │   └── exception/      Custom exceptions
│   └── src/main/resources/
│       └── db/migration/   Flyway SQL files
│
├── ai-service/       # Flask Python microservice (Port 5000)
│   ├── routes/       API endpoints
│   ├── services/     Groq client
│   ├── prompts/      Prompt templates
│   └── app.py
│
├── frontend/         # React 18 + Vite (Port 80)
│   └── src/
│       ├── pages/    LoginPage, Dashboard, List, Detail, Form
│       ├── components/
│       ├── services/ api.js (Axios)
│       └── context/  AuthContext
│
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## Key Features

- ✅ Full CRUD for DSR requests with soft-delete
- ✅ JWT authentication with role-based access (ADMIN / MANAGER / USER)
- ✅ AI-powered description, recommendations, and report generation (Groq LLaMA-3.3-70b)
- ✅ Redis caching (10 min TTL on GETs, auto-eviction on writes)
- ✅ Full-text search + multi-field filtering
- ✅ CSV export with UTF-8 BOM for Excel compatibility
- ✅ File upload with type and size validation
- ✅ Email notifications via JavaMailSender
- ✅ Audit logging via Spring AOP
- ✅ Swagger UI with all endpoints documented
- ✅ 30 realistic seeded demo records on first startup
- ✅ Responsive design (375px → 1280px+)

---

## Running Tests

```bash
# Java unit + integration tests
cd backend && mvn test

# Python AI service tests
cd ai-service && pytest

# Coverage report
cd backend && mvn jacoco:report
open target/site/jacoco/index.html
```

---

## Demo Day Checklist

- [ ] `docker-compose down -v && docker-compose up --build` — fresh seeded state
- [ ] Login as `admin` — show dashboard KPIs
- [ ] Create a new REQUEST live — watch AI description appear
- [ ] Click AI Recommend — read aloud
- [ ] Click Generate Report
- [ ] Show search/filter with `status=PENDING`
- [ ] Export CSV — download opens
- [ ] Show `DELETE /api/v1/dsr/1` without JWT → 401
- [ ] Reference `SECURITY.md`

---

## Team

| Role              | Responsibilities                                              |
|-------------------|---------------------------------------------------------------|
| **Java Developer 1**  | **Spring Boot, Service layer, JWT, Docker Compose, README**       |
| Java Developer 2  | DB schema, Repository, React frontend, Email, Tests, Seeder |
| AI Developer 1    | Flask, /describe, /recommend, AiServiceClient                 |
| AI Developer 2    | Groq client, /generate-report, security review                |
| Security Reviewer | OWASP ZAP, SECURITY.md, penetration testing                   |

---

*Capstone Sprint: 14 April – 9 May 2026 | Demo Day: 9 May 2026*
