package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas

import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import amf.plugins.domain.webapi.models.Parameter

class CleanParameterExamples() extends TransformationStep {

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    try {
      model.iterator().foreach {
        case param: Parameter =>
          param.binding
            .option()
            .filterNot(_.equalsIgnoreCase("body"))
            .flatMap(_ => Option(param.schema))
            .map(_.fields.removeField(AnyShapeModel.Examples))

        case _ => // ignore
      }
      model
    } catch {
      case _: Throwable => model // ignore: we don't want this to break anything
    }
  }
}
