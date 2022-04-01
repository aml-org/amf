package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.internal.metamodel.domain.NodeMappableModel
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.AnyShape

case class TransformationResult(encoded: DomainElement,
                                declared: Seq[DomainElement],
                                externals: Map[String, String],
                                terms: Seq[CandidateProperty])

case class SchemaTransformerOptions(dialectName: String,
                                    dialectVersion: String,
                                    vocabId: String,
                                    vocabBase: String,
                                    vocabName: String,
                                    termPrefix: String)

object SchemaTransformerOptions {
  val DEFAULT: SchemaTransformerOptions = SchemaTransformerOptions("amf-json-schema-generated-dialect",
                                                                   "1.0",
                                                                   "semantic_vocabulary",
                                                                   "https://a.ml/semantic-json-schema#",
                                                                   "Semantic JSON Schema Vocabulary",
                                                                   "semantics")
}

case class SchemaTransformer(shape: AnyShape, options: SchemaTransformerOptions)(
    implicit errorHandler: AMFErrorHandler) {

  def transform[T <: NodeMappableModel](): TransformationResult = {
    val ctx         = ShapeTransformationContext(options)
    val transformed = ShapeTransformation(shape, ctx).transform()
    val declared    = ctx.transformed()
    val aliases     = ctx.externals.toMap
    TransformationResult(transformed, declared, aliases, ctx.termsToExtract.toSeq)
  }

}
