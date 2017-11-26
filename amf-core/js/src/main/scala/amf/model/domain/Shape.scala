package amf.model.domain

import amf.core.model.domain

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll


@JSExportAll
abstract class Shape(private[amf] val shape: domain.Shape) extends DomainElement with Linkable {

  val name: String                    = shape.name
  val displayName: String             = shape.displayName
  val description: String             = shape.description
  val default: String                 = shape.default
  val values: js.Iterable[String]     = shape.values.toJSArray
  val inherits: js.Iterable[Shape]    = shape.inherits.map(Shape(_)).toJSArray

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

}

object Shape {
  def apply(shape: domain.Shape): Shape = throw new Exception("Shape is abstract and cannot be built")
}