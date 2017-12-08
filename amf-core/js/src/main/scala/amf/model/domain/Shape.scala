package amf.model.domain

import amf.core.model.domain
import amf.core.unsafe.PlatformSecrets

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}


@JSExportAll
@JSExportTopLevel("model.domain.Shape")
class Shape(private[amf] val shape: domain.Shape) extends DomainElement with Linkable {

  def name: String                    = shape.name
  def displayName: String             = shape.displayName
  def description: String             = shape.description
  def default: String                 = shape.default
  def values: js.Iterable[String]     = Option(shape.values).getOrElse(Seq()).toJSArray
  def inherits: js.Iterable[Shape]    = Option(shape.inherits).getOrElse(Seq()).map(Shape(_)).toJSArray

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
  def withDefault(default: String): this.type = {
    shape.withDefault(default)
    this
  }
  def withValues(values: js.Iterable[String]): this.type = {
    shape.withValues(values.toList)
    this
  }

  def withInherits(inherits: js.Iterable[Shape]): this.type = {
    shape.withInherits(inherits.toList.map(_.shape))
    this
  }

  override def linkTarget: Option[DomainElement with Linkable] = throw new Exception("Shape is abstract")

  override def linkCopy(): DomainElement with Linkable = throw new Exception("Shape is abstract")
}

object Shape extends PlatformSecrets {
  def apply(shape: domain.Shape): Shape = platform.wrap[Shape](shape)
}