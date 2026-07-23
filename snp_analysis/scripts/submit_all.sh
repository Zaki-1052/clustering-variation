#!/bin/bash
# submit_all.sh — submit all analysis jobs in parallel
#
# Dependency graph (all independent, no job depends on another):
#
#   run_structk.sb        structk: amova, het, ibd, barplot, combined  (~1-2h)
#   run_snpk_light.sb     snpk: cv, pca, barplot, het                  (~1-4h)
#   run_snpk_amova.sb     snpk: AMOVA only (999 perms, 10.7M SNPs)     (~24-48h)
#   run_snpk_ibd.sb       snpk: pairwise Fst + Mantel                  (~12-24h)
#   run_snpk_combined.sb  snpk: combined figure (recomputes internally) (~24-48h)
#   run_comparison.sb     cross-method comparison (recomputes both)     (~24-48h)
#
# The three heavy jobs (amova, combined, comparison) each independently
# load the .bed file and recompute AMOVA with 999 permutations. This is
# redundant but they produce different outputs and the architecture
# doesn't share pre-computed results. Parallelizing is still faster than
# running the old sequential pipeline.

#set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "${SCRIPT_DIR}"

mkdir -p logs

echo "Submitting all analysis jobs..."
echo ""

JOB1=$(sbatch --parsable run_structk.sb)
echo "  structk_all        ${JOB1}  (amova+het+ibd+barplot+combined)"

JOB2=$(sbatch --parsable run_snpk_light.sb)
echo "  snpk_light         ${JOB2}  (cv+pca+barplot+het)"

JOB3=$(sbatch --parsable run_snpk_amova.sb)
echo "  snpk_amova         ${JOB3}  (999 perms, longest job)"

JOB4=$(sbatch --parsable run_snpk_ibd.sb)
echo "  snpk_ibd           ${JOB4}  (pairwise Fst + Mantel)"

JOB5=$(sbatch --parsable run_snpk_combined.sb)
echo "  snpk_combined      ${JOB5}  (combined figure)"

JOB6=$(sbatch --parsable run_comparison.sb)
echo "  comparison         ${JOB6}  (cross-method comparison)"

echo ""
echo "All 6 jobs submitted. Monitor with:"
echo "  squeue -u \$USER"
echo "  tail -f logs/*.out"
