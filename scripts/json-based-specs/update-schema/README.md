# JSON-Based Specs — Schema Bundler

Bundles JSON Schema files by resolving all external `$ref` references and inlining them into a single self-contained schema.

## Prerequisites

- Python 3.10+
- [PyYAML](https://pypi.org/project/PyYAML/):
  ```bash
  pip install pyyaml
  ```
- The [agent fabric spec repository](https://github.com/mulesoft-emu/agent-fabric-specification) checked out and updated in locally in the directory `~/mulesoft/agent-fabric-specification` (only needed to run `bundle_all_schemas.py`). 

## Files

| File | Description |
|---|---|
| `bundle_schema.py` | Bundles a single root schema into a self-contained output file. |
| `bundle_all_schemas.py` | Reads `schemas.yaml` and runs `bundle_schema.py` for each entry. |
| `schemas.yaml` | Configuration file listing the schemas to bundle. |

## Configuration

Edit `schemas.yaml` to define the schemas to bundle. Each entry has three fields:

```yaml
- name: "agent graph"
  rootSchema: "~/mulesoft/agent-fabric-specification/agent-fabric-schema/src/main/resources/agent_network_v2.json"
  output: "amf-agent-graph/shared/src/main/resources/schema_agent_graph.json"
```

| Field | Description |
|---|---|
| `name` | A human-readable label for the schema (used in log output). |
| `rootSchema` | Absolute path to the root JSON schema file. Supports `~` expansion. |
| `output` | Relative path for the bundled output file (resolved from the project root). |

## Usage

### Bundle all schemas

```bash
python3 scripts/json-based-specs/update-schema/bundle_all_schemas.py
```

### Bundle a single schema

```bash
python3 scripts/json-based-specs/update-schema/bundle_schema.py <root_schema> <output_file>
```

- **`root_schema`** — Absolute path to the root JSON schema file.
- **`output_file`** — Relative path (from cwd) to the bundled output file.

Example:

```bash
python3 scripts/json-based-specs/update-schema/bundle_schema.py \
  /Users/you/project/base-schemas-agent-graph/agent_network_v2.json \
  schema_agent_graph.json
```
