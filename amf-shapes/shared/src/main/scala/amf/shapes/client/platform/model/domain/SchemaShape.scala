package amf.shapes.client.platform.model.domain

import amf.core.client.scala.model.StrField
import amf.shapes.client.scala.model.domain
import amf.shapes.internal.convert.ShapeClientConverters.ClientOption
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.shapes.client.scala.model.domain.{SchemaShape => InternalSchemaShape}

@JSExportAll
case class SchemaShape(override private[amf] val _internal: domain.SchemaShape) extends AnyShape(_internal) {

  @JSExportTopLevel("SchemaShape")
  def this() = this(InternalSchemaShape())

  def mediaType: StrField            = _internal.mediaType
  def raw: StrField                  = _internal.raw
  def location: ClientOption[String] = _internal.location().asClient

  def withMediatype(mediaType: String): this.type = {
    _internal.withMediaType(mediaType)
    this
  }

  def withRaw(text: String): this.type = {
    _internal.withRaw(text)
    this
  }

  override def linkCopy(): SchemaShape = _internal.linkCopy()
}
