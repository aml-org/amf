package amf.compiler

import amf.core.annotations.{LexicalInformation, SourceAST, SourceNode}
import amf.core.model.document.Document
import amf.core.model.domain.{AmfArray, AmfObject, Shape}
import amf.core.parser.{Annotations, Range => PositionRange}
import amf.core.remote.{Oas20JsonHint, Oas20YamlHint, Raml10YamlHint}
import amf.plugins.domain.shapes.models.{AnyShape, NodeShape}
import amf.plugins.domain.webapi.models.api.WebApi
import amf.plugins.domain.webapi.models.{Parameter, Response}
import org.mulesoft.lexer.InputRange
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.ExecutionContext

class SourceNodeAnnotationTest extends AsyncFunSuite with CompilerTestBuilder with Matchers {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test full raml") {
    build("file://amf-client/shared/src/test/resources/upanddown/cycle/raml10/full-example/api.raml", Raml10YamlHint) map {
      checkAnnotation
    }
  }

  test("test full oas") {
    build("file://amf-client/shared/src/test/resources/upanddown/full-example.json", Oas20JsonHint) map {
      checkAnnotation
    }
  }

  test("test full raml with type xml tag") {
    build("file://amf-client/shared/src/test/resources/org/raml/api/v10/xmlbodyinlinewithfacet/input.raml",
          Raml10YamlHint) map { checkAnnotation }
  }

  private def checkAnnotation(obj: AmfObject): Assertion = {
    obj.fields.foreach {
      case (field, v) =>
        v.value match {
          case amfObject: AmfObject =>
            if (amfObject.annotations.contains(classOf[SourceAST]))
              assert(amfObject.annotations.contains(classOf[SourceNode]))
            checkAnnotation(amfObject)
          case array: AmfArray if array.values.nonEmpty && array.values.head.isInstanceOf[AmfObject] =>
            array.values.asInstanceOf[Seq[AmfObject]].foreach { checkAnnotation }
          case _ =>
        }
    }
    succeed
  }

  test("test node and entry annotation in oas parameters") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/oas-parameters.yaml",
                    Oas20YamlHint)
    } yield {
      val document = unit.asInstanceOf[Document]
      document.declares.collectFirst({ case p: Parameter => (p.id, p.annotations) }) match {
        case Some((id, annotations)) =>
          assertRangeElement(id, annotations, PositionRange((4, 2), (11, 0)), Some(PositionRange((4, 5), (11, 0))))
        case None => fail("Any parameter declared found")
      }

      val endpoint = document.encodes.asInstanceOf[WebApi].endPoints.head
      val param    = endpoint.parameters.head
      assertRangeElement(param.id, param.annotations, PositionRange((14, 7), (20, 0)))
      val queryParam = endpoint.operations.head.request.queryParameters.head
      assertRangeElement(queryParam.id, queryParam.annotations, PositionRange((22, 9), (25, 19)))
      succeed
    }
  }

  test("test node and entry annotation in oas path items") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/oas-parameters.yaml",
                    Oas20YamlHint)
    } yield {
      val endpoint = unit.asInstanceOf[Document].encodes.asInstanceOf[WebApi].endPoints.head
      assertRangeElement(endpoint.id,
                         endpoint.annotations,
                         PositionRange((12, 2), (25, 19)),
                         Some(PositionRange((12, 12), (25, 19))))
      succeed
    }
  }

  test("test node and entry annotation in oas operations") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/oas-parameters.yaml",
                    Oas20YamlHint)
    } yield {
      val api = unit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
      val op  = api.endPoints.head.operations.head
      assertRangeElement(op.id,
                         op.annotations,
                         PositionRange((20, 4), (25, 19)),
                         Some(PositionRange((20, 8), (25, 19))))
      succeed
    }
  }

  test("test node and entry annotation in oas responses") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/oas-responses.yaml",
                    Oas20YamlHint)
    } yield {
      val document = unit.asInstanceOf[Document]
      document.declares.collectFirst({ case p: Response => (p.id, p.annotations) }) match {
        case Some((id, annotations)) =>
          assertRangeElement(id, annotations, PositionRange((4, 2), (10, 0)), Some(PositionRange((4, 10), (10, 0))))
        case None => fail("Any response declared found")
      }

      val api = document.encodes.asInstanceOf[WebApi]

      api.endPoints.head.operations.head.responses.collectFirst({
        case r: Response if r.linkTarget.isEmpty => (r.id, r.annotations)
      }) match {
        case Some((id, annotations)) =>
          assertRangeElement(id, annotations, PositionRange((16, 8), (19, 0)), Some(PositionRange((16, 12), (19, 0))))
        case None => fail("Any response with target found")
      }

      api.endPoints.head.operations.head.responses.collectFirst({
        case r: Response if r.linkTarget.isDefined => (r.id, r.annotations)
      }) match {
        case Some((id, annotations)) =>
          assertRangeElement(id,
                             annotations,
                             PositionRange((20, 16), (20, 37)),
                             Some(PositionRange((20, 16), (20, 37))))
        case None => fail("Any response with target found")
      }
      succeed
    }
  }

  test("test node and entry annotation in oas definitions and schemes") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/oas-schemes.yaml",
                    Oas20YamlHint)
    } yield {
      val document = unit.asInstanceOf[Document]
      document.declares.collectFirst({ case p: Shape => (p.id, p.annotations) }) match {
        case Some((id, annotations)) =>
          assertRangeElement(id, annotations, PositionRange((4, 2), (11, 0)), Some(PositionRange((4, 9), (11, 0))))
        case None => fail("Any response declared found")
      }

      val api    = document.encodes.asInstanceOf[WebApi]
      val schema = api.endPoints.head.operations.head.responses.head.payloads.head.schema
      assertRangeElement(
        schema.id,
        schema.annotations,
        PositionRange((18, 10), (20, 24)),
        Some(PositionRange((18, 17), (20, 24)))
      )
      succeed
    }
  }

  test("test node and entry annotation in oas xml shape property") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/oas-xmlSerializer.yaml",
                    Oas20YamlHint)
    } yield {
      val document = unit.asInstanceOf[Document]
      document.declares.collectFirst({
        case s: NodeShape =>
          val serialization = s.properties.head.range.asInstanceOf[AnyShape].xmlSerialization
          (serialization.id, serialization.annotations)
      }) match {
        case Some((id, annotations)) =>
          assertRangeElement(id,
                             annotations,
                             PositionRange((12, 0), (13, 21)),
                             Some(PositionRange((10, 12), (13, 21))))
        case None => fail("Any response declared found")
      }
      succeed
    }
  }

  private def assertRangeElement(id: String,
                                 annotations: Annotations,
                                 sourceRange: PositionRange,
                                 nodeRange: Option[PositionRange] = None): Assertion = {
    annotations.foreach {
      case ast: SourceAST =>
        assertRange(id, ast.ast.range, sourceRange)
      case lex: LexicalInformation =>
        assertRange(id, lex.range, sourceRange)
      case node: SourceNode =>
        assertRange(id, node.node.range, nodeRange.getOrElse(sourceRange))
      case _ =>
    }
    if (!containsSourceAnnotations(annotations)) fail("Missing some annotation type")
    succeed
  }

  def containsSourceAnnotations(annotations: Annotations): Boolean = {
    annotations.contains(classOf[LexicalInformation]) &&
    annotations.contains(classOf[SourceAST]) &&
    annotations.contains(classOf[SourceNode])
  }

  private def assertRange(id: String, actual: InputRange, expected: PositionRange): Assertion = {
    assertRange(id, PositionRange((actual.lineFrom, actual.columnFrom), (actual.lineTo, actual.columnTo)), expected)
  }

  private def assertRange(id: String, actual: PositionRange, expected: PositionRange): Assertion = {

    if (actual.equals(expected)) succeed
    else fail(s"Input range: ${actual.toString} for element $id not equals to ${expected.toString}")
  }
}
