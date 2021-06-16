package amf.shapes.client.platform.model.domain

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.PropertyDependencies
import amf.shapes.internal.convert.ShapeClientConverters.ClientList

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.shapes.internal.convert.ShapeClientConverters._

/**
  * Property dependencies model class
  */
@JSExportAll
case class PropertyDependencies(override private[amf] val _internal: PropertyDependencies) extends DomainElement {

  @JSExportTopLevel("model.domain.PropertyDependencies")
  def this() = this(InternalPropertyDependencies())

  def source: StrField             = _internal.propertySource
  def target: ClientList[StrField] = _internal.propertyTarget.asClient

  def withPropertySource(propertySource: String): this.type = {
    _internal.withPropertySource(propertySource)
    this
  }

  def withPropertyTarget(propertyTarget: ClientList[String]): this.type = {
    _internal.withPropertyTarget(propertyTarget.asInternal)
    this
  }
}
