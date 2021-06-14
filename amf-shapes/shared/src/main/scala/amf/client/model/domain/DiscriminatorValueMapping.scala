package amf.client.model.domain

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.plugins.domain.apicontract.models.{DiscriminatorValueMapping => InternalDiscriminatorValueMapping}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class DiscriminatorValueMapping(override private[amf] val _internal: InternalDiscriminatorValueMapping)
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
