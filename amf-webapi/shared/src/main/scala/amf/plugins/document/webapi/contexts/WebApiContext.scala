package amf.plugins.document.webapi.contexts

import amf.core.client.ParsingOptions
import amf.core.model.document.{ExternalFragment, Fragment, RecursiveUnit}
import amf.core.model.domain.Shape
import amf.core.parser.{ErrorHandler, ParsedReference, ParserContext, YMapOps}
import amf.core.remote._
import amf.core.unsafe.PlatformSecrets
import amf.core.utils.Strings
import amf.plugins.document.webapi.JsonSchemaPlugin
import amf.plugins.document.webapi.contexts.RamlWebApiContextType.RamlWebApiContextType
import amf.plugins.document.webapi.parser.RamlShapeTypeBeautifier
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.declaration.{
  JSONSchemaDraft3SchemaVersion,
  JSONSchemaDraft4SchemaVersion,
  JSONSchemaUnspecifiedVersion,
  JSONSchemaVersion,
  TypeInfo
}
import amf.plugins.document.webapi.parser.spec.domain.OasParameter
import amf.plugins.document.webapi.parser.spec.oas.{Oas2Syntax, Oas3Syntax}
import amf.plugins.document.webapi.parser.spec.raml.{Raml08Syntax, Raml10Syntax}
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.features.validation.CoreValidations.DeclarationNotFound
import amf.validation.DialectValidations.ClosedShapeSpecification
import amf.validations.ParserSideValidations.InvalidJsonSchemaVersion
import org.yaml.model._

import scala.collection.mutable

class PayloadContext(loc: String,
                     refs: Seq[ParsedReference],
                     override val wrapped: ParserContext,
                     private val ds: Option[RamlWebApiDeclarations] = None,
                     override val eh: Option[ErrorHandler] = None,
                     parserCount: Option[Int] = None,
                     contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT,
                     options: ParsingOptions = ParsingOptions())
    extends RamlWebApiContext(loc, refs, options, wrapped, ds, parserCount, eh, contextType = contextType) {
  override protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext = {
    new PayloadContext(loc, refs, wrapped, Some(declarations), eh, options = options)
  }
  override val factory: RamlSpecVersionFactory = new Raml10VersionFactory()(this)
  override val syntax: SpecSyntax = new SpecSyntax {
    override val nodes: Map[String, Set[String]] = Map()
  }
  override val vendor: Vendor = Payload
}

class Raml10WebApiContext(loc: String,
                          refs: Seq[ParsedReference],
                          override val wrapped: ParserContext,
                          private val ds: Option[RamlWebApiDeclarations] = None,
                          parserCount: Option[Int] = None,
                          override val eh: Option[ErrorHandler] = None,
                          contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT,
                          options: ParsingOptions = ParsingOptions())
    extends RamlWebApiContext(loc, refs, options, wrapped, ds, parserCount, eh, contextType) {
  override val factory: RamlSpecVersionFactory = new Raml10VersionFactory()(this)
  override val vendor: Vendor                  = Raml10
  override val syntax: SpecSyntax              = Raml10Syntax

  override protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext =
    new Raml10WebApiContext(loc, refs, wrapped, Some(declarations), eh = eh, options = options)
}

class Raml08WebApiContext(loc: String,
                          refs: Seq[ParsedReference],
                          override val wrapped: ParserContext,
                          private val ds: Option[RamlWebApiDeclarations] = None,
                          parserCount: Option[Int] = None,
                          override val eh: Option[ErrorHandler] = None,
                          contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT,
                          options: ParsingOptions = ParsingOptions())
    extends RamlWebApiContext(loc, refs, options, wrapped, ds, parserCount, eh, contextType) {
  override val factory: RamlSpecVersionFactory = new Raml08VersionFactory()(this)
  override val vendor: Vendor                  = Raml08
  override val syntax: SpecSyntax              = Raml08Syntax

  override protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext =
    new Raml08WebApiContext(loc, refs, wrapped, Some(declarations), eh = eh, options = options)

  override protected def supportsAnnotations: Boolean = false
}

