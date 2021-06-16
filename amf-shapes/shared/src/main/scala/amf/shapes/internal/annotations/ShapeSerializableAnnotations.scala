package amf.shapes.internal.annotations

import amf.core.client.scala.model.domain.AnnotationGraphLoader
import amf.core.internal.annotations.{InheritanceProvenance, InheritedShapes, NilUnion}
import amf.core.internal.annotations.serializable.SerializableAnnotations

private[amf] object ShapeSerializableAnnotations extends SerializableAnnotations {

  override val annotations: Map[String, AnnotationGraphLoader] = Map(
    "type-expression"        -> ParsedFromTypeExpression,
    "inheritance-provenance" -> InheritanceProvenance,
    "inherited-shapes"       -> InheritedShapes,
    "nil-union"              -> NilUnion
  )

}
