# structk/__main__.py
import argparse
import csv
import os
import sys


def _print_table(summaries):
    header = f"{'K':>3}  {'Mean L(K)':>12}  {'Stdev':>10}  {'L\'(K)':>12}  {'L\'\'(K)':>12}  {'Delta K':>10}"
    print(header)
    print("-" * len(header))
    for s in summaries:
        lp = f"{s.ln_prime:12.1f}" if s.ln_prime is not None else f"{'--':>12}"
        lpp = f"{s.ln_double_prime:12.1f}" if s.ln_double_prime is not None else f"{'--':>12}"
        dk = f"{s.delta_k:10.2f}" if s.delta_k is not None else f"{'--':>10}"
        print(f"{s.k:3d}  {s.mean_ln_prob:12.1f}  {s.std_ln_prob:10.1f}  {lp}  {lpp}  {dk}")


def _write_csv(summaries, output_path):
    with open(output_path, "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["K", "Mean_LnPD", "Stdev_LnPD", "Ln_prime_K", "Ln_double_prime_K", "Delta_K"])
        for s in summaries:
            writer.writerow([
                s.k,
                f"{s.mean_ln_prob:.1f}",
                f"{s.std_ln_prob:.1f}",
                f"{s.ln_prime:.1f}" if s.ln_prime is not None else "",
                f"{s.ln_double_prime:.1f}" if s.ln_double_prime is not None else "",
                f"{s.delta_k:.4f}" if s.delta_k is not None else "",
            ])


def main():
    p = argparse.ArgumentParser(
        prog="structk",
        description="K-selection analysis for STRUCTURE (elbow + Evanno delta-K)",
        epilog=(
            "Example with HGDP exercise data:\n"
            "  python -m structk \\\n"
            "    --structure-bin ./structure \\\n"
            "    -i structureexercisedata/diversitydata.str \\\n"
            "    -m mainparams -e extraparams \\\n"
            "    --k-min 1 --k-max 7 --reps 5 --threads 4\n"
        ),
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    p.add_argument("--structure-bin", required=True, help="Path to compiled STRUCTURE binary")
    p.add_argument("--input", "-i", required=True, help="STRUCTURE input data file")
    p.add_argument("--mainparams", "-m", required=True, help="Path to mainparams file")
    p.add_argument("--extraparams", "-e", required=True, help="Path to extraparams file")
    p.add_argument("--k-min", type=int, default=1, help="Minimum K to test (default: 1)")
    p.add_argument("--k-max", type=int, required=True, help="Maximum K to test")
    p.add_argument("--reps", type=int, default=10, help="Replicates per K (default: 10)")
    p.add_argument("--threads", type=int, default=1, help="Parallel STRUCTURE processes (default: 1)")
    p.add_argument("--seed", type=int, default=None, help="Base random seed for reproducibility")
    p.add_argument("--output-dir", "-o", default="structk_output", help="Output directory (default: structk_output)")
    p.add_argument("--plot-format", choices=["png", "pdf"], default="png", help="Plot format (default: png)")
    p.add_argument("--skip-run", action="store_true", help="Skip running STRUCTURE; parse existing results in output-dir")

    args = p.parse_args()

    if args.k_min < 1:
        p.error("--k-min must be >= 1")
    if args.k_max <= args.k_min:
        p.error("--k-max must be > --k-min")
    if args.reps < 1:
        p.error("--reps must be >= 1")
    if args.reps < 2:
        print("WARNING: delta-K requires >= 2 replicates (stdev is undefined with 1).", file=sys.stderr)
    if args.k_max - args.k_min < 2:
        print("WARNING: delta-K requires >= 3 K values to compute.", file=sys.stderr)

    if not args.skip_run:
        if not os.path.isfile(args.structure_bin) and not _which(args.structure_bin):
            p.error(f"STRUCTURE binary not found: {args.structure_bin}")
        for label, path in [("input", args.input), ("mainparams", args.mainparams), ("extraparams", args.extraparams)]:
            if not os.path.isfile(path):
                p.error(f"{label} file not found: {path}")

    os.makedirs(args.output_dir, exist_ok=True)

    if not args.skip_run:
        from .runner import run_structure_sweep
        successes, failures = run_structure_sweep(
            structure_bin=os.path.abspath(args.structure_bin),
            input_file=os.path.abspath(args.input),
            mainparams=os.path.abspath(args.mainparams),
            extraparams=os.path.abspath(args.extraparams),
            k_min=args.k_min,
            k_max=args.k_max,
            reps=args.reps,
            output_dir=os.path.abspath(args.output_dir),
            threads=args.threads,
            base_seed=args.seed,
        )
        if not successes:
            print("ERROR: all STRUCTURE runs failed. Cannot proceed.", file=sys.stderr)
            sys.exit(1)

    from .parser import parse_output_dir
    results, parse_errors = parse_output_dir(os.path.abspath(args.output_dir))

    if parse_errors:
        print(f"\nWARNING: {len(parse_errors)} files failed to parse:", file=sys.stderr)
        for k, rep, err in parse_errors[:5]:
            print(f"  K={k} rep={rep}: {err}", file=sys.stderr)

    if not results:
        print("ERROR: no results to analyze.", file=sys.stderr)
        sys.exit(1)

    from .analysis import compute_evanno
    summaries = compute_evanno(results)

    print()
    _print_table(summaries)
    print()

    csv_path = os.path.join(args.output_dir, "evanno_summary.csv")
    _write_csv(summaries, csv_path)
    print(f"CSV written to: {csv_path}")

    from .plotting import plot_both
    plot_both(summaries, args.output_dir, fmt=args.plot_format)
    print(f"Plots written to: {args.output_dir}/elbow.{args.plot_format}, {args.output_dir}/delta_k.{args.plot_format}")

    best = [s for s in summaries if s.delta_k is not None]
    if best:
        peak = max(best, key=lambda s: s.delta_k)
        print(f"\nBest K by Evanno delta-K: {peak.k} (delta K = {peak.delta_k:.2f})")


def _which(name):
    import shutil
    return shutil.which(name)


if __name__ == "__main__":
    main()
