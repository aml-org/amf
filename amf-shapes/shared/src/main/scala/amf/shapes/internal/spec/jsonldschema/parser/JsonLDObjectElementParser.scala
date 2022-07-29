package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.vocabulary.Namespace
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, SemanticContext}
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.jsonldschema.parser
import amf.shapes.internal.spec.jsonldschema.parser.builder.{
  JsonLDElementBuilder,
  JsonLDObjectElementBuilder,
  JsonLDPropertyBuilder
}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.{
  IncompatibleItemNodes,
  IncompatibleNodes,
  IncompatibleScalarDataType,
  UnsupportedShape
}
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform.ShapeTransformationContext
import org.mulesoft.common.client.lexical.SourceLocation
import org.yaml.model.{YMap, YMapEntry, YNode}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class JsonLDObjectElementParser(
    map: YMap
)(implicit val ctx: JsonLDParserContext)
    extends JsonLDBaseElementParser[JsonLDObjectElementBuilder](map)(ctx) {

  override def parseNode(s: Shape): JsonLDObjectElementBuilder = {
    // TODO native-jsonld: weird to update context here for any and at withObject method for nodeshapes.
    s match {
      case n: NodeShape => parseWithObject(n)
      case anyShape: AnyShape if anyShape.meta.`type`.headOption.exists(_.iri() == AnyShapeModel.`type`.head.iri()) =>
        parseDynamic(Seq.empty, anyShape.semanticContext)
      case other => unsupported(other)
    }
  }

  override def unsupported(s: Shape): JsonLDObjectElementBuilder = {
    ctx.violation(UnsupportedShape, s.id, "Invalid shape class for map node")
    JsonLDObjectElementBuilder.empty
  }

  // TODO native-jsonld: support additional dynamic properties
  def parseWithObject(n: NodeShape): JsonLDObjectElementBuilder = parseDynamic(n.properties, n.semanticContext)

  def parseDynamic(p: Seq[PropertyShape], semanticContext: Option[SemanticContext]): JsonLDObjectElementBuilder = {
    val builder = new JsonLDObjectElementBuilder(map.location)
    setClassTerm(builder, semanticContext)
    map.entries.foreach { e =>
      val (element, term) =
        p.find(_.path.value() == e.key.toString)
          .fold(parseEntry(e, semanticContext.getOrElse(SemanticContext.default)))(
            parseWithProperty(_, e.value, semanticContext)
          )
      builder + JsonLDPropertyBuilder(term, e.key.toString, None, element, e.location)
    }
    builder
  }

  def parseEntry(e: YMapEntry, semanticContext: SemanticContext): (JsonLDElementBuilder, String) = {
    val term = semanticContext.base.getOrElse(Namespace.Core.base) + e.key.toString
    (parser.JsonLDSchemaNodeParser(AnyShape(), e.value).parse(), term)
  }

  def parseWithProperty(p: PropertyShape, node: YNode, semantics: SemanticContext): (JsonLDElementBuilder, String) = {
    val term = findTerm(semantics, p.path.value())
    (parser.JsonLDSchemaNodeParser(p.range, node).parse(), term)
  }

  private def findTerm(ctx: SemanticContext, name: String): String = {
    val strings = ctx.mapping.flatMap { semanticMapping =>
      semanticMapping.alias
        .option()
        .filter(alias => alias == name)
        .flatMap { _ =>
          semanticMapping.iri.option()
        }
    }
    strings.headOption.getOrElse(ctx.base.getOrElse(Namespace.Core.base) + name)
  }

  override def foldLeft(
      current: JsonLDObjectElementBuilder,
      other: JsonLDObjectElementBuilder
  ): JsonLDObjectElementBuilder = current.merge(other)(ctx)
}
