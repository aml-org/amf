package amf.tools

import amf.ProfileName
import amf.client.parse.DefaultParserErrorHandler
import amf.core.AMF
import amf.core.emitter.RenderOptions
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.services.RuntimeValidator
import amf.core.unsafe.PlatformSecrets
import amf.emit.AMFRenderer
import amf.facades.{AMFCompiler, Validation}
import amf.io.BuildCycleTests
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.webapi.Raml10Plugin
import amf.tools.canonical.{CanonicalWebAPISpecTransformer, CanonicalWebAPITransformer}
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.Future

class CanonicalWebAPIDialectTest extends AsyncFunSuite with BuildCycleTests with PlatformSecrets {

  val CANONICAL_WEBAPI_DIALECT  = "file://vocabularies/dialects/canonical_webapi_spec.yaml"
  override def basePath: String = "file://amf-client/shared/src/test/resources/transformations/"

  def checkCanonicalDialectTransformation(source: String, target: String, shouldTranform: Boolean): Future[Assertion] = {
    val amfWebApi = basePath + source
    val golden    = basePath + target
    val eh        = DefaultParserErrorHandler.withRun()
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
      // jsonld          <- new AMFRenderer(resolved, Vendor.AMF, RenderOptions(), Some(Syntax.Json)).renderToString
      dialectInstance <- CanonicalWebAPISpecTransformer().transform(resolved)
      // jsonld          <- new AMFRenderer(dialectInstance, Vendor.AMF, RenderOptions(), Some(Syntax.Json)).renderToString
      rendered <- new AMFRenderer(dialectInstance, Vendor.AML, RenderOptions().withNodeIds, Some(Syntax.Yaml)).renderToString
      tmp      <- writeTemporaryFile(golden)(rendered)
      res <- {
        // println(jsonld)
        assertLinesDifferences(tmp, golden)
      }
      report <- {
        RuntimeValidator(
          dialectInstance,
          ProfileName(CanonicalWebAPITransformer.CANONICAL_WEBAPI_NAME)
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
    "macros/api.raml"
//    "modular/api.raml"

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

  tests.foreach {
    case input =>
      val golden = input.replace("api.raml", "webapi.yaml")
      test(s"Test parsed RAML/OAS WebAPIs can be re-parsed with the WebAPI dialect '$golden'") {
        checkCanonicalDialectTransformation(input, golden, shouldTranform = false)
      }
  }
}
