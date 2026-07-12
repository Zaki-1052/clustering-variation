# snpk/comparison.py
import csv
import numpy as np
from scipy import stats

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt


POP_NAME_MAP = {
    "BiakaPygmy": "Biaka",
    "MbutiPygmy": "Mbuti",
    "Italian": "BergamoItalian",
    "Han-NChina": "NorthernHan",
    "Melanesian": "Bougainville",
    "Mongola": "Mongolian",
    "Papuan": ["PapuanHighlands", "PapuanSepik"],
}


def _find_common_pops(structk_pops, snpk_pops):
    """Find populations common to both datasets, accounting for name changes."""
    reverse_map = {}
    for old, new in POP_NAME_MAP.items():
        if isinstance(new, list):
            for n in new:
                reverse_map[n] = old
        else:
            reverse_map[new] = old

    common = []
    for sp in structk_pops:
        if sp in snpk_pops:
            common.append((sp, sp))
        elif sp in POP_NAME_MAP:
            new = POP_NAME_MAP[sp]
            if isinstance(new, str) and new in snpk_pops:
                common.append((sp, new))

    return common


def compare_k_selection(structk_csv_path, snpk_csv_path, out_path, dpi=200):
    """Side-by-side K selection: Evanno delta-K vs ADMIXTURE CV error."""
    structk_data = {"k": [], "mean_lnp": [], "delta_k": []}
    with open(structk_csv_path) as f:
        reader = csv.DictReader(f)
        for row in reader:
            structk_data["k"].append(int(row["K"]))
            structk_data["mean_lnp"].append(float(row["Mean_LnPD"]))
            dk = row.get("Delta_K", "")
            structk_data["delta_k"].append(float(dk) if dk else None)

    snpk_data = {"k": [], "mean_cv": [], "std_cv": []}
    with open(snpk_csv_path) as f:
        reader = csv.DictReader(f)
        for row in reader:
            snpk_data["k"].append(int(row["K"]))
            snpk_data["mean_cv"].append(float(row["Mean_CV"]))
            snpk_data["std_cv"].append(float(row["Std_CV"]))

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))

    dk_vals = [v for v in structk_data["delta_k"] if v is not None]
    dk_ks = [k for k, v in zip(structk_data["k"], structk_data["delta_k"])
             if v is not None]
    best_structk = dk_ks[np.argmax(dk_vals)] if dk_vals else None

    ax1.bar(dk_ks, dk_vals, color="#377EB8", edgecolor="white", width=0.7)
    if best_structk:
        ax1.axvline(x=best_structk, color="#E41A1C", linestyle="--",
                    label=f"Best K = {best_structk}")
    ax1.set_xlabel("K", fontsize=12)
    ax1.set_ylabel("Delta K", fontsize=12)
    ax1.set_title("Microsatellites: Evanno Delta K\n(STRUCTURE)", fontsize=12)
    ax1.set_xticks(dk_ks)
    ax1.legend(fontsize=10)
    ax1.grid(True, alpha=0.2, axis="y")

    cv_means = snpk_data["mean_cv"]
    cv_stds = snpk_data["std_cv"]
    cv_ks = snpk_data["k"]
    best_snpk = cv_ks[np.argmin(cv_means)]

    ax2.errorbar(cv_ks, cv_means, yerr=cv_stds, fmt="o-", capsize=4,
                 color="#4DAF4A", markersize=6, linewidth=1.5)
    ax2.axvline(x=best_snpk, color="#E41A1C", linestyle="--",
                label=f"Best K = {best_snpk}")
    ax2.set_xlabel("K", fontsize=12)
    ax2.set_ylabel("Cross-validation error", fontsize=12)
    ax2.set_title("SNPs: ADMIXTURE CV Error\n(650k SNPs)", fontsize=12)
    ax2.set_xticks(cv_ks)
    ax2.legend(fontsize=10)
    ax2.grid(True, alpha=0.2)

    fig.suptitle("K Selection: Microsatellites vs SNPs", fontsize=14, fontweight="bold")
    fig.tight_layout()
    fig.savefig(out_path, dpi=dpi, bbox_inches="tight")
    plt.close(fig)
    print(f"K comparison plot saved to: {out_path}")

    return {"structk_best_k": best_structk, "snpk_best_k": best_snpk}


