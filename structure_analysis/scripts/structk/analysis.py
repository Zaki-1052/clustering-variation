# structk/analysis.py
import math
from dataclasses import dataclass
from collections import defaultdict


@dataclass
class KSummary:
    k: int
    mean_ln_prob: float
    std_ln_prob: float
    ln_prime: float | None
    ln_double_prime: float | None
    delta_k: float | None


def _mean(xs):
    return math.fsum(xs) / len(xs)


def _stdev(xs):
    if len(xs) < 2:
        return None
    m = _mean(xs)
    ss = math.fsum((x - m) ** 2 for x in xs)
    return math.sqrt(ss / (len(xs) - 1))


def compute_evanno(results):
    by_k = defaultdict(list)
    for r in results:
        by_k[r.k].append(r.ln_prob_data)

    ks = sorted(by_k)
    means = {k: _mean(by_k[k]) for k in ks}
    stds = {k: _stdev(by_k[k]) for k in ks}

    ln_prime = {}
    for i in range(1, len(ks)):
        ln_prime[ks[i]] = means[ks[i]] - means[ks[i - 1]]

    ln_double_prime = {}
    for i in range(1, len(ks) - 1):
        k = ks[i]
        ln_double_prime[k] = ln_prime[ks[i + 1]] - ln_prime[k]

    summaries = []
    for k in ks:
        lpp = ln_double_prime.get(k)
        sd = stds[k]

        if lpp is not None and sd is not None and sd > 0:
            dk = abs(lpp) / sd
        else:
            dk = None

        summaries.append(KSummary(
            k=k,
            mean_ln_prob=means[k],
            std_ln_prob=sd if sd is not None else 0.0,
            ln_prime=ln_prime.get(k),
            ln_double_prime=lpp,
            delta_k=dk,
        ))

    return summaries
