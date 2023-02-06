package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.shapes.client.scala.model.domain.SemanticContext.baseIri
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.spec.jsonldschema.parser.builder.JsonLDObjectElementBuilder
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.UnsupportedShape
import org.yaml.model.YMap

case class JsonLDObjectElementParser(
    map: YMap,
    key: String,
    path: JsonPath
)(implicit val ctx: JsonLDParserContext)
    extends JsonLDBaseElementParser[JsonLDObjectElementBuilder](map)(ctx) {

  override def parseNode(s: Shape): JsonLDObjectElementBuilder = {
    s match {
      case n: NodeShape                                   => parseWithObject(n)
      case anyShape: AnyShape if anyShape.isStrictAnyMeta => parseDynamic(Seq.empty, anyShape.semanticContext)
      case other                                          => unsupported(other)
    }
  }

  override def unsupported(s: Shape): JsonLDObjectElementBuilder = {
    ctx.violation(UnsupportedShape, s.id, "Invalid shape class for map node")
    JsonLDObjectElementBuilder.empty(key, path)
  }

  override def foldLeft(
      current: JsonLDObjectElementBuilder,
      other: JsonLDObjectElementBuilder
  ): JsonLDObjectElementBuilder = {
    val result = current.merge(other)(ctx)
    result
  }

  override def findClassTerm(ctx: SemanticContext): Seq[String] = {
    val terms = super.findClassTerm(ctx).map(ctx.expand)
    if (terms.isEmpty) {
      val fragment = if (path.toString.isEmpty) key else path.toString
      Seq(ctx.expand(ctx.computeBase(fragment)))
    } else terms
  }

  private def parseWithObject(n: NodeShape): JsonLDObjectElementBuilder = parseDynamic(n.properties, n.semanticContext)

  private def parseDynamic(
      properties: Seq[PropertyShape],
      semanticContext: Option[SemanticContext]
  ): JsonLDObjectElementBuilder = {

    val propertyParser   = JsonLDPropertyParser(properties, semanticContext, path)
    val propertyBuilders = propertyParser.parse(map.entries)

    val objectBuilder =
      new JsonLDObjectElementBuilder(map.location, key, semanticContext.map(_.computeBase).getOrElse(baseIri), path)
    setClassTerm(objectBuilder, semanticContext)

    objectBuilder ++ propertyBuilders

  }

  private def setClassTerm(builder: JsonLDObjectElementBuilder, semantics: Option[SemanticContext]) =
    builder.classTerms ++= findClassTerm(semantics.getOrElse(SemanticContext.default))

}
