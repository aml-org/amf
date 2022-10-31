package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.scala.model.domain.Shape
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, MatrixShape, SemanticContext, TupleShape}
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.jsonldschema.parser.builder.{JsonLDArrayElementBuilder, JsonLDElementBuilder}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.UnsupportedShape
import org.yaml.model.{YSequence, YType}

case class JsonLDArrayElementParser(seq: YSequence, path: JsonPath)(implicit val ctx: JsonLDParserContext)
    extends JsonLDBaseElementParser[JsonLDArrayElementBuilder](seq)(ctx) {
  override def foldLeft(
      current: JsonLDArrayElementBuilder,
      other: JsonLDArrayElementBuilder
  ): JsonLDArrayElementBuilder = {
    current.merge(other)
  }

  override def unsupported(s: Shape): JsonLDArrayElementBuilder = {
    ctx.violation(UnsupportedShape, s.id, "Invalid shape class for array node")
    parseItems(AnyShape())
  }

  override def parseNode(shape: Shape): JsonLDArrayElementBuilder = {

    shape match {
      case a: ArrayShape => parseItems(a.items)

      // case t:TupleShape =>
//      case m:MatrixShape if seq.nodes.headOption.exists(_.tagType == YType.Seq) =>
//        seq.nodes.collect({ s => s.as}).map(n => JsonLDArrayElementBuilder)
      case a: AnyShape if a.meta.`type`.headOption.exists(_.iri() == AnyShapeModel.`type`.head.iri()) =>
        parseItems(a)
      case _ => unsupported(shape)
    }
  }

  def parseItems(items: Shape): JsonLDArrayElementBuilder = {
    val builder = new JsonLDArrayElementBuilder(seq.location, path)

    builder.withItems(seq.nodes.zipWithIndex.map({ case (node, index) =>
      JsonLDSchemaNodeParser(items, node, index.toString, path.concat(index.toString)).parse()
    }))
  }
}
