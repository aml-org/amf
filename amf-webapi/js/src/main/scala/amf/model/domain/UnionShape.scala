package amf.model.domain

import amf.plugins.domain.shapes.models

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

@JSExportAll
case class UnionShape(private[amf] val schema: models.UnionShape) extends AnyShape(schema) {

  @JSExportTopLevel("model.domain.UnionShape")
  def this() = this(models.UnionShape())

  def anyOf: js.Iterable[AnyShape] =
    Option(schema.anyOf)
      .getOrElse(Seq())
      .map { s =>
        platform.wrap[AnyShape](s)
      }
      .toJSIterable
  def withAnyOf(anyOf: js.Iterable[AnyShape]) = {
    schema.withAnyOf(anyOf.toSeq.map(_.element))
    this
  }
}
