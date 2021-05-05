package amf.validation

import _root_.org.scalatest.{Assertion, AsyncFunSuite, Matchers}
import amf.client.AMF
import amf.client.convert.NativeOps
import amf.client.environment.Environment
import amf.client.model.domain.ScalarShape
import amf.client.parse.{DefaultParserErrorHandler, Oas20YamlParser, RamlParser}
import amf.client.resolve.ClientErrorHandlerConverter.ErrorHandlerConverter
import amf.client.resolve.{Oas20Resolver, Raml10Resolver}
import amf.core.client.ParsingOptions
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.validation.AMFValidationResult
import amf.{AMFStyle, Oas20Profile, Raml10Profile}

import scala.concurrent.{ExecutionContext, Future}

trait YamlAnchorsValidationTest extends AsyncFunSuite with Matchers with NativeOps {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("payload validation") {
    AMF.init().asFuture flatMap { _ =>
      val validator =
        new ScalarShape().payloadValidator("application/yaml", Environment.empty().setMaxYamlReferences(50)).asOption
      val report = validator.get
        .validate(
          "application/yaml",
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
        .asFuture
      report.map { r =>
        assertThresholdViolation(r.results.asSeq.map(_._internal))
      }
    }
  }

  test("parsing and resolution violation - raml resolution with examples") {
    val api           = """#%RAML 1.0
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
    val eh            = DefaultParserErrorHandler.withRun()
    val clientHandler = ErrorHandlerConverter.asClient(eh)
    for {
      _           <- AMF.init().asFuture
      unit        <- new RamlParser().parseStringAsync(api, ParsingOptions().setMaxYamlReferences(50)).asFuture
      parseReport <- AMF.validate(unit, Raml10Profile, AMFStyle).asFuture
      _           <- Future(new Raml10Resolver().resolve(unit, ResolutionPipeline.EDITING_PIPELINE, clientHandler))
    } yield {
      assertThresholdViolation(eh.getErrors)
      assertThresholdViolation(parseReport.results.asSeq.map(_._internal))
    }
  }

  test("parsing and resolution violation - oas with examples") {
    val api           = """swagger: '2.0'
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
    val eh            = DefaultParserErrorHandler.withRun()
    val clientHandler = ErrorHandlerConverter.asClient(eh)
    for {
      _           <- AMF.init().asFuture
      unit        <- new Oas20YamlParser().parseStringAsync(api, ParsingOptions().setMaxYamlReferences(50)).asFuture
      parseReport <- AMF.validate(unit, Oas20Profile, AMFStyle).asFuture
      _           <- Future(new Oas20Resolver().resolve(unit, ResolutionPipeline.EDITING_PIPELINE, clientHandler))
    } yield {
      assertThresholdViolation(eh.getErrors)
      assertThresholdViolation(parseReport.results.asSeq.map(_._internal))
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
    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseStringAsync(api, ParsingOptions().setMaxYamlReferences(50)).asFuture
      r    <- AMF.validate(unit, Raml10Profile, AMFStyle).asFuture
      _    <- Future(new Raml10Resolver().resolve(unit, ResolutionPipeline.EDITING_PIPELINE))
    } yield {
      assertThresholdViolation(r.results.asSeq.map(_._internal))
    }
  }

  test("inlcuding external yaml payload") {
    val file = "file://amf-client/shared/src/test/resources/validations/external-yaml-payload/api.raml"
    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseFileAsync(file, ParsingOptions().setMaxYamlReferences(50)).asFuture
      r    <- AMF.validate(unit, Raml10Profile, AMFStyle).asFuture
      _    <- Future(new Raml10Resolver().resolve(unit, ResolutionPipeline.EDITING_PIPELINE))
    } yield {
      assertThresholdViolation(r.results.asSeq.map(_._internal))
    }
  }

  private def assertThresholdViolation(results: Seq[AMFValidationResult]): Assertion = {
    assert(results.size == 1)
    assert(results.head.message == "Exceeded maximum yaml references threshold")
  }

}
