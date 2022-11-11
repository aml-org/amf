package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.metamodel.Type
import amf.core.internal.plugins.document.graph.JsonLdKeywords
import amf.shapes.client.scala.model.domain.SemanticContext.baseIri
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.domain.metamodel.jsonldschema.JsonLDElementModel
import amf.shapes.internal.spec.jsonldschema.parser
import amf.shapes.internal.spec.jsonldschema.parser.builder.{
  JsonLDElementBuilder,
  JsonLDObjectElementBuilder,
  JsonLDPropertyBuilder
}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.{
  ContainerCheckErrorList,
  UnsupportedShape
}
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar}

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
  ): JsonLDObjectElementBuilder = current.merge(other)(ctx)

  override def findClassTerm(ctx: SemanticContext): Seq[String] = {
    val terms = super.findClassTerm(ctx)
    if (terms.isEmpty) {
      val fragment = if (path.toString.isEmpty) key else path.toString
      Seq(computeDefaultTerm(ctx, fragment))
    } else terms
  }

  private def parseWithObject(n: NodeShape): JsonLDObjectElementBuilder = parseDynamic(n.properties, n.semanticContext)

  private def parseDynamic(
      p: Seq[PropertyShape],
      semanticContext: Option[SemanticContext]
  ): JsonLDObjectElementBuilder = {
    val builder = new JsonLDObjectElementBuilder(map.location, key, computeBase(semanticContext), path)
    setClassTerm(builder, semanticContext)
    map.entries.foreach { e =>
      val sc              = semanticContext.getOrElse(SemanticContext.default)
      val key             = getKeyOrEmpty(e)
      val (element, term) = p.find(_.name.value() == key).fold(parseEntry(e, sc))(parseWithProperty(_, e.value, sc))
      builder + JsonLDPropertyBuilder(term, e.key, None, element, element.path, e.location)
    }
    builder
  }

  private def setClassTerm(builder: JsonLDObjectElementBuilder, semantics: Option[SemanticContext]) =
    builder.classTerms ++= findClassTerm(semantics.getOrElse(SemanticContext.default))

  private def parseEntry(e: YMapEntry, semantics: SemanticContext): (JsonLDElementBuilder, String) = {
    val entryKey = e.key.as[YScalar].text
    val nextPath = path.concat(entryKey)
    val term     = computeTerm(semantics, nextPath)
    val builder  = JsonLDSchemaNodeParser(buildEmptyAnyShape(semantics), e.value, entryKey, nextPath).parse()
    (builder, term)
  }

  private def parseWithProperty(
      p: PropertyShape,
      node: YNode,
      semantics: SemanticContext
  ): (JsonLDElementBuilder, String) = {
    val propertyName                    = p.name.value()
    val mapping: Option[ContextMapping] = findAssociatedContextMapping(semantics, propertyName)
    val term                            = findTerm(semantics, path.concat(propertyName), mapping)
    val containers                      = findContainers(mapping, p)
    val elementBuilder = JsonLDSchemaNodeParser(p.range, node, p.path.value(), path.concat(propertyName)).parse()
    applyContainers(containers, elementBuilder)
    (elementBuilder, term)
  }

  private def findAssociatedContextMapping(ctx: SemanticContext, name: String): Option[ContextMapping] =
    ctx.mapping.find(mapping => mapping.alias.option().contains(name))

  private def findTerm(ctx: SemanticContext, path: JsonPath, mapping: Option[ContextMapping]): String =
    mapping
      .flatMap(_.iri.option().map(ctx.expand))
      .getOrElse(computeTerm(ctx, path))

  private def findContainers(maybeMapping: Option[ContextMapping], p: PropertyShape): Containers = {
    val base = Containers()
    maybeMapping.fold(base) { mapping =>
      val containers = mapping.containers.map(_.value())
      if (containers.contains(JsonLdKeywords.List)) {
        validateListContainer(p.range)
        base.copy(list = true)
      } else base
    }
  }

  private def applyContainers(c: Containers, builder: JsonLDElementBuilder): Unit = {
    if (c.list) {
      builder.withOverriddenType(Type.SortedArray(JsonLDElementModel))
    }
  }

  private def validateListContainer(range: Shape): Unit =
    if (!range.isInstanceOf[ArrayShape])
      ctx.eh.violation(ContainerCheckErrorList, range, ContainerCheckErrorList.message)

  private def getKeyOrEmpty(e: YMapEntry) = e.key.asScalar.map(_.text).getOrElse("")

  private def computeTerm(ctx: SemanticContext, path: JsonPath): String = {
    propertyName(path).flatMap(ctx.getTermFor).map(ctx.expand).getOrElse(computeDefaultTerm(ctx, path.toString))
  }
  private def propertyName(path: JsonPath) = path.lastSegment

  private def computeDefaultTerm(ctx: SemanticContext, fragment: String): String = computeBase(ctx) + fragment

  private def computeBase(ctx: Option[SemanticContext]): String = ctx.map(computeBase).getOrElse(baseIri)

  private def computeBase(ctx: SemanticContext): String = ctx.base.flatMap(_.iri.option()).getOrElse(baseIri)

}
