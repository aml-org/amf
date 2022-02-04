package amf.apiinstance.internal.spec.document

import amf.apiinstance.client.scala.model.domain.{ProtocolListener, Proxy}
import amf.apiinstance.internal.spec.context.KongDeclarativeConfigContext
import amf.apiinstance.internal.utils.NodeTraverser
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, BaseUnitProcessingData, Document}
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.Root
import amf.core.internal.remote.KongConfig
import org.yaml.model.YMap

case class KongDeclarativeConfigDocumentParser(root: Root)(implicit ctx: KongDeclarativeConfigContext) extends NodeTraverser {

  val doc: Document = Document()
  private def proxy = doc.encodes.asInstanceOf[Proxy]

  def parseDocument(): BaseUnit = {
    doc
      .withLocation(root.location)
      .withProcessingData(BaseUnitProcessingData().withSourceSpec(KongConfig))

    val rootNode = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    doc.withEncodes(Proxy(rootNode))
    addListener()

    doc
  }

  def addListener() = {
    // by default we will just generate a TCP listener in the default
    // kong port, since this information is not present in the declarative
    // kong configuration.
    // kong works with two configuration parameters to listen for HTTP/gRPC
    // (proxy_listen) and TCP traffic (stream_listen)
    proxy.withProtocolListener(
      ProtocolListener()
        .withAddress("0.0.0.0")
        .withPort("8000")
        .withProtocol("TCP")
    )
  }

  override def error_handler: AMFErrorHandler = ctx.eh
}
