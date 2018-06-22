package amf.client.model.domain

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.StrField
import amf.plugins.document.vocabularies.model.domain.{NodeMapping => InternalNodeMapping}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class NodeMapping(override private[amf] val _internal: InternalNodeMapping) extends DomainElement {

  @JSExportTopLevel("model.domain.NodeMapping")
  def this() = this(InternalNodeMapping())

  def name: StrField                                   = _internal.name
  def nodetypeMapping: StrField                        = _internal.nodetypeMapping
  def propertiesMapping(): ClientList[PropertyMapping] = _internal.propertiesMapping().asClient
  def idTemplate: StrField                             = _internal.idTemplate

  def withName(name: String): NodeMapping = {
    _internal.withName(name)
    this
  }

  def withNodeTypeMapping(nodeType: String): NodeMapping = {
    _internal.withNodeTypeMapping(nodeType)
    this
  }

  def withPropertiesMapping(props: ClientList[PropertyMapping]): NodeMapping = {
    _internal.withPropertiesMapping(props.asInternal)
    this
  }

  def withIdTemplate(idTemplate: String) = {
    _internal.withIdTemplate(idTemplate)
    this
  }
}
