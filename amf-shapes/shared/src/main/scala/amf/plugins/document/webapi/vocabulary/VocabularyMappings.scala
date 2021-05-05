package amf.plugins.document.webapi.vocabulary

import amf.core.vocabulary.Namespace

object VocabularyMappings {

  val webapi: String               = (Namespace.ApiContract + "WebAPI").iri()
  val documentationItem: String    = (Namespace.ApiContract + "DocumentationItem").iri()
  val endpoint: String             = (Namespace.ApiContract + "EndPoint").iri()
  val operation: String            = (Namespace.ApiContract + "Operation").iri()
  val response: String             = (Namespace.ApiContract + "Response").iri()
  val request: String              = (Namespace.ApiContract + "Request").iri()
  val payload: String              = (Namespace.ApiContract + "Payload").iri()
  val shape: String                = (Namespace.Shacl + "Shape").iri()
  val example: String              = (Namespace.ApiContract + "Example").iri()
  val resourceType: String         = (Namespace.ApiContract + "ResourceType").iri()
  val `trait`: String              = (Namespace.ApiContract + "Trait").iri()
  val securityScheme: String       = (Namespace.ApiContract + "SecurityScheme").iri()
  val securitySettings: String     = (Namespace.ApiContract + "SecuritySettings").iri()
  val customDomainProperty: String = (Namespace.Document + "CustomDomainProperty").iri()
  val library: String              = (Namespace.Document + "Module").iri()
  val overlay: String              = (Namespace.Document + "AbstractDocument").iri()
  val extension: String            = (Namespace.Document + "PartialDocument").iri()

  val uriToRaml: Map[String, String] = Map[String, String](
    webapi               -> "API",
    documentationItem    -> "DocumentationItem",
    endpoint             -> "Resource",
    operation            -> "Method",
    response             -> "Response",
    request              -> "RequestBody",
    payload              -> "ResponseBody",
    shape                -> "TypeDeclaration",
    example              -> "Example",
    resourceType         -> "ResourceType",
    `trait`              -> "Trait",
    securityScheme       -> "SecurityScheme",
    securitySettings     -> "SecuritySchemeSettings",
    customDomainProperty -> "AnnotationType",
    library              -> "Library",
    overlay              -> "Overlay",
    extension            -> "Extension"
  )

  val ramlToUri: Map[String, String] = uriToRaml.map { case (k, v) => v -> k }
}
