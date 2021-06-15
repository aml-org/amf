package amf.client.model.domain

import amf.client.convert.shapeconverters.ShapeClientConverters._
import amf.core.client.platform.model.{BoolField, StrField}
import amf.plugins.domain.shapes.models.{XMLSerializer => InternalXMLSerializer}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.core.client.platform.model.domain.DomainElement

/**
  * XMLSerializer model class.
  */
@JSExportAll
case class XMLSerializer(override private[amf] val _internal: InternalXMLSerializer) extends DomainElement {

  @JSExportTopLevel("model.domain.XMLSerializer")
  def this() = this(InternalXMLSerializer())

  def attribute: BoolField = _internal.attribute
  def wrapped: BoolField   = _internal.wrapped
  def name: StrField       = _internal.name
  def namespace: StrField  = _internal.namespace
  def prefix: StrField     = _internal.prefix

  def withAttribute(attribute: Boolean): this.type = {
    _internal.withAttribute(attribute)
    this
  }

  def withWrapped(wrapped: Boolean): this.type = {
    _internal.withWrapped(wrapped)
    this
  }

  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withNamespace(namespace: String): this.type = {
    _internal.withNamespace(namespace)
    this
  }

  def withPrefix(prefix: String): this.type = {
    _internal.withPrefix(prefix)
    this
  }
}
