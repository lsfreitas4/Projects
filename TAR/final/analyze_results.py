import os
import re
import argparse
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from pathlib import Path

# Cores consistentes para cada protocolo
COLORS = {
    "http1.1": "#378ADD",
    "http2":   "#1D9E75",
    "http3":   "#D85A30",
}
LABELS = {
    "http1.1": "HTTP/1.1",
    "http2":   "HTTP/2",
    "http3":   "HTTP/3",
}

plt.rcParams.update({
    "font.family": "sans-serif",
    "font.size": 11,
    "axes.spines.top": False,
    "axes.spines.right": False,
    "axes.grid": True,
    "grid.alpha": 0.3,
    "figure.dpi": 150,
})


def parse_impairment_log(path):
    #Lê um ficheiro impairment_*.log e devolve {proto: {ttfb: [], total: []}}
    results = {}
    current_proto = None
    with open(path) as f:
        for line in f:
            line = line.strip()
            m = re.match(r"--- (http\S+) ---", line)
            if m:
                current_proto = m.group(1)
                results[current_proto] = {"ttfb": [], "total": []}
                continue
            m = re.match(r"Run \d+: TTFB=([\d.]+)s\s+Total=([\d.]+)s", line)
            if m and current_proto:
                results[current_proto]["ttfb"].append(float(m.group(1)) * 1000)
                results[current_proto]["total"].append(float(m.group(2)) * 1000)
    return results


def parse_mosaico_log(path):
    #Lê um ficheiro mosaico_*.log e devolve {proto: [tempos_ms]}
    results = {}
    current_proto = None
    with open(path) as f:
        for line in f:
            line = line.strip()
            m = re.match(r"--- (http\S+) ---", line)
            if m:
                current_proto = m.group(1)
                results[current_proto] = []
                continue
            m = re.match(r"Run \d+: (\d+)ms", line)
            if m and current_proto:
                results[current_proto].append(int(m.group(1)))
    return results


def stats(values):
    arr = np.array(values)
    return {"mean": arr.mean(), "std": arr.std(), "min": arr.min(), "max": arr.max(), "n": len(arr)}


def plot_ttfb_by_condition(data_by_cond, output_dir):
    #Gráfico de linhas: TTFB médio por condição com barras de erro.
    conditions = list(data_by_cond.keys())
    protos = ["http1.1", "http2", "http3"]
    fig, ax = plt.subplots(figsize=(10, 5))

    for proto in protos:
        means, stds = [], []
        for cond in conditions:
            d = data_by_cond[cond].get(proto, {})
            if d and d.get("ttfb"):
                s = stats(d["ttfb"])
                means.append(s["mean"])
                stds.append(s["std"])
            else:
                means.append(None)
                stds.append(0)
        valid = [i for i, v in enumerate(means) if v is not None]
        x = [i for i in valid]
        y = [means[i] for i in valid]
        e = [stds[i] for i in valid]
        ax.errorbar(
            x, y, yerr=e,
            label=LABELS[proto],
            color=COLORS[proto],
            marker="o", linewidth=2, markersize=6,
            capsize=4, capthick=1.5,
            linestyle="--" if proto == "http2" else "-",
        )

    ax.set_xticks(range(len(conditions)))
    ax.set_xticklabels(conditions, rotation=20, ha="right")
    ax.set_ylabel("TTFB médio (ms)")
    ax.set_title("TTFB por condição de rede — média ± desvio padrão")
    ax.legend()
    fig.tight_layout()
    path = os.path.join(output_dir, "ttfb_by_condition.png")
    fig.savefig(path)
    plt.close(fig)
    print(f"  Guardado: {path}")


def plot_mosaico_by_condition(data_by_cond, output_dir):
    #Gráfico de barras agrupadas: tempo mosaico por condição.
    conditions = list(data_by_cond.keys())
    protos = ["http1.1", "http2", "http3"]
    x = np.arange(len(conditions))
    width = 0.25
    fig, ax = plt.subplots(figsize=(10, 5))

    for i, proto in enumerate(protos):
        means, stds = [], []
        for cond in conditions:
            d = data_by_cond[cond].get(proto, [])
            if d:
                s = stats(d)
                means.append(s["mean"])
                stds.append(s["std"])
            else:
                means.append(0)
                stds.append(0)
        ax.bar(
            x + i * width, means, width,
            label=LABELS[proto],
            color=COLORS[proto],
            yerr=stds, capsize=4,
            alpha=0.85,
        )

    ax.set_xticks(x + width)
    ax.set_xticklabels(conditions, rotation=20, ha="right")
    ax.set_ylabel("Tempo total médio (ms)")
    ax.set_title("Mosaico (50 ficheiros) por condição — média ± desvio padrão")
    ax.legend()
    fig.tight_layout()
    path = os.path.join(output_dir, "mosaico_by_condition.png")
    fig.savefig(path)
    plt.close(fig)
    print(f"  Guardado: {path}")


