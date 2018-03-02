package amf.core.model.domain

import amf.core.annotations.LexicalInformation
import amf.core.metamodel.Obj
import amf.core.metamodel.domain.DomainElementModel._
import amf.core.model.domain.extensions.{BaseDomainExtension, DomainExtension}
import amf.core.parser.{FieldEntry, Range}
import amf.core.vocabulary.Namespace

/**
  * Internal model for any domain element
  */
trait DomainElement extends AmfObject {

  def meta: Obj

  def customDomainProperties: Seq[BaseDomainExtension] = fields(CustomDomainProperties)
  def extend: Seq[DomainElement]                       = fields(Extends)

  def withCustomDomainProperties(customProperties: Seq[BaseDomainExtension]): this.type =
    setArray(CustomDomainProperties, customProperties)

  def withCustomDomainProperty(customProperty: BaseDomainExtension): this.type =
    add(CustomDomainProperties, customProperty)

  def withExtends(extend: Seq[DomainElement]): this.type = setArray(Extends, extend)

  def getTypeIds(): List[String] = (dynamicTypes().toList ++ `type`.map(_.iri()) ++ meta.`type`.map(_.iri())).distinct

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

  def position(): Option[Range] = annotations.find(classOf[LexicalInformation]) match {
    case Some(info) => Some(info.range)
    case _          => None
  }
}
