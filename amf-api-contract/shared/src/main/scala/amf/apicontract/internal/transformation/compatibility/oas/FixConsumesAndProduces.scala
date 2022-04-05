package amf.apicontract.internal.transformation.compatibility.oas

import amf.apicontract.client.scala.model.domain.{Operation, Payload}
import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.metamodel.Field

class FixConsumesAndProduces() extends TransformationStep() {

  override def transform(model: BaseUnit,
                         errorHandler: AMFErrorHandler,
                         configuration: AMFGraphConfiguration): BaseUnit = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[Api] => fixFields(doc.encodes.asInstanceOf[Api])
      case _                                              =>
    }
    model
  }

  protected def fixFields(api: Api): Unit = {
    api.endPoints.foreach { endPoint =>
      endPoint.operations.foreach { operation =>
        fixConsumes(operation)
        fixProduces(operation)
      }
    }
  }

  private def fixConsumes(operation: Operation) = {
    val payloads          = operation.requests.flatMap(_.payloads)
    val requestMediaTypes = collectMediaTypes(payloads)
    setFieldIfValueIsNotEmpty(operation, OperationModel.Accepts, requestMediaTypes.toSeq)
  }

  private def fixProduces(operation: Operation) = {
    val payloads           = operation.responses.flatMap(_.payloads)
    val responseMediaTypes = collectMediaTypes(payloads)
    setFieldIfValueIsNotEmpty(operation, OperationModel.ContentType, responseMediaTypes.toSeq)
  }

  private def collectMediaTypes(payloads: Seq[Payload]): Set[String] = {
    payloads.foldLeft(Set[String]()) { (acc, payload) =>
      if (payload.mediaType.nonNull) acc + payload.mediaType.value() else acc
    }
  }

  private def setFieldIfValueIsNotEmpty(operation: Operation, field: Field, mediaTypes: Seq[String]) = {
    if (mediaTypes.nonEmpty) operation.set(field, mediaTypes)
  }
}
