package amf.apicontract.internal.spec.raml.parser.context

import amf.apicontract.internal.spec.common.RamlWebApiDeclarations
import amf.apicontract.internal.spec.common.parser.{ParsingHelpers, WebApiContext}
import amf.apicontract.internal.validation.definitions.ParserSideValidations.ClosedShapeSpecification
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.plugins.syntax.SYamlAMFParserErrorHandler
import amf.core.internal.remote.Vendor
import amf.core.internal.validation.CoreValidations.DeclarationNotFound
import amf.shapes.internal.spec.RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.spec.common.parser.SpecSyntax
import amf.shapes.internal.spec.raml.parser.TypeInfo
import amf.shapes.internal.spec.{RamlShapeTypeBeautifier, RamlWebApiContextType}
import org.yaml.model._

import scala.collection.mutable

abstract class RamlWebApiContext(override val loc: String,
                                 refs: Seq[ParsedReference],
                                 options: ParsingOptions,
                                 val wrapped: ParserContext,
                                 private val ds: Option[RamlWebApiDeclarations] = None,
                                 var contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT)
    extends WebApiContext(loc, refs, options, wrapped, ds)
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

  override val declarations: RamlWebApiDeclarations =
    ds.getOrElse(new RamlWebApiDeclarations(alias = None, futureDeclarations = futureDeclarations, errorHandler = eh))
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
          violation(DeclarationNotFound, "", s"Cannot find declarations in context '${path.mkString(".")}")
          declarations
      }
    }
  }

  override def link(node: YNode): Either[String, YNode] = {
    implicit val errorHandler: IllegalTypeHandler = new SYamlAMFParserErrorHandler(eh)

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

    implicit val errorHandler: IllegalTypeHandler = new SYamlAMFParserErrorHandler(eh)

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
            eh.violation(
              ClosedShapeSpecification,
              node,
              s"$subject ${errors.map(_.key.as[YScalar].text).map(e => s"'$e'").mkString(",")} not supported in a $spec $shapeLabel node",
              errors.head.location
            ) // pointing only to the first failed error
        }

      case None =>
        eh.violation(
          ClosedShapeSpecification,
          node,
          s"Cannot validate unknown node type $shapeType for $spec",
          shape.annotations
        )
    }
  }

  override def autoGeneratedAnnotation(s: Shape): Unit = ParsingHelpers.ramlAutoGeneratedAnnotation(s)
}

class PayloadContext(loc: String,
                     refs: Seq[ParsedReference],
                     override val wrapped: ParserContext,
                     private val ds: Option[RamlWebApiDeclarations] = None,
                     contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT,
                     options: ParsingOptions = ParsingOptions())
    extends RamlWebApiContext(loc, refs, options, wrapped, ds, contextType = contextType) {
  override protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext = {
    new PayloadContext(loc, refs, wrapped, Some(declarations), options = options)
  }
  override val factory: RamlSpecVersionFactory = new Raml10VersionFactory()(this)
  override val syntax: SpecSyntax = new SpecSyntax {
    override val nodes: Map[String, Set[String]] = Map()
  }
  override val spec: Spec = Spec.PAYLOAD
}
