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
      case n: NodeShape => parseWithObject(n)
      case anyShape: AnyShape if anyShape.meta.`type`.headOption.exists(_.iri() == AnyShapeModel.`type`.head.iri()) =>
        parseDynamic(Seq.empty, anyShape.semanticContext)
      case other => unsupported(other)
    }
  }

  override def unsupported(s: Shape): JsonLDObjectElementBuilder = {
    ctx.violation(UnsupportedShape, s.id, "Invalid shape class for map node")
    JsonLDObjectElementBuilder.empty(key, path)
  }

  def parseWithObject(n: NodeShape): JsonLDObjectElementBuilder = parseDynamic(n.properties, n.semanticContext)

  def parseDynamic(p: Seq[PropertyShape], semanticContext: Option[SemanticContext]): JsonLDObjectElementBuilder = {
    val builder = new JsonLDObjectElementBuilder(map.location, key, computeBase(semanticContext), path)
    setClassTerm(builder, semanticContext)
    map.entries.foreach { e =>
      val sc  = semanticContext.getOrElse(SemanticContext.default)
      val key = e.key.asScalar.map(_.text).getOrElse("")
      val (element, term) =
        p.find(_.name.value() == key)
          .fold(parseEntry(e, sc))(
            parseWithProperty(_, e.value, sc)
          )
      builder + JsonLDPropertyBuilder(term, e.key, None, element, element.path, e.location)
    }
    builder
  }

  private def computeTerm(ctx: SemanticContext, fragment: String): String = computeBase(ctx) + fragment
  private def computeTerm(ctx: SemanticContext, path: JsonPath): String   = computeTerm(ctx, path.toString)

  private def computeBase(ctx: Option[SemanticContext]): String = ctx.map(computeBase).getOrElse(baseIri)
  private def computeBase(ctx: SemanticContext): String         = ctx.base.flatMap(_.iri.option()).getOrElse(baseIri)

  private def setClassTerm(builder: JsonLDObjectElementBuilder, semantics: Option[SemanticContext]) =
    builder.classTerms ++= findClassTerm(semantics.getOrElse(SemanticContext.default))

  def parseEntry(e: YMapEntry, semantics: SemanticContext): (JsonLDElementBuilder, String) = {
    val entryKey = e.key.as[YScalar].text
    val nextPath = path.concat(entryKey)
    val term     = computeTerm(semantics, nextPath)
    (
      parser
        .JsonLDSchemaNodeParser(buildEmptyAnyShape(semantics), e.value, entryKey, nextPath)
        .parse(),
      term
    )
  }

  def parseWithProperty(p: PropertyShape, node: YNode, semantics: SemanticContext): (JsonLDElementBuilder, String) = {
    val propertyName                    = p.name.value()
    val mapping: Option[ContextMapping] = findAssociatedContextMapping(semantics, propertyName)
    val term                            = findTerm(semantics, path.concat(propertyName), mapping)
    val containers                      = findContainers(mapping, p)
    val elementBuilder = parser.JsonLDSchemaNodeParser(p.range, node, p.path.value(), path.concat(propertyName)).parse()
    applyContainers(containers, elementBuilder)
    (elementBuilder, term)
  }

  override def findClassTerm(ctx: SemanticContext): Seq[String] = {
    val terms = super.findClassTerm(ctx)
    if (terms.isEmpty) {
      val fragment = if (path.toString.isEmpty) key else path.toString
      Seq(computeTerm(ctx, fragment))
    } else terms
  }

  private def findAssociatedContextMapping(ctx: SemanticContext, name: String): Option[ContextMapping] =
    ctx.mapping.find(mapping => mapping.alias.option().contains(name))

  private def findTerm(ctx: SemanticContext, path: JsonPath, mapping: Option[ContextMapping]): String =
    mapping
      .flatMap(_.iri.option().map(ctx.expand))
      .getOrElse(computeTerm(ctx, path))

  private def findContainers(mapping: Option[ContextMapping], p: PropertyShape): Containers = {
    var c = Containers()
    mapping match {
      case Some(m) =>
        val containers = m.containers.map(_.value())
        if (containers.contains(JsonLdKeywords.List)) {
          validateListContainer(p.range)
          c = c.copy(list = true)
        }
      case None => // Nothing to do
    }
    c
  }

  private def applyContainers(c: Containers, builder: JsonLDElementBuilder): Unit = {
    if (c.list) {
      builder.withOverriddenType(Type.SortedArray(JsonLDElementModel))
    }
  }

  private def validateListContainer(range: Shape): Unit =
    if (!range.isInstanceOf[ArrayShape])
      ctx.eh.violation(ContainerCheckErrorList, range, ContainerCheckErrorList.message)

  override def foldLeft(
      current: JsonLDObjectElementBuilder,
      other: JsonLDObjectElementBuilder
  ): JsonLDObjectElementBuilder = current.merge(other)(ctx)
}
