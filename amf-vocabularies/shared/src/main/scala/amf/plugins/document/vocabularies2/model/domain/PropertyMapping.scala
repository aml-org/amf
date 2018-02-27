package amf.plugins.document.vocabularies2.model.domain

import amf.core.metamodel.Obj
import amf.core.model.domain.{AmfScalar, DomainElement}
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
  def withLiteralRange(range: String)             = set(LiteralRange, range)
  def literalRange(): String                      = fields(LiteralRange)
  def withObjectRange(range: String)              = set(ObjectRange, range)
  def objectRange(): String                       = fields(ObjectRange)
  def mapKeyProperty(): String                    = fields(MapKeyProperty)
  def withMapKeyProperty(key: String)             = set(MapKeyProperty, key)
  def mapValueProperty(): String                  = fields(MapValueProperty)
  def withMapValueProperty(value: String)         = set(MapValueProperty, value)
  def minCount(): Int                             = fields(MinCount)
  def withMinCount(minCount: Int)                 = set(MinCount, minCount)
  def pattern(): String                           = fields(Pattern)
  def withPattern(pattern: String)                = set(Pattern, pattern)
  def minimum(): Int                              = fields(Minimum)
  def withMinimum(min: Int)                       = set(Minimum, min)
  def maximum(): Int                              = fields(Maximum)
  def withMaximum(max: Int)                       = set(Maximum, max)
  def allowMultiple(): Boolean                    = fields(AllowMultiple)
  def withAllowMultiple(allow: Boolean)           = set(AllowMultiple, allow)
  def enum(): Seq[Any]                            = fields(PropertyMappingModel.Enum)
  def withEnum(values: Seq[Any])                  = setArray(PropertyMappingModel.Enum, values.map(AmfScalar(_)))
  def sorted(): Boolean                           = fields(Sorted)
  def withSorted(sorted: Boolean)                 = set(Sorted, sorted)
}

object PropertyMapping {
  def apply(): PropertyMapping = apply(Annotations())

  def apply(ast: YMap): PropertyMapping = apply(Annotations(ast))

  def apply(annotations: Annotations): PropertyMapping = PropertyMapping(Fields(), annotations)
}