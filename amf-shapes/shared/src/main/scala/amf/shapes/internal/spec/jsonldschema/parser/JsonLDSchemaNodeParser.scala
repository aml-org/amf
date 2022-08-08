package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.scala.model.domain.Shape
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.spec.jsonldschema.parser.builder.JsonLDElementBuilder
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform.ShapeTransformationContext
import org.yaml.model._

case class JsonLDSchemaNodeParser(shape: Shape, node: YNode, key: String)(implicit ctx: JsonLDParserContext) {

  // TODO native-jsonld: key is only used to generate default class term for objects. Shall we analyse another way?
  def parse(): JsonLDElementBuilder = {
    node.tagType match {
      case YType.Map => JsonLDObjectElementParser(node.as[YMap], key)(ctx).parse(shape)
      case YType.Seq => JsonLDArrayElementParser(node.as[YSequence])(ctx).parse(shape)
      case _ if node.value.isInstanceOf[YScalar] =>
        JsonLDScalarElementParser(node.as[YScalar], node.tagType).parse(shape)
    }
  }
}
