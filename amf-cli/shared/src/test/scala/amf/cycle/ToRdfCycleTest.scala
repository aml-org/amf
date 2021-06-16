package amf.cycle

import amf.apicontract.client.scala.config.WebAPIConfiguration
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.internal.remote.Vendor
import amf.core.internal.unsafe.PlatformSecrets
import amf.facades.Validation
import amf.io.FileAssertionTest
import amf.resolution.ResolutionCapabilities
import org.mulesoft.common.test.AsyncBeforeAndAfterEach
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

// TODO: ARM uncomment
class ToRdfCycleTest
    extends AsyncFunSuite
    with FileAssertionTest
    with AsyncBeforeAndAfterEach
    with Matchers
    with ResolutionCapabilities
    with PlatformSecrets {

//  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
//
//  val basePath = "file://amf-cli/shared/src/test/resources/rdf/"
//
//  test("TrackedElement annotations are emitted to rdf") {
//    rdfFromApi("apis/tracked-element.raml", Vendor.RAML10).map { n3: String =>
//      n3 should include("http://a.ml/vocabularies/document-source-maps#tracked-element")
//    }
//  }
//
//  override protected def beforeEach(): Future[Unit] = Validation(platform).map(_ => Unit)
//
//  private def build(path: String, config: AMFGraphConfiguration): Future[BaseUnit] = {
//    val fullPath = basePath + path
//    config.createClient().parse(fullPath).map(_.bu)
//  }
//
//  private def rdfFromApi(path: String, vendor: Vendor): Future[String] = {
//    val config = WebAPIConfiguration.WebAPI().withErrorHandlerProvider(() => UnhandledErrorHandler)
//    build(path, config)
//      .map(transform(_, PipelineId.Editing, vendor, config))
//      .map(bu => new RdfExportable())
//      .map(_.toN3())
//  }
}
