package amf.apicontract.internal.transformation.compatibility.oas

import amf.apicontract.client.scala.model.domain.{Operation, Payload}
import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.metamodel.Field
import amf.shapes.client.scala.model.domain.FileShape

/** Contrary to OAS, In RAML, mediaTypes defined in requests/responses bodies are not added to the field
  * accepts/contentType of the model So in the MediaTypeResolutionStage, when converting from RAML to OAS 2.0, the
  * accepts and contentType only include the global mediaType (if defined). This led to some inconsistent
  * transformations when mediaTypes were defined in bodies as accepts/contentType did not correlate to the payload's
  * mediaTypes. The purpose of this step is to address this issue. To do this, mediaTypes are collected from payloads
  * and accepts and contentType fields are set with that values. This way payload's mediaTypes and modified fields will
  * correlate, solving the inconsistency. The only particular case is when a file parameter is defined inside the
  * request body. The accepts field is filled with the 2 possible mediaTypes according to the spec.
  */

class FixConsumesAndProduces() extends TransformationStep() {

  val validConsumesForFileParam = List("multipart/form-data", "application/x-www-form-urlencoded")

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
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
    val payloads = operation.requests.flatMap(_.payloads)
    val requestMediaTypes =
      if (fileShapePresent(payloads)) validConsumesForFileParam else collectMediaTypes(payloads)
    setFieldIfValueIsNotEmpty(operation, OperationModel.Accepts, requestMediaTypes)
  }

  private def fixProduces(operation: Operation) = {
    val payloads           = operation.responses.flatMap(_.payloads)
    val responseMediaTypes = collectMediaTypes(payloads)
    setFieldIfValueIsNotEmpty(operation, OperationModel.ContentType, responseMediaTypes)
  }

  private def collectMediaTypes(payloads: Seq[Payload]): Seq[String] = {
    val mediaTypes = payloads.foldLeft(Set[String]()) { (acc, payload) =>
      if (payload.mediaType.nonNull) acc + payload.mediaType.value() else acc
    }
    mediaTypes.toSeq
  }

  private def setFieldIfValueIsNotEmpty(operation: Operation, field: Field, mediaTypes: Seq[String]) = {
    if (mediaTypes.nonEmpty) operation.set(field, mediaTypes)
  }

  def fileShapePresent(payloads: Seq[Payload]): Boolean = {
    payloads.exists { p =>
      Option(p.schema).exists(_.isInstanceOf[FileShape])
    }
  }
}
