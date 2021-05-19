package amf.cycle

import amf.client.remod.{AMFGraphConfiguration, ParseConfiguration}
import amf.core.emitter.RenderOptions
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.{Cache, Context, Vendor}
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.services.RuntimeCompiler
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

  val basePath = "file://amf-client/shared/src/test/resources/rdf/"

  test("TrackedElement annotations are emitted to rdf") {
    rdfFromApi("apis/tracked-element.raml", Vendor.RAML10).map { n3: String =>
      n3 should include("http://a.ml/vocabularies/document-source-maps#tracked-element")
    }
  }

  override protected def beforeEach(): Future[Unit] = Validation(platform).map(_ => Unit)

  private def build(path: String): Future[BaseUnit] = {
    val fullPath = basePath + path
    RuntimeCompiler.apply(None,
                          Context(platform),
                          Cache(),
                          ParseConfiguration(AMFGraphConfiguration.fromEH(UnhandledErrorHandler), fullPath))
  }

  private def rdfFromApi(path: String, vendor: Vendor): Future[String] = {
    build(path)
      .map(transform(_, TransformationPipeline.EDITING_PIPELINE, vendor))
      .map(_.toNativeRdfModel(RenderOptions().withSourceMaps))
      .map(_.toN3())
  }
}
