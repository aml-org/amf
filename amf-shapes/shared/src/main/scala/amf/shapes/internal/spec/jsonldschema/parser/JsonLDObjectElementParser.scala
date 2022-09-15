package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.vocabulary.Namespace
import amf.core.client.scala.vocabulary.Namespace.Core
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
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class JsonLDObjectElementParser(
    map: YMap,
    key: String
)(implicit val ctx: JsonLDParserContext)
    extends JsonLDBaseElementParser[JsonLDObjectElementBuilder](map)(ctx) {

  override def parseNode(s: Shape): JsonLDObjectElementBuilder = {
    s match {
      case n: NodeShape => parseWithObject(n)
      case anyShape: AnyShape if anyShape.meta.`type`.headOption.exists(_.iri() == AnyShapeModel.`type`.head.iri()) =>
        parseDynamic(Seq.empty, anyShape.semanticContext)
      case other => unsupported(other)
    }
  }

  override def unsupported(s: Shape): JsonLDObjectElementBuilder = {
    ctx.violation(UnsupportedShape, s.id, "Invalid shape class for map node")
    JsonLDObjectElementBuilder.empty(key)
  }

  def parseWithObject(n: NodeShape): JsonLDObjectElementBuilder = parseDynamic(n.properties, n.semanticContext)

  def parseDynamic(p: Seq[PropertyShape], semanticContext: Option[SemanticContext]): JsonLDObjectElementBuilder = {
    val builder = new JsonLDObjectElementBuilder(map.location, key)
    setClassTerm(builder, semanticContext)
    map.entries.foreach { e =>
      val sc  = semanticContext.getOrElse(SemanticContext.default)
      val key = e.key.asScalar.map(_.text).getOrElse("")
      val (element, term) =
        p.find(_.name.value() == key)
          .fold(parseEntry(e, sc))(
            parseWithProperty(_, e.value, sc)
          )
      builder + JsonLDPropertyBuilder(term, e.key.toString, None, element, e.location)
    }
    builder
  }

  private def setClassTerm(builder: JsonLDObjectElementBuilder, semantics: Option[SemanticContext]) =
    builder.classTerms ++= findClassTerm(semantics.getOrElse(SemanticContext.default))

  def parseEntry(e: YMapEntry, semanticContext: SemanticContext): (JsonLDElementBuilder, String) = {
    val entryKey = e.key.as[YScalar].text
    val term     = semanticContext.base.map(_.iri.value()).getOrElse(Namespace.Core.base) + entryKey
    (parser.JsonLDSchemaNodeParser(buildEmptyAnyShape(semanticContext), e.value, entryKey).parse(), term)
  }

  def parseWithProperty(p: PropertyShape, node: YNode, semantics: SemanticContext): (JsonLDElementBuilder, String) = {
    val term = findTerm(semantics, p.name.value())
    (parser.JsonLDSchemaNodeParser(p.range, node, p.path.value()).parse(), term)
  }

  override def findClassTerm(ctx: SemanticContext): Seq[String] = {
    val terms = super.findClassTerm(ctx)
    if (terms.isEmpty) {
      Seq(ctx.base.map(_.iri.value()).getOrElse(Core.base) + key)
    } else terms
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
    strings.headOption.map(ctx.expand).getOrElse(ctx.base.map(_.iri.value()).getOrElse(Namespace.Core.base) + name)
  }

  override def foldLeft(
      current: JsonLDObjectElementBuilder,
      other: JsonLDObjectElementBuilder
  ): JsonLDObjectElementBuilder = current.merge(other)(ctx)
}
