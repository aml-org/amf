package amf.plugins.domain.webapi.resolution.stages

import amf._
import amf.core.metamodel.Field
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.AmfScalar
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.metamodel.{EndPointModel, OperationModel}
import amf.plugins.domain.webapi.models.{EndPoint, Operation, WebApi}

/**
  * Applies summary and description defined in path item to all of its child operations
  *
  * @param profile target profile
  */
class PathDescriptionNormalizationStage(profile: ProfileName, val keepEditingInfo: Boolean = false)(
    override implicit val errorHandler: ErrorHandler)
    extends ResolutionStage() {

  override def resolve[T <: BaseUnit](model: T): T = {
    profile match {
      // TODO should run for Amf too
      case Oas30Profile => normalizeDescriptions(model).asInstanceOf[T]
      case _            => model
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
      case doc: Document if doc.encodes.isInstanceOf[WebApi] =>
        val webApi = doc.encodes.asInstanceOf[WebApi]
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
