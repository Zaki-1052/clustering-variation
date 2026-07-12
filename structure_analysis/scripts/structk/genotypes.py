# structk/genotypes.py
import re
import numpy as np
from dataclasses import dataclass, field
from pathlib import Path


@dataclass
class PopInfo:
    pop_id: int
    pop_name: str
    location: str
    region: str


@dataclass
class Individual:
    indiv_id: int
    pop_id: int
    pop_name: str
    country: str
    region: str
    alleles: np.ndarray  # shape (2, n_loci), dtype=int16


@dataclass
class GenotypeData:
    marker_names: list
    individuals: list
    pop_info: dict = field(default_factory=dict)


REGION_ORDER = [
    "AFRICA", "MIDDLE_EAST", "EUROPE",
    "CENTRAL_SOUTH_ASIA", "EAST_ASIA", "OCEANIA", "AMERICA",
]

REGION_COLORS = {
    "AFRICA": "#E41A1C",
    "MIDDLE_EAST": "#FF7F00",
    "EUROPE": "#377EB8",
    "CENTRAL_SOUTH_ASIA": "#4DAF4A",
    "EAST_ASIA": "#984EA3",
    "OCEANIA": "#A65628",
    "AMERICA": "#F781BF",
}


def read_names(path):
    pop_info = {}
    with open(path) as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            parts = line.split()
            if len(parts) < 4:
                continue
            pop_id = int(parts[0])
            pop_info[pop_id] = PopInfo(
                pop_id=pop_id,
                pop_name=parts[1],
                location=parts[2],
                region=parts[3],
            )
    return pop_info


def read_genotypes(geno_path, names_path):
    pop_info = read_names(names_path)

    with open(geno_path) as f:
        lines = f.readlines()

    marker_names = lines[0].strip().split()
    n_loci = len(marker_names)

    individuals = []
    data_lines = lines[1:]
    for i in range(0, len(data_lines), 2):
        row1 = data_lines[i].strip().split()
        row2 = data_lines[i + 1].strip().split()

        indiv_id = int(row1[0])
        pop_id = int(row1[1])
        pop_name = row1[2]
        country = row1[3]
        region = row1[4]

        allele1 = np.array([int(x) for x in row1[5:5 + n_loci]], dtype=np.int16)
        allele2 = np.array([int(x) for x in row2[5:5 + n_loci]], dtype=np.int16)
        alleles = np.stack([allele1, allele2])

        individuals.append(Individual(
            indiv_id=indiv_id,
            pop_id=pop_id,
            pop_name=pop_name,
            country=country,
            region=region,
            alleles=alleles,
        ))

    return GenotypeData(
        marker_names=marker_names,
        individuals=individuals,
        pop_info=pop_info,
    )


_Q_LINE = re.compile(r"\s*(\d+)\s+(\d+)\s+\(\s*\d+\)\s+(\d+)\s*:\s+([\d.\s]+)")


def read_qmatrix(structure_output_path):
    q_rows = []
    indiv_ids = []
    pop_ids = []
    in_section = False

    with open(structure_output_path) as f:
        for line in f:
            if "Inferred ancestry of individuals:" in line:
                in_section = True
                continue
            if in_section and "Label" in line and "Inferred clusters" in line:
                continue
            if in_section:
                if not line.strip():
                    if q_rows:
                        break
                    continue
                m = _Q_LINE.match(line)
                if m:
                    indiv_ids.append(int(m.group(2)))
                    pop_ids.append(int(m.group(3)))
                    vals = [float(x) for x in m.group(4).strip().split()]
                    q_rows.append(vals)

    Q = np.array(q_rows)
    return Q, indiv_ids, pop_ids


def find_best_replicate(output_dir, k):
    output_dir = Path(output_dir)
    pattern = re.compile(r"Estimated Ln Prob of Data\s+=\s+([-\d.]+)")
    best_path = None
    best_lnp = -np.inf

    for path in sorted(output_dir.glob(f"k{k}_r*_f")):
        with open(path) as f:
            for line in f:
                m = pattern.search(line)
                if m:
                    lnp = float(m.group(1))
                    if lnp > best_lnp:
                        best_lnp = lnp
                        best_path = str(path)
                    break

    if best_path is None:
        raise FileNotFoundError(f"No output files found for K={k} in {output_dir}")
    return best_path
