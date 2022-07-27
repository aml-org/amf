package amf.shapes.internal.spec.jsonschema

import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.plugins.render.{RenderConfiguration, RenderInfo, SYAMLBasedRenderPlugin}
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.Spec
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.emitter.JsonSchemaShapeEmitterContext
import amf.shapes.internal.spec.jsonschema.emitter.document.JsonSchemaDocumentEmitter
import org.yaml.model.YDocument

object JsonSchemaRenderPlugin extends SYAMLBasedRenderPlugin {

  override val id: String = Spec.JSONSCHEMA.id

  override def priority: PluginPriority = NormalPriority

  override def defaultSyntax(): String = `application/json`

  override def mediaTypes: Seq[String] = Seq(`application/json`)

  override def applies(element: RenderInfo): Boolean = element.unit match {
    case _: JsonSchemaDocument => true
    case _                     => false
  }

  override protected def unparseAsYDocument(
      unit: BaseUnit,
      renderConfig: RenderConfiguration,
      errorHandler: AMFErrorHandler
  ): Option[YDocument] = {
    unit match {
      case document: JsonSchemaDocument => Some(emitDocument(renderConfig, errorHandler, document))
      case _                            => None
    }
  }

  private def emitDocument(
      renderConfig: RenderConfiguration,
      errorHandler: AMFErrorHandler,
      document: JsonSchemaDocument
  ) = {
    new JsonSchemaDocumentEmitter(document)(specContext(renderConfig, document.schemaVersion.value(), errorHandler))
      .emit()
  }

  private def specContext(
      renderConfig: RenderConfiguration,
      schemaVersion: String,
      errorHandler: AMFErrorHandler
  ): JsonSchemaShapeEmitterContext = {
    val schema = SchemaVersion.unapply(schemaVersion)
    new JsonSchemaShapeEmitterContext(errorHandler, schema, renderConfig)
  }
}
