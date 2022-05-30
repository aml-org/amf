package amf.validation

import amf.core.internal.remote.{Hint, Raml08YamlHint, Raml10YamlHint}

class RamlInterSpecRefsReportTest extends UniquePlatformReportGenTest {

  override val basePath    = "file://amf-cli/shared/src/test/resources/production/inter-spec-refs/"
  override val reportsPath = "amf-cli/shared/src/test/resources/production/inter-spec-refs/reports/"

  test("Raml refs json schema fragment") {
    validate("raml-json-schema/api.raml", None)
  }

  test("Raml refs json schema Oas API") {
    validate("raml-oas-jsonpointer/api.raml", Some("raml-oas-jsonpointer.report"))
  }

  test("Raml 0.8 refs Raml 1.0 datatype fragment") {
    validate(
      "raml08-raml1-datatype/api.raml",
      Some("raml08-raml1-datatype.report"),
      overridedHint = Some(Raml08YamlHint)
    )
  }

  override val hint: Hint = Raml10YamlHint
}
