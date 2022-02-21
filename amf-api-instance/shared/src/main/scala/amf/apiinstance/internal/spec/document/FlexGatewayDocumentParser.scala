package amf.apiinstance.internal.spec.document

import amf.apiinstance.client.scala.model.domain.{FilterChain, ProtocolListener, Proxy}
import amf.apiinstance.internal.spec.context.FlexGWConfigContext
import amf.apiinstance.internal.spec.domain.flex.{FlexGatewayPolicyParser, FlexGatewayUpstreamServiceParser, FlexRuleParser}
import amf.apiinstance.internal.utils.NodeTraverser
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, BaseUnitProcessingData, Document}
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.Root
import amf.core.internal.remote.Spec.FLEXGW
import org.yaml.model.{YMap, YScalar, YSequence}

case class FlexGatewayDocumentParser(root: Root)(implicit ctx: FlexGWConfigContext) extends NodeTraverser {

  val doc: Document = Document()

  private def proxy = doc.encodes.asInstanceOf[Proxy]

  private def listener = proxy.protocolListeners.head

  def parseDocument(): BaseUnit = {
    doc
      .withLocation(root.location)
      .withProcessingData(BaseUnitProcessingData().withSourceSpec(FLEXGW))

    val rootNode = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    doc.withEncodes(Proxy(rootNode))
    parseListeners(rootNode)
    parsePolicies(rootNode)
    parseServices(rootNode)
    doc
  }

  def parsePolicies(rootNode: YMap): Unit = {
    traverse(rootNode).errorFor(listener).fetch("spec").fetch("policies").arrayOr(()) { policiesNode: YSequence =>
      val filters = FilterChain(policiesNode)
      listener.withFilters(Seq(filters))

      policiesNode.nodes.foreach { policyNode =>
        traverse(policyNode).map {
          case Some(policyMap) =>
            FlexGatewayPolicyParser(policyMap).parse { parsedPolicy =>
              filters.withPolicy(parsedPolicy)
            }
            FlexRuleParser(policyMap).parse { parsedRule =>
              filters.withRule(parsedRule)
            }
          case _ => // TODO: record violation
        }
      }
    }
  }

  def parseServices(rootNode: YMap): Unit = {
    traverse(rootNode).errorFor(proxy).fetch("spec").fetch("services").mapOr(()) { servciesNode: YMap =>
      servciesNode.entries.foreach { serviceNodeEntry =>
        traverse(serviceNodeEntry.value) map {
          case Some(serviceMap) =>
            FlexGatewayUpstreamServiceParser(serviceMap).parse { parsedServiceUpstream =>
              parsedServiceUpstream.withName(serviceNodeEntry.key.as[YScalar].text)
              proxy.withUpstreamService(parsedServiceUpstream)
            }
          case _ => // TODO: record violation
        }
      }
    }
  }

  def parseListeners(rootNode: YMap): Unit = {
    traverse(rootNode).errorFor(proxy).fetch("spec").mapOr(()) { spec =>
      addListener(spec)
    }
  }

  def addListener(spec: YMap): Unit = {
    val listener = ProtocolListener(spec).withName("default")
    proxy.withProtocolListener(listener)
    val address = traverse(spec).errorFor(listener).fetch("address").stringOr("http://0.0.0.0")
    val parts = address.split("://")
    if (address.contains("://")) {
      val protocol = parts.head
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
