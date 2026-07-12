# snpk/admixture.py
import re
import numpy as np
from pathlib import Path

_CV_PATTERN = re.compile(r"CV error \(K=(\d+)\):\s+([\d.]+)")
_LL_PATTERN = re.compile(r"Loglikelihood:\s+([-\d.]+)")


def read_q(path):
    """Read an ADMIXTURE .Q file. Returns ndarray of shape (n_ind, K)."""
    return np.loadtxt(path)


def parse_cv_error(log_path):
    """Extract CV error from an ADMIXTURE log file."""
    with open(log_path) as f:
        for line in f:
            m = _CV_PATTERN.search(line)
            if m:
                return int(m.group(1)), float(m.group(2))
    raise ValueError(f"No CV error found in {log_path}")


def parse_loglikelihood(log_path):
    """Extract final log-likelihood from an ADMIXTURE log file."""
    ll = None
    with open(log_path) as f:
        for line in f:
            m = _LL_PATTERN.search(line)
            if m:
                ll = float(m.group(1))
    if ll is None:
        raise ValueError(f"No log-likelihood found in {log_path}")
    return ll


def scan_runs(output_dir):
    """Scan snpk_output/ directory for ADMIXTURE results.

    Expects structure: output_dir/K{k}_rep{r}/ containing .Q files and
    output_dir/K{k}_rep{r}.log containing CV error.

    Returns {K: [{'rep': int, 'q_path': str, 'log_path': str,
                   'cv_error': float, 'loglik': float}, ...]}
    """
    output_dir = Path(output_dir)
    results = {}

    for log_path in sorted(output_dir.glob("K*_rep*.log")):
        m = re.match(r"K(\d+)_rep(\d+)\.log", log_path.name)
        if not m:
            continue
        k = int(m.group(1))
        rep = int(m.group(2))

        run_dir = output_dir / f"K{k}_rep{rep}"
        q_files = list(run_dir.glob("*.Q")) if run_dir.is_dir() else []
        q_path = str(q_files[0]) if q_files else None

        try:
            _, cv_error = parse_cv_error(str(log_path))
        except ValueError:
            cv_error = None
        try:
            loglik = parse_loglikelihood(str(log_path))
        except ValueError:
            loglik = None

        entry = {
            "rep": rep,
            "q_path": q_path,
            "log_path": str(log_path),
            "cv_error": cv_error,
            "loglik": loglik,
        }
        results.setdefault(k, []).append(entry)

    for k in results:
        results[k].sort(key=lambda e: e["rep"])

    return results


def find_best_replicate(output_dir, k):
    """Return path to .Q file of replicate with highest log-likelihood."""
    runs = scan_runs(output_dir)
    if k not in runs:
        raise FileNotFoundError(f"No ADMIXTURE runs found for K={k} in {output_dir}")

    best = max(
        (r for r in runs[k] if r["loglik"] is not None and r["q_path"] is not None),
        key=lambda r: r["loglik"],
    )
    return best["q_path"]
