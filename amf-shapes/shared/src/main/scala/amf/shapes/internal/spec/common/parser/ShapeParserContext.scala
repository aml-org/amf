package amf.shapes.internal.spec.common.parser

import amf.aml.internal.semantic.{SemanticExtensionsFacade, SemanticExtensionsFacadeBuilder}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Fragment}
import amf.core.client.scala.model.domain.{AmfObject, Shape}
import amf.core.client.scala.parse.document.{
  EmptyFutureDeclarations,
  ErrorHandlingContext,
  ParsedReference,
  ParserContext
}
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.{Declarations, SearchScope}
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.remote.{Oas30, Spec}
import amf.core.internal.utils.{AliasCounter, QName}
import amf.shapes.client.scala.model.domain.{AnyShape, CreativeWork, Example, SemanticContext}
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.contexts.JsonSchemaRefGuide
import amf.shapes.internal.spec.jsonschema.parser
import amf.shapes.internal.spec.jsonschema.parser.JsonSchemaSettings
import amf.shapes.internal.spec.jsonschema.ref.{AstIndex, AstIndexBuilder, JsonSchemaInference}
import amf.shapes.internal.spec.oas.parser.{Oas2Settings, Oas2ShapeSyntax, Oas3Settings, Oas3ShapeSyntax}
import amf.shapes.internal.spec.raml.parser.RamlWebApiContextType.{DEFAULT, RamlWebApiContextType}
import amf.shapes.internal.spec.raml.parser.{Raml08Settings, Raml08ShapeSyntax, Raml10Settings, Raml10ShapeSyntax}
import org.mulesoft.common.client.lexical.SourceLocation
import org.yaml.model._

import scala.collection.mutable

