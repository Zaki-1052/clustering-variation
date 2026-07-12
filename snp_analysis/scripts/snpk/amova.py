# snpk/amova.py
import argparse
import numpy as np
from collections import defaultdict

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

from .psam import read_psam, REGION_ORDER
from .bed_io import read_bed


def compute_amova(plink_data, psam):
    """Vectorized hierarchical AMOVA for biallelic SNP data.

    Three-level variance decomposition:
      - Among 7 geographic regions
      - Among populations within regions
      - Within populations
    """
    geno = plink_data.genotypes  # (n_ind, n_snps), int8, 0/1/2/-9
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

    hs_per_snp = np.zeros(n_snps)
    hs_weight = np.zeros(n_snps)
    pop_p = {}
    pop_n = {}

    for pop_name, indices in pop_members.items():
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
        pop_p[pop_name] = p
        pop_n[pop_name] = n

    nz = hs_weight > 0
    hs_per_snp[nz] /= hs_weight[nz]

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
            region_n += pop_n[pop_name]
        region_valid = region_count > 0
        p = np.zeros(n_snps)
        p[region_valid] = region_dosage[region_valid] / region_count[region_valid]
        h = 2 * p * (1 - p)
        hg_per_snp += region_n * h
        hg_weight += region_n

    nz = hg_weight > 0
    hg_per_snp[nz] /= hg_weight[nz]

    H_T = float(np.mean(ht_per_snp[valid_snps]))
    H_S = float(np.mean(hs_per_snp[valid_snps]))
    H_G = float(np.mean(hg_per_snp[valid_snps]))

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
        "n_loci_used": int(valid_snps.sum()),
    }


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
    args = p.parse_args()

    print("Loading genotype data...")
    plink_data = read_bed(args.bed_prefix)
    psam = read_psam(args.psam)
    print(f"Loaded {len(plink_data.iids)} individuals, {len(plink_data.snp_ids)} SNPs")

    result = compute_amova(plink_data, psam)
    print_amova(result)
    plot_amova(result, args.out, args.dpi)
