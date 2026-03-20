# JSON-Based Specs — Schema Bundler & Instance Updater

Bundles JSON Schema files by resolving all external `$ref` references and inlining them into a single self-contained schema. Also copies test instance files (valid/invalid) from the source specification repository into each module's test resources.

## Prerequisites

- Python 3.10+
- [PyYAML](https://pypi.org/project/PyYAML/):
  ```bash
  pip install pyyaml
  ```
- The [agent fabric spec repository](https://github.com/mulesoft-emu/agent-fabric-specification) checked out and updated locally in the directory `~/mulesoft/agent-fabric-specification`.

## Files

| File | Description |
|---|---|
| `bundle_schema.py` | Bundles a single root schema into a self-contained output file. |
| `bundle_all_schemas.py` | Reads `schemas.yaml`, runs `bundle_schema.py` for each entry, and copies test instances. |
| `schemas.yaml` | Configuration file listing the schemas to bundle and their test instances. |

## Configuration

Edit `schemas.yaml` to define the schemas to bundle. Each entry has the following fields:

```yaml
- name: "agent card"
  module: "amf-agent-card"
  rootSchema: "~/mulesoft/agent-fabric-specification/agent-fabric-schema/src/main/resources/agent_card.json"
  output: "shared/src/main/resources/schema_agent_card.json"
  instances:
    valid: "~/mulesoft/agent-fabric-specification/agent-fabric-schema/src/test/resources/agent_card/basic/valid"
    invalid: "~/mulesoft/agent-fabric-specification/agent-fabric-schema/src/test/resources/agent_card/basic/invalid"
```

| Field | Description |
|---|---|
| `name` | A human-readable label (used in log output). |
| `module` | The AMF module directory name (e.g., `amf-agent-card`). |
| `rootSchema` | Absolute path to the root JSON schema file. Supports `~` expansion. |
| `output` | Path for the bundled output file, relative to the module directory. |
| `instances.valid` | Absolute path to the directory containing the valid `asset.*` source instance. |
| `instances.invalid` | Absolute path to the directory containing the invalid `asset.*` source instance. |

The script looks for an `asset.*` file (e.g., `asset.json` or `asset.yaml`) in each instance source directory and copies it to `<module>/shared/src/test/resources/instances/{valid,invalid}/asset.<ext>`.

## Usage

### Bundle all schemas and update instances

```bash
python3 scripts/json-based-specs/update-schema/bundle_all_schemas.py
```

Alternately, you can run it in a venv:

```bash
python3 -m venv ~/venvs/bundle-schemas
source ~/venvs/bundle-schemas/bin/activate
pip install pyyaml
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
  ~/mulesoft/agent-fabric-specification/agent-fabric-schema/src/main/resources/agent_card.json \
  amf-agent-card/shared/src/main/resources/schema_agent_card.json
```