class ShapeParserContext(
    loc: String,
    refs: Seq[ParsedReference],
    options: ParsingOptions,
    wrapped: ParserContext,
    declarationsOption: Option[ShapeDeclarations] = None,
    nodeRefIds: mutable.Map[YNode, String] = mutable.Map.empty,
    settings: SpecSettings
) extends ExtensionsContext(loc, refs, options, wrapped, declarationsOption, nodeRefIds)
    with ErrorHandlingContext
    with JsonSchemaInference
    with ParseErrorHandler
    with IllegalTypeHandler {

  override def eh: AMFErrorHandler                     = wrapped.eh
  def syamleh                                          = new SyamlAMFErrorHandler(eh)
  val defaultSchemaVersion: SchemaVersion              = settings.defaultSchemaVersion
  protected var jsonSchemaRefGuide: JsonSchemaRefGuide = JsonSchemaRefGuide(loc, refs)(this)
  def getJsonSchemaRefGuide: JsonSchemaRefGuide        = jsonSchemaRefGuide

  private var semanticContext: Option[SemanticContext] = None
  private var localJSONSchemaContext: Option[YNode] = wrapped match {
    case wac: ShapeParserContext => wac.getLocalJsonSchemaContext
    case _                       => None
  }

  private var jsonSchemaIndex: Option[AstIndex] = wrapped match {
    case wac: ShapeParserContext => wac.jsonSchemaIndex
    case _                       => None
  }

  var indexCache: Map[String, AstIndex] = Map[String, AstIndex]()
  globalSpace = wrapped.globalSpace

  def handle[T](error: YError, defaultValue: T): T              = syamleh.handle(error, defaultValue)
  def handle(location: SourceLocation, e: SyamlException): Unit = syamleh.handle(location, e)

  def addDeclaredShape(shape: Shape): Unit                 = declarations += shape
  def libraries: Map[String, Declarations]                 = declarations.libraries
  def promotedFragments: Seq[Fragment]                     = declarations.promotedFragments
  def addPromotedFragments(fragments: Seq[Fragment]): Unit = declarations.promotedFragments ++= fragments
  def findExample(key: String, scope: SearchScope.Scope): Option[Example] =
    declarations.findExample(key, scope)
  def findType(key: String, scope: SearchScope.Scope, error: Option[String => Unit] = None): Option[AnyShape] =
    declarations.findType(key, scope, error)
  def shapes: Map[String, Shape] = declarations.shapes
  def findDocumentations(
      key: String,
      scope: SearchScope.Scope,
      error: Option[String => Unit] = None
  ): Option[CreativeWork] = declarations.findDocumentations(key, scope, error)

  def findCachedJsonSchema(url: String): Option[AnyShape] =
    globalSpace
      .get(normalizedJsonPointer(url))
      .collect { case shape: AnyShape => shape }

  def registerJsonSchema(url: String, shape: AnyShape): Unit =
    globalSpace.update(normalizedJsonPointer(url), shape)

  def setJsonSchemaAST(value: YNode): Unit = {
    val location = value.sourceName
    localJSONSchemaContext = Some(value)
    val index = indexCache.getOrElse(
      location, {
        val result =
          AstIndexBuilder.buildAst(value, AliasCounter(options.getMaxYamlReferences), computeJsonSchemaVersion(value))(
            this
          )
        result
      }
    )
    indexCache = indexCache + (location -> index)
    jsonSchemaIndex = Some(index)
  }

  def computeJsonSchemaVersion(ast: YNode): SchemaVersion = parseSchemaVersion(ast, eh)

  def addNodeRefIds(ids: mutable.Map[YNode, String]): Unit = nodeRefIds ++= ids

  def obtainRemoteYNode(ref: String): Option[YNode] =
    jsonSchemaRefGuide.obtainRemoteYNode(ref)

  def removeLocalJsonSchemaContext: Unit = localJSONSchemaContext = None

  def getLocalJsonSchemaContext: Option[YNode] = localJSONSchemaContext

  def spec: Spec = settings.spec

  def syntax: SpecSyntax = settings.syntax

  def isMainFileContext: Boolean = wrapped.rootContextDocument == jsonSchemaRefGuide.currentLoc

  def link(node: YNode): Either[String, YNode] = settings.link(node)(syamleh)

  def ignoreCriteria: IgnoreCriteria = settings.ignoreCriteria

  def linkTypes: Boolean = settings.shouldLinkTypes(wrapped)

  def isOasLikeContext: Boolean = settings.isOasLikeContext

  def isOas2Context: Boolean = settings.isOas2Context

  def isOas3Context: Boolean = settings.isOas3Context

  def isAsyncContext: Boolean = settings.isAsyncContext

  def isRamlContext: Boolean = settings.isRamlContext

  def isOas3Syntax: Boolean = settings.isOas3Context

  def isOas2Syntax: Boolean = settings.isOas2Context

  def ramlContextType: Option[RamlWebApiContextType] = settings.ramlContextType

  def findLocalJSONPath(path: String): Option[YMapEntryLike] =
    jsonSchemaIndex.flatMap(index => findJsonPathIn(index, path))

  def findNamedExampleOrError(ast: YPart)(key: String): Example =
    declarations.findNamedExampleOrError(ast)(key)
  def findNamedExample(key: String, error: Option[String => Unit] = None): Option[Example] =
    declarations.findNamedExample(key, error)

  def getInheritedDeclarations: Option[ShapeDeclarations] = declarationsOption

  def makeJsonSchemaContextForParsing(
      url: String,
      document: Root,
      options: ParsingOptions
  ): ShapeParserContext = {
    val cleanNested = ParserContext(url, document.references, EmptyFutureDeclarations(), config)
    cleanNested.globalSpace = globalSpace

    // Apparently, in a RAML 0.8 API spec the JSON Schema has a closure over the schemas declared in the spec...
    val inheritedDeclarations = getInheritedDeclarations
    val schemaContext = new ShapeParserContext(
      url,
      document.references,
      options,
      cleanNested,
      inheritedDeclarations,
      nodeRefIds,
      JsonSchemaSettings(Oas3ShapeSyntax, settings.defaultSchemaVersion)
    )
    schemaContext.indexCache = indexCache
    schemaContext
  }

  def closedShape(node: AmfObject, ast: YMap, shape: String): Unit =
    settings.closedShapeValidator.evaluate(node, ast, shape)(syamleh)

  def toOas: ShapeParserContext = {
    val settings = spec match {
      case Oas30 => Oas3Settings(Oas3ShapeSyntax)
      case _     => Oas2Settings(Oas2ShapeSyntax)
    }
    new ShapeParserContext(loc, refs, options, this, Some(declarations.copy()), nodeRefIds, settings)
  }

  def toJsonSchema(): ShapeParserContext = toJsonSchema(loc, refs)

  def toJsonSchema(root: String, refs: Seq[ParsedReference]): ShapeParserContext = {
    val next = new ShapeParserContext(
      root,
      refs,
      options,
      this,
      Some(declarations.copy()),
      nodeRefIds,
      parser.JsonSchemaSettings(Oas3ShapeSyntax, settings.defaultSchemaVersion)
    )
    next.indexCache = this.indexCache
    next
  }

  def toRaml10: ShapeParserContext = new ShapeParserContext(
    loc,
    refs,
    options,
    this,
    Some(declarations),
    nodeRefIds,
    new Raml10Settings(Raml10ShapeSyntax, settings.ramlContextType.getOrElse(DEFAULT))
  )

  def toRaml08: ShapeParserContext = new ShapeParserContext(
    loc,
    refs,
    options,
    this,
    Some(declarations),
    nodeRefIds,
    new Raml08Settings(Raml08ShapeSyntax, settings.ramlContextType.getOrElse(DEFAULT))
  )

  def promoteExternalToDataTypeFragment(text: String, fullRef: String, shape: Shape): Unit =
    declarations.promoteExternalToDataTypeFragment(text, fullRef, shape)

  def registerExternalRef(external: (String, AnyShape)): Unit = declarations.registerExternalRef(external)

  def findInExternalsLibs(lib: String, name: String): Option[AnyShape] =
    declarations.findInExternalsLibs(lib, name)

  def findInExternals(url: String): Option[AnyShape] = declarations.findInExternals(url)

  def registerExternalLib(url: String, content: Map[String, AnyShape]): Unit =
    declarations.registerExternalLib(url, content)

  def extensionsFacadeBuilder: SemanticExtensionsFacadeBuilder = DefaultSemanticExtensionsFacadeBuilder(
    settings.annotationValidatorBuilder
  )

  def validateRefFormatWithError(ref: String): Boolean = true

  def getSemanticContext: Option[SemanticContext]            = semanticContext
  def withSemanticContext(sc: Option[SemanticContext]): Unit = semanticContext = sc

  def copyForBase(unit: BaseUnit): ShapeParserContext = {
    makeCopyWithJsonPointerContext().moveToReference(unit.location().get)
  }

  protected def normalizedJsonPointer(url: String): String = if (url.endsWith("/")) url.dropRight(1) else url

  private def findJsonPathIn(index: AstIndex, path: String): Option[YMapEntryLike] =
    index.getNode(normalizeJsonPath(path))

  private def normalizeJsonPath(path: String): String = {
    if (path == "#" || path == "" || path == "/") "/" // exception root cases
    else {
      val s = if (path.startsWith("#/")) path.replace("#/", "") else path
      if (s.startsWith("/")) s.stripPrefix("/") else s
    }
  }

  private def makeCopyWithJsonPointerContext() = {
    val copy = makeCopy()
    copy.jsonSchemaRefGuide = this.jsonSchemaRefGuide
    copy.indexCache = this.indexCache
    copy
  }

  private def makeCopy(): ShapeParserContext = {
    new ShapeParserContext(loc, refs, options, this, Some(declarations.copy()), nodeRefIds, settings)
  }

  private def moveToReference(loc: String): this.type = {
    jsonSchemaRefGuide = jsonSchemaRefGuide.changeJsonSchemaSearchDestination(loc)
    this
  }

  case class DefaultSemanticExtensionsFacadeBuilder(annotationSchemaValidatorBuilder: AnnotationSchemaValidatorBuilder)
      extends SemanticExtensionsFacadeBuilder {
    def extensionName(name: String): SemanticExtensionsFacade = {
      val fqn = QName(name)
      val facadeBuilder = if (fqn.isQualified) {
        val maybeDeclarations: Option[ShapeDeclarations] =
          declarations.libraries.get(fqn.qualification).collectFirst({ case w: ShapeDeclarations => w })
        maybeDeclarations.flatMap(d =>
          d.extensions
            .get(fqn.name)
            .map(SemanticExtensionsFacade(fqn.name, _, annotationSchemaValidatorBuilder.build(d.annotations)))
        )
      } else
        declarations.extensions
          .get(name)
          .map(d => SemanticExtensionsFacade(name, d, annotationSchemaValidatorBuilder.build(declarations.annotations)))
      facadeBuilder.getOrElse(
        SemanticExtensionsFacade(name, wrapped.config, annotationSchemaValidatorBuilder.build(declarations.annotations))
      )
    }
  }
}
