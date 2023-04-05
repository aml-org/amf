package amf.validation

import amf.apicontract.client.scala.{AMFConfiguration, RAMLConfiguration}
import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.{ProfileName, Raml10Profile}

class Raml10ValidationTest extends AbstractValidationTest {
  protected def config: AMFConfiguration = RAMLConfiguration.RAML10()
  protected val pipeline: String         = PipelineId.Editing
  protected val profile: ProfileName     = Raml10Profile

  private val path = "amf-cli/shared/src/test/resources/validations/raml"

  test("Sub-schemas") {
    assertReport("api.raml", "api.report", s"$path/sub-schemas")
  }

}
