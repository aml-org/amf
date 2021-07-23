package amf.apicontract.internal.spec.common.transformation.stage

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.model.domain.{Payload, Request}
import amf.apicontract.internal.metamodel.domain.api.BaseApiModel
import amf.apicontract.internal.metamodel.domain.{EndPointModel, OperationModel, PayloadModel, RequestModel}
import amf.apicontract.internal.validation.definitions.ResolutionSideValidations.InvalidConsumesWithFileParameter
import amf.core.client.common.validation.{Oas20Profile, ProfileName}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{AmfScalar, DomainElement, Shape}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.metamodel.Field
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.client.scala.model.domain.{FileShape, NodeShape}
import amf.shapes.internal.domain.resolution.ExampleTracking

/** Apply root and operation mime types to payloads.
  *
  * Request payloads will have as default mime type the 'accepts' field.
  * Response payloads will have as default mime type the 'contentType' field.
  */
class MediaTypeResolutionStage(profile: ProfileName,
                               isValidation: Boolean = false,
                               val keepEditingInfo: Boolean = false)
    extends TransformationStep() {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[Api] =>
        propagatePayloads(doc.encodes.asInstanceOf[Api])
        resolveMediaTypes(doc.encodes.asInstanceOf[Api])(errorHandler)
      case _ =>
    }
    model
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

  def resolveMediaTypes(api: Api)(implicit errorHandler: AMFErrorHandler): Unit = {
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
              .setWithoutId(PayloadModel.Schema, replaceTrackedAnnotation(payload, parsedPayload))
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
  private def replaceTrackedAnnotation(payload: Payload, newPayloadId: AmfObject): Shape =
    ExampleTracking.replaceTracking(payload.schema, newPayloadId, payload.id)

  def overrideWith(root: Option[Seq[String]], overrider: Option[Seq[String]]): Option[Seq[String]] =
    overrider.orElse(root).filter(_.nonEmpty)

  /** Oas 2.0 violation in which all file parameters must comply with specific consumes property */
  private def validateFilePayloads(request: Request)(implicit errorHandler: AMFErrorHandler): Unit = {
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

  private def mediaTypeError(prop: PropertyShape)(implicit errorHandler: AMFErrorHandler): Unit =
    errorHandler.violation(
      InvalidConsumesWithFileParameter,
      prop.id,
      "Consumes must be either 'multipart/form-data', 'application/x-www-form-urlencoded', or both when a file parameter is present",
      prop.range.annotations
    )

}
