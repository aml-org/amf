package amf.tools

import amf.ProfileName
import amf.client.parse.DefaultParserErrorHandler
import amf.core.AMF
import amf.core.emitter.RenderOptions
import amf.core.model.document.ExternalFragment
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote._
import amf.core.services.RuntimeValidator
import amf.core.unsafe.PlatformSecrets
import amf.emit.AMFRenderer
import amf.facades.{AMFCompiler, Validation}
import amf.io.FunSuiteCycleTests
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.tools.canonical.{CanonicalWebAPISpecTransformer, DocumentExpectedException}
import org.scalatest.{Assertion, Matchers}

import scala.concurrent.Future

class CanonicalWebAPISpecDialectTest extends FunSuiteCycleTests with PlatformSecrets with Matchers {

  val CANONICAL_WEBAPI_DIALECT  = "file://vocabularies/dialects/canonical_webapi_spec.yaml"
  override def basePath: String = "file://amf-client/shared/src/test/resources/transformations/"

  def checkCanonicalDialectTransformation(source: String, target: String, shouldTranform: Boolean): Future[Assertion] = {
    val amfWebApi  = basePath + source
    val goldenYaml = s"$basePath$target.yaml"
    val goldenJson = s"$basePath$target.json"

    for {
      _ <- AMF.init()
      _ <- Future(amf.Core.registerPlugin(AMLPlugin))
      v <- Validation(platform)
      d <- AMFCompiler(CANONICAL_WEBAPI_DIALECT, platform, VocabularyYamlHint, eh = UnhandledParserErrorHandler)
        .build()
      _           <- Future { AMLPlugin.registry.resolveRegisteredDialect(d.asInstanceOf[Dialect].header) }
      unit        <- AMFCompiler(amfWebApi, platform, RamlYamlHint, eh = DefaultParserErrorHandler()).build()
      transformed <- CanonicalWebAPISpecTransformer.transform(unit)
      json        <- new AMFRenderer(transformed, Vendor.AMF, RenderOptions().withPrettyPrint, Some(Syntax.Json)).renderToString
      yaml        <- new AMFRenderer(transformed, Vendor.AML, RenderOptions().withNodeIds, Some(Syntax.Yaml)).renderToString
      tmpYaml     <- writeTemporaryFile(goldenYaml)(yaml)
      tmpJson     <- writeTemporaryFile(goldenJson)(json)
      res <- {
        assertDifferences(tmpYaml, goldenYaml)
        assertDifferences(tmpJson, goldenJson)
      }
      report <- {
        RuntimeValidator(
          transformed,
          ProfileName(CanonicalWebAPISpecTransformer.CANONICAL_WEBAPI_NAME)
        )
      }
    } yield {
      assert(report.conforms)
      res
    }
  }

  val tests: Seq[String] = Seq(
    "simple/api.raml",
    "annotations/api.raml",
    "macros/api.raml",
    "modular/api.raml",
    "security/api.raml",
    "declares/api.raml",
    "tuple-shape-schema/api.raml"
//    "modular-recursion/api.raml"
  )

  tests.foreach { input =>
    val golden = input.replace("api.raml", "webapi")
    test(s"Test '$input' for WebAPI dialect transformation and yaml/json rendering") {
      checkCanonicalDialectTransformation(input, golden, shouldTranform = false)
    }
  }

  test("Test that canonical transformer only accepts Documents") {
    val unit = ExternalFragment()
    assertThrows[DocumentExpectedException](CanonicalWebAPISpecTransformer.transform(unit))
  }
}
