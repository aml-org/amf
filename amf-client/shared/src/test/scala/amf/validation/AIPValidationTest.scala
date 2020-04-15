package amf.validation

import amf.ProfileName
import amf.core.remote.{Hint, RamlYamlHint}

class AIPValidationTest extends UniquePlatformReportGenTest {

    override val basePath = "file://amf-client/shared/src/test/resources/validations/aip/"
    override val reportsPath = "amf-client/shared/src/test/resources/validations/reports/aip/"
    override val hint: Hint = RamlYamlHint


    test("Resource property name") {
      validate("ex1.raml", Some("ex1-aip122-resource-name.report"), ProfileName("AIP"), Some("../profiles/aip.yaml"))
    }

    test("Enumerations") {
        validate("ex2.raml", Some("ex2-aip.report"), ProfileName("AIP"), Some("../profiles/aip.yaml"))
    }

}
