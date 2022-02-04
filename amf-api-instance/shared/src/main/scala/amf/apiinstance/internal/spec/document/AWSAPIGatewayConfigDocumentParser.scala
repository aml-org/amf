package amf.apiinstance.internal.spec.document

import amf.apiinstance.client.scala.model.domain.{ProtocolListener, Proxy}
import amf.apiinstance.internal.spec.context.{AWSAPIGWConfigContext, KongDeclarativeConfigContext}
import amf.apiinstance.internal.utils.NodeTraverser
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, BaseUnitProcessingData, Document}
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.Root
import amf.core.internal.remote.AWSAPIGWConfig
import org.yaml.model.YMap

case class AWSAPIGatewayConfigDocumentParser(root: Root)(implicit ctx: AWSAPIGWConfigContext) extends NodeTraverser {

  val doc: Document = Document()
  private def proxy = doc.encodes.asInstanceOf[Proxy]

  def parseDocument(): BaseUnit = {
    doc
      .withLocation(root.location)
      .withProcessingData(BaseUnitProcessingData().withSourceSpec(AWSAPIGWConfig))

    val rootNode = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    doc.withEncodes(Proxy(rootNode))
    addListener()

    doc
  }

  def addListener() = {
    // by default we will just generate a TCP listener in the default
    // HTTP port since there is no other information configuration options
    // available
    proxy.withProtocolListener(
      ProtocolListener()
        .withPort("80")
        .withProtocol("TCP")
    )
  }

  override def error_handler: AMFErrorHandler = ctx.eh
}
