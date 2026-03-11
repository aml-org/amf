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

### Stages to Modify
- **Building stages**: `Build JS Package`
- **Publishing stages**: `Publish JVM Artifact`, `Publish JS Package`

### Jenkinsfile Updates
- **For RC**: Add `"release/*"` to branch list in all build and publish stages
- **For Release**: Remove `"release/*"` from branch list in all publish stages

## Release Steps

### Publishing RC (x.y.z-RC.r)
1. Check if amf-antlr-ast has a new version and update if needed
2. Update amf-apicontract.versions with amf-aml RC version from previous step
3. From develop: `git checkout -b release/x.y.z`
4. Edit Jenkinsfile: add `"release/*"` to `Build JS Package`, `Publish JVM Artifact`, and `Publish JS Package` stages
5. Edit amf-apicontract.versions: update version to `x.y.z-RC.r`
6. Commit: `git commit -m "Publish x.y.z-RC.r"`
7. Push: `git push -u origin release/x.y.z`

### Publishing Release (x.y.z)
1. Update amf-apicontract.versions with amf-aml release version
2. Follow standard three-PR process (setup → master → develop)
3. After merging to master, manually tag: `git tag x.y.z origin/master && git push origin x.y.z`

### Publishing Hotfix (x.y.z-n)
1. Checkout/create `support/x.y.z` branch from tag
2. Cherry-pick required commits
3. Update version in amf-apicontract.versions to `x.y.z-n`
4. Check if amf-aml hotfix is also needed and update dependency
5. Ensure Jenkinsfile has `support/*` in all build/publish stages (for first HF only)
6. Push and create PR to merge into `support/x.y.z`

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
