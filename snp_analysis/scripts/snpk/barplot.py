# snpk/barplot.py
import argparse
import numpy as np

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

from .psam import read_psam, REGION_ORDER
from .admixture import align_replicates
from .bed_io import _read_fam

CLUSTER_COLORS = [
    "#377EB8", "#E41A1C", "#4DAF4A", "#FF7F00",
    "#984EA3", "#A65628", "#F781BF", "#999999",
    "#66C2A5", "#FC8D62",
]


def _sort_order(iids, psam):
    """Sort individuals by region order, then population name."""
    region_rank = {r: i for i, r in enumerate(REGION_ORDER)}
    entries = []
    for idx, iid in enumerate(iids):
        info = psam.get(iid)
        if info:
            entries.append((region_rank.get(info.region, 99), info.pop_name, idx))
        else:
            entries.append((99, iid, idx))
    entries.sort()
    return [e[2] for e in entries]


def _plot_barplot(ax, Q, iids, psam, title):
    order = _sort_order(iids, psam)
    Q_sorted = Q[order]
    iids_sorted = [iids[i] for i in order]

    n, k = Q_sorted.shape
    x = np.arange(n)
    bottom = np.zeros(n)

    for cluster in range(k):
        color = CLUSTER_COLORS[cluster % len(CLUSTER_COLORS)]
        ax.bar(x, Q_sorted[:, cluster], bottom=bottom, width=1.0,
               color=color, edgecolor="none", linewidth=0)
        bottom += Q_sorted[:, cluster]

    prev_region = None
    regions_seen = []
    for i, iid in enumerate(iids_sorted):
        info = psam.get(iid)
        region = info.region if info else "?"
        if region != prev_region:
            if prev_region is not None:
                ax.axvline(x=i - 0.5, color="black", linewidth=0.8)
            regions_seen.append((i, region))
            prev_region = region

    for idx, (start, region) in enumerate(regions_seen):
        end = regions_seen[idx + 1][0] if idx + 1 < len(regions_seen) else n
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


def plot_barplots(output_dir, psam_path, fam_path, k_values, out_path, dpi=200):
    """Generate distruct-style bar plots for given K values."""
    psam = read_psam(psam_path)
    iids = _read_fam(fam_path)

    fig, axes = plt.subplots(len(k_values), 1, figsize=(18, 3.2 * len(k_values)))
    if len(k_values) == 1:
        axes = [axes]

    for ax, k in zip(axes, k_values):
        Q, info = align_replicates(output_dir, k)
        if Q.shape[0] != len(iids):
            raise ValueError(
                f"Q matrix has {Q.shape[0]} rows but .fam has {len(iids)} individuals"
            )
        _plot_barplot(ax, Q, iids, psam, f"K = {k}")
        print(f"K={k}: CLUMPP-aligned {info['n_replicates']} replicates "
              f"(reference rep={info['reference_rep']})")

    fig.tight_layout(rect=[0, 0.05, 1, 1])
    fig.savefig(out_path, dpi=dpi, bbox_inches="tight")
    plt.close(fig)
    print(f"Bar plot saved to: {out_path}")


if __name__ == "__main__":
    p = argparse.ArgumentParser(description="ADMIXTURE bar plots (distruct-style)")
    p.add_argument("--admixture-dir", default="snpk_output")
    p.add_argument("--psam", required=True, help="Path to .psam file")
    p.add_argument("--fam", required=True, help="Path to .fam file (same order as .Q)")
    p.add_argument("--k-values", nargs="+", type=int, default=[5, 7])
    p.add_argument("--out", default="snp_barplot.png")
    p.add_argument("--dpi", type=int, default=200)
    args = p.parse_args()
    plot_barplots(args.admixture_dir, args.psam, args.fam,
                  args.k_values, args.out, args.dpi)
