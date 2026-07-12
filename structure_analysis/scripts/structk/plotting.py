# structk/plotting.py
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from pathlib import Path


def plot_elbow(kstats, output_path):
    ks = [s.k for s in kstats]
    means = [s.mean_ln_prob for s in kstats]
    stds = [s.std_ln_prob for s in kstats]

    fig, ax = plt.subplots(figsize=(8, 5))
    ax.errorbar(ks, means, yerr=stds, fmt="o-", capsize=4, linewidth=1.5, markersize=6)
    ax.set_xlabel("K (number of populations)", fontsize=12)
    ax.set_ylabel("Mean Ln P(D|K)", fontsize=12)
    ax.set_title("Elbow Plot: Estimated Log Probability of Data", fontsize=13)
    ax.set_xticks(ks)
    ax.grid(True, alpha=0.3)
    fig.tight_layout()
    fig.savefig(str(output_path), dpi=150)
    plt.close(fig)


def plot_delta_k(kstats, output_path):
    valid = [(s.k, s.delta_k) for s in kstats if s.delta_k is not None]
    if not valid:
        return

    ks, dks = zip(*valid)

    fig, ax = plt.subplots(figsize=(8, 5))
    ax.bar(ks, dks, color="#4878CF", edgecolor="black", linewidth=0.5)
    ax.set_xlabel("K (number of populations)", fontsize=12)
    ax.set_ylabel("Delta K", fontsize=12)
    ax.set_title("Evanno Method: Delta K", fontsize=13)
    ax.set_xticks(ks)
    ax.grid(True, axis="y", alpha=0.3)
    fig.tight_layout()
    fig.savefig(str(output_path), dpi=150)
    plt.close(fig)


def plot_both(kstats, output_dir, fmt="png"):
    output_dir = Path(output_dir)
    plot_elbow(kstats, output_dir / f"elbow.{fmt}")
    plot_delta_k(kstats, output_dir / f"delta_k.{fmt}")
