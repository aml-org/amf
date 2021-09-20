package amf.apicontract.internal.transformation.compatibility.oas

import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.transform.TransformationStep

class MandatoryDocumentationUrl() extends TransformationStep {

  var tagCounter = 0

  override def transform(model: BaseUnit,
                         errorHandler: AMFErrorHandler,
                         configuration: AMFGraphConfiguration): BaseUnit = {
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
