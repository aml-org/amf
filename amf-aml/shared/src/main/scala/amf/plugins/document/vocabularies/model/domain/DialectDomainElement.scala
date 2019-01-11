package amf.plugins.document.vocabularies.model.domain

import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel}
import amf.core.metamodel.{Field, Obj, Type}
import amf.core.model.BoolField
import amf.core.model.domain._
import amf.core.parser.{Annotations, Fields, Value}
import amf.core.vocabulary.ValueType
import amf.plugins.document.vocabularies.metamodel.domain.DialectDomainElementModel
import org.mulesoft.common.time.SimpleDateTime
import org.yaml.model.{YMap, YNode}

import scala.collection.mutable

case class DialectDomainElement(override val fields: Fields, annotations: Annotations)
    extends DynamicDomainElement
    with Linkable {

  val literalProperties: mutable.Map[String, Any]                                = mutable.HashMap()
  val linkProperties: mutable.Map[String, Any]                                   = mutable.HashMap()
  val mapKeyProperties: mutable.Map[String, Any]                                 = mutable.HashMap()
  val objectProperties: mutable.Map[String, DialectDomainElement]                = mutable.HashMap()
  val objectCollectionProperties: mutable.Map[String, Seq[DialectDomainElement]] = mutable.HashMap()
  val propertyAnnotations: mutable.Map[String, Annotations]                      = mutable.HashMap()

  def isAbstract: BoolField = fields.field(meta.asInstanceOf[DialectDomainElementModel].Abstract)
  def withAbstract(isAbstract: Boolean): DialectDomainElement = {
    set(meta.asInstanceOf[DialectDomainElementModel].Abstract, isAbstract)
    this
  }

  // Types of the instance
  protected var instanceTypes: Seq[String] = Nil
  def withInstanceTypes(types: Seq[String]): DialectDomainElement = {
    instanceTypes = types
    this
  }
  override def dynamicType: List[ValueType] =
    (instanceTypes.distinct.map(iriToValue) ++ DialectDomainElementModel().`type`).toList

  // Dialect mapping defining the instance
  protected var instanceDefinedBy: Option[NodeMapping] = None
  def withDefinedBy(nodeMapping: NodeMapping): DialectDomainElement = {
    instanceDefinedBy = Some(nodeMapping)
    this
  }
  def definedBy: NodeMapping = instanceDefinedBy match {
    case Some(mapping) => mapping
    case None          => throw new Exception("NodeMapping for the instance not defined")
  }

  def localRefName: String = {
    if (isLink)
      linkTarget.map(_.id.split("#").last.split("/").last).getOrElse {
        throw new Exception(s"Cannot produce local reference without linked element at elem $id")
      } else id.split("#").last.split("/").last
  }

  def includeName: String = {
    if (isLink)
      linkLabel
        .option()
        .getOrElse(
          linkTarget
            .map(_.id.split("#").head)
            .getOrElse(throw new Exception(s"Cannot produce include reference without linked element at elem $id")))
    else
      throw new Exception(s"Cannot produce include reference without linked element at elem $id")
  }

  def iriToValue(iri: String) = ValueType(iri)

  override def dynamicFields: List[Field] = {
    val mapKeyFields = mapKeyProperties.keys map { propertyId =>
      Field(Type.Str, iriToValue(propertyId))
    }

    (literalProperties.keys ++ linkProperties.keys ++ objectProperties.keys ++ objectCollectionProperties.keys).map {
      propertyId =>
        instanceDefinedBy.get.propertiesMapping().find(_.id == propertyId).get.toField
    }.toList ++ mapKeyFields ++ fields
      .fields()
      .filter(f => f.field != LinkableElementModel.Target && f.field != DomainElementModel.CustomDomainProperties)
      .map(_.field)
  }

  def findPropertyByTermPropertyId(termPropertyId: String): String =
    definedBy
      .propertiesMapping()
      .find(_.nodePropertyMapping().value() == termPropertyId)
      .map(_.id)
      .getOrElse(termPropertyId)

  def findPropertyMappingByTermPropertyId(termPropertyId: String): Option[PropertyMapping] =
    definedBy.propertiesMapping().find(_.nodePropertyMapping().value() == termPropertyId)

  override def valueForField(f: Field): Option[Value] = {
    val termPropertyId = f.value.iri()
    val propertyId     = findPropertyByTermPropertyId(termPropertyId)
    val annotations    = propertyAnnotations.getOrElse(propertyId, Annotations())

    // Warning, mapKey has the term property id, no the property mapping id because
    // there's no real propertyMapping for it
    mapKeyProperties.get(termPropertyId) map { stringValue =>
      AmfScalar(stringValue, annotations)
    } orElse objectProperties.get(propertyId) map { dialectDomainElement =>
      dialectDomainElement
    } orElse {
      objectCollectionProperties.get(propertyId) map { seqElements =>
        AmfArray(seqElements, annotations)
      }
    } orElse {
      literalProperties.get(propertyId) map {
        case vs: Seq[_] =>
          val scalars = vs.map { s =>
            AmfScalar(s)
          }
          AmfArray(scalars, annotations)
        case other =>
          AmfScalar(other, annotations)
      }
    } orElse {
      linkProperties.get(propertyId) map {
        case vs: Seq[_] =>
          val scalars = vs.map { s =>
            AmfScalar(s)
          }
          AmfArray(scalars, annotations)
        case other =>
          AmfScalar(other, annotations)
      }
    } map { Value(_, Annotations()) } orElse {
      fields.fields().find(_.field == f).map(_.value)
    }
  }

  protected def propertyMappingForField(field: Field): Option[PropertyMapping] = {
    val iri = field.value.iri()
    definedBy.propertiesMapping().find(_.nodePropertyMapping().value() == iri)
  }

  def removeField(patchField: Field) = {
    propertyMappingForField(patchField) match {
      case Some(property) =>
        val id = property.id
        mapKeyProperties.remove(id)
        objectCollectionProperties.remove(id)
        objectProperties.remove(id)
        literalProperties.remove(id)
        linkProperties.remove(id)
        propertyAnnotations.remove(id)
        fields.remove(id)
      case _ => // ignore
    }
  }

  def containsProperty(property: PropertyMapping): Boolean = {
    mapKeyProperties.contains(property.nodePropertyMapping().value()) ||
    objectCollectionProperties.contains(property.id) ||
    literalProperties.contains(property.id) ||
    linkProperties.contains(property.id)
  }

  def setObjectField(property: PropertyMapping, value: DialectDomainElement, node: YNode): DialectDomainElement = {
    objectProperties.put(property.id, value)
    propertyAnnotations.put(property.id, Annotations(node))
    if (value.isUnresolved) {
      value.toFutureRef {
        case resolvedDialectDomainElement: DialectDomainElement =>
          objectProperties.put(
            property.id,
            resolveUnreferencedLink(value.refName,
                                    value.annotations,
                                    resolvedDialectDomainElement,
                                    value.supportsRecursion.option().getOrElse(false))
              .withId(value.id)
          )
        case resolved =>
          throw new Exception(s"Cannot resolve reference with not dialect domain element value ${resolved.id}")
      }
    }
    this
  }

  def setObjectField(property: PropertyMapping, value: Seq[DialectDomainElement], node: YNode): DialectDomainElement = {
    objectCollectionProperties.put(property.id, value)
    propertyAnnotations.put(property.id, Annotations(node))
    value.foreach {
      case linkable: Linkable if linkable.isUnresolved =>
        linkable.toFutureRef((resolved) => {
          objectCollectionProperties.get(property.id) map { oldValues =>
            oldValues map { oldValue =>
              if (oldValue.id == resolved.id) {
                resolved
              } else {
                oldValue
              }
            }
          } foreach {
            case updatedValues: Seq[DialectDomainElement] =>
              objectCollectionProperties.put(property.id, updatedValues)
            case _ => // ignore
          }
        })
      case _ => // ignore
    }
    this
  }

  def patchLiteralField(field: Field, value: Any): DialectDomainElement = {
    propertyMappingForField(field) match {
      case Some(property) =>
        val id = property.id
        if (mapKeyProperties.contains(id)) {
          mapKeyProperties.put(id, value)
        } else if (linkProperties.contains(id)) {
          linkProperties.put(id, value.toString)
        } else {
          literalProperties.put(id, value)
        }
      case _ => // ignore
    }

    this
  }

  def patchObjectField(field: Field, value: DialectDomainElement): DialectDomainElement = {
    propertyMappingForField(field) match {
      case Some(property) =>
        val id = property.id
        if (mapKeyProperties.contains(id)) {
          mapKeyProperties.put(id, value)
        } else {
          objectProperties.put(id, value)
        }
      case _ => // ignore
    }

    this
  }

  def patchObjectField(field: Field, value: Seq[DialectDomainElement]): DialectDomainElement = {
    propertyMappingForField(field) match {
      case Some(property) =>
        val id = property.id
        objectCollectionProperties.put(id, value)
      case _ => // ignore
    }

    this
  }

  def setLinkField(property: PropertyMapping, value: String, node: YNode): DialectDomainElement = {
    linkProperties.put(property.id, value)
    propertyAnnotations.put(property.id, Annotations(node))
    this
  }

  def setLiteralField(property: PropertyMapping, value: Int, node: YNode): DialectDomainElement = {
    literalProperties.put(property.id, value)
    propertyAnnotations.put(property.id, Annotations(node))
    this
  }

  def setLiteralField(property: PropertyMapping, value: Float, node: YNode): DialectDomainElement = {
    literalProperties.put(property.id, value)
    propertyAnnotations.put(property.id, Annotations(node))
    this
  }

  def setLiteralField(property: PropertyMapping, value: Double, node: YNode): DialectDomainElement = {
    literalProperties.put(property.id, value)
    propertyAnnotations.put(property.id, Annotations(node))
    this
  }

  def setLiteralField(property: PropertyMapping, value: Boolean, node: YNode): DialectDomainElement = {
    literalProperties.put(property.id, value)
    propertyAnnotations.put(property.id, Annotations(node))
    this
  }

  def setLiteralField(property: PropertyMapping, value: Seq[_], node: YNode): DialectDomainElement = {
    literalProperties.put(property.id, value)
    propertyAnnotations.put(property.id, Annotations(node))
    this
  }

  def setLiteralField(property: PropertyMapping, value: String, node: YNode): DialectDomainElement = {
    literalProperties.put(property.id, value)
    propertyAnnotations.put(property.id, Annotations(node))
    this
  }

  def setLiteralField(property: PropertyMapping, value: SimpleDateTime, node: YNode): DialectDomainElement = {
    literalProperties.put(property.id, value)
    propertyAnnotations.put(property.id, Annotations(node))
    this
  }

  def setMapKeyField(propertyId: String, value: String, node: YNode): DialectDomainElement = {
    mapKeyProperties.put(propertyId, value)
    this
  }

  override def meta: Obj =
    if (instanceTypes.isEmpty) {
      DialectDomainElementModel()
    } else {
      new DialectDomainElementModel(instanceTypes.head, dynamicFields, Some(definedBy))
    }

  override def adopted(newId: String): DialectDomainElement.this.type =
    if (Option(this.id).isEmpty) simpleAdoption(newId) else this

  override def linkCopy(): Linkable =
    DialectDomainElement().withId(id).withDefinedBy(definedBy).withInstanceTypes(instanceTypes)

  override def resolveUnreferencedLink[T](label: String,
                                          annotations: Annotations,
                                          unresolved: T,
                                          supportsRecursion: Boolean): T = {
    val unresolvedNodeMapping = unresolved.asInstanceOf[DialectDomainElement]
    val linked: T             = unresolvedNodeMapping.link(label, annotations)
    if (supportsRecursion && linked.isInstanceOf[Linkable])
      linked.asInstanceOf[Linkable].withSupportsRecursion(supportsRecursion)
    linked.asInstanceOf[DialectDomainElement].asInstanceOf[T]
  }

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = ""

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    DialectDomainElement.apply
}

object DialectDomainElement {
  def apply(): DialectDomainElement = apply(Annotations())

  def apply(ast: YMap): DialectDomainElement = apply(Annotations(ast))

  def apply(annotations: Annotations): DialectDomainElement = DialectDomainElement(Fields(), annotations)

}
