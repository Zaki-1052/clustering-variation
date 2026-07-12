# structk/barplot.py
import argparse
import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

from .genotypes import read_names, read_qmatrix, find_best_replicate, REGION_ORDER

CLUSTER_COLORS = [
    "#377EB8", "#E41A1C", "#4DAF4A", "#FF7F00",
    "#984EA3", "#A65628", "#F781BF", "#999999",
]


def _sort_order(pop_ids, pop_info):
    region_rank = {r: i for i, r in enumerate(REGION_ORDER)}
    entries = []
    for idx, pid in enumerate(pop_ids):
        info = pop_info.get(pid)
        if info:
            entries.append((region_rank.get(info.region, 99), info.pop_name, idx))
        else:
            entries.append((99, str(pid), idx))
    entries.sort()
    return [e[2] for e in entries]


def _plot_barplot(ax, Q, pop_ids, pop_info, title):
    order = _sort_order(pop_ids, pop_info)
    Q_sorted = Q[order]
    pids_sorted = [pop_ids[i] for i in order]

    n, k = Q_sorted.shape
    x = np.arange(n)
    bottom = np.zeros(n)

    for cluster in range(k):
        color = CLUSTER_COLORS[cluster % len(CLUSTER_COLORS)]
        ax.bar(x, Q_sorted[:, cluster], bottom=bottom, width=1.0,
               color=color, edgecolor="none", linewidth=0)
        bottom += Q_sorted[:, cluster]

    regions_seen = []
    prev_region = None
    for i, pid in enumerate(pids_sorted):
        info = pop_info.get(pid)
        region = info.region if info else "?"
        if region != prev_region:
            if prev_region is not None:
                ax.axvline(x=i - 0.5, color="black", linewidth=0.8)
            regions_seen.append((i, region))
            prev_region = region

    for idx, (start, region) in enumerate(regions_seen):
        if idx + 1 < len(regions_seen):
            end = regions_seen[idx + 1][0]
        else:
            end = n
        mid = (start + end) / 2
        label = region.replace("_", " ").title()
        ax.text(mid, -0.06, label, ha="center", va="top", fontsize=7,
                rotation=45, transform=ax.get_xaxis_transform())

    ax.set_xlim(-0.5, n - 0.5)
    ax.set_ylim(0, 1)
    ax.set_ylabel("Ancestry proportion", fontsize=9)
    ax.set_title(title, fontsize=11, fontweight="bold")
    ax.set_xticks([])
    ax.tick_params(axis="y", labelsize=8)


def plot_barplots(output_dir, names_path, k_values, out_path, dpi=200):
    pop_info = read_names(names_path)

    fig, axes = plt.subplots(len(k_values), 1, figsize=(18, 3.2 * len(k_values)))
    if len(k_values) == 1:
        axes = [axes]

    for ax, k in zip(axes, k_values):
        best = find_best_replicate(output_dir, k)
        Q, indiv_ids, pop_ids = read_qmatrix(best)
        _plot_barplot(ax, Q, pop_ids, pop_info, f"K = {k}")
        print(f"K={k}: using {best} ({Q.shape[0]} individuals, {Q.shape[1]} clusters)")

    fig.tight_layout(rect=[0, 0.05, 1, 1])
    fig.savefig(out_path, dpi=dpi, bbox_inches="tight")
    plt.close(fig)
    print(f"Bar plot saved to: {out_path}")


if __name__ == "__main__":
    p = argparse.ArgumentParser(description="STRUCTURE admixture bar plots (distruct-style)")
    p.add_argument("--output-dir", default="structk_output", help="Directory with STRUCTURE output files")
    p.add_argument("--names", required=True, help="Path to names.txt")
    p.add_argument("--k-values", nargs="+", type=int, default=[4, 5], help="K values to plot (default: 4 5)")
    p.add_argument("--out", default="barplot.png", help="Output image path")
    p.add_argument("--dpi", type=int, default=200)
    args = p.parse_args()
    plot_barplots(args.output_dir, args.names, args.k_values, args.out, args.dpi)
