# amf

**Repository**: https://github.com/aml-org/amf

## Release Process
Follows the regular release process (see ~/mulesoft/CLAUDE.md).

## Release Position
- **Third** in the RC/release line
- Depends on **amf-aml** RC/release and **amf-antlr-ast** (check for new version)
- RC and full version must be adopted in **amf-metadata** and **amf-custom-validator-scalajs** before their releases

## Files & Locations

### Version File
- `amf-apicontract.versions`

### Dependencies
- amf-aml (update to RC version for RC, release version for release)
- amf-antlr-ast (check for new version before publishing)
- Validation dialects (located in `.versions` file)

## Jenkins Configuration

### Dashboard
- **Base URL**: https://jenkins.build.msap.io/job/application/job/AMF/job/amf
- **Branch URL format**: `{base-url}/job/{branch-name}` (URL encode `/` as `%252F`)
- **Example**: https://jenkins.build.msap.io/job/application/job/AMF/job/amf/job/release%252F5.10.1

### Stages to Modify
- **Building stages**: `Build JS Package`
- **Publishing stages**: `Publish JVM Artifact`, `Publish JS Package`

### Jenkinsfile Updates
- **For RC**: Add `"release/*"` to branch list in all build and publish stages
- **For Release**: Remove `"release/*"` from branch list in all publish stages

## Dependencies Impact
After releasing amf, update the version in:
- amf-metadata (next in release line)
- amf-custom-validator-scalajs (next in release line)
- apb (later in release line)
- amf-interface-test (post-release, if snapshot changed)
- examples (post-release, if snapshot changed)
- amf-tckutor (post-release)

## Notes
- If new validation dialect versions exist, adopt them in `.versions` file
