package amf.plugins.document.webapi.resolution.pipelines.compatibility
import amf.core.model.document.{BaseUnit, Document}
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.metamodel.WebApiModel
import amf.plugins.domain.webapi.models.WebApi

import scala.language.postfixOps

class CapitalizeSchemes(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  private def capitalizeProtocols(api: WebApi) = {
    val valid = Seq("http", "https")
    val schemes =
      api.schemes.flatMap(_.option()).filter(scheme => valid.exists(scheme.equalsIgnoreCase)).map(_.toUpperCase)
    api.fields.removeField(WebApiModel.Schemes)
    if (schemes.nonEmpty) api.withSchemes(schemes)
  }

  override def resolve[T <: BaseUnit](model: T): T = model match {
    case d: Document if d.encodes.isInstanceOf[WebApi] =>
      try {
        capitalizeProtocols(d.encodes.asInstanceOf[WebApi])
      } catch {
        case _: Throwable => // ignore: we don't want this to break anything
      }
      model
    case _ => model
  }
}