def compare_amova(structk_amova, snpk_amova, out_path, dpi=200):
    """Side-by-side AMOVA comparison."""
    labels = ["Among groups", "Among pops\nwithin groups", "Within pops"]
    keys = ["pct_among_groups", "pct_among_pops", "pct_within_pops"]

    structk_vals = [structk_amova[k] for k in keys]
    snpk_vals = [snpk_amova[k] for k in keys]

    x = np.arange(len(labels))
    width = 0.35

    fig, ax = plt.subplots(figsize=(8, 5))
    bars1 = ax.bar(x - width / 2, structk_vals, width, label="Microsatellites (377 STR)",
                   color="#377EB8", edgecolor="white")
    bars2 = ax.bar(x + width / 2, snpk_vals, width, label="SNPs (650k)",
                   color="#E41A1C", edgecolor="white")

    for bars in [bars1, bars2]:
        for bar in bars:
            h = bar.get_height()
            ax.text(bar.get_x() + bar.get_width() / 2, h + 0.5,
                    f"{h:.1f}%", ha="center", va="bottom", fontsize=9)

    ax.set_ylabel("% of total variance", fontsize=12)
    ax.set_title("AMOVA Comparison: Microsatellites vs SNPs", fontsize=13)
    ax.set_xticks(x)
    ax.set_xticklabels(labels, fontsize=10)
    ax.legend(fontsize=10)
    ax.grid(True, alpha=0.2, axis="y")

    fig.tight_layout()
    fig.savefig(out_path, dpi=dpi, bbox_inches="tight")
    plt.close(fig)
    print(f"AMOVA comparison saved to: {out_path}")

    return {
        "structk": {k: structk_amova[k] for k in keys + ["Fst"]},
        "snpk": {k: snpk_amova[k] for k in keys + ["Fst"]},
    }


def compare_heterozygosity(structk_het, snpk_het, out_path, dpi=200):
    """Compare het vs distance regression parameters."""
    def _fit(het_data):
        hets, dists = [], []
        for data in het_data.values():
            if np.isfinite(data["distance_km"]):
                hets.append(data["het"])
                dists.append(data["distance_km"])
        h, d = np.array(hets), np.array(dists)
        slope, intercept, r_value, _, _ = stats.linregress(d, h)
        return {"slope": slope, "intercept": intercept, "r": r_value,
                "hets": h, "dists": d}

    fit_str = _fit(structk_het)
    fit_snp = _fit(snpk_het)

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5), sharey=False)

    ax1.scatter(fit_str["dists"] / 1000, fit_str["hets"], s=20, alpha=0.7,
                color="#377EB8", edgecolors="none")
    x = np.array([fit_str["dists"].min(), fit_str["dists"].max()])
    ax1.plot(x / 1000, fit_str["slope"] * x + fit_str["intercept"],
             "k--", linewidth=1.2)
    ax1.text(0.95, 0.95, f"r = {fit_str['r']:.3f}",
             transform=ax1.transAxes, fontsize=10, va="top", ha="right",
             bbox=dict(boxstyle="round", facecolor="wheat", alpha=0.8))
    ax1.set_xlabel("Distance from Addis Ababa (×10³ km)", fontsize=11)
    ax1.set_ylabel("Expected heterozygosity", fontsize=11)
    ax1.set_title("Microsatellites (377 STR)", fontsize=12)
    ax1.grid(True, alpha=0.2)

    ax2.scatter(fit_snp["dists"] / 1000, fit_snp["hets"], s=20, alpha=0.7,
                color="#E41A1C", edgecolors="none")
    x = np.array([fit_snp["dists"].min(), fit_snp["dists"].max()])
    ax2.plot(x / 1000, fit_snp["slope"] * x + fit_snp["intercept"],
             "k--", linewidth=1.2)
    ax2.text(0.95, 0.95, f"r = {fit_snp['r']:.3f}",
             transform=ax2.transAxes, fontsize=10, va="top", ha="right",
             bbox=dict(boxstyle="round", facecolor="wheat", alpha=0.8))
    ax2.set_xlabel("Distance from Addis Ababa (×10³ km)", fontsize=11)
    ax2.set_ylabel("Expected heterozygosity", fontsize=11)
    ax2.set_title("SNPs (650k)", fontsize=12)
    ax2.grid(True, alpha=0.2)

    fig.suptitle("Serial Founder Effect Comparison", fontsize=14, fontweight="bold")
    fig.tight_layout()
    fig.savefig(out_path, dpi=dpi, bbox_inches="tight")
    plt.close(fig)
    print(f"Heterozygosity comparison saved to: {out_path}")

    return {
        "structk": {"r": fit_str["r"], "slope": fit_str["slope"]},
        "snpk": {"r": fit_snp["r"], "slope": fit_snp["slope"]},
    }


