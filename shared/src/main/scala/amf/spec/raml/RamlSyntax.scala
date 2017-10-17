package amf.spec.raml

import amf.domain.Annotation.LexicalInformation
import amf.validation.{SeverityLevels, Validation}
import amf.vocabulary.Namespace
import org.yaml.model.{YMap, YScalar}

trait RamlSyntax {

  val nodes: Map[String, Map[String, Boolean]] = Map(
    "webApi" -> Map(
      "title"             -> true,
      "description"       -> true,
      "version"           -> true,
      "baseUri"           -> true,
      "baseUriParameters" -> true,
      "protocols"         -> true,
      "mediaType"         -> true,
      "documentation"     -> true,
      "schemas"           -> true,
      "types"             -> true,
      "traits"            -> true,
      "resourceTypes"     -> true,
      "annotationTypes"   -> true,
      "securitySchemes"   -> true,
      "securedBy"         -> true,
      "uses"              -> true
    ),
    "userDocumentation" -> Map(
      "title" -> true,
      "content" -> true
    ),
    "shape" -> Map(
      "type"        -> true,
      "default"     -> true,
      "schema"      -> true,
      "example"     -> true,
      "examples"    -> true,
      "displayName" -> true,
      "description" -> true,
      "facets"      -> true,
      "xml"         -> true,
      "enum"        -> true,
      "required"    -> true
    ),

    "nodeShape" -> Map(
      "type"               -> true,
      "default"            -> true,
      "schema"             -> true,
      "example"            -> true,
      "examples"           -> true,
      "displayName"        -> true,
      "description"        -> true,
      "facets"             -> true,
      "xml"                -> true,
      "enum"               -> true,
      "properties"         -> true,
      "minProperties"      -> true,
      "maxProperties"      -> true,
      "discriminator"      -> true,
      "discriminatorValue" -> true,
      "required"           -> true
    ),

    "arrayShape" -> Map(
      "type"        -> true,
      "default"     -> true,
      "schema"      -> true,
      "example"     -> true,
      "examples"    -> true,
      "displayName" -> true,
      "description" -> true,
      "facets"      -> true,
      "xml"         -> true,
      "enum"        -> true,
      "uniqueItems" -> true,
      "items"       -> true,
      "minItems"    -> true,
      "maxItems"    -> true,
      "required"    -> true
    ),

    "stringScalarShape" -> Map(
      "type"        -> true,
      "default"     -> true,
      "schema"      -> true,
      "example"     -> true,
      "examples"    -> true,
      "displayName" -> true,
      "description" -> true,
      "facets"      -> true,
      "xml"         -> true,
      "enum"        -> true,
      "pattern"     -> true,
      "minLength"   -> true,
      "maxLength"   -> true,
      "required"    -> true
    ),

    "numberScalarShape" -> Map(
      "type"        -> true,
      "default"     -> true,
      "schema"      -> true,
      "example"     -> true,
      "examples"    -> true,
      "displayName" -> true,
      "description" -> true,
      "facets"      -> true,
      "xml"         -> true,
      "enum"        -> true,
      "minimum"     -> true,
      "maximum"     -> true,
      "format"      -> true,
      "multipleOf"  -> true,
      "required"    -> true
    ),

    "fileShape" -> Map(
      "type"        -> true,
      "default"     -> true,
      "schema"      -> true,
      "example"     -> true,
      "examples"    -> true,
      "displayName" -> true,
      "description" -> true,
      "facets"      -> true,
      "xml"         -> true,
      "enum"        -> true,
      "fileTypes"   -> true,
      "minLength"   -> true,
      "maxLength"   -> true,
      "required"    -> true
    ),

    "example" -> Map(
      "displayName" -> true,
      "description" -> true,
      "value"       -> true,
      "strict"      -> true
    ),

    "xmlSerialization" -> Map(
      "attribute" -> true,
      "wrapped"   -> true,
      "name"      -> true,
      "namespace" -> true,
      "prefix"    -> true
    ),

    "endPoint" -> Map(
      "displayName"   -> true,
      "description"   -> true,
      "get"           -> true,
      "patch"         -> true,
      "put"           -> true,
      "post"          -> true,
      "delete"        -> true,
      "options"       -> true,
      "head"          -> true,
      "is"            -> true,
      "type"          -> true,
      "securedBy"     -> true,
      "uriParameters" -> true
    ),

    "operation" -> Map(
      "displayName"     -> true,
      "description"     -> true,
      "queryParameters" -> true,
      "headers"         -> true,
      "queryString"     -> true,
      "responses"       -> true,
      "body"            -> true,
      "protocols"       -> true,
      "is"              -> true,
      "securedBy"       -> true
    ),

    "response" -> Map(
      "displayName" -> true,
      "description" -> true,
      "headers"     -> true,
      "body"        -> true
    ),

    "securitySchema" -> Map(
      "type"        -> true,
      "displayName" -> true,
      "description" -> true,
      "decribedBy"  -> true,
      "settings"    -> true
    ),

    "annotation" -> Map(
      "displayName"   -> true,
      "description"   -> true,
      "allowedTarget" -> true,
      "type"          -> true
    )
  )

  def validateClosedShape(id: String, ast: YMap, nodeType: String) = {
    nodes.get(nodeType) match {
      case Some(properties) =>
        ast.entries.foreach { entry =>
          val key = entry.key.value.asInstanceOf[YScalar].text
          if ((key.startsWith("(") && key.endsWith(")")) || (key.startsWith("/") && (nodeType == "webApi" || nodeType == "endPoint"))) {
            // annotation or path in endpoint/webapi => ignore
          } else {
            properties.get(key) match {
              case Some(true) => // ignore
              case _          => Validation.reportConstraintFailure(
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
