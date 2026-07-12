# structk/parser.py
import re
from dataclasses import dataclass

@dataclass
class StructureResult:
    k: int
    rep: int
    ln_prob_data: float
    mean_ln_likelihood: float
    variance_ln_likelihood: float
    filepath: str


_PATTERNS = {
    "ln_prob_data": re.compile(r"Estimated Ln Prob of Data\s+=\s+([-\d.]+)"),
    "mean_ln_likelihood": re.compile(r"Mean value of ln likelihood\s+=\s+([-\d.]+)"),
    "variance_ln_likelihood": re.compile(r"Variance of ln likelihood\s+=\s+([-\d.]+)"),
}


def parse_output(filepath, k, rep):
    found = {}
    with open(filepath) as f:
        for line in f:
            for key, pattern in _PATTERNS.items():
                if key not in found:
                    m = pattern.search(line)
                    if m:
                        found[key] = float(m.group(1))
            if len(found) == len(_PATTERNS):
                break

    missing = set(_PATTERNS) - set(found)
    if missing:
        raise ValueError(
            f"{filepath}: could not find {', '.join(sorted(missing))}. "
            f"Check that COMPUTEPROB=1 and NUMREPS > BURNIN + 2."
        )

    return StructureResult(
        k=k,
        rep=rep,
        ln_prob_data=found["ln_prob_data"],
        mean_ln_likelihood=found["mean_ln_likelihood"],
        variance_ln_likelihood=found["variance_ln_likelihood"],
        filepath=filepath,
    )


def parse_output_dir(output_dir):
    """Scan an output directory for STRUCTURE result files and parse them."""
    from pathlib import Path

    pattern = re.compile(r"^k(\d+)_r(\d+)_f$")
    results = []
    errors = []

    for path in sorted(Path(output_dir).iterdir()):
        m = pattern.match(path.name)
        if not m:
            continue
        k, rep = int(m.group(1)), int(m.group(2))
        try:
            results.append(parse_output(str(path), k, rep))
        except ValueError as e:
            errors.append((k, rep, str(e)))

    return results, errors
