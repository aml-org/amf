package amf.plugins.render
import amf.client.remod.amfcore.plugins.{LowPriority, PluginPriority}
import amf.client.remod.amfcore.plugins.render.AMFRenderPlugin.APPLICATION_JSON
import amf.client.remod.amfcore.plugins.render.{AMFRenderPlugin, RenderConfiguration, RenderInfo}
import amf.core.model.document.{BaseUnit, PayloadFragment}
import amf.core.remote.{Payload, Vendor}
import amf.plugins.common.PayloadMediaTypes
import amf.plugins.document.webapi.parser.spec.common.PayloadEmitter
import org.yaml.builder.{DocBuilder, YDocumentBuilder}

object PayloadRenderPlugin extends AMFRenderPlugin {

  override val id: String = Vendor.PAYLOAD.name

  override def applies(element: RenderInfo): Boolean = element.unit.isInstanceOf[PayloadFragment]

  override def emit[T](unit: BaseUnit, builder: DocBuilder[T], renderConfiguration: RenderConfiguration): Boolean = {
    (builder, unit) match {
      case (sb: YDocumentBuilder, p: PayloadFragment) =>
        sb.document = PayloadEmitter(p.encodes)(renderConfiguration.errorHandler).emitDocument()
        true
      case _ => false
    }
  }

  override def defaultSyntax(): String = APPLICATION_JSON

  override def mediaTypes: Seq[String] = PayloadMediaTypes.mediaTypes

  override def priority: PluginPriority = LowPriority
}
