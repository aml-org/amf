package amf.client.model.domain

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.StrField
import amf.plugins.document.vocabularies.model.domain.{NodeMapping => InternalNodeMapping}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class NodeMapping(override private[amf] val _internal: InternalNodeMapping) extends DomainElement {

  @JSExportTopLevel("model.domain.NodeMapping")
  def this() = this(InternalNodeMapping())

  def name: StrField = _internal.name
  def withName(name: String) = {
    _internal.withName(name)
    this
  }
  def nodetypeMapping: StrField = _internal.nodetypeMapping
  def withNodeTypeMapping(nodeType: String) = {
    _internal.withNodeTypeMapping(nodeType)
    this
  }
  def propertiesMapping(): ClientList[PropertyMapping] = _internal.propertiesMapping().asClient
  def withPropertiesMapping(props: ClientList[PropertyMapping]) = {
    _internal.withPropertiesMapping(props.asInternal)
    this
  }
}
