package amf.apicontract.internal.spec.common.parser

import amf.aml.client.scala.model.document.Dialect
import amf.aml.internal.parse.common.DeclarationContext
import amf.aml.internal.registries.AMLRegistry
import amf.aml.internal.semantic.{SemanticExtensionsFacade, SemanticExtensionsFacadeBuilder}
import amf.apicontract.internal.spec.common.WebApiDeclarations
import amf.apicontract.internal.spec.common.emitter.SpecAwareContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{
  ClosedShapeSpecification,
  ClosedShapeSpecificationWarning
}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.document.{ExternalFragment, Fragment, RecursiveUnit}
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.model.domain.{AmfObject, Shape}
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.datanode.DataNodeParserContext
import amf.core.internal.parser._
import amf.core.internal.parser.domain.{Annotations, FragmentRef, SearchScope}
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.remote.Spec
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.utils.{AliasCounter, QName}
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.parser.{SpecSyntax, YMapEntryLike}
import amf.shapes.internal.spec.common.{JSONSchemaDraft4SchemaVersion, SchemaVersion}
import amf.shapes.internal.spec.contexts.JsonSchemaRefGuide
import amf.shapes.internal.spec.jsonschema.ref.{AstFinder, AstIndex, AstIndexBuilder, JsonSchemaInference}
import org.mulesoft.common.client.lexical.SourceLocation
import org.yaml.model._
import scala.collection.mutable

abstract class ExtensionsContext(
    val loc: String,
    refs: Seq[ParsedReference],
    val options: ParsingOptions,
    wrapped: ParserContext,
    val declarationsOption: Option[WebApiDeclarations] = None,
    val nodeRefIds: mutable.Map[YNode, String] = mutable.Map.empty
) extends ParserContext(loc, refs, wrapped.futureDeclarations, wrapped.config)
    with DataNodeParserContext {

  private def getExtensionsMap: Map[String, Dialect] = wrapped.config.registryContext.getRegistry match {
    case amlRegistry: AMLRegistry => amlRegistry.getExtensionRegistry
    case _                        => Map.empty
  }

  val declarations: WebApiDeclarations = declarationsOption.getOrElse(
    new WebApiDeclarations(
      None,
      errorHandler = eh,
      futureDeclarations = futureDeclarations,
      extensions = getExtensionsMap
    )
  )

  override def findAnnotation(key: String, scope: SearchScope.Scope): Option[CustomDomainProperty] =
    declarations.findAnnotation(key, scope)

  override def getMaxYamlReferences: Option[Int] = options.getMaxYamlReferences

  override def fragments: Map[String, FragmentRef] = declarations.fragments
}

