# structk/ibd.py
import argparse
import numpy as np
from math import radians, sin, cos, sqrt, atan2, log
from collections import defaultdict
from scipy import stats

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

from .genotypes import read_genotypes

# Coordinates from gnomAD HGDP .psam; names mapped to microsatellite panel
HGDP_COORDS = {
    "Orcadian": (59.0, -3.0),
    "Adygei": (44.0, 39.0),
    "Russian": (61.0, 40.0),
    "Basque": (43.0, 0.0),
    "French": (46.0, 2.0),
    "Italian": (46.0, 10.0),
    "Sardinian": (40.0, 9.0),
    "Tuscan": (43.0, 11.0),
    "Mozabite": (32.0, 3.0),
    "Bedouin": (31.0, 35.0),
    "Druze": (32.0, 35.0),
    "Palestinian": (32.0, 35.0),
    "Balochi": (30.5, 66.5),
    "Brahui": (30.5, 66.5),
    "Burusho": (36.5, 74.0),
    "Hazara": (33.5, 70.0),
    "Kalash": (36.0, 71.5),
    "Makrani": (26.0, 64.0),
    "Pathan": (33.5, 70.5),
    "Sindhi": (25.5, 69.0),
    "Uygur": (44.0, 81.0),
    "Han": (32.3, 114.0),
    "Han-NChina": (34.7, 107.8),
    "Dai": (21.0, 100.0),
    "Daur": (48.5, 124.0),
    "Hezhen": (47.5, 133.5),
    "Lahu": (22.0, 100.0),
    "Miao": (28.0, 109.0),
    "Oroqen": (50.4, 126.5),
    "She": (27.0, 119.0),
    "Tujia": (29.0, 109.0),
    "Tu": (36.0, 101.0),
    "Xibo": (43.5, 81.5),
    "Yi": (28.0, 103.0),
    "Mongola": (48.5, 119.0),
    "Naxi": (26.0, 100.0),
    "Cambodian": (12.0, 105.0),
    "Japanese": (37.5, 139.0),
    "Yakut": (63.0, 129.5),
    "Melanesian": (-6.0, 155.0),
    "Papuan": (-6.1, 145.4),
    "Colombian": (3.0, -68.0),
    "Karitiana": (-10.0, -63.0),
    "Surui": (-11.0, -62.0),
    "Maya": (19.0, -91.0),
    "Pima": (29.0, -108.0),
    "BantuKenya": (-3.0, 37.0),
    "Mandenka": (12.0, -12.0),
    "Yoruba": (8.0, 5.0),
    "BiakaPygmy": (4.0, 17.0),
    "MbutiPygmy": (1.0, 29.0),
    "San": (-21.0, 20.0),
}


def _haversine(lat1, lon1, lat2, lon2):
    R = 6371.0
    phi1, phi2 = radians(lat1), radians(lat2)
    dphi = radians(lat2 - lat1)
    dlam = radians(lon2 - lon1)
    a = sin(dphi / 2) ** 2 + cos(phi1) * cos(phi2) * sin(dlam / 2) ** 2
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))


def _cache_pop_data(individuals, n_loci):
    """Build per-population allele count tables and heterozygosity counts."""
    pops = defaultdict(list)
    for ind in individuals:
        pops[ind.pop_name].append(ind)

    pop_names = sorted(pops)
    allele_counts = {}
    het_counts = {}
    n_valid = {}

    for pname in pop_names:
        members = pops[pname]
        ac = {}
        hc = {}
        nv = {}
        for loc in range(n_loci):
            counts = defaultdict(int)
            hets = defaultdict(int)
            n = 0
            for ind in members:
                a0 = ind.alleles[0, loc]
                a1 = ind.alleles[1, loc]
                if a0 == -9 or a1 == -9:
                    continue
                n += 1
                counts[a0] += 1
                counts[a1] += 1
                if a0 != a1:
                    hets[a0] += 1
                    hets[a1] += 1
            ac[loc] = dict(counts)
            hc[loc] = dict(hets)
            nv[loc] = n
        allele_counts[pname] = ac
        het_counts[pname] = hc
        n_valid[pname] = nv

    return pop_names, allele_counts, het_counts, n_valid


