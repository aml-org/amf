package amf.plugins.document.vocabularies.model.domain

import amf.core.metamodel.{Field, Obj, Type}
import amf.core.model.domain.{AmfScalar, DomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.metamodel.domain.{DialectDomainElementModel, PropertyMappingModel}
import amf.plugins.document.vocabularies.metamodel.domain.PropertyMappingModel._
import org.yaml.model.YMap

class PropertyClassification
object ExtensionPointProperty extends PropertyClassification
object LiteralProperty extends PropertyClassification
object ObjectProperty extends PropertyClassification
object ObjectPropertyCollection extends PropertyClassification
object ObjectMapProperty extends PropertyClassification
object ObjectMapInheritanceProperty extends PropertyClassification
object ObjectPairProperty extends PropertyClassification
object LiteralPropertyCollection extends PropertyClassification

case class PropertyMapping(fields: Fields, annotations: Annotations) extends DomainElement {

  override def meta: Obj = PropertyMappingModel
  override def adopted(parent: String): PropertyMapping.this.type = withId(parent)

  def withName(name: String)                      = set(Name, name)
  def name(): String                              = fields(Name)
  def withNodePropertyMapping(propertyId: String) = set(NodePropertyMapping, propertyId)
  def nodePropertyMapping(): String               = fields(NodePropertyMapping)
  def withLiteralRange(range: String)             = set(LiteralRange, range)
  def literalRange(): String                      = fields(LiteralRange)
  def withObjectRange(range: Seq[String])         = set(ObjectRange, range)
  def objectRange(): Seq[String]                  = fields(ObjectRange)
  def mapKeyProperty(): String                    = fields(MapKeyProperty)
  def withMapKeyProperty(key: String)             = set(MapKeyProperty, key)
  def mapValueProperty(): String                  = fields(MapValueProperty)
  def withMapValueProperty(value: String)         = set(MapValueProperty, value)
  def minCount(): Option[Int]                     = Option(fields(MinCount))
  def withMinCount(minCount: Int)                 = set(MinCount, minCount)
  def pattern(): String                           = fields(Pattern)
  def withPattern(pattern: String)                = set(Pattern, pattern)
  def minimum(): Option[Double]                   = Option(fields(Minimum))
  def withMinimum(min: Double)                    = set(Minimum, min)
  def maximum(): Option[Double]                   = Option(fields(Maximum))
  def withMaximum(max: Double)                    = set(Maximum, max)
  def allowMultiple(): Boolean                    = fields(AllowMultiple)
  def withAllowMultiple(allow: Boolean)           = set(AllowMultiple, allow)
  def enum(): Seq[Any]                            = fields(PropertyMappingModel.Enum)
  def withEnum(values: Seq[Any])                  = setArray(PropertyMappingModel.Enum, values.map(AmfScalar(_)))
  def sorted(): Boolean                           = fields(Sorted)
  def withSorted(sorted: Boolean)                 = set(Sorted, sorted)
  def typeDiscrminator(): Map[String,String]     = Option(fields(TypeDiscriminator)).map { disambiguator: String =>
    disambiguator.split(",").foldLeft(Map[String,String]()){ case (acc, typeMapping) =>
      val pair = typeMapping.split("->")
      acc + (pair(1) -> pair(0))
    }
  }.orNull
  def withTypeDiscriminator(typesMapping: Map[String,String]) = set(TypeDiscriminator, typesMapping.map {  case (a,b) => s"$a->$b" }.mkString(","))
  def typeDiscriminatorName(): String                         = fields(TypeDiscriminatorName)
  def withTypeDiscriminatorName(name: String)                 = set(TypeDiscriminatorName, name)

  def classification(): PropertyClassification = {
    val isAnyNode = Option(objectRange()).getOrElse(Nil).contains((Namespace.Meta + "anyNode").iri())
    val isLiteral = Option(literalRange()).isDefined
    val isObject = Option(objectRange()).isDefined && objectRange().nonEmpty
    val multiple = Option(allowMultiple()).getOrElse(false)
    val isMap = Option(mapKeyProperty()).isDefined
    val isMapValue = Option(mapValueProperty()).isDefined

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

  def nodesInRange: Seq[String] = Option(objectRange()).getOrElse(Option(typeDiscrminator()).getOrElse(Map()).values).toSeq

  def isUnion: Boolean = nodesInRange.nonEmpty

  def toField(): Field = {
    val propertyIdValue = ValueType(nodePropertyMapping())

    val isObjectRange = Option(objectRange()).isDefined || Option(typeDiscrminator()).isDefined

    if (isObjectRange) {
      if (allowMultiple() || Option(mapKeyProperty()).isDefined) {
        Field(Type.Array(DialectDomainElementModel()), propertyIdValue)
      } else {
        Field(DialectDomainElementModel(), propertyIdValue)
      }
    } else {
      val fieldType = literalRange() match {
        case literal if literal.endsWith("anyUri")  => Type.Iri
        case literal if literal.endsWith("anyType") => Type.Any
        case literal if literal.endsWith("number")  => Type.Float
        case literal if literal == (Namespace.Xsd + "integer").iri() => Type.Int
        case literal if literal == (Namespace.Xsd + "float").iri() => Type.Float
        case literal if literal == (Namespace.Xsd + "boolean").iri() => Type.Bool
        case literal if literal == (Namespace.Xsd + "decimal").iri() => Type.Int
        case literal if literal == (Namespace.Xsd + "time").iri() => Type.Time
        case literal if literal == (Namespace.Xsd + "date").iri() => Type.Date
        case _ => Type.Str
      }

      if (allowMultiple()) {
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