# snpk/__main__.py
import argparse
from pathlib import Path


def main():
    p = argparse.ArgumentParser(
        description="SNP population genetics analysis suite (HGDP 650k SNPs)",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""\
Individual analyses can also be run as standalone modules:
  python -m snpk.cv_analysis   --admixture-dir snpk_output
  python -m snpk.barplot       --admixture-dir snpk_output --psam hgdp_all.psam --fam hgdp_pruned.fam
  python -m snpk.pca           --eigenvec hgdp_pca.eigenvec --eigenval hgdp_pca.eigenval --psam hgdp_all.psam
  python -m snpk.amova         --bed-prefix hgdp_qc --psam hgdp_all.psam
  python -m snpk.ibd           --bed-prefix hgdp_qc --psam hgdp_all.psam --mantel
  python -m snpk.heterozygosity --bed-prefix hgdp_qc --psam hgdp_all.psam
  python -m snpk.combined      --admixture-dir snpk_output --psam hgdp_all.psam --bed-prefix hgdp_qc --eigenvec hgdp_pca.eigenvec --eigenval hgdp_pca.eigenval
""",
    )
    p.add_argument("--admixture-dir", default="snpk_output",
                   help="Directory with ADMIXTURE K-sweep output")
    p.add_argument("--psam", required=True, help="Path to .psam sample metadata")
    p.add_argument("--bed-prefix", required=True,
                   help="PLINK bed prefix for genotype analyses (e.g. hgdp_qc)")
    p.add_argument("--fam", help="Path to .fam file (default: bed-prefix + .fam)")
    p.add_argument("--eigenvec", required=True, help="plink2 .eigenvec file")
    p.add_argument("--eigenval", required=True, help="plink2 .eigenval file")
    p.add_argument("--k-values", nargs="+", type=int, default=[5, 7],
                   help="K values for bar plots (default: 5 7)")
    p.add_argument("--output-dir", "-o", default="snpk_results",
                   help="Output directory for all results")
    p.add_argument("--mantel", action="store_true", help="Run Mantel test for IBD")
    p.add_argument("--mantel-perms", type=int, default=9999)
    p.add_argument("--dpi", type=int, default=200)
    p.add_argument("--skip", nargs="*", default=[],
                   choices=["cv", "barplot", "pca", "amova", "ibd", "het", "combined"],
                   help="Skip specific analyses")

    args = p.parse_args()
    fam_path = args.fam or (args.bed_prefix + ".fam")
    out = Path(args.output_dir)
    out.mkdir(parents=True, exist_ok=True)

    skip = set(args.skip)

    if "cv" not in skip:
        print("\n=== Cross-Validation Analysis ===")
        from .cv_analysis import compute_cv_summary, plot_cv, write_cv_csv, print_cv_table
        summaries = compute_cv_summary(args.admixture_dir)
        if summaries:
            print_cv_table(summaries)
            write_cv_csv(summaries, str(out / "cv_summary.csv"))
            best_k = plot_cv(summaries, str(out / "cv_plot.png"), args.dpi)
            print(f"Optimal K by cross-validation: {best_k}")
        else:
            print("No ADMIXTURE results found. Skipping CV analysis.")

    if "barplot" not in skip:
        print("\n=== Admixture Bar Plots ===")
        from .barplot import plot_barplots
        plot_barplots(args.admixture_dir, args.psam, fam_path,
                      args.k_values, str(out / "barplot.png"), args.dpi)

    if "pca" not in skip:
        print("\n=== PCA ===")
        from .pca import plot_pca
        plot_pca(args.eigenvec, args.eigenval, args.psam,
                 str(out / "pca.png"), args.dpi)

    plink_data = None
    psam = None

    def _load_geno():
        nonlocal plink_data, psam
        if plink_data is None:
            print("Loading genotype data...")
            from .bed_io import read_bed
            from .psam import read_psam
            plink_data = read_bed(args.bed_prefix)
            psam = read_psam(args.psam)
            print(f"  {len(plink_data.iids)} individuals, "
                  f"{len(plink_data.snp_ids)} SNPs")

    if "amova" not in skip:
        print("\n=== AMOVA ===")
        _load_geno()
        from .amova import compute_amova, print_amova, plot_amova
        amova_result = compute_amova(plink_data, psam)
        print_amova(amova_result)
        plot_amova(amova_result, str(out / "amova.png"), args.dpi)

    if "ibd" not in skip:
        print("\n=== Isolation by Distance ===")
        _load_geno()
        from .ibd import (compute_pairwise_fst, compute_geo_distances,
                          mantel_test, plot_ibd)
        print("Computing pairwise Fst (Weir & Cockerham, biallelic)...")
        import numpy as np
        pop_names, fst_matrix = compute_pairwise_fst(plink_data, psam)
        mean_fst = fst_matrix[np.triu_indices(len(pop_names), k=1)].mean()
        print(f"Mean Fst = {mean_fst:.4f}")
        dist_matrix = compute_geo_distances(pop_names, psam)

        mantel_r, mantel_p = None, None
        if args.mantel:
            print(f"Running Mantel test ({args.mantel_perms} permutations)...")
            mantel_r, mantel_p = mantel_test(dist_matrix, fst_matrix,
                                             args.mantel_perms)
            print(f"Mantel r = {mantel_r:.4f}, p = {mantel_p:.4f}")

        plot_ibd(pop_names, fst_matrix, dist_matrix,
                 str(out / "ibd.png"), args.dpi, mantel_r, mantel_p)

    if "het" not in skip:
        print("\n=== Heterozygosity vs Distance from Africa ===")
        _load_geno()
        from .heterozygosity import compute_pop_heterozygosity, plot_heterozygosity
        het_data = compute_pop_heterozygosity(plink_data, psam)
        for pname in sorted(het_data, key=lambda x: -het_data[x]["het"]):
            d = het_data[pname]
            print(f"  {pname:20s}  Het={d['het']:.4f}  "
                  f"Dist={d['distance_km']:.0f} km")
        plot_heterozygosity(het_data, str(out / "heterozygosity.png"), args.dpi)

    if "combined" not in skip:
        print("\n=== Combined Figure ===")
        from .combined import generate_combined
        generate_combined(args.admixture_dir, args.psam, args.bed_prefix,
                          args.eigenvec, args.eigenval,
                          args.k_values, str(out / "combined_figure.png"), args.dpi)

    print("\n=== All analyses complete ===")
    print(f"Results in: {out}")


if __name__ == "__main__":
    main()
