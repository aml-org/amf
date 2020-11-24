package amf.plugins.document.webapi.contexts

import amf.core.client.ParsingOptions
import amf.core.model.document.{ExternalFragment, Fragment, RecursiveUnit}
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, ParsedReference, ParserContext}
import amf.core.remote._
import amf.core.unsafe.PlatformSecrets
import amf.core.utils.{AmfStrings, IdCounter}
import amf.plugins.document.webapi.annotations.DeclarationKey
import amf.plugins.document.webapi.contexts.parser.oas.{JsonSchemaAstIndex, OasWebApiContext}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.declaration.{
  JSONSchemaDraft4SchemaVersion,
  JSONSchemaVersion,
  SchemaVersion
}
import amf.plugins.document.webapi.parser.spec.domain.OasParameter
import amf.plugins.document.webapi.parser.spec.jsonschema.{AstFinder, JsonSchemaInference}
import amf.plugins.domain.shapes.models.AnyShape
import amf.validations.ParserSideValidations.{
  ClosedShapeSpecification,
  ClosedShapeSpecificationWarning,
  InvalidJsonSchemaVersion
}
import org.yaml.model._

abstract class WebApiContext(val loc: String,
                             refs: Seq[ParsedReference],
                             val options: ParsingOptions,
                             wrapped: ParserContext,
                             declarationsOption: Option[WebApiDeclarations] = None)
    extends ParserContext(loc, refs, wrapped.futureDeclarations, wrapped.eh)
    with SpecAwareContext
    with PlatformSecrets
    with JsonSchemaInference {

  override val defaultSchemaVersion: JSONSchemaVersion = JSONSchemaDraft4SchemaVersion

  def validateRefFormatWithError(ref: String): Boolean = true

  val syntax: SpecSyntax
  val vendor: Vendor
  private var declarationKeys: List[DeclarationKey] = List.empty
  val declarations: WebApiDeclarations = declarationsOption.getOrElse(
    new WebApiDeclarations(None, errorHandler = eh, futureDeclarations = futureDeclarations))

  var localJSONSchemaContext: Option[YNode] = wrapped match {
    case wac: WebApiContext => wac.localJSONSchemaContext
    case _                  => None
  }

  private var jsonSchemaIndex: Option[JsonSchemaAstIndex] = wrapped match {
    case wac: WebApiContext => wac.jsonSchemaIndex
    case _                  => None
  }

  var jsonSchemaRefGuide: JsonSchemaRefGuide = JsonSchemaRefGuide(loc, refs)(this)

  def setJsonSchemaAST(value: YNode): Unit = {
    localJSONSchemaContext = Some(value)
    jsonSchemaIndex = Some(JsonSchemaAstIndex(value)(this))
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

  // TODO this should not have OasWebApiContext as a dependency
  def parseRemoteOasParameter(fileUrl: String, parentId: String)(
      implicit ctx: OasWebApiContext): Option[OasParameter] = {
    val referenceUrl = getReferenceUrl(fileUrl)
    obtainFragment(fileUrl) flatMap { fragment =>
      AstFinder.findAst(fragment, referenceUrl).map { node =>
        ctx.factory.parameterParser(YMapEntryLike(node), parentId, None, new IdCounter()).parse
      }
    }
  }

  def obtainRemoteYNode(ref: String, refAnnotations: Annotations = Annotations())(
      implicit ctx: WebApiContext): Option[YNode] = {
    jsonSchemaRefGuide.obtainRemoteYNode(ref)
  }

  private def obtainFragment(fileUrl: String): Option[Fragment] = {
    val baseFileUrl = fileUrl.split("#").head
    refs
      .filter(r => r.unit.location().isDefined)
      .filter(_.unit.location().get == baseFileUrl) collectFirst {
      case ref if ref.unit.isInstanceOf[ExternalFragment] =>
        ref.unit.asInstanceOf[ExternalFragment]
      case ref if ref.unit.isInstanceOf[RecursiveUnit] =>
        ref.unit.asInstanceOf[RecursiveUnit]
    }
  }

  private def getReferenceUrl(fileUrl: String): Option[String] = {
    fileUrl.split("#") match {
      case s: Array[String] if s.size > 1 => Some(s.last)
      case _                              => None
    }
  }

  def computeJsonSchemaVersion(ast: YNode): SchemaVersion = parseSchemaVersion(ast, eh)

  def resolvedPath(base: String, str: String): String =
    if (str.isEmpty) platform.normalizePath(base)
    else if (str.startsWith("/")) str
    else if (str.contains(":")) str
    else if (str.startsWith("#")) base.split("#").head + str
    else if (shouldSkipDot(base, str)) basePath(base) + str.substring(2)
    else platform.normalizePath(basePath(base).urlDecoded + str)

  def shouldSkipDot(base: String, path: String): Boolean =
    (basePath(base) == "file://" || basePath(base) == "file:///") && path.startsWith("./")

  def basePath(path: String): String = {
    val withoutHash = if (path.contains("#")) path.split("#").head else path
    withoutHash.splitAt(withoutHash.lastIndexOf("/"))._1 + "/"
  }

  private def normalizeJsonPath(path: String): String = {
    if (path == "#" || path == "" || path == "/") "/" // exception root cases
    else {
      val s = if (path.startsWith("#/")) path.replace("#/", "") else path
      if (s.startsWith("/")) s.stripPrefix("/") else s
    }
  }

  def findJsonPathIn(index: JsonSchemaAstIndex, path: String) =
    index.getNodeAndEntry(normalizeJsonPath(path)).map { (path, _) }

  // TODO: Evaluate if this can return a YMapEntryLike
  def findLocalJSONPath(path: String): Option[(String, Either[YNode, YMapEntry])] = {
    // todo: past uri?
    jsonSchemaIndex.flatMap(index => findJsonPathIn(index, path))
  }

  def link(node: YNode): Either[String, YNode]
  protected def ignore(shape: String, property: String): Boolean
  def autoGeneratedAnnotation(s: Shape): Unit

  /** Validate closed shape. */
  def closedShape(node: String, ast: YMap, shape: String): Unit = closedShape(node, ast, shape, syntax)

  protected def closedShape(node: String, ast: YMap, shape: String, syntax: SpecSyntax): Unit =
    syntax.nodes.get(shape) match {
      case Some(properties) =>
        ast.entries.foreach { entry =>
          val key: String = getEntryKey(entry)
          if (!ignore(shape, key) && !properties(key)) {
            throwClosedShapeError(node, s"Property '$key' not supported in a $vendor $shape node", entry)
          }
        }
      case None => nextValidation(node, shape, ast)
    }

  def getEntryKey(entry: YMapEntry): String = {
    entry.key.asOption[YScalar].map(_.text).getOrElse(entry.key.toString)
  }

  def addDeclarationKey(key: DeclarationKey): Unit = {
    declarationKeys = key :: declarationKeys
  }

  def getDeclarationKeys: List[DeclarationKey] = this.declarationKeys

  protected def nextValidation(node: String, shape: String, ast: YMap): Unit =
    throwClosedShapeError(node, s"Cannot validate unknown node type $shape for $vendor", ast)

  protected def throwClosedShapeError(node: String, message: String, entry: YPart, isWarning: Boolean = false): Unit =
    if (isWarning) eh.warning(ClosedShapeSpecificationWarning, node, message, entry)
    else eh.violation(ClosedShapeSpecification, node, message, entry)
}
