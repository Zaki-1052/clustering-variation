# snpk/heterozygosity.py
import argparse
import numpy as np
from math import radians, sin, cos, sqrt, atan2
from collections import defaultdict
from scipy import stats

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

from .psam import read_psam, REGION_ORDER, REGION_COLORS
from .bed_io import read_bed

ADDIS_ABABA = (9.0, 38.75)

WAYPOINT_ROUTES = {
    "AFRICA": [],
    "MIDDLE_EAST": [],
    "EUROPE": [(30.0, 31.0), (41.0, 29.0)],
    "CENTRAL_SOUTH_ASIA": [(30.0, 31.0), (33.0, 45.0)],
    "EAST_ASIA": [(30.0, 31.0), (33.0, 45.0)],
    "OCEANIA": [(30.0, 31.0), (33.0, 45.0), (11.0, 105.0)],
    "AMERICA": [(30.0, 31.0), (41.0, 29.0), (55.0, 90.0),
                (65.0, 170.0), (55.0, -130.0)],
}


def _haversine(lat1, lon1, lat2, lon2):
    R = 6371.0
    phi1, phi2 = radians(lat1), radians(lat2)
    dphi = radians(lat2 - lat1)
    dlam = radians(lon2 - lon1)
    a = sin(dphi / 2) ** 2 + cos(phi1) * cos(phi2) * sin(dlam / 2) ** 2
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))


def _waypoint_distance(lat, lon, region):
    """Distance from Addis Ababa via waypoints to (lat, lon)."""
    waypoints = WAYPOINT_ROUTES.get(region, [])
    path = [ADDIS_ABABA] + waypoints + [(lat, lon)]
    total = 0.0
    for i in range(len(path) - 1):
        total += _haversine(path[i][0], path[i][1], path[i + 1][0], path[i + 1][1])
    return total


def compute_pop_heterozygosity(plink_data, psam):
    """Expected heterozygosity per population: mean(2p(1-p)) across SNPs."""
    geno = plink_data.genotypes
    iids = plink_data.iids
    mask = (geno != -9)

    pop_indices = defaultdict(list)
    for idx, iid in enumerate(iids):
        if iid in psam:
            pop_indices[psam[iid].pop_name].append(idx)

    pop_coords = {}
    pop_regions = {}
    for s in psam.values():
        if s.pop_name not in pop_coords:
            pop_coords[s.pop_name] = (s.latitude, s.longitude)
            pop_regions[s.pop_name] = s.region

    results = {}
    for pname, indices in sorted(pop_indices.items()):
        idx = np.array(indices)
        g = geno[idx]
        m = mask[idx]
        dosage = np.where(m, g, 0).astype(np.float64).sum(axis=0)
        count = m.sum(axis=0).astype(np.float64) * 2
        valid = count > 0
        p = np.zeros(g.shape[1])
        p[valid] = dosage[valid] / count[valid]
        het_per_snp = 2 * p * (1 - p)
        mean_het = float(np.mean(het_per_snp[valid])) if valid.any() else 0.0

        lat, lon = pop_coords.get(pname, (np.nan, np.nan))
        region = pop_regions.get(pname, "UNKNOWN")
        dist = _waypoint_distance(lat, lon, region) if np.isfinite(lat) else np.nan

        results[pname] = {
            "het": mean_het,
            "distance_km": dist,
            "region": region,
            "n": len(indices),
        }

    return results


def plot_heterozygosity(het_data, out_path, dpi=200):
    pop_names = []
    hets = []
    dists = []
    regions = []

    for pname, data in sorted(het_data.items()):
        if np.isfinite(data["distance_km"]):
            pop_names.append(pname)
            hets.append(data["het"])
            dists.append(data["distance_km"])
            regions.append(data["region"])

    hets = np.array(hets)
    dists = np.array(dists)

    slope, intercept, r_value, p_value, _ = stats.linregress(dists, hets)

    fig, ax = plt.subplots(figsize=(9, 6))

    for region in REGION_ORDER:
        idx = [i for i, r in enumerate(regions) if r == region]
        if not idx:
            continue
        label = region.replace("_", " ").title()
        ax.scatter(
            dists[idx], hets[idx],
            c=REGION_COLORS[region], label=label,
            s=50, alpha=0.85, edgecolors="black", linewidth=0.3, zorder=3,
        )

    for i, pname in enumerate(pop_names):
        ax.annotate(pname, (dists[i], hets[i]), fontsize=5, alpha=0.6,
                    xytext=(3, 3), textcoords="offset points")

    x_line = np.array([dists.min(), dists.max()])
    ax.plot(x_line, slope * x_line + intercept, color="black",
            linewidth=1.2, linestyle="--", zorder=2)

    ax.text(0.95, 0.95,
            f"r = {r_value:.3f}\nslope = {slope:.2e}/km",
            transform=ax.transAxes, fontsize=10,
            verticalalignment="top", horizontalalignment="right",
            bbox=dict(boxstyle="round", facecolor="wheat", alpha=0.8))

    ax.set_xlabel("Waypoint distance from Addis Ababa (km)", fontsize=12)
    ax.set_ylabel("Mean expected heterozygosity", fontsize=12)
    ax.set_title("Heterozygosity vs. Distance from Africa\n(Serial Founder Effect, SNP data)",
                 fontsize=13)
    ax.legend(fontsize=8, markerscale=0.8, framealpha=0.9, loc="lower left")
    ax.grid(True, alpha=0.2)

    fig.tight_layout()
    fig.savefig(out_path, dpi=dpi, bbox_inches="tight")
    plt.close(fig)
    print(f"Heterozygosity plot saved to: {out_path}")


if __name__ == "__main__":
    p = argparse.ArgumentParser(
        description="Heterozygosity vs distance from Africa (SNP data)")
    p.add_argument("--bed-prefix", required=True)
    p.add_argument("--psam", required=True)
    p.add_argument("--out", default="snp_heterozygosity.png")
    p.add_argument("--dpi", type=int, default=200)
    args = p.parse_args()

    print("Loading genotype data...")
    plink_data = read_bed(args.bed_prefix)
    psam = read_psam(args.psam)
    print(f"Loaded {len(plink_data.iids)} individuals, {len(plink_data.snp_ids)} SNPs")

    het_data = compute_pop_heterozygosity(plink_data, psam)
    for pname in sorted(het_data, key=lambda x: -het_data[x]["het"]):
        d = het_data[pname]
        print(f"  {pname:20s}  Het={d['het']:.4f}  Dist={d['distance_km']:.0f} km")

    plot_heterozygosity(het_data, args.out, args.dpi)
