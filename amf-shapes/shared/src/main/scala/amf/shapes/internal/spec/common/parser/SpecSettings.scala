package amf.shapes.internal.spec.common.parser

import amf.aml.internal.parse.dialects.DialectAstOps.DialectYMapOps
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.parse.document.{ErrorHandlingContext, ParserContext}
import amf.core.internal.plugins.syntax.{SYamlAMFParserErrorHandler, SyamlAMFErrorHandler}
import amf.core.internal.remote.{JsonSchema, Spec}
import amf.core.internal.remote.Spec.{ASYNC20, OAS20, OAS30}
import amf.shapes.internal.spec.RamlWebApiContextType
import amf.shapes.internal.spec.RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.spec.common.{
  JSONSchemaDraft4SchemaVersion,
  JSONSchemaDraft7SchemaVersion,
  OAS20SchemaVersion,
  OAS30SchemaVersion,
  SchemaPosition,
  SchemaVersion
}
import org.yaml.model.{IllegalTypeHandler, YMap, YNode, YScalar, YType}

trait SpecSettings {
  val spec: Spec
  val syntax: SpecSyntax
  def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode]
  def ignoreCriteria: IgnoreCriteria
  def isOasLikeContext: Boolean = isOas3Context || isOas2Context || isAsyncContext
  def isOas2Context: Boolean    = spec == Spec.OAS20
  def isOas3Context: Boolean    = spec == OAS30
  def isAsyncContext: Boolean   = spec == Spec.ASYNC20
  def isRamlContext: Boolean    = spec == Spec.RAML10 || spec == Spec.RAML08
  def ramlContextType: Option[RamlWebApiContextType]
  val defaultSchemaVersion: SchemaVersion
  def closedShapeValidator: ClosedShapeValidator = DefaultClosedShapeValidator(ignoreCriteria, spec, syntax)
  def shouldLinkTypes(parent: ParserContext)     = false
  val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder
}

object OasLink {
  def getLinkValue(node: YNode)(implicit eh: SyamlAMFErrorHandler) = {
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
}

object RamlLink {
  def link(node: YNode)(implicit eh: AMFErrorHandler): Either[String, YNode] = {
    implicit val errorHandler: IllegalTypeHandler = new SYamlAMFParserErrorHandler(eh)

    node match {
      case _ if isInclude(node) => Left(node.as[YScalar].text)
      case _                    => Right(node)
    }
  }

  private def isInclude(node: YNode) = node.tagType == YType.Include
}

case class Oas2Settings(syntax: SpecSyntax) extends SpecSettings {
  override val spec: Spec = OAS20

  override def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode] = OasLink.getLinkValue(node)

  override def ignoreCriteria: IgnoreCriteria = OasLikeIgnoreCriteria

  override def ramlContextType: Option[RamlWebApiContextType] = None

  override val defaultSchemaVersion: SchemaVersion = OAS20SchemaVersion.apply(SchemaPosition.Other)
  override val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder = IgnoreAnnotationSchemaValidatorBuilder

  override def shouldLinkTypes(parent: ParserContext) = parent match {
    case ctx: CommonShapeParseContext if ctx.isRamlContext => false
    case _                                                 => true
  }
}

case class Oas3Settings(syntax: SpecSyntax) extends SpecSettings {
  override val spec: Spec = OAS30

  override def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode] = OasLink.getLinkValue(node)

  override def ignoreCriteria: IgnoreCriteria = OasLikeIgnoreCriteria

  override def ramlContextType: Option[RamlWebApiContextType] = None

  override val defaultSchemaVersion: SchemaVersion = OAS30SchemaVersion.apply(SchemaPosition.Other)
  override val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder = IgnoreAnnotationSchemaValidatorBuilder

  override def shouldLinkTypes(parent: ParserContext) = parent match {
    case ctx: CommonShapeParseContext if ctx.isRamlContext => false
    case _                                                 => true
  }
}

