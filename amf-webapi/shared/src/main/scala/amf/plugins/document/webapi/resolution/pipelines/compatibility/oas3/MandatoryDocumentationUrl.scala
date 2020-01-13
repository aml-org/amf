package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas3

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.models.WebApi

class MandatoryDocumentationUrl()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  var tagCounter = 0

  override def resolve[T <: BaseUnit](model: T): T = {
    model match {
      case d: Document if d.encodes.isInstanceOf[WebApi] =>
        try {
          ensureDocumentationUrl(d.encodes.asInstanceOf[WebApi])
        } catch {
          case _: Throwable => // ignore: we don't want this to break anything
        }
        model
      case _ => model
    }
  }

  private def ensureDocumentationUrl(api: WebApi): Unit = {
    api.documentations.foreach { documentation =>
      if (documentation.url.isNullOrEmpty) {
        documentation.withUrl("http://")
      }
    }
  }

}
