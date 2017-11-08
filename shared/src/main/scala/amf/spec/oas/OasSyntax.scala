package amf.spec.oas

import amf.domain.Annotation.LexicalInformation
import amf.validation.model.ParserSideValidations
import amf.validation.model.ParserSideValidations.ClosedShapeSpecification
import amf.validation.{SeverityLevels, Validation, ValidationAware}
import amf.vocabulary.Namespace
import org.yaml.model.YMap

trait OasSyntax {

  val nodes: Map[String, Set[String]] = Map(
    "webApi" -> Set(
      "swagger",
      "info",
      "host",
      "basePath",
      "schemes",
      "consumes",
      "produces",
      "paths",
      "definitions",
      "parameters",
      "responses",
      "securityDefinitions",
      "security",
      "tags",
      "externalDocs"
    ),
    "info" -> Set(
      "title",
      "description",
      "termsOfService",
      "contact",
      "license",
      "version"
    ),
    "contact" -> Set(
      "name",
      "url",
      "email"
    ),
    "license" -> Set(
      "name",
      "url"
    ),
    "xmlSerialization" -> Set(
      "attribute",
      "wrapped",
      "name",
      "namespace",
      "prefix"
    ),
    "pathItem" -> Set(
      "get",
      "put",
      "post",
      "delete",
      "options",
      "head",
      "patch",
      "parameters",
      "$ref"
    ),
    "operation" -> Set(
      "tags",
      "summary",
      "description",
      "externalDocs",
      "operationId",
      "consumes",
      "produces",
      "parameters",
      "responses",
      "schemes",
      "deprecated",
      "security"
    ),
    "externalDoc" -> Set(
      "url"
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
      "multipleOf",
      "uniqueItems",
      "enum",
      "multipleOf",
      "items",
      "example"
    ),
    "bodyParameter" -> Set(
      "name",
      "in",
      "description",
      "required",
      "schema"
    ),
    "response" -> Set(
      "description",
      "schema",
      "headers",
      "examples"
    ),
    "headerParameter" -> Set(
      "description",
      "type",
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
      "multipleOf"
    ),
    "tag" -> Set(
      "name",
      "description",
      "externalDocs"
    ),
    "schema" -> Set(
      "$ref",
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
      "allOf",
      "properties",
      "additionalProperties",
      "discriminator",
      "readOnly",
      "xml",
      "externalDocs",
      "example",
      "allOf",
      "anyOf",
      "dependencies",
      "multipleOf",
      "default",
      "example"
    )
  )

  def validateClosedShape(validation: ValidationAware, node: String, ast: YMap, nodeType: String): Unit = {
    nodes.get(nodeType) match {
      case Some(properties) =>
        ast.entries.foreach { entry =>
          val key: String = entry.key
          if (key.startsWith("x-") || key == "$ref" || (key.startsWith("/") && nodeType == "webApi")) {
            // annotation or path in webapi => ignore
          } else if (!properties(key)) {
            validation.violation(ClosedShapeSpecification.id(),
                                 node,
                                 s"Property $key not supported in a OpenAPI $nodeType node",
                                 entry)
          }
        }
      case None => throw new Exception(s"Cannot validate unknown node type $nodeType")
    }
  }
}
