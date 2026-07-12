# snpk/psam.py
from dataclasses import dataclass
from collections import defaultdict

from structk.genotypes import REGION_ORDER, REGION_COLORS  # noqa: F401


@dataclass
class SampleInfo:
    iid: str
    sex: int
    pop_name: str
    latitude: float
    longitude: float
    region: str


def read_psam(path):
    """Parse a PLINK2 .psam file into {IID: SampleInfo}."""
    samples = {}
    with open(path) as f:
        header = f.readline().strip().split("\t")
        col = {name.lstrip("#"): i for i, name in enumerate(header)}
        for line in f:
            parts = line.strip().split("\t")
            iid = parts[col["IID"]]
            samples[iid] = SampleInfo(
                iid=iid,
                sex=int(parts[col["SEX"]]),
                pop_name=parts[col["population"]],
                latitude=float(parts[col["latitude"]]),
                longitude=float(parts[col["longitude"]]),
                region=parts[col["region"]],
            )
    return samples


def pop_to_region(psam):
    """Return {pop_name: region} from psam data."""
    mapping = {}
    for s in psam.values():
        mapping[s.pop_name] = s.region
    return mapping


def pop_coords(psam):
    """Return {pop_name: (mean_lat, mean_lon)} from psam data."""
    lat_acc = defaultdict(list)
    lon_acc = defaultdict(list)
    for s in psam.values():
        lat_acc[s.pop_name].append(s.latitude)
        lon_acc[s.pop_name].append(s.longitude)
    return {
        p: (sum(lat_acc[p]) / len(lat_acc[p]), sum(lon_acc[p]) / len(lon_acc[p]))
        for p in lat_acc
    }


def pop_iids(psam):
    """Return {pop_name: [iid, ...]} preserving insertion order."""
    groups = defaultdict(list)
    for s in psam.values():
        groups[s.pop_name].append(s.iid)
    return dict(groups)
