package amf.validation

import amf.agentnetwork.client.scala.AgentNetworkConfiguration
import amf.apicontract.client.scala.{OASConfiguration, RAMLConfiguration}
import amf.client.validation.PayloadValidationUtils
import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.ValidationMode.StrictValidationMode
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.validation.AMFValidationResult
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.remote.Mimes
import amf.core.internal.remote.Mimes._
import amf.shapes.client.scala.config.JsonLDSchemaConfiguration
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.shapes.internal.helper.SyncJsonSchemaCompiler
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class YamlAnchorsValidationTest
    extends AsyncFunSuiteWithPlatformGlobalExecutionContext
    with Matchers
    with PayloadValidationUtils {

  private val ramlConfig = RAMLConfiguration.RAML10().withParsingOptions(ParsingOptions().setMaxYamlReferences(50))
  private val oasConfig  = OASConfiguration.OAS20().withParsingOptions(ParsingOptions().setMaxYamlReferences(50))
  private val agentConfig =
    AgentNetworkConfiguration.AgentNetwork().withParsingOptions(ParsingOptions().setMaxYamlReferences(50))

  private val baseSchema =
    SyncJsonSchemaCompiler
      .compile("""{"$schema":"http://json-schema.org/draft-07/schema#","type":"object"""")
      .baseUnit
      .asInstanceOf[JsonSchemaDocument]
  private val jsonLdSchemaConfig =
    JsonLDSchemaConfiguration
      .JsonLDSchema()
      .withParsingOptions(ParsingOptions().setMaxYamlReferences(50))
      .withJsonLDSchema(baseSchema)

  test("payload validation") {

    val validator =
      ramlConfig.elementClient().payloadValidatorFor(ScalarShape(), `application/yaml`, StrictValidationMode)

    val report = validator
      .validate(
        """
          |a: &a ["lol","lol","lol","lol","lol","lol","lol","lol","lol"]
          |b: &b [*a,*a,*a,*a,*a,*a,*a,*a,*a]
          |c: &c [*b,*b,*b,*b,*b,*b,*b,*b,*b]
          |d: &d [*c,*c,*c,*c,*c,*c,*c,*c,*c]
          |e: &e [*d,*d,*d,*d,*d,*d,*d,*d,*d]
          |f: &f [*e,*e,*e,*e,*e,*e,*e,*e,*e]
          |g: &g [*f,*f,*f,*f,*f,*f,*f,*f,*f]
          |""".stripMargin
      )

    report.map { r =>
      assertThresholdViolation(r.results)
    }
  }

  test("parsing and resolution violation - raml resolution with examples") {
    val api = """#%RAML 1.0
                          |title: my API
                          |/person:
                          |  get:
                          |    body:
                          |      application/json:
                          |        examples:
                          |          a: &a ["lol","lol","lol","lol","lol","lol","lol","lol","lol"]
                          |          b: &b [*a,*a,*a,*a,*a,*a,*a,*a,*a]
                          |          c: &c [*b,*b,*b,*b,*b,*b,*b,*b,*b]
                          |          d: &d [*c,*c,*c,*c,*c,*c,*c,*c,*c]
                          |          e: &e [*d,*d,*d,*d,*d,*d,*d,*d,*d]
                          |          f: &f [*e,*e,*e,*e,*e,*e,*e,*e,*e]
                          |          g: &g [*f,*f,*f,*f,*f,*f,*f,*f,*f]
                          |          h: &h [*g,*g,*g,*g,*g,*g,*g,*g,*g]
                          |""".stripMargin
    val client = ramlConfig.baseUnitClient()
    for {
      parseResult    <- client.parseContent(api)
      unit           <- Future.successful { parseResult.baseUnit }
      validateReport <- client.validate(unit)
      _              <- Future(client.transform(unit, PipelineId.Editing))
    } yield {
      assertThresholdViolation(validateReport.results)
      assertThresholdViolation(parseResult.results)
    }
  }

  test("parsing and resolution violation - oas with examples") {
    val api = """swagger: '2.0'
                          |info:
                          |  version: 1.0.0
                          |  title: test
                          |produces: [application/json]
                          |paths:
                          |  '/pets':
                          |    get:
                          |      responses:
                          |        default:
                          |          description: asd
                          |          schema:
                          |            type: array
                          |          examples:
                          |            a: &a ["lol","lol","lol","lol","lol","lol","lol","lol","lol"]
                          |            b: &b [*a,*a,*a,*a,*a,*a,*a,*a,*a]
                          |            c: &c [*b,*b,*b,*b,*b,*b,*b,*b,*b]
                          |            d: &d [*c,*c,*c,*c,*c,*c,*c,*c,*c]
                          |            e: &e [*d,*d,*d,*d,*d,*d,*d,*d,*d]
                          |            f: &f [*e,*e,*e,*e,*e,*e,*e,*e,*e]
                          |            g: &g [*f,*f,*f,*f,*f,*f,*f,*f,*f]
                          |            application/json: &h [*g,*g,*g,*g,*g,*g,*g,*g,*g]
                          |""".stripMargin
    val client = oasConfig.baseUnitClient()
    for {
      parseResult    <- client.parseContent(api, Mimes.`application/yaml`)
      unit           <- Future.successful { parseResult.baseUnit }
      validateReport <- client.validate(unit)
      _              <- Future(client.transform(unit, PipelineId.Editing))
    } yield {
      assertThresholdViolation(parseResult.results)
      assertThresholdViolation(validateReport.results)
    }
  }

  test("custom security scheme settings") {
    val api = """#%RAML 1.0
                          |title: my API
                          |securitySchemes:
                          |  custom_scheme:
                          |    type: x-custom
                          |    settings:
                          |      a: &a ["lol","lol","lol","lol","lol","lol","lol","lol","lol"]
                          |      b: &b [*a,*a,*a,*a,*a,*a,*a,*a,*a]
                          |      c: &c [*b,*b,*b,*b,*b,*b,*b,*b,*b]
                          |      d: &d [*c,*c,*c,*c,*c,*c,*c,*c,*c]
                          |      e: &e [*d,*d,*d,*d,*d,*d,*d,*d,*d]
                          |      f: &f [*e,*e,*e,*e,*e,*e,*e,*e,*e]
                          |      g: &g [*f,*f,*f,*f,*f,*f,*f,*f,*f]
                          |      h: &h [*g,*g,*g,*g,*g,*g,*g,*g,*g]
                          |      i: &i [*h,*h,*h,*h,*h,*h,*h,*h,*h]""".stripMargin
    verifyMaxThresholdParsingViolation(api)
  }

  test("default and enum values") {
    val api = """#%RAML 1.0
                          |title: my API
                          |
                          |/person:
                          |  get:
                          |    body:
                          |      application/json:
                          |        type: object
                          |        enum:
                          |          - a: &a ["lol","lol","lol","lol","lol","lol","lol","lol","lol"]
                          |            b: &b [*a,*a,*a,*a,*a,*a,*a,*a,*a]
                          |            c: &c [*b,*b,*b,*b,*b,*b,*b,*b,*b]
                          |            d: &d [*c,*c,*c,*c,*c,*c,*c,*c,*c]
                          |            e: &e [*d,*d,*d,*d,*d,*d,*d,*d,*d]
                          |            f: &f [*e,*e,*e,*e,*e,*e,*e,*e,*e]
                          |            g: &g [*f,*f,*f,*f,*f,*f,*f,*f,*f]
                          |            h: &h [*g,*g,*g,*g,*g,*g,*g,*g,*g]
                          |            i: &i [*h,*h,*h,*h,*h,*h,*h,*h,*h]
                          |        default:
                          |          a: &a ["lol","lol","lol","lol","lol","lol","lol","lol","lol"]
                          |          b: &b [*a,*a,*a,*a,*a,*a,*a,*a,*a]
                          |          c: &c [*b,*b,*b,*b,*b,*b,*b,*b,*b]
                          |          d: &d [*c,*c,*c,*c,*c,*c,*c,*c,*c]
                          |          e: &e [*d,*d,*d,*d,*d,*d,*d,*d,*d]
                          |          f: &f [*e,*e,*e,*e,*e,*e,*e,*e,*e]
                          |          g: &g [*f,*f,*f,*f,*f,*f,*f,*f,*f]
                          |          h: &h [*g,*g,*g,*g,*g,*g,*g,*g,*g]
                          |          i: &i [*h,*h,*h,*h,*h,*h,*h,*h,*h]
                          |""".stripMargin
    verifyMaxThresholdParsingViolation(api)
  }

  test("extension defined when using annotation") {
    val api = """#%RAML 1.0
                          |title: my API
                          |
                          |annotationTypes:
                          |  some-annotations: any
                          |
                          |(some-annotation):
                          |    a: &a ["lol","lol","lol","lol","lol","lol","lol","lol","lol"]
                          |    b: &b [*a,*a,*a,*a,*a,*a,*a,*a,*a]
                          |    c: &c [*b,*b,*b,*b,*b,*b,*b,*b,*b]
                          |    d: &d [*c,*c,*c,*c,*c,*c,*c,*c,*c]
                          |    e: &e [*d,*d,*d,*d,*d,*d,*d,*d,*d]
                          |    f: &f [*e,*e,*e,*e,*e,*e,*e,*e,*e]
                          |    g: &g [*f,*f,*f,*f,*f,*f,*f,*f,*f]
                          |    h: &h [*g,*g,*g,*g,*g,*g,*g,*g,*g]
                          |    i: &i [*h,*h,*h,*h,*h,*h,*h,*h,*h]
                          |""".stripMargin
    verifyMaxThresholdParsingViolation(api)
  }

  private def verifyMaxThresholdParsingViolation(api: String) = {
    val client = ramlConfig.baseUnitClient()
    for {
      parseResult    <- client.parseContent(api)
      unit           <- Future.successful { parseResult.baseUnit }
      validateReport <- client.validate(unit)
      _              <- Future(client.transform(unit, PipelineId.Editing))
    } yield {
      assertThresholdViolation(parseResult.results)
    }
  }

  test("including external yaml payload") {
    val file   = "file://amf-cli/shared/src/test/resources/validations/external-yaml-payload/api.raml"
    val client = ramlConfig.baseUnitClient()
    for {
      parseResult    <- client.parse(file)
      unit           <- Future.successful { parseResult.baseUnit }
      validateReport <- client.validate(unit)
      _              <- Future(client.transform(unit, PipelineId.Editing))
    } yield {
      assertThresholdViolation(parseResult.results)
      assertThresholdViolation(validateReport.results)
    }
  }

  test("JsonLdInstance parsing with anchors") {

    val file   = "file://amf-cli/shared/src/test/resources/validations/yaml-anchors.yaml"
    val client = jsonLdSchemaConfig.baseUnitClient()

    for {
      parseResult <- client.parseJsonLDInstance(file, baseSchema)
      unit        <- Future.successful { parseResult.baseUnit }
      _           <- client.validate(unit)
      _           <- Future(client.transform(unit, PipelineId.Editing))
    } yield {
      // Size is not 1 in the error in JsonLdInstance, the paths in the arrays are independent, so they start failing in cascade
      assertThresholdViolationMessage(parseResult.results)
    }
  }

  test("AgentNetwork parsing with anchors") {

    val file   = "file://amf-cli/shared/src/test/resources/validations/agent-anchors.yaml"
    val client = agentConfig.baseUnitClient()

    for {
      parseResult <- client.parse(file)
      unit        <- Future.successful { parseResult.baseUnit }
      _           <- client.validate(unit)
      _           <- Future(client.transform(unit, PipelineId.Editing))
    } yield {
      // Size is not 1 in the error in JsonLdInstance, the paths in the arrays are independent, so they start failing in cascade
      assertThresholdViolationMessage(parseResult.results)
    }
  }

  private def assertThresholdViolation(results: Seq[AMFValidationResult]): Assertion = {
    assert(results.size == 1)
    assertThresholdViolationMessage(results)
  }

  private def assertThresholdViolationMessage(results: Seq[AMFValidationResult]): Assertion = {
    assert(results.forall(_.message == "Exceeded maximum yaml references threshold"))
  }

}
