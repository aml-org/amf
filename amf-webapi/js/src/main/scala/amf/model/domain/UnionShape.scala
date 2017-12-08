package amf.model.domain

import amf.plugins.domain.shapes.models

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

@JSExportAll
@JSExportTopLevel("model.domain.UnionShape")
case class UnionShape(private[amf] val schema: models.UnionShape) extends AnyShape(schema) {

  def anyOf: js.Iterable[AnyShape] = Option(schema.anyOf).getOrElse(Seq()).map { s => platform.wrap[AnyShape](s) }.toJSIterable
  def withAnyOf(anyOf: js.Iterable[AnyShape]) = {
    schema.withAnyOf(anyOf.toSeq.map(_.element))
    this
  }
}
