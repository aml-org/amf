package amf.shapes.client.platform.model.domain.operations

import amf.core.client.platform.model.domain.{DomainElement, Linkable, NamedDomainElement, Shape}
import amf.core.client.platform.model.StrField
import amf.core.internal.unsafe.PlatformSecrets

import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel}
import amf.shapes.client.scala.model.domain.operations.{ShapeResponse => InternalShapeResponse}
import amf.shapes.internal.convert.ShapeClientConverters._

@JSExportAll
case class ShapeResponse(override private[amf] val _internal: InternalShapeResponse) extends DomainElement with NamedDomainElement with PlatformSecrets {
  @JSExportTopLevel("ShapeResponse")
  def this() = this(InternalShapeResponse())

  /** Return DomainElement name. */
  override def name: StrField = _internal.name

  /** Update DomainElement name. */
  override def withName(name: String): ShapeResponse.this.type = {
    _internal.withName(name)
    this
  }
}