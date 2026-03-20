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
CONFIG_FILE = os.path.join(SCRIPT_DIR, "schemas.yaml")
BUNDLE_SCRIPT = os.path.join(SCRIPT_DIR, "bundle_schema.py")


def find_asset_file(directory):
    """Find the asset.* file in a directory. Returns the path or None."""
    matches = glob.glob(os.path.join(directory, "asset.*"))
    if len(matches) == 1:
        return matches[0]
    if len(matches) > 1:
        print(f"  WARNING: multiple asset.* files in {directory}, using first: {matches[0]}", file=sys.stderr)
        return matches[0]
    return None


def copy_file(src, dst_dir, filename=None):
    """Copy a single file into dst_dir, optionally renaming it. Returns True on success."""
    if not os.path.isfile(src):
        print(f"  ERROR: source file not found: {src}", file=sys.stderr)
        return False
    target = os.path.join(dst_dir, filename or os.path.basename(src))
    os.makedirs(dst_dir, exist_ok=True)
    shutil.copy2(src, target)
    print(f"    {src} -> {target}")
    return True


def update_amf_instances(name, spec_instances, amf_instances, spec_root, amf_root):
    """Copy source asset.* files into the AMF module's test resources."""
    ok = True
    for kind in ("valid", "invalid"):
        src_rel = spec_instances.get(kind)
        dst_rel = amf_instances.get(kind)
        if not src_rel or not dst_rel:
            continue

        src_dir = os.path.join(spec_root, src_rel)
        src_file = find_asset_file(src_dir)
        if not src_file:
            print(f"  ERROR: no asset.* file found in {src_dir}", file=sys.stderr)
            ok = False
            continue

        dst_dir = os.path.join(amf_root, dst_rel)
        if not copy_file(src_file, dst_dir):
            ok = False

    return ok


def update_apb_instances(name, spec_instances, apb_instances, spec_root, apb_root):
    """Copy source asset.* and exchange.json files into APB test resources."""
    ok = True
    for kind in ("valid", "invalid"):
        src_rel = spec_instances.get(kind)
        dst_entries = apb_instances.get(kind)
        if not src_rel or not dst_entries:
            continue

        src_dir = os.path.join(spec_root, src_rel)

        # Find source files
        asset_file = find_asset_file(src_dir)
        exchange_file = os.path.join(src_dir, "exchange.json")

        if not asset_file:
            print(f"  ERROR: no asset.* file found in {src_dir}", file=sys.stderr)
            ok = False
            continue
        if not os.path.isfile(exchange_file):
            print(f"  ERROR: exchange.json not found in {src_dir}", file=sys.stderr)
            ok = False
            continue

        # Normalize dst_entries to a list
        if isinstance(dst_entries, str):
            dst_entries = [dst_entries]

        for dst_rel in dst_entries:
            dst_dir = os.path.join(apb_root, dst_rel)
            if not copy_file(asset_file, dst_dir):
                ok = False
            if not copy_file(exchange_file, dst_dir):
                ok = False

    return ok


def main():
    with open(CONFIG_FILE, "r") as f:
        config = yaml.safe_load(f)

    repos = config.get("repositories", {})
    specs = config.get("specs", [])

    if not isinstance(specs, list):
        print(f"ERROR: 'specs' in {CONFIG_FILE} must be a list.", file=sys.stderr)
        sys.exit(1)

    # Resolve repository root paths
    amf_root = os.path.abspath(os.path.expanduser(repos["amfLocalPath"]))
    apb_root = os.path.abspath(os.path.expanduser(repos["apbLocalPath"]))
    spec_root = os.path.abspath(os.path.expanduser(repos["specLocalPath"]))

    failed = []
    for entry in specs:
        name = entry["name"]
        module = entry["amf-module"]
        schema = entry["schema"]

        # Resolve schema paths
        root_schema = os.path.join(spec_root, schema["spec"])
        output = os.path.join(amf_root, module, schema["amf"])

        print(f"\n{'='*60}")
        print(f"Bundling: {name}")
        print(f"  root:   {root_schema}")
        print(f"  output: {output}")
        print(f"{'='*60}")

        result = subprocess.run(
            [sys.executable, BUNDLE_SCRIPT, root_schema, output],
            cwd=amf_root,
        )

        if result.returncode != 0:
            failed.append(name)
            continue

        # Update instances
        instances = entry.get("instances", {})
        spec_instances = instances.get("spec", {})
        amf_instances = instances.get("amf", {})
        apb_instances = instances.get("apb", {})
        module_root = os.path.join(amf_root, module)

        if spec_instances and amf_instances:
            print(f"Updating AMF instances: {name}")
            if not update_amf_instances(name, spec_instances, amf_instances, spec_root, module_root):
                failed.append(name)

        if spec_instances and apb_instances:
            print(f"Updating APB instances: {name}")
            if not update_apb_instances(name, spec_instances, apb_instances, spec_root, apb_root):
                failed.append(name)

    print()
    if failed:
        print(f"FAILED ({len(failed)}/{len(specs)}): {', '.join(failed)}")
        sys.exit(1)
    else:
        print(f"All {len(specs)} schema(s) bundled and instances updated successfully.")


if __name__ == "__main__":
    main()