abstract class RamlWebApiContext(override val loc: String,
                                 refs: Seq[ParsedReference],
                                 options: ParsingOptions,
                                 val wrapped: ParserContext,
                                 private val ds: Option[RamlWebApiDeclarations] = None,
                                 parserCount: Option[Int] = None,
                                 override val eh: Option[ErrorHandler] = None,
                                 var contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT)
    extends WebApiContext(loc, refs, options, wrapped, ds, parserCount, eh)
    with RamlSpecAwareContext {

  var globalMediatype: Boolean                                  = false
  val operationContexts: mutable.Map[String, RamlWebApiContext] = mutable.Map()

  def mergeOperationContext(operation: String): Unit = {
    val contextOption = operationContexts.get(operation)
    contextOption.foreach(mergeContext)
    operationContexts.remove(operation)
  }

  def mergeAllOperationContexts(): Unit = {
    operationContexts.values.foreach(mergeContext)
    operationContexts.keys.foreach(operationContexts.remove)
  }
  def mergeContext(subContext: RamlWebApiContext): Unit = {
    declarations.absorb(subContext.declarations)
    subContext.declarations.futureDeclarations.promises.foreach(fd => declarations.futureDeclarations.promises += fd)
    subContext.futureDeclarations.promises.foreach(fd => futureDeclarations.promises += fd)
  }

  override val declarations: RamlWebApiDeclarations = ds.getOrElse(
    new RamlWebApiDeclarations(alias = None, errorHandler = Some(this), futureDeclarations = futureDeclarations))
  protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext

  /**
    * Adapt this context for a nested library, used when evaluating resource type / traits
    * Using a path to the library whose context is going to be looked up, e.g. lib.TypeA
    */
  def adapt[T](path: String)(k: RamlWebApiContext => T): T = {
    val pathElements        = path.split("\\.").dropRight(1)
    val adaptedDeclarations = findDeclarations(pathElements, declarations)
    k(clone(declarations.merge(adaptedDeclarations)))
  }

  protected def findDeclarations(path: Seq[String], declarations: RamlWebApiDeclarations): RamlWebApiDeclarations = {
    if (path.isEmpty) {
      declarations
    } else {
      val nextLibrary = path.head
      declarations.libraries.get(nextLibrary) match {
        case Some(library: RamlWebApiDeclarations) =>
          findDeclarations(path.tail, library)
        case _ =>
          violation(DeclarationNotFound,
                    "",
                    None,
                    s"Cannot find declarations in context '${path.mkString(".")}",
                    None,
                    None)
          declarations
      }
    }
  }

  override def link(node: YNode): Either[String, YNode] = {
    node match {
      case _ if isInclude(node) => Left(node.as[YScalar].text)
      case _                    => Right(node)
    }
  }

  protected def supportsAnnotations = true

  override def ignore(shape: String, property: String): Boolean = {
    def isAnnotation = supportsAnnotations && property.startsWith("(") && property.endsWith(")")

    def isAllowedNestedEndpoint = {
      val shapesIgnoringNestedEndpoints = "webApi" :: "endPoint" :: Nil
      property.startsWith("/") && shapesIgnoringNestedEndpoints.contains(shape)
    }

    def reportedByOtherConstraint = {
      val nestedEndpointsConstraintShapes = "resourceType" :: Nil
      property.startsWith("/") && nestedEndpointsConstraintShapes.contains(shape)
    }

    def isAllowedParameter = {
      val shapesWithParameters = "resourceType" :: "trait" :: Nil
      property.matches("<<.+>>") && shapesWithParameters.contains(shape)
    }

    isAnnotation || isAllowedNestedEndpoint || isAllowedParameter || reportedByOtherConstraint
  }

  private def isInclude(node: YNode) = node.tagType == YType.Include

  val factory: RamlSpecVersionFactory

  /**
    * raml types nodes are different from other shapes because they can have 'custom facets' essentially, client
    * defined constraints expressed as additional properties syntactically in the type definition.
    * The problem is that they cannot be recognised just looking into the AST as we do with annotations, so we
    * need to first, compute them, and then, add them as additional valid properties to the set of properties that
    * can be defined in the AST node
    */
  def closedRamlTypeShape(shape: Shape, ast: YMap, shapeType: String, typeInfo: TypeInfo): Unit = {
    val node       = shape.id
    val facets     = shape.collectCustomShapePropertyDefinitions(onlyInherited = true)
    val shapeLabel = RamlShapeTypeBeautifier.beautify(shapeType)

    syntax.nodes.get(shapeType) match {
      case Some(props) =>
        var initialProperties = props
        if (typeInfo.isAnnotation) initialProperties ++= syntax.nodes("annotation")
        if (typeInfo.isPropertyOrParameter) initialProperties ++= syntax.nodes("property")
        val allResults: Seq[Seq[YMapEntry]] = facets.map { propertiesMap =>
          val totalProperties     = initialProperties ++ propertiesMap.keys.toSet
          val acc: Seq[YMapEntry] = Seq.empty
          ast.entries.foldLeft(acc) { (results: Seq[YMapEntry], entry) =>
            val key: String = entry.key.as[YScalar].text
            if (ignore(shapeType, key)) {
              results
            } else if (!totalProperties(key)) {
              results ++ Seq(entry)
            } else {
              results
            }
          }
        }
        allResults.find(_.nonEmpty) match {
          case None => // at least we found a solution, this is a valid shape
          case Some(errors: Seq[YMapEntry]) =>
            val subject = if (errors.size > 1) "Properties" else "Property"
            violation(
              ClosedShapeSpecification,
              node,
              s"$subject ${errors.map(_.key.as[YScalar].text).map(e => s"'$e'").mkString(",")} not supported in a $vendor $shapeLabel node",
              errors.head
            ) // pointing only to the first failed error
        }

      case None =>
        violation(
          ClosedShapeSpecification,
          node,
          s"Cannot validate unknown node type $shapeType for $vendor",
          shape.annotations
        )
    }
  }
}

