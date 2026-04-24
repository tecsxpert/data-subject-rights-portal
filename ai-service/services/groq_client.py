import os
import requests
import time
import logging
from dotenv import load_dotenv

load_dotenv()

logging.basicConfig(level=logging.INFO)

class GroqClient:
    def __init__(self):
        self.api_key = os.getenv("GROQ_API_KEY")
        self.url = "https://api.groq.com/openai/v1/chat/completions"
        self.headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }

    def generate(self, prompt, retries=3):
        data = {
            "model": "llama-3.3-70b-versatile",
            "messages": [
                {"role": "user", "content": prompt}
            ]
        }

        for attempt in range(retries):
            try:
                response = requests.post(
                    self.url,
                    headers=self.headers,
                    json=data,
                    timeout=10
                )

                if response.status_code == 200:
                    result = response.json()
                    return result["choices"][0]["message"]["content"]

                else:
                    logging.error(f"Error {response.status_code}: {response.text}")

            except Exception as e:
                logging.error(f"Attempt {attempt+1} failed: {str(e)}")
                time.sleep(2 ** attempt)  # backoff: 1s → 2s → 4s

        return "AI service unavailable (fallback)"