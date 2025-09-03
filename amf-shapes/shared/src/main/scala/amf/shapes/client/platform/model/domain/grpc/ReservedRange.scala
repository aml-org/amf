package amf.shapes.client.platform.model.domain.grpc

import amf.core.client.platform.model.IntField
import amf.core.client.platform.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.grpc.{ReservedRange => InternalRange}
import amf.shapes.internal.convert.ShapeClientConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ReservedRange(override private[amf] val _internal: InternalRange) extends DomainElement {

  @JSExportTopLevel("Range")
  def this() = this(InternalRange())

  def from: IntField = _internal.from
  def to: IntField   = _internal.to

  def withFrom(from: Int): this.type = {
    _internal.withFrom(from)
    this
  }

  def withTo(to: Int): this.type = {
    _internal.withTo(to)
    this
  }

}
