package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.metamodel.Type
import amf.core.internal.plugins.document.graph.JsonLdKeywords
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.domain.metamodel.jsonldschema.JsonLDElementModel
import amf.shapes.internal.spec.jsonldschema.parser.builder.{JsonLDElementBuilder, JsonLDPropertyBuilder}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.ContainerCheckErrorList
import org.yaml.model.{YMapEntry, YNode, YScalar}

import scala.util.matching.Regex

case class JsonLDPropertyParser(
    properties: Seq[PropertyShape],
    sCtx: Option[SemanticContext],
    path: JsonPath
)(implicit val ctx: JsonLDParserContext) {

  private val (patternProperties, literalProperties) = properties.partition(p => p.patternName.option().nonEmpty)
  private val semanticContext                        = sCtx.getOrElse(SemanticContext.default)

  def parse(entries: Seq[YMapEntry]): Seq[JsonLDPropertyBuilder] = entries.map(parseEntry)

  private def parseEntry(entry: YMapEntry): JsonLDPropertyBuilder = {
    parseWithLiteralProperties(entry).getOrElse(parseWithPatterProperties(entry).getOrElse(parseDynamic(entry)))
  }

  private def parseWithLiteralProperties(entry: YMapEntry): Option[JsonLDPropertyBuilder] =
    parseWithPropertyLike(literalProperties, entry, literalMatcher)

  private def parseWithPatterProperties(entry: YMapEntry): Option[JsonLDPropertyBuilder] =
    parseWithPropertyLike(patternProperties, entry, regexMatcher)

  private def parseDynamic(entry: YMapEntry): JsonLDPropertyBuilder = {
    val (element, term) = parseDynamicEntry(entry)
    generateBuilder(element, term, entry)
  }

  private def parseWithPropertyLike(
      properties: Seq[PropertyShape],
      entry: YMapEntry,
      matcher: (PropertyShape, String) => Boolean
  ): Option[JsonLDPropertyBuilder] = {
    val key = getKeyOrEmpty(entry)
    properties
      .find(matcher(_, key))
      .map(parseWithProperty(_, entry))
      .map(r => generateBuilder(r._1, r._2, entry))
  }

  private def generateBuilder(element: JsonLDElementBuilder, term: String, entry: YMapEntry) = {
    JsonLDPropertyBuilder(term, entry.key, None, element, element.path, entry.location)
  }

  private def literalMatcher(property: PropertyShape, entryKey: String): Boolean = {
    property.name.value() == entryKey
  }

  private def regexMatcher(patternProperty: PropertyShape, entryKey: String): Boolean = {
    val regex = new Regex(patternProperty.name.value())
    regex.pattern.matcher(entryKey).matches()
  }

  private def getKeyOrEmpty(e: YMapEntry): String = e.key.asScalar.map(_.text).getOrElse("")

  private def parseDynamicEntry(e: YMapEntry): (JsonLDElementBuilder, String) = {
    val entryKey = e.key.as[YScalar].text
    val nextPath = path.concat(entryKey)
    val term     = computeTerm(nextPath)
    val builder  = JsonLDSchemaNodeParser(buildEmptyAnyShape(semanticContext), e.value, entryKey, nextPath).parse()
    (builder, term)
  }

  private def parseWithProperty(
      p: PropertyShape,
      entry: YMapEntry
  ): (JsonLDElementBuilder, String) = {
    val propertyName                    = entry.key.as[String]
    val mapping: Option[ContextMapping] = findAssociatedContextMapping(propertyName)
    val term                            = findTerm(path.concat(propertyName), mapping)
    val containers                      = findContainers(mapping, p)
    val elementBuilder = JsonLDSchemaNodeParser(p.range, entry.value, p.path.value(), path.concat(propertyName)).parse()
    applyContainers(containers, elementBuilder)
    (elementBuilder, term)
  }

  private def findAssociatedContextMapping(name: String): Option[ContextMapping] =
    semanticContext.mapping.find(mapping => mapping.alias.option().contains(name))

  private def findTerm(path: JsonPath, mapping: Option[ContextMapping]): String =
    mapping
      .flatMap(_.iri.option().map(semanticContext.expand))
      .getOrElse(computeTerm(path))

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

  private def computeTerm(path: JsonPath): String = {
    propertyName(path)
      .flatMap(semanticContext.getTermFor)
      .map(semanticContext.expand)
      .getOrElse(semanticContext.computeBase(path.toString))
  }

  private def propertyName(path: JsonPath): Option[String] = path.lastSegment

  private def buildEmptyAnyShape(parentCtx: SemanticContext): AnyShape =
    AnyShape().withSemanticContext(parentCtx.cloneContext().withTypeMappings(Nil))

}
