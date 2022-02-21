package amf.apiinstance.client.scala.model.domain

import amf.apicontract.internal.metamodel.domain.ServerModel.Protocol
import amf.apiinstance.internal.metamodel.domain.ProtocolListenerModel
import amf.apiinstance.internal.metamodel.domain.ProtocolListenerModel.{Description, Filters, Name, NamedNetworkPort, NetworkAddress, NetworkPort, Pipe, PipeMode}
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.metamodel.Obj
import amf.core.internal.parser.domain.{Annotations, Fields}
import org.yaml.model.{YMap, YNode}

case class ProtocolListener(fields: Fields, annotations: Annotations) extends DomainElement {

  override def meta: Obj = ProtocolListenerModel

  def name: StrField            = fields.field(Name)
  def description: StrField     = fields.field(Description)
  def protocol: StrField        = fields.field(Protocol)
  def address: StrField         = fields.field(NetworkAddress)
  def port: StrField            = fields.field(NetworkPort)
  def namedPort: StrField       = fields.field(NamedNetworkPort)
  def pipe: StrField            = fields.field(Pipe)
  def pipeMode: StrField        = fields.field(PipeMode)
  def filters: Seq[FilterChain] = fields.field(Filters)

  def withName(name: String) = set(Name, name)
  def withDescription(description: String) = set(Description, description)
  def withProtocol(protocol: String) = set(Protocol, protocol)
  def withAddress(address: String) = set(NetworkAddress, address)
  def withPort(port: String) = set(NetworkPort, port)
  def withNamedPort(namedPort: String) = set(NamedNetworkPort, namedPort)
  def withPipe(pipe: String) = set(Pipe, pipe)
  def withPipeMode(pipeMode: String) = set(PipeMode, pipeMode)
  def withFilters(filters: Seq[FilterChain]) = setArray(Filters, filters)


  /** Value , path + field value that is used to compose the id when the object its adopted */
  override private[amf] def componentId = s"/${name.value()}"

}

object ProtocolListener {
  def apply(): ProtocolListener = apply(Annotations())

  def apply(ast: YMap): ProtocolListener = apply(Annotations(ast))

  def apply(node: YNode): ProtocolListener = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): ProtocolListener = ProtocolListener(Fields(), annotations)
}