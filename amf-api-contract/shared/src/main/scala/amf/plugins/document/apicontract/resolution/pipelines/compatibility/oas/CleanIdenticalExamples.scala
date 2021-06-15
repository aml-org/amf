package amf.plugins.document.apicontract.resolution.pipelines.compatibility.oas

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.stages.TransformationStep
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import amf.plugins.domain.shapes.models.{AnyShape, Example}

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

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
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
