package amf.plugins.document.webapi.parser.spec.common

object WellKnownAnnotation {

  private val annotations = Set(
    "(termsOfService)",
    "(parameters)",
    "(binding)",
    "(contact)",
    "(externalDocs)",
    "(license)",
    "x-base-uri-parameters",
    "(base-uri-parameters)",
    "x-annotationTypes",
    "(deprecated)",
    "(summary)",
    "(externalDocs)",
    "x-request-payloads",
    "(request-payloads)",
    "x-response-payloads",
    "x-uses",
    "(response-payloads)",
    "x-media-type",
    "(media-type)",
    "(readOnly)",
    "(dependencies)",
    "(tuple)",
    "(format)",
    "(exclusiveMaximum)",
    "(exclusiveMinimum)",
    "x-traits",
    "x-resourceTypes",
    "x-is",
    "x-type",
    "(consumes)",
    "(produces)",
    "x-extension-type",
    "x-fragment-type",
    "x-usage",
    "x-title",
    "x-user-documentation",
    "x-description",
    "x-displayName",
    "x-extends",
    "(flow)",
    "x-displayName",
    "x-describedBy",
    "x-discriminator-value",
    "x-requestTokenUri",
    "x-authorizationUri",
    "x-tokenCredentialsUri",
    "x-signatures",
    "x-settings",
    "x-securitySchemes",
    "x-queryParameters",
    "x-headers",
    "x-queryString",
    "(examples)",
    "x-examples",
    "x-fileTypes",
    "x-schema"
  )

  def normalAnnotation(field: String): Boolean =
    if (isRamlAnnotation(field) || isOasAnnotation(field)) {
      !field.startsWith("x-facets") && !annotations.contains(field)
    } else {
      false
    }

  def isOasAnnotation(field: String): Boolean  = field.startsWith("x-") || field.startsWith("X-")
  def isRamlAnnotation(field: String): Boolean = field.startsWith("(") && field.endsWith(")")

  def parseRamlName(s: String): String = s.replace("(", "").replace(")", "")
  def parseOasName(s: String): String  = s.replace("x-", "").replace("X-", "")

  def parseName(s: String): String = parseOasName(parseRamlName(s))
}
