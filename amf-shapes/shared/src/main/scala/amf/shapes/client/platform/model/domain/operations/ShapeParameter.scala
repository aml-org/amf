package amf.shapes.client.platform.model.domain.operations

import amf.core.client.platform.model.domain.{DomainElement, Linkable, NamedDomainElement, Shape}
import amf.core.client.platform.model.StrField
import amf.core.internal.unsafe.PlatformSecrets

import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel}
import amf.shapes.client.scala.model.domain.operations.{ShapeParameter => InternalShapeParameter}
import amf.shapes.internal.convert.ShapeClientConverters._

@JSExportAll
case class ShapeParameter(override private[amf] val _internal: InternalShapeParameter) extends DomainElement with NamedDomainElement with PlatformSecrets {

  @JSExportTopLevel("ShapeParameter")
  def this() = this(InternalShapeParameter())

  /** Return DomainElement name. */
  override def name: StrField = _internal.name

  /** Update DomainElement name. */
  override def withName(name: String): ShapeParameter.this.type = {
    _internal.withName(name)
    this
  }
}
