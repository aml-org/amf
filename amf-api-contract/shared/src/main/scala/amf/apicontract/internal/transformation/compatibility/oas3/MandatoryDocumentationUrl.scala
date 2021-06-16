package amf.apicontract.internal.transformation.compatibility.oas3

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.transform.stages.TransformationStep
import amf.apicontract.client.scala.model.domain.api.Api

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
