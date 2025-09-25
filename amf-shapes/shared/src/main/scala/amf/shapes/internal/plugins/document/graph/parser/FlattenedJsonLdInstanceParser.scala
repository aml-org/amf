package amf.shapes.internal.plugins.document.graph.parser

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.document.SourceMap
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.model.domain.context.EntityModel
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.{Field, ModelDefaultBuilder, Type}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.parser.{ParseConfiguration, YMapOps}
import amf.core.internal.plugins.document.graph.parser.{
  FlattenedGraphParser,
  FlattenedUnitGraphParser,
  GraphParserContext
}
import amf.core.internal.validation.CoreValidations.UnableToParseNode
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDArray, JsonLDElement, JsonLDObject, JsonLDScalar}
import amf.shapes.internal.domain.metamodel.jsonldschema.{JsonLDElementModel, JsonLDEntityModel}
import amf.shapes.internal.spec.jsonldschema.parser.JsonPath
import org.yaml.model._

class FlattenedJsonLdInstanceParser(startingPoint: String, overrideAliases: Map[String, String] = Map.empty)(implicit
    ctx: GraphParserContext
) extends FlattenedGraphParser(startingPoint, overrideAliases) {

  // This is to avoid register an error when a `findType` miss. I want to process the registered entities in the regular way (e.g.: Document entities) but I will catch the non-existing ones here
  override protected val shouldRegisterMissingTypes: Boolean = false

  override protected def retrieveType(id: String, map: YMap): Option[ModelDefaultBuilder] = {
    super.retrieveType(id, map).orElse(Some(JsonLDElementModel))
  }

  // I don't load new nodes to parent `nodes` variable because it is used only for very specific annotations not used here
  // I don't save processed nodes in the parent `cache` variable because in this kind of document there are no recursions
  override def parseNode(map: YMap, id: String, model: ModelDefaultBuilder): Option[AmfObject] = model match {
    case _: JsonLDElementModel => processJsonLdObject(map, Some(id))
    case _                     => super.parseNode(map, id, model)
  }

  private def processJsonLdObject(map: YMap, id: Option[String] = None): Option[JsonLDObject] = {

    val sources: SourceMap    = retrieveSources(map)
    val transformedId: String = id.orElse(retrieveId(map, ctx)).map(transformIdFromContext).getOrElse("")
    val ann: Annotations      = annotations(nodes, sources, transformedId)
    val typeIris: Seq[String] = getTypeIris(transformedId, map)

    getContextEntity(typeIris.head) match {
      case Some(contextEntity) =>
        val fieldEntries = traverseJsonLdObjectProperties(map, contextEntity)
        val jsonPath     = JsonPath()
        val entityModel  = generateEntityModel(typeIris, fieldEntries, jsonPath)
        Some(JsonLDObject(fieldEntries, ann, entityModel, jsonPath).withId(transformedId))
      case None =>
        ctx.eh.violation(
          UnableToParseNode,
          transformedId,
          s"Error parsing JSON-LD node, cannot find ContextEntity for @types $typeIris",
          map.location
        )
        None
    }

  }

  private def traverseJsonLdObjectProperties(map: YMap, contextEntity: EntityModel): Fields = {
    val fields = Fields()
    contextEntity.properties.foreach { property =>
      val propertyIri   = property._1
      val propertyValue = property._2

      map
        .key(propertyIri.iri())
        .orElse(map.key(compactUriFromContext(propertyIri.iri())))  // iri from JSON-LD could be compacted
        .flatMap { entry =>
          traverseJsonLdObjectProperty(entry, propertyIri, propertyValue)
        }
        .foreach { case (field, value) =>
          fields.setWithoutId(field, value)
        }
    }
    fields
  }

  private def traverseJsonLdObjectProperty(
      entry: YMapEntry,
      propertyIri: ValueType,
      propertyValueIri: ValueType
  ): Option[(Field, JsonLDElement)] = {
    traverseJsonLdElement(entry.value, propertyValueIri.iri()).map { element =>
      val field = generateField(element, propertyIri, propertyValueIri)
      (field, element)
    }
  }

  private def traverseJsonLdElement(node: YNode, iri: String = ""): Option[JsonLDElement] = {

    node.tagType match {
      case YType.Map =>
        val map = node.as[YMap]
        // All this cases should be JSON-LD links. If not, it is something wrong
        if (isReferenceNode(map)) {
          parseReferenceNode(map).flatMap(r => processJsonLdObject(r))
        } else {
          unexpectedError(iri, node)
          None
        }
      case YType.Seq =>
        processJsonLdArray(node, iri)
      case _ if isJsonLdScalar(iri) =>
        processJsonLdScalar(node, iri)
      case _ =>
        unexpectedError(iri, node)
        None
    }
  }

  private def processJsonLdScalar(scalar: YNode, iri: String): Option[JsonLDScalar] = {
    scalar.asScalar.map(s => new JsonLDScalar(s.value, iri))
  }

  private def processJsonLdArray(seq: YNode, iri: String): Option[JsonLDArray] = {
    val array = new JsonLDArray(Annotations())
    seq.as[YSequence].nodes.foreach { entry =>
      traverseJsonLdElement(entry, iri).foreach(array.+=)
    }
    Some(array)
  }

  private def generateEntityModel(iris: Seq[String], fields: Fields, jsonPath: JsonPath): JsonLDEntityModel = {
    val terms = iris.map(iri => ValueType(iri)).toList
    JsonLDEntityModel(terms, fields.fieldsMeta(), jsonPath)
  }

  private def generateField(element: JsonLDElement, iri: ValueType, valueIri: ValueType): Field = {
    val fieldType = element match {
      case _: JsonLDScalar => Type.Scalar(valueIri.name)
      case array: JsonLDArray =>
        val memberType: Type = array.jsonLDElements.headOption match {
          case Some(element) =>
            element match {
              case scalar: JsonLDScalar => Type.Scalar(ValueType(scalar.dataType).name)
              case obj: JsonLDObject    => obj.getModel
              case _                    => Type.Any // This case should not be reachable
            }
          case None => Type.Any // This case should not be reachable
        }
        Type.Array(memberType)
      case obj: JsonLDObject => obj.getModel
    }
    Field(fieldType, iri)
  }

  private def getContextEntity(iri: String): Option[EntityModel] = ctx.graphContext.findContextEntity(iri)

  private def getTypeIris(id: String, map: YMap): Seq[String] = ts(map, id).map(expandUriFromContext)

  private def isJsonLdScalar(iri: String): Boolean = DataType.isKnownScalarDataType(iri)

  private def parseReferenceNode(node: YMap): Option[YMap] = retrieveId(node, ctx).flatMap(nodeFromId)

  private def unexpectedError(id: String, part: YPart): Unit = ctx.eh.violation(
    UnableToParseNode,
    id,
    s"Unexpected error parsing JSON-LD node",
    part.location
  )
}

class FlattenedJsonLdInstanceUnitGraphParser(overrideAliases: Map[String, String] = Map.empty)(implicit
    ctx: GraphParserContext
) extends FlattenedUnitGraphParser(overrideAliases) {

  override def parserProvider(
      rootId: String,
      overrideAliases: Map[String, String],
      ctx: GraphParserContext
  ): FlattenedGraphParser = new FlattenedJsonLdInstanceParser(rootId, overrideAliases)(ctx)

}

object FlattenedJsonLdInstanceUnitGraphParser {

  def apply(config: ParseConfiguration, aliases: Map[String, String]): FlattenedJsonLdInstanceUnitGraphParser =
    new FlattenedJsonLdInstanceUnitGraphParser(aliases)(new GraphParserContext(config = config))

  def canParse(document: SyamlParsedDocument, aliases: Map[String, String] = Map.empty): Boolean =
    FlattenedUnitGraphParser.canParse(document, aliases)

}
