# snpk/combined.py
import argparse
import numpy as np
from scipy import stats

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.gridspec import GridSpec
from matplotlib.patches import Rectangle

from .psam import read_psam, REGION_ORDER, REGION_COLORS
from .bed_io import read_bed, _read_fam
from .admixture import read_q, find_best_replicate
from .barplot import _sort_order, CLUSTER_COLORS
from .pca import read_eigenvec, read_eigenval, compute_var_explained
from .amova import compute_amova
from .ibd import compute_pairwise_fst, compute_geo_distances
from .heterozygosity import compute_pop_heterozygosity


def _draw_barplot(ax, Q, iids, psam, title):
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
                ax.axvline(x=i - 0.5, color="black", linewidth=0.6)
            regions_seen.append((i, region))
            prev_region = region

    for idx, (start, region) in enumerate(regions_seen):
        end = regions_seen[idx + 1][0] if idx + 1 < len(regions_seen) else n
        mid = (start + end) / 2
        label = region.replace("_", " ").title()
        ax.text(mid, -0.08, label, ha="center", va="top", fontsize=5.5,
                rotation=45, transform=ax.get_xaxis_transform())

    ax.set_xlim(-0.5, n - 0.5)
    ax.set_ylim(0, 1)
    ax.set_ylabel("Ancestry", fontsize=7)
    ax.set_title(title, fontsize=9, fontweight="bold", pad=2)
    ax.set_xticks([])
    ax.tick_params(axis="y", labelsize=6)


def _draw_pca(ax, iids, coords, var_exp, psam):
    regions = [psam[iid].region if iid in psam else "UNKNOWN" for iid in iids]

    for region in REGION_ORDER:
        idx = [i for i, r in enumerate(regions) if r == region]
        if not idx:
            continue
        label = region.replace("_", " ").title()
        ax.scatter(coords[idx, 0], coords[idx, 1],
                   c=REGION_COLORS[region], label=label,
                   s=6, alpha=0.6, edgecolors="none")

    ax.set_xlabel(f"PC1 ({var_exp[0]*100:.1f}%)", fontsize=8)
    ax.set_ylabel(f"PC2 ({var_exp[1]*100:.1f}%)", fontsize=8)
    ax.set_title("PCA (650k SNPs)", fontsize=9, fontweight="bold")
    ax.legend(fontsize=5, markerscale=1.5, framealpha=0.9, loc="upper left",
              handletextpad=0.3, borderpad=0.3)
    ax.grid(True, alpha=0.15)
    ax.tick_params(labelsize=6)


def _draw_ibd(ax, pop_names, fst_matrix, dist_matrix):
    n = len(pop_names)
    triu = np.triu_indices(n, k=1)
    fst_vals = fst_matrix[triu]
    dist_vals = dist_matrix[triu]

    valid = np.isfinite(dist_vals) & np.isfinite(fst_vals) & (dist_vals > 0)
    fst_v = np.maximum(fst_vals[valid], 0.0)
    dist_v = dist_vals[valid]

    fst_lin = fst_v / (1 - fst_v)
    dist_ln = np.log(dist_v)

    slope, intercept, r_value, _, _ = stats.linregress(dist_ln, fst_lin)

    ax.scatter(dist_ln, fst_lin, s=4, alpha=0.3, color="#377EB8", edgecolors="none")
    x_line = np.array([dist_ln.min(), dist_ln.max()])
    ax.plot(x_line, slope * x_line + intercept, color="#E41A1C", linewidth=1.2)

    ax.text(0.05, 0.95, f"$R^2$ = {r_value**2:.3f}",
            transform=ax.transAxes, fontsize=6.5, verticalalignment="top",
            bbox=dict(boxstyle="round", facecolor="wheat", alpha=0.8, pad=0.3))

    ax.set_xlabel("ln(Geographic distance, km)", fontsize=8)
    ax.set_ylabel("$F_{ST}$ / (1 − $F_{ST}$)", fontsize=8)
    ax.set_title("Isolation by Distance", fontsize=9, fontweight="bold")
    ax.grid(True, alpha=0.15)
    ax.tick_params(labelsize=6)


def _draw_het(ax, het_data):
    pop_names = []
    hets = []
    dists = []
    regions = []
    for pname, data in sorted(het_data.items()):
        if np.isfinite(data["distance_km"]):
            pop_names.append(pname)
            hets.append(data["het"])
            dists.append(data["distance_km"])
            regions.append(data["region"])

    hets = np.array(hets)
    dists = np.array(dists)
    slope, intercept, r_value, _, _ = stats.linregress(dists, hets)

    for region in REGION_ORDER:
        idx = [i for i, r in enumerate(regions) if r == region]
        if not idx:
            continue
        label = region.replace("_", " ").title()
        ax.scatter(dists[idx] / 1000, hets[idx],
                   c=REGION_COLORS[region], label=label,
                   s=25, alpha=0.85, edgecolors="black", linewidth=0.3, zorder=3)

    x_line = np.array([dists.min(), dists.max()])
    ax.plot(x_line / 1000, slope * x_line + intercept, color="black",
            linewidth=1.0, linestyle="--", zorder=2)

    ax.text(0.95, 0.95, f"r = {r_value:.3f}",
            transform=ax.transAxes, fontsize=6.5,
            verticalalignment="top", horizontalalignment="right",
            bbox=dict(boxstyle="round", facecolor="wheat", alpha=0.8, pad=0.3))

    ax.set_xlabel("Distance from Addis Ababa (×10³ km)", fontsize=8)
    ax.set_ylabel("Expected heterozygosity", fontsize=8)
    ax.set_title("Het. vs. Distance from Africa", fontsize=9, fontweight="bold")
    ax.grid(True, alpha=0.15)
    ax.tick_params(labelsize=6)


