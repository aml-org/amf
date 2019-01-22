package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas

import amf.core.model.document.BaseUnit
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.Parameter

class CleanParameterExamples()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  override def resolve[T <: BaseUnit](model: T): T = {
    try {
      model.findByType(ParameterModel.`type`.head.iri()).foreach {
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
