# snpk/cv_analysis.py
import argparse
import csv
import numpy as np
from dataclasses import dataclass

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

from .admixture import scan_runs


@dataclass
class CVSummary:
    k: int
    mean_cv: float
    std_cv: float
    min_cv: float
    max_cv: float
    mean_loglik: float
    n_reps: int


def compute_cv_summary(output_dir):
    """Aggregate CV errors and log-likelihoods per K."""
    runs = scan_runs(output_dir)
    summaries = []

    for k in sorted(runs):
        cv_errors = [r["cv_error"] for r in runs[k] if r["cv_error"] is not None]
        logliks = [r["loglik"] for r in runs[k] if r["loglik"] is not None]
        if not cv_errors:
            continue
        arr = np.array(cv_errors)
        summaries.append(CVSummary(
            k=k,
            mean_cv=float(np.mean(arr)),
            std_cv=float(np.std(arr, ddof=1)) if len(arr) > 1 else 0.0,
            min_cv=float(np.min(arr)),
            max_cv=float(np.max(arr)),
            mean_loglik=float(np.mean(logliks)) if logliks else float("nan"),
            n_reps=len(cv_errors),
        ))

    return summaries


def plot_cv(summaries, output_path, dpi=200):
    """Plot cross-validation error vs K (lower is better)."""
    ks = [s.k for s in summaries]
    means = [s.mean_cv for s in summaries]
    stds = [s.std_cv for s in summaries]

    best_k = ks[np.argmin(means)]

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))

    ax1.errorbar(ks, means, yerr=stds, fmt="o-", capsize=4,
                 color="#377EB8", markersize=6, linewidth=1.5)
    ax1.axvline(x=best_k, color="#E41A1C", linestyle="--", alpha=0.7,
                label=f"Best K = {best_k}")
    ax1.set_xlabel("K (number of ancestral populations)", fontsize=12)
    ax1.set_ylabel("Cross-validation error", fontsize=12)
    ax1.set_title("ADMIXTURE Cross-Validation", fontsize=13)
    ax1.set_xticks(ks)
    ax1.legend(fontsize=10)
    ax1.grid(True, alpha=0.2)

    logliks = [s.mean_loglik for s in summaries]
    ax2.plot(ks, logliks, "o-", color="#4DAF4A", markersize=6, linewidth=1.5)
    ax2.set_xlabel("K (number of ancestral populations)", fontsize=12)
    ax2.set_ylabel("Mean log-likelihood", fontsize=12)
    ax2.set_title("Log-likelihood vs K", fontsize=13)
    ax2.set_xticks(ks)
    ax2.grid(True, alpha=0.2)

    fig.tight_layout()
    fig.savefig(output_path, dpi=dpi, bbox_inches="tight")
    plt.close(fig)
    print(f"CV plot saved to: {output_path}")
    return best_k


def write_cv_csv(summaries, output_path):
    """Write CV summary to CSV."""
    with open(output_path, "w", newline="") as f:
        w = csv.writer(f)
        w.writerow(["K", "Mean_CV", "Std_CV", "Min_CV", "Max_CV",
                     "Mean_LogLik", "N_Reps"])
        for s in summaries:
            w.writerow([s.k, f"{s.mean_cv:.6f}", f"{s.std_cv:.6f}",
                        f"{s.min_cv:.6f}", f"{s.max_cv:.6f}",
                        f"{s.mean_loglik:.2f}", s.n_reps])
    print(f"CV summary written to: {output_path}")


def print_cv_table(summaries):
    best_k = min(summaries, key=lambda s: s.mean_cv).k
    print()
    print("ADMIXTURE Cross-Validation Summary")
    print("=" * 70)
    print(f"{'K':>3}  {'Mean CV':>10}  {'Std CV':>10}  {'Mean LogLik':>14}  {'Reps':>5}")
    print("-" * 70)
    for s in summaries:
        marker = " <-- best" if s.k == best_k else ""
        print(f"{s.k:>3}  {s.mean_cv:>10.6f}  {s.std_cv:>10.6f}  "
              f"{s.mean_loglik:>14.2f}  {s.n_reps:>5}{marker}")
    print()


if __name__ == "__main__":
    p = argparse.ArgumentParser(description="ADMIXTURE cross-validation analysis")
    p.add_argument("--admixture-dir", default="snpk_output",
                   help="Directory with ADMIXTURE output")
    p.add_argument("--out", default="cv_plot.png", help="Output plot path")
    p.add_argument("--csv", default="cv_summary.csv", help="Output CSV path")
    p.add_argument("--dpi", type=int, default=200)
    args = p.parse_args()

    summaries = compute_cv_summary(args.admixture_dir)
    print_cv_table(summaries)
    write_cv_csv(summaries, args.csv)
    best_k = plot_cv(summaries, args.out, args.dpi)
    print(f"Optimal K by cross-validation: {best_k}")
