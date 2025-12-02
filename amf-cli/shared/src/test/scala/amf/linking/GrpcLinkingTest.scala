package amf.linking

import amf.cache.CustomUnitCache
import amf.core.client.scala.config.CachedReference
import amf.core.client.scala.errorhandling.{ErrorHandlerProvider, UnhandledErrorHandler}
import amf.core.client.scala.model.document.Document
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.grpc.client.scala.GRPCConfiguration
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class GrpcLinkingTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val basePath: String           = "file://amf-cli/shared/src/test/resources/upanddown/grpc/ref/"
  val errorHandler: ErrorHandlerProvider = () => UnhandledErrorHandler

  test("gRPC document could be referenced from another gRPC document (not External Fragment)") {
    val refPath = "lib.proto"
    for {
      referenced <- withGrpcReference(refPath)
      doc <- {
        val cache = buildCache("lib.proto", referenced)
        getConfig(Some(cache))
          .baseUnitClient()
          .parseDocument(computePath("ref.proto"))
      }
    } yield {
      doc.conforms shouldBe true
      val references   = doc.document.references
      val declarations = doc.document.declares
      references.size shouldBe 1
      declarations.size shouldBe 1
      references.head.isInstanceOf[Document] shouldBe true
      references.head.location().get should include("lib.proto")
      declarations.head.isInstanceOf[NodeShape] shouldBe true
    }
  }

  private def buildCache(key: String, component: Document) = {
    CustomUnitCache(List(CachedReference(computePath(key), component)))
  }

  private def getConfig(cache: Option[CustomUnitCache] = None) = {
    val configuration = GRPCConfiguration.GRPC().withErrorHandlerProvider(errorHandler)
    cache match {
      case Some(c) => configuration.withUnitCache(c)
      case None    => configuration
    }
  }

  private def withGrpcReference(uri: String): Future[Document] = {
    getConfig()
      .baseUnitClient()
      .parse(basePath + uri)
      .map(_.baseUnit.asInstanceOf[Document])
  }

  protected def computePath(ref: String): String = {
    if (basePath.startsWith("file://")) basePath + ref
    else s"file://$basePath" + ref
  }
}
