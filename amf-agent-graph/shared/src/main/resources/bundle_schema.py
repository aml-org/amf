#!/usr/bin/env python3
"""
Bundles agent_network_v2.json into schema_agent_graph.json by resolving
all external $ref references and inlining them as local #/definitions/... refs.

Usage:
    python3 bundle_schema.py

Reads:  base-schemas-agent-graph/v2/agent_network_v2.json (and all referenced schemas)
Writes: schema_agent_graph.json
"""

import json
import os
import sys
from copy import deepcopy

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
ROOT_SCHEMA = os.path.join(SCRIPT_DIR, "base-schemas-agent-graph", "v2", "agent_network_v2.json")
OUTPUT_FILE = os.path.join(SCRIPT_DIR, "schema_agent_graph.json")

# Cache of loaded schema files (absolute path -> parsed JSON)
_file_cache: dict[str, dict] = {}

# Tracks which file each collected definition originated from
_def_source: dict[str, str] = {}


def load_schema(filepath: str) -> dict:
    """Load and cache a JSON schema file."""
    abs_path = os.path.abspath(filepath)
    if abs_path not in _file_cache:
        with open(abs_path, "r") as f:
            _file_cache[abs_path] = json.load(f)
    return _file_cache[abs_path]


def resolve_file_ref(ref_value: str, current_file: str) -> tuple[str, str]:
    """
    Parse a $ref string and resolve the target file path.
    Returns (def_name, absolute_target_file_path).
    """
    if "#" in ref_value:
        file_part, fragment = ref_value.split("#", 1)
    else:
        raise ValueError(f"Unsupported ref (no #): {ref_value}")

    if file_part:
        current_dir = os.path.dirname(os.path.abspath(current_file))
        target_file = os.path.abspath(os.path.join(current_dir, file_part))
    else:
        target_file = os.path.abspath(current_file)

    if not fragment.startswith("/definitions/"):
        raise ValueError(f"Unsupported ref fragment: {fragment} in {ref_value}")

    def_name = fragment.split("/")[-1]
    return def_name, target_file


def get_definition(def_name: str, filepath: str) -> dict:
    """Get a definition by name from a schema file."""
    schema = load_schema(filepath)
    return deepcopy(schema["definitions"][def_name])


