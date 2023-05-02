package amf.resolution.element

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.{AMFConfiguration, APIConfiguration}
import amf.core.client.common.validation.ProfileNames
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel, EncodesModel}
import amf.core.client.scala.model.domain.Shape
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.domain.resolution.elements.CompleteShapeTransformationPipeline
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

class ShapeTransformationPipelineWithFilesTest extends AsyncFunSuite with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val base = "file://amf-cli/shared/src/test/resources/shape-transformation-pipeline-test"

  test("Array with inherits") {
    val client = APIConfiguration.API().baseUnitClient()
    client.parseDocument(s"$base/array-with-inherits/api.raml").flatMap { result =>
      val conf = APIConfiguration.fromSpec(result.sourceSpec)
      val assertion = for {
        shape <- findShapeInDeclares[ArrayShape](result.baseUnit) { arr =>
          arr.name.value() == "ar"
        }
      } yield {
        val resolvedShape = resolveShape(shape, conf)
        resolvedShape mustBe a[ArrayShape]

        val array = resolvedShape.asInstanceOf[ArrayShape]

        array.items mustBe a[NodeShape]

        val node = array.items.asInstanceOf[NodeShape]
        node.inherits should have size 0
        node.properties should have size 3
      }
      assertion.getOrElse(fail("Array shape not found"))
    }
  }

  test("Shape with inherits") {
    val client = APIConfiguration.API().baseUnitClient()
    client.parseDocument(s"$base/shape-with-inherits/api.raml").flatMap { result =>
      val conf = APIConfiguration.fromSpec(result.sourceSpec)
      val shape = result.baseUnit
        .asInstanceOf[EncodesModel]
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

        val resolvedShape = resolveShape(shape, conf)
        resolvedShape mustBe a[NodeShape]

        val nodeShape = resolvedShape.asInstanceOf[NodeShape]

        nodeShape.properties.size shouldBe 3

        val maybeProp = nodeShape.properties.find(p => p.name.value() == "application")
        maybeProp.nonEmpty shouldBe true
        val propRange = maybeProp.get.range
        propRange mustBe a[NodeShape]
        val nodeProp = propRange.asInstanceOf[NodeShape]
        nodeProp.inherits should have size 0
        nodeProp.properties should have size 3
    }
  }

  // This explodes with SOF during link resolution that happens before Shape Normalization. This is not a regression, it
  // was already failing in 5.2.6
  ignore("Cyclic shape") {
    val client = APIConfiguration.API().baseUnitClient()
    client.parseDocument(s"$base/cyclic-shape/api.raml").flatMap { result =>
      val conf = APIConfiguration.fromSpec(result.sourceSpec)
      val assertion = for {
        shape <- findShapeInDeclares[NodeShape](result.baseUnit) { node =>
          node.name.value() == "A"
        }
      } yield {
        val resolvedShape = resolveShape(shape, conf)
        resolvedShape mustBe a[NodeShape]
      }
      assertion.getOrElse(fail("Array shape not found"))
    }
  }

  private def resolveShape(shape: Shape, conf: AMFConfiguration) = {
    val pipeline = new CompleteShapeTransformationPipeline(shape, UnhandledErrorHandler, ProfileNames.RAML10)
    pipeline.transform(conf)
  }

  private def findShapeInDeclares[T <: Shape](
      unit: BaseUnit
  )(selector: T => Boolean)(implicit ct: ClassTag[T]): Option[T] = {
    unit match {
      case d: DeclaresModel =>
        d.declares
          .find {
            case s: T => selector(s)
            case _    => false
          }
          .map(_.asInstanceOf[T])
      case _ => None
    }

  }

}
