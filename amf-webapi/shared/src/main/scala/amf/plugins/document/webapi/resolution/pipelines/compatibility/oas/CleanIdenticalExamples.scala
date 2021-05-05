package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import amf.plugins.domain.shapes.models.{AnyShape, Example}

class CleanIdenticalExamples() extends ResolutionStage {

  def cleanExamples(shape: AnyShape): Unit = {

    val uniqueExamples = shape.examples
      .filter(e => e.raw.option().isDefined)
      .foldLeft(Map[String, Example]()) { (acc, curr) =>
        acc.updated(curr.raw.value().hashCode().toString, curr)
      }
      .values
    shape.setArrayWithoutId(AnyShapeModel.Examples, uniqueExamples.toSeq)
  }

  override def resolve[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T = {
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
