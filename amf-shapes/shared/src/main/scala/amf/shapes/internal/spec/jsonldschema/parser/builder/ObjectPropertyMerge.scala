package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.shapes.internal.spec.jsonldschema.parser.JsonLDParserContext
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.IncompatibleNodes

object ObjectPropertyMerge {

  def mergeProperties(current: JsonLDPropertyBuilder, other: JsonLDPropertyBuilder)(implicit
      ctx: JsonLDParserContext
  ) = {
    mergeElements(current.element, other.element)
  }

  private def mergeElements(current: JsonLDElementBuilder, other: JsonLDElementBuilder)(implicit
      ctx: JsonLDParserContext
  ) = {
    (current, other) match {
      case (currObj: JsonLDObjectElementBuilder, otherObj: JsonLDObjectElementBuilder) => currObj.merge(otherObj)
      case (currArr: JsonLDArrayElementBuilder, otherArr: JsonLDArrayElementBuilder)   => currArr.merge(otherArr)
      case (currScalar: JsonLDScalarElementBuilder, otherScalar: JsonLDScalarElementBuilder) =>
        currScalar.merge(otherScalar)
      case (_: JsonLDErrorBuilder, _: JsonLDErrorBuilder) =>
        ctx.violation(IncompatibleNodes, "", IncompatibleNodes.message, current.location)
        other
      case (_: JsonLDErrorBuilder, _) => other
      case (_, _: JsonLDErrorBuilder) => current
      case _ =>
        ctx.violation(IncompatibleNodes, "", IncompatibleNodes.message, current.location)
        other
    }
  }
}
