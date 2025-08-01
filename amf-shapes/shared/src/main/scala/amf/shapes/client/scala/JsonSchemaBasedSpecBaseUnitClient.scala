package amf.shapes.client.scala

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.AMFParser
import amf.core.client.scala.validation.AMFValidationReport
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationHelper

import scala.concurrent.ExecutionContext

/** The AMF Client contains common AMF operations associated to base unit and documents. For more complex uses see
  * [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
abstract class JsonSchemaBasedSpecBaseUnitClient private[amf] (
    override protected val configuration: JsonSchemaBasedSpecConfiguration
) extends ShapesBaseUnitClient(configuration) {

  protected def schemaShape: Shape

  protected def profile: ProfileName

  override implicit val exec: ExecutionContext = configuration.getExecutionContext

  override def getConfiguration: JsonSchemaBasedSpecConfiguration = configuration

  def syncValidate(baseUnit: BaseUnit): AMFValidationReport = {
    JsonSchemaBasedSpecValidationHelper.validateInstance(
      baseUnit.asInstanceOf[JsonLDInstanceDocument],
      schemaShape,
      profile
    )
  }
}
