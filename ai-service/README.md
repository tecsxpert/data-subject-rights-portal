# AI Service - Data Subject Rights (DSR) Portal

This microservice provides the intelligent backend for the Data Subject Rights Portal. It uses Generative AI to categorize user requests, recommend compliance actions, and generate summary reports.

## 🚀 Overview

The AI service is built with **Flask** and integrated with **Groq Cloud API** (`llama-3.3-70b-versatile`) for high-speed inference. It includes a **Redis** caching layer to ensure repeat requests are served under 2 seconds.

## 🛠️ Tech Stack

* **Framework:** Flask
* **AI Model:** Llama 3.3 70B (via Groq)
* **Cache:** Redis
* **Security:** OWASP ZAP Hardened (Security Headers)
* **Environment:** Python 3.13+

## ⚙️ Setup & Installation

### 1. Clone the Repository & Navigate

```powershell
cd ai-service
```

### 2. Set Up Environment Variables

Create a `.env` file in the `ai-service` root:

```
GROQ_API_KEY=your_actual_groq_api_key
MODEL_NAME=llama-3.3-70b-versatile
REDIS_HOST=localhost
REDIS_PORT=6379
```

### 3. Install Dependencies

```powershell
pip install -r requirements.txt
```

### 4. Start the Service

```powershell
python app.py
```

The service will run on:
👉 http://localhost:5000

---

## 📡 API Documentation

### 1. POST /describe

Analyzes raw request text to provide a structured summary.

**Request Body:**

```json
{
  "text": "I want to delete my account data."
}
```

**Validation:** Max 5000 characters

---

### 2. POST /recommend

Generates a list of 3 prioritized compliance actions.

**Request Body:**

```json
{
  "text": "I want to delete my account data."
}
```

---

### 3. POST /generate-report

Creates a final summary for internal auditing.

**Request Body:**

```json
{
  "text": "..."
}
```

---

### 4. GET /health

Monitoring endpoint for uptime and performance.

**Sample Output:**

```json
{
  "status": "up",
  "avg_response_time_sec": 1.25,
  "model": "llama-3.3-70b-versatile",
  "uptime_sec": 3600
}
```

---

## 🛡️ Security & Resilience

* **Security Headers:**
  Implemented `X-Frame-Options`, `X-Content-Type-Options`, and `Content-Security-Policy` to mitigate OWASP risks.

* **Graceful Fallback:**
  If the AI provider is unavailable, the service returns a structured fallback response with `"is_fallback": true`.

* **Caching:**
  SHA256-based caching in Redis prevents redundant API calls and reduces costs.

---

## 👥 Contributors

* **AI Developer 1:** Jeevitha Divakar

---

