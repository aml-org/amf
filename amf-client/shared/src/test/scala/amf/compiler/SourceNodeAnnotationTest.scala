package amf.compiler

import amf.core.annotations.{LexicalInformation, SourceAST, SourceNode}
import amf.core.model.document.Document
import amf.core.model.domain.{AmfArray, AmfObject, Shape}
import amf.core.remote.{OasJsonHint, OasYamlHint, RamlYamlHint}
import amf.plugins.domain.webapi.models.{Parameter, Response, WebApi}
import org.mulesoft.lexer.InputRange
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}
import amf.core.parser.{Annotations, Range => PositionRange}
import amf.plugins.domain.shapes.models.{AnyShape, NodeShape}

import scala.concurrent.ExecutionContext

class SourceNodeAnnotationTest extends AsyncFunSuite with CompilerTestBuilder with Matchers {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test full raml") {
    build("file://amf-client/shared/src/test/resources/upanddown/full-example.raml", RamlYamlHint) map {
      checkAnnotation
    }
  }

  test("test full oas") {
    build("file://amf-client/shared/src/test/resources/upanddown/full-example.json", OasJsonHint) map {
      checkAnnotation
    }
  }

  test("test full raml with type xml tag") {
    build("file://amf-client/shared/src/test/resources/org/raml/api/v10/xmlbodyinlinewithfacet/input.raml",
          RamlYamlHint) map { checkAnnotation }
  }

  private def checkAnnotation(obj: AmfObject): Assertion = {
    obj.fields.foreach {
      case (field, value) =>
        value.value match {
          case amfObject: AmfObject =>
            if (amfObject.annotations.contains(classOf[SourceAST]))
              assert(amfObject.annotations.contains(classOf[SourceNode]))
            checkAnnotation(amfObject)
          case array: AmfArray if array.values.head.isInstanceOf[AmfObject] =>
            array.values.asInstanceOf[Seq[AmfObject]].foreach { checkAnnotation }
          case _ =>
        }
    }
    succeed
  }

  test("test node and entry annotation in oas parameters") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/oas-parameters.yaml",
                    OasYamlHint)
    } yield {
      val document = unit.asInstanceOf[Document]
      document.declares.collectFirst({ case p: Parameter => p.annotations }) match {
        case Some(annotations) =>
          assertRange(annotations, PositionRange((4, 2), (11, 0)), Some(PositionRange((4, 5), (11, 0))))
        case None => fail("Any parameter declared found")
      }

      val api = document.encodes.asInstanceOf[WebApi]
      assertRange(api.endPoints.head.parameters.head.annotations, PositionRange((14, 7), (20, 0)))
      assertRange(api.endPoints.head.operations.head.request.queryParameters.head.annotations,
                  PositionRange((22, 9), (25, 19)))
      succeed
    }
  }

  test("test node and entry annotation in oas path items") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/oas-parameters.yaml",
                    OasYamlHint)
    } yield {
      val api = unit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
      assertRange(api.endPoints.head.annotations,
                  PositionRange((12, 2), (25, 19)),
                  Some(PositionRange((12, 12), (25, 19))))
      succeed
    }
  }

  test("test node and entry annotation in oas operations") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/oas-parameters.yaml",
                    OasYamlHint)
    } yield {
      val api = unit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
      assertRange(api.endPoints.head.operations.head.annotations,
                  PositionRange((20, 4), (25, 19)),
                  Some(PositionRange((20, 8), (25, 19))))
      succeed
    }
  }

  test("test node and entry annotation in oas responses") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/oas-responses.yaml",
                    OasYamlHint)
    } yield {
      val document = unit.asInstanceOf[Document]
      document.declares.collectFirst({ case p: Response => p.annotations }) match {
        case Some(annotations) =>
          assertRange(annotations, PositionRange((4, 2), (10, 0)), Some(PositionRange((4, 10), (10, 0))))
        case None => fail("Any response declared found")
      }

      val api = document.encodes.asInstanceOf[WebApi]
      assertRange(api.endPoints.head.operations.head.responses.head.annotations,
                  PositionRange((16, 8), (18, 30)),
                  Some(PositionRange((16, 12), (18, 30))))
      succeed
    }
  }

  test("test node and entry annotation in oas definitions and schemes") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/oas-schemes.yaml",
                    OasYamlHint)
    } yield {
      val document = unit.asInstanceOf[Document]
      document.declares.collectFirst({ case p: Shape => p.annotations }) match {
        case Some(annotations) =>
          assertRange(annotations, PositionRange((4, 2), (11, 0)), Some(PositionRange((4, 9), (11, 0))))
        case None => fail("Any response declared found")
      }

      val api = document.encodes.asInstanceOf[WebApi]
      assertRange(
        api.endPoints.head.operations.head.responses.head.payloads.head.schema.annotations,
        PositionRange((18, 10), (20, 24)),
        Some(PositionRange((18, 17), (20, 24)))
      )
      succeed
    }
  }

  test("test node and entry annotation in oas xml shape property") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/oas-xmlSerializer.yaml",
                    OasYamlHint)
    } yield {
      val document = unit.asInstanceOf[Document]
      document.declares.collectFirst({
        case s: NodeShape =>
          s.properties.head.range.asInstanceOf[AnyShape].xmlSerialization.annotations
      }) match {
        case Some(annotations) =>
          assertRange(annotations, PositionRange((12, 0), (13, 21)), Some(PositionRange((10, 12), (13, 21))))
        case None => fail("Any response declared found")
      }
      succeed
    }
  }

  private def assertRange(annotations: Annotations,
                          sourceRange: PositionRange,
                          nodeRange: Option[PositionRange] = None): Assertion = {
    var c = 0
    annotations.foreach {
      case ast: SourceAST =>
        c = c + 1
        assertRange(ast.ast.range, sourceRange)
      case lex: LexicalInformation =>
        c = c + 1
        assertRange(lex.range, sourceRange)
      case node: SourceNode =>
        c = c + 1
        assertRange(node.node.range, nodeRange.getOrElse(sourceRange))
      case _ =>
    }
    if (c != 3) fail("Missing some annotation type")
    succeed
  }

  private def assertRange(actual: InputRange, expected: PositionRange): Assertion = {
    assertRange(PositionRange((actual.lineFrom, actual.columnFrom), (actual.lineTo, actual.columnTo)), expected)
  }

  private def assertRange(actual: PositionRange, expected: PositionRange): Assertion = {
    actual should be(expected)
  }
}
