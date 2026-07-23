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


def _compute_hs_hg(individuals, n_loci, pop_grouping, region_grouping):
    """Compute H_S and H_G from explicit grouping dicts.

    pop_grouping: {pop_name: [Individual, ...]}
    region_grouping: {region_name: [pop_name, ...]}
    """
    pop_n = {pn: len(members) for pn, members in pop_grouping.items()}

    ht_sum = 0.0
    hs_sum = 0.0
    hg_sum = 0.0
    n_valid = 0

    all_alleles = defaultdict(lambda: defaultdict(int))
    all_total = defaultdict(int)
    for members in pop_grouping.values():
        for ind in members:
            for loc in range(n_loci):
                for h in range(2):
                    a = ind.alleles[h, loc]
                    if a != -9:
                        all_alleles[loc][a] += 1
                        all_total[loc] += 1
    global_freqs = {}
    for loc in range(n_loci):
        if all_total[loc] > 0:
            global_freqs[loc] = {a: c / all_total[loc]
                                 for a, c in all_alleles[loc].items()}
        else:
            global_freqs[loc] = {}

    pop_freqs = {}
    for pname, members in pop_grouping.items():
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
        pop_freqs[pname] = locus_freqs

    region_freqs = {}
    region_n = {}
    for rname, pops in region_grouping.items():
        rn = sum(pop_n.get(pn, 0) for pn in pops)
        region_n[rname] = rn
        locus_freqs = {}
        for loc in range(n_loci):
            counts = defaultdict(int)
            total = 0
            for pn in pops:
                for ind in pop_grouping.get(pn, []):
                    for h in range(2):
                        a = ind.alleles[h, loc]
                        if a != -9:
                            counts[a] += 1
                            total += 1
            if total > 0:
                locus_freqs[loc] = {a: c / total for a, c in counts.items()}
            else:
                locus_freqs[loc] = {}
        region_freqs[rname] = locus_freqs

    for loc in range(n_loci):
        if not global_freqs[loc]:
            continue
        n_valid += 1
        ht_sum += _het(global_freqs[loc])

        ws = 0.0
        wn = 0
        for pname in pop_grouping:
            if loc in pop_freqs[pname] and pop_freqs[pname][loc]:
                n_p = pop_n[pname]
                ws += n_p * _het(pop_freqs[pname][loc])
                wn += n_p
        if wn > 0:
            hs_sum += ws / wn

        wg = 0.0
        wng = 0
        for rname in region_grouping:
            if loc in region_freqs[rname] and region_freqs[rname][loc]:
                n_r = region_n[rname]
                wg += n_r * _het(region_freqs[rname][loc])
                wng += n_r
        if wng > 0:
            hg_sum += wg / wng

    if n_valid == 0:
        return 0.0, 0.0, 0.0, 0
    return ht_sum / n_valid, hs_sum / n_valid, hg_sum / n_valid, n_valid


def _significance_stars(p):
    if p < 0.001:
        return "***"
    if p < 0.01:
        return "**"
    if p < 0.05:
        return "*"
    return "ns"


