#!/usr/bin/env python3
"""
Reads schemas.yaml (next to this script) and:
  1. Invokes bundle_schema.py for each entry to produce bundled JSON Schema files.
  2. Copies source test instances (valid/invalid asset files) into each module's
     test resources directory.

Usage:
    python3 bundle_all_schemas.py
"""

import glob
import os
import shutil
import subprocess
import sys

import yaml

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", "..", ".."))
CONFIG_FILE = os.path.join(SCRIPT_DIR, "schemas.yaml")
BUNDLE_SCRIPT = os.path.join(SCRIPT_DIR, "bundle_schema.py")

INSTANCES_REL_PATH = "shared/src/test/resources/instances"


def find_asset_file(directory):
    """Find the asset.* file in a directory. Returns the path or None."""
    matches = glob.glob(os.path.join(directory, "asset.*"))
    if len(matches) == 1:
        return matches[0]
    if len(matches) > 1:
        print(f"  WARNING: multiple asset.* files in {directory}, using first: {matches[0]}", file=sys.stderr)
        return matches[0]
    return None


def update_instances(entry):
    """Copy source instance files into the module's test resources."""
    name = entry["name"]
    module = entry.get("module")
    instances = entry.get("instances")

    if not module or not instances:
        print(f"  Skipping instance update for '{name}': missing module or instances config")
        return True

    target_base = os.path.join(PROJECT_ROOT, module, INSTANCES_REL_PATH)

    ok = True
    for kind in ("valid", "invalid"):
        src_dir = instances.get(kind)
        if not src_dir:
            continue
        src_dir = os.path.abspath(os.path.expanduser(src_dir))
        src_file = find_asset_file(src_dir)
        if not src_file:
            print(f"  ERROR: no asset.* file found in {src_dir}", file=sys.stderr)
            ok = False
            continue

        # Target file has the same name (asset.json or asset.yaml)
        target_file = os.path.join(target_base, kind, os.path.basename(src_file))
        os.makedirs(os.path.dirname(target_file), exist_ok=True)
        shutil.copy2(src_file, target_file)
        print(f"  {kind}: {src_file} -> {target_file}")

    return ok


def main():
    with open(CONFIG_FILE, "r") as f:
        entries = yaml.safe_load(f)

    if not isinstance(entries, list):
        print(f"ERROR: {CONFIG_FILE} must contain a YAML array.", file=sys.stderr)
        sys.exit(1)

    failed = []
    for entry in entries:
        name = entry["name"]
        module = entry["module"]
        root_schema = entry["rootSchema"]
        output = entry["output"]

        # Resolve rootSchema as absolute path (expand ~)
        root_schema = os.path.abspath(os.path.expanduser(root_schema))

        # Resolve output relative to the module inside the project root
        output = os.path.join(PROJECT_ROOT, module, output)

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
            continue

        print(f"Updating instances: {name}")
        if not update_instances(entry):
            failed.append(name)

    print()
    if failed:
        print(f"FAILED ({len(failed)}/{len(entries)}): {', '.join(failed)}")
        sys.exit(1)
    else:
        print(f"All {len(entries)} schema(s) bundled and instances updated successfully.")


if __name__ == "__main__":
    main()
