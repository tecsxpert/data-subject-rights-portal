import os
import chromadb
from chromadb.utils import embedding_functions

# 1. Initialize the ChromaDB Persistent Client (saves data to a folder)
client = chromadb.PersistentClient(path="./chroma_db")

# 2. Use the same model you pre-loaded in app.py for consistency
# This model converts text into vectors that the AI can understand
embedding_model = embedding_functions.SentenceTransformerEmbeddingFunction(
    model_name="all-MiniLM-L6-v2"
)

# 3. Create or get the collection
collection = client.get_or_create_collection(
    name="dsr_knowledge", 
    embedding_function=embedding_model
)

def seed_knowledge_base():
    data_path = "./data"
    documents = []
    metadatas = []
    ids = []

    # 4. Loop through the files in your data folder
    for i, filename in enumerate(os.listdir(data_path)):
        if filename.endswith(".txt"):
            with open(os.path.join(data_path, filename), "r", encoding="utf-8") as file:
                content = file.read()
                documents.append(content)
                metadatas.append({"source": filename})
                ids.append(f"doc_{i}")

    # 5. Add documents to the vector database
    if documents:
        collection.add(
            documents=documents,
            metadatas=metadatas,
            ids=ids
        )
        print(f"Successfully seeded {len(documents)} documents into ChromaDB.")
    else:
        print("No documents found in the data/ folder.")

if __name__ == "__main__":
    seed_knowledge_base()