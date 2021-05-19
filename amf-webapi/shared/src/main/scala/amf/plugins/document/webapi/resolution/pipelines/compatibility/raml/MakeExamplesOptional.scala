package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.shapes.models.Example

class MakeExamplesOptional() extends TransformationStep {

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    try {
      model.iterator().foreach {
        case example: Example =>
          makeOptional(example)
        case _ => // ignore
      }
    } catch {
      case e: Throwable => // ignore: we don't want this to break anything
    }
    model
  }

  def makeOptional(example: Example): Unit = {
    example.withStrict(false)
  }

}
