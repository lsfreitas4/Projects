#!/bin/bash
CONDITION=$1
mkdir -p ~/results

LOG=~/results/mosaico_${CONDITION}.log
echo "=== Mosaico | Condição: $CONDITION ===" | tee "$LOG"

# Gera a lista de 50 URLs
URLS=$(for i in $(seq 1 50); do echo "https://server.test/file$i.dat"; done)

for PROTO in http1.1 http2 http3; do
    echo "--- $PROTO ---" | tee -a "$LOG"
    for i in {1..3}; do
        START=$(date +%s%3N)
        echo "$URLS" | xargs -P1 curl -k --${PROTO} -s -o /dev/null
        END=$(date +%s%3N)
        echo "  Run $i: $((END-START))ms" | tee -a "$LOG"
    done
done
echo "=== Concluído ===" | tee -a "$LOG"
