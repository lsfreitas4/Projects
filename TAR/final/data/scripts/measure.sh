#!/bin/bash
CONDITION=$1
mkdir -p ~/results

LOG=~/results/impairment_${CONDITION}.log
echo "=== Condição: $CONDITION ===" | tee "$LOG"
echo "Hora: $(date)" | tee -a "$LOG"

for PROTO in http1.1 http2 http3; do
    echo "--- $PROTO ---" | tee -a "$LOG"
    for i in {1..5}; do
        TIME=$(curl -k --${PROTO} -o /dev/null -s \
          -w "%{time_starttransfer} %{time_total}" \
          https://server.test/)
        echo "  Run $i: TTFB=$(echo $TIME | cut -d' ' -f1)s  Total=$(echo $TIME | cut -d' ' -f2)s" | tee -a "$LOG"
    done
done
echo "=== Concluído ===" | tee -a "$LOG"
