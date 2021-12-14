package amf.shapes.client.platform.model.domain.operations

import amf.core.client.platform.model.domain.{DomainElement, Linkable, NamedDomainElement, Shape}
import amf.core.client.platform.model.StrField
import amf.core.internal.unsafe.PlatformSecrets

import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel}
import amf.shapes.client.scala.model.domain.operations.{ShapeOperation => InternalShapeOperation}
import amf.shapes.internal.convert.ShapeClientConverters._

@JSExportAll
case class ShapeOperation(override private[amf] val _internal: InternalShapeOperation) extends DomainElement with NamedDomainElement with PlatformSecrets {

  @JSExportTopLevel("ShapeOperation")
  def this() = this(InternalShapeOperation())

  /** Return DomainElement name. */
  override def name: StrField = _internal.name

  /** Update DomainElement name. */
  override def withName(name: String): ShapeOperation.this.type = {
    _internal.withName(name)
    this
  }
}