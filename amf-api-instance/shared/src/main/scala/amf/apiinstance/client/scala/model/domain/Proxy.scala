package amf.apiinstance.client.scala.model.domain

import amf.apiinstance.internal.metamodel.domain.ProxyModel
import amf.apiinstance.internal.metamodel.domain.ProxyModel.{ProtocolListeners, UpstreamServices}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.metamodel.Obj
import amf.core.internal.parser.domain.{Annotations, Fields}
import org.yaml.model.{YMap, YNode}

case class Proxy(fields: Fields, annotations: Annotations) extends DomainElement {

  override def meta: Obj = ProxyModel

  def protocolListeners: Seq[ProtocolListener] = fields.field(ProtocolListeners)
  def withProtocolListeners(listeners: Seq[ProtocolListener]) = setArray(ProtocolListeners, listeners)
  def withProtocolListener(listener: ProtocolListener) = {
    val newListeners = protocolListeners ++ Seq(listener)
    withProtocolListeners(newListeners)
  }

  def upstreamServices: Seq[UpstreamService] = fields.field(UpstreamServices)
  def withUpstreamServices(upstreams: Seq[UpstreamService]) = setArray(UpstreamServices, upstreams)
  def withUpstreamService(upstream: UpstreamService) = {
    val newUpstreams = upstreamServices ++ Seq(upstream)
    withUpstreamServices(newUpstreams)
  }

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override private[amf] def componentId = "/proxy"
}

object Proxy {
  def apply(): Proxy = apply(Annotations())

  def apply(ast: YMap): Proxy = apply(Annotations(ast))

  def apply(node: YNode): Proxy = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): Proxy = Proxy(Fields(), annotations)
}