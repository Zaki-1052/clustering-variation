# snpk/amova.py
import argparse
import numpy as np
from collections import defaultdict

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

from .psam import read_psam, REGION_ORDER
from .bed_io import read_bed


def _compute_hs(dosage, mask, pop_members, n_snps, valid_snps):
    """H_S per SNP: n-weighted mean of within-population 2p(1-p)."""
    hs_per_snp = np.zeros(n_snps)
    hs_weight = np.zeros(n_snps)
    for indices in pop_members.values():
        idx = np.array(indices)
        pop_dosage = dosage[idx].sum(axis=0)
        pop_count = mask[idx].sum(axis=0) * 2
        pop_valid = pop_count > 0
        p = np.zeros(n_snps)
        p[pop_valid] = pop_dosage[pop_valid] / pop_count[pop_valid]
        h = 2 * p * (1 - p)
        n = len(indices)
        hs_per_snp += n * h
        hs_weight += n
    nz = hs_weight > 0
    hs_per_snp[nz] /= hs_weight[nz]
    return float(np.mean(hs_per_snp[valid_snps]))


def _compute_hg(dosage, mask, pop_members, region_pops, n_snps, valid_snps):
    """H_G per SNP: n-weighted mean of within-region pooled 2p(1-p)."""
    hg_per_snp = np.zeros(n_snps)
    hg_weight = np.zeros(n_snps)
    for region, pops in region_pops.items():
        region_dosage = np.zeros(n_snps)
        region_count = np.zeros(n_snps)
        region_n = 0
        for pop_name in pops:
            idx = np.array(pop_members[pop_name])
            region_dosage += dosage[idx].sum(axis=0)
            region_count += mask[idx].sum(axis=0) * 2
            region_n += len(pop_members[pop_name])
        region_valid = region_count > 0
        p = np.zeros(n_snps)
        p[region_valid] = region_dosage[region_valid] / region_count[region_valid]
        h = 2 * p * (1 - p)
        hg_per_snp += region_n * h
        hg_weight += region_n
    nz = hg_weight > 0
    hg_per_snp[nz] /= hg_weight[nz]
    return float(np.mean(hg_per_snp[valid_snps]))


def _significance_stars(p):
    if p < 0.001:
        return "***"
    if p < 0.01:
        return "**"
    if p < 0.05:
        return "*"
    return "ns"


