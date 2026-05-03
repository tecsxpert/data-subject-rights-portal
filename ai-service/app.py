import os
import json
from datetime import datetime
from flask import Flask, request, jsonify
from flask_cors import CORS
import requests
from dotenv import load_dotenv

load_dotenv()

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
    data = request.get_json()
    
    # 1. Validate Input
    if not data or 'text' not in data:
        return jsonify({"error": "Missing 'text' field"}), 400
    
    try:
        # 2. Call AI Logic
        ai_response = get_ai_description(data['text'])
        
        # 3. Add generated_at timestamp
        ai_response['generated_at'] = datetime.utcnow().isoformat() + "Z"
        
        return jsonify(ai_response), 200
        
    except Exception as e:
        # Day 9 requirement: return fallback if AI fails
        return jsonify({
            "error": "AI service unavailable",
            "is_fallback": True,
            "generated_at": datetime.utcnow().isoformat() + "Z"
        }), 500
    
@app.route('/recommend', methods=['POST'])
def recommend_actions():
    data = request.get_json()
    if not data or 'text' not in data:
        return jsonify({"error": "Missing 'text' field"}), 400
    
    try:
        recommendations = get_ai_recommendations(data['text'])
        # Ensure we only return 3 items
        return jsonify(recommendations[:3]), 200
        
    except Exception as e:
        return jsonify([
            {"action_type": "Identity Verification", "description": "Verify the identity of the requester manually.", "priority": "High"},
            {"action_type": "Legal Review", "description": "Consult the legal team regarding this request.", "priority": "Medium"},
            {"action_type": "Internal Log", "description": "Document this request in the manual audit log.", "priority": "Low"}
        ]), 200 # Return hardcoded fallback as per Day 9 standards early

@app.route('/generate-report', methods=['POST'])
def generate_report():
    data = request.get_json()
    if not data or 'text' not in data:
        return jsonify({"error": "Missing 'text' field"}), 400
    
    try:
        report = get_ai_report(data['text'])
        return jsonify(report), 200
    except Exception as e:
        return jsonify({"error": "Failed to generate report"}), 500
    
@app.route('/health', methods=['GET'])
def health():
    return {"status": "up", "message": "AI service is running"}, 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)