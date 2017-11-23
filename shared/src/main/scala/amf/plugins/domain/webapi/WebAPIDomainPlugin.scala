package amf.plugins.domain.webapi

import amf.framework.plugins.AMFDomainPlugin
import amf.plugins.domain.webapi.models.annotations._

object WebAPIDomainPlugin extends AMFDomainPlugin {

  override val ID = "WebAPI Domain"

  override def dependencies() = Seq()

  override def serializableAnnotations() = Map(
    "type-exprssion" -> ParsedFromTypeExpression,
    "parent-end-point" -> ParentEndPoint,
    "parsed-json-schema" -> ParsedJSONSchema,
    "source-vendor" -> SourceVendor,
    "declared-element" -> DeclaredElement,
    "synthesized-field" -> SynthesizedField,
    "single-value-array" -> SingleValueArray,
    "aliases-array" -> Aliases
  )
}
