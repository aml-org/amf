#!/usr/bin/env python3
"""
Reads schemas.yaml (next to this script) and invokes bundle_schema.py for each
entry to produce bundled JSON Schema files.

Usage:
    python3 bundle_all_schemas.py
"""

import os
import subprocess
import sys

import yaml

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))
CONFIG_FILE = os.path.join(SCRIPT_DIR, "schemas.yaml")
BUNDLE_SCRIPT = os.path.join(SCRIPT_DIR, "bundle_schema.py")


def main():
    with open(CONFIG_FILE, "r") as f:
        entries = yaml.safe_load(f)

    if not isinstance(entries, list):
        print(f"ERROR: {CONFIG_FILE} must contain a YAML array.", file=sys.stderr)
        sys.exit(1)

    failed = []
    for entry in entries:
        name = entry["name"]
        root_schema = entry["rootSchema"]
        output = entry["output"]

        # Resolve rootSchema as absolute path (expand ~)
        root_schema = os.path.abspath(os.path.expanduser(root_schema))

        # Resolve output relative to the project root
        output = os.path.join(PROJECT_ROOT, output)

        print(f"\n{'='*60}")
        print(f"Bundling: {name}")
        print(f"  root:   {root_schema}")
        print(f"  output: {output}")
        print(f"{'='*60}")

        result = subprocess.run(
            [sys.executable, BUNDLE_SCRIPT, root_schema, output],
            cwd=PROJECT_ROOT,
        )

        if result.returncode != 0:
            failed.append(name)

    print()
    if failed:
        print(f"FAILED ({len(failed)}/{len(entries)}): {', '.join(failed)}")
        sys.exit(1)
    else:
        print(f"All {len(entries)} schema(s) bundled successfully.")


if __name__ == "__main__":
    main()
