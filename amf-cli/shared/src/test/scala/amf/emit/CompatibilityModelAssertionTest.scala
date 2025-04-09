package amf.emit

import amf.core.client.common.transform.PipelineId
import amf.core.internal.remote.Spec._
import amf.core.internal.remote.{Mimes, Spec}
import amf.core.io.FileAssertionTest
import amf.testing.ConfigProvider.configFor
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class CompatibilityModelAssertionTest extends FileAssertionTest with Matchers {

  private val basePath = "file://amf-cli/shared/src/test/resources/compatibility/"

  def assertRender(
      path: String,
      fromSpec: Spec,
      toSpec: Spec,
      pipelineId: String = PipelineId.Compatibility,
      outputMime: String = Mimes.`application/yaml`
  ): Future[String] = {
    val clientFrom = configFor(fromSpec).baseUnitClient()
    clientFrom.parse(basePath + path) flatMap { parseResult =>
      if (!parseResult.conforms) println(parseResult)
      val clientTo = configFor(toSpec).baseUnitClient()

      val transformResult = clientTo.transform(parseResult.baseUnit, pipelineId)
      if (!transformResult.conforms) println(transformResult)

      clientTo.validate(transformResult.baseUnit) map { validationResult =>
        if (!validationResult.conforms) println(validationResult)
        validationResult.results.size shouldBe 0
        clientTo.render(transformResult.baseUnit, outputMime)
      }
    }
  }

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

  test("Test RAML type array to oas 2") {
    assertRender("raml10/type-array.raml", RAML10, OAS20) map { output =>
      output.nonEmpty shouldBe true
      assert(output.contains("items:"))
    }
  }

  test("Test no empty request in RAML to OAS conversion 1") {
    assertRender("raml10/empty-body-1.raml", RAML10, OAS30) map { output =>
      assert(output.nonEmpty)
      assert(!output.contains("content: {}"))
    }
  }

  test("Test no empty request in RAML to OAS conversion 2") {
    assertRender("raml10/empty-body-2.raml", RAML10, OAS30) map { output =>
      assert(output.nonEmpty)
      assert(!output.contains("content: {}"))
    }
  }
}
