package amf.client.model.domain

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.StrField
import amf.plugins.document.vocabularies.model.domain.{
  DatatypePropertyTerm => InternalDatatypePropertyTerm,
  ObjectPropertyTerm => InternalObjectPropertyTerm,
  PropertyTerm => InternalPropertyTerm
}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
abstract class PropertyTerm(override private[amf] val _internal: InternalPropertyTerm) extends DomainElement {

  def name: StrField                      = _internal.name
  def displayName: StrField               = _internal.displayName
  def description: StrField               = _internal.description
  def range: StrField                     = _internal.range
  def subPropertyOf: ClientList[StrField] = _internal.subPropertyOf.asClient

  def withName(name: String) = {
    _internal.withName(name)
    this
  }
  def withDisplayName(displayName: String) = {
    _internal.withDisplayName(displayName)
    this
  }
  def withDescription(description: String) = {
    _internal.withDescription(description)
    this
  }
  def withRange(range: String) = {
    _internal.withRange(range)
    this
  }

  def withSubClasOf(superProperties: Seq[String]) = {
    _internal.withSubClassOf(superProperties)
    this
  }
}

/**
  * Object property term from a vocabulary
  * @param _internal
  */
@JSExportAll
case class ObjectPropertyTerm(override private[amf] val _internal: InternalObjectPropertyTerm)
    extends PropertyTerm(_internal) {

  @JSExportTopLevel("model.domain.ObjectPropertyTerm")
  def this() = this(InternalObjectPropertyTerm())

}

/**
  * Datatype property term from a vocabulary
  * @param _internal
  */
@JSExportAll
case class DatatypePropertyTerm(override private[amf] val _internal: InternalDatatypePropertyTerm)
    extends PropertyTerm(_internal) {

  @JSExportTopLevel("model.domain.DatatypePropertyTerm")
  def this() = this(InternalDatatypePropertyTerm())

}