def compare_fst_matrices(structk_pops, structk_fst, snpk_pops, snpk_fst):
    """Mantel-like correlation between Fst matrices for common populations."""
    common = _find_common_pops(structk_pops, snpk_pops)
    if len(common) < 3:
        return {"r": np.nan, "n_common": len(common),
                "message": "Too few common populations"}

    str_idx = {p: i for i, p in enumerate(structk_pops)}
    snp_idx = {p: i for i, p in enumerate(snpk_pops)}

    str_fst_vals = []
    snp_fst_vals = []
    for i, (s1, n1) in enumerate(common):
        for j, (s2, n2) in enumerate(common):
            if j <= i:
                continue
            str_fst_vals.append(structk_fst[str_idx[s1], str_idx[s2]])
            snp_fst_vals.append(snpk_fst[snp_idx[n1], snp_idx[n2]])

    r, p = stats.pearsonr(str_fst_vals, snp_fst_vals)
    return {"r": r, "p": p, "n_common": len(common),
            "n_pairs": len(str_fst_vals)}


def print_comparison_summary(k_result, amova_result, het_result, fst_result=None):
    """Print a formatted comparison table."""
    print()
    print("=" * 70)
    print("COMPARISON: Microsatellites (377 STR) vs SNPs (650k)")
    print("=" * 70)

    print(f"\n{'Metric':<35} {'Microsats':>12} {'SNPs':>12}")
    print("-" * 70)

    if k_result:
        print(f"{'Optimal K':<35} {k_result['structk_best_k']:>12} "
              f"{k_result['snpk_best_k']:>12}")

    if amova_result:
        for key, label in [("pct_among_groups", "% Among groups"),
                           ("pct_among_pops", "% Among pops within groups"),
                           ("pct_within_pops", "% Within populations"),
                           ("Fst", "Fst")]:
            sv = amova_result["structk"][key]
            nv = amova_result["snpk"][key]
            fmt = ".1f" if "pct" in key else ".4f"
            print(f"{label:<35} {sv:>12{fmt}} {nv:>12{fmt}}")

    if het_result:
        print(f"{'Het vs dist: r':<35} {het_result['structk']['r']:>12.3f} "
              f"{het_result['snpk']['r']:>12.3f}")
        print(f"{'Het vs dist: slope':<35} {het_result['structk']['slope']:>12.2e} "
              f"{het_result['snpk']['slope']:>12.2e}")

    if fst_result and np.isfinite(fst_result.get("r", np.nan)):
        print(f"\nFst matrix correlation (Pearson r): {fst_result['r']:.3f} "
              f"(p = {fst_result['p']:.4f}, {fst_result['n_common']} common pops, "
              f"{fst_result['n_pairs']} pairs)")

    print()


def write_comparison_csv(k_result, amova_result, het_result, fst_result, out_path):
    """Write comparison results to CSV."""
    with open(out_path, "w", newline="") as f:
        w = csv.writer(f)
        w.writerow(["Metric", "Microsatellites", "SNPs"])

        if k_result:
            w.writerow(["Optimal_K", k_result["structk_best_k"],
                        k_result["snpk_best_k"]])

        if amova_result:
            for key, label in [("pct_among_groups", "AMOVA_%_among_groups"),
                               ("pct_among_pops", "AMOVA_%_among_pops_within_groups"),
                               ("pct_within_pops", "AMOVA_%_within_pops"),
                               ("Fst", "Fst")]:
                w.writerow([label, f"{amova_result['structk'][key]:.4f}",
                            f"{amova_result['snpk'][key]:.4f}"])

        if het_result:
            w.writerow(["Het_r", f"{het_result['structk']['r']:.4f}",
                        f"{het_result['snpk']['r']:.4f}"])
            w.writerow(["Het_slope", f"{het_result['structk']['slope']:.6e}",
                        f"{het_result['snpk']['slope']:.6e}"])

        if fst_result and np.isfinite(fst_result.get("r", np.nan)):
            w.writerow(["Fst_matrix_correlation", f"{fst_result['r']:.4f}", ""])
            w.writerow(["Fst_correlation_p", f"{fst_result['p']:.4f}", ""])

    print(f"Comparison CSV written to: {out_path}")
