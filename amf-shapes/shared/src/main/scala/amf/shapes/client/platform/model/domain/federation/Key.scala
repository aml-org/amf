package amf.shapes.client.platform.model.domain.federation

import amf.core.client.platform.model.BoolField
import amf.shapes.client.scala.model.domain.federation.{Key => InternalKey}
import amf.shapes.internal.convert.ShapeClientConverters.ClientList
import amf.core.client.platform.model.domain.{DomainElement, PropertyShapePath}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Key(override private[amf] val _internal: InternalKey) extends DomainElement {

  def components: ClientList[PropertyShapePath] = _internal.components.asClient
  def withComponents(components: ClientList[PropertyShapePath]): this.type = {
    _internal.withComponents(components.asInternal)
    this
  }

  def isResolvable: BoolField = _internal.isResolvable
  def withResolvable(isResolvable: Boolean): this.type = {
    _internal.withResolvable(isResolvable)
    this
  }

  @JSExportTopLevel("Key")
  def this() = this(InternalKey())
}
