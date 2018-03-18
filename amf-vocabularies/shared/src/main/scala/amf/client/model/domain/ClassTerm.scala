package amf.client.model.domain

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.StrField
import amf.plugins.document.vocabularies.model.domain.{ClassTerm => InternalClassTerm}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Class term from a vocabulary model
  */
@JSExportAll
case class ClassTerm(override private[amf] val _internal: InternalClassTerm) extends DomainElement {

  @JSExportTopLevel("model.domain.ClassTerm")
  def this() = this(InternalClassTerm())

  /**
    * Name of the term used as the basis of the term URI identifeir
    * @return
    */
  def name: StrField = _internal.name

  /**
    * Human readable name for the term
    * @return
    */
  def displayName: StrField = _internal.displayName

  /**
    * Human readable long description of the term
    * @return
    */
  def description: StrField = _internal.description

  /**
    * Properties that have the class term in the domain
    * @return
    */
  def properties: ClientList[StrField] = _internal.properties.asClient

  /**
    * List of super terms for the class term
    * @return
    */
  def subClassOf: ClientList[StrField] = _internal.subClassOf.asClient

  /**
    * Sets the name for the class term
    * @param name
    * @return
    */
  def withName(name: String) = {
    _internal.withName(name)
    this
  }

  /**
    * Sets the human readable name of the term
    * @param displayName
    * @return
    */
  def withDisplayName(displayName: String) = {
    _internal.withDisplayName(displayName)
    this
  }

  /**
    * Sets the description of the class term
    * @param description
    * @return
    */
  def withDescription(description: String) = {
    _internal.withDescription(description)
    this
  }

  /**
    * Sets the class term in the domain of the provaded properties
    * @param properties
    * @return
    */
  def withProperties(properties: ClientList[String]) = {
    _internal.withProperties(properties.asInternal)
    this
  }

  /**
    * Sets the super classes for this class term
    * @param superClasses
    * @return
    */
  def withSubClassOf(superClasses: ClientList[String]) = {
    _internal.withSubClassOf(superClasses.asInternal)
    this
  }

}
