package amf.plugins.document.vocabularies2.model.domain

import amf.core.metamodel.Obj
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies2.metamodel.domain.PropertyMappingModel
import amf.plugins.document.vocabularies2.metamodel.domain.PropertyMappingModel._
import org.yaml.model.YMap

case class PropertyMapping(fields: Fields, annotations: Annotations) extends DomainElement {

  override def meta: Obj = PropertyMappingModel
  override def adopted(parent: String): PropertyMapping.this.type = withId(parent)

  def withName(name: String)                      = set(Name, name)
  def name(): String                              = fields(Name)
  def withNodePropertyMapping(propertyId: String) = set(NodePropertyMapping, propertyId)
  def nodePropertyMapping(): String               = fields(NodePropertyMapping)
}

object PropertyMapping {
  def apply(): PropertyMapping = apply(Annotations())

  def apply(ast: YMap): PropertyMapping = apply(Annotations(ast))

  def apply(annotations: Annotations): PropertyMapping = PropertyMapping(Fields(), annotations)
}