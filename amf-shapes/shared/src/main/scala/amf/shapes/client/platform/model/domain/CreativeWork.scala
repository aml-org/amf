package amf.shapes.client.platform.model.domain

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.DomainElement

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.shapes.client.scala.domain.models.{ CreativeWork => InternalCreativeWork }
import amf.shapes.internal.convert.ShapeClientConverters._

/**
  * CreativeWork model class.
  */
@JSExportAll
case class CreativeWork(private[amf] val _internal: InternalCreativeWork) extends DomainElement {

  @JSExportTopLevel("model.domain.CreativeWork")
  def this() = this(InternalCreativeWork())

  def url: StrField         = _internal.url
  def description: StrField = _internal.description
  def title: StrField       = _internal.title

  /** Set url property of this CreativeWork. */
  def withUrl(url: String): this.type = {
    _internal.withUrl(url)
    this
  }

  /** Set title property of this CreativeWork. */
  def withTitle(title: String): this.type = {
    _internal.withTitle(title)
    this
  }

  /** Set description property of this CreativeWork. */
  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }
}
