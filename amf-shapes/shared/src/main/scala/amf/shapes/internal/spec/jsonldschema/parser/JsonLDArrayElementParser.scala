package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.scala.model.domain.Shape
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, MatrixShape, SemanticContext, TupleShape}
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.jsonldschema.parser.builder.JsonLDArrayElementBuilder
import org.yaml.model.{YSequence, YType}

case class JsonLDArrayElementParser(seq: YSequence)(implicit val ctx: JsonLDParserContext)
    extends JsonLDBaseElementParser[JsonLDArrayElementBuilder](seq)(ctx) {
  override def foldLeft(
      current: JsonLDArrayElementBuilder,
      other: JsonLDArrayElementBuilder
  ): JsonLDArrayElementBuilder = {
    current.merge(other)
  }

  override def unsupported(s: Shape): JsonLDArrayElementBuilder = JsonLDArrayElementBuilder.empty()

  override def parseNode(shape: Shape): JsonLDArrayElementBuilder = {
    val builder = new JsonLDArrayElementBuilder(seq.location)

    shape match {
      case a: ArrayShape =>
        setClassTerm(builder, a.semanticContext)
        builder.withItems(seq.nodes.map(new JsonLDSchemaNodeParser(a.items, _).parse()))
      // TODO native-jsonld: support matrix and tuple parsing
      // case t:TupleShape =>
//      case m:MatrixShape if seq.nodes.headOption.exists(_.tagType == YType.Seq) =>
//        seq.nodes.collect({ s => s.as}).map(n => JsonLDArrayElementBuilder)
      case a: AnyShape if a.meta.`type`.headOption.exists(_.iri() == AnyShapeModel.`type`.head.iri()) =>
        setClassTerm(builder, a.semanticContext)
        builder.withItems(seq.nodes.map(new JsonLDSchemaNodeParser(AnyShape(), _).parse()))
      case _ => unsupported(shape)
    }
  }
}
