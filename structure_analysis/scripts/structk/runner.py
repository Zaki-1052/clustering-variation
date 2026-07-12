# structk/runner.py
import os
import re
import sys
import random
import subprocess
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed


def _prepare_extraparams(extraparams_path, output_dir):
    """Copy extraparams with RANDOMIZE forced to 0."""
    with open(extraparams_path) as f:
        content = f.read()

    content = re.sub(
        r"(#define\s+RANDOMIZE\s+)\d+",
        r"\g<1>0",
        content,
    )

    modified_path = os.path.join(output_dir, "_extraparams_modified")
    with open(modified_path, "w") as f:
        f.write(content)

    return modified_path


def _run_single(structure_bin, input_file, mainparams, extraparams, k, rep, output_dir, seed):
    out_prefix = os.path.join(output_dir, f"k{k}_r{rep}")
    cmd = [
        structure_bin,
        "-K", str(k),
        "-i", input_file,
        "-o", out_prefix,
        "-m", mainparams,
        "-e", extraparams,
        "-D", str(seed),
    ]

    result = subprocess.run(
        cmd,
        capture_output=True,
        text=True,
        cwd=output_dir,
    )

    if result.returncode != 0:
        raise RuntimeError(
            f"STRUCTURE failed (K={k}, rep={rep}, seed={seed}): "
            f"exit code {result.returncode}\n"
            f"stderr: {result.stderr[-500:] if result.stderr else '(empty)'}"
        )

    output_file = f"{out_prefix}_f"
    if not os.path.exists(output_file):
        raise FileNotFoundError(f"Expected output file not found: {output_file}")

    return output_file


def run_structure_sweep(
    structure_bin,
    input_file,
    mainparams,
    extraparams,
    k_min,
    k_max,
    reps,
    output_dir,
    threads=1,
    base_seed=None,
):
    os.makedirs(output_dir, exist_ok=True)

    if base_seed is None:
        base_seed = random.randrange(2**31)
    print(f"Base seed: {base_seed}", file=sys.stderr)

    with open(os.path.join(output_dir, "seed.log"), "w") as f:
        f.write(f"base_seed={base_seed}\n")

    modified_extraparams = _prepare_extraparams(extraparams, output_dir)

    jobs = []
    for k in range(k_min, k_max + 1):
        for rep in range(1, reps + 1):
            seed = base_seed + k * 10000 + rep
            jobs.append((k, rep, seed))

    total = len(jobs)
    successes = []
    failures = []
    lock = threading.Lock()
    done_count = 0

    def run_job(k, rep, seed):
        return _run_single(
            structure_bin, input_file, mainparams,
            modified_extraparams, k, rep, output_dir, seed,
        )

    with ThreadPoolExecutor(max_workers=threads) as pool:
        futures = {
            pool.submit(run_job, k, rep, seed): (k, rep, seed)
            for k, rep, seed in jobs
        }

        for future in as_completed(futures):
            k, rep, seed = futures[future]
            with lock:
                done_count += 1
            try:
                path = future.result()
                successes.append(path)
                print(
                    f"[{done_count}/{total}] K={k} rep={rep} done (seed={seed})",
                    file=sys.stderr,
                )
            except Exception as e:
                failures.append((k, rep, str(e)))
                print(
                    f"[{done_count}/{total}] K={k} rep={rep} FAILED: {e}",
                    file=sys.stderr,
                )

    if failures:
        print(
            f"\nWARNING: {len(failures)}/{total} runs failed.",
            file=sys.stderr,
        )

    return successes, failures
