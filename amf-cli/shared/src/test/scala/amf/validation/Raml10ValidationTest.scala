package amf.validation

import amf.apicontract.client.scala.{AMFConfiguration, RAMLConfiguration}
import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.{ProfileName, Raml10Profile}

class Raml10ValidationTest extends AbstractValidationTest {
  protected def config: AMFConfiguration = RAMLConfiguration.RAML10()
  protected val pipeline: String         = PipelineId.Editing
  protected val profile: ProfileName     = Raml10Profile

  private val path = "amf-cli/shared/src/test/resources/validations/raml"

  ignore("Sub-schemas") {
    assertReport("api.raml", "api.report", s"$path/sub-schemas")
  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Lexicals ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // external example
  ignore("Test failing external example in endpoint") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-external-example-in-endpoint")
  }

  ignore("Test failing external example in parametrized rt") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-external-example-in-parametrized-rt")
  }

  ignore("Test failing external example in rt") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-external-example-in-rt")
  }

  // external typed example

  ignore("Test failing external typed example in endpoint") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-external-typed-example-in-endpoint")
  }

  ignore("Test failing external typed example in parametrized rt") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-external-typed-example-in-parametrized-rt")
  }

  ignore("Test failing external typed example in rt") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-external-typed-example-in-rt")
  }

  // inlined example

  ignore("Test failing inline example in endpoint") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-inline-example-in-endpoint")
  }

  ignore("Test failing inline example in parametrized rt") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-inline-example-in-parametrized-rt")
  }

  ignore("Test failing inline example in rt") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-inline-example-in-rt")
  }

  // external type

  ignore("Test failing external type in endpoint") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-external-type-in-endpoint")
  }

  ignore("Test failing external type in parametrized rt") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-external-type-in-parametrized-rt")
  }

  ignore("Test failing external type in rt") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-external-type-in-rt")
  }

  // inlined type
  ignore("Test failing inline type in endpoint") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-inline-type-in-endpoint")
  }

  ignore("Test failing inline type in parametrized rt") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-inline-type-in-parametrized-rt")
  }

  ignore("Test failing inline type in rt") {
    assertReport("api.raml", "api.report", s"$path/lexicals/failing-inline-type-in-rt")
  }


}
