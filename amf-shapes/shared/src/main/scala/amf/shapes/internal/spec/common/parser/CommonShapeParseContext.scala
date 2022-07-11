package amf.shapes.internal.spec.common.parser

import amf.aml.internal.semantic.{
  AnnotationSchemaValidator,
  IgnoreAnnotationSchemaValidator,
  SemanticExtensionsFacade,
  SemanticExtensionsFacadeBuilder
}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.Fragment
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.model.domain.{AmfObject, Shape}
import amf.core.client.scala.parse.document.{EmptyFutureDeclarations, ParsedReference, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.{Annotations, Declarations, SearchScope}
import amf.core.internal.remote.{Oas30, Spec}
import amf.core.internal.utils.{AliasCounter, QName}
import amf.shapes.client.scala.model.domain.{AnyShape, CreativeWork, Example}
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.RamlWebApiContextType.{DEFAULT, RamlWebApiContextType}
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.contexts.JsonSchemaRefGuide
import amf.shapes.internal.spec.jsonschema.ref.{AstIndex, AstIndexBuilder, JsonSchemaInference, JsonSchemaParser}
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.{
  AnnotationSchemaMustBeAny,
  MissingAnnotationSchema
}
import org.mulesoft.common.client.lexical.SourceLocation
import org.yaml.model._

import scala.collection.mutable

class CommonShapeParseContext(
    loc: String,
    refs: Seq[ParsedReference],
    options: ParsingOptions,
    wrapped: ParserContext,
    declarationsOption: Option[ShapeDeclarations] = None,
    nodeRefIds: mutable.Map[YNode, String] = mutable.Map.empty,
    settings: SpecSettings
) extends ExtensionsContext(loc, refs, options, wrapped, declarationsOption, nodeRefIds)
    with JsonSchemaInference
    with ShapeParserContext {

  override def eh: AMFErrorHandler                 = wrapped.eh
  override val defaultSchemaVersion: SchemaVersion = settings.defaultSchemaVersion
  var jsonSchemaRefGuide: JsonSchemaRefGuide       = JsonSchemaRefGuide(loc, refs)(this)

  private var localJSONSchemaContext: Option[YNode] = wrapped match {
    case wac: CommonShapeParseContext => wac.getLocalJsonSchemaContext
    case _                            => None
  }

  private var jsonSchemaIndex: Option[AstIndex] = wrapped match {
    case wac: CommonShapeParseContext => wac.jsonSchemaIndex
    case _                            => None
  }

  var indexCache: Map[String, AstIndex] = Map[String, AstIndex]()
  globalSpace = wrapped.globalSpace

  override def handle[T](error: YError, defaultValue: T): T              = syamleh.handle(error, defaultValue)
  override def handle(location: SourceLocation, e: SyamlException): Unit = syamleh.handle(location, e)

  override def addDeclaredShape(shape: Shape): Unit                 = declarations += shape
  override def libraries: Map[String, Declarations]                 = declarations.libraries
  override def promotedFragments: Seq[Fragment]                     = declarations.promotedFragments
  override def addPromotedFragments(fragments: Seq[Fragment]): Unit = declarations.promotedFragments ++= fragments
  override def findExample(key: String, scope: SearchScope.Scope): Option[Example] =
    declarations.findExample(key, scope)
  override def findType(key: String, scope: SearchScope.Scope, error: Option[String => Unit]): Option[AnyShape] =
    declarations.findType(key, scope, error)
  override def shapes: Map[String, Shape] = declarations.shapes
  override def findDocumentations(
      key: String,
      scope: SearchScope.Scope,
      error: Option[String => Unit]
  ): Option[CreativeWork] = declarations.findDocumentations(key, scope, error)

  override def findJsonSchema(url: String): Option[AnyShape] =
    globalSpace
      .get(normalizedJsonPointer(url))
      .collect { case shape: AnyShape => shape }

  override def registerJsonSchema(url: String, shape: AnyShape): Unit =
    globalSpace.update(normalizedJsonPointer(url), shape)

  override def setJsonSchemaAST(value: YNode): Unit = {
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

  override def computeJsonSchemaVersion(ast: YNode): SchemaVersion = parseSchemaVersion(ast, eh)

  override def addNodeRefIds(ids: mutable.Map[YNode, String]): Unit = nodeRefIds ++= ids

  override def obtainRemoteYNode(ref: String, refAnnotations: Annotations): Option[YNode] =
    jsonSchemaRefGuide.obtainRemoteYNode(ref)

  override def removeLocalJsonSchemaContext: Unit = localJSONSchemaContext = None

  override def getLocalJsonSchemaContext: Option[YNode] = localJSONSchemaContext

  override def parseRemoteJSONPath(ref: String): Option[AnyShape] = {
    jsonSchemaRefGuide.withFragmentAndInFileReference(ref) { (fragment, referenceUrl) =>
      val newCtx = makeCopyWithJsonPointerContext().moveToReference(fragment.location().get)
      new JsonSchemaParser().parse(fragment, referenceUrl)(newCtx)
    }
  }

  override def spec: Spec = settings.spec

  override def syntax: SpecSyntax = settings.syntax

  override def isMainFileContext: Boolean = wrapped.rootContextDocument == jsonSchemaRefGuide.currentLoc

  override def link(node: YNode): Either[String, YNode] = settings.link(node)(syamleh)

  override def ignoreCriteria: IgnoreCriteria = settings.ignoreCriteria

  override def linkTypes: Boolean = settings.shouldLinkTypes(wrapped)

  override def isOasLikeContext: Boolean = settings.isOasLikeContext

  override def isOas2Context: Boolean = settings.isOas2Context

  override def isOas3Context: Boolean = settings.isOas3Context

  override def isAsyncContext: Boolean = settings.isAsyncContext

  override def isRamlContext: Boolean = settings.isRamlContext

  override def isOas3Syntax: Boolean = settings.isOas3Context

  override def isOas2Syntax: Boolean = settings.isOas2Context

  override def ramlContextType: Option[RamlWebApiContextType] = settings.ramlContextType

  def findLocalJSONPath(path: String): Option[YMapEntryLike] =
    jsonSchemaIndex.flatMap(index => findJsonPathIn(index, path))

  override def findNamedExampleOrError(ast: YPart)(key: String): Example =
    declarations.findNamedExampleOrError(ast)(key)
  override def findNamedExample(key: String, error: Option[String => Unit]): Option[Example] =
    declarations.findNamedExample(key, error)

  override def getInheritedDeclarations: Option[ShapeDeclarations] = declarationsOption

  override def makeJsonSchemaContextForParsing(
      url: String,
      document: Root,
      options: ParsingOptions
  ): ShapeParserContext = {
    val cleanNested = ParserContext(url, document.references, EmptyFutureDeclarations(), config)
    cleanNested.globalSpace = globalSpace

    // Apparently, in a RAML 0.8 API spec the JSON Schema has a closure over the schemas declared in the spec...
    val inheritedDeclarations = getInheritedDeclarations
    val schemaContext = new CommonShapeParseContext(
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

  override def closedShape(node: AmfObject, ast: YMap, shape: String): Unit =
    settings.closedShapeValidator.evaluate(node, ast, shape)(syamleh)

  override def toOas: ShapeParserContext = {
    val settings = spec match {
      case Oas30 => Oas3Settings(Oas3ShapeSyntax)
      case _     => Oas2Settings(Oas2ShapeSyntax)
    }
    new CommonShapeParseContext(loc, refs, options, this, Some(declarations.copy()), nodeRefIds, settings)
  }

  override def toJsonSchema(): ShapeParserContext = toJsonSchema(loc, refs)

  override def toJsonSchema(root: String, refs: Seq[ParsedReference]): ShapeParserContext = {
    val next = new CommonShapeParseContext(
      root,
      refs,
      options,
      this,
      Some(declarations.copy()),
      nodeRefIds,
      JsonSchemaSettings(Oas3ShapeSyntax, settings.defaultSchemaVersion)
    )
    next.indexCache = this.indexCache
    next
  }

  override def toRaml10: ShapeParserContext = new CommonShapeParseContext(
    loc,
    refs,
    options,
    this,
    Some(declarations),
    nodeRefIds,
    new Raml10Settings(Raml10ShapeSyntax, settings.ramlContextType.getOrElse(DEFAULT))
  )

  override def toRaml08: ShapeParserContext = new CommonShapeParseContext(
    loc,
    refs,
    options,
    this,
    Some(declarations),
    nodeRefIds,
    new Raml08Settings(Raml08ShapeSyntax, settings.ramlContextType.getOrElse(DEFAULT))
  )

  override def promoteExternalToDataTypeFragment(text: String, fullRef: String, shape: Shape): Unit =
    declarations.promoteExternalToDataTypeFragment(text, fullRef, shape)

  override def registerExternalRef(external: (String, AnyShape)): Unit = declarations.registerExternalRef(external)

  override def findInExternalsLibs(lib: String, name: String): Option[AnyShape] =
    declarations.findInExternalsLibs(lib, name)

  override def findInExternals(url: String): Option[AnyShape] = declarations.findInExternals(url)

  override def registerExternalLib(url: String, content: Map[String, AnyShape]): Unit =
    declarations.registerExternalLib(url, content)

  override def extensionsFacadeBuilder: SemanticExtensionsFacadeBuilder = DefaultSemanticExtensionsFacadeBuilder(
    settings.annotationValidatorBuilder
  )

  override def validateRefFormatWithError(ref: String): Boolean = true

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

  private def makeCopy(): CommonShapeParseContext = {
    new CommonShapeParseContext(loc, refs, options, this, Some(declarations.copy()), nodeRefIds, settings)
  }

  private def moveToReference(loc: String): this.type = {
    jsonSchemaRefGuide = jsonSchemaRefGuide.changeJsonSchemaSearchDestination(loc)
    this
  }

  case class DefaultSemanticExtensionsFacadeBuilder(annotationSchemaValidatorBuilder: AnnotationSchemaValidatorBuilder)
      extends SemanticExtensionsFacadeBuilder {
    override def extensionName(name: String): SemanticExtensionsFacade = {
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
