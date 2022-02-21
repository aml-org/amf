package amf.apiinstance.client.scala.model.domain

import amf.apiinstance.internal.metamodel.domain.FilterRuleModel
import amf.apiinstance.internal.metamodel.domain.FilterRuleModel._
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.metamodel.Obj
import amf.core.internal.parser.domain.{Annotations, Fields}
import org.yaml.model.{YMap, YNode}

case class FilterRule(fields: Fields, annotations: Annotations) extends DomainElement {
  override def meta: Obj = FilterRuleModel

  def destinationPorts: Seq[String] = fields.field(DestinationPort)
  def withDestinationPorts(ports: Seq[String]) = set(DestinationPort, ports)

  def destinationIPAddresses: Seq[String] = fields.field(DestinationIPAddress)
  def withDestinationIPAddresses(ports: Seq[String]) = set(DestinationIPAddress, ports)

  def sourcePorts: Seq[String] = fields.field(SourcePort)
  def withSourcePorts(ports: Seq[String]) = set(SourcePort, ports)

  def sourceIPAddresses: Seq[String] = fields.field(SourceIpAddress)
  def withSourceIPAddresses(ports: Seq[String]) = set(SourceIpAddress, ports)

  def directSourceIPAddresses: Seq[String] = fields.field(DirectSourceIpAddress)
  def withDirectSourceIPAddresses(ports: Seq[String]) = set(DirectSourceIpAddress, ports)

  def hosts: Seq[String] = fields.field(Host)
  def withHosts(ports: Seq[String]) = set(Host, ports)

  def methods: Seq[String] = fields.field(Method)
  def withMethods(methods: Seq[String]) = set(Method, methods)

  def paths: Seq[String] = fields.field(Path)
  def withPaths(paths: Seq[String]) = set(Path, paths)

  def headers: Seq[String] = fields.field(Header)
  def withHeaders(headers: Seq[String]) = set(Header, headers)

  def enabled: Boolean = fields.field(Enabled)
  def withEnabled(enabled: Boolean) = set(Enabled, enabled)

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override private[amf] def componentId = "/rule"
}

object FilterRule {
  def apply(): FilterRule = apply(Annotations())

  def apply(ast: YMap): FilterRule = apply(Annotations(ast))

  def apply(node: YNode): FilterRule = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): FilterRule = FilterRule(Fields(), annotations)
}