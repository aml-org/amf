package amf.shapes.client.platform.model.domain.grpc

import amf.core.client.platform.model.domain.DomainElement
import amf.core.client.platform.model.{IntField, StrField}
import amf.shapes.client.scala.model.domain.grpc.{Reserved => InternalReserved}
import amf.shapes.internal.convert.ShapeClientConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Reserved(override private[amf] val _internal: InternalReserved) extends DomainElement {

  @JSExportTopLevel("Reserved")
  def this() = this(InternalReserved())

  def number: IntField = _internal.number
  def fieldName: StrField = _internal.fieldName
  def range: ReservedRange = _internal.range

  def withNumber(number: Int): this.type = {
    _internal.withNumber(number)
    this
  }

  def withFieldName(fieldName: String): this.type = {
    _internal.withFieldName(fieldName)
    this
  }
}
