package amf.apiinstance.internal.spec.document

import amf.apiinstance.client.scala.model.domain.Proxy
import amf.apiinstance.internal.spec.context.AWSAPIGWConfigContext
import amf.apiinstance.internal.spec.domain.envoy.EnvoyConfigProtocolListenerParser
import amf.apiinstance.internal.utils.NodeTraverser
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, BaseUnitProcessingData, Document}
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.Root
import amf.core.internal.remote.EnvoyConfig
import org.yaml.model.YMap

case class EnvoyConfigDocumentParser(root: Root)(implicit ctx: AWSAPIGWConfigContext) extends NodeTraverser {

  val doc: Document = Document()
  private def proxy = doc.encodes.asInstanceOf[Proxy]

  def parseDocument(): BaseUnit = {
    doc
      .withLocation(root.location)
      .withProcessingData(BaseUnitProcessingData().withSourceSpec(EnvoyConfig))
    parseStaticResources() foreach { resources =>
      parseListeners(resources)
    }
    doc
  }

  private def parseListeners(resources: YMap) = {
    EnvoyConfigProtocolListenerParser(resources).parse { protocolListener =>
      proxy.withProtocolListener(protocolListener)
    }
  }

  private def parseStaticResources(): Option[YMap] = {
    val rootMap = root.parsed.asInstanceOf[SyamlParsedDocument].document.node
    doc.withEncodes(Proxy(rootMap))
    traverse(rootMap)
      .errorFor(doc)
      .fetch("static_resources").map(collect)
  }

  override def error_handler: AMFErrorHandler = ctx.eh
}