abstract class WebApiContext(
    loc: String,
    refs: Seq[ParsedReference],
    options: ParsingOptions,
    wrapped: ParserContext,
    declarationsOption: Option[WebApiDeclarations] = None,
    nodeRefIds: mutable.Map[YNode, String] = mutable.Map.empty
) extends ExtensionsContext(loc, refs, options, wrapped, declarationsOption, nodeRefIds)
    with DeclarationContext
    with SpecAwareContext
    with PlatformSecrets
    with JsonSchemaInference
    with ParseErrorHandler
    with IllegalTypeHandler {

  private val syamlEh                                                    = new SyamlAMFErrorHandler(wrapped.config.eh)
  override def handle[T](error: YError, defaultValue: T): T              = syamlEh.handle(error, defaultValue)
  override def handle(location: SourceLocation, e: SyamlException): Unit = syamlEh.handle(location, e)

  override val defaultSchemaVersion: SchemaVersion = JSONSchemaDraft4SchemaVersion

  def validateRefFormatWithError(ref: String): Boolean = true

  def syntax: SpecSyntax
  def spec: Spec
  def ignoreCriteria: IgnoreCriteria

  protected val closedShapeValidator: ClosedShapeValidator = DefaultClosedShapeValidator(ignoreCriteria, spec, syntax)

  val extensionsFacadeBuilder: SemanticExtensionsFacadeBuilder = WebApiSemanticExtensionsFacadeBuilder(
    DeclaredAnnotationSchemaValidatorBuilder
  )

  def getLocalJsonSchemaContext: Option[YNode] = localJSONSchemaContext
  def removeLocalJsonSchemaContext: Unit       = localJSONSchemaContext = None

  private var localJSONSchemaContext: Option[YNode] = wrapped match {
    case wac: WebApiContext => wac.localJSONSchemaContext
    case _                  => None
  }

  private var jsonSchemaIndex: Option[AstIndex] = wrapped match {
    case wac: WebApiContext => wac.jsonSchemaIndex
    case _                  => None
  }

  def getJsonSchemaRefGuide: JsonSchemaRefGuide                           = jsonSchemaRefGuide
  protected def setJsonSchemaRefGuide(refGuide: JsonSchemaRefGuide): Unit = jsonSchemaRefGuide = refGuide

  protected var jsonSchemaRefGuide: JsonSchemaRefGuide =
    JsonSchemaRefGuide(loc, refs)(WebApiShapeParserContextAdapter(this))

  var indexCache: Map[String, AstIndex] = Map[String, AstIndex]()

  def setJsonSchemaAST(value: YNode): Unit = {
    val location = value.sourceName
    localJSONSchemaContext = Some(value)
    val index = indexCache.getOrElse(
      location, {
        val result =
          AstIndexBuilder.buildAst(value, AliasCounter(options.getMaxYamlReferences), computeJsonSchemaVersion(value))(
            WebApiShapeParserContextAdapter(this)
          )
        result
      }
    )
    indexCache = indexCache + (location -> index)
    jsonSchemaIndex = Some(index)
  }

  globalSpace = wrapped.globalSpace

  // JSON Schema has a global namespace

  protected def normalizedJsonPointer(url: String): String = if (url.endsWith("/")) url.dropRight(1) else url

  def findJsonSchema(url: String): Option[AnyShape] =
    globalSpace
      .get(normalizedJsonPointer(url))
      .collect { case shape: AnyShape => shape }

  def registerJsonSchema(url: String, shape: AnyShape): Unit =
    globalSpace.update(normalizedJsonPointer(url), shape)

  def obtainRemoteYNode(ref: String)(implicit ctx: WebApiContext): Option[YNode] = {
    jsonSchemaRefGuide.obtainRemoteYNode(ref)
  }

  def computeJsonSchemaVersion(ast: YNode): SchemaVersion = parseSchemaVersion(ast, eh)

  private def normalizeJsonPath(path: String): String = {
    if (path == "#" || path == "" || path == "/") "/" // exception root cases
    else {
      val s = if (path.startsWith("#/")) path.replace("#/", "") else path
      if (s.startsWith("/")) s.stripPrefix("/") else s
    }
  }

  def findJsonPathIn(index: AstIndex, path: String): Option[YMapEntryLike] = index.getNode(normalizeJsonPath(path))

  def findLocalJSONPath(path: String): Option[YMapEntryLike] = {
    jsonSchemaIndex.flatMap(index => findJsonPathIn(index, path))
  }

  def link(node: YNode): Either[String, YNode]
  def autoGeneratedAnnotation(s: Shape): Unit

  /** Validate closed shape. */
  def closedShape(node: AmfObject, ast: YMap, shape: String): Unit = closedShapeValidator.evaluate(node, ast, shape)

  def getEntryKey(entry: YMapEntry): String = {
    entry.key.asOption[YScalar].map(_.text).getOrElse(entry.key.toString)
  }

  case class WebApiSemanticExtensionsFacadeBuilder(annotationSchemaValidatorBuilder: AnnotationSchemaValidatorBuilder)
      extends SemanticExtensionsFacadeBuilder {
    override def extensionName(name: String): SemanticExtensionsFacade = {
      val fqn = QName(name)
      val facadeBuilder = if (fqn.isQualified) {
        val maybeDeclarations: Option[WebApiDeclarations] =
          declarations.libraries.get(fqn.qualification).collectFirst({ case w: WebApiDeclarations => w })
        maybeDeclarations.flatMap(d =>
          (d.extensions
            .get(fqn.name))
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
