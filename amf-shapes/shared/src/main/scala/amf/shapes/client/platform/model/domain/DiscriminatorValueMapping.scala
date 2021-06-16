package amf.shapes.client.platform.model.domain

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, Shape}
import amf.shapes.client.scala.model.domain.DiscriminatorValueMapping

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.shapes.internal.convert.ShapeClientConverters._

@JSExportAll
case class DiscriminatorValueMapping(override private[amf] val _internal: DiscriminatorValueMapping)
    extends DomainElement {

  @JSExportTopLevel("model.domain.DiscriminatorValueMapping")
  def this() = this(InternalDiscriminatorValueMapping())

  def value: StrField    = _internal.value
  def targetShape: Shape = _internal.targetShape

  def withValue(value: String): this.type = {
    _internal.withValue(value)
    this
  }

  def withTargetShape(shape: Shape): this.type = {
    _internal.withTargetShape(shape)
    this
  }
}
