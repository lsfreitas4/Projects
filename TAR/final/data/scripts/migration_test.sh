#!/bin/bash
PROTOCOL=$1
LOG=~/results/migration_${PROTOCOL}_$(date +%s).log
mkdir -p ~/results

echo "=== Teste de migração: $PROTOCOL ===" | tee "$LOG"
echo "Hora início: $(date)" | tee -a "$LOG"

curl -k --${PROTOCOL} \
  --max-time 120 \
  -o /dev/null \
  -w "Tempo total: %{time_total}s | HTTP: %{http_code}\n" \
  https://server.test/bigfile.bin >> "$LOG" 2>&1 &

CURL_PID=$!

sleep 10

echo "$(date): A simular queda de WiFi (enp7s0 down)..." | tee -a "$LOG"
sudo ip route add 192.168.56.101 via 192.168.57.101 dev enp8s0 2>/dev/null
sudo ip link set enp7s0 down
echo "Interface ativa agora: enp8s0 (192.168.57.102)" | tee -a "$LOG"

wait $CURL_PID
EXIT=$?
echo "Exit code: $EXIT" | tee -a "$LOG"
echo "Hora fim: $(date)" | tee -a "$LOG"
echo "=== Concluído ===" | tee -a "$LOG"

sudo ip link set enp7s0 up
sudo ip route del 192.168.56.101 via 192.168.57.101 dev enp8s0 2>/dev/null
sleep 2
