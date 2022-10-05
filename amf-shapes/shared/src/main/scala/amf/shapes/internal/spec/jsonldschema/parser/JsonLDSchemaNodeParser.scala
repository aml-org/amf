package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.internal.parser.domain.Annotations
import amf.core.client.scala.model.domain.Shape
import amf.shapes.internal.spec.jsonldschema.parser.builder.{JsonLDElementBuilder, JsonLDErrorBuilder}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.{
  UnsupportedRootLevel,
  UnsupportedScalarRootLevel
}
import org.yaml.model._

case class JsonLDSchemaNodeParser(shape: Shape, node: YNode, key: String, isRoot: Boolean = false)(implicit
    ctx: JsonLDParserContext
) {

  // TODO native-jsonld: key is only used to generate default class term for objects. Shall we analyse another way?
  def parse(): JsonLDElementBuilder = {
    node.tagType match {
      case YType.Map => JsonLDObjectElementParser(node.as[YMap], key)(ctx).parse(shape)
      case YType.Seq => JsonLDArrayElementParser(node.as[YSequence])(ctx).parse(shape)
      case _ if isScalarNode && !isRoot =>
        JsonLDScalarElementParser(node.as[YScalar], node.tagType).parse(shape)
      case _ if isScalarNode && isRoot =>
        ctx.eh.violation(UnsupportedScalarRootLevel, shape, UnsupportedScalarRootLevel.message, Annotations(node))
        JsonLDErrorBuilder()
      case _ =>
        ctx.eh.violation(UnsupportedRootLevel, shape, UnsupportedRootLevel.message, Annotations(node))
        JsonLDErrorBuilder()
    }
  }

  private def isScalarNode = {
    node.value.isInstanceOf[YScalar]
  }
}
