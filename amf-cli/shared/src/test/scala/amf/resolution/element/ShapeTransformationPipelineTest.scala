package amf.resolution.element
import amf.core.client.common.validation.ProfileNames
import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.annotations.SourceLocation
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.domain.models._
import amf.shapes.internal.domain.resolution.elements.CompleteShapeTransformationPipeline
import org.scalatest.FunSuite
import org.scalatest.Matchers._

class ShapeTransformationPipelineTest extends FunSuite {

  test("test shape with linkable") {
    val ann = Annotations() += SourceLocation("file://location.com")
    val prop1 = PropertyShape(ann)
      .withId("file://location.com#/prop1")
      .withName("prop1")
      .withPath("prop1")
      .withRange(ScalarShape(ann).withId("file://location.com#/prop1range").withDataType(DataTypes.String))
    val prop2 = PropertyShape(ann)
      .withId("file://location.com#/prop2")
      .withName("prop2")
      .withPath("prop2")
      .withRange(ScalarShape(ann).withId("file://location.com#/prop2range").withDataType(DataTypes.Integer))

    val target = NodeShape(ann).withId("file://location.com/#nodeshape1").withProperties(Seq(prop1, prop2))
    val origin =
      NodeShape(ann).withId("file://location.com/#nodeshape2").withLinkLabel("linkTo").withLinkTarget(target)

    val result: Shape =
      new CompleteShapeTransformationPipeline(origin, UnhandledErrorHandler, ProfileNames.RAML10).resolve()

    result.linkTarget.isEmpty should be(true)
    val node = result.asInstanceOf[NodeShape]
    node.properties.size should be(2)
    node.properties.exists(_.name.value() == "prop1") should be(true)
  }

  test("test shape with inheritance") {
    val ann = Annotations() += SourceLocation("file://location.com")

    val prop1 = PropertyShape(ann)
      .withId("file://location.com#/prop1")
      .withName("prop1")
      .withPath("prop1")
      .withRange(ScalarShape(ann).withId("file://location.com#/prop1range").withDataType(DataTypes.String))
    val prop2 = PropertyShape(ann)
      .withId("file://location.com#/prop2")
      .withName("prop2")
      .withPath("prop2")
      .withRange(ScalarShape(ann).withId("file://location.com#/prop2range").withDataType(DataTypes.Integer))

    val prop3 = PropertyShape(ann)
      .withId("file://location.com#/prop3")
      .withName("prop3")
      .withPath("prop3")
      .withRange(ScalarShape(ann).withId("file://location.com#/prop3range").withDataType(DataTypes.Integer))

    val father = NodeShape(ann).withId("file://location.com/#nodeshape1").withProperties(Seq(prop1, prop2))
    val origin =
      NodeShape(ann).withId("file://location.com/#nodeshape2").withProperties(Seq(prop3)).withInherits(Seq(father))

    val result: Shape =
      new CompleteShapeTransformationPipeline(origin, UnhandledErrorHandler, ProfileNames.RAML10).resolve()

    result.inherits.isEmpty should be(true)
    val node = result.asInstanceOf[NodeShape]
    node.properties.size should be(3)
    node.properties.exists(_.name.value() == "prop1") should be(true)
  }

  test("test shape with linkable not at top") {
    val ann = Annotations() += SourceLocation("file://location.com")
    val prop1 = PropertyShape(ann)
      .withId("file://location.com#/prop1")
      .withName("prop1")
      .withPath("prop1")
      .withRange(ScalarShape(ann).withId("file://location.com#/prop1range").withDataType(DataTypes.String))
    val prop2 = PropertyShape(ann)
      .withId("file://location.com#/prop2")
      .withName("prop2")
      .withPath("prop2")
      .withRange(ScalarShape(ann).withId("file://location.com#/prop2range").withDataType(DataTypes.Integer))

    val target = NodeShape(ann).withId("file://location.com/#nodeshape1").withProperties(Seq(prop1, prop2))
    val origin =
      NodeShape(ann).withId("file://location.com/#nodeshape2").withLinkLabel("linkTo").withLinkTarget(target)

    val prop3 = PropertyShape(ann)
      .withId("file://location.com#/prop3")
      .withName("prop3")
      .withPath("prop3")
      .withRange(origin)

    val lastObject = NodeShape(ann).withId("file://location.com/#nodeshape3").withProperties(Seq(prop3))
    val result: Shape =
      new CompleteShapeTransformationPipeline(lastObject, UnhandledErrorHandler, ProfileNames.RAML10).resolve()

    result.linkTarget.isEmpty should be(true)
    val node = result.asInstanceOf[NodeShape]
    node.properties.size should be(1)
    node.properties.head.range.isLink should be(false)
  }

  test("test shape with inherited linkable") {
    val ann = Annotations() += SourceLocation("file://location.com")
    val prop1 = PropertyShape(ann)
      .withId("file://location.com#/prop1")
      .withName("prop1")
      .withPath("prop1")
      .withRange(ScalarShape(ann).withId("file://location.com#/prop1range").withDataType(DataTypes.String))
    val prop2 = PropertyShape(ann)
      .withId("file://location.com#/prop2")
      .withName("prop2")
      .withPath("prop2")
      .withRange(ScalarShape(ann).withId("file://location.com#/prop2range").withDataType(DataTypes.Integer))

    val target = NodeShape(ann).withId("file://location.com/#nodeshape1").withProperties(Seq(prop1, prop2))
    val origin =
      NodeShape(ann).withId("file://location.com/#nodeshape2").withLinkLabel("linkTo").withLinkTarget(target)

    val prop3 = PropertyShape(ann)
      .withId("file://location.com#/prop3")
      .withName("prop3")
      .withPath("prop3")
      .withRange(origin)
    val father =
      NodeShape(ann).withId("file://location.com/#fathers").withProperties(Seq(prop3))
    val lastObject = NodeShape(ann).withId("file://location.com/#nodeshape3").withInherits(Seq(father))
    val result: Shape =
      new CompleteShapeTransformationPipeline(lastObject, UnhandledErrorHandler, ProfileNames.RAML10).resolve()

    val ns         = result.asInstanceOf[NodeShape]
    val prop3After = ns.properties.find(_.name.value() == "prop3").get
    prop3After.range.isLink should be(false)
    prop3After.range.asInstanceOf[NodeShape].properties.size should be(2)
  }
}