class ExtensionLikeWebApiContext(loc: String,
                                 refs: Seq[ParsedReference],
                                 override val wrapped: ParserContext,
                                 val ds: Option[RamlWebApiDeclarations] = None,
                                 val parentDeclarations: RamlWebApiDeclarations,
                                 parserCount: Option[Int] = None,
                                 contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT,
                                 options: ParsingOptions = ParsingOptions())
    extends Raml10WebApiContext(loc,
                                refs,
                                wrapped,
                                ds,
                                parserCount = parserCount,
                                contextType = contextType,
                                options = options) {

  override val declarations: ExtensionWebApiDeclarations =
    ds match {
      case Some(dec) =>
        new ExtensionWebApiDeclarations(dec.externalShapes,
                                        dec.externalLibs,
                                        parentDeclarations,
                                        dec.alias,
                                        dec.errorHandler,
                                        dec.futureDeclarations)
      case None =>
        new ExtensionWebApiDeclarations(parentDeclarations = parentDeclarations,
                                        alias = None,
                                        errorHandler = Some(this),
                                        futureDeclarations = futureDeclarations)
    }

  override protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext =
    new ExtensionLikeWebApiContext(loc, refs, wrapped, Some(declarations), parentDeclarations)
}

abstract class OasWebApiContext(loc: String,
                                refs: Seq[ParsedReference],
                                options: ParsingOptions,
                                private val wrapped: ParserContext,
                                private val ds: Option[OasWebApiDeclarations] = None,
                                parserCount: Option[Int] = None,
                                override val eh: Option[ErrorHandler] = None,
                                private val operationIds: mutable.Set[String] = mutable.HashSet())
    extends WebApiContext(loc, refs, options, wrapped, ds, parserCount, eh) {

  override val declarations: OasWebApiDeclarations =
    ds.getOrElse(
      new OasWebApiDeclarations(
        refs
          .flatMap(
            r =>
              if (r.isExternalFragment)
                r.unit.asInstanceOf[ExternalFragment].encodes.parsed.map(node => r.origin.url -> node)
              else None)
          .toMap,
        None,
        errorHandler = Some(this),
        futureDeclarations = futureDeclarations
      ))
  val factory: OasSpecVersionFactory

  override def link(node: YNode): Either[String, YNode] = {
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

  val linkTypes: Boolean = wrapped match {
    case _: RamlWebApiContext => false
    case _                    => true
  }
  override def ignore(shape: String, property: String): Boolean =
    property.startsWith("x-") || property == "$ref" || (property.startsWith("/") && (shape == "webApi" || shape == "paths"))

  /** Used for accumulating operation ids.
    * returns true if id was not present, and false if operation being added is already present. */
  def registerOperationId(id: String): Boolean = operationIds.add(id)
}

class Oas2WebApiContext(loc: String,
                        refs: Seq[ParsedReference],
                        private val wrapped: ParserContext,
                        private val ds: Option[OasWebApiDeclarations] = None,
                        parserCount: Option[Int] = None,
                        override val eh: Option[ErrorHandler] = None,
                        options: ParsingOptions = ParsingOptions())
    extends OasWebApiContext(loc, refs, options, wrapped, ds, parserCount, eh) {
  override val factory: Oas2VersionFactory = Oas2VersionFactory()(this)
  override val vendor: Vendor              = Oas20
  override val syntax: SpecSyntax          = Oas2Syntax
}

class Oas3WebApiContext(loc: String,
                        refs: Seq[ParsedReference],
                        private val wrapped: ParserContext,
                        private val ds: Option[OasWebApiDeclarations] = None,
                        parserCount: Option[Int] = None,
                        override val eh: Option[ErrorHandler] = None,
                        options: ParsingOptions)
    extends OasWebApiContext(loc, refs, options, wrapped, ds, parserCount, eh) {
  override val factory: Oas3VersionFactory = Oas3VersionFactory()(this)
  override val vendor: Vendor              = Oas30
  override val syntax: SpecSyntax          = Oas3Syntax
}

abstract class WebApiContext(val loc: String,
                             refs: Seq[ParsedReference],
                             val options: ParsingOptions,
                             private val wrapped: ParserContext,
                             private val ds: Option[WebApiDeclarations] = None,
                             parserCount: Option[Int] = None,
                             override val eh: Option[ErrorHandler])
    extends ParserContext(loc,
                          refs,
                          wrapped.futureDeclarations,
                          parserCount = parserCount.getOrElse(wrapped.parserCount),
                          eh.orElse(wrapped.eh))
    with SpecAwareContext
    with PlatformSecrets {

  val syntax: SpecSyntax
  val vendor: Vendor

  val declarations: WebApiDeclarations =
    ds.getOrElse(new WebApiDeclarations(None, errorHandler = Some(this), futureDeclarations = futureDeclarations))

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

  def parseRemoteJSONPath(fileUrl: String)(implicit ctx: OasWebApiContext): Option[AnyShape] = {
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
                violation(InvalidJsonSchemaVersion, "", "JSON Schema version value must be a string", node)
                JSONSchemaDraft4SchemaVersion
            }
          case _ => JSONSchemaUnspecifiedVersion
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
            violation(ClosedShapeSpecification, node, s"Property '$key' not supported in a $vendor $shape node", entry)
          }
        }
      case None =>
        violation(ClosedShapeSpecification, node, s"Cannot validate unknown node type $shape for $vendor", ast)
    }
}

object RamlWebApiContextType extends Enumeration {
  type RamlWebApiContextType = Value
  val DEFAULT, RESOURCE_TYPE, TRAIT, EXTENSION, OVERLAY = Value
}
