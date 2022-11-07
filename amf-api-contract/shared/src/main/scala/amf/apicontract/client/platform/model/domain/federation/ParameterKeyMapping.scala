package amf.apicontract.client.platform.model.domain.federation

import amf.apicontract.client.platform.model.domain.Parameter
import amf.apicontract.client.scala.model.domain.federation.{ParameterKeyMapping => InternalParameterKeyMapping}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.domain.PropertyShapePath
import amf.shapes.client.platform.model.domain.federation.KeyMapping

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ParameterKeyMapping(override private[amf] val _internal: InternalParameterKeyMapping) extends KeyMapping {
  override def source: Parameter         = _internal.source
  override def target: PropertyShapePath = _internal.target

  override def withSource(source: Parameter): this.type = {
    _internal.withSource(source)
    this
  }

  override def withTarget(target: PropertyShapePath): this.type = {
    _internal.withTarget(target)
    this
  }

  @JSExportTopLevel("ParameterKeyMapping")
  def this() = this(InternalParameterKeyMapping())

  override type Source     = Parameter
  override type Target     = PropertyShapePath
  override type WithTarget = PropertyShapePath

}
