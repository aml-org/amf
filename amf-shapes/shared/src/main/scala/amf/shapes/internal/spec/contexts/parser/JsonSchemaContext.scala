package amf.shapes.internal.spec.contexts.parser

import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.{Annotations, Declarations, Fields, FragmentRef, FutureDeclarations, SearchScope}
import amf.core.internal.remote.{JsonSchemaDialect, Vendor}
import amf.core.internal.validation.CoreValidations.DeclarationNotFound
import amf.core.internal.validation.core.ValidationSpecification
import amf.shapes.client.scala.model.domain.{AnyShape, CreativeWork, Example}
import amf.shapes.internal.spec.RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.spec.{RamlExternalSchemaExpressionFactory, ShapeParserContext}
import amf.shapes.internal.spec.common.{JSONSchemaDraft4SchemaVersion, JSONSchemaVersion, SchemaVersion}
import amf.shapes.internal.spec.common.parser.{SpecSyntax, YMapEntryLike}
import amf.shapes.internal.spec.contexts.JsonSchemaRefGuide
import amf.shapes.internal.spec.jsonschema.ref.{AstIndex, AstIndexBuilder, JsonSchemaInference}
import amf.shapes.internal.spec.raml.parser.{DefaultType, RamlTypeParser, TypeInfo}
import org.yaml.model._
import amf.core.internal.parser.YMapOps
import amf.core.internal.utils.AliasCounter

import scala.collection.mutable

object JsonSchemaSyntax extends SpecSyntax {
  override  val nodes: Map[String,Set[String]] = Map(
    "schema" -> Set(
      "$ref",
      "$schema",
      "format",
      "title",
      "description",
      "maximum",
      "exclusiveMaximum",
      "minimum",
      "exclusiveMinimum",
      "maxLength",
      "minLength",
      "nullable",
      "pattern",
      "maxItems",
      "minItems",
      "uniqueItems",
      "maxProperties",
      "minProperties",
      "required",
      "enum",
      "type",
      "items",
      "additionalItems",
      "collectionFormat",
      "allOf",
      "properties",
      "additionalProperties",
      "discriminator",
      "readOnly",
      "writeOnly",
      "xml",
      "deprecated",
      "externalDocs",
      "allOf",
      "anyOf",
      "oneOf",
      "not",
      "dependencies",
      "multipleOf",
      "default",
      "example",
      "id",
      "name",
      "patternProperties"
    )
  )
}

trait JsonSchemaLikeContext  extends JsonSchemaInference { this: ShapeParserContext =>
  var jsonSchemaIndex: Option[AstIndex]
  var globalSpace: mutable.Map[String, Any]
  var localJSONSchemaContext: Option[YNode]
  var indexCache: mutable.Map[String, AstIndex]
  var jsonSchemaRefGuide: JsonSchemaRefGuide = JsonSchemaRefGuide(loc, refs)(this)


  def findJsonPathIn(index: AstIndex, path: String): Option[YMapEntryLike] = index.getNode(normalizeJsonPath(path))

  private def normalizeJsonPath(path: String): String = {
    if (path == "#" || path == "" || path == "/") "/" // exception root cases
    else {
      val s = if (path.startsWith("#/")) path.replace("#/", "") else path
      if (s.startsWith("/")) s.stripPrefix("/") else s
    }
  }

  def findLocalJSONPath(path: String): Option[YMapEntryLike] = {
    jsonSchemaIndex.flatMap(index => findJsonPathIn(index, path))
  }

  protected def normalizedJsonPointer(url: String): String = if (url.endsWith("/")) url.dropRight(1) else url

  def link(node: YNode): Either[String, YNode] = {
    node.to[YMap] match {
      case Right(map) =>
        val ref: Option[String] = map.key("$ref").flatMap(v => v.value.asOption[YScalar]).map(_.text)
        ref match {
          case Some(url) => Left(url)
          case None      => Right(node)
        }
      case _ => Right(node)
    }
  }

  def findJsonSchema(url: String): Option[AnyShape] =
    globalSpace
      .get(normalizedJsonPointer(url))
      .collect { case shape: AnyShape => shape }

  def setJsonSchemaAST(value: YNode): Unit = {
    val location = value.sourceName
    localJSONSchemaContext = Some(value)
    val index = indexCache.getOrElse(
      location, {
        val result = AstIndexBuilder.buildAst(value,
          AliasCounter(),
          computeJsonSchemaVersion(value))(this)
        indexCache.put(location, result)
        result
      }
    )
    jsonSchemaIndex = Some(index)
  }

  def computeJsonSchemaVersion(ast: YNode): SchemaVersion = parseSchemaVersion(ast, eh)

}

