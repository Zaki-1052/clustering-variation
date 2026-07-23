# structk/heterozygosity.py
import argparse
import numpy as np
from collections import defaultdict
from math import radians, sin, cos, sqrt, atan2
from scipy import stats

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

from .genotypes import read_genotypes, REGION_ORDER, REGION_COLORS

ADDIS_ABABA = (9.0, 38.75)

# Ramachandran et al. 2005 (PNAS 102:15942), p. 15943 & Supp. Fig. 6
WAYPOINT_ROUTES = {
    "AFRICA": [],
    "MIDDLE_EAST": [],
    "EUROPE": [(30, 31), (41, 28)],              # Cairo → Istanbul
    "CENTRAL_SOUTH_ASIA": [(30, 31)],             # Cairo (overland east via Levant/Iran)
    "EAST_ASIA": [(30, 31)],                      # Cairo (overland via Central Asia)
    "OCEANIA": [(30, 31), (11, 104)],             # Cairo → Phnom Penh
    "AMERICA": [(30, 31), (64, 177), (54, -130)], # Cairo → Anadyr → Prince Rupert
}

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


def _waypoint_distance(pop_name, region):
    coords = HGDP_COORDS.get(pop_name)
    if coords is None:
        return np.nan

    waypoints = WAYPOINT_ROUTES.get(region, [])
    path = [ADDIS_ABABA] + waypoints + [coords]

    total = 0.0
    for i in range(len(path) - 1):
        total += _haversine(path[i][0], path[i][1], path[i + 1][0], path[i + 1][1])
    return total


def compute_pop_heterozygosity(geno_data):
    """Expected heterozygosity per population, averaged across loci."""
    individuals = geno_data.individuals
    n_loci = len(geno_data.marker_names)

    pops = defaultdict(list)
    pop_regions = {}
    for ind in individuals:
        pops[ind.pop_name].append(ind)
        pop_regions[ind.pop_name] = ind.region

    results = {}
    for pname, members in sorted(pops.items()):
        het_per_locus = []
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
                het = 1.0 - sum((c / total) ** 2 for c in counts.values())
                if total > 1:
                    het *= total / (total - 1)
                het_per_locus.append(het)

        mean_het = np.mean(het_per_locus) if het_per_locus else 0.0
        dist = _waypoint_distance(pname, pop_regions[pname])
        results[pname] = {
            "het": mean_het,
            "distance_km": dist,
            "region": pop_regions[pname],
            "n": len(members),
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
    ax.set_title("Heterozygosity vs. Distance from Africa\n(Serial Founder Effect)",
                 fontsize=13)
    ax.legend(fontsize=8, markerscale=0.8, framealpha=0.9, loc="lower left")
    ax.grid(True, alpha=0.2)

    fig.tight_layout()
    fig.savefig(out_path, dpi=dpi, bbox_inches="tight")
    plt.close(fig)
    print(f"Heterozygosity plot saved to: {out_path}")


if __name__ == "__main__":
    p = argparse.ArgumentParser(
        description="Heterozygosity vs. distance from Africa (serial founder effect)")
    p.add_argument("--geno", required=True, help="Path to diversitydata.str")
    p.add_argument("--names", required=True, help="Path to names.txt")
    p.add_argument("--out", default="heterozygosity_plot.png")
    p.add_argument("--dpi", type=int, default=200)
    args = p.parse_args()

    geno = read_genotypes(args.geno, args.names)
    print(f"Loaded {len(geno.individuals)} individuals, {len(geno.marker_names)} loci")
    het_data = compute_pop_heterozygosity(geno)

    for pname in sorted(het_data, key=lambda p: -het_data[p]["het"]):
        d = het_data[pname]
        print(f"  {pname:20s}  Het={d['het']:.4f}  Dist={d['distance_km']:.0f} km")

    plot_heterozygosity(het_data, args.out, args.dpi)
