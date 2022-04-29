package amf.apicontract.internal.plugins

import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel}
import amf.core.internal.plugins.render.{RenderConfiguration, RenderInfo}
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.Spec
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.JSONSchemaRoot
import amf.shapes.internal.spec.jsonschema.emitter.JsonSchemaEmitter
import org.yaml.model.YDocument

object JsonSchemaRenderPlugin extends ApiRenderPlugin {

  override def spec: Spec = Spec.JSONSCHEMA

  override def applies(element: RenderInfo): Boolean = firstAnyShape(element.unit).isDefined

  private def firstAnyShape(unit: BaseUnit): Option[AnyShape] = unit match {
    case d: DeclaresModel => d.declares.collectFirst({ case a: AnyShape => a })
    case _                => None
  }

  override def defaultSyntax(): String = `application/json`

  override def mediaTypes: Seq[String] = Seq(`application/json`)

  override def unparseAsYDocument(
      unit: BaseUnit,
      renderConfig: RenderConfiguration,
      errorHandler: AMFErrorHandler
  ): Option[YDocument] = unit match {
    case d: DeclaresModel =>
      // The root element of the JSON Schema must be identified with the annotation [[JSONSchemaRoot]]
      val root = d.declares.find(d => d.annotations.contains(classOf[JSONSchemaRoot]) && d.isInstanceOf[AnyShape])
      root match {
        case Some(r: AnyShape) =>
          Some(
            JsonSchemaEmitter(r, d.declares, renderConfig = renderConfig, errorHandler = errorHandler)
              .emitDocument()
          )
        case _ => None
      }
    case _ => None
  }

  override def priority: PluginPriority = NormalPriority
}
