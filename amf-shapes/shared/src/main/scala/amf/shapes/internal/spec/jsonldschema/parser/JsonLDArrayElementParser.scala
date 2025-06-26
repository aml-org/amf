package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape}
import amf.shapes.internal.spec.jsonldschema.parser.builder.JsonLDArrayElementBuilder
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.UnsupportedShape
import org.yaml.model.YSequence

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
      case a: ArrayShape => parseItems(a.items, Some(a))

      // case t:TupleShape =>
//      case m:MatrixShape if seq.nodes.headOption.exists(_.tagType == YType.Seq) =>
//        seq.nodes.collect({ s => s.as}).map(n => JsonLDArrayElementBuilder)
      case a: AnyShape if a.isStrictAnyMeta => parseItems(a)
      case _                                => unsupported(shape)
    }
  }

  private def parseItems(items: Shape, defSchema: Option[Shape] = None): JsonLDArrayElementBuilder = {
    val annotation = Annotations(seq) ++= JsonLDBaseElementParser.schemaDefAnnotations(defSchema, ctx)
    val builder    = new JsonLDArrayElementBuilder(annotation, path)

    builder.withItems(seq.nodes.zipWithIndex.map({ case (node, index) =>
      JsonLDSchemaNodeParser(items, node, index.toString, path.concat(index.toString)).parse()
    }))
  }
}
