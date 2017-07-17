package amf.spec

import amf.metadata.Field
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
import amf.spec.FieldEmitter.{SpecEmitter, StringValueEmitter}
import amf.spec.FieldParser._
import amf.spec.Matcher.{KeyMatcher, RegExpMatcher}
import amf.spec.SpecImplicits._

/**
  * Vendor specs.
  */
object Spec {

  def apply(vendor: Vendor): Spec = vendor match {
    case Raml => RamlSpec
    case Oas  => OasSpec
    case Amf  => JsonLdSpec
    case _    => Spec()
  }

  case class Spec(fields: SpecField*) {
    def emitter: SpecEmitter = SpecEmitter(fields.toList)
  }

  val RamlSpec = Spec(
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

  val OasSpec = Spec(
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

  val JsonLdSpec = Spec(
    SpecField(
      Nil,
      RegExpMatcher(".*#encodes"),
      ChildrenParser(),
      null,
      List(
        SpecField(Nil,
                  RegExpMatcher(".*name"),
                  ChildrenJsonLdParser(),
                  null,
                  List(
                    SpecField(Name, KeyMatcher("@value"), StringValueParser, StringValueEmitter)
                  )),
        SpecField(Nil,
                  RegExpMatcher(".*host"),
                  ChildrenJsonLdParser(),
                  null,
                  List(
                    SpecField(Host, KeyMatcher("@value"), StringValueParser, StringValueEmitter)
                  )),
        SpecField(Schemes, RegExpMatcher(".*scheme"), StringJsonListParser(), null)
      )
    )
  )
}
