from flask import Flask

app = Flask(__name__)

@app.route('/health', methods=['GET'])
def health():
    return {"status": "up", "message": "AI service is running"}, 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)