package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.internal.metamodel.domain.NodeMappableModel
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.AnyShape

import scala.collection.mutable

case class TransformationResult(encoded: DomainElement,
                                declared: Seq[DomainElement],
                                externals: mutable.Map[String, String])

case class SchemaTransformer(shape: AnyShape)(implicit errorHandler: AMFErrorHandler) {

  def transform[T <: NodeMappableModel](): TransformationResult = {
    val ctx         = ShapeTransformationContext()
    val transformed = ShapeTransformation(shape, ctx).transform()
    val declared    = ctx.transformed()
    TransformationResult(transformed, declared, ctx.externals)
  }
}
