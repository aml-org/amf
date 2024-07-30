package amf.apicontract.internal.spec.avro

import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.plugins.render.{RenderConfiguration, RenderInfo, SYAMLBasedRenderPlugin}
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.Spec
import amf.apicontract.internal.spec.avro.emitters.context.AvroShapeEmitterContext
import amf.apicontract.internal.spec.avro.emitters.document.AvroSchemaDocumentEmitter
import amf.shapes.client.scala.model.document.AvroSchemaDocument
import org.yaml.model.YDocument

object AvroRenderPlugin extends SYAMLBasedRenderPlugin {

  override val id: String = Spec.AVRO_SCHEMA.id

  override def priority: PluginPriority = NormalPriority

  override def defaultSyntax(): String = `application/json`

  override def mediaTypes: Seq[String] = Seq(`application/json`)

  override def applies(element: RenderInfo): Boolean = element.unit match {
    case _: AvroSchemaDocument => true
    case _                     => false
  }

  override protected def unparseAsYDocument(
      unit: BaseUnit,
      renderConfig: RenderConfiguration,
      errorHandler: AMFErrorHandler
  ): Option[YDocument] = {
    unit match {
      case document: AvroSchemaDocument => Some(emitDocument(renderConfig, errorHandler, document))
      case _                            => None
    }
  }

  private def emitDocument(
      renderConfig: RenderConfiguration,
      errorHandler: AMFErrorHandler,
      document: AvroSchemaDocument
  ): YDocument = {
    new AvroSchemaDocumentEmitter(document)(
      specContext(renderConfig, errorHandler)
    ).emit()
  }

  private def specContext(
      renderConfig: RenderConfiguration,
      errorHandler: AMFErrorHandler
  ): AvroShapeEmitterContext = {
    new AvroShapeEmitterContext(errorHandler, renderConfig)
  }
}
