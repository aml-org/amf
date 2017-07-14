package amf.spec

import amf.builder._
import amf.metadata.Field
import amf.metadata.domain.APIDocumentationModel._
import amf.metadata.domain._
import amf.remote.{Amf, Oas, Raml, Vendor}
import amf.spec.FieldEmitter.{ObjectEmitter, SpecEmitter, StringListValueEmitter, StringValueEmitter}
import amf.spec.FieldParser._
import amf.spec.Matcher.{KeyMatcher, RegExpMatcher}
import amf.spec.SpecImplicits._
import amf.metadata.domain.OrganizationModel.{
  Url => OrganizationUrl,
  Name => OrganizationName,
  Email => OrganizationEmail
}
import amf.metadata.domain.CreativeWorkModel.{Url => CreativeWorkUrl, Description => CreativeWorkDescription}
import amf.metadata.domain.LicenseModel.{Url => LicenseUrl, Name => LicenseName}

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

  implicit def fieldAsList(field: Field): List[Field] = List(field)

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
    SpecField(
      EndPoints,
      RegExpMatcher("/.*"),
      new EndPointParser(),
      ObjectEmitter,
      List(
        SpecField(EndPointModel.Name, KeyMatcher("displayName"), StringValueParser, StringValueEmitter),
        SpecField(EndPointModel.Description, KeyMatcher("description"), StringValueParser, StringValueEmitter)
      )
    )
  )

  val OasSpec = Spec(
    SpecField(
      Nil,
      KeyMatcher("info"),
      ChildrenParser(),
      null,
      List(
        SpecField(Name, KeyMatcher("title"), StringValueParser, StringValueEmitter),
        SpecField(Description, KeyMatcher("description"), StringValueParser, StringValueEmitter),
        SpecField(TermsOfService, KeyMatcher("termsOfService"), StringValueParser, StringValueEmitter),
        SpecField(Version, KeyMatcher("version"), StringValueParser, StringValueEmitter),
        SpecField(
          License,
          KeyMatcher("license"),
          ObjectParser,
          ObjectEmitter,
          List(
            SpecField(LicenseModel.Url, KeyMatcher("url"), StringValueParser, StringValueEmitter),
            SpecField(LicenseModel.Name, KeyMatcher("name"), StringValueParser, StringValueEmitter)
          )
        )
      )
    ),
    SpecField(Host, KeyMatcher("host"), StringValueParser, StringValueEmitter),
    SpecField(BasePath, KeyMatcher("basePath"), StringValueParser, StringValueEmitter),
    SpecField(Accepts, KeyMatcher("consumes"), StringValueParser, StringValueEmitter),
    SpecField(ContentType, KeyMatcher("produces"), StringValueParser, StringValueEmitter),
    SpecField(Schemes, KeyMatcher("schemes"), StringListParser, StringListValueEmitter),
    SpecField(
      Nil,
      KeyMatcher("paths"),
      ChildrenParser(),
      null,
      List(
        SpecField(
          EndPoints,
          RegExpMatcher("/.*"),
          new EndPointParser(),
          null,
          List(
            SpecField(EndPointModel.Name, KeyMatcher("displayName"), StringValueParser, StringValueEmitter),
            SpecField(EndPointModel.Description, KeyMatcher("description"), StringValueParser, StringValueEmitter)
          )
        )
      )
    ),
    SpecField(
      Provider,
      KeyMatcher("contact"),
      ObjectParser,
      ObjectEmitter,
      List(
        SpecField(OrganizationModel.Url, KeyMatcher("url"), StringValueParser, StringValueEmitter),
        SpecField(OrganizationModel.Name, KeyMatcher("name"), StringValueParser, StringValueEmitter),
        SpecField(OrganizationModel.Email, KeyMatcher("email"), StringValueParser, StringValueEmitter)
      )
    ),
    SpecField(
      Documentation,
      KeyMatcher("externalDocs"),
      ObjectParser,
      ObjectEmitter,
      List(
        SpecField(CreativeWorkModel.Url, KeyMatcher("url"), StringValueParser, StringValueEmitter),
        SpecField(CreativeWorkModel.Description, KeyMatcher("description"), StringValueParser, StringValueEmitter)
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
