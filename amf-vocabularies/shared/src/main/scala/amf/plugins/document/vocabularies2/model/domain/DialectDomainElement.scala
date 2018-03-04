package amf.plugins.document.vocabularies2.model.domain

import amf.core.metamodel.domain.LinkableElementModel
import amf.core.metamodel.{Field, Obj, Type}
import amf.core.model.domain._
import amf.core.parser.{Annotations, Fields}
import amf.core.vocabulary.ValueType
import amf.plugins.document.vocabularies2.metamodel.domain.DialectDomainElementModel
import org.yaml.model.YMap

import scala.collection.mutable

case class DialectDomainElement(override val fields: Fields, annotations: Annotations) extends DynamicDomainElement with Linkable {

  val literalProperties: mutable.Map[String, Any] = mutable.HashMap()
  val mapKeyProperties: mutable.Map[String, Any] = mutable.HashMap()
  val objectProperties: mutable.Map[String, DialectDomainElement] = mutable.HashMap()
  val objectCollectionProperties: mutable.Map[String, Seq[DialectDomainElement]] = mutable.HashMap()
  val propertyAnnotations: mutable.Map[String, Annotations] = mutable.HashMap()

  // Types of the instance
  protected var instanceTypes: Seq[String] = Nil
  def withInstanceTypes(types: Seq[String]) = {
    instanceTypes = types
    this
  }
  override def dynamicType: List[ValueType] = (instanceTypes.distinct.map(iriToValue) ++ DialectDomainElementModel().`type`).toList

  // Dialect mapping defining the instance
  protected var instanceDefinedBy: Option[NodeMapping] = None
  def withDefinedBy(nodeMapping: NodeMapping) = {
    instanceDefinedBy = Some(nodeMapping)
    this
  }
  def definedBy: NodeMapping = instanceDefinedBy.orNull


  def iriToValue(iri: String) = ValueType(iri)

  override def dynamicFields: List[Field] = {
    val mapKeyFields = mapKeyProperties.keys map { propertyId =>
        Field(Type.Str, iriToValue(propertyId))
    }

    (literalProperties.keys ++ objectProperties.keys ++ objectCollectionProperties.keys).map { propertyId =>
       val property = instanceDefinedBy.get.propertiesMapping().find(_.id == propertyId).get
       property.toField()
    }.toList ++ mapKeyFields ++ fields.fields().map(_.field)// ++ LinkableElementModel.fields
  }


  def findPropertyByTermPropertyId(termPropertyId: String) =
    definedBy.propertiesMapping().find(_.nodePropertyMapping() == termPropertyId).map(_.id).getOrElse(termPropertyId)


  override def valueForField(f: Field): Option[AmfElement] = {
    val termPropertyId = f.value.iri()
    val propertyId = findPropertyByTermPropertyId(termPropertyId)
    val annotations = propertyAnnotations.getOrElse(propertyId, Annotations())

    // Warning, mapKey has the term property id, no the property mapping id because
    // there's no real propertyMapping for it
    mapKeyProperties.get(termPropertyId) map { stringValue =>
      AmfScalar(stringValue)
    } orElse objectProperties.get(propertyId) map { dialectDomainElement =>
      dialectDomainElement
    } orElse  {
      objectCollectionProperties.get(propertyId) map { seqElements =>
        AmfArray(seqElements, annotations)
      }
    } orElse {
      literalProperties.get(propertyId) map {
        case vs: Seq[_] =>
          val scalars = vs.map { s => AmfScalar(s) }
          AmfArray(scalars)
        case other =>
          AmfScalar(other)
      }
    } orElse {
      fields.fields().find(_.field == f).map(_.element)
    }
  }

  def setObjectField(property: PropertyMapping, value: DialectDomainElement) = {
    objectProperties.put(property.id, value)
    this
  }

  def setObjectField(property: PropertyMapping, value: Seq[DialectDomainElement]) = {
    objectCollectionProperties.put(property.id, value)
    this
  }

  def setLiteralField(property: PropertyMapping, value: Int) = {
    literalProperties.put(property.id, value)
    this
  }

  def setLiteralField(property: PropertyMapping, value: Float) = {
    literalProperties.put(property.id, value)
    this
  }

  def setLiteralField(property: PropertyMapping, value: Boolean) = {
    literalProperties.put(property.id, value)
    this
  }

  def setLiteralField(property: PropertyMapping, value: Seq[_]) = {
    literalProperties.put(property.id, value)
    this
  }

  def setLiteralField(property: PropertyMapping, value: String) = {
    literalProperties.put(property.id, value)
    this
  }

  def setMapKeyField(propertyId: String, value: String) = {
    mapKeyProperties.put(propertyId, value)
    this
  }

  override def meta: Obj = if (instanceTypes.isEmpty) {
    DialectDomainElementModel()
  } else {
    new DialectDomainElementModel(instanceTypes.head, dynamicFields, Option(definedBy))
  }

  override def adopted(newId: String): DialectDomainElement.this.type = if (Option(this.id).isEmpty) withId(newId) else this

  override def linkCopy(): Linkable = DialectDomainElement().withId(id).withDefinedBy(definedBy)

  override def resolveUnreferencedLink[T](label: String, annotations: Annotations, unresolved: T): T = {
    val unresolvedNodeMapping = unresolved.asInstanceOf[DialectDomainElement]
    unresolvedNodeMapping.link(label, annotations).asInstanceOf[NodeMapping].withId(unresolvedNodeMapping.id).asInstanceOf[T]
  }
}

object DialectDomainElement {
  def apply(): DialectDomainElement = apply(Annotations())

  def apply(ast: YMap): DialectDomainElement = apply(Annotations(ast))

  def apply(annotations: Annotations): DialectDomainElement = DialectDomainElement(Fields(), annotations)

}