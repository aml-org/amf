package amf.tools

import amf.ProfileName
import amf.core.AMF
import amf.core.emitter.RenderOptions
import amf.core.parser.UnhandledErrorHandler
import amf.core.remote._
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

  val CANONICAL_WEBAPI_DIALECT = "file://vocabularies/dialects/canonical_webapi_spec.yaml"
  override def basePath: String = "amf-tools/jvm/src/test/resources/transformed/"

  def checkCanonicalDialectTransformation(amfWebApi: String,
                                          canonicalTarget: String,
                                          shouldTranform: Boolean): Future[Assertion] = {
    val golden = basePath + canonicalTarget
    for {
      _               <- AMF.init()
      _               <- Future(amf.Core.registerPlugin(AMLPlugin))
      v               <- Validation(platform).map(_.withEnabledValidation(true))
      d               <- AMFCompiler(CANONICAL_WEBAPI_DIALECT, platform, VocabularyYamlHint, v).build()
      _               <- Future { AMLPlugin.registry.resolveRegisteredDialect(d.asInstanceOf[Dialect].header) }
      unit            <- AMFCompiler(amfWebApi, platform, AmfJsonHint, v).build()
      resolved        <- {
        if (shouldTranform)
          Future { Raml10Plugin.resolve(unit, UnhandledErrorHandler) } else
          Future(unit)
      }
      dialectInstance <- CanonicalWebAPISpecTransformer.transform(resolved)
      rendered        <- new AMFRenderer(dialectInstance, Vendor.AML, RenderOptions(), Some(Syntax.Yaml)).renderToString
      tmp             <- writeTemporaryFile(golden)(rendered)
      res             <- assertDifferences(tmp, golden)
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

  val tests: Map[String, String] = Map(
//    TODO: positions moving
//    "file://amf-client/shared/src/test/resources/upanddown/banking-api.raml.jsonld"        -> "banking-api.webapi.yaml",

//    "file://amf-client/shared/src/test/resources/upanddown/cycle/raml10/all-type-types/api.raml.jsonld" -> "all-type-types.webapi.yaml",
//     TODO: refactor annotations as common logic for webapi | dialects
//    "file://amf-client/shared/src/test/resources/upanddown/annotations.raml.jsonld" -> "annotations.webapi.yaml",
//     TODO: data nodes
//    "file://amf-client/shared/src/test/resources/upanddown/cycle/raml10/jukebox-api/api.raml.jsonld" -> "jukebox-api.webapi.yaml"

//    "file://amf-client/shared/src/test/resources/upanddown/cycle/raml10/secured-by/api.raml.jsonld" -> "secured-by.webapi.yaml",
//    TODO: positions moving
    "file://amf-client/shared/src/test/resources/production/raml10/banking-api/api.raml.jsonld"     -> "full-banking-api.webapi.yaml",
//    "file://amf-tools/jvm/src/test/resources/input/sample.raml.resolved.jsonld"                     -> "sample.webapi.yaml"
  )

  val resolve: Map[String, Boolean] = Map(
    "file://amf-client/shared/src/test/resources/upanddown/cycle/raml10/secured-by/sample.oas.resolved.jsonld" -> false,
    "file://amf-client/shared/src/test/resources/upanddown/cycle/raml10/secured-by/api.raml.jsonld" -> false
  )

  tests.foreach {case (input, golden) =>
    ignore(s"HERE_HERE Test parsed RAML/OAS WebAPIs can be re-parsed with the WebAPI dialect '${golden}'") {
      checkCanonicalDialectTransformation(input, golden, resolve.get(input).getOrElse(false))
    }
  }
}
