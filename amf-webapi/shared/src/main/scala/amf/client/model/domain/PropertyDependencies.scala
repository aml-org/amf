package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField
import amf.plugins.domain.shapes.models.{PropertyDependencies => InternalPropertyDependencies}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Property dependencies model class
  */
@JSExportAll
case class PropertyDependencies(override private[amf] val _internal: InternalPropertyDependencies)
    extends DomainElement {

  @JSExportTopLevel("model.domain.PropertyDependencies")
  def this() = this(InternalPropertyDependencies())

  def source: StrField             = _internal.propertySource
  def target: ClientList[StrField] = _internal.propertyTarget.asClient
  def appliedShape: Shape          = _internal.propertyAppliedShape

  def withPropertySource(propertySource: String): this.type = {
    _internal.withPropertySource(propertySource)
    this
  }

  def withPropertyTarget(propertyTarget: ClientList[String]): this.type = {
    _internal.withPropertyTarget(propertyTarget.asInternal)
    this
  }

  def withPropertyAppliedShape(appliedShape: Shape): this.type = {
    _internal.withPropertyAppliedShape(appliedShape)
    this
  }
}
