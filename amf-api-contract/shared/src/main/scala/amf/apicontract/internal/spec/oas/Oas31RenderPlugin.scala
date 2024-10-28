package amf.apicontract.internal.spec.oas

import amf.apicontract.internal.spec.oas.emitter.context.Oas3SpecEmitterContext
import amf.apicontract.internal.spec.oas.emitter.document.{Oas31DocumentEmitter, Oas3DocumentEmitter}
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document, ExternalFragment}
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.Spec
import org.yaml.model.{YDocument, YNode}

object Oas31RenderPlugin extends OasRenderPlugin {

  override def spec: Spec = Spec.OAS31

  override def mediaTypes: Seq[String] = Seq(`application/json`, `application/yaml`)

  override def defaultSyntax(): String = `application/json`

  override def priority: PluginPriority = NormalPriority

  override protected def unparseAsYDocument(
      unit: BaseUnit,
      renderConfig: RenderConfiguration,
      errorHandler: AMFErrorHandler
  ): Option[YDocument] =
    unit match {
      case document: Document =>
        Some(Oas31DocumentEmitter(document)(specContext(renderConfig, errorHandler)).emitDocument())
      case external: ExternalFragment => Some(YDocument(YNode(external.encodes.raw.value())))
      case _                          => None
    }

  private def specContext(renderConfig: RenderConfiguration, errorHandler: AMFErrorHandler): Oas3SpecEmitterContext =
    new Oas3SpecEmitterContext(errorHandler, config = renderConfig)
}
