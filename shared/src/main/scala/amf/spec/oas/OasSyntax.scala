package amf.spec.oas

trait OasSyntax {

  val nodes: Map[String, Map[String, Boolean]] = Map(
    "webApi" -> Map(
      "swagger"             -> true,
      "info"                -> true,
      "host"                -> true,
      "basePath"            -> true,
      "schemes"             -> true,
      "consumes"            -> true,
      "produces"            -> true,
      "paths"               -> true,
      "definitions"         -> true,
      "parameters"          -> true,
      "responses"           -> true,
      "securityDefinitions" -> true,
      "security"            -> true,
      "tags"                -> true,
      "externalDocs"        -> true
    ),

    "info" -> Map(
      "title"          -> true,
      "description"    -> true,
      "termsOfService" -> true,
      "contact"        -> true,
      "license"        -> true,
      "version"        -> true
    ),

    "contact" -> Map(
      "name"  -> true,
      "url"   -> true,
      "email" -> true
    ),

    "license" -> Map(
      "name"  -> true,
      "url"   -> true
    ),

    "endPoint" -> Map(
      "get"        -> true,
      "put"        -> true,
      "post"       -> true,
      "delete"     -> true,
      "options"    -> true,
      "head"       -> true,
      "patch"      -> true,
      "parameters" -> true,
      "$ref"       -> true
    ),

    "operation"-> Map(
      "tags"         -> true,
      "summary"      -> true,
      "description"  -> true,
      "externalDocs" -> true,
      "operationId"  -> true,
      "consumes"     -> true,
      "produces"     -> true,
      "parameters"   -> true,
      "responses"    -> true,
      "schemes"      -> true,
      "deprecated"   -> true,
      "security"     -> true
    ),

    "externalDoc" -> Map(
      "url" -> true
    ),

    "parameter" -> Map(
      "name"        -> true,
      "in"          -> true,
      "description" -> true,
      "required"    -> true
    ),

    "bodyParameter" -> Map(
      "name"             -> true,
      "in"               -> true,
      "description"      -> true,
      "required"         -> true,
      "type"             -> true,
      "format"           -> true,
      "allowEmptyValue"  -> true,
      "items"            -> true,
      "collectionFormat" -> true,
      "default"          -> true,
      "maximum"          -> true,
      "exclusiveMaximum" -> true,
      "minimum"          -> true,
      "exclusiveMinimum" -> true,
      "maxLength"        -> true,
      "minLength"        -> true,
      "pattern"          -> true,
      "maxItems"         -> true,
      "minItems"         -> true,
      "multipleOf"       -> true,
      "uniqueItems"      -> true,
      "enum"             -> true,
      "multipleOf"       -> true,
      "items"            -> true
    ),

    "response" -> Map(
      "description" -> true,
      "schema"      -> true,
      "headers"     -> true,
      "examples"    -> true
    ),

    "headerParameter" -> Map(
      "description"      -> true,
      "type"             -> true,
      "items"            -> true,
      "collectionFormat" -> true,
      "default"          -> true,
      "maximum"          -> true,
      "exclusiveMaximum" -> true,
      "minimum"          -> true,
      "exclusiveMinimum" -> true,
      "maxLength"        -> true,
      "minLength"        -> true,
      "pattern"          -> true,
      "maxItems"         -> true,
      "minItems"         -> true,
      "uniqueItems"      -> true,
      "enum"             -> true,
      "multipleOf"       -> true
    ),

    "tag" -> Map(
      "name"         -> true,
      "description"  -> true,
      "externalDocs" -> true
    ),

    "schema" -> Map(
      "$ref"                 -> true,
      "format"               -> true,
      "title"                -> true,
      "description"          -> true,
      "maximum"              -> true,
      "exclusiveMaximum"     -> true,
      "minimum"              -> true,
      "exclusiveMinimum"     -> true,
      "maxLength"            -> true,
      "minLength"            -> true,
      "pattern"              -> true,
      "maxItems"             -> true,
      "minItems"             -> true,
      "uniqueItems"          -> true,
      "maxProperties"        -> true,
      "minProperties"        -> true,
      "required"             -> true,
      "enum"                 -> true,
      "type"                 -> true,
      "items"                -> true,
      "allOf"                -> true,
      "properties"           -> true,
      "additionalProperties" -> true,
      "discriminator"        -> true,
      "readOnly"             -> true,
      "xml"                  -> true,
      "externalDocs"         -> true,
      "example"              -> true
    )
  )
}
