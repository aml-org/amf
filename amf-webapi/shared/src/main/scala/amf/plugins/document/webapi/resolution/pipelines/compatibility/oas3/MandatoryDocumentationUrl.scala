package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas3

import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.webapi.models.api.Api

class MandatoryDocumentationUrl() extends TransformationStep {

  var tagCounter = 0

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
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
