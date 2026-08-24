#!/usr/bin/env python3
from flask import Flask, request
import sqlite3
import logging
from datetime import datetime
from pathlib import Path

app = Flask(__name__)

# Configurar logging
logging.basicConfig(
    filename='webhp.log',
    level=logging.INFO,
    format='%(asctime)s - %(message)s'
)

BASE_DIR = Path(__file__).resolve().parent.parent 
DB_PATH = BASE_DIR / "cowrie_iocs.db"

def upsert_ioc(ioc_type, value, source="webhp", note=None):
    try:
        conn = sqlite3.connect(DB_PATH)
        c = conn.cursor()
        now = datetime.utcnow().isoformat()

        # Garante que a tabela existe
        c.execute("""
            CREATE TABLE IF NOT EXISTS iocs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                value TEXT NOT NULL,
                first_seen TEXT NOT NULL,
                last_seen TEXT NOT NULL,
                count INTEGER DEFAULT 1,
                source TEXT NOT NULL,
                note TEXT,
                sid INTEGER,
                severity TEXT DEFAULT 'MEDIUM',
                UNIQUE(type, value)
            )
        """)

        # Tentar inserir
        c.execute("SELECT count FROM iocs WHERE value=? AND type=?", (value, ioc_type))
        row = c.fetchone()
        
        if row:
            # Atualizar existente
            c.execute("""
                UPDATE iocs
                SET last_seen = ?, count = count + 1, note = ?
                WHERE value = ? AND type = ?
            """, (now, note, value, ioc_type))
        else:
            # Inserir novo
            c.execute("""
                INSERT INTO iocs(type, value, first_seen, last_seen, count, source, note)
                VALUES (?,?,?,?,1,?,?)
            """, (ioc_type, value, now, now, source, note))
        
        conn.commit()
        conn.close()
        return True
        
    except Exception as e:
        logging.error(f"Database error: {e}")
        return False

@app.route("/", defaults={"path": ""}, methods=["GET","POST","PUT","DELETE","PATCH","OPTIONS"])
@app.route("/<path:path>", methods=["GET","POST","PUT","DELETE","PATCH","OPTIONS"])
def catch_all(path):
    attacker_ip = request.remote_addr or "unknown"
    full_path = "/" + path if path else "/"
    method = request.method
    ua = request.headers.get("User-Agent", "unknown")
    
    # Log detalhado
    log_msg = f"[{method}] {attacker_ip} → {full_path} | UA: {ua}"
    logging.info(log_msg)
    print(f"[CAPTURE] {log_msg}")
    
    # Capturar POST data (se existir)
    post_data = ""
    if method == "POST" and request.data:
        post_data = request.data.decode('utf-8', errors='ignore')[:200]
        logging.info(f"POST DATA: {post_data}")
    
    # Guardar IoCs
    upsert_ioc("ip", attacker_ip, source="webhp", note=f"HTTP {method} to {full_path}")
    upsert_ioc("url", full_path, source="webhp", note=f"method={method}, ua={ua[:50]}")
    
    # Resposta fake (simula servidor real)
    fake_responses = {
        "/admin": "<html><body><h1>Admin Panel</h1><form action='/login' method='POST'><input name='user'><input type='password' name='pass'><button>Login</button></form></body></html>",
        "/login": "<html><body><p>Invalid credentials</p></body></html>",
        "/.env": "APP_KEY=base64:fake_key_here\nDB_PASSWORD=secret123",
        "/wp-admin": "<html><body><h1>WordPress Admin</h1></body></html>",
        "/.git/config": "[core]\nrepositoryformatversion = 0",
    }
    
    # Resposta padrão
    response_body = fake_responses.get(full_path, "<html><body><h1>It works!</h1></body></html>")
    
    return response_body, 200

if __name__ == "__main__":
    print("=" * 60)
    print("WebHP - HTTP Honeypot")
    print("=" * 60)
    print("Listening on: http://0.0.0.0:8080")
    print("Database: " + str(DB_PATH))
    print("Log file: webhp.log")
    print("=" * 60)
    app.run(host="0.0.0.0", port=8080, debug=False)