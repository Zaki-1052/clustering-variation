# Clustering

Population structure analysis of the HGDP dataset. Two analyses, two methods, same question: how many genetically distinct clusters exist in the human population?

The microsatellite analysis uses STRUCTURE locally. The SNP analysis uses ADMIXTURE on SDSC Expanse. Both run K-sweeps from K=1 to K=10 with 5 replicates each.

## Directory layout

```
clustering/
├── structure_analysis/          # Microsatellite STRUCTURE analysis
│   ├── bin/         (22 files)  # C source, .o files, Makefile, structure binary
│   ├── scripts/     (13 files)  # structk/ Python package
│   ├── inputs/       (8 files)  # diversitydata.str, params, exercise data
│   ├── output/      (52 files)  # raw k*_r*_f structure runs + seed files
│   └── results/     (12 files)  # barplot, pcoa, pca, amova, het, ibd, combined, elbow, delta_k, evanno, barplot_K7
│
├── snp_analysis/                # SNP ADMIXTURE analysis
│   ├── scripts/     (16 files)  # snpk/ Python package + SLURM scripts
│   ├── inputs/       (8 files)  # hgdp_pruned.*, hgdp_qc.*, psam, pca, ldprune
│   ├── output/      (50 files)  # K*_rep*.log from admixture runs
│   ├── results/     (10 files)  # barplot, cv, pca, amova, het, ibd, combined, snp_barplot_K7
│   └── logs/        (12 files)  # SLURM output, plink logs, snpk_analysis.log
│
├── comparison_results/  (5 files)  # Cross-method comparison: k_comparison, amova, het, summary CSV, log
│
├── tools/                       # Shared third-party binaries
│   ├── admixture_linux-1.4.0/
│   ├── admixture_macosx-1.3.0/
│   ├── distruct1.1/
│   ├── frappe_macOSX/
│   ├── plink2_mac_arm64_20260504/
│   └── structure_frontend_src/
│
└── docs/            (12 files)  # Papers, manuals, archives
```

## Microsatellite STRUCTURE analysis

Runs the STRUCTURE binary (compiled from C source in `bin/`) on 377 microsatellite loci from the HGDP diversity panel. The `structk` Python package in `scripts/` automates K-sweeps and generates bar plots, PCoA, PCA, AMOVA, heterozygosity, and IBD figures.

Input data is `diversitydata.str` from Prof. Amy Non's structure exercise at UCSD. STRUCTURE param files (`mainparams`, `extraparams`) live in `inputs/`.

K=4 had the greatest delta K in the Evanno method, consistent with Serre & Paabo 2004's finding that K=4 was the most stable clustering. K=2 came close, anchored by the Africa-America split (the largest genetic distance in the dataset).

## SNP ADMIXTURE analysis

Runs ADMIXTURE on ~900K LD-pruned SNPs from the HGDP panel. Preprocessing (QC, LD pruning, PCA) used plink2. The K-sweep ran on Expanse via `run_admixture.sh` with 8 concurrent runs x 8 threads each on a 64-core shared node.

Post-processing uses the `snpk` Python package, invoked through `run_analysis.sb`.

The `.bed` file lives on Expanse at `/expanse/lustre/projects/csd940/zalibhai/clustering/`. Local copies of `.bim`, `.fam`, and metadata are in `inputs/`.

## Tools

All third-party binaries live in `tools/`:

- ADMIXTURE 1.3.0 (macOS) and 1.4.0 (Linux/HPC)
- plink2 (macOS ARM64, 2025-05-04 build)
- STRUCTURE frontend (Java) and distruct 1.1
- frappe (macOS, from the original exercise)

## Cross-method comparison

Compares microsatellite STRUCTURE results against SNP ADMIXTURE results: K-selection (Evanno delta-K vs CV error), AMOVA variance partitioning, heterozygosity-distance gradients, and pairwise Fst matrix correlation. Run on Expanse via `snp_analysis/scripts/run_comparison.sb`, which invokes `python -m snpk.comparison`. Output goes to `comparison_results/`.

Key findings: microsatellites resolve K=4 (continental-level), SNPs resolve K=7 (subcontinental). Both recover the serial founder effect (r = -0.90 and -0.88). Pairwise Fst matrices correlate at r = 0.83 across 51 shared populations.

## References

Papers and source archives are in `docs/`. The primary paper is Rosenberg et al. 2002 (`science.1153717.pdf`), with supplementary materials from Li et al. and the Serre & Paabo 2004 response.