def compute_amova(plink_data, psam, n_perm=999):
    """Vectorized hierarchical AMOVA for biallelic SNP data.

    Three-level variance decomposition:
      - Among 7 geographic regions
      - Among populations within regions
      - Within populations

    When n_perm > 0, runs permutation tests for Fct, Fsc, and Fst.
    """
    geno = plink_data.genotypes
    iids = plink_data.iids
    n_snps = geno.shape[1]

    iid_to_pop = {iid: psam[iid].pop_name for iid in iids if iid in psam}
    iid_to_region = {iid: psam[iid].region for iid in iids if iid in psam}

    pop_members = defaultdict(list)
    for idx, iid in enumerate(iids):
        if iid in iid_to_pop:
            pop_members[iid_to_pop[iid]].append(idx)

    region_pops = defaultdict(list)
    for pop_name in pop_members:
        region = iid_to_region[iids[pop_members[pop_name][0]]]
        if pop_name not in region_pops[region]:
            region_pops[region].append(pop_name)

    mask = (geno != -9).astype(np.float64)
    dosage = np.where(geno == -9, 0, geno).astype(np.float64)

    allele_total = dosage.sum(axis=0)
    count_total = mask.sum(axis=0) * 2
    valid_snps = count_total > 0
    p_total = np.zeros(n_snps)
    p_total[valid_snps] = allele_total[valid_snps] / count_total[valid_snps]
    ht_per_snp = 2 * p_total * (1 - p_total)

    H_T = float(np.mean(ht_per_snp[valid_snps]))
    H_S = _compute_hs(dosage, mask, pop_members, n_snps, valid_snps)
    H_G = _compute_hg(dosage, mask, pop_members, region_pops, n_snps, valid_snps)

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
        "n_loci_used": int(valid_snps.sum()),
    }

    if n_perm > 0:
        all_indices = []
        pop_sizes = []
        pop_names_ordered = sorted(pop_members.keys())
        for pn in pop_names_ordered:
            all_indices.extend(pop_members[pn])
            pop_sizes.append(len(pop_members[pn]))
        all_indices = np.array(all_indices)

        region_names = sorted(region_pops.keys())
        region_pop_sizes = []
        for rn in region_names:
            region_pop_sizes.append(len(region_pops[rn]))

        count_fst = 0
        count_fsc = 0
        count_fct = 0

        for i in range(n_perm):
            if (i + 1) % 100 == 0:
                print(f"  AMOVA permutation {i + 1}/{n_perm}", flush=True)

            # Fst: permute individuals among ALL populations
            perm = np.random.permutation(all_indices)
            perm_pop_members = {}
            offset = 0
            for pi, pn in enumerate(pop_names_ordered):
                perm_pop_members[pn] = perm[offset:offset + pop_sizes[pi]].tolist()
                offset += pop_sizes[pi]
            perm_hs = _compute_hs(dosage, mask, perm_pop_members, n_snps, valid_snps)
            perm_fst = (H_T - perm_hs) / H_T if H_T > 0 else 0
            if perm_fst >= Fst:
                count_fst += 1

            # Fsc: permute individuals among pops WITHIN each region
            perm_pop_members_sc = {}
            for rn in region_names:
                region_indices = []
                region_pop_list = sorted(region_pops[rn])
                for pn in region_pop_list:
                    region_indices.extend(pop_members[pn])
                perm_region = np.random.permutation(region_indices)
                offset = 0
                for pn in region_pop_list:
                    sz = len(pop_members[pn])
                    perm_pop_members_sc[pn] = perm_region[offset:offset + sz].tolist()
                    offset += sz
            perm_hs_sc = _compute_hs(dosage, mask, perm_pop_members_sc, n_snps, valid_snps)
            perm_fsc = (H_G - perm_hs_sc) / H_G if H_G > 0 else 0
            if perm_fsc >= Fsc:
                count_fsc += 1

            # Fct: permute whole populations among regions
            perm_region_pops = defaultdict(list)
            shuffled_pops = list(np.random.permutation(pop_names_ordered))
            offset = 0
            for ri, rn in enumerate(region_names):
                for _ in range(region_pop_sizes[ri]):
                    perm_region_pops[rn].append(shuffled_pops[offset])
                    offset += 1
            perm_hg = _compute_hg(dosage, mask, pop_members, perm_region_pops,
                                  n_snps, valid_snps)
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
    print("Hierarchical AMOVA — Variance Partitioning (SNP data)")
    print("=" * 65)
    print(f"{'Source of variation':<35} {'Variance':>10} {'% of total':>10}")
    print("-" * 65)
    print(f"{'Among groups (regions)':<35} {result['among_groups']:>10.4f} "
          f"{result['pct_among_groups']:>9.1f}%")
    print(f"{'Among pops within groups':<35} {result['among_pops_within_groups']:>10.4f} "
          f"{result['pct_among_pops']:>9.1f}%")
    print(f"{'Within populations':<35} {result['within_pops']:>10.4f} "
          f"{result['pct_within_pops']:>9.1f}%")
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
    values = [result["pct_among_groups"], result["pct_among_pops"],
              result["pct_within_pops"]]
    colors = ["#E41A1C", "#FF7F00", "#377EB8"]

    fig, ax = plt.subplots(figsize=(8, 3))
    left = 0
    for val, color, label in zip(values, colors, labels):
        ax.barh(0, val, left=left, color=color, edgecolor="white", height=0.6)
        if val > 5:
            ax.text(left + val / 2, 0, f"{val:.1f}%", ha="center", va="center",
                    fontsize=11, fontweight="bold", color="white")
        left += val

    ax.set_xlim(0, 100)
    ax.set_xlabel("Percentage of total variance", fontsize=11)
    ax.set_yticks([])
    ax.set_title("Lewontin-style AMOVA: Variance Partitioning (SNPs)", fontsize=13)

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
    p = argparse.ArgumentParser(description="AMOVA for SNP data")
    p.add_argument("--bed-prefix", required=True,
                   help="PLINK bed prefix (e.g. hgdp_qc)")
    p.add_argument("--psam", required=True, help="Path to .psam file")
    p.add_argument("--out", default="snp_amova_plot.png")
    p.add_argument("--dpi", type=int, default=200)
    p.add_argument("--amova-perms", type=int, default=999,
                   help="Number of permutations for AMOVA significance test")
    args = p.parse_args()

    print("Loading genotype data...")
    plink_data = read_bed(args.bed_prefix)
    psam = read_psam(args.psam)
    print(f"Loaded {len(plink_data.iids)} individuals, {len(plink_data.snp_ids)} SNPs")

    result = compute_amova(plink_data, psam, n_perm=args.amova_perms)
    print_amova(result)
    plot_amova(result, args.out, args.dpi)
