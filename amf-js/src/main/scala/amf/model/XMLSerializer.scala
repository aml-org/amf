package amf.model

import amf.plugins.domain.shapes.models

import scala.scalajs.js.annotation.JSExportAll

/**
  * JS XMLSerializer model class.
  */
@JSExportAll
case class XMLSerializer private[model] (private[amf] val xmlSerializer: models.XMLSerializer)
    extends DomainElement {

  def this() = this(models.XMLSerializer())

  val attribute: Boolean = xmlSerializer.attribute
  val wrapped: Boolean   = xmlSerializer.wrapped
  val name: String       = xmlSerializer.name
  val namespace: String  = xmlSerializer.namespace
  val prefix: String     = xmlSerializer.prefix

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
