# structk/pca.py
import argparse
import numpy as np
from collections import defaultdict

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

from .genotypes import read_genotypes, REGION_ORDER, REGION_COLORS


def _compute_psa_distance(geno_data):
    """Proportion of shared alleles distance matrix (Bowcock et al. 1994).

    For each pair of individuals, at each locus, count shared alleles
    via minimum-cost diploid matching. PSA = mean shared / 2 across loci.
    Distance = 1 - PSA.
    """
    individuals = geno_data.individuals
    n_ind = len(individuals)
    n_loci = len(geno_data.marker_names)

    alleles = np.stack([ind.alleles for ind in individuals])  # (n_ind, 2, n_loci)

    shared_sum = np.zeros((n_ind, n_ind), dtype=np.float64)
    valid_count = np.zeros((n_ind, n_ind), dtype=np.float64)

    for loc in range(n_loci):
        a0 = alleles[:, 0, loc]  # (n,)
        a1 = alleles[:, 1, loc]  # (n,)

        has_data = (a0 != -9) & (a1 != -9)  # (n,)
        pair_valid = has_data[:, None] & has_data[None, :]  # (n, n)

        m1 = (a0[:, None] == a0[None, :]).astype(np.float32) + \
             (a1[:, None] == a1[None, :]).astype(np.float32)
        m2 = (a0[:, None] == a1[None, :]).astype(np.float32) + \
             (a1[:, None] == a0[None, :]).astype(np.float32)

        shared = np.maximum(m1, m2)
        shared[~pair_valid] = 0.0

        shared_sum += shared
        valid_count += pair_valid

        if (loc + 1) % 50 == 0:
            print(f"  PSA distance: {loc+1}/{n_loci} loci", flush=True)

    print(f"  PSA distance: {n_loci}/{n_loci} loci")

    valid_count = np.maximum(valid_count, 1)
    psa = shared_sum / (2.0 * valid_count)
    D = 1.0 - psa
    np.fill_diagonal(D, 0.0)
    return D


def _classical_pcoa(D, n_components=10):
    """Classical PCoA (Gower 1966) via eigendecomposition of double-centered
    squared distance matrix."""
    n = D.shape[0]
    D2 = D ** 2

    row_mean = D2.mean(axis=1)
    col_mean = D2.mean(axis=0)
    grand_mean = D2.mean()

    B = -0.5 * (D2 - row_mean[:, None] - col_mean[None, :] + grand_mean)

    eigenvalues, eigenvectors = np.linalg.eigh(B)

    idx = np.argsort(eigenvalues)[::-1]
    eigenvalues = eigenvalues[idx]
    eigenvectors = eigenvectors[:, idx]

    positive = eigenvalues > 0
    total_var = eigenvalues[positive].sum()

    k = min(n_components, positive.sum())
    coords = eigenvectors[:, :k] * np.sqrt(np.maximum(eigenvalues[:k], 0))
    var_explained = eigenvalues[:k] / total_var

    return coords, var_explained


def run_pcoa(geno_data, n_components=10):
    print("Computing proportion-of-shared-alleles distance matrix...")
    D = _compute_psa_distance(geno_data)
    print("Running classical PCoA...")
    coords, var_explained = _classical_pcoa(D, n_components)
    return coords, var_explained


def run_pca(geno_data, n_components=10):
    """Kept for backwards compatibility — now delegates to PCoA."""
    return run_pcoa(geno_data, n_components)


def plot_pca(geno_data, coords, var_explained, out_path, dpi=200,
             method_label="PCoA"):
    individuals = geno_data.individuals
    regions = [ind.region for ind in individuals]

    fig, ax = plt.subplots(figsize=(10, 8))

    for region in REGION_ORDER:
        idx = [i for i, r in enumerate(regions) if r == region]
        if not idx:
            continue
        label = region.replace("_", " ").title()
        ax.scatter(
            coords[idx, 0], coords[idx, 1],
            c=REGION_COLORS[region], label=label,
            s=12, alpha=0.7, edgecolors="none",
        )

    pop_centroids = defaultdict(lambda: [[], []])
    for i, ind in enumerate(individuals):
        pop_centroids[ind.pop_name][0].append(coords[i, 0])
        pop_centroids[ind.pop_name][1].append(coords[i, 1])

    for pname, (xs, ys) in pop_centroids.items():
        cx, cy = np.mean(xs), np.mean(ys)
        ax.annotate(pname, (cx, cy), fontsize=5, alpha=0.6,
                    ha="center", va="bottom")

    ax.set_xlabel(f"{method_label}1 ({var_explained[0]*100:.1f}% variance)", fontsize=12)
    ax.set_ylabel(f"{method_label}2 ({var_explained[1]*100:.1f}% variance)", fontsize=12)
    ax.set_title(f"{method_label} of HGDP Microsatellite Data\n(Proportion of Shared Alleles Distance)",
                 fontsize=13)
    ax.legend(fontsize=9, markerscale=2, framealpha=0.9)
    ax.grid(True, alpha=0.2)

    fig.tight_layout()
    fig.savefig(out_path, dpi=dpi, bbox_inches="tight")
    plt.close(fig)
    print(f"Plot saved to: {out_path}")


if __name__ == "__main__":
    p = argparse.ArgumentParser(
        description="PCoA on microsatellite data (proportion of shared alleles distance)")
    p.add_argument("--geno", required=True, help="Path to diversitydata.str")
    p.add_argument("--names", required=True, help="Path to names.txt")
    p.add_argument("--n-components", type=int, default=10)
    p.add_argument("--out", default="pcoa_plot.png", help="Output image path")
    p.add_argument("--dpi", type=int, default=200)
    args = p.parse_args()

    geno = read_genotypes(args.geno, args.names)
    print(f"Loaded {len(geno.individuals)} individuals, {len(geno.marker_names)} loci")
    coords, var_exp = run_pcoa(geno, args.n_components)
    print(f"Axis 1: {var_exp[0]*100:.1f}%, Axis 2: {var_exp[1]*100:.1f}%")
    plot_pca(geno, coords, var_exp, args.out, args.dpi)
