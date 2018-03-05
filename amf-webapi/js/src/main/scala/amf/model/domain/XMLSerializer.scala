package amf.model.domain

import amf.plugins.domain.shapes.models

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * JS XMLSerializer model class.
  */
@JSExportAll
case class XMLSerializer private[model] (private[amf] val xmlSerializer: models.XMLSerializer) extends DomainElement {

  @JSExportTopLevel("model.domain.XMLSerializer")
  def this() = this(models.XMLSerializer())

  def attribute: Boolean = xmlSerializer.attribute
  def wrapped: Boolean   = xmlSerializer.wrapped
  def name: String       = xmlSerializer.name
  def namespace: String  = xmlSerializer.namespace
  def prefix: String     = xmlSerializer.prefix

  override private[amf] def element: models.XMLSerializer = xmlSerializer

  def withAttribute(attribute: Boolean): this.type = {
    xmlSerializer.withAttribute(attribute)
    this
  }

  def withWrapped(wrapped: Boolean): this.type = {
    xmlSerializer.withWrapped(wrapped)
    this
  }

  def withName(name: String): this.type = {
    xmlSerializer.withName(name)
    this
  }

  def withNamespace(namespace: String): this.type = {
    xmlSerializer.withNamespace(namespace)
    this
  }

  def withPrefix(prefix: String): this.type = {
    xmlSerializer.withPrefix(prefix)
    this
  }
}
