package amf.framework.model.domain

import amf.domain.FieldEntry
import amf.domain.`abstract`.{ParametrizedResourceType, ParametrizedTrait}
import amf.domain.extensions.DomainExtension
import amf.framework.metamodel.Obj
import amf.metadata.domain.DomainElementModel.{CustomDomainProperties, Extends, `type`}
import amf.model.{AmfArray, AmfObject, AmfScalar}
import amf.vocabulary.Namespace

/**
  * Internal model for any domain element
  */
trait DomainElement extends AmfObject {

  def meta: Obj

  def customDomainProperties: Seq[DomainExtension] = fields(CustomDomainProperties)
  def extend: Seq[DomainElement]                   = fields(Extends)

  def withCustomDomainProperties(customProperties: Seq[DomainExtension]): this.type =
    setArray(CustomDomainProperties, customProperties)

  def withExtends(extend: Seq[DomainElement]): this.type = setArray(Extends, extend)

  def withResourceType(name: String): ParametrizedResourceType = {
    val result = ParametrizedResourceType().withName(name)
    add(Extends, result)
    result
  }

  def withTrait(name: String): ParametrizedTrait = {
    val result = ParametrizedTrait().withName(name)
    add(Extends, result)
    result
  }

  def getTypeIds(): List[String] = dynamicTypes().toList ++ `type`.map(_.iri())

  def getPropertyIds(): List[String] = fields.fields().map(f => f.field.value.iri()).toList

  def getScalarByPropertyId(propertyId: String): List[Any] = {
    fields.fields().find { f: FieldEntry =>
      f.field.value.iri() == Namespace.uri(propertyId).iri()
    } match {
      case Some(fieldEntry) =>
        fieldEntry.element match {
          case scalar: AmfScalar                    => List(scalar.value)
          case arr: AmfArray if arr.values.nonEmpty => arr.values.toList
          case _                                    => List()
        }
      case None => List()
    }
  }

  def getObjectByPropertyId(propertyId: String): Seq[DomainElement] = {
    fields.fields().find { f: FieldEntry =>
      f.field.value.iri() == Namespace.uri(propertyId).iri()
    } match {
      case Some(fieldEntry) =>
        fieldEntry.element match {
          case entity: DomainElement => List(entity)
          case arr: AmfArray if arr.values.nonEmpty && arr.values.head.isInstanceOf[DomainElement] =>
            arr.values.map(_.asInstanceOf[DomainElement]).toList
          case _ => List()
        }
      case None => List()
    }
  }

  def position(): Option[amf.parser.Range] = annotations.find(classOf[LexicalInformation]) match {
    case Some(info) => Some(info.range)
    case _          => None
  }
}
