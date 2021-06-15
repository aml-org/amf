package amf.validation

import amf.core.client.common.validation.ProfileName
import amf.core.internal.remote.{Hint, Raml10YamlHint}

class AIPValidationTest extends UniquePlatformReportGenTest {

  override val basePath    = "file://amf-cli/shared/src/test/resources/validations/aip/"
  override val reportsPath = "amf-cli/shared/src/test/resources/validations/reports/aip/"
  override val hint: Hint  = Raml10YamlHint

  test("Resource property name") {
    validate("ex1.raml", Some("ex1-aip122-resource-name.report"), ProfileName("AIP"), Some("../profiles/aip.yaml"))
  }

  test("Enumerations") {
    validate("ex2.raml", Some("ex2-aip.report"), ProfileName("AIP"), Some("../profiles/aip.yaml"))
  }

}
