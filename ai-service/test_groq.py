import os
import requests
from dotenv import load_dotenv

load_dotenv()

GROQ_API_KEY = os.getenv("GROQ_API_KEY")
MODEL = "llama-3.3-70b-versatile"

def test_prompt(user_input):
    # Loading prompt template
    with open("prompts/describe_rights.txt", "r") as f:
        template = f.read()
    
    # Injecting the user input into the template
    full_prompt = template.replace("{user_input}", user_input)

    headers = {
        "Authorization": f"Bearer {GROQ_API_KEY}",
        "Content-Type": "application/json"
    }
    
    data = {
        "model": MODEL,
        "messages": [{"role": "user", "content": full_prompt}],
        "temperature": 0.3 # Low temperature for factual JSON
    }

    response = requests.post("https://api.groq.com/openai/v1/chat/completions", headers=headers, json=data)
    return response.json()['choices'][0]['message']['content']

# Day 2 Task: Test with 5 real inputs
test_inputs = [
    "I want you to delete all my personal data immediately.",
    "Can I get a copy of my chat history and profile info in a CSV format?",
    "I noticed my birthdate is wrong in my settings, please fix it to July 10th.",
    "I want to see exactly what information your company has collected about me.",
    "Please stop using my data for marketing purposes, but keep my account active."
]

for i, user_text in enumerate(test_inputs, 1):
    print(f"--- Test {i} ---")
    print(f"Input: {user_text}")
    print(f"Output: {test_prompt(user_text)}\n")
