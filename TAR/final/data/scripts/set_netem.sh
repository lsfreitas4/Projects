#!/bin/bash
sudo tc qdisc del dev enp7s0 root 2>/dev/null
if [ -n "$1" ]; then
    sudo tc qdisc add dev enp7s0 root netem $@
    echo "Condição aplicada: netem $@"
else
    echo "Netem removido (baseline)"
fi
tc qdisc show dev enp7s0
