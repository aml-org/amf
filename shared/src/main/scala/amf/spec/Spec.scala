package amf.spec

import amf.metadata.domain.CreativeWorkModel.{Description => CreativeWorkDescription, Url => CreativeWorkUrl}
import amf.metadata.domain.EndPointModel.{
  Operations,
  Description => EndPointDescription,
  Name => EndPointName,
  Parameters => EndPointParameters
}
import amf.metadata.domain.LicenseModel.{Name => LicenseName, Url => LicenseUrl}
import amf.metadata.domain.OperationModel.{
  Deprecated,
  Responses,
  Summary,
  Description => OperationDescription,
  Documentation => OperationDocumentation,
  Name => OperationName,
  Schemes => OperationSchemes
}
import amf.metadata.domain.OrganizationModel.{
  Email => OrganizationEmail,
  Name => OrganizationName,
  Url => OrganizationUrl
}
import amf.metadata.domain.ParameterModel.{
  Required,
  Schema,
  Binding => ParameterBinding,
  Description => ParameterDescription,
  Name => ParameterName
}
import amf.metadata.domain.RequestModel.{Headers, Payloads, QueryParameters}
import amf.metadata.domain.ResponseModel.{Description => ResponseDescription, Headers => ResponseHeaders}
import amf.metadata.domain.WebApiModel._
import amf.remote.{Oas, Raml, Vendor}
import amf.spec.FieldEmitter.SpecEmitter
import amf.spec.SpecImplicits._

/**
  * Vendor specs.
  */
object Spec {

  def apply(vendor: Vendor): Spec = vendor match {
    case Raml => RamlSpec
    case Oas  => OasSpec
  }

  case class Spec(private val fs: SpecField*)(vendor: Vendor) {
    val fields: Seq[SpecField] = fs.map(_.copy(vendor = vendor))

    def emitter: SpecEmitter = SpecEmitter(fields.toList)
  }

  private val RamlParam = List(
    'description ~ ParameterDescription,
    'required ~ Required,
    'type ~ Schema
  )
  private val RamlSpec: Spec =
    Spec(
      'title ~ Name,
      'baseUri ~ Host,
      'baseUriParameters ~ BaseUriParameters -> RamlParam,
      'description ~ Description,
      'mediaType ~ (ContentType | Accepts),
      'version ~ Version,
      'termsOfService ~ TermsOfService,
      'protocols ~ Schemes,
      'contact ~ Provider -> (
        'url ~ OrganizationUrl,
        'name ~ OrganizationName,
        'email ~ OrganizationEmail
      ),
      'externalDocs ~ Documentation -> (
        'url ~ CreativeWorkUrl,
        'description ~ CreativeWorkDescription
      ),
      'license ~ License -> (
        'url ~ LicenseUrl,
        'name ~ LicenseName
      ),
      "^/.*" ~ EndPoints -> (
        'displayName ~ EndPointName,
        'description ~ EndPointDescription,
        'uriParameters ~ EndPointParameters -> RamlParam,
        "get|patch|put|post|delete|options|head" ~ Operations -> (
          'title ~ OperationName,
          'description ~ OperationDescription,
          'deprecated ~ Deprecated,
          'summary ~ Summary,
          'externalDocs ~ OperationDocumentation -> (
            'url ~ CreativeWorkUrl,
            'description ~ CreativeWorkDescription
          ),
          'protocols ~ OperationSchemes,
          'responses -> (
            "\\d{3}" ~ Responses -> (
              'description ~ ResponseDescription,
              'headers ~ ResponseHeaders -> RamlParam,
              'body ~ Responses
            )
          )
        )
      )
    )(Raml)

  private val OasParam = List(
    'name ~ ParameterName,
    'description ~ ParameterDescription,
    'required ~ Required,
    'in ~ ParameterBinding,
    'schema ~ Schema
  )

  private val OasSpec: Spec =
    Spec(
      'info -> (
        'title ~ Name,
        'description ~ Description,
        'termsOfService ~ TermsOfService,
        'version ~ Version,
        'license ~ License -> (
          'url ~ LicenseUrl,
          'name ~ LicenseName
        )
      ),
      'host ~ Host,
      'basePath ~ BasePath,
      Symbol("x-baseUriParameters") ~ BaseUriParameters -> OasParam,
      'consumes ~ Accepts,
      'produces ~ ContentType,
      'schemes ~ Schemes,
      'contact ~ Provider -> (
        'url ~ OrganizationUrl,
        'name ~ OrganizationName,
        'email ~ OrganizationEmail
      ),
      'externalDocs ~ Documentation -> (
        'url ~ CreativeWorkUrl,
        'description ~ CreativeWorkDescription
      ),
      'paths -> (
        "^/.*" ~ EndPoints -> (
          'displayName ~ EndPointName,
          'description ~ EndPointDescription,
          'parameters ~ EndPointParameters -> OasParam,
          "get|patch|put|post|delete|options|head" ~ Operations -> (
            'operationId ~ OperationName,
            'description ~ OperationDescription,
            'deprecated ~ Deprecated,
            'summary ~ Summary,
            'externalDocs ~ OperationDocumentation -> (
              'url ~ CreativeWorkUrl,
              'description ~ CreativeWorkDescription
            ),
            'schemes ~ OperationSchemes,
            'responses -> (
              "default|\\d{3}" ~ Responses -> (
                'description ~ ResponseDescription,
                'headers ~ ResponseHeaders -> RamlParam
              )
            )
          )
        )
      )
    )(Oas)

  private val RamlRequestSpec =
    Spec(
      'headers ~ Headers                 -> RamlParam,
      'queryParameters ~ QueryParameters -> RamlParam,
      'body ~ Payloads
    )(Raml)

  private val OasRequestSpec =
    Spec(
      'parameters ~ (QueryParameters | Headers) -> OasParam,
      'body ~ Payloads
    )(Oas)

  private[spec] val RequestSpec: (Vendor) => Spec = {
    case Raml => RamlRequestSpec
    case Oas  => OasRequestSpec
  }

  val RAML_10: String = "#%RAML 1.0\n"
}
