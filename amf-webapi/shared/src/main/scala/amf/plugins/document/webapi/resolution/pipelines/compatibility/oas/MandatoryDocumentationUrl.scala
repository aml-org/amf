package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.webapi.models.api.Api

class MandatoryDocumentationUrl() extends TransformationStep {

  var tagCounter = 0

  override def transform[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T = {
    model match {
      case d: Document if d.encodes.isInstanceOf[Api] =>
        try {
          ensureDocumentationUrl(d.encodes.asInstanceOf[Api])
        } catch {
          case _: Throwable => // ignore: we don't want this to break anything
        }
        model
      case _ => model
    }
  }

  private def ensureDocumentationUrl(api: Api): Unit = {
    api.documentations.foreach { documentation =>
      if (documentation.url.isNullOrEmpty) {
        documentation.withUrl("http://")
      }
    }
  }

}
