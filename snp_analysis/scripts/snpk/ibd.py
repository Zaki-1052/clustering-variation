# snpk/ibd.py
import argparse
import numpy as np
from math import radians, sin, cos, sqrt, atan2
from collections import defaultdict
from scipy import stats

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

from .psam import read_psam
from .bed_io import read_bed


def _haversine(lat1, lon1, lat2, lon2):
    R = 6371.0
    phi1, phi2 = radians(lat1), radians(lat2)
    dphi = radians(lat2 - lat1)
    dlam = radians(lon2 - lon1)
    a = sin(dphi / 2) ** 2 + cos(phi1) * cos(phi2) * sin(dlam / 2) ** 2
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))


def _build_pop_data(plink_data, psam):
    """Group individuals by population and precompute allele frequencies."""
    iids = plink_data.iids
    geno = plink_data.genotypes
    mask = (geno != -9)

    pop_indices = defaultdict(list)
    for idx, iid in enumerate(iids):
        if iid in psam:
            pop_indices[psam[iid].pop_name].append(idx)

    pop_names = sorted(pop_indices)
    return pop_names, pop_indices, geno, mask


def compute_pairwise_fst(plink_data, psam):
    """Weir & Cockerham (1984) Fst for biallelic SNPs, ratio-of-averages.

    Vectorized per population pair: processes all SNPs in one numpy pass.
    """
    pop_names, pop_indices, geno, mask = _build_pop_data(plink_data, psam)
    n_pops = len(pop_names)
    n_snps = geno.shape[1]
    r = 2

    pop_dosage = {}
    pop_count = {}
    pop_het_count = {}

    for pname in pop_names:
        idx = np.array(pop_indices[pname])
        g = geno[idx]
        m = mask[idx]
        pop_dosage[pname] = np.where(m, g, 0).sum(axis=0).astype(np.float64)
        pop_count[pname] = m.sum(axis=0).astype(np.float64)
        pop_het_count[pname] = (g == 1).sum(axis=0).astype(np.float64)

    fst_matrix = np.zeros((n_pops, n_pops))
    total_pairs = n_pops * (n_pops - 1) // 2
    done = 0

    for i in range(n_pops):
        p1 = pop_names[i]
        n1 = pop_count[p1]
        d1 = pop_dosage[p1]
        h1 = pop_het_count[p1]

        for j in range(i + 1, n_pops):
            p2 = pop_names[j]
            n2 = pop_count[p2]
            d2 = pop_dosage[p2]
            h2 = pop_het_count[p2]

            valid = (n1 >= 2) & (n2 >= 2)
            if not valid.any():
                continue

            n1v = n1[valid]
            n2v = n2[valid]
            n_bar = (n1v + n2v) / r
            n_c = (r * n_bar - (n1v ** 2 + n2v ** 2) / (r * n_bar)) / (r - 1)

            p1_freq = d1[valid] / (2 * n1v)
            p2_freq = d2[valid] / (2 * n2v)
            p_bar = (n1v * p1_freq + n2v * p2_freq) / (n1v + n2v)

            s_sq = (n1v * (p1_freq - p_bar) ** 2 +
                    n2v * (p2_freq - p_bar) ** 2) / ((r - 1) * n_bar)

            h1_rate = h1[valid] / n1v
            h2_rate = h2[valid] / n2v
            h_bar = (h1_rate + h2_rate) / r

            a_comp = (n_bar / n_c) * (
                s_sq - (1 / (n_bar - 1)) * (
                    p_bar * (1 - p_bar) - ((r - 1) / r) * s_sq - h_bar / 4
                )
            )
            b_comp = (n_bar / (n_bar - 1)) * (
                p_bar * (1 - p_bar)
                - ((r - 1) / r) * s_sq
                - ((2 * n_bar - 1) / (4 * n_bar)) * h_bar
            )
            c_comp = h_bar / 2

            sum_a = np.nansum(a_comp)
            sum_abc = np.nansum(a_comp + b_comp + c_comp)

            fst = sum_a / sum_abc if sum_abc != 0 else 0.0
            fst_matrix[i, j] = fst
            fst_matrix[j, i] = fst
            done += 1
            if done % 100 == 0:
                print(f"  Fst: {done}/{total_pairs} pairs computed", flush=True)

    print(f"  Fst: {done}/{total_pairs} pairs computed")
    return pop_names, fst_matrix


