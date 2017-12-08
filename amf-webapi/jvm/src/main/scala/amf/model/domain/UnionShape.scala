package amf.model.domain

import amf.plugins.domain.shapes.models

import scala.collection.JavaConverters._

case class UnionShape(private[amf] val schema: models.UnionShape) extends AnyShape(schema) {

  def anyOf: java.util.List[AnyShape] = Option(schema.anyOf).getOrElse(Seq()).map { s => platform.wrap[AnyShape](s) }.asJava

  def withAnyOf(anyOf: java.util.List[AnyShape]) = {
    schema.withAnyOf(anyOf.asScala.map(_.element))
    this
  }
}
