package amf.compiler

import amf.core.annotations.{LexicalInformation, SourceAST}
import amf.core.model.document.Document
import amf.core.parser.Annotations
import amf.core.remote.OasYamlHint
import amf.plugins.domain.webapi.models.{Response, WebApi}
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class AnnotationInFieldTest extends AsyncFunSuite with CompilerTestBuilder {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test source and lexical info in response name oas") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/oas-responses.yaml",
                    OasYamlHint)
    } yield {
      val document = unit.asInstanceOf[Document]
      document.declares.collectFirst({ case r: Response => r }) match {
        case Some(res) => assertAnnotationsInName(res)
        case None      => fail("Any response declared found")
      }

      val responses = document.encodes.asInstanceOf[WebApi].endPoints.head.operations.head.responses
      responses.foreach { assertAnnotationsInName }
      succeed
    }
  }

  private def assertAnnotationsInName(res: Response): Unit = {
    val annotations = res.name.annotations()
    findLexical(res.id, annotations)
    findSourceAST(res.id, annotations)
  }
  private def findLexical(id: String, annotations: Annotations): Unit =
    if (!annotations.contains(classOf[LexicalInformation]))
      fail(s"LexicalInformation annotation not found for name in respose $id")

  private def findSourceAST(id: String, annotations: Annotations): Unit =
    if (!annotations.contains(classOf[SourceAST])) fail(s"SourceAST annotation not found for name in respose $id")

}
