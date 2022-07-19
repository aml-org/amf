package amf.shapes.client.platform.model.domain.federation

import amf.shapes.client.scala.model.domain.federation.{ExternalPropertyShape => InternalExternalPropertyShape}
import amf.shapes.internal.convert.ShapeClientConverters._
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.DomainElement

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ExternalPropertyShape(override private[amf] val _internal: InternalExternalPropertyShape)
    extends DomainElement {

  @JSExportTopLevel("ExternalPropertyShape")
  def this() = this(InternalExternalPropertyShape())

  def name: StrField                              = _internal.name
  def keyMappings: ClientList[PropertyKeyMapping] = _internal.keyMappings.asClient
  def rangeName: StrField                         = _internal.rangeName

  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withKeyMappings(keyMappings: ClientList[PropertyKeyMapping]): this.type = {
    _internal.withKeyMappings(keyMappings.asInternal)
    this
  }

  def withRangeName(rangeName: String): this.type = {
    _internal.withRangeName(rangeName)
    this
  }

}
