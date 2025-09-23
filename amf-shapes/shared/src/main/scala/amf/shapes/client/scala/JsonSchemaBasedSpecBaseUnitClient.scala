package amf.shapes.client.scala

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.AMFParser
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.remote.Mimes
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.internal.plugins.render.AMFJsonLDSchemaGraphRenderPlugin
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationHelper
import org.yaml.builder.{DocBuilder, JsonOutputBuilder}

import java.io.StringWriter
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

  // Sync Validate method returns no LexicalInformation in case of a validation error. If it is needed use `validate`
  def syncValidate(baseUnit: BaseUnit): AMFValidationReport = {
    JsonSchemaBasedSpecValidationHelper.validateInstanceSync(
        baseUnit.asInstanceOf[JsonLDInstanceDocument],
        schemaShape,
        profile
    )
  }

  /** These render overrides are to hack the hack that already exists in AMFSerializer which uses AMFGraphRenderPlugin
   * without take into account the existing plugins of the config
   */
  override def render(baseUnit: BaseUnit, mediaType: String): String = {
    if (mediaType == Mimes.`application/ld+json`) {
      val w = new StringWriter
      val b = JsonOutputBuilder(w, configuration.options.renderOptions.isPrettyPrint)
      AMFJsonLDSchemaGraphRenderPlugin.emitToYDocBuilder(baseUnit, b, configuration.renderConfiguration)
      w.toString
    }
    else {
      super.render(baseUnit, mediaType)
    }
  }

  override def renderGraphToBuilder[T](baseUnit: BaseUnit, builder: DocBuilder[T]): T = {
    AMFJsonLDSchemaGraphRenderPlugin.emitToYDocBuilder(baseUnit, builder, configuration.renderConfiguration)
    builder.result
  }
}
