package amf.apicontract.internal.transformation.compatibility.oas

import amf.apicontract.client.scala.model.domain.Payload
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.annotations.FormBodyParameter
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.parser.domain.Annotations

/** To represent a method with file upload:
  *
  * in RAML: \- You define a body with multipart/form-data and a property of type: file \- You can specify acceptable
  * file types directly with fileTypes: ['application/xml']
  *
  * In OAS 2.0 you need a parameter of type: file and in: formData with a consumes: - multipart/form-data, to correctly
  * emit this the FormBodyParameter annotation is needed
  */
case class FixFilePayloads() extends TransformationStep() {
  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[WebApi] => fixFields(doc.encodes.asInstanceOf[WebApi])
      case _                                                 => // ignore
    }
    model
  }

  private def fixFields(api: WebApi): Unit = {
    api.endPoints.foreach { endPoint =>
      endPoint.operations.foreach { operation =>
        val requestPayloads  = operation.requests.flatMap(_.payloads)
        val responsePayloads = operation.responses.flatMap(_.payloads)
        val payloads         = requestPayloads ++ responsePayloads
        payloads.foreach(checkFormData)
      }
    }
  }

  private def checkFormData(payload: Payload): Unit = {
    val fileTypes = Seq("multipart/form-data", "application/x-www-form-urlencoded")
    if (fileTypes.contains(payload.mediaType.value())) {
      payload.set(NameFieldSchema.Name, AmfScalar("formData"), Annotations.inferred())
      payload.add(FormBodyParameter())
    }
  }
}
