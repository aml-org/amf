package amf.compiler

import amf.antlr.client.scala.parse.syntax.SourceASTElement
import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.model.domain.{Parameter, Response}
import amf.core.client.scala.errorhandling.IgnoringErrorHandler
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.{AmfArray, AmfObject, Shape}
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.annotations.{LexicalInformation, SourceAST, SourceNode, SourceYPart}
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.{Oas20JsonHint, Oas20YamlHint, Raml10YamlHint}
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import org.mulesoft.common.client.lexical.PositionRange
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

class SourceNodeAnnotationTest
    extends AsyncFunSuiteWithPlatformGlobalExecutionContext
    with CompilerTestBuilder
    with Matchers {

  override def defaultConfig: AMFConfiguration =
    super.defaultConfig.withErrorHandlerProvider(() => IgnoringErrorHandler)
  test("test full raml") {
    build("file://amf-cli/shared/src/test/resources/upanddown/cycle/raml10/full-example/api.raml", Raml10YamlHint) map {
      checkAnnotation
    }
  }

  test("test full oas") {
    build("file://amf-cli/shared/src/test/resources/upanddown/full-example.json", Oas20JsonHint) map {
      checkAnnotation
    }
  }

  test("test full raml with type xml tag") {
    build(
      "file://amf-cli/shared/src/test/resources/org/raml/api/v10/xmlbodyinlinewithfacet/input.raml",
      Raml10YamlHint
    ) map { checkAnnotation }
  }

  private def checkAnnotation(obj: AmfObject): Assertion = {
    obj.fields.foreach { case (field, v) =>
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
      unit <- build(
        "file://amf-cli/shared/src/test/resources/nodes-annotations-examples/oas-parameters.yaml",
        Oas20YamlHint
      )
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
      unit <- build(
        "file://amf-cli/shared/src/test/resources/nodes-annotations-examples/oas-parameters.yaml",
        Oas20YamlHint
      )
    } yield {
      val endpoint = unit.asInstanceOf[Document].encodes.asInstanceOf[WebApi].endPoints.head
      assertRangeElement(
        endpoint.id,
        endpoint.annotations,
        PositionRange((12, 2), (25, 19)),
        Some(PositionRange((12, 12), (25, 19)))
      )
      succeed
    }
  }

  test("test node and entry annotation in oas operations") {
    for {
      unit <- build(
        "file://amf-cli/shared/src/test/resources/nodes-annotations-examples/oas-parameters.yaml",
        Oas20YamlHint
      )
    } yield {
      val api = unit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
      val op  = api.endPoints.head.operations.head
      assertRangeElement(
        op.id,
        op.annotations,
        PositionRange((20, 4), (25, 19)),
        Some(PositionRange((20, 8), (25, 19)))
      )
      succeed
    }
  }

  test("test node and entry annotation in oas responses") {
    for {
      unit <- build(
        "file://amf-cli/shared/src/test/resources/nodes-annotations-examples/oas-responses.yaml",
        Oas20YamlHint
      )
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
          assertRangeElement(
            id,
            annotations,
            PositionRange((20, 16), (20, 37)),
            Some(PositionRange((20, 16), (20, 37)))
          )
        case None => fail("Any response with target found")
      }
      succeed
    }
  }

  test("test node and entry annotation in oas definitions and schemes") {
    for {
      unit <- build(
        "file://amf-cli/shared/src/test/resources/nodes-annotations-examples/oas-schemes.yaml",
        Oas20YamlHint
      )
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
      unit <- build(
        "file://amf-cli/shared/src/test/resources/nodes-annotations-examples/oas-xmlSerializer.yaml",
        Oas20YamlHint
      )
    } yield {
      val document = unit.asInstanceOf[Document]
      document.declares.collectFirst({ case s: NodeShape =>
        val serialization = s.properties.head.range.asInstanceOf[AnyShape].xmlSerialization
        (serialization.id, serialization.annotations)
      }) match {
        case Some((id, annotations)) =>
          assertRangeElement(id, annotations, PositionRange((12, 0), (13, 21)), Some(PositionRange((10, 12), (13, 21))))
        case None => fail("Any response declared found")
      }
      succeed
    }
  }

  private def assertRangeElement(
      id: String,
      annotations: Annotations,
      sourceRange: PositionRange,
      nodeRange: Option[PositionRange] = None
  ): Assertion = {
    annotations.foreach {
      case ast: SourceYPart =>
        assertRange(id, ast.ast.range, sourceRange)
      case ast: SourceASTElement =>
        assertRange(id, ast.ast.location.range, sourceRange)
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

  private def assertRange(id: String, actual: PositionRange, expected: PositionRange): Assertion = {

    if (actual.equals(expected)) succeed
    else fail(s"Input range: ${actual.toString} for element $id not equals to ${expected.toString}")
  }
}
