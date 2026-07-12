#!/bin/bash
#SBATCH --job-name=admixture_ksweep
#SBATCH --output=logs/admixture_ksweep_%j.out
#SBATCH --partition=shared
#SBATCH --nodes=1
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=64
#SBATCH --mem=128G
#SBATCH --time=48:00:00
#SBATCH --account=csd940
# Run ADMIXTURE K-sweep: K=1..10, 5 replicates each, with cross-validation

echo "======================================"
echo "ADMIXTURE K-sweep (K=1..10, 5 reps)"
echo "======================================"
echo "Started: $(date)"
echo ""

source ~/.bashrc
conda activate mariner_env

WORK_DIR="/expanse/lustre/projects/csd940/zalibhai/clustering"
ADMIXTURE_BIN="${WORK_DIR}/admixture_linux-1.4.0/admixture"
INPUT="${WORK_DIR}/hgdp_pruned.bed"
OUTPUT_DIR="${WORK_DIR}/snpk_output"
MAX_PARALLEL=8   # 8 concurrent x 8 threads each = 64 CPUs total

cd "${WORK_DIR}"
mkdir -p "${OUTPUT_DIR}" logs

echo "Input: ${INPUT}"
echo "Output: ${OUTPUT_DIR}"
echo "ADMIXTURE binary: ${ADMIXTURE_BIN}"
echo "Parallelism: ${MAX_PARALLEL} concurrent runs x 8 threads"
echo ""

# --- Verify inputs exist ---
if [ ! -f "${INPUT}" ]; then
    echo "ERROR: Input file not found: ${INPUT}"
    exit 1
fi
if [ ! -x "${ADMIXTURE_BIN}" ]; then
    echo "ERROR: ADMIXTURE binary not found or not executable: ${ADMIXTURE_BIN}"
    exit 1
fi

N_SNPS=$(wc -l < "${WORK_DIR}/hgdp_pruned.bim")
N_IND=$(wc -l < "${WORK_DIR}/hgdp_pruned.fam")
echo "Dataset: ${N_IND} individuals, ${N_SNPS} SNPs"
echo ""

# --- Run ADMIXTURE for all K x rep combinations ---
echo "=== Starting ADMIXTURE runs ==="
for K in $(seq 1 10); do
    for REP in $(seq 1 5); do
        SEED=$((42 + K * 1000 + REP))
        RUNDIR="${OUTPUT_DIR}/K${K}_rep${REP}"
        mkdir -p "${RUNDIR}"

        echo "[$(date +%H:%M:%S)] Launching K=${K} rep=${REP} seed=${SEED}"
        (cd "${RUNDIR}" && "${ADMIXTURE_BIN}" --cv -s "${SEED}" -j8 "${INPUT}" "${K}") \
            > "${OUTPUT_DIR}/K${K}_rep${REP}.log" 2>&1 &

        # Throttle: wait if we've hit the concurrency cap
        while [ "$(jobs -rp | wc -l)" -ge "${MAX_PARALLEL}" ]; do
            sleep 30
        done
    done
done

echo ""
echo "All jobs launched. Waiting for final batch..."
wait

echo ""
echo "======================================"
echo "=== CV Error Summary ==="
echo "======================================"
grep "CV error" "${OUTPUT_DIR}"/*.log | sort -t= -k2 -n

echo ""
echo "=== Log-likelihood Summary ==="
for K in $(seq 1 10); do
    for REP in $(seq 1 5); do
        LOG="${OUTPUT_DIR}/K${K}_rep${REP}.log"
        if [ -f "${LOG}" ]; then
            LL=$(grep "Loglikelihood:" "${LOG}" | tail -1 | awk '{print $2}')
            CV=$(grep "CV error" "${LOG}" | awk '{print $NF}')
            echo "K=${K} rep=${REP}: LL=${LL} CV=${CV}"
        fi
    done
done

echo ""
echo "Finished: $(date)"
