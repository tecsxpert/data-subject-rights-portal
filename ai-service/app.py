"""
Tool-48 — DSR Portal  |  AI Microservice  |  Port 5000
Flask app with Groq LLaMA-3.3-70b integration.
Falls back to stub responses when Groq is unavailable.
"""
import os
import time
import hashlib
from datetime import datetime, timezone
from typing import List, Dict, Optional

from flask import Flask, request, jsonify
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

# ── Groq client (optional — graceful degradation) ─────────────
try:
    from groq import Groq
    _groq_client = Groq(api_key=os.getenv("GROQ_API_KEY", ""))
    GROQ_AVAILABLE = bool(os.getenv("GROQ_API_KEY"))
except Exception:
    _groq_client = None
    GROQ_AVAILABLE = False

# ── Flask app ─────────────────────────────────────────────────
app = Flask(__name__)
app.config["JSON_SORT_KEYS"] = False

limiter = Limiter(
    get_remote_address,
    app=app,
    default_limits=["30 per minute"],
    storage_uri="memory://"
)

_start_time = time.time()
_request_times: list[float] = []

MODEL = "llama-3.3-70b-versatile"


# ── Helpers ───────────────────────────────────────────────────

def _sanitise(text: str, max_len: int = 2000) -> str:
    """Strip dangerous characters and truncate."""
    if not text:
        return ""
    # Reject obvious prompt-injection attempts
    injection_markers = ["ignore previous", "ignore all", "system:", "###"]
    lower = text.lower()
    for marker in injection_markers:
        if marker in lower:
            return "[INPUT SANITISED]"
    return text[:max_len]


def call_groq(messages: List[Dict], temperature: float = 0.4) -> Optional[str]:
    """Call Groq with 3 retries and exponential back-off."""
    if not GROQ_AVAILABLE or _groq_client is None:
        return None

    for attempt in range(3):
        try:
            t0 = time.time()

            resp = _groq_client.chat.completions.create(
                model=MODEL,
                messages=messages,
                temperature=temperature,
                max_tokens=1024,
            )

            _request_times.append(time.time() - t0)

            if len(_request_times) > 100:
                _request_times.pop(0)

            return resp.choices[0].message.content

        except Exception as exc:
            app.logger.warning(
                "Groq attempt %d failed: %s",
                attempt + 1,
                exc
            )

            time.sleep(2 ** attempt)

    return None


# ── Routes ────────────────────────────────────────────────────

@app.get("/health")
def health():
    avg_ms = (
        round(sum(_request_times) / len(_request_times) * 1000, 1)
        if _request_times else 0
    )
    return jsonify({
        "status":       "ok",
        "model":        MODEL if GROQ_AVAILABLE else "stub",
        "groq_enabled": GROQ_AVAILABLE,
        "uptime_s":     round(time.time() - _start_time, 1),
        "avg_response_ms": avg_ms,
    })


@app.post("/describe")
def describe():
    data = request.get_json(silent=True) or {}
    desc = _sanitise(data.get("description", ""))
    req_type = _sanitise(data.get("request_type", "UNKNOWN"), 50)

    content = call_groq([
        {"role": "system", "content": (
            "You are a GDPR/data-protection compliance officer. "
            "Summarise the data subject request in 2-3 clear sentences. "
            "Be factual and professional. Return plain text only."
        )},
        {"role": "user", "content": (
            f"Request type: {req_type}\n"
            f"Description: {desc}\n\n"
            "Provide a concise professional summary of this request."
        )},
    ], temperature=0.3)

    if content:
        return jsonify({
            "summary":      content.strip(),
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "is_fallback":  False,
        })

    return jsonify({
        "summary":      f"[{req_type}] {desc[:120]}..." if len(desc) > 120 else f"[{req_type}] {desc}",
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "is_fallback":  True,
    })


@app.post("/recommend")
def recommend():
    data = request.get_json(silent=True) or {}
    desc = _sanitise(data.get("description", ""))
    req_type = _sanitise(data.get("request_type", "UNKNOWN"), 50)

    content = call_groq([
        {"role": "system", "content": (
            "You are a GDPR compliance expert. "
            "Return exactly 3 action recommendations as a JSON array. "
            "Each item: {\"action_type\": string, \"description\": string, \"priority\": \"HIGH\"|\"MEDIUM\"|\"LOW\"}. "
            "Return ONLY the JSON array, no other text."
        )},
        {"role": "user", "content": (
            f"Request type: {req_type}\nDescription: {desc}\n\n"
            "Provide 3 prioritised recommendations."
        )},
    ], temperature=0.5)

    if content:
        import json, re
        try:
            match = re.search(r'\[.*\]', content, re.DOTALL)
            if match:
                return jsonify(json.loads(match.group()))
        except Exception:
            pass

    return jsonify([
        {"action_type": "ACKNOWLEDGE",  "description": f"Acknowledge receipt of the {req_type} request within 72 hours.", "priority": "HIGH"},
        {"action_type": "VERIFY",       "description": "Verify the identity of the data subject before processing.", "priority": "HIGH"},
        {"action_type": "PROCESS",      "description": f"Process the {req_type} request within the statutory 30-day window.", "priority": "MEDIUM"},
    ])


@app.post("/generate-report")
def generate_report():
    data = request.get_json(silent=True) or {}
    dsr_id = data.get("dsr_id", "N/A")
    desc = _sanitise(data.get("description", ""))
    req_type = _sanitise(data.get("request_type", "UNKNOWN"), 50)

    content = call_groq([
        {"role": "system", "content": (
            "You are a data protection compliance officer writing a formal report. "
            "Return a JSON object with keys: title, summary, overview, key_findings (array of strings), recommendations (array of strings). "
            "Return ONLY the JSON object."
        )},
        {"role": "user", "content": (
            f"DSR ID: {dsr_id}\nRequest type: {req_type}\nDescription: {desc}\n\n"
            "Generate a formal compliance report."
        )},
    ], temperature=0.4)

    if content:
        import json, re
        try:
            match = re.search(r'\{.*\}', content, re.DOTALL)
            if match:
                report = json.loads(match.group())
                report["is_fallback"] = False
                return jsonify(report)
        except Exception:
            pass

    return jsonify({
        "title":            f"DSR Report — {req_type} (ID: {dsr_id})",
        "summary":          f"Formal report for {req_type} request. Manual review required.",
        "overview":         desc[:300] if desc else "No description provided.",
        "key_findings":     ["Request received and logged.", "Identity verification required."],
        "recommendations":  ["Complete within 30-day statutory period.", "Maintain audit trail."],
        "is_fallback":      True,
    })


# ── Entry point ───────────────────────────────────────────────
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)
