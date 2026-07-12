# snpk/bed_io.py
import numpy as np
from dataclasses import dataclass


@dataclass
class PlinkData:
    genotypes: np.ndarray   # (n_ind, n_snps), int8: 0/1/2 = dosage, -9 = missing
    iids: list              # individual IDs from .fam
    snp_ids: list           # from .bim col 2
    chroms: list            # from .bim col 1
    positions: list         # from .bim col 4
    allele1: list           # from .bim col 5 (usually minor)
    allele2: list           # from .bim col 6 (usually major)


def _read_fam(path):
    iids = []
    with open(path) as f:
        for line in f:
            parts = line.strip().split()
            iids.append(parts[1])
    return iids


def _read_bim(path):
    chroms, snp_ids, positions, a1, a2 = [], [], [], [], []
    with open(path) as f:
        for line in f:
            parts = line.strip().split("\t")
            chroms.append(parts[0])
            snp_ids.append(parts[1])
            positions.append(int(parts[3]))
            a1.append(parts[4])
            a2.append(parts[5])
    return chroms, snp_ids, positions, a1, a2


_GENO_LOOKUP = np.array([2, -9, 1, 0], dtype=np.int8)


def read_bed(prefix):
    """Read PLINK .bed/.bim/.fam triplet.

    Returns PlinkData with genotypes as (n_ind, n_snps) int8 array.
    Encoding: 0 = homozygous A1, 1 = het, 2 = homozygous A2, -9 = missing.
    """
    iids = _read_fam(prefix + ".fam")
    chroms, snp_ids, positions, a1, a2 = _read_bim(prefix + ".bim")

    n_ind = len(iids)
    n_snp = len(snp_ids)
    bytes_per_snp = (n_ind + 3) // 4

    with open(prefix + ".bed", "rb") as f:
        magic = f.read(3)
        if magic[:2] != b"\x6c\x1b":
            raise ValueError("Not a valid PLINK .bed file (bad magic bytes)")
        if magic[2] != 1:
            raise ValueError(".bed file is in individual-major mode; need SNP-major")
        raw = np.frombuffer(f.read(), dtype=np.uint8)

    expected = bytes_per_snp * n_snp
    if len(raw) != expected:
        raise ValueError(f"Expected {expected} bytes, got {len(raw)}")

    raw = raw.reshape(n_snp, bytes_per_snp)

    unpacked = np.empty((n_snp, bytes_per_snp * 4), dtype=np.int8)
    for shift in range(4):
        two_bits = (raw >> (2 * shift)) & 0x03
        unpacked[:, shift::4] = _GENO_LOOKUP[two_bits]

    geno = unpacked[:, :n_ind].T

    return PlinkData(
        genotypes=geno,
        iids=iids,
        snp_ids=snp_ids,
        chroms=chroms,
        positions=positions,
        allele1=a1,
        allele2=a2,
    )