def collect_definitions(root_file: str) -> tuple[dict, dict]:
    """
    Starting from root_file, collect all definitions by recursively resolving
    $ref references. Handles both external refs and local refs from external files.

    Returns:
      - The root schema (with all $refs rewritten to #/definitions/...)
      - A dict of all collected definitions (name -> definition object)
    """
    root_file = os.path.abspath(root_file)
    root_schema = deepcopy(load_schema(root_file))
    all_definitions: dict[str, dict] = {}

    # Seed with the root schema's own definitions
    root_defs = root_schema.pop("definitions", {})
    for name, defn in root_defs.items():
        all_definitions[name] = defn
        _def_source[name] = root_file

    # Track which definitions have been fully processed
    processed: set[str] = set()

    # Maps (original_name, source_file) -> actual_name_in_bundle for renamed defs
    _rename_map: dict[tuple[str, str], str] = {}

    def _make_unique_name(def_name: str) -> str:
        """Generate a unique name for a colliding definition."""
        candidate = f"Node{def_name}"
        i = 2
        while candidate in all_definitions:
            candidate = f"Node{def_name}{i}"
            i += 1
        return candidate

    def ensure_definition(def_name: str, source_file: str) -> str:
        """
        Ensure a definition is collected (pull it from source_file if needed).
        Returns the actual name used in the bundle (may differ if renamed due to collision).
        """
        key = (def_name, source_file)
        if key in _rename_map:
            return _rename_map[key]

        if def_name not in all_definitions:
            all_definitions[def_name] = get_definition(def_name, source_file)
            _def_source[def_name] = source_file
            _rename_map[key] = def_name
            return def_name

        # Definition already exists — check if it's from the same source
        if _def_source.get(def_name) == source_file:
            _rename_map[key] = def_name
            return def_name

        # Name collision from different files — check if content is identical
        existing = all_definitions[def_name]
        incoming = get_definition(def_name, source_file)
        if existing == incoming:
            _rename_map[key] = def_name
            return def_name

        # Genuine collision: rename the incoming definition
        new_name = _make_unique_name(def_name)
        all_definitions[new_name] = incoming
        _def_source[new_name] = source_file
        _rename_map[key] = new_name
        # Also map any local sibling refs from this file to the new name
        return new_name

    def resolve_local_ref(def_name: str, source_file: str) -> str:
        """Resolve a local #/definitions/X ref, accounting for renames."""
        key = (def_name, source_file)
        if key in _rename_map:
            return _rename_map[key]
        # If not yet mapped, ensure it
        return ensure_definition(def_name, source_file)

    def process_obj(obj, source_file: str):
        """
        Recursively process a JSON object:
        - External $refs (file.json#/definitions/X): resolve, collect, rewrite to #/definitions/X
        - Local $refs (#/definitions/X) from non-root files: collect sibling def, keep as #/definitions/X
        - Local $refs (#/definitions/X) from root file: keep as-is
        """
        if isinstance(obj, dict):
            if "$ref" in obj:
                ref_value = obj["$ref"]

                if ref_value.startswith("#/definitions/"):
                    # Local ref
                    def_name = ref_value.split("/")[-1]
                    if source_file != root_file:
                        # This is a local ref inside an external file — pull the sibling def
                        actual_name = resolve_local_ref(def_name, source_file)
                    else:
                        actual_name = def_name
                    # Rewrite with actual name (may have been renamed)
                    new_obj = {k: v for k, v in obj.items() if k != "$ref"}
                    new_obj["$ref"] = f"#/definitions/{actual_name}"
                    return new_obj

                else:
                    # External ref — resolve it
                    def_name, target_file = resolve_file_ref(ref_value, source_file)
                    actual_name = ensure_definition(def_name, target_file)

                    # Rewrite to local ref, preserving sibling properties (like "description")
                    new_obj = {k: v for k, v in obj.items() if k != "$ref"}
                    new_obj["$ref"] = f"#/definitions/{actual_name}"
                    return new_obj
            else:
                return {k: process_obj(v, source_file) for k, v in obj.items()}

        elif isinstance(obj, list):
            return [process_obj(item, source_file) for item in obj]

        return obj

    # Process root schema (non-definitions part)
    root_schema = process_obj(root_schema, root_file)

    # Process all definitions iteratively until stable
    # (new definitions may be added during processing)
    changed = True
    while changed:
        changed = False
        for name in list(all_definitions.keys()):
            if name not in processed:
                processed.add(name)
                source = _def_source.get(name, root_file)
                all_definitions[name] = process_obj(all_definitions[name], source)
                changed = True

    return root_schema, all_definitions


def bundle() -> dict:
    """Bundle the root schema into a single self-contained schema."""
    root_schema, definitions = collect_definitions(ROOT_SCHEMA)
    root_schema["definitions"] = definitions
    return root_schema


def main():
    print(f"Bundling: {ROOT_SCHEMA}")
    bundled = bundle()

    # Validate: check all $refs resolve
    def find_refs(obj, path=""):
        refs = []
        if isinstance(obj, dict):
            for k, v in obj.items():
                if k == "$ref" and isinstance(v, str) and v.startswith("#/definitions/"):
                    refs.append((v.replace("#/definitions/", ""), path))
                else:
                    refs.extend(find_refs(v, f"{path}.{k}"))
        elif isinstance(obj, list):
            for i, v in enumerate(obj):
                refs.extend(find_refs(v, f"{path}[{i}]"))
        return refs

    all_refs = find_refs(bundled)
    defs = set(bundled.get("definitions", {}).keys())
    missing = [(name, path) for name, path in all_refs if name not in defs]

    if missing:
        print("ERROR: Unresolved references:")
        for name, path in missing:
            print(f"  {name} (at {path})")
        sys.exit(1)

    with open(OUTPUT_FILE, "w") as f:
        json.dump(bundled, f, indent=2, ensure_ascii=False)
        f.write("\n")

    print(f"Output:   {OUTPUT_FILE}")
    print(f"  {len(defs)} definitions, {len(all_refs)} internal refs — all resolved ✓")


if __name__ == "__main__":
    main()
