package amf.plugins.document.vocabularies.model.domain

import amf.client.model._
import amf.core.metamodel.{Field, Obj, Type}
import amf.core.model.domain.{AmfScalar, DomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.metamodel.domain.{DialectDomainElementModel, PropertyMappingModel}
import amf.plugins.document.vocabularies.metamodel.domain.PropertyMappingModel._
import org.yaml.model.YMap

class PropertyClassification
object ExtensionPointProperty       extends PropertyClassification
object LiteralProperty              extends PropertyClassification
object ObjectProperty               extends PropertyClassification
object ObjectPropertyCollection     extends PropertyClassification
object ObjectMapProperty            extends PropertyClassification
object ObjectMapInheritanceProperty extends PropertyClassification
object ObjectPairProperty           extends PropertyClassification
object LiteralPropertyCollection    extends PropertyClassification

case class PropertyMapping(fields: Fields, annotations: Annotations) extends DomainElement {

  override def meta: Obj                                          = PropertyMappingModel
  override def adopted(parent: String): PropertyMapping.this.type = withId(parent)

  def withName(name: String)                      = set(Name, name)
  def name(): StrField                            = fields.field(Name)
  def withNodePropertyMapping(propertyId: String) = set(NodePropertyMapping, propertyId)
  def nodePropertyMapping(): StrField             = fields.field(NodePropertyMapping)
  def withLiteralRange(range: String)             = set(LiteralRange, range)
  def literalRange(): StrField                    = fields.field(LiteralRange)
  def withObjectRange(range: Seq[String])         = set(ObjectRange, range)
  def objectRange(): Seq[StrField]                = fields.field(ObjectRange)
  def mapKeyProperty(): StrField                  = fields.field(MapKeyProperty)
  def withMapKeyProperty(key: String)             = set(MapKeyProperty, key)
  def mapValueProperty(): StrField                = fields.field(MapValueProperty)
  def withMapValueProperty(value: String)         = set(MapValueProperty, value)
  def minCount(): IntField                        = fields.field(MinCount)
  def withMinCount(minCount: Int)                 = set(MinCount, minCount)
  def pattern(): StrField                         = fields.field(Pattern)
  def withPattern(pattern: String)                = set(Pattern, pattern)
  def minimum(): DoubleField                      = fields.field(Minimum)
  def withMinimum(min: Double)                    = set(Minimum, min)
  def maximum(): DoubleField                      = fields.field(Maximum)
  def withMaximum(max: Double)                    = set(Maximum, max)
  def allowMultiple(): BoolField                  = fields.field(AllowMultiple)
  def withAllowMultiple(allow: Boolean)           = set(AllowMultiple, allow)
  def enum(): Seq[AnyField]                       = fields.field(PropertyMappingModel.Enum)
  def withEnum(values: Seq[Any])                  = setArray(PropertyMappingModel.Enum, values.map(AmfScalar(_)))
  def sorted(): BoolField                         = fields.field(Sorted)
  def withSorted(sorted: Boolean)                 = set(Sorted, sorted)
  def typeDiscrminator(): Map[String, String] =
    Option(fields(TypeDiscriminator)).map { disambiguator: String =>
      disambiguator.split(",").foldLeft(Map[String, String]()) {
        case (acc, typeMapping) =>
          val pair = typeMapping.split("->")
          acc + (pair(1) -> pair(0))
      }
    }.orNull
  def withTypeDiscriminator(typesMapping: Map[String, String]) =
    set(TypeDiscriminator, typesMapping.map { case (a, b) => s"$a->$b" }.mkString(","))
  def typeDiscriminatorName(): StrField       = fields.field(TypeDiscriminatorName)
  def withTypeDiscriminatorName(name: String) = set(TypeDiscriminatorName, name)

  def classification(): PropertyClassification = {
    val isAnyNode = objectRange().exists { obj =>
      obj.value() == (Namespace.Meta + "anyNode").iri()
    }
    val isLiteral  = literalRange().nonNull
    val isObject   = objectRange().nonEmpty
    val multiple   = allowMultiple().option().getOrElse(false)
    val isMap      = mapKeyProperty().nonNull
    val isMapValue = mapValueProperty().nonNull

    if (isAnyNode)
      ExtensionPointProperty
    else if (isLiteral && !multiple)
      LiteralProperty
    else if (isLiteral)
      LiteralPropertyCollection
    else if (isObject && isMap && isMapValue)
      ObjectPairProperty
    else if (isObject && isMap)
      ObjectMapProperty
    else if (isObject && !multiple)
      ObjectProperty
    else
      ObjectPropertyCollection
  }

  def nodesInRange: Seq[String] = {
    val range = objectRange()
    if (range.isEmpty) {
      Option(typeDiscrminator()).getOrElse(Map()).values.toSeq
    } else {
      range.map(_.value())
    }
  }

  def isUnion: Boolean = nodesInRange.nonEmpty

  def toField(): Field = {
    val propertyIdValue = ValueType(nodePropertyMapping().value())

    val isObjectRange = objectRange.nonEmpty || Option(typeDiscrminator()).isDefined

    if (isObjectRange) {
      if (allowMultiple().value() || mapKeyProperty().nonNull) {
        Field(Type.Array(DialectDomainElementModel()), propertyIdValue)
      } else {
        Field(DialectDomainElementModel(), propertyIdValue)
      }
    } else {
      val fieldType = literalRange().value() match {
        case literal if literal.endsWith("anyUri")                   => Type.Iri
        case literal if literal.endsWith("anyType")                  => Type.Any
        case literal if literal.endsWith("number")                   => Type.Float
        case literal if literal == (Namespace.Xsd + "integer").iri() => Type.Int
        case literal if literal == (Namespace.Xsd + "float").iri()   => Type.Float
        case literal if literal == (Namespace.Xsd + "boolean").iri() => Type.Bool
        case literal if literal == (Namespace.Xsd + "decimal").iri() => Type.Int
        case literal if literal == (Namespace.Xsd + "time").iri()    => Type.Time
        case literal if literal == (Namespace.Xsd + "date").iri()    => Type.Date
        case _                                                       => Type.Str
      }

      if (allowMultiple().value()) {
        Field(Type.Array(fieldType), propertyIdValue)
      } else {
        Field(fieldType, propertyIdValue)
      }
    }
  }
}

object PropertyMapping {
  def apply(): PropertyMapping = apply(Annotations())

  def apply(ast: YMap): PropertyMapping = apply(Annotations(ast))

  def apply(annotations: Annotations): PropertyMapping = PropertyMapping(Fields(), annotations)
}
