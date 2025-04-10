package amf.emit

import amf.core.client.common.transform.PipelineId
import amf.core.internal.remote.Spec._
import amf.core.internal.remote.{Mimes, Spec}
import amf.testing.ConfigProvider.configFor
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class CompatibilityModelAssertionTest extends AsyncFunSuite with Matchers {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  private val basePath                                     = "file://amf-cli/shared/src/test/resources/compatibility/"

  def validateCycle(
      path: String,
      fromSpec: Spec,
      toSpec: Spec,
      pipelineId: String = PipelineId.Compatibility,
      outputMime: String = Mimes.`application/yaml`
  ): Future[Assertion] = {
    val clientFrom = configFor(fromSpec).baseUnitClient()
    clientFrom.parse(basePath + path) flatMap { parseResult =>
      if (!parseResult.conforms) println(parseResult)
      parseResult.conforms shouldBe true

      val clientTo        = configFor(toSpec).baseUnitClient()
      val transformResult = clientTo.transform(parseResult.baseUnit, pipelineId)
      if (!transformResult.conforms) println(transformResult)
      transformResult.conforms shouldBe true

      val firstRender = clientTo.render(transformResult.baseUnit, outputMime)

      clientTo.parseContent(firstRender) flatMap { cycleParseResult =>
        if (!cycleParseResult.conforms) println(cycleParseResult)
        cycleParseResult.conforms shouldBe true

        val cycleTransformResult = clientTo.transform(cycleParseResult.baseUnit, PipelineId.Editing)
        if (!cycleTransformResult.conforms) println(cycleTransformResult)
        cycleTransformResult.conforms shouldBe true

        clientTo.validate(cycleTransformResult.baseUnit) flatMap { cycleValidationResult =>
          if (!cycleValidationResult.conforms) println(cycleValidationResult)
          cycleValidationResult.conforms shouldBe true
        }
      }
    }
  }

  test("Test RAML to oas 2 displayName in property") {
    validateCycle("raml10/display-name-prop.raml", RAML10, OAS20)
  }

  test("Test RAML to oas 2 multiple body content types") {
    validateCycle("raml10/multiple-body-content-types.raml", RAML10, OAS20)
  }

  test("Test RAML to oas 2 multiple properties in body") {
    validateCycle("raml10/multiple-properties-in-body.raml", RAML10, OAS20)
  }

  test("Test RAML to oas 2 duplicated params") {
    validateCycle("raml10/form-data-with-props.raml", RAML10, OAS20)
  }

  test("multiple body params RAML to oas 2") {
    validateCycle("raml10/multiple-body-params.raml", RAML10, OAS20)
  }

  test("Property 'properties' not supported in a OAS 2.0 parameter node") {
    validateCycle("raml10/form-data-schema.raml", RAML10, OAS20)
  }
}
