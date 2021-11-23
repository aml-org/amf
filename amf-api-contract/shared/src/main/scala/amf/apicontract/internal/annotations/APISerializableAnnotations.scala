package amf.apicontract.internal.annotations

import amf.core.client.scala.model.domain.AnnotationGraphLoader
import amf.core.internal.annotations.serializable.SerializableAnnotations
import amf.shapes.internal.annotations.{OrphanOasExtension, TypePropertyLexicalInfo}

private[amf] object APISerializableAnnotations extends SerializableAnnotations {

  override val annotations: Map[String, AnnotationGraphLoader] = Map(
    "parent-end-point"                       -> ParentEndPoint,
    "orphan-oas-extension"                   -> OrphanOasExtension,
    "type-property-lexical-info"             -> TypePropertyLexicalInfo,
    "parameter-binding-in-body-lexical-info" -> ParameterBindingInBodyLexicalInfo,
    "invalid-binding"                        -> InvalidBinding,
    "extends-reference"                      -> ExtendsReference
  )

}
