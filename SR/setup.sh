#!/bin/bash
set -e

echo "[*] A instalar dependências Python..."
pip3 install -r requirements.txt

DB_PATH="cowrie_iocs.db"

if [ ! -f "$DB_PATH" ]; then
  echo "[*] A criar base de dados inicial em $DB_PATH..."
  sqlite3 "$DB_PATH" "CREATE TABLE IF NOT EXISTS iocs (
      id INTEGER PRIMARY KEY,
      type TEXT,
      value TEXT UNIQUE,
      first_seen TEXT,
      last_seen TEXT,
      count INTEGER DEFAULT 1,
      source TEXT,
      note TEXT,
      sid INTEGER
  );"
  echo "[OK] Base de dados criada."
else
  echo "[*] Base de dados já existe, a manter."
fi

echo "[*] Pronto! Agora corre o parser e depois a dashboard."
