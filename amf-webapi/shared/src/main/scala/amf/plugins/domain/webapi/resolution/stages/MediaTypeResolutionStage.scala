package amf.plugins.domain.webapi.resolution.stages

import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Field
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{AmfScalar, DomainElement, Shape}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.models.{ExampleTracking, FileShape, NodeShape}
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.metamodel.api.BaseApiModel
import amf.plugins.domain.webapi.models.api.Api
import amf.plugins.domain.webapi.models.{Payload, Request}
import amf.validations.ResolutionSideValidations.InvalidConsumesWithFileParameter
import amf.{Oas20Profile, ProfileName}

/** Apply root and operation mime types to payloads.
  *
  * Request payloads will have as default mime type the 'accepts' field.
  * Response payloads will have as default mime type the 'contentType' field.
  */
class MediaTypeResolutionStage(profile: ProfileName,
                               isValidation: Boolean = false,
                               val keepEditingInfo: Boolean = false)(override implicit val errorHandler: ErrorHandler)
    extends ResolutionStage() {
  override def resolve[T <: BaseUnit](model: T): T = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[Api] =>
        propagatePayloads(doc.encodes.asInstanceOf[Api])
        resolveMediaTypes(doc.encodes.asInstanceOf[Api])
      case _ =>
    }
    model.asInstanceOf[T]
  }

  def propagatePayloads(api: Api): Unit = {
    api.endPoints.foreach { endpoint =>
      val payloads = endpoint.payloads
      endpoint.fields.removeField(EndPointModel.Payloads)
      if (payloads.nonEmpty) {
        endpoint.operations.foreach { operation =>
          Option(operation.request) match {
            case Some(request) =>
              payloads.foreach { payload =>
                request.add(RequestModel.Payloads, payload)
              }
            case None => operation.withRequest().withPayloads(payloads)
          }
        }
      }
    }
  }

  def resolveMediaTypes(api: Api): Unit = {
    val rootAccepts     = getAndRemove(api, BaseApiModel.Accepts, keepMediaTypesInModel)
    val rootContentType = getAndRemove(api, BaseApiModel.ContentType, keepMediaTypesInModel)

    api.endPoints.foreach { endPoint =>
      endPoint.operations.foreach { operation =>
        // I need to know if this is an empty array or if it's not defined.
        val opAccepts     = getAndRemove(operation, OperationModel.Accepts, keepMediaTypesInModel)
        val opContentType = getAndRemove(operation, OperationModel.ContentType, keepMediaTypesInModel)

        val accepts     = overrideWith(rootAccepts, opAccepts)
        val contentType = overrideWith(rootContentType, opContentType)

        Option(operation.request).foreach { request =>
          // Use accepts field.
          accepts match {
            case Some(a) =>
              if (!isValidation && profile.isOas()) operation.set(OperationModel.Accepts, a)
              request.setArray(RequestModel.Payloads, payloads(request.payloads, a, request.id))
            case None =>
          }
          if (profile == Oas20Profile) validateFilePayloads(request)
        }

        operation.responses.foreach { response =>
          // Use contentType field.
          contentType match {
            case Some(ct) =>
              if (!isValidation && profile == Oas20Profile) operation.set(OperationModel.ContentType, ct)
              response.setArray(RequestModel.Payloads, payloads(response.payloads, ct, response.id))
            case None =>
          }
        }
      }
    }
  }

  private def keepMediaTypesInModel = () => !isValidation && !keepEditingInfo

  private def getAndRemove(element: DomainElement, field: Field, removeCondition: () => Boolean) = {
    val result = element.fields.entry(field).map(_.array.values.map(v => v.asInstanceOf[AmfScalar].toString))
    if (removeCondition()) element.fields.removeField(field)
    result
  }

  private def payloads(p: Seq[Payload], mediaTypes: Seq[String], parent: String) = {
    var (noMediaType, result) = p.partition(_.fields.entry(PayloadModel.MediaType).isEmpty)

    noMediaType.foreach { payload =>
      mediaTypes.foreach { mediaType =>
        // Schema must not be empty, or it would be an empty payload ¯\_(ツ)_/¯
        result = result :+ {
          val parsedPayload = Payload(payload.annotations)
            .withMediaType(mediaType)
            .adopted(parent)
          // TODO: Evaluate using a custom copy function in Payload
          payload.name.option().foreach(name => parsedPayload.withName(name))
          if (Option(payload.schema).isDefined)
            parsedPayload.fields
              .setWithoutId(PayloadModel.Schema, replaceTrackedAnnotation(payload, parsedPayload.id))
          parsedPayload
        }
      }

      /** Remove old tracking from new payloads with the same examples.
        * Done here because of multiple media type propagation. */
      result.foreach(newPayload => removeTracked(newPayload, payload.id))
    }

    result
  }

  /** Remove tracking from examples in the given payload */
  private def removeTracked(p: Payload, idToRemove: String): Unit =
    ExampleTracking.removeTracking(p.schema, idToRemove)

  /** Add tracked annotation only to examples that tracked the old payload with no media type. */
  private def replaceTrackedAnnotation(payload: Payload, newPayloadId: String): Shape =
    ExampleTracking.replaceTracking(payload.schema, newPayloadId, payload.id)

  def overrideWith(root: Option[Seq[String]], overrider: Option[Seq[String]]): Option[Seq[String]] =
    overrider.orElse(root).filter(_.nonEmpty)

  /** Oas 2.0 violation in which all file parameters must comply with specific consumes property */
  private def validateFilePayloads(request: Request): Unit = {
    val filePayloads = request.payloads.filter(_.schema match {
      // another violation is present to make sure all file parameters are NodeShapes
      case node: NodeShape => node.properties.exists(_.range.isInstanceOf[FileShape])
      case _               => false
    })
    filePayloads
      .filter(_.mediaType.option() match {
        case Some("multipart/form-data") | Some("application/x-www-form-urlencoded") => false
        case _                                                                       => true
      })
      .foreach { payload =>
        val errorProperties = payload.schema match {
          case n: NodeShape => n.properties.filter(_.range.isInstanceOf[FileShape])
          case _            => Nil
        }
        errorProperties.foreach(mediaTypeError)
      }
  }

  private def mediaTypeError(prop: PropertyShape): Unit = errorHandler.violation(
    InvalidConsumesWithFileParameter,
    prop.id,
    "Consumes must be either 'multipart/form-data', 'application/x-www-form-urlencoded', or both when a file parameter is present",
    prop.range.annotations
  )

}
