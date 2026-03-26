# Generate New JsonSchemaBasedSpec Module

This script generates a complete new JsonSchemaBasedSpec module in the AMF project, including all necessary Scala source files, tests, build configuration, and amf-core registration.

## Prerequisites

- Python 3.6+
- No external dependencies required (uses only Python standard library)
- The three projects (`amf`, `amf-core`, and `apb`) must be located in the same parent folder, each on the `develop` branch with no uncommitted changes
- `schemas.yaml` in `scripts/json-based-specs/update-schema/` must have a valid `specLocalPath` under `repositories`

## Usage

```bash
cd scripts/json-based-specs/new-spec
python3 generate_new_spec.py
```

The script will interactively prompt for the following inputs:

| Input | Description | Example |
|---|---|---|
| **Spec name** | Human-readable name for the spec | `My Spec` |
| **Schema path** | Path to root JSON Schema file, relative to `specLocalPath` | `agent-fabric-schema/src/main/resources/my_spec.json` |
| **Media type** | `application/json` or `application/yaml` | `1` (for JSON) |
| **Has ID entry** | Whether instances have an identifying key | `y` or `n` |
| **ID entry key** | The JSON key used for identification (if applicable) | `schemaVersion` |
| **Valid instances dir** | Directory with valid `asset.*` file, relative to `specLocalPath` | `agent-fabric-schema/src/test/resources/my_spec/basic/valid` |
| **Invalid instances dir** | Directory with invalid `asset.*` file, relative to `specLocalPath` | `agent-fabric-schema/src/test/resources/my_spec/basic/invalid` |
| **Classifier** | String identifier for the asset kind in APB | `agent-graph`, `mcp-metadata` |

## What it generates

### In the `amf` repo

1. **Module directory** (`amf-my-spec/`) with the full Scala source tree:
   - `shared/src/main/scala/amf/myspec/` — Parse plugin, render plugin, validation plugin, schema, entry, configuration, client, converters
   - `js/src/main/scala/` and `jvm/src/main/scala/` — Platform-specific converter traits
   - `shared/src/test/scala/amf/` — CycleTest (with TestConfig), SchemaLoaderTest, ValidationTest, SourceSpecTest, entry test. All tests use shared base classes from `amf.shapes.test._`
   - `shared/src/test/resources/instances/` — Valid and invalid test instances, entry test resources. The golden `.jsonld` file for the CycleTest must be generated after first run
   - `shared/src/main/resources/` — Schema file placeholder (populated by the bundler)

2. **build.sbt** updates:
   - New module definition with `sourceGenerators` for schema embedding
   - JVM/JS sub-project definitions
   - Added to `cli` and `adhoc-cli` dependencies

3. **schemas.yaml** — New entry with schema paths, instance directories (spec, amf, apb), and module name for the update-schema bundler scripts

### In the `amf-core` repo

4. **Spec.scala** — New `case object`, `@JSExport val`, and `unapply` case
5. **ProfileNames.scala** — New profile object, `val`, `specProfiles` entry, `unapply` and `apply` cases

### In the `apb` repo

6. **Classifier.scala** — New classifier constant
7. **ConfigProvider.scala** — New import, `fromClassifier` case, `fromSpec` case, and error message update
8. **build.sbt** — New `ProjectRef`/library lazy vals and `.sourceDependency` entries for JVM/JS
9. **Test resources** — `exchange.json` and `asset.yaml` files for spec, spec-invalid, and api-project test directories
10. **Test files** — New test cases in `APIProjectClientTest`, `E2EAPBContractClientTest`, and `ForSpecAPBContractClientTest`

## ID Entry patterns

- **No ID entry** (`IdEntryNoEntry`): The spec accepts any document without checking for a specific key. Used by specs like MCP.
- **With ID entry** (`IdEntry` + `IdEntryVersion`): The spec checks for a specific key in the document root to identify instances. Used by specs like Agent Graph (`agentNetwork` key) and Agent Network (`schemaVersion` key).

## After generation

1. **Bundle the schema**: Run the schema bundler to populate the schema file from the root JSON Schema:
   ```bash
   cd scripts/json-based-specs/update-schema
   python3 bundle_all_schemas.py
   ```

2. **Compile**:
   ```bash
   sbt mySpecJVM/compile
   ```

3. **Generate the golden `.jsonld` file** for the CycleTest:
   ```bash
   sbt "mySpecJVM/testOnly amf.MySpecCycleTest"
   ```
   This will fail on the first run. Copy the generated temporary output to `shared/src/test/resources/instances/valid/asset.jsonld` (or the corresponding golden path).

4. **Test**:
   ```bash
   sbt mySpecJVM/test
   ```

5. **Review** the generated files and adjust as needed (e.g., `invalidInstances` error count in `TestConfig`, entry test resources).

6. **Verify amf-core changes** — Review `Spec.scala` and `ProfileNames.scala` for correctness.

7. **Compile and test APB**:
   ```bash
   cd ~/mulesoft/apb
   sbt apbProjectJVM/compile
   sbt apbJVM/test
   ```

8. **Verify APB changes** — Review `Classifier.scala`, `ConfigProvider.scala`, and `build.sbt`.

9. **Generate the golden `.jsonld` file** for the `ForSpecAPBContractClientTest` by running the test once and accepting the output.
