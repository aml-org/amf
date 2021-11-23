package amf.shapes.internal.annotations

import amf.core.client.scala.model.domain.AnnotationGraphLoader
import amf.core.internal.annotations.serializable.SerializableAnnotations
import amf.core.internal.annotations.{InferredProperty, InheritanceProvenance, InheritedShapes, NilUnion}

private[amf] object ShapeSerializableAnnotations extends SerializableAnnotations {

  override val annotations: Map[String, AnnotationGraphLoader] = Map(
    "type-expression"        -> ParsedFromTypeExpression,
    "inheritance-provenance" -> InheritanceProvenance,
    "inherited-shapes"       -> InheritedShapes,
    "nil-union"              -> NilUnion,
    "inferred-property"      -> InferredProperty
  )
}
