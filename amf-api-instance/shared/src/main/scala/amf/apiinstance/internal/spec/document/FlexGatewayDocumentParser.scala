package amf.apiinstance.internal.spec.document

import amf.apiinstance.client.scala.model.domain.{ProtocolListener, Proxy}
import amf.apiinstance.internal.spec.context.{FlexGWConfigContext, KongDeclarativeConfigContext}
import amf.apiinstance.internal.utils.NodeTraverser
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, BaseUnitProcessingData, Document}
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.Root
import amf.core.internal.remote.Spec.FLEXGW
import amf.core.internal.remote.{KongConfig, Spec}
import jdk.jfr.FlightRecorder.addListener
import org.yaml.model.YMap


case class FlexGatewayDocumentParser(root: Root)(implicit ctx: FlexGWConfigContext) extends NodeTraverser {

  val doc: Document = Document()
  private def proxy = doc.encodes.asInstanceOf[Proxy]

  def parseDocument(): BaseUnit = {
    doc
      .withLocation(root.location)
      .withProcessingData(BaseUnitProcessingData().withSourceSpec(FLEXGW))

    val rootNode = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    doc.withEncodes(Proxy(rootNode))
    traverse(rootNode).errorFor(proxy).fetch("spec").mapOr(()) { spec =>
      addListener(spec)
    }
    doc
  }

  def addListener(spec: YMap): Unit = {
    val listener = ProtocolListener(spec).withName("default")
    proxy.withProtocolListener(listener)
    val address = traverse(spec).errorFor(listener).fetch("address").stringOr("http://0.0.0.0")
    val parts = address.split("://")
    if (address.contains("://")) {
      val protocol  = parts.head
      listener.withProtocol(protocol)
    }
    if (parts.last.contains(":")) {
      val domain = parts.last.split(":").head
      val portAndPath = parts.last.split(":").last.split("/")
      listener.withPort(portAndPath.head).withAddress(domain)
      ctx.withPath(portAndPath.tail.headOption)
    } else {
      val domainAndPath = parts.last.split("/")
      listener.withPort("80").withAddress(domainAndPath.head)
      ctx.withPath(domainAndPath.tail.headOption)
    }
  }

  override def error_handler: AMFErrorHandler = ctx.eh
}
