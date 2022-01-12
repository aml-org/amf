package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.{NodeMapping, UnionNodeMapping}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.SemanticJsonSchemaValidations
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.SemanticJsonSchemaValidations.UnknownTransformedMappingType

import scala.collection.mutable

case class TransformationResult(encoded: Either[NodeMapping, UnionNodeMapping],
                                declared: Seq[DomainElement],
                                externals: mutable.Map[String, String])

case class SchemaTransformer(shape: AnyShape)(implicit errorHandler: AMFErrorHandler) {

  def transform(): TransformationResult = {
    val ctx         = ShapeTransformationContext()
    val transformed = ShapeTransformation(shape, ctx).transform()
    val declared    = ctx.transformed()
    transformed match {
      case nm: NodeMapping       => TransformationResult(Left(nm), declared, ctx.externals)
      case unm: UnionNodeMapping => TransformationResult(Right(unm), declared, ctx.externals)
//      case _ => errorHandler.violation(UnknownTransformedMappingType, transformed.id, s"Unknown transformed shape ${transformed}")
      case _ => throw new Exception(s"Unknown transformed shape ${transformed}")
    }
  }
}
