package amf.cycle

import amf.client.environment.WebAPIConfiguration
import amf.client.remod.AMFGraphConfiguration
import amf.client.environment.{AsyncAPIConfiguration, WebAPIConfiguration}
import amf.client.remod.amfcore.config.RenderOptions
import amf.client.remod.{AMFGraphConfiguration, ParseConfiguration}
import amf.core.AMFCompiler
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.Vendor
import amf.core.resolution.pipelines.TransformationPipeline
import amf.facades.Validation
import amf.io.FileAssertionTest
import amf.resolution.ResolutionCapabilities
import org.mulesoft.common.test.AsyncBeforeAndAfterEach
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class ToRdfCycleTest
    extends AsyncFunSuite
    with FileAssertionTest
    with AsyncBeforeAndAfterEach
    with Matchers
    with ResolutionCapabilities {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://amf-cli/shared/src/test/resources/rdf/"

  test("TrackedElement annotations are emitted to rdf") {
    rdfFromApi("apis/tracked-element.raml", Vendor.RAML10).map { n3: String =>
      n3 should include("http://a.ml/vocabularies/document-source-maps#tracked-element")
    }
  }

  override protected def beforeEach(): Future[Unit] = Validation(platform).map(_ => Unit)

  private def build(path: String, config: AMFGraphConfiguration): Future[BaseUnit] = {
    val fullPath = basePath + path
    config.createClient().parse(fullPath).map(_.bu)
  }

  private def rdfFromApi(path: String, vendor: Vendor): Future[String] = {
    val config = WebAPIConfiguration.WebAPI().withErrorHandlerProvider(() => UnhandledErrorHandler)
    build(path, config)
      .map(transform(_, TransformationPipeline.EDITING_PIPELINE, vendor, config))
      .map(_.toNativeRdfModel(RenderOptions().withSourceMaps))
      .map(_.toN3())
  }
}
