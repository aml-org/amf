package amf.cycle

import amf.apicontract.client.scala.WebAPIConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.rdf.RdfUnitConverter
import amf.core.internal.remote.SpecId
import amf.core.internal.unsafe.PlatformSecrets
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
    with ResolutionCapabilities
    with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://amf-cli/shared/src/test/resources/rdf/"

  test("TrackedElement annotations are emitted to rdf") {
    rdfFromApi("apis/tracked-element.raml", SpecId.RAML10).map { n3: String =>
      n3 should include("http://a.ml/vocabularies/document-source-maps#tracked-element")
    }
  }

  private def build(path: String, config: AMFGraphConfiguration): Future[BaseUnit] = {
    val fullPath = basePath + path
    config.baseUnitClient().parse(fullPath).map(_.baseUnit)
  }

  private def rdfFromApi(path: String, vendor: SpecId): Future[String] = {
    val config = WebAPIConfiguration.WebAPI().withErrorHandlerProvider(() => UnhandledErrorHandler)
    build(path, config)
      .map(transform(_, PipelineId.Editing, vendor, config))
      .map(bu => RdfUnitConverter.toNativeRdfModel(bu, RenderOptions().withSourceMaps))
      .map(_.toN3())
  }
}
