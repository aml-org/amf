package amf.apicontract.internal.spec.payload

import amf.core.client.common.{LowPriority, PluginPriority}
import amf.core.client.scala.model.document.{BaseUnit, PayloadFragment}
import amf.core.internal.plugins.render.{AMFRenderPlugin, RenderConfiguration, RenderInfo}
import amf.core.internal.remote.{Mimes, SpecId}
import amf.core.internal.remote.Mimes._
import amf.shapes.internal.spec.common.emitter.PayloadEmitter
import org.yaml.builder.{DocBuilder, YDocumentBuilder}

object PayloadRenderPlugin extends AMFRenderPlugin {

  override val id: String = SpecId.PAYLOAD.name

  override def applies(element: RenderInfo): Boolean = element.unit.isInstanceOf[PayloadFragment]

  override def emit[T](unit: BaseUnit, builder: DocBuilder[T], renderConfiguration: RenderConfiguration): Boolean = {
    (builder, unit) match {
      case (sb: YDocumentBuilder, p: PayloadFragment) =>
        sb.document = PayloadEmitter(p.encodes)(renderConfiguration.errorHandler).emitDocument()
        true
      case _ => false
    }
  }

  override def defaultSyntax(): String = `application/json`

  override def mediaTypes: Seq[String] = Seq(`application/json`, `application/yaml`)

  override def priority: PluginPriority = LowPriority
}
