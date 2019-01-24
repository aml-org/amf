package amf.tools.canonical

import amf.ProfileName
import amf.core.AMF
import amf.core.emitter.RenderOptions
import amf.core.remote.{AmfJsonHint, Syntax, Vendor, VocabularyYamlHint}
import amf.core.services.RuntimeValidator
import amf.core.unsafe.PlatformSecrets
import amf.emit.AMFRenderer
import amf.facades.{AMFCompiler, Validation}
import amf.io.BuildCycleTests
import amf.plugins.document.vocabularies.AMLPlugin
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class CanonicalWebAPIDialectTest extends AsyncFunSuite with BuildCycleTests with  PlatformSecrets {

  val CANONICAL_WEBAPI_DIALECT = "file://vocabularies/dialects/canonical_webapi.yaml"
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  override def basePath: String = "amf-tools/jvm/src/test/resources/transformed/"

  def checkCanonicalDialectTransformation(amfWebApi: String, canonicalTarget: String): Future[Assertion] = {
    val golden = basePath + canonicalTarget
    for {
      _               <- AMF.init()
      _               <- Future(amf.Core.registerPlugin(AMLPlugin))
      v               <- Validation(platform).map(_.withEnabledValidation(true))
      _               <- AMFCompiler(CANONICAL_WEBAPI_DIALECT, platform, VocabularyYamlHint, v).build()
      unit            <- AMFCompiler(amfWebApi, platform, AmfJsonHint, v).build()
      dialectInstance <- CanonicalWebAPITransformer.transform(unit)
      rendered        <- new AMFRenderer(dialectInstance, Vendor.AML, RenderOptions(), Some(Syntax.Yaml)).renderToString
      tmp             <- writeTemporaryFile(golden)(rendered)
      res             <- assertDifferences(tmp, golden)
      report          <- {
        RuntimeValidator(
          dialectInstance,
          ProfileName(CanonicalWebAPITransformer.CANONICAL_WEBAPI_NAME)
        )
      }
    } yield {
      //println("RENDERED")
      //println(rendered)
      assert(report.conforms)
      res
    }
  }

  test("HERE_HERE Test parsed RAML/OAS WebAPIs can be re-parsed with the WebAPI dialect") {
    val input = "file://amf-client/shared/src/test/resources/upanddown/banking-api.raml.jsonld"
    val golden = "banking-api.webapi.yaml"
    checkCanonicalDialectTransformation(input, golden)
  }

}
