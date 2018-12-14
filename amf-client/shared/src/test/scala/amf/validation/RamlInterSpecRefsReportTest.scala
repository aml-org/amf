package amf.validation

import amf.core.remote.{Hint, RamlYamlHint}

class RamlInterSpecRefsReportTest extends UniquePlatformReportGenTest {

  override val basePath    = "file://amf-client/shared/src/test/resources/production/inter-spec-refs/"
  override val reportsPath = "amf-client/shared/src/test/resources/production/inter-spec-refs/reports/"

  test("Raml refs json schema fragment") {
    validate("raml-json-schema/api.raml", None)
  }

  test("Raml refs json schema Oas API") {
    validate("raml-json-schema/api.raml", None)
  }

  test("Raml 0.8 refs Raml 1.0 datatype fragment") {
    validate("raml08-raml1-datatype/api.raml", None)
  }

  override val hint: Hint = RamlYamlHint
}
