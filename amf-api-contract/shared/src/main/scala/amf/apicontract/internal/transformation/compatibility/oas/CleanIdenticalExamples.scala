package amf.apicontract.internal.transformation.compatibility.oas

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.TransformationStep
import amf.shapes.client.scala.model.domain.Example
import amf.shapes.client.scala.model.domain.{AnyShape, Example}
import amf.shapes.internal.domain.metamodel.AnyShapeModel

class CleanIdenticalExamples() extends TransformationStep {

  def cleanExamples(shape: AnyShape): Unit = {

    val uniqueExamples = shape.examples
      .filter(e => e.raw.option().isDefined)
      .foldLeft(Map[String, Example]()) { (acc, curr) =>
        acc.updated(curr.raw.value().hashCode().toString, curr)
      }
      .values
    shape.setArrayWithoutId(AnyShapeModel.Examples, uniqueExamples.toSeq)
  }

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    try {
      model.iterator().foreach {
        case shape: AnyShape => cleanExamples(shape)
        case _               => // ignore
      }
      model
    } catch {
      case _: Throwable => model // ignore: we don't want this to break anything
    }
  }
}