case class Async2Settings(syntax: SpecSyntax) extends SpecSettings {
  override val spec: Spec = ASYNC20

  override def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode] = OasLink.getLinkValue(node)

  override def ignoreCriteria: IgnoreCriteria = OasLikeIgnoreCriteria

  override def ramlContextType: Option[RamlWebApiContextType] = None

  override val defaultSchemaVersion: SchemaVersion                          = JSONSchemaDraft7SchemaVersion
  override val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder = IgnoreAnnotationSchemaValidatorBuilder

  override def shouldLinkTypes(parent: ParserContext) = parent match {
    case ctx: CommonShapeParseContext if ctx.isRamlContext => false
    case _                                                 => true
  }
}

case class JsonSchemaSettings(syntax: SpecSyntax, defaultSchemaVersion: SchemaVersion) extends SpecSettings {
  override val spec: Spec = JsonSchema

  override def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode] = OasLink.getLinkValue(node)

  override def ignoreCriteria: IgnoreCriteria = OasLikeIgnoreCriteria

  override def ramlContextType: Option[RamlWebApiContextType]               = None
  override val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder = IgnoreAnnotationSchemaValidatorBuilder

  override def shouldLinkTypes(parent: ParserContext) = parent match {
    case ctx: CommonShapeParseContext if ctx.isOas2Context || ctx.isOas3Context => true
    case ctx: CommonShapeParseContext if ctx.isRamlContext                      => false
    case _                                                                      => false
  }
}

class Raml10Settings(
    val syntax: SpecSyntax,
    private var contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT
) extends SpecSettings {
  override val spec: Spec = Spec.RAML10

  override def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode] = RamlLink.link(node)

  override def ignoreCriteria: IgnoreCriteria = Raml10IgnoreCriteria

  override def ramlContextType: Option[RamlWebApiContextType] = Some(contextType)

  def setRamlContextType(ramlContextType: RamlWebApiContextType): Unit = {
    this.contextType = ramlContextType
  }

  override val defaultSchemaVersion: SchemaVersion                          = JSONSchemaDraft4SchemaVersion
  override val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder = DeclaredAnnotationSchemaValidatorBuilder
}

class Raml08Settings(val syntax: SpecSyntax, var contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT)
    extends SpecSettings {
  override val spec: Spec = Spec.RAML08

  override def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode] = RamlLink.link(node)

  override def ignoreCriteria: IgnoreCriteria = Raml08IgnoreCriteria

  override def ramlContextType: Option[RamlWebApiContextType] = Some(contextType)

  def setRamlContextType(ramlContextType: RamlWebApiContextType): Unit = {
    this.contextType = ramlContextType
  }

  override val defaultSchemaVersion: SchemaVersion                          = JSONSchemaDraft4SchemaVersion
  override val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder = DeclaredAnnotationSchemaValidatorBuilder
}

object OasLikeIgnoreCriteria extends IgnoreCriteria {

  private val shapesThatDontPermitRef = List("paths", "operation")

  override def shouldIgnore(shape: String, property: String): Boolean = {
    property.startsWith("x-") || (property == "$ref" && !shapesThatDontPermitRef.contains(shape)) || (property
      .startsWith("/") && shape == "paths")
  }
}

trait RamlIgnoreCriteria extends IgnoreCriteria {

  protected val supportsAnnotations: Boolean

  override def shouldIgnore(shape: String, property: String): Boolean = {
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
}

object Raml10IgnoreCriteria extends RamlIgnoreCriteria {
  override protected val supportsAnnotations: Boolean = true
}

object Raml08IgnoreCriteria extends RamlIgnoreCriteria {
  override protected val supportsAnnotations: Boolean = false
}

object Raml10ShapeSyntax extends SpecSyntax {

  private val shapeFacets = Set(
    "type",
    "default",
    "schema",
    "example",
    "examples",
    "displayName",
    "description",
    "facets",
    "xml",
    "enum"
  )