def compute_geo_distances(pop_names, psam):
    """Geographic distance matrix using coordinates from psam."""
    coords = {}
    for s in psam.values():
        if s.pop_name not in coords:
            coords[s.pop_name] = (s.latitude, s.longitude)

    n = len(pop_names)
    dist_matrix = np.zeros((n, n))
    for i in range(n):
        for j in range(i + 1, n):
            c1 = coords.get(pop_names[i])
            c2 = coords.get(pop_names[j])
            if c1 and c2:
                d = _haversine(c1[0], c1[1], c2[0], c2[1])
            else:
                d = np.nan
            dist_matrix[i, j] = d
            dist_matrix[j, i] = d
    return dist_matrix


def mantel_test(dist_matrix, fst_matrix, n_perm=9999):
    n = dist_matrix.shape[0]
    triu = np.triu_indices(n, k=1)
    d = dist_matrix[triu]
    f = fst_matrix[triu]

    valid = np.isfinite(d) & np.isfinite(f)
    d, f = d[valid], f[valid]

    r_obs = np.corrcoef(d, f)[0, 1]
    count = 0
    for _ in range(n_perm):
        perm = np.random.permutation(n)
        f_perm = fst_matrix[np.ix_(perm, perm)][triu][valid]
        r_perm = np.corrcoef(d, f_perm)[0, 1]
        if r_perm >= r_obs:
            count += 1
    p_value = (count + 1) / (n_perm + 1)
    return r_obs, p_value


def plot_ibd(pop_names, fst_matrix, dist_matrix, out_path, dpi=200,
             mantel_r=None, mantel_p=None):
    n = len(pop_names)
    triu = np.triu_indices(n, k=1)
    fst_vals = fst_matrix[triu]
    dist_vals = dist_matrix[triu]

    valid = np.isfinite(dist_vals) & np.isfinite(fst_vals) & (dist_vals > 0)
    fst_v = fst_vals[valid]
    dist_v = dist_vals[valid]

    fst_lin = fst_v / (1 - fst_v)
    dist_ln = np.log(dist_v)

    slope, intercept, r_value, p_value, _ = stats.linregress(dist_ln, fst_lin)

    fig, ax = plt.subplots(figsize=(9, 6))
    ax.scatter(dist_ln, fst_lin, s=8, alpha=0.4, color="#377EB8", edgecolors="none")

    x_line = np.array([dist_ln.min(), dist_ln.max()])
    ax.plot(x_line, slope * x_line + intercept, color="#E41A1C", linewidth=1.5)

    text = f"$R^2$ = {r_value**2:.3f}\np = {p_value:.2e}"
    if mantel_r is not None:
        text += f"\nMantel r = {mantel_r:.3f}, p = {mantel_p:.4f}"
    ax.text(0.05, 0.95, text, transform=ax.transAxes, fontsize=10,
            verticalalignment="top",
            bbox=dict(boxstyle="round", facecolor="wheat", alpha=0.8))

    ax.set_xlabel("ln(Geographic distance, km)", fontsize=12)
    ax.set_ylabel("$F_{ST}$ / (1 − $F_{ST}$)", fontsize=12)
    ax.set_title("Isolation by Distance (SNP data)", fontsize=13)
    ax.grid(True, alpha=0.2)

    fig.tight_layout()
    fig.savefig(out_path, dpi=dpi, bbox_inches="tight")
    plt.close(fig)
    print(f"IBD plot saved to: {out_path}")


if __name__ == "__main__":
    p = argparse.ArgumentParser(description="Isolation by distance (SNP data)")
    p.add_argument("--bed-prefix", required=True)
    p.add_argument("--psam", required=True)
    p.add_argument("--out", default="snp_ibd_plot.png")
    p.add_argument("--mantel-perms", type=int, default=9999,
                   help="Number of permutations for Mantel test")
    p.add_argument("--dpi", type=int, default=200)
    args = p.parse_args()

    print("Loading genotype data...")
    plink_data = read_bed(args.bed_prefix)
    psam = read_psam(args.psam)
    print(f"Loaded {len(plink_data.iids)} individuals, {len(plink_data.snp_ids)} SNPs")

    print("Computing pairwise Fst (Weir & Cockerham, biallelic)...")
    pop_names, fst_matrix = compute_pairwise_fst(plink_data, psam)
    print(f"Mean Fst = {fst_matrix[np.triu_indices(len(pop_names), k=1)].mean():.4f}")

    dist_matrix = compute_geo_distances(pop_names, psam)

    print(f"Running Mantel test ({args.mantel_perms} permutations)...")
    mantel_r, mantel_p = mantel_test(dist_matrix, fst_matrix, args.mantel_perms)
    print(f"Mantel r = {mantel_r:.4f}, p = {mantel_p:.4f}")

    plot_ibd(pop_names, fst_matrix, dist_matrix, args.out, args.dpi,
             mantel_r, mantel_p)
