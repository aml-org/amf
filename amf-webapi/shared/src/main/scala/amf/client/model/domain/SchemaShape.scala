package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField
import amf.plugins.domain.shapes.models.{SchemaShape => InternalSchemaShape}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class SchemaShape(override private[amf] val _internal: InternalSchemaShape) extends AnyShape(_internal) {

  @JSExportTopLevel("model.domain.SchemaShape")
  def this() = this(InternalSchemaShape())

  def mediaType: StrField = _internal.mediaType
  def raw: StrField       = _internal.raw

  def withMediatype(mediaType: String): this.type = {
    _internal.withMediaType(mediaType)
    this
  }

  def withRaw(text: String): this.type = {
    _internal.withRaw(text)
    this
  }

  override def linkTarget: Option[DomainElement] =
    _internal.linkTarget.map({ case l: InternalSchemaShape => l }).asClient

  override def linkCopy(): SchemaShape = _internal.linkCopy()
}