def compute_amova(geno_data, n_perm=999):
    individuals = geno_data.individuals
    n_loci = len(geno_data.marker_names)

    pop_grouping = defaultdict(list)
    pop_region = {}
    for ind in individuals:
        pop_grouping[ind.pop_name].append(ind)
        pop_region[ind.pop_name] = ind.region

    region_grouping = defaultdict(list)
    for pn, region in pop_region.items():
        if pn not in region_grouping[region]:
            region_grouping[region].append(pn)

    H_T, H_S, H_G, n_valid = _compute_hs_hg(
        individuals, n_loci, dict(pop_grouping), dict(region_grouping))

    among_groups = H_T - H_G
    among_pops = H_G - H_S
    within_pops = H_S

    Fct = among_groups / H_T if H_T > 0 else 0
    Fsc = among_pops / H_G if H_G > 0 else 0
    Fst = (H_T - H_S) / H_T if H_T > 0 else 0

    result = {
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

    if n_perm > 0:
        pop_names_ordered = sorted(pop_grouping.keys())
        all_individuals = []
        pop_sizes = []
        for pn in pop_names_ordered:
            all_individuals.extend(pop_grouping[pn])
            pop_sizes.append(len(pop_grouping[pn]))

        region_names = sorted(region_grouping.keys())
        region_pop_sizes = [len(region_grouping[rn]) for rn in region_names]

        count_fst = 0
        count_fsc = 0
        count_fct = 0

        for i in range(n_perm):
            if (i + 1) % 100 == 0:
                print(f"  AMOVA permutation {i + 1}/{n_perm}", flush=True)

            # Fst: permute individuals among ALL populations
            perm_inds = list(np.random.permutation(all_individuals))
            perm_pop = {}
            offset = 0
            for pi, pn in enumerate(pop_names_ordered):
                perm_pop[pn] = perm_inds[offset:offset + pop_sizes[pi]]
                offset += pop_sizes[pi]
            _, perm_hs, _, _ = _compute_hs_hg(
                all_individuals, n_loci, perm_pop, dict(region_grouping))
            perm_fst = (H_T - perm_hs) / H_T if H_T > 0 else 0
            if perm_fst >= Fst:
                count_fst += 1

            # Fsc: permute individuals within each region
            perm_pop_sc = {}
            for rn in region_names:
                region_pop_list = sorted(region_grouping[rn])
                region_inds = []
                for pn in region_pop_list:
                    region_inds.extend(pop_grouping[pn])
                perm_region = list(np.random.permutation(region_inds))
                offset = 0
                for pn in region_pop_list:
                    sz = len(pop_grouping[pn])
                    perm_pop_sc[pn] = perm_region[offset:offset + sz]
                    offset += sz
            _, perm_hs_sc, _, _ = _compute_hs_hg(
                all_individuals, n_loci, perm_pop_sc, dict(region_grouping))
            perm_fsc = (H_G - perm_hs_sc) / H_G if H_G > 0 else 0
            if perm_fsc >= Fsc:
                count_fsc += 1

            # Fct: permute whole populations among regions
            perm_region_grouping = defaultdict(list)
            shuffled_pops = list(np.random.permutation(pop_names_ordered))
            offset = 0
            for ri, rn in enumerate(region_names):
                for _ in range(region_pop_sizes[ri]):
                    perm_region_grouping[rn].append(shuffled_pops[offset])
                    offset += 1
            _, _, perm_hg, _ = _compute_hs_hg(
                all_individuals, n_loci, dict(pop_grouping),
                dict(perm_region_grouping))
            perm_fct = (H_T - perm_hg) / H_T if H_T > 0 else 0
            if perm_fct >= Fct:
                count_fct += 1

        result["p_Fct"] = (count_fct + 1) / (n_perm + 1)
        result["p_Fsc"] = (count_fsc + 1) / (n_perm + 1)
        result["p_Fst"] = (count_fst + 1) / (n_perm + 1)
        result["n_perm"] = n_perm

    return result


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
    if "p_Fct" in result:
        print(f"  Fct (among groups)              = {result['Fct']:.4f}  "
              f"(p = {result['p_Fct']:.4f} {_significance_stars(result['p_Fct'])})")
        print(f"  Fsc (among pops within groups)  = {result['Fsc']:.4f}  "
              f"(p = {result['p_Fsc']:.4f} {_significance_stars(result['p_Fsc'])})")
        print(f"  Fst (among pops / total)        = {result['Fst']:.4f}  "
              f"(p = {result['p_Fst']:.4f} {_significance_stars(result['p_Fst'])})")
        print(f"\nLoci used: {result['n_loci_used']}  |  Permutations: {result['n_perm']}")
    else:
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

    if "p_Fct" in result:
        fstat_text = (
            f"Fct = {result['Fct']:.4f} {_significance_stars(result['p_Fct'])}\n"
            f"Fsc = {result['Fsc']:.4f} {_significance_stars(result['p_Fsc'])}\n"
            f"Fst = {result['Fst']:.4f} {_significance_stars(result['p_Fst'])}\n"
            f"({result['n_perm']} permutations)"
        )
        ax.text(0.98, -0.25, fstat_text, transform=ax.transAxes,
                fontsize=8, va="top", ha="right", family="monospace",
                bbox=dict(boxstyle="round", facecolor="wheat", alpha=0.8))

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
    p.add_argument("--amova-perms", type=int, default=999,
                   help="Number of permutations for AMOVA significance test")
    args = p.parse_args()

    geno = read_genotypes(args.geno, args.names)
    result = compute_amova(geno, n_perm=args.amova_perms)
    print_amova(result)
    plot_amova(result, args.out, args.dpi)
