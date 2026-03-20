# JSON-Based Specs — Schema Bundler & Instance Updater

Bundles JSON Schema files by resolving all external `$ref` references and inlining them into a single self-contained schema. Also copies test instance files (valid/invalid) from the source specification repository into the AMF and APB projects.

## Prerequisites

- Python 3.10+
- [PyYAML](https://pypi.org/project/PyYAML/):
  ```bash
  pip install pyyaml
  ```
- The following repositories checked out and updated locally (paths configured in `schemas.yaml`):
  - [agent-fabric-specification](https://github.com/mulesoft-emu/agent-fabric-specification)
  - [amf](https://github.com/aml-org-emu/amf)
  - [apb](https://github.com/aml-org-emu/apb)

## Files

| File | Description |
|---|---|
| `bundle_schema.py` | Bundles a single root schema into a self-contained output file. |
| `bundle_all_schemas.py` | Reads `schemas.yaml`, runs `bundle_schema.py` for each entry, and copies test instances to AMF and APB. |
| `schemas.yaml` | Configuration file listing repository paths, schemas to bundle, and test instance locations. |

## Configuration

`schemas.yaml` has two top-level sections: `repositories` and `specs`.

### Repositories

Defines local paths to each project. Supports `~` expansion.

```yaml
repositories:
  amfLocalPath: "~/mulesoft/amf"
  apbLocalPath: "~/mulesoft/apb"
  specLocalPath: "~/mulesoft/agent-fabric-specification"
```

### Specs

Each entry in the `specs` list defines a schema and its associated test instances:

```yaml
specs:
  - name: "agent card"
    amf-module: "amf-agent-card"
    schema:
      spec: "agent-fabric-schema/src/main/resources/agent_card.json"
      amf: "shared/src/main/resources/schema_agent_card.json"
    instances:
      spec:
        valid: "agent-fabric-schema/src/test/resources/agent_card/basic/valid"
        invalid: "agent-fabric-schema/src/test/resources/agent_card/basic/invalid"
      amf:
        valid: "shared/src/test/resources/instances/valid"
        invalid: "shared/src/test/resources/instances/invalid"
      apb:
        valid:
          - "apb/shared/src/test/resources/spec/local/agent-card"
          - "apb/shared/src/test/resources/api-project/local/main-agent-card"
        invalid: "apb/shared/src/test/resources/spec/local/agent-card-invalid"
```

| Field | Description |
|---|---|
| `name` | A human-readable label (used in log output). |
| `amf-module` | The AMF module directory name (e.g., `amf-agent-card`). |
| `schema.spec` | Path to the root JSON schema, relative to `specLocalPath`. |
| `schema.amf` | Path for the bundled output, relative to the AMF module directory. |
| `instances.spec.valid` | Source directory for valid instances, relative to `specLocalPath`. |
| `instances.spec.invalid` | Source directory for invalid instances, relative to `specLocalPath`. |
| `instances.amf.valid` | AMF target directory for valid instances, relative to the AMF module. |
| `instances.amf.invalid` | AMF target directory for invalid instances, relative to the AMF module. |
| `instances.apb.valid` | APB target directory/directories for valid instances, relative to `apbLocalPath`. Can be a list. |
| `instances.apb.invalid` | APB target directory for invalid instances, relative to `apbLocalPath`. |

### Instance copying behavior

- **AMF**: Copies `asset.*` (e.g., `asset.json` or `asset.yaml`) from the spec source directory into the AMF module target.
- **APB**: Copies both `asset.*` and `exchange.json` from the spec source directory into each APB target. The `valid` field can be a list of paths — all are updated.

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
