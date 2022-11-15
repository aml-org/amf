package amf.shapes.client.platform.model.domain.federation
import amf.shapes.client.scala.model.domain.federation.{PropertyKeyMapping => InternalPropertyKeyMapping}
import amf.core.client.platform.model.domain.PropertyShape
import amf.core.client.platform.model.StrField
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class PropertyKeyMapping(override private[amf] val _internal: InternalPropertyKeyMapping) extends KeyMapping {

  override type Source     = PropertyShape
  override type Target     = StrField
  override type WithTarget = String

  @JSExportTopLevel("PropertyKeyMapping")
  def this() = this(InternalPropertyKeyMapping())

  override def source: PropertyShape = _internal.source

  override def target: StrField = _internal.target

  override def withSource(source: PropertyShape): PropertyKeyMapping.this.type = {
    _internal.withSource(source)
    this
  }

  override def withTarget(target: String): PropertyKeyMapping.this.type = {
    _internal.withTarget(target)
    this
  }
}
