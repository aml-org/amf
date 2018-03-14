package amf.model.domain

import amf.core.model.domain
import amf.core.model.domain.NamedDomainElement
import amf.core.unsafe.PlatformSecrets

import scala.collection.JavaConverters._

abstract class Shape(private[amf] val shape: domain.Shape)
    extends DomainElement
    with Linkable
    with NamedDomainElement {

  def name: String                    = shape.name
  def displayName: String             = shape.displayName
  def description: String             = shape.description
  def defaultValue: DataNode          = DataNode(shape.default)
  def values: java.util.List[String]  = Option(shape.values).getOrElse(Nil).asJava
  def inherits: java.util.List[Shape] = Option(shape.inherits).getOrElse(Nil).map(platform.wrap[Shape](_)).asJava

  def withName(name: String): this.type = {
    shape.withName(name)
    this
  }
  def withDisplayName(name: String): this.type = {
    shape.withDisplayName(name)
    this
  }
  def withDescription(description: String): this.type = {
    shape.withDescription(description)
    this
  }
  def withDefaultValue(default: DataNode): this.type = {
    shape.withDefault(default.dataNode)
    this
  }
  def withValues(values: java.util.List[String]): this.type = {
    shape.withValues(values.asScala)
    this
  }

  def withInherits(inherits: java.util.List[Shape]): this.type = {
    shape.withInherits(inherits.asScala.map(_.shape))
    this
  }

}

object Shape extends PlatformSecrets {
  def apply(shape: domain.Shape): Shape = platform.wrap[Shape](shape)
}
