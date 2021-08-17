package amf.apicontract.internal.spec.payload

import amf.core.client.common.{LowPriority, PluginPriority}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document
import amf.core.client.scala.model.document.{BaseUnit, PayloadFragment}
import amf.core.internal.plugins.render.{AMFRenderPlugin, RenderConfiguration, RenderInfo, SYAMLBasedRenderPlugin}
import amf.core.internal.remote.{Mimes, Spec}
import amf.core.internal.remote.Mimes._
import amf.shapes.internal.spec.common.emitter.PayloadEmitter
import org.yaml.builder.{DocBuilder, YDocumentBuilder}
import org.yaml.model.YDocument

object PayloadRenderPlugin extends SYAMLBasedRenderPlugin {

  override val id: String = Spec.PAYLOAD.id

  override def applies(element: RenderInfo): Boolean = element.unit.isInstanceOf[PayloadFragment]

  override def defaultSyntax(): String = `application/json`

  override def mediaTypes: Seq[String] = Seq(`application/json`, `application/yaml`)

  override def priority: PluginPriority = LowPriority

  override protected def unparseAsYDocument(unit: BaseUnit,
                                            renderOptions: RenderOptions,
                                            errorHandler: AMFErrorHandler): Option[YDocument] = {
    unit match {
      case p: PayloadFragment => Some(PayloadEmitter(p.encodes)(errorHandler).emitDocument())
      case _                  => None
    }
  }
}
