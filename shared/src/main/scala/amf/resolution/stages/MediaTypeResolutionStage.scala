package amf.resolution.stages

import amf.ProfileNames
import amf.document.{BaseUnit, Document}
import amf.domain.{DomainElement, Payload, WebApi}
import amf.metadata.Field
import amf.metadata.domain.{OperationModel, PayloadModel, RequestModel, WebApiModel}
import amf.model.AmfScalar
import amf.validation.Validation

/** Apply root and operation mime types to payloads.
  *
  * Request payloads will have as default mime type the 'accepts' field.
  * Response payloads will have as default mime type the 'contentType' field.
  */
class MediaTypeResolutionStage(profile: String)(override implicit val currentValidation: Validation) extends ResolutionStage(profile) {

  override def resolve(model: BaseUnit): BaseUnit = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[WebApi] => resolveMediaTypes(doc.encodes.asInstanceOf[WebApi])
      case _                                                 =>
    }
    model
  }

  def resolveMediaTypes(api: WebApi): Unit = {
    val rootAccepts     = field(api, WebApiModel.Accepts)
    val rootContentType = field(api, WebApiModel.ContentType)

    api.endPoints.foreach { endPoint =>
      endPoint.operations.foreach { operation =>
        // I need to know if this is an empty array or if it's not defined.
        val opAccepts     = field(operation, OperationModel.Accepts)
        val opContentType = field(operation, OperationModel.ContentType)

        val accepts     = merge(rootAccepts, opAccepts)
        val contentType = merge(rootContentType, opContentType)

        Option(operation.request).foreach { request =>
          // Use accepts field.
          accepts.foreach { a =>
            if (profile == ProfileNames.OAS) operation.set(OperationModel.Accepts, a)
            request.setArray(RequestModel.Payloads, payloads(request.payloads, a, request.id))
          }
        }

        operation.responses.foreach { response =>
          // Use contentType field.
          contentType.foreach { ct =>
            if (profile == ProfileNames.OAS) operation.set(OperationModel.ContentType, ct)
            response.setArray(RequestModel.Payloads, payloads(response.payloads, ct, response.id))
          }
        }
      }
    }
  }

  /** Get and remove field from domain element */
  private def field(element: DomainElement, field: Field) = {
    val result = element.fields.entry(field).map(_.array.values.map(v => v.asInstanceOf[AmfScalar].toString))
    element.fields.remove(field)
    result
  }

  private def payloads(p: Seq[Payload], mediaTypes: Seq[String], parent: String) = {
    var (noMediaType, result) = p.partition(_.fields.entry(PayloadModel.MediaType).isEmpty)

    noMediaType.foreach { payload =>
      mediaTypes.foreach { mediaType =>
        // Schema must not be empty, or it would be an empty payload ¯\_(ツ)_/¯
        result = result :+ Payload(payload.annotations)
          .withMediaType(mediaType)
          .adopted(parent)
          .withSchema(payload.schema.cloneShape())
      }
    }

    result
  }

  def merge(root: Option[Seq[String]], op: Option[Seq[String]]): Option[Seq[String]] =
    op.orElse(root).filter(_.nonEmpty)
}
