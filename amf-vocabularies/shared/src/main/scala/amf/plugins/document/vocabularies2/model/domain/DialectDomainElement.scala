package amf.plugins.document.vocabularies2.model.domain

import amf.core.metamodel.Type
import amf.core.metamodel.{Field, Obj}
import amf.core.model.domain.{AmfArray, AmfElement, AmfScalar, DynamicDomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies2.metamodel.domain.DialectDomainElementModel
import org.yaml.model.YMap

import scala.collection.mutable

case class DialectDomainElement(override val fields: Fields, val annotations: Annotations) extends DynamicDomainElement {

  val literalProperties: mutable.Map[String, Any] = mutable.HashMap()
  val objectProperties: mutable.Map[String, DialectDomainElement] = mutable.HashMap()
  val objectCollectionProperties: mutable.Map[String, Seq[DialectDomainElement]] = mutable.HashMap()
  val propertyAnnotations: mutable.Map[String, Annotations] = mutable.HashMap()

  // Types of the instance
  protected var instanceTypes: Seq[String] = Nil
  def withInstanceTypes(types: Seq[String]) = instanceTypes = types
  override def dynamicType: List[ValueType] = (instanceTypes.distinct.map(ValueType(_)) ++ DialectDomainElementModel.`type`).toList

  // Dialect mapping defining the instance
  protected var instanceDefinedBy: Option[NodeMapping] = None
  def withDefinedBy(nodeMapping: NodeMapping) = {
    instanceDefinedBy = Some(nodeMapping)
    this
  }
  def definedBy: NodeMapping = instanceDefinedBy.orNull


  override def dynamicFields: List[Field] = {
    (literalProperties.keys ++ objectProperties.keys ++ objectCollectionProperties.keys).map { propertyId =>
      val property = instanceDefinedBy.get.propertiesMapping().find(_.id == propertyId).get
      val propertyIdValue = ValueType(property.id)
      Option(property.objectRange()).map { objProp =>
        if (property.allowMultiple()) {
          Field(Type.Array(DialectDomainElementModel), propertyIdValue)
        } else {
          Field(DialectDomainElementModel, propertyIdValue)
        }
      }.getOrElse {
        val fieldType = property.literalRange() match {
          case literal if literal.endsWith("anyUri")  => Type.Iri
          case literal if literal.endsWith("anyType") => Type.Any
          case literal if literal == (Namespace.Xsd + "integer").iri() => Type.Int
          case literal if literal == (Namespace.Xsd + "float").iri() => Type.Float
          case literal if literal == (Namespace.Xsd + "boolean").iri() => Type.Bool
          case literal if literal == (Namespace.Xsd + "decimal").iri() => Type.Int
          case literal if literal == (Namespace.Xsd + "time").iri() => Type.Time
          case literal if literal == (Namespace.Xsd + "date").iri() => Type.Date
          case _ => Type.Str
        }

        if (property.allowMultiple()) {
          Field(Type.Array(fieldType), propertyIdValue)
        } else {
          Field(fieldType, propertyIdValue)
        }
      }
    }.toList
  }

  override def valueForField(f: Field): Option[AmfElement] = {
    val propertyId = f.value.iri()
    val annotations = propertyAnnotations.getOrElse(propertyId, Annotations())

    objectProperties.get(propertyId) map { dialectDomainElement =>
      dialectDomainElement
    } orElse  {
      objectCollectionProperties.get(propertyId) map { seqElements =>
        AmfArray(seqElements, annotations)
      }
    } orElse {
      literalProperties.get(propertyId) map { elems =>
        elems match {
          case vs: Seq[_] =>
            val scalars = vs.map { s => AmfScalar(s) }
            AmfArray(scalars)
          case other =>
            AmfScalar(other)
        }
      }
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

  override def meta: Obj = DialectDomainElementModel

  override def adopted(newId: String): DialectDomainElement.this.type = if (Option(this.id).isEmpty) withId(newId) else this
}

object DialectDomainElement {
  def apply(): DialectDomainElement = apply(Annotations())

  def apply(ast: YMap): DialectDomainElement = apply(Annotations(ast))

  def apply(annotations: Annotations): DialectDomainElement = DialectDomainElement(Fields(), annotations)
}