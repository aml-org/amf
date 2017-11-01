package amf.spec.raml

import amf.domain.Annotation.LexicalInformation
import amf.validation.{SeverityLevels, Validation}
import amf.vocabulary.Namespace
import org.yaml.model.{YMap, YScalar}

trait RamlSyntax {

  val nodes: Map[String, Set[String]] = Map(
    "webApi" -> Set(
      "title",
      "description",
      "version",
      "baseUri",
      "baseUriParameters",
      "protocols",
      "mediaType",
      "documentation",
      "schemas",
      "types",
      "traits",
      "resourceTypes",
      "annotationTypes",
      "securitySchemes",
      "securedBy",
      "usage",
      "extends",
      "uses"
    ),
    "userDocumentation" -> Set(
      "title",
      "content"
    ),
    "shape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "xml",
      "enum",
      "required"
    ),
    "nodeShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "xml",
      "enum",
      "properties",
      "minProperties",
      "maxProperties",
      "discriminator",
      "discriminatorValue",
      "required",
      "additionalProperties"
    ),
    "arrayShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "xml",
      "enum",
      "uniqueItems",
      "items",
      "minItems",
      "maxItems",
      "required"
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
      "xml",
      "enum",
      "pattern",
      "minLength",
      "maxLength",
      "required"
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
      "xml",
      "enum",
      "minimum",
      "maximum",
      "format",
      "multipleOf",
      "required"
    ),
    "fileShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "xml",
      "enum",
      "fileTypes",
      "minLength",
      "maxLength",
      "required"
    ),
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
    "endPoint" -> Set(
      "displayName",
      "description",
      "get",
      "patch",
      "put",
      "post",
      "delete",
      "options",
      "head",
      "is",
      "type",
      "securedBy",
      "uriParameters"
    ),
    "operation" -> Set(
      "displayName",
      "description",
      "queryParameters",
      "headers",
      "queryString",
      "responses",
      "body",
      "protocols",
      "is",
      "securedBy"
    ),
    "response" -> Set(
      "displayName",
      "description",
      "headers",
      "body"
    ),
    "securitySchema" -> Set(
      "type",
      "displayName",
      "description",
      "decribedBy",
      "settings"
    ),
    "annotation" -> Set(
      "displayName",
      "description",
      "allowedTargets",
      "type"
    )
  )

  def validateClosedShape(currentValidation: Validation, id: String, ast: YMap, nodeType: String): Unit = {
    nodes.get(nodeType) match {
      case Some(properties) =>
        ast.entries.foreach { entry =>
          val key = entry.key.value.asInstanceOf[YScalar].text
          if ((key.startsWith("(") && key.endsWith(")")) || (key.startsWith("/") && (nodeType == "webApi" || nodeType == "endPoint"))) {
            // annotation or path in endpoint/webapi => ignore
          } else {
            if (!properties(key)) {
              currentValidation.reportConstraintFailure(
                SeverityLevels.VIOLATION,
                (Namespace.AmfParser + "closed-shape").iri(),
                id,
                None,
                s"Property $key not supported in a RAML $nodeType node",
                Some(LexicalInformation(amf.parser.Range(ast.range)))
              )
            }
          }
        }
      case None => throw new Exception(s"Cannot validate unknown node type $nodeType")
    }
  }

}