  override val nodes = Map(
    "shape"    -> shapeFacets,
    "anyShape" -> shapeFacets,
    "schemaShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "required"
    ),
    "unionShape" -> (shapeFacets + "anyOf"),
    "nodeShape" -> (shapeFacets ++ Set(
      "properties",
      "minProperties",
      "maxProperties",
      "discriminator",
      "discriminatorValue",
      "additionalProperties"
    )),
    "arrayShape" -> (shapeFacets ++ Set(
      "uniqueItems",
      "items",
      "minItems",
      "maxItems"
    )),
    "stringScalarShape" -> (shapeFacets ++ Set(
      "pattern",
      "minLength",
      "maxLength"
    )),
    "numberScalarShape" -> (shapeFacets ++ Set(
      "minimum",
      "maximum",
      "format",
      "multipleOf"
    )),
    "dateScalarShape" -> (shapeFacets + "format"),
    "fileShape" -> (shapeFacets ++ Set(
      "fileTypes",
      "minLength",
      "maxLength"
    )),
    "example" -> Set(
      "displayName",
      "description",
      "value",
      "strict"
    ),
    "xmlSerialization" -> Set(
      "attribute",
      "wrapped",
      "name",
      "namespace",
      "prefix"
    ),
    "property" -> Set(
      "required"
    ),
    "annotation" -> Set(
      "displayName",
      "description",
      "allowedTargets"
    )
  )
}

object Raml08ShapeSyntax extends SpecSyntax {

  override val nodes = Map(
    "shape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "enum",
      "required",
      "repeat"
    ),
    "stringScalarShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "enum",
      "repeat",
      "pattern",
      "minLength",
      "maxLength",
      "required"
    ),
    "dateScalarShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "enum",
      "required",
      "repeat",
      "format"
    ),
    "numberScalarShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "enum",
      "required",
      "repeat",
      "minimum",
      "maximum",
      "format",
      "multipleOf"
    )
  )
}

object Oas2ShapeSyntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = Map(
    "xmlSerialization" -> Set(
      "attribute",
      "wrapped",
      "name",
      "namespace",
      "prefix"
    ),
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
      "xml",
      "externalDocs",
      "allOf",
      "anyOf",
      "not",
      "dependencies",
      "multipleOf",
      "default",
      "example",
      "id",
      "name",
      "patternProperties"
    ),
    "parameter" -> Set(
      "name",
      "in",
      "description",
      "required",
      "type",
      "format",
      "allowEmptyValue",
      "items",
      "collectionFormat",
      "default",
      "maximum",
      "exclusiveMaximum",
      "minimum",
      "exclusiveMinimum",
      "maxLength",
      "minLength",
      "pattern",
      "maxItems",
      "minItems",
      "uniqueItems",
      "enum",
      "multipleOf",
      "deprecated",
      "example"
    )
  )
}

object Oas3ShapeSyntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = Map(
    "xmlSerialization" -> Set(
      "attribute",
      "wrapped",
      "name",
      "namespace",
      "prefix"
    ),
    "example" -> Set(
      "summary",
      "description",
      "value",
      "externalValue"
    ),
    "discriminator" -> Set(
      "propertyName",
      "mapping"
    ),
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

object Async20ShapeSyntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = Map(
    // Async Schema Object
    "schema" -> Set(
      "$ref",
      "$schema",
      "$comment",
      "$id",
      "format",
      "title",
      "description",
      "maximum",
      "exclusiveMaximum",
      "minimum",
      "exclusiveMinimum",
      "maxLength",
      "minLength",
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
      "propertyNames",
      "discriminator",
      "readOnly",
      "writeOnly",
      "deprecated",
      "externalDocs",
      "allOf",
      "anyOf",
      "oneOf",
      "not",
      "dependencies",
      "multipleOf",
      "default",
      "examples",
      "if",
      "then",
      "else",
      "const",
      "contains",
      "name",
      "patternProperties"
    )
  )
}
