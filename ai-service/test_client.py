from services.groq_client import GroqClient

client = GroqClient()

response = client.generate("Explain AI in one line")

print(response)