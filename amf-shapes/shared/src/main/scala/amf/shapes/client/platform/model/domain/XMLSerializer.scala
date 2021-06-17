package amf.shapes.client.platform.model.domain

import amf.core.client.platform.model.domain.DomainElement
import amf.core.client.platform.model.{BoolField, StrField}
import amf.shapes.client.scala.model.domain.{XMLSerializer => InternalXMLSerializer}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * XMLSerializer model class.
  */
@JSExportAll
case class XMLSerializer(override private[amf] val _internal: InternalXMLSerializer) extends DomainElement {

  @JSExportTopLevel("XMLSerializer")
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
