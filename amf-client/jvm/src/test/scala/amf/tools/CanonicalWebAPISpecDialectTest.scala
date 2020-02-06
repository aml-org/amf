package amf.tools

import amf.ProfileName
import amf.client.parse.DefaultParserErrorHandler
import amf.core.AMF
import amf.core.emitter.RenderOptions
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.services.RuntimeValidator
import amf.core.unsafe.PlatformSecrets
import amf.emit.AMFRenderer
import amf.facades.{AMFCompiler, Validation}
import amf.io.FunSuiteCycleTests
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.webapi.Raml10Plugin
import amf.tools.canonical.CanonicalWebAPISpecTransformer
import org.scalatest.Assertion

import scala.concurrent.Future

class CanonicalWebAPISpecDialectTest extends FunSuiteCycleTests with PlatformSecrets {

  val CANONICAL_WEBAPI_DIALECT  = "file://vocabularies/dialects/canonical_webapi_spec.yaml"
  override def basePath: String = "file://amf-client/shared/src/test/resources/transformations/"

  def checkCanonicalDialectTransformation(source: String, target: String, shouldTranform: Boolean): Future[Assertion] = {
    val amfWebApi  = basePath + source
    val goldenYaml = s"$basePath$target.yaml"
    val goldenJson = s"$basePath$target.json"
    val eh         = DefaultParserErrorHandler.withRun()
    for {
      _    <- AMF.init()
      _    <- Future(amf.Core.registerPlugin(AMLPlugin))
      v    <- Validation(platform)
      d    <- AMFCompiler(CANONICAL_WEBAPI_DIALECT, platform, VocabularyYamlHint, eh = eh).build()
      _    <- Future { AMLPlugin.registry.resolveRegisteredDialect(d.asInstanceOf[Dialect].header) }
      unit <- AMFCompiler(amfWebApi, platform, RamlYamlHint, eh = eh).build()
      resolved <- {
        if (shouldTranform) {
          Future { Raml10Plugin.resolve(unit, eh, ResolutionPipeline.EDITING_PIPELINE) }
        } else {
          Future(unit)
        }
      }
      transformed <- CanonicalWebAPISpecTransformer.transform(resolved)
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
//    "simple/api.raml",
//    "annotations/api.raml",
//    "macros/api.raml",
//    "modular/api.raml",
    "modular-recursion/api.raml"

    // "file://amf-client/shared/src/test/resources/production/raml10/banking-api/api.raml.jsonld" -> "banking-api.webapi.yaml",
    // "file://amf-client/shared/src/test/resources/upanddown/banking-api.raml.jsonld" -> "banking-api.webapi.yaml",

//    "file://amf-client/shared/src/test/resources/upanddown/cycle/raml10/all-type-types/api.raml.jsonld" -> "all-type-types.webapi.yaml",

//    "file://amf-client/shared/src/test/resources/upanddown/cycle/raml10/secured-by/api.raml.jsonld" -> "secured-by.webapi.yaml",
//    TODO: positions moving
//    "file://amf-client/shared/src/test/resources/production/raml10/banking-api/api.raml.jsonld"     -> "full-banking-api.webapi.yaml",
//    "file://amf-tools/jvm/src/test/resources/input/sample.raml.resolved.jsonld"                     -> "sample.webapi.yaml"
  )

  /*
  val resolve: Map[String, Boolean] = Map(
    "file://amf-client/shared/src/test/resources/upanddown/cycle/raml10/secured-by/sample.oas.resolved.jsonld" -> false,
    "file://amf-client/shared/src/test/resources/upanddown/cycle/raml10/secured-by/api.raml.jsonld" -> false
  )
   */

  tests.foreach { input =>
    val golden = input.replace("api.raml", "webapi")
    (1 to 5).foreach { n =>
      test(s"Test WebAPI dialect transformation and yaml/json rendering '$input' ($n)") {
        checkCanonicalDialectTransformation(input, golden, shouldTranform = false)
      }
    }
  }
}
