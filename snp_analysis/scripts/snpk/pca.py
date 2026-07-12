# snpk/pca.py
import argparse
import numpy as np

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

from .psam import read_psam, REGION_ORDER, REGION_COLORS


def read_eigenvec(path):
    """Read plink2 .eigenvec file.

    Format: tab-delimited, header row with #IID PC1 PC2 ... PCn.
    Returns (iid_list, coords array of shape (n, n_pcs)).
    """
    iids = []
    rows = []
    with open(path) as f:
        header = f.readline().strip().split()
        n_id_cols = 2 if header[0] in ("#FID", "FID") else 1
        for line in f:
            parts = line.strip().split("\t")
            if len(parts) < 2:
                parts = line.strip().split()
            iids.append(parts[n_id_cols - 1])
            rows.append([float(x) for x in parts[n_id_cols:]])
    return iids, np.array(rows)


def read_eigenval(path):
    """Read plink2 .eigenval file (one value per line)."""
    vals = []
    with open(path) as f:
        for line in f:
            line = line.strip()
            if line:
                vals.append(float(line))
    return np.array(vals)


def compute_var_explained(eigenval):
    """Proportion of variance explained per PC."""
    total = eigenval.sum()
    return eigenval / total if total > 0 else eigenval


def plot_pca(eigenvec_path, eigenval_path, psam_path, out_path,
             dpi=200):
    """Generate a 2x2 figure: PC1vPC2, PC1vPC3, PC2vPC3, scree plot."""
    iids, coords = read_eigenvec(eigenvec_path)
    eigenval = read_eigenval(eigenval_path)
    var_exp = compute_var_explained(eigenval)
    psam = read_psam(psam_path)

    regions = [psam[iid].region if iid in psam else "UNKNOWN" for iid in iids]

    fig, axes = plt.subplots(2, 2, figsize=(14, 12))
    pc_pairs = [(0, 1), (0, 2), (1, 2)]

    for ax, (pcx, pcy) in zip(axes.flat[:3], pc_pairs):
        for region in REGION_ORDER:
            idx = [i for i, r in enumerate(regions) if r == region]
            if not idx:
                continue
            label = region.replace("_", " ").title()
            ax.scatter(
                coords[idx, pcx], coords[idx, pcy],
                c=REGION_COLORS[region], label=label,
                s=12, alpha=0.7, edgecolors="none",
            )
        ax.set_xlabel(f"PC{pcx+1} ({var_exp[pcx]*100:.1f}%)", fontsize=11)
        ax.set_ylabel(f"PC{pcy+1} ({var_exp[pcy]*100:.1f}%)", fontsize=11)
        ax.set_title(f"PC{pcx+1} vs PC{pcy+1}", fontsize=12, fontweight="bold")
        ax.grid(True, alpha=0.15)
        ax.tick_params(labelsize=9)

    axes[0, 0].legend(fontsize=7, markerscale=1.5, framealpha=0.9,
                      loc="best", handletextpad=0.3)

    ax_scree = axes[1, 1]
    n_show = min(20, len(eigenval))
    pcs = np.arange(1, n_show + 1)
    ax_scree.bar(pcs, var_exp[:n_show] * 100, color="#377EB8", edgecolor="white")
    ax_scree.set_xlabel("Principal Component", fontsize=11)
    ax_scree.set_ylabel("% Variance Explained", fontsize=11)
    ax_scree.set_title("Scree Plot", fontsize=12, fontweight="bold")
    ax_scree.set_xticks(pcs)
    ax_scree.grid(True, alpha=0.15, axis="y")
    ax_scree.tick_params(labelsize=9)

    cumulative = np.cumsum(var_exp[:n_show]) * 100
    ax2 = ax_scree.twinx()
    ax2.plot(pcs, cumulative, "o-", color="#E41A1C", markersize=4, linewidth=1.2)
    ax2.set_ylabel("Cumulative %", fontsize=10, color="#E41A1C")
    ax2.tick_params(axis="y", labelcolor="#E41A1C", labelsize=8)

    fig.suptitle("PCA on HGDP SNP Data", fontsize=14, fontweight="bold", y=1.01)
    fig.tight_layout()
    fig.savefig(out_path, dpi=dpi, bbox_inches="tight")
    plt.close(fig)

    print(f"PCA plot saved to: {out_path}")
    print(f"PC1: {var_exp[0]*100:.1f}%, PC2: {var_exp[1]*100:.1f}%, "
          f"PC3: {var_exp[2]*100:.1f}%")
    print(f"Total PC1+PC2: {(var_exp[0]+var_exp[1])*100:.1f}%")


if __name__ == "__main__":
    p = argparse.ArgumentParser(description="PCA visualization from plink2 output")
    p.add_argument("--eigenvec", required=True, help="plink2 .eigenvec file")
    p.add_argument("--eigenval", required=True, help="plink2 .eigenval file")
    p.add_argument("--psam", required=True, help="Path to .psam file")
    p.add_argument("--out", default="snp_pca.png")
    p.add_argument("--dpi", type=int, default=200)
    args = p.parse_args()
    plot_pca(args.eigenvec, args.eigenval, args.psam, args.out, args.dpi)
