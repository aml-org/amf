package amf.model

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll
import scala.scalajs.js.JSConverters._

/**
  *
  */
@JSExportAll
case class PropertyDependencies(private[amf] val property: amf.shape.PropertyDependencies) extends DomainElement {

  def propertySource: String              = property.propertySource
  def propertyTarget: js.Iterable[String] = property.propertyTarget.toJSArray

  def withPropertySource(propertySource: String): this.type = {
    property.withPropertySource(propertySource)
    this
  }

  def withPropertyTarget(propertyTarget: js.Iterable[String]): this.type = {
    property.withPropertyTarget(propertyTarget.toSeq)
    this
  }

  override private[amf] def element: amf.shape.PropertyDependencies = property
}
