package amf.client.model.domain

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.DomainElement
import amf.plugins.domain.shapes.models.{CreativeWork => InternalClientCreativeWork}

import amf.client.convert.shapeconverters.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * CreativeWork model class.
  */
@JSExportAll
case class CreativeWork(private[amf] val _internal: InternalClientCreativeWork) extends DomainElement {

  @JSExportTopLevel("model.domain.CreativeWork")
  def this() = this(InternalClientCreativeWork())

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
