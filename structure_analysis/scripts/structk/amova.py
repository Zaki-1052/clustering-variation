# structk/amova.py
import argparse
import numpy as np
from collections import defaultdict

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

from .genotypes import read_genotypes, REGION_ORDER


def _allele_freqs_per_group(individuals, group_key):
    """Compute allele frequencies grouped by group_key function."""
    n_loci = individuals[0].alleles.shape[1]
    groups = defaultdict(list)
    for ind in individuals:
        groups[group_key(ind)].append(ind)

    freq_by_group = {}
    n_by_group = {}
    for gname, members in groups.items():
        n_by_group[gname] = len(members)
        locus_freqs = {}
        for loc in range(n_loci):
            counts = defaultdict(int)
            total = 0
            for ind in members:
                for h in range(2):
                    a = ind.alleles[h, loc]
                    if a != -9:
                        counts[a] += 1
                        total += 1
            if total > 0:
                locus_freqs[loc] = {a: c / total for a, c in counts.items()}
            else:
                locus_freqs[loc] = {}
        freq_by_group[gname] = locus_freqs

    return freq_by_group, n_by_group


def _het(freq_dict):
    """Expected heterozygosity: 1 - sum(p^2)."""
    return 1.0 - sum(p ** 2 for p in freq_dict.values())


def compute_amova(geno_data):
    individuals = geno_data.individuals
    n_loci = len(geno_data.marker_names)

    pop_freqs, pop_n = _allele_freqs_per_group(individuals, lambda ind: ind.pop_name)
    region_freqs, region_n = _allele_freqs_per_group(individuals, lambda ind: ind.region)

    all_alleles = defaultdict(lambda: defaultdict(int))
    all_total = defaultdict(int)
    for ind in individuals:
        for loc in range(n_loci):
            for h in range(2):
                a = ind.alleles[h, loc]
                if a != -9:
                    all_alleles[loc][a] += 1
                    all_total[loc] += 1
    global_freqs = {}
    for loc in range(n_loci):
        if all_total[loc] > 0:
            global_freqs[loc] = {a: c / all_total[loc] for a, c in all_alleles[loc].items()}
        else:
            global_freqs[loc] = {}

    N_total = sum(pop_n.values())
    N_regions = sum(region_n.values())

    ht_sum = 0.0
    hs_sum = 0.0
    hg_sum = 0.0
    n_valid = 0

    for loc in range(n_loci):
        if not global_freqs[loc]:
            continue
        n_valid += 1

        ht_sum += _het(global_freqs[loc])

        ws = 0.0
        wn = 0
        for pname, freqs in pop_freqs.items():
            if loc in freqs and freqs[loc]:
                n_p = pop_n[pname]
                ws += n_p * _het(freqs[loc])
                wn += n_p
        if wn > 0:
            hs_sum += ws / wn

        wg = 0.0
        wng = 0
        for rname, freqs in region_freqs.items():
            if loc in freqs and freqs[loc]:
                n_r = region_n[rname]
                wg += n_r * _het(freqs[loc])
                wng += n_r
        if wng > 0:
            hg_sum += wg / wng

    H_T = ht_sum / n_valid
    H_S = hs_sum / n_valid
    H_G = hg_sum / n_valid

    among_groups = H_T - H_G
    among_pops = H_G - H_S
    within_pops = H_S

    Fct = among_groups / H_T if H_T > 0 else 0
    Fsc = among_pops / H_G if H_G > 0 else 0
    Fst = (H_T - H_S) / H_T if H_T > 0 else 0

    return {
        "among_groups": among_groups,
        "among_pops_within_groups": among_pops,
        "within_pops": within_pops,
        "total": H_T,
        "pct_among_groups": 100 * among_groups / H_T,
        "pct_among_pops": 100 * among_pops / H_T,
        "pct_within_pops": 100 * within_pops / H_T,
        "Fct": Fct,
        "Fsc": Fsc,
        "Fst": Fst,
        "n_loci_used": n_valid,
    }


def print_amova(result):
    print()
    print("Hierarchical AMOVA — Variance Partitioning")
    print("=" * 65)
    print(f"{'Source of variation':<35} {'Variance':>10} {'% of total':>10}")
    print("-" * 65)
    print(f"{'Among groups (regions)':<35} {result['among_groups']:>10.4f} {result['pct_among_groups']:>9.1f}%")
    print(f"{'Among pops within groups':<35} {result['among_pops_within_groups']:>10.4f} {result['pct_among_pops']:>9.1f}%")
    print(f"{'Within populations':<35} {result['within_pops']:>10.4f} {result['pct_within_pops']:>9.1f}%")
    print("-" * 65)
    print(f"{'Total':<35} {result['total']:>10.4f} {'100.0%':>10}")
    print()
    print("F-statistics:")
    print(f"  Fct (among groups)              = {result['Fct']:.4f}")
    print(f"  Fsc (among pops within groups)  = {result['Fsc']:.4f}")
    print(f"  Fst (among pops / total)        = {result['Fst']:.4f}")
    print(f"\nLoci used: {result['n_loci_used']}")
    print()


def plot_amova(result, out_path, dpi=200):
    labels = ["Among\ngroups", "Among pops\nwithin groups", "Within\npopulations"]
    values = [result["pct_among_groups"], result["pct_among_pops"], result["pct_within_pops"]]
    colors = ["#E41A1C", "#FF7F00", "#377EB8"]

    fig, ax = plt.subplots(figsize=(8, 3))
    left = 0
    for val, color, label in zip(values, colors, labels):
        bar = ax.barh(0, val, left=left, color=color, edgecolor="white", height=0.6)
        if val > 5:
            ax.text(left + val / 2, 0, f"{val:.1f}%", ha="center", va="center",
                    fontsize=11, fontweight="bold", color="white")
        left += val

    ax.set_xlim(0, 100)
    ax.set_xlabel("Percentage of total variance", fontsize=11)
    ax.set_yticks([])
    ax.set_title("Lewontin-style AMOVA: Variance Partitioning", fontsize=13)

    handles = [plt.Rectangle((0, 0), 1, 1, color=c) for c in colors]
    ax.legend(handles, labels, loc="upper right", fontsize=9, framealpha=0.9)

    fig.tight_layout()
    fig.savefig(out_path, dpi=dpi, bbox_inches="tight")
    plt.close(fig)
    print(f"AMOVA plot saved to: {out_path}")


if __name__ == "__main__":
    p = argparse.ArgumentParser(description="Hierarchical AMOVA (Lewontin-style variance partitioning)")
    p.add_argument("--geno", required=True, help="Path to diversitydata.str")
    p.add_argument("--names", required=True, help="Path to names.txt")
    p.add_argument("--out", default="amova_plot.png", help="Output image path")
    p.add_argument("--dpi", type=int, default=200)
    args = p.parse_args()

    geno = read_genotypes(args.geno, args.names)
    result = compute_amova(geno)
    print_amova(result)
    plot_amova(result, args.out, args.dpi)
