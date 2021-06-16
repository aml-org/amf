package amf.apicontract.internal.spec.common.transformation.stage

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.internal.metamodel.domain.{EndPointModel, OperationModel}
import amf.core.client.common.validation.{Async20Profile, Oas30Profile, ProfileName}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.client.scala.transform.stages.TransformationStep
import amf.core.internal.metamodel.Field

/**
  * Applies summary and description defined in path item to all of its child operations
  *
  * @param profile target profile
  */
class PathDescriptionNormalizationStage(profile: ProfileName, val keepEditingInfo: Boolean = false)
    extends TransformationStep() {

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    profile match {
      // TODO should run for Amf too
      case Oas30Profile | Async20Profile => normalizeDescriptions(model)
      case _                             => model
    }
  }

  /**
    * Applies summary and description defined in EndPoint to all Operations.
    * If editing is true, the Endpoint with maintain its values.
    *
    * @param unit BaseUnit in
    * @return unit BaseUnit out
    */
  protected def normalizeDescriptions(unit: BaseUnit): BaseUnit = {
    unit match {
      case doc: Document if doc.encodes.isInstanceOf[Api] =>
        val webApi = doc.encodes.asInstanceOf[Api]
        webApi.endPoints foreach applyToOperations
        doc
      case _ => unit
    }
  }

  private def applyToOperations(endPoint: EndPoint): Unit = {
    val description: Option[String] = endPoint.fields.?[AmfScalar](EndPointModel.Description).map(_.toString)
    val summary: Option[String]     = endPoint.fields.?[AmfScalar](EndPointModel.Summary).map(_.toString)
    val operations                  = endPoint.operations
    if (operations.nonEmpty && !keepEditingInfo) {
      endPoint.fields.removeField(EndPointModel.Description)
      endPoint.fields.removeField(EndPointModel.Summary)
    }
    operations.foreach { operation =>
      setValueIfNotPresent(operation, OperationModel.Description, description)
      setValueIfNotPresent(operation, OperationModel.Summary, summary)
    }
  }

  private def setValueIfNotPresent(op: Operation, field: Field, value: Option[String]): Unit =
    if (!op.fields.exists(field))
      value.foreach(op.set(field, _))
}