def plot_ttfb_latency(data_by_cond, output_dir):
    #Gráfico focado nas condições de latência: confirma vantagem de 1 RTT do QUIC.
    latency_conds = {k: v for k, v in data_by_cond.items()
                    if k in ["baseline", "delay_20ms", "delay_50ms", 
                            "delay_100ms", "delay_200ms"]}
    if len(latency_conds) < 2:
        return

    cond_names = list(latency_conds.keys())
    protos = ["http1.1", "http2", "http3"]
    fig, ax = plt.subplots(figsize=(9, 5))

    for proto in protos:
        means, stds = [], []
        for cond in cond_names:
            d = latency_conds[cond].get(proto, {})
            if d and d.get("ttfb"):
                s = stats(d["ttfb"])
                means.append(s["mean"])
                stds.append(s["std"])
            else:
                means.append(None)
                stds.append(0)
        valid = [i for i, v in enumerate(means) if v is not None]
        ax.errorbar(
            valid,
            [means[i] for i in valid],
            yerr=[stds[i] for i in valid],
            label=LABELS[proto],
            color=COLORS[proto],
            marker="o", linewidth=2.5, markersize=7,
            capsize=4,
            linestyle="--" if proto == "http2" else "-",
        )

    ax.set_xticks(range(len(cond_names)))
    ax.set_xticklabels(cond_names, rotation=15, ha="right")
    ax.set_ylabel("TTFB médio (ms)")
    ax.set_title("Impacto da latência no TTFB — vantagem do handshake QUIC (1 RTT vs 3 RTTs)")
    ax.legend()
    fig.tight_layout()
    path = os.path.join(output_dir, "ttfb_latency_impact.png")
    fig.savefig(path)
    plt.close(fig)
    print(f"  Guardado: {path}")


def export_csv(imp_data, mos_data, output_dir):
    #Exporta tabela CSV com todas as médias e desvios padrão.
    rows = []
    all_conds = sorted(set(list(imp_data.keys()) + list(mos_data.keys())))
    for cond in all_conds:
        for proto in ["http1.1", "http2", "http3"]:
            row = {"condição": cond, "protocolo": LABELS[proto]}
            if cond in imp_data and proto in imp_data[cond]:
                d = imp_data[cond][proto]
                if d.get("ttfb"):
                    s = stats(d["ttfb"])
                    row["ttfb_mean_ms"] = round(s["mean"], 2)
                    row["ttfb_std_ms"] = round(s["std"], 2)
                    row["ttfb_n"] = s["n"]
            if cond in mos_data and proto in mos_data[cond]:
                d = mos_data[cond][proto]
                if d:
                    s = stats(d)
                    row["mosaico_mean_ms"] = round(s["mean"], 1)
                    row["mosaico_std_ms"] = round(s["std"], 1)
                    row["mosaico_n"] = s["n"]
            rows.append(row)

    df = pd.DataFrame(rows)
    path = os.path.join(output_dir, "complete_results.csv")
    df.to_csv(path, index=False)
    print(f"  Guardado: {path}")
    return df

# Ordem lógica e sem duplicados
ORDER = ["baseline", "delay_20ms", "delay_50ms", "delay_100ms", "delay_200ms",
         "loss_01", "loss_1", "loss_2", "loss_5", "delay100_loss2"]


def main():
    parser = argparse.ArgumentParser(description="Analisa resultados do projeto TAR HTTP")
    parser.add_argument("--results", default="./data/final", help="Pasta com os resultados")
    parser.add_argument("--output", default="./graficos", help="Pasta de saída para gráficos")
    args = parser.parse_args()

    results_dir = Path(args.results)
    output_dir = Path(args.output)
    output_dir.mkdir(parents=True, exist_ok=True)

    print(f"\nA ler resultados de: {results_dir}")
    print(f"A guardar gráficos em: {output_dir}\n")

    # Lê ficheiros de impairment
    imp_dir = results_dir / "impairment"
    imp_data = {}
    if imp_dir.exists():
        for f in sorted(imp_dir.glob("impairment_*.log")):
            cond = f.stem.replace("impairment_", "")
            imp_data[cond] = parse_impairment_log(f)
            print(f"  Lido: {f.name}")

    # Lê ficheiros de mosaico
    mos_dir = results_dir / "mosaico"
    mos_data = {}
    if mos_dir.exists():
        for f in sorted(mos_dir.glob("mosaico_*.log")):
            cond = f.stem.replace("mosaico_", "")
            mos_data[cond] = parse_mosaico_log(f)
            print(f"  Lido: {f.name}")

    # Ordena dados pela ordem lógica definida
    imp_data = {k: v for k, v in imp_data.items() if k in ORDER}
    imp_data = dict(sorted(imp_data.items(), key=lambda x: ORDER.index(x[0]) if x[0] in ORDER else 99))

    mos_data = {k: v for k, v in mos_data.items() if k in ORDER}
    mos_data = dict(sorted(mos_data.items(), key=lambda x: ORDER.index(x[0]) if x[0] in ORDER else 99))

    print(f"\nCondições de impairment encontradas: {list(imp_data.keys())}")
    print(f"Condições de mosaico encontradas:    {list(mos_data.keys())}\n")

    print("A gerar gráficos...")
    if imp_data:
        plot_ttfb_by_condition(imp_data, output_dir)
        plot_ttfb_latency(imp_data, output_dir)
    if mos_data:
        plot_mosaico_by_condition(mos_data, output_dir)

    print("\nA exportar CSV...")
    df = export_csv(imp_data, mos_data, output_dir)

    print("\nResumo dos resultados principais:")
    print(df.to_string(index=False))
    print("\nConcluído.")


if __name__ == "__main__":
    main()