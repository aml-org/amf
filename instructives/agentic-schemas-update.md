# Updating Agentic Schemas and Test Instances

This document describes the process of updating the agentic JSON schemas in the AMF project and synchronizing the test instances across AMF and APB.

## Prerequisites

### Repositories

The following repositories must be checked out locally under `~/mulesoft`:

| Repository | URL |
|---|---|
| AMF | https://github.com/aml-org-emu/amf |
| APB | https://github.com/mulesoft-emu/apb |
| Agent Fabric Specification | https://github.com/mulesoft-emu/agent-fabric-specification |

### Tools

- **Python 3.10+**
- **[PyYAML](https://pypi.org/project/PyYAML/)**

## Steps

### 1. Ensure repositories are up to date

Pull the latest commits in each repository and verify you are on the correct branch:

- **AMF** and **APB**: `develop`
- **Agent Fabric Specification**: `master` (or the branch you are working on)

### 2. Run the update script

From the root of the AMF project, run:

```bash
python3 scripts/json-based-specs/update-schema/bundle_all_schemas.py
```

Alternatively, you can use a virtual environment:

```bash
python3 -m venv ~/venvs/bundle-schemas
source ~/venvs/bundle-schemas/bin/activate
pip install pyyaml
python3 scripts/json-based-specs/update-schema/bundle_all_schemas.py
```

This will:
- Bundle all agentic JSON schemas into each AMF module.
- Copy the test instances (valid/invalid) into both AMF and APB.

### 3. Run tests

Verify that tests pass in both **AMF** and **APB**:

```bash
sbt -mem 16000 test
```

If the instances were updated, the JSON-LD golden files may need to be regenerated. You can override them by adding the `golden.override` system property:

```bash
sbt -mem 16000 -Dgolden.override=true test
```

> **⚠️ Caution:** This will overwrite the golden files in the project. Always review the changes to make sure they are correct before committing.

### 4. Create pull requests

Once tests are passing, create a pull request targeting the `develop` branch in each repository (AMF and APB).
