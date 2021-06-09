package amf.plugins.parser.dialect

import amf.core.model.domain.DomainElement
import amf.plugins.document.vocabularies.model.domain.{NodeMapping, UnionNodeMapping}
import amf.plugins.domain.shapes.models.AnyShape

case class TransformationResult(encoded: Either[NodeMapping, UnionNodeMapping], declared: Seq[DomainElement])

case class SchemaTransformer(shape: AnyShape) {

  def transform(): TransformationResult = {
    val ctx = ShapeTransformationContext()
    val transformed = ShapeTransformer(shape,ctx).transform()
    val declared = ctx.transformed()
    transformed match {
      case nm: NodeMapping       => TransformationResult(Left(nm), declared)
      case unm: UnionNodeMapping =>TransformationResult(Right(unm), declared)
      case _                     => throw new Exception(s"Unknown transformed shape ${transformed}")
    }
  }
}
