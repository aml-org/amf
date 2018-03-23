package amf.plugins.document.webapi.contexts

import amf.core.model.domain.Shape
import amf.core.parser.{ParserContext, YMapOps}
import amf.core.remote.{Oas, Raml08, Raml10, Vendor}
import amf.plugins.document.webapi.parser.spec.oas.OasSyntax
import amf.plugins.document.webapi.parser.spec.raml.{Raml08Syntax, Raml10Syntax}
import amf.plugins.document.webapi.parser.spec.{SpecSyntax, WebApiDeclarations}
import amf.plugins.features.validation.ParserSideValidations.ClosedShapeSpecification
import org.yaml.model._

class Raml10WebApiContext(private val wrapped: ParserContext, private val ds: Option[WebApiDeclarations] = None)
    extends RamlWebApiContext(wrapped, ds) {
  override val factory: RamlSpecVersionFactory = new Raml10VersionFactory()(this)
  override val vendor: Vendor                  = Raml10
  override val syntax: SpecSyntax              = Raml10Syntax
}

class Raml08WebApiContext(private val wrapped: ParserContext, private val ds: Option[WebApiDeclarations] = None)
    extends RamlWebApiContext(wrapped, ds) {
  override val factory: RamlSpecVersionFactory = new Raml08VersionFactory()(this)
  override val vendor: Vendor                  = Raml08
  override val syntax: SpecSyntax              = Raml08Syntax
}

abstract class RamlWebApiContext(private val wrapped: ParserContext, private val ds: Option[WebApiDeclarations] = None)
    extends WebApiContext(wrapped, ds)
    with RamlSpecAwareContext {

  override def link(node: YNode): Either[String, YNode] = {
    node match {
      case _ if isInclude(node) => Left(node.as[YScalar].text)
      case _                    => Right(node)
    }
  }

  override def ignore(shape: String, property: String): Boolean =
    (property.startsWith("(") && property.endsWith(")")) || (property.startsWith("/") && (shape == "webApi" || shape == "endPoint"))

  private def isInclude(node: YNode) = node.tagType == YType.Include

  val factory: RamlSpecVersionFactory // todo: move up to SpecAwareContext for oas 2.0 and 3.0??

  /**
    * raml types nodes are different from other shapes because they can have 'custom facets' essentially, client
    * defined constraints expressed as additional properties syntactically in the type definition.
    * The problem is that they cannot be recognised just looking into the AST as we do with annotations, so we
    * need to first, compute them, and then, add them as additional valid properties to the set of properties that
    * can be defined in the AST node
    */
  def closedRamlTypeShape(shape: Shape, ast: YMap, shapeType: String, annotation: Boolean = false): Unit = {
    val node   = shape.id
    val facets = shape.collectCustomShapePropertyDefinitions(onlyInherited = true)

    syntax.nodes.get(shapeType) match {
      case Some(props) =>
        val initialProperties = if (annotation) {
          props ++ syntax.nodes("annotation")
        } else {
          props
        }
        val allResults: Seq[Seq[YMapEntry]] = facets.map { propertiesMap =>
          val totalProperties     = initialProperties ++ propertiesMap.keys.toSet
          val acc: Seq[YMapEntry] = Seq.empty
          ast.entries.foldLeft(acc) { (results: Seq[YMapEntry], entry) =>
            val key: String = entry.key
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
            violation(ClosedShapeSpecification.id(),
                      node,
                      s"Properties ${errors.map(_.key).mkString(",")} not supported in a $vendor $shapeType node",
                      errors.head) // pointing only to the first failed error
        }

      case None => throw new Exception(s"Cannot validate unknown node type $shapeType for $vendor")
    }
  }
}

class OasWebApiContext(private val wrapped: ParserContext, private val ds: Option[WebApiDeclarations] = None)
    extends WebApiContext(wrapped, ds) {

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

  override def ignore(shape: String, property: String): Boolean =
    property.startsWith("x-") || property == "$ref" || (property.startsWith("/") && shape == "webApi")

  override val syntax: SpecSyntax = OasSyntax
  override val vendor: Vendor     = Oas
}

abstract class WebApiContext(private val wrapped: ParserContext, private val ds: Option[WebApiDeclarations] = None)
    extends ParserContext(wrapped.rootContextDocument,
                          wrapped.refs,
                          wrapped.futureDeclarations,
                          parserCount = wrapped.parserCount)
    with SpecAwareContext {

  val syntax: SpecSyntax
  val vendor: Vendor

  val declarations: WebApiDeclarations =
    ds.getOrElse(new WebApiDeclarations(None, errorHandler = Some(this), futureDeclarations = futureDeclarations))

  var localJSONSchemaContext: Option[YNode] = wrapped match {
    case (wac: WebApiContext) => wac.localJSONSchemaContext
    case _                    => None
  }

  def findLocalJSONPath(path: String): Option[(String, YNode)] = {
    localJSONSchemaContext match {
      case None => None
      case Some(schema) =>
        var tmp: YNode = schema
        var name       = "schema"
        var parts      = path.replace("#", "").split("/").filter(_ != "")
        var error      = false

        while (parts.nonEmpty && !error) {
          tmp.tagType match {
            case YType.Map => {
              val nextPart = parts.head
              parts = parts.tail
              val map = tmp.as[YMap]
              map.key(nextPart) match {
                case Some(entry) =>
                  name = nextPart
                  tmp = entry.value
                case _ =>
                  error = true
              }
            }
            case _ =>
              error = true
          }
        }

        if (!error) Some((name, tmp)) else None
    }
  }

  def link(node: YNode): Either[String, YNode]
  def ignore(shape: String, property: String): Boolean

  /** Validate closed shape. */
  def closedShape(node: String, ast: YMap, shape: String, annotation: Boolean = false): Unit = {
    syntax.nodes.get(shape) match {
      case Some(props) =>
        val properties = if (annotation) {
          props ++ syntax.nodes("annotation")
        } else {
          props
        }

        ast.entries.foreach { entry =>
          val key: String = entry.key
          if (ignore(shape, key)) {
            // annotation or path in endpoint/webapi => ignore
          } else if (!properties(key)) {
            violation(ClosedShapeSpecification.id(),
                      node,
                      s"Property $key not supported in a $vendor $shape node",
                      entry)
          }
        }
      case None => throw new Exception(s"Cannot validate unknown node type $shape for $vendor")
    }
  }
}
