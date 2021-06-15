package amf.plugins.document.apicontract.resolution.pipelines.compatibility.oas

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.stages.TransformationStep
import amf.plugins.domain.apicontract.models.Parameter

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
