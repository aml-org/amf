package amf.resolution.element
import amf.ProfileNames
import amf.client.model.DataTypes
import amf.core.annotations.SourceLocation
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.domain.Shape
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.Annotations
import amf.plugins.domain.shapes.models.{NodeShape, ScalarShape}
import amf.plugins.domain.shapes.resolution.stages.elements.ShapeTransformationPipeline
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

    val result: Shape = new ShapeTransformationPipeline(origin, UnhandledErrorHandler, ProfileNames.RAML10).resolve()

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

    val result: Shape = new ShapeTransformationPipeline(origin, UnhandledErrorHandler, ProfileNames.RAML10).resolve()

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
      new ShapeTransformationPipeline(lastObject, UnhandledErrorHandler, ProfileNames.RAML10).resolve()

    result.linkTarget.isEmpty should be(true)
    val node = result.asInstanceOf[NodeShape]
    node.properties.size should be(1)
    node.properties.head.range.isLink should be(false)
  }

}
