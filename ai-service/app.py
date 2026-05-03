import os
import json
from datetime import datetime
from flask import Flask, request, jsonify
from flask_cors import CORS
import requests
from dotenv import load_dotenv
import time
import hashlib
import redis

load_dotenv()

cache = redis.Redis(host='localhost', port=6379, db=0, decode_responses=True)

START_TIME = time.time()
response_times = []

app = Flask(__name__)
CORS(app) # Necessary for the Java/React layers to talk to Flask

GROQ_API_KEY = os.getenv("GROQ_API_KEY")
MODEL = "llama-3.3-70b-versatile"

def get_ai_description(user_input):
    # 1. Loading Prompt Template
    with open("prompts/describe_rights.txt", "r") as f:
        template = f.read()
    
    full_prompt = template.replace("{user_input}", user_input)

    headers = {
        "Authorization": f"Bearer {GROQ_API_KEY}",
        "Content-Type": "application/json"
    }
    
    data = {
        "model": MODEL,
        "messages": [{"role": "user", "content": full_prompt}],
        "temperature": 0.3,
        "response_format": {"type": "json_object"} # Force JSON mode
    }

    response = requests.post("https://api.groq.com/openai/v1/chat/completions", headers=headers, json=data)
    response.raise_for_status()
    
    # Extracting the JSON string from AI response
    content = response.json()['choices'][0]['message']['content']
    return json.loads(content)

def get_ai_recommendations(user_input):
    with open("prompts/recommendations.txt", "r") as f:
        template = f.read()
    
    full_prompt = template.replace("{user_input}", user_input)

    headers = {
        "Authorization": f"Bearer {GROQ_API_KEY}",
        "Content-Type": "application/json"
    }
    
    data = {
        "model": MODEL,
        "messages": [{"role": "user", "content": full_prompt}],
        "temperature": 0.5, # Slightly higher for creative suggestions
        "response_format": {"type": "json_object"}
    }

    response = requests.post("https://api.groq.com/openai/v1/chat/completions", headers=headers, json=data)
    response.raise_for_status()
    
    # Extract the array from the JSON response
    content = json.loads(response.json()['choices'][0]['message']['content'])
    
    # Ensure it returns the list part if the AI wraps it in a key like "recommendations"
    if isinstance(content, dict):
        for key in content:
            if isinstance(content[key], list):
                return content[key]
    return content

def get_ai_report(user_input):
    with open("prompts/report_template.txt", "r") as f:
        template = f.read()
    
    full_prompt = template.replace("{user_input}", user_input)

    headers = {
        "Authorization": f"Bearer {GROQ_API_KEY}",
        "Content-Type": "application/json"
    }
    
    data = {
        "model": MODEL,
        "messages": [{"role": "user", "content": full_prompt}],
        "temperature": 0.4, # Balanced for structure and detail
        "response_format": {"type": "json_object"}
    }

    response = requests.post("https://api.groq.com/openai/v1/chat/completions", headers=headers, json=data)
    response.raise_for_status()
    return json.loads(response.json()['choices'][0]['message']['content'])

@app.route('/describe', methods=['POST'])
def describe_request():
    start = time.time()
    data = request.get_json()
    
    # 1. Validate Input
    if not data or 'text' not in data:
        response_times.append(time.time() - start)
        return jsonify({"error": "Missing 'text' field"}), 400
    
    # 1. Generate SHA256 Hash of the input text for the cache key
    text_input = data['text']
    cache_key = f"describe:{hashlib.sha256(text_input.encode()).hexdigest()}"
    
    try:
        # 2. Check if result is in Redis
        cached_result = cache.get(cache_key)
        if cached_result:
            ai_response = json.loads(cached_result)
        else:
            # 3. If not in cache, call AI Logic
            ai_response = get_ai_description(text_input)
            ai_response['generated_at'] = datetime.utcnow().isoformat() + "Z"
            
            # 4. Store in Redis with 15-minute TTL (900 seconds)
            cache.setex(cache_key, 900, json.dumps(ai_response))
        response_times.append(time.time() - start)
        return jsonify(ai_response), 200
        
    except Exception as e:
        response_times.append(time.time() - start)
        # Day 9 requirement: return fallback if AI fails
        return jsonify({
            "error": "AI service unavailable",
            "is_fallback": True,
            "generated_at": datetime.utcnow().isoformat() + "Z"
        }), 500
    
@app.route('/recommend', methods=['POST'])
def recommend_actions():
    start = time.time()
    data = request.get_json()
    
    if not data or 'text' not in data:
        response_times.append(time.time() - start)
        return jsonify({"error": "Missing 'text' field"}), 400
    
    text_input = data['text']
    # Create a specific key for recommendations to avoid collisions with /describe
    cache_key = f"recommend:{hashlib.sha256(text_input.encode()).hexdigest()}"

    try:
        # Check Redis
        cached_result = cache.get(cache_key)
        if cached_result:
            recommendations = json.loads(cached_result)
        else:
            # Call AI and cache the result for 15 minutes
            recommendations = get_ai_recommendations(text_input)
            cache.setex(cache_key, 900, json.dumps(recommendations))
        
        response_times.append(time.time() - start)
        return jsonify(recommendations[:3]), 200
        
    except Exception as e:
        response_times.append(time.time() - start)
        # Standard fallback if AI fails
        return jsonify([
            {"action_type": "Identity Verification", "description": "Verify the identity of the requester manually.", "priority": "High"},
            {"action_type": "Legal Review", "description": "Consult the legal team regarding this request.", "priority": "Medium"},
            {"action_type": "Internal Log", "description": "Document this request in the manual audit log.", "priority": "Low"}
        ]), 200

@app.route('/generate-report', methods=['POST'])
def generate_report():
    start = time.time()
    data = request.get_json()
    
    if not data or 'text' not in data:
        response_times.append(time.time() - start)
        return jsonify({"error": "Missing 'text' field"}), 400
    
    text_input = data['text']
    cache_key = f"report:{hashlib.sha256(text_input.encode()).hexdigest()}"

    try:
        cached_result = cache.get(cache_key)
        if cached_result:
            report = json.loads(cached_result)
        else:
            report = get_ai_report(text_input)
            cache.setex(cache_key, 900, json.dumps(report))
            
        response_times.append(time.time() - start)
        return jsonify(report), 200
    except Exception as e:
        response_times.append(time.time() - start)
        return jsonify({"error": "Failed to generate report"}), 500
    
@app.route('/health', methods=['GET'])
def health():
    avg_time = sum(response_times) / len(response_times) if response_times else 0
    uptime = time.time() - START_TIME
    
    return jsonify({
        "status": "up",
        "model": MODEL,
        "avg_response_time_sec": round(avg_time, 3),
        "uptime_sec": int(uptime),
        "request_count": len(response_times),
        "timestamp": datetime.utcnow().isoformat() + "Z"
    }), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)