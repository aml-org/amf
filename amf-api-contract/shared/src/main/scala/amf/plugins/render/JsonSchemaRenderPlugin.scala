package amf.plugins.render

import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel}
import amf.core.internal.plugins.render.{AMFRenderPlugin, RenderInfo}
import amf.core.internal.remote.{JsonSchema, Vendor}
import amf.plugins.document.apicontract.annotations.JSONSchemaRoot
import amf.plugins.document.apicontract.parser.spec.common.JsonSchemaEmitter
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument

object JsonSchemaRenderPlugin extends ApiRenderPlugin {

  override def vendor: Vendor = Vendor.JSONSCHEMA

  override def applies(element: RenderInfo): Boolean = firstAnyShape(element.unit).isDefined

  private def firstAnyShape(unit: BaseUnit): Option[AnyShape] = unit match {
    case d: DeclaresModel => d.declares.collectFirst({ case a: AnyShape => a })
    case _                => None
  }

  override def defaultSyntax(): String = AMFRenderPlugin.APPLICATION_JSON

  override def mediaTypes: Seq[String] = Seq(JsonSchema.mediaType)

  override def unparseAsYDocument(unit: BaseUnit,
                                  renderOptions: RenderOptions,
                                  errorHandler: AMFErrorHandler): Option[YDocument] = unit match {
    case d: DeclaresModel =>
      // The root element of the JSON Schema must be identified with the annotation [[JSONSchemaRoot]]
      val root = d.declares.find(d => d.annotations.contains(classOf[JSONSchemaRoot]) && d.isInstanceOf[AnyShape])
      root match {
        case Some(r: AnyShape) =>
          Some(
            JsonSchemaEmitter(r, d.declares, options = renderOptions.shapeRenderOptions, errorHandler = errorHandler)
              .emitDocument())
        case _ => None
      }
    case _ => None
  }

  override def priority: PluginPriority = NormalPriority
}