def _pairwise_fst_wc(pop1, pop2, allele_counts, het_counts, n_valid, n_loci):
    """Weir & Cockerham (1984) Fst for two populations, ratio-of-averages."""
    r = 2
    sum_a = 0.0
    sum_abc = 0.0

    for loc in range(n_loci):
        n1 = n_valid[pop1][loc]
        n2 = n_valid[pop2][loc]
        if n1 < 2 or n2 < 2:
            continue

        n_bar = (n1 + n2) / r
        n_c = (r * n_bar - (n1 ** 2 + n2 ** 2) / (r * n_bar)) / (r - 1)

        ac1 = allele_counts[pop1][loc]
        ac2 = allele_counts[pop2][loc]
        hc1 = het_counts[pop1][loc]
        hc2 = het_counts[pop2][loc]

        all_alleles = set(ac1) | set(ac2)

        for allele in all_alleles:
            c1 = ac1.get(allele, 0)
            c2 = ac2.get(allele, 0)
            p1 = c1 / (2 * n1)
            p2 = c2 / (2 * n2)

            p_bar = (n1 * p1 + n2 * p2) / (n1 + n2)

            s_sq = (n1 * (p1 - p_bar) ** 2 + n2 * (p2 - p_bar) ** 2) / ((r - 1) * n_bar)

            h1 = hc1.get(allele, 0) / n1
            h2 = hc2.get(allele, 0) / n2
            h_bar = (h1 + h2) / r

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

            sum_a += a_comp
            sum_abc += a_comp + b_comp + c_comp

    if sum_abc == 0:
        return 0.0
    return sum_a / sum_abc


def compute_pairwise_fst(geno_data):
    n_loci = len(geno_data.marker_names)
    pop_names, allele_counts, het_counts, n_valid = _cache_pop_data(
        geno_data.individuals, n_loci
    )

    n_pops = len(pop_names)
    fst_matrix = np.zeros((n_pops, n_pops))

    total_pairs = n_pops * (n_pops - 1) // 2
    done = 0
    for i in range(n_pops):
        for j in range(i + 1, n_pops):
            fst = _pairwise_fst_wc(
                pop_names[i], pop_names[j],
                allele_counts, het_counts, n_valid, n_loci,
            )
            fst_matrix[i, j] = fst
            fst_matrix[j, i] = fst
            done += 1
            if done % 100 == 0:
                print(f"  Fst: {done}/{total_pairs} pairs computed", flush=True)

    print(f"  Fst: {done}/{total_pairs} pairs computed")
    return pop_names, fst_matrix


def compute_geo_distances(pop_names):
    n = len(pop_names)
    dist_matrix = np.zeros((n, n))
    for i in range(n):
        for j in range(i + 1, n):
            c1 = HGDP_COORDS.get(pop_names[i])
            c2 = HGDP_COORDS.get(pop_names[j])
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
            verticalalignment="top", bbox=dict(boxstyle="round", facecolor="wheat", alpha=0.8))

    ax.set_xlabel("ln(Geographic distance, km)", fontsize=12)
    ax.set_ylabel("$F_{ST}$ / (1 − $F_{ST}$)", fontsize=12)
    ax.set_title("Isolation by Distance", fontsize=13)
    ax.grid(True, alpha=0.2)

    fig.tight_layout()
    fig.savefig(out_path, dpi=dpi, bbox_inches="tight")
    plt.close(fig)
    print(f"IBD plot saved to: {out_path}")


if __name__ == "__main__":
    p = argparse.ArgumentParser(description="Isolation by distance analysis")
    p.add_argument("--geno", required=True, help="Path to diversitydata.str")
    p.add_argument("--names", required=True, help="Path to names.txt")
    p.add_argument("--out", default="ibd_plot.png", help="Output image path")
    p.add_argument("--mantel-perms", type=int, default=9999,
                   help="Number of permutations for Mantel test")
    p.add_argument("--dpi", type=int, default=200)
    args = p.parse_args()

    geno = read_genotypes(args.geno, args.names)
    print(f"Loaded {len(geno.individuals)} individuals, {len(geno.marker_names)} loci")

    print("Computing pairwise Fst (Weir & Cockerham)...")
    pop_names, fst_matrix = compute_pairwise_fst(geno)
    print(f"Mean Fst = {fst_matrix[np.triu_indices(len(pop_names), k=1)].mean():.4f}")

    dist_matrix = compute_geo_distances(pop_names)

    print(f"Running Mantel test ({args.mantel_perms} permutations)...")
    mantel_r, mantel_p = mantel_test(dist_matrix, fst_matrix, args.mantel_perms)
    print(f"Mantel r = {mantel_r:.4f}, p = {mantel_p:.4f}")

    plot_ibd(pop_names, fst_matrix, dist_matrix, args.out, args.dpi, mantel_r, mantel_p)
