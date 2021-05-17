package amf.plugins.domain.shapes.annotations.serializable

import amf.core.annotations.serializable.SerializableAnnotations
import amf.core.annotations.{InheritanceProvenance, InheritedShapes, NilUnion}
import amf.core.model.domain.AnnotationGraphLoader
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression

private[amf] object ShapeSerializableAnnotations extends SerializableAnnotations {

  override val annotations: Map[String, AnnotationGraphLoader] = Map(
    "type-expression"        -> ParsedFromTypeExpression,
    "inheritance-provenance" -> InheritanceProvenance,
    "inherited-shapes"       -> InheritedShapes,
    "nil-union"              -> NilUnion
  )

}
