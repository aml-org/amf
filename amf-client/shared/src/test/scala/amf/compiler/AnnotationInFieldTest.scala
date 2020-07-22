package amf.compiler

import amf.core.annotations.{LexicalInformation, ReferenceTargets, SourceAST}
import amf.core.model.document.Document
import amf.core.model.domain.NamedDomainElement
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.{Annotations, Position}
import amf.core.remote.{OasYamlHint, RamlYamlHint}
import amf.plugins.domain.shapes.models.NodeShape
import amf.plugins.domain.webapi.models.templates.ResourceType
import amf.plugins.domain.webapi.models.{Parameter, Response, WebApi}
import org.scalatest.AsyncFunSuite
import amf.core.parser.Range
import amf.plugins.document.webapi.annotations.ExternalJsonSchemaShape

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

  test("test empty oas parameter") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/empty-parameter.json",
                    OasYamlHint)
    } yield {
      val document = unit.asInstanceOf[Document]
      document.encodes.asInstanceOf[WebApi].endPoints.head.parameters.headOption match {
        case Some(p) => findLexical(p.id, p.annotations)
        case None    => fail("Any parameter declared found")
      }
      succeed
    }
  }

  test("test annotation at property path") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/property-path.yaml",
                    OasYamlHint)
    } yield {
      val document                       = unit.asInstanceOf[Document]
      val properties: Seq[PropertyShape] = document.declares.head.asInstanceOf[NodeShape].properties
      assertRange(properties.head.range.name.annotations().find(classOf[LexicalInformation]).get.range,
                  new amf.core.parser.Range(Position(7, 8), Position(7, 9)))
      assertRange(
        properties
          .find(_.name.value() == "n")
          .get
          .range
          .name
          .annotations()
          .find(classOf[LexicalInformation])
          .get
          .range,
        new amf.core.parser.Range(Position(9, 8), Position(9, 9))
      )
      assertRange(
        properties
          .find(_.name.value() == "a")
          .get
          .range
          .name
          .annotations()
          .find(classOf[LexicalInformation])
          .get
          .range,
        new amf.core.parser.Range(Position(11, 8), Position(11, 9))
      )
      assertRange(
        properties
          .find(_.name.value() == "k")
          .get
          .range
          .name
          .annotations()
          .find(classOf[LexicalInformation])
          .get
          .range,
        new amf.core.parser.Range(Position(14, 8), Position(14, 9))
      )
      succeed
    }
  }

  test("test annotation at raml property path") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/property-path.raml",
                    RamlYamlHint)
    } yield {
      val document                       = unit.asInstanceOf[Document]
      val properties: Seq[PropertyShape] = document.declares.head.asInstanceOf[NodeShape].properties

      assertRange(properties.head.range.name.annotations().find(classOf[LexicalInformation]).get.range,
                  new amf.core.parser.Range(Position(8, 6), Position(8, 7)))
      assertRange(
        properties
          .find(_.name.value() == "n")
          .get
          .range
          .name
          .annotations()
          .find(classOf[LexicalInformation])
          .get
          .range,
        new amf.core.parser.Range(Position(9, 6), Position(9, 7))
      )
      assertRange(
        properties
          .find(_.name.value() == "a")
          .get
          .range
          .name
          .annotations()
          .find(classOf[LexicalInformation])
          .get
          .range,
        new amf.core.parser.Range(Position(11, 6), Position(11, 7))
      )
      assertRange(
        properties
          .find(_.name.value() == "k")
          .get
          .range
          .name
          .annotations()
          .find(classOf[LexicalInformation])
          .get
          .range,
        new amf.core.parser.Range(Position(12, 6), Position(12, 7))
      )

      succeed
    }
  }

  test("test raml resource type position") {
    for {
      unit <- build("file://amf-client/shared/src/test/resources/nodes-annotations-examples/resource-type.raml",
                    RamlYamlHint)
    } yield {
      val document = unit.asInstanceOf[Document]
      val point    = document.declares.head.asInstanceOf[ResourceType].asEndpoint(document)
      assertRange(point.annotations.find(classOf[LexicalInformation]).get.range,
                  amf.core.parser.Range((6, 2), (9, 12)))
      assertRange(point.path.annotations().find(classOf[LexicalInformation]).get.range,
                  amf.core.parser.Range((6, 2), (6, 5)))
      succeed
    }
  }

  test("test raml ReferenceTarget annotations - ExternalFragment") {
    val uri = "file://amf-client/shared/src/test/resources/nodes-annotations-examples/reference-targets/"
    for {
      unit <- build(s"${uri}root.raml", RamlYamlHint)
    } yield {
      val targets = unit.annotations.find(classOf[ReferenceTargets]).map(_.targets).getOrElse(Map.empty)
      assert(targets.size == 1)
      assert(targets.head._1 == s"${uri}reference.json")
      assert(targets.head._2 == Range(Position(6, 5), Position(6, 28)))
      succeed
    }
  }

  test("test raml ReferenceTarget annotations - DataType") {
    val uri = "file://amf-client/shared/src/test/resources/nodes-annotations-examples/reference-targets/"
    for {
      unit <- build(s"${uri}root-2.raml", RamlYamlHint)
    } yield {
      val targets = unit.annotations.find(classOf[ReferenceTargets]).map(_.targets).getOrElse(Map.empty)
      assert(targets.size == 1)
      assert(targets.head._1 == s"${uri}reference.raml")
      assert(targets.head._2 == Range(Position(6, 5), Position(6, 28)))
      succeed
    }
  }

  test("test raml ReferenceTarget annotations - double External") {
    val uri = "file://amf-client/shared/src/test/resources/nodes-annotations-examples/reference-targets/"
    for {
      unit <- build(s"${uri}root-3.raml", RamlYamlHint)
    } yield {
      val targets = unit.annotations.find(classOf[ReferenceTargets]).map(_.targets).getOrElse(Map.empty)
      val reftargets =
        unit.references.head.annotations.find(classOf[ReferenceTargets]).map(_.targets).getOrElse(Map.empty)

      assert(targets.size == 1)
      assert(targets.head._1 == s"${uri}reference-1.yaml")
      assert(targets.head._2 == Range(Position(6, 5), Position(6, 30)))

      assert(reftargets.size == 1)
      assert(reftargets.head._1 == s"${uri}reference.json")
      assert(reftargets.head._2 == Range(Position(1, 6), Position(1, 29)))

      succeed
    }
  }

  test("test raml ReferenceTarget annotations coincides with references (invalid inclusion)") {
    val uri =
      "file://amf-client/shared/src/test/resources/nodes-annotations-examples/reference-targets/invalid-include/"
    for {
      unit <- build(s"${uri}api.raml", RamlYamlHint)
    } yield {
      val targets = unit.annotations.find(classOf[ReferenceTargets]).map(_.targets).getOrElse(Map.empty)

      assert(targets.size == 1)
      assert(unit.references.size == 1)
      assert(targets.head._1 == unit.references.head.id)

      succeed
    }
  }

  test("Test ExternalJsonSchemaShape in JSON ref to shape in external fragment") {
    val uri = "file://amf-client/shared/src/test/resources/nodes-annotations-examples/ref-to-shape/api.yaml"
    for {
      unit <- build(uri, OasYamlHint)
    } yield {
      val shape = unit
        .asInstanceOf[Document]
        .encodes
        .asInstanceOf[WebApi]
        .endPoints
        .head
        .operations
        .head
        .responses
        .head
        .payloads
        .head
        .schema

      val annotation = shape.annotations.find(classOf[ExternalJsonSchemaShape])
      assert(annotation.nonEmpty)
      assert(annotation.get.original != null)

      succeed
    }
  }

  private def assertRange(actual: amf.core.parser.Range, expected: amf.core.parser.Range) = {
    assert(actual.start.line == expected.start.line)
    assert(actual.start.column == expected.start.column)
    assert(actual.end.line == expected.end.line)
    assert(actual.end.column == expected.end.column)
  }

  private def assertAnnotationsInName(id: String, element: NamedDomainElement): Unit = {
    val annotations = element.name.annotations()
    findLexical(id, annotations)
    findSourceAST(id, annotations)
  }
  private def findLexical(id: String, annotations: Annotations): Unit =
    if (!annotations.contains(classOf[LexicalInformation]))
      fail(s"LexicalInformation annotation not found for name in response $id")

  private def findSourceAST(id: String, annotations: Annotations): Unit =
    if (!annotations.contains(classOf[SourceAST])) fail(s"SourceAST annotation not found for name in response $id")

}
