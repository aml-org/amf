package amf.resolution

import amf.apicontract.client.scala.AsyncAPIConfiguration
import amf.apicontract.client.scala.model.domain.api.AsyncApi
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.model.document.Document
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class Async20ResolutionModelTest extends AsyncFunSuite {
  override val executionContext: ExecutionContext = global

  private val basePath: String = "amf-cli/shared/src/test/resources/references/async/"

  test("async 2.0 should resolve inner references correctly") {
    processApi(basePath + "async20.yaml") map { document =>
      val endPoints = document.encodes.asInstanceOf[AsyncApi].endPoints
      assert(endPoints.nonEmpty)
      val operations = endPoints.head.operations
      assert(operations.nonEmpty)
      val payloads = operations.head.request.payloads
      assert(payloads.nonEmpty)
      val schema = payloads.head.schema
      assert(schema.isInstanceOf[NodeShape])
      val properties = schema.asInstanceOf[NodeShape].properties
      assert(properties.nonEmpty)
      val range = properties.head.range
      assert(range.isInstanceOf[ScalarShape])
    }
  }

  test("async 2.6 should resolve inner references correctly") {
    processApi(basePath + "async26.yaml") map { document =>
      val endPoints = document.encodes.asInstanceOf[AsyncApi].endPoints
      assert(endPoints.nonEmpty)
      val operations = endPoints.head.operations
      assert(operations.nonEmpty)
      val payloads = operations.head.request.payloads
      assert(payloads.nonEmpty)
      val schema = payloads.head.schema
      assert(schema.isInstanceOf[NodeShape])
      val properties = schema.asInstanceOf[NodeShape].properties
      assert(properties.nonEmpty)
      val range = properties.head.range
      assert(range.isInstanceOf[ScalarShape])
    }
  }

  private def processApi(path: String): Future[Document] = {
    val client = AsyncAPIConfiguration.Async20().baseUnitClient()
    client
      .parse("file://" + path)
      .map(p => client.transform(p.baseUnit, PipelineId.Editing))
      .map(_.baseUnit.asInstanceOf[Document])
  }

}