abstract class JsonSchemaContext(ctx: ParserContext) extends ShapeParserContext(ctx.eh)
  with JsonSchemaLikeContext {

  override def vendor: Vendor = JsonSchemaDialect

  override def syntax: SpecSyntax = JsonSchemaSyntax

  override def closedRamlTypeShape(shape: Shape, ast: YMap, shapeType: String, typeInfo: TypeInfo): Unit =
    throw new Exception("Parser - not in RAML!")

  override def rootContextDocument: String = ctx.rootContextDocument

  override def refs: Seq[ParsedReference] = ctx.refs

  override def getMaxYamlReferences: Option[Long] = None

  override def fragments: Map[String, FragmentRef] = Map()

  override def toOasNext: ShapeParserContext = this

  override def findExample(key: String, scope: SearchScope.Scope): Option[Example] = None

  override def futureDeclarations: FutureDeclarations = ctx.futureDeclarations

  override def findType(key: String, scope: SearchScope.Scope, error: Option[String => Unit]): Option[AnyShape] = None


  override def loc: String = ctx.rootContextDocument

  override def shapes: Map[String, Shape] = Map()

  override def closedShape(node: String, ast: YMap, shape: String): Unit = {}

  override def registerJsonSchema(url: String, shape: AnyShape): Unit =
    ctx.globalSpace.update(normalizedJsonPointer(url), shape)


  override def isMainFileContext: Boolean = false

  override def findNamedExampleOrError(ast: YPart)(key: String): Example = {
    eh.violation(DeclarationNotFound, "", s"NamedExample '$key' not found", ast)
    Example(Fields(), Annotations(ast))
  }

  override def linkTypes: Boolean = false


  override def findNamedExample(key: String, error: Option[String => Unit]): Option[Example] = None

  override def isOasLikeContext: Boolean = false

  override def isOas2Context: Boolean = false

  override def isOas3Context: Boolean = false

  override def isAsyncContext: Boolean = false

  override def isRamlContext: Boolean = false

  override def isOas3Syntax: Boolean = false

  override def isOas2Syntax: Boolean = false

  override def ramlContextType: RamlWebApiContextType =
    throw new Exception("Parser - Can only be used from JSON Schema")

  override def promoteExternaltoDataTypeFragment(text: String, fullRef: String, shape: Shape): Shape =
    throw new Exception("Parser - Can only be used from JSON Schema")


  override def findDocumentations(key: String,
                                  scope: SearchScope.Scope,
                                  error: Option[String => Unit]): Option[CreativeWork] = None

  override def obtainRemoteYNode(ref: String, refAnnotations: Annotations): Option[YNode] =
    jsonSchemaRefGuide.obtainRemoteYNode(ref)

  override def findAnnotation(key: String, scope: SearchScope.Scope): Option[CustomDomainProperty] = None

  override def violation(violationId: ValidationSpecification, node: String, message: String): Unit =
    ctx.violation(violationId, node, message)

  override def addNodeRefIds(ids: mutable.Map[YNode, String]): Unit = {}

  override def nodeRefIds: mutable.Map[YNode, String] = mutable.Map()

  override def raml10createContextFromRaml: ShapeParserContext = this

  override def raml08createContextFromRaml: ShapeParserContext = this

  override def libraries: Map[String, Declarations] = Map()

  override def typeParser: (YMapEntry, Shape => Unit, Boolean, DefaultType) => RamlTypeParser =
    throw new Exception("Parser - Cann called only from JSON Schema")


  override def ramlExternalSchemaParserFactory: RamlExternalSchemaExpressionFactory =
    throw new Exception("Parser - Cann called only from JSON Schema")

  override def validateRefFormatWithError(ref: String): Boolean = true

  override val defaultSchemaVersion: JSONSchemaVersion = JSONSchemaDraft4SchemaVersion

  override def parseRemoteJSONPath(ref: String): Option[AnyShape] = None

  override def getInheritedDeclarations: Option[Declarations] = None

}

object JsonSchemaContext {
  def apply(ctx: ParserContext): ShapeParserContext = {
    new JsonSchemaContext(ctx) {
      override var jsonSchemaIndex: Option[AstIndex] = None
      override var globalSpace: mutable.Map[String, Any] = mutable.Map()
      override var localJSONSchemaContext: Option[YNode] = None
      override var indexCache: mutable.Map[String, AstIndex] = mutable.Map()

      override def makeJsonSchemaContextForParsing(url: String, document: Root, options: ParsingOptions): ShapeParserContext = JsonSchemaContext(ctx)

    }
  }
}
