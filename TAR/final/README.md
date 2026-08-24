# Comparative Analysis of HTTP/1.1, HTTP/2, and HTTP/3

**Course:** Advanced Topics in Networks (TAR)  
**Institution:** FCUP  
**Authors:** Diogo Valverde (up202509119), Luís Freitas (up201905767), Pedro Mariano (up202509341)

---

## Repository Structure

```
final/
├── analyze_results.py          # Python script to generate graphs from logs
├── graficos/                   # Generated graphs and CSV summary
│   ├── ttfb_latency_impact.png
│   ├── ttfb_by_condition.png
│   ├── mosaico_by_condition.png
│   └── resultados_completos.csv
└── data/
    ├── fase1-evidencias/       # Phase 1: testbed setup evidence
    │   ├── Caddyfile               # Caddy server configuration (h1/h2/h3)
    │   ├── build-curl-http3.sh     # Script used to compile curl with HTTP/3
    │   ├── curl_version.txt        # Compiled curl version with ngtcp2/nghttp3
    │   ├── client_ip_config.txt    # Client network interface configuration
    │   ├── client_routes.txt       # Client routing table (initial)
    │   ├── client_routes_final.txt # Client routing table (with migration path)
    │   ├── client_enp8s0_config.txt# Second interface config (migration)
    │   ├── server_enp8s0_config.txt# Server second interface config (migration)
    │   ├── client_hosts.txt        # /etc/hosts showing server.test mapping
    │   ├── server-netplan.yaml     # Server network configuration
    │   ├── client-netplan.yaml     # Client network configuration
    │   ├── index.html              # Mosaico workload page (50 embedded files)
    │   ├── http1_test.txt          # Preliminary HTTP/1.1 test result
    │   ├── http2_test.txt          # Preliminary HTTP/2 test result
    │   └── http3_test.txt          # Preliminary HTTP/3 test result
    ├── impairment/             # TTFB measurements under network conditions
    │   ├── impairment_baseline.log
    │   ├── impairment_delay_20ms.log
    │   ├── impairment_delay_50ms.log
    │   ├── impairment_delay_100ms.log
    │   ├── impairment_delay_200ms.log
    │   ├── impairment_loss_01.log
    │   ├── impairment_loss_1.log
    │   ├── impairment_loss_2.log
    │   ├── impairment_loss_5.log
    │   └── impairment_delay100_loss2.log
    ├── mosaico/                # Multi-resource workload measurements (50 files)
    │   ├── mosaico_baseline.log
    │   ├── mosaico_delay_20ms.log
    │   ├── mosaico_delay_50ms.log
    │   ├── mosaico_delay_100ms.log
    │   ├── mosaico_delay_200ms.log
    │   ├── mosaico_loss_01.log
    │   ├── mosaico_loss_1.log
    │   ├── mosaico_loss_2.log
    │   ├── mosaico_loss_5.log
    │   └── mosaico_delay100_loss2.log
    ├── migration/              # Connection migration test results
    │   ├── migration_http1.1_final.log
    │   ├── migration_http2_final.log
    │   └── migration_http3_final.log
    ├── scripts/                # All measurement scripts
    │   ├── measure.sh              # TTFB and total time measurement script
    │   ├── measure_mosaico.sh      # Mosaico workload measurement script
    │   ├── migration_test.sh       # Connection migration test script
    │   ├── set_netem.sh            # Server-side network impairment control
    │   └── build-curl-http3.sh     # curl HTTP/3 compilation script
    └── statistical_analysis.html   # Interactive statistical summary
```

---

## Testbed

Two KVM/QEMU virtual machines running Ubuntu Server 24.04.4 LTS on the same physical host.

| Machine | Primary interface | Migration interface |
|---------|------------------|---------------------|
| Server  | enp7s0 — 192.168.56.101 | enp8s0 — 192.168.57.101 |
| Client  | enp7s0 — 192.168.56.102 | enp8s0 — 192.168.57.102 |

The server runs **Caddy** with `protocols h1 h2 h3` and `tls internal`.  
The client uses a custom **curl 8.19.0** compiled with `ngtcp2`, `nghttp3`, and OpenSSL QUIC support.

---

## How to Reproduce the Measurements

### 1. Apply a network condition on the server

```bash
# On the server VM
./set_netem.sh delay 100ms       # add 100ms one-way delay
./set_netem.sh loss 2%           # add 2% packet loss
./set_netem.sh delay 100ms loss 2%  # combined
./set_netem.sh                   # remove all impairments (baseline)
```

### 2. Run TTFB measurements on the client

```bash
# On the client VM
./measure.sh <condition_name>
# Example: ./measure.sh delay_100ms
```

### 3. Run mosaico measurements on the client

```bash
./measure_mosaico.sh <condition_name>
# Example: ./measure_mosaico.sh loss_5
```

### 4. Run connection migration test

```bash
./migration_test.sh http1.1
./migration_test.sh http2
./migration_test.sh http3
```

### 5. Generate graphs

```bash
# On the host machine
pip install matplotlib numpy pandas
python3 analyze_results.py --results ./results --output ./graficos
```

---

## Key Findings

- **Latency:** HTTP/3 saves exactly one RTT relative to HTTP/1.1 and HTTP/2 at every delay condition, confirming the QUIC 1-RTT handshake advantage.
- **Packet loss:** HTTP/3 shows lower TTFB variance at 5% loss. Multi-resource completion times converge across protocols due to sequential measurement methodology.
- **Connection migration:** HTTP/1.1 and HTTP/2 terminate on interface change. HTTP/3 also fails due to the absence of automatic path validation in `curl`/`ngtcp2`, despite the protocol defining the mechanism in RFC 9000.
- **Goodput:** HTTP/3 achieves lower raw throughput (~237 MB/s) than HTTP/1.1 (~945 MB/s) and HTTP/2 (~513 MB/s) on the clean path, consistent with user-space QUIC stack overhead.
