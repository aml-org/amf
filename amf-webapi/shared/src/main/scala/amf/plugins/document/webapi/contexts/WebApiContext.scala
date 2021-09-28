package amf.plugins.document.webapi.contexts

import amf.core.client.ParsingOptions
import amf.core.model.document.{ExternalFragment, Fragment, RecursiveUnit}
import amf.core.parser.{ParsedReference, ParserContext}
import amf.core.remote._
import amf.core.unsafe.PlatformSecrets
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.JsonSchemaPlugin
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.{JsonSchemaAstIndex, OasWebApiContext}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.declaration.{
  JSONSchemaDraft3SchemaVersion,
  JSONSchemaDraft4SchemaVersion,
  JSONSchemaUnspecifiedVersion,
  JSONSchemaVersion
}
import amf.plugins.document.webapi.parser.spec.domain.OasParameter
import amf.plugins.domain.shapes.models.AnyShape
import amf.validation.DialectValidations.ClosedShapeSpecification
import amf.validations.ParserSideValidations.InvalidJsonSchemaVersion
import org.yaml.model._

abstract class WebApiContext(val loc: String,
                             refs: Seq[ParsedReference],
                             val options: ParsingOptions,
                             private val wrapped: ParserContext,
                             private val ds: Option[WebApiDeclarations] = None)
    extends ParserContext(loc, refs, wrapped.futureDeclarations, wrapped.eh)
    with SpecAwareContext
    with PlatformSecrets {

  val syntax: SpecSyntax
  val vendor: Vendor

  val declarations: WebApiDeclarations =
    ds.getOrElse(new WebApiDeclarations(None, errorHandler = eh, futureDeclarations = futureDeclarations))

  var localJSONSchemaContext: Option[YNode] = wrapped match {
    case wac: WebApiContext => wac.localJSONSchemaContext
    case _                  => None
  }

  private var jsonSchemaIndex: Option[JsonSchemaAstIndex] = wrapped match {
    case wac: WebApiContext => wac.jsonSchemaIndex
    case _                  => None
  }

  def setJsonSchemaAST(value: YNode): Unit = {
    localJSONSchemaContext = Some(value)
    jsonSchemaIndex = Some(new JsonSchemaAstIndex(value)(this))
  }

  globalSpace = wrapped.globalSpace
  reportDisambiguation = wrapped.reportDisambiguation

  // JSON Schema has a global namespace

  protected def normalizedJsonPointer(url: String): String = if (url.endsWith("/")) url.dropRight(1) else url

  def findJsonSchema(url: String): Option[AnyShape] = globalSpace.get(normalizedJsonPointer(url)) match {
    case Some(shape: AnyShape) => Some(shape)
    case _                     => None
  }
  def registerJsonSchema(url: String, shape: AnyShape): Unit = {
    globalSpace.update(normalizedJsonPointer(url), shape)
  }

  // TODO this should not have OasWebApiContext as a dependency
  def parseRemoteJSONPath(fileUrl: String)(implicit ctx: OasLikeWebApiContext): Option[AnyShape] = {
    val referenceUrl =
      fileUrl.split("#") match {
        case s: Array[String] if s.size > 1 => Some(s.last)
        case _                              => None
      }
    val baseFileUrl = fileUrl.split("#").head
    val res: Option[Option[AnyShape]] = refs
      .filter(r => r.unit.location().isDefined)
      .filter(_.unit.location().get == baseFileUrl) collectFirst {
      case ref if ref.unit.isInstanceOf[ExternalFragment] =>
        val jsonFile = ref.unit.asInstanceOf[ExternalFragment]
        JsonSchemaPlugin.parseFragment(jsonFile, referenceUrl)
      case ref if ref.unit.isInstanceOf[RecursiveUnit] =>
        val jsonFile = ref.unit.asInstanceOf[RecursiveUnit]
        JsonSchemaPlugin.parseFragment(jsonFile, referenceUrl)
    }
    res.flatten
  }

  // TODO this should not have OasWebApiContext as a dependency
  def parseRemoteOasParameter(fileUrl: String, parentId: String)(
      implicit ctx: OasWebApiContext): Option[OasParameter] = {
    val referenceUrl = getReferenceUrl(fileUrl)
    obtainFragment(fileUrl) flatMap { fragment =>
      JsonSchemaPlugin.parseParameterFragment(fragment, referenceUrl, parentId)
    }
  }

  def obtainRemoteYNode(ref: String)(implicit ctx: WebApiContext): Option[YNode] = {
    val fileUrl      = ctx.resolvedPath(ctx.rootContextDocument, ref)
    val referenceUrl = getReferenceUrl(fileUrl)
    obtainFragment(fileUrl) flatMap { fragment =>
      JsonSchemaPlugin.obtainRootAst(fragment, referenceUrl)
    }
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

  def computeJsonSchemaVersion(rootAst: YNode): JSONSchemaVersion = {
    rootAst.value match {
      case map: YMap =>
        map.map.get("$schema") match {
          case Some(node) =>
            node.value match {
              case scalar: YScalar =>
                scalar.text match {
                  case txt if txt.contains("http://json-schema.org/draft-01/schema") =>
                    JSONSchemaDraft3SchemaVersion // 1 -> 3
                  case txt if txt.contains("http://json-schema.org/draft-02/schema") =>
                    JSONSchemaDraft3SchemaVersion // 2 -> 3
                  case txt if txt.contains("http://json-schema.org/draft-03/schema") => JSONSchemaDraft3SchemaVersion
                  case _                                                             => JSONSchemaDraft4SchemaVersion // we upgrade anything else to 4
                }
              case _ =>
                eh.violation(InvalidJsonSchemaVersion, "", "JSON Schema version value must be a string", node)
                JSONSchemaDraft4SchemaVersion
            }
          case _ => JSONSchemaDraft4SchemaVersion
        }
      case _ => JSONSchemaUnspecifiedVersion
    }
  }

  def resolvedPath(base: String, str: String): String = {
    if (str.isEmpty) platform.normalizePath(base)
    else if (str.startsWith("/")) str
    else if (str.contains(":")) str
    else if (str.startsWith("#")) base.split("#").head + str
    else platform.normalizePath(basePath(base).urlDecoded + str)
  }

  def basePath(path: String): String = {
    val withoutHash = if (path.contains("#")) path.split("#").head else path
    withoutHash.splitAt(withoutHash.lastIndexOf("/"))._1 + "/"
  }

  private def normalizeJsonPath(path: String): String = {
    if (path == "#" || path == "" || path == "/") "/" // exception root cases
    else {
      val s = if (path.startsWith("#")) path.replace("#", "") else path
      if (s.startsWith("/")) s.stripPrefix("/") else s
    }
  }
  def findLocalJSONPath(path: String): Option[(String, YNode)] = {
    // todo: past uri?
    jsonSchemaIndex match {
      case Some(jsi) => jsi.getNode(normalizeJsonPath(path)).map { (path, _) }
      case _         => None

    }
  }

  def link(node: YNode): Either[String, YNode]
  def ignore(shape: String, property: String): Boolean

  /** Validate closed shape. */
  def closedShape(node: String, ast: YMap, shape: String): Unit =
    syntax.nodes.get(shape) match {
      case Some(properties) =>
        ast.entries.foreach { entry =>
          val key: String = entry.key.asOption[YScalar].map(_.text).getOrElse(entry.key.toString)
          if (ignore(shape, key)) {
            // annotation or path in endpoint/webapi => ignore
          } else if (!properties(key)) {
            eh.violation(ClosedShapeSpecification,
                         node,
                         s"Property '$key' not supported in a $vendor $shape node",
                         entry)
          }
        }
      case None =>
        eh.violation(ClosedShapeSpecification, node, s"Cannot validate unknown node type $shape for $vendor", ast)
    }
}
