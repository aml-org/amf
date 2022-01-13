package amf.shapes.client.platform.model.domain.operations

import amf.core.client.platform.model.domain.{DomainElement, Linkable, NamedDomainElement, Shape}
import amf.core.client.platform.model.StrField
import amf.core.internal.unsafe.PlatformSecrets

import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel}
import amf.shapes.client.scala.model.domain.operations.{ShapeRequest => InternalShapeRequest}
import amf.shapes.internal.convert.ShapeClientConverters._

@JSExportAll
case class ShapeRequest(override private[amf] val _internal: InternalShapeRequest) extends DomainElement with NamedDomainElement with PlatformSecrets {

  @JSExportTopLevel("ShapeRequest")
  def this() = this(InternalShapeRequest())

  /** Return DomainElement name. */
  override def name: StrField = _internal.name

  /** Update DomainElement name. */
  override def withName(name: String): ShapeRequest.this.type = {
    _internal.withName(name)
    this
  }
}
