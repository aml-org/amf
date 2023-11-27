package amf.cycle

import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.Spec
import amf.core.io.FileAssertionTest
import amf.rdf.client.scala.RdfUnitConverter
import amf.resolution.ResolutionCapabilities
import amf.testing.ConfigProvider.configFor
import org.mulesoft.common.test.AsyncBeforeAndAfterEach
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class ToRdfCycleTest extends FileAssertionTest with AsyncBeforeAndAfterEach with Matchers with ResolutionCapabilities {

  val basePath = "file://amf-cli/shared/src/test/resources/rdf/"

  test("TrackedElement annotations are emitted to rdf") {
    rdfFromApi("apis/tracked-element.raml", Spec.RAML10).map { n3: String =>
      n3 should include("http://a.ml/vocabularies/document-source-maps#tracked-element")
    }
  }

  private def build(path: String, config: AMFGraphConfiguration): Future[BaseUnit] = {
    val fullPath = basePath + path
    config.baseUnitClient().parse(fullPath).map(_.baseUnit)
  }

  private def rdfFromApi(path: String, spec: Spec): Future[String] = {
    val config = configFor(spec).withErrorHandlerProvider(() => UnhandledErrorHandler)
    build(path, config)
      .map(transform(_, PipelineId.Editing, spec, config))
      .map(bu => RdfUnitConverter.toNativeRdfModel(bu, RenderOptions().withSourceMaps))
      .map(_.toN3())
  }
}