def _draw_amova(ax, result):
    labels = ["Among\ngroups", "Among pops\nwithin groups", "Within\npopulations"]
    values = [result["pct_among_groups"], result["pct_among_pops"],
              result["pct_within_pops"]]
    colors = ["#E41A1C", "#FF7F00", "#377EB8"]

    left = 0
    for val, color in zip(values, colors):
        ax.barh(0, val, left=left, color=color, edgecolor="white", height=0.5)
        if val > 4:
            ax.text(left + val / 2, 0, f"{val:.1f}%", ha="center", va="center",
                    fontsize=7, fontweight="bold", color="white")
        left += val

    ax.set_xlim(0, 100)
    ax.set_xlabel("% of total variance", fontsize=8)
    ax.set_yticks([])
    ax.set_title("AMOVA: Variance Partitioning", fontsize=9, fontweight="bold")
    ax.tick_params(labelsize=6)

    handles = [Rectangle((0, 0), 1, 1, color=c) for c in colors]
    ax.legend(handles, labels, fontsize=5, loc="center right",
              framealpha=0.9, handletextpad=0.3, borderpad=0.3)


def generate_combined(admixture_dir, psam_path, bed_prefix, eigenvec_path,
                      eigenval_path, k_values, out_path, dpi=250):
    print("Loading data...")
    psam = read_psam(psam_path)
    fam_iids = _read_fam(bed_prefix + ".fam")

    fig = plt.figure(figsize=(18, 18))
    gs = GridSpec(5, 4, figure=fig,
                  height_ratios=[1, 1, 0.12, 2.2, 2.2],
                  hspace=0.5, wspace=0.35)

    print("Plotting bar plots...")
    for row, k in enumerate(k_values[:2]):
        ax = fig.add_subplot(gs[row, :])
        q_path = find_best_replicate(admixture_dir, k)
        Q = read_q(q_path)
        _draw_barplot(ax, Q, fam_iids, psam, f"K = {k}")

    ax_pca = fig.add_subplot(gs[3, :2])
    ax_ibd = fig.add_subplot(gs[3, 2:])
    ax_het = fig.add_subplot(gs[4, :2])
    ax_amova = fig.add_subplot(gs[4, 2:])

    print("Loading PCA results...")
    pca_iids, coords = read_eigenvec(eigenvec_path)
    eigenval = read_eigenval(eigenval_path)
    var_exp = compute_var_explained(eigenval)
    _draw_pca(ax_pca, pca_iids, coords, var_exp, psam)

    print("Loading genotype data for Fst/Het/AMOVA...")
    plink_data = read_bed(bed_prefix)

    print("Computing pairwise Fst...")
    pop_names, fst_matrix = compute_pairwise_fst(plink_data, psam)
    dist_matrix = compute_geo_distances(pop_names, psam)
    _draw_ibd(ax_ibd, pop_names, fst_matrix, dist_matrix)

    print("Computing heterozygosity vs distance...")
    het_data = compute_pop_heterozygosity(plink_data, psam)
    _draw_het(ax_het, het_data)

    print("Computing AMOVA...")
    amova_result = compute_amova(plink_data, psam)
    _draw_amova(ax_amova, amova_result)

    panel_labels = ["A", "B", "C", "D", "E", "F"]
    panel_axes = [fig.axes[0], fig.axes[1],
                  ax_pca, ax_ibd, ax_het, ax_amova]
    for label, a in zip(panel_labels, panel_axes):
        a.text(-0.03, 1.08, label, transform=a.transAxes,
               fontsize=14, fontweight="bold", va="top")

    fig.savefig(out_path, dpi=dpi, bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print(f"Combined figure saved to: {out_path}")


if __name__ == "__main__":
    p = argparse.ArgumentParser(description="Combined SNP population genetics figure")
    p.add_argument("--admixture-dir", default="snpk_output")
    p.add_argument("--psam", required=True)
    p.add_argument("--bed-prefix", required=True)
    p.add_argument("--eigenvec", required=True)
    p.add_argument("--eigenval", required=True)
    p.add_argument("--k-values", nargs="+", type=int, default=[5, 7])
    p.add_argument("--out", default="snp_combined_figure.png")
    p.add_argument("--dpi", type=int, default=250)
    args = p.parse_args()

    generate_combined(args.admixture_dir, args.psam, args.bed_prefix,
                      args.eigenvec, args.eigenval,
                      args.k_values, args.out, args.dpi)
