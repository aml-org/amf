package amf.compiler

import amf.core.annotations.{LexicalInformation, SourceAST}
import amf.core.model.document.Document
import amf.core.model.domain.NamedDomainElement
import amf.core.parser.Annotations
import amf.core.remote.OasYamlHint
import amf.plugins.domain.webapi.models.{Parameter, Response, WebApi}
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
        case Some(res) => assertAnnotationsInName(res.id, res)
        case None      => fail("Any response declared found")
      }

      val responses = document.encodes.asInstanceOf[WebApi].endPoints.head.operations.head.responses
      responses.foreach { r =>
        assertAnnotationsInName(r.id, r)
      }
      succeed
    }
  }

  test("test source and lexical info in parameter name oas") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/oas-parameters.yaml",
                    OasYamlHint)
    } yield {
      val document = unit.asInstanceOf[Document]
      document.declares.collectFirst({ case r: Parameter => r }) match {
        case Some(p) => assertAnnotationsInName(p.id, p)
        case None    => fail("Any parameter declared found")
      }
      succeed
    }
  }

  private def assertAnnotationsInName(id: String, element: NamedDomainElement): Unit = {
    val annotations = element.name.annotations()
    findLexical(id, annotations)
    findSourceAST(id, annotations)
  }
  private def findLexical(id: String, annotations: Annotations): Unit =
    if (!annotations.contains(classOf[LexicalInformation]))
      fail(s"LexicalInformation annotation not found for name in respose $id")

  private def findSourceAST(id: String, annotations: Annotations): Unit =
    if (!annotations.contains(classOf[SourceAST])) fail(s"SourceAST annotation not found for name in respose $id")

}
