@echo off
echo ==================================================
echo A limpar e a iniciar o Cluster de Leiloes...
echo ==================================================
docker compose down -v
docker compose build
docker compose up -d

echo A abrir terminais individuais...
start "Miner 1" cmd /k "docker attach ledger-miner-1"
start "Miner 2" cmd /k "docker attach ledger-miner-2"

echo ==================================================
echo Operacao concluida com sucesso.
echo ==================================================