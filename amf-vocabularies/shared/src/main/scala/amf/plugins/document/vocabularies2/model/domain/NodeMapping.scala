package amf.plugins.document.vocabularies2.model.domain

import amf.core.metamodel.Obj
import amf.core.utils._
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies2.metamodel.domain.NodeMappingModel
import amf.plugins.document.vocabularies2.metamodel.domain.NodeMappingModel._
import org.yaml.model.YMap

case class NodeMapping(fields: Fields, annotations: Annotations) extends DomainElement with Linkable {

  override def meta: Obj = NodeMappingModel
  override def adopted(parent: String): NodeMapping.this.type = withId(parent + "/" + name.urlEncoded)

  def name: String                                       = fields(Name)
  def withName(name: String)                             = set(Name, name)
  def nodetypeMapping: String                            = fields(NodeTypeMapping)
  def withNodeTypeMapping(nodeType: String)              = set(NodeTypeMapping, nodeType)
  def propertiesMapping(): Seq[PropertyMapping]          = fields(PropertiesMapping)
  def withPropertiesMapping(props: Seq[PropertyMapping]) = setArrayWithoutId(PropertiesMapping, props)

  override def linkCopy(): Linkable = NodeMapping().withId(id)
}

object NodeMapping {
  def apply(): NodeMapping = apply(Annotations())

  def apply(ast: YMap): NodeMapping = apply(Annotations(ast))

  def apply(annotations: Annotations): NodeMapping = NodeMapping(Fields(), annotations)
}