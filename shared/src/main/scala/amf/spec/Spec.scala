package amf.spec

import amf.metadata.domain.APIDocumentationModel._
import amf.metadata.domain.CreativeWorkModel.{Description => CreativeWorkDescription, Url => CreativeWorkUrl}
import amf.metadata.domain.EndPointModel.{Description => EndPointDescription, Name => EndPointName}
import amf.metadata.domain.LicenseModel.{Name => LicenseName, Url => LicenseUrl}
import amf.metadata.domain.OrganizationModel.{
  Email => OrganizationEmail,
  Name => OrganizationName,
  Url => OrganizationUrl
}
import amf.remote.{Amf, Oas, Raml, Vendor}
import amf.spec.FieldEmitter.SpecEmitter
import amf.spec.SpecImplicits._

/**
  * Vendor specs.
  */
object Spec {

  def apply(vendor: Vendor): Spec = vendor match {
    case Raml => ramlSpec
    case Oas  => oasSpec
  }

  case class Spec(vendor: Vendor, private val fs: SpecField*) {
    val fields: Seq[SpecField] = fs.map(_.copy(vendor = vendor))

    val emitter: SpecEmitter = SpecEmitter(fields.toList)
  }

  def ramlSpec: Spec = {
    Spec(
      Raml,
      'title ~ Name,
      'baseUri ~ Host,
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
      "/.*" ~ EndPoints -> (
        'displayName ~ EndPointName,
        'description ~ EndPointDescription
      )
    )
  }

  def oasSpec: Spec = {
    Spec(
      Oas,
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
        "/.*" ~ EndPoints -> (
          'displayName ~ EndPointName,
          'description ~ EndPointDescription
        )
      )
    )
  }
}
