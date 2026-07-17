import os
import sys
import time
import chromadb
from chromadb.utils import embedding_functions
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

# Automatically detects whatever folder you dropped this script into
CURRENT_DIR = os.getcwd()
# Creates a hidden folder for the database so it doesn't clutter your project
DB_PATH = os.path.join(CURRENT_DIR, ".local_ai_index")

# Initialize the database and offline AI model
client = chromadb.PersistentClient(path=DB_PATH)
brain = embedding_functions.SentenceTransformerEmbeddingFunction(model_name="all-MiniLM-L6-v2")
collection = client.get_or_create_collection(name="project_index", embedding_function=brain)

def index_file(filepath):
    # Add or remove file types you want to index here
    if not filepath.endswith(('.py', '.txt', '.js', '.html', '.json', '.md')):
        return
    try:
        with open(filepath, 'r', encoding='utf-8') as file:
            code_text = file.read()
            collection.upsert(
                documents=[code_text],
                metadatas=[{"file_path": filepath}],
                ids=[filepath]
            )
            print(f"✅ Indexed: {os.path.basename(filepath)}")
    except Exception:
        pass # Silently skip files it can't read so your terminal stays clean

def initial_scan():
    print(f"\n🔍 Scanning your project folder: {CURRENT_DIR}")
    for root, dirs, files in os.walk(CURRENT_DIR):
        # Skips heavy folders so it doesn't index useless garbage
        if any(skip in root for skip in ['.git', 'venv', '__pycache__', '.local_ai_index', 'node_modules']):
            continue
        for file in files:
            index_file(os.path.join(root, file))
    print("✅ Project scan complete!")

# The watcher that looks for live saves
class Watcher(FileSystemEventHandler):
    def on_modified(self, event):
        if not event.is_directory: index_file(event.src_path)
    def on_created(self, event):
        if not event.is_directory: index_file(event.src_path)

def start_indexer():
    initial_scan()
    print("\n👀 The watcher is now running in the background...")
    print("If you save or change any file, I will instantly update the index.")
    print("Press Ctrl+C to stop.")
    
    observer = Observer()
    observer.schedule(Watcher(), CURRENT_DIR, recursive=True)
    observer.start()
    try:
        while True: time.sleep(1)
    except KeyboardInterrupt:
        print("\n🛑 Stopping watcher...")
        observer.stop()
    observer.join()

def start_search():
    print("\n🔍 Offline Code Search is ready! (Type 'exit' to quit)")
    while True:
        query = input("\nWhat are you looking for in this project? \n> ")
        if query.lower() == 'exit': break
        
        results = collection.query(query_texts=[query], n_results=3)
        print("\n" + "="*60)
        
        for i in range(len(results['documents'][0])):
            file_path = results['metadatas'][0][i]['file_path']
            code_snippet = results['documents'][0][i]
            
            # Print the relative path so it's easier to read
            relative_path = os.path.relpath(file_path, CURRENT_DIR)
            
            print(f"📂 File: {relative_path}")
            print("-" * 60)
            print(code_snippet[:400].strip() + "\n... [truncated] ...\n")
        print("="*60)

if __name__ == "__main__":
    print("\n🤖 ALL-IN-ONE LOCAL AI AGENT")
    print("1. Start the Indexer & Live Watcher")
    print("2. Search the Codebase")
    
    choice = input("\nChoose an option (1 or 2): ")
    
    if choice == '1':
        start_indexer()
    elif choice == '2':
        start_search()
    else:
        print("Invalid choice. Please run the script again.")