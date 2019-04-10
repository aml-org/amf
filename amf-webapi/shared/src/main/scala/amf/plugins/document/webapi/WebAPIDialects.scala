package amf.plugins.document.webapi

import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.XsdTypes._
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.vocabularies.model.domain.{NodeMapping, PropertyMapping}
import amf.plugins.document.webapi.parser.spec.declaration.DataNodeEmitter
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.metamodel.security.{ApiKeySettingsModel, ParametrizedSecuritySchemeModel, SecuritySchemeModel, SettingsModel}


object WebAPIDialects {

  // This will be used to mark collapsed nodes, like WebAPIObject and InfoObject merged into the WebAPI node in the model
  val OwlSameAs = (Namespace.Owl + "sameAs").iri()

  // Marking syntactic fields in the AST that are not directly mapped to properties in the mdoel
  val ImplicitField = (Namespace.Meta + "implicit").iri()

  // Base location for all information in the OAS20 dialect
  val OAS20DialectLocation = "file://vocabularies/dialects/oas20.yaml"

  // Nodes
  object OAS20DialectNodes {

    val WebAPIObject = NodeMapping()
      .withId(OAS20DialectLocation + "#/WebAPIObject")
      .withName("WebAPIObject")
      .withNodeTypeMapping(WebApiModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/WebAPIObject/implicit/swagger")
          .withName("swagger")
          .withMinCount(1)
          .withNodePropertyMapping(ImplicitField)
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/WebAPIObject/info")
          .withName("info")
          .withMinCount(1)
          .withNodePropertyMapping(OwlSameAs)
          .withObjectRange(Seq(
            InfoObject.id
          )),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/WebAPIObject/Servers/url_host")
          .withName("host")
          .withNodePropertyMapping(ServerModel.Url.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/WebAPIObject/Servers/url_basePath")
          .withName("basePath")
          .withNodePropertyMapping(ServerModel.Url.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/WebAPIObject/schemes")
          .withName("schemes")
          .withNodePropertyMapping(WebApiModel.Schemes.value.iri())
          .withEnum(Seq("ws", "wss", "http", "https"))
          .withAllowMultiple(true)
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/WebAPIObject/consumes")
          .withName("consumes")
          .withNodePropertyMapping(WebApiModel.Accepts.value.iri())
          .withAllowMultiple(true)
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/WebAPIObject/produces")
          .withName("produces")
          .withNodePropertyMapping(WebApiModel.ContentType.value.iri())
          .withAllowMultiple(true)
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/WebAPIObject/paths")
          .withName("paths")
          .withMinCount(1)
          .withNodePropertyMapping(WebApiModel.EndPoints.value.iri())
          .withMapKeyProperty(EndPointModel.Name.value.iri())
          .withObjectRange(Seq(
            PathObject.id
          ))

      ))

    val InfoObject = NodeMapping()
      .withId(OAS20DialectLocation + "#/InfoObject")
      .withName("InfoObject")
      .withNodeTypeMapping(WebApiModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/InfoObject/title")
          .withName("title")
          .withNodePropertyMapping(WebApiModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/InfoObject/description")
          .withName("description")
          .withNodePropertyMapping(WebApiModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/InfoObject/termsOfService")
          .withName("termsOfService")
          .withNodePropertyMapping(WebApiModel.TermsOfService.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/InfoObject/version")
          .withName("version")
          .withNodePropertyMapping(WebApiModel.Version.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/InfoObject/contact")
          .withName("contact")
          .withNodePropertyMapping(WebApiModel.Provider.value.iri())
          .withObjectRange(Seq(
            ContactObject.id
          )),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/InfoObject/license")
          .withName("license")
          .withNodePropertyMapping(WebApiModel.License.value.iri())
          .withObjectRange(Seq(
            LicenseObject.id
          )),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/InfoObject/security")
          .withName("security")
          .withNodePropertyMapping(WebApiModel.Security.value.iri())
          .withAllowMultiple(true)
          .withMapKeyProperty(ParametrizedSecuritySchemeModel.Name.value.iri())
          .withTypeDiscriminatorName("type")
          .withTypeDiscriminator(Map(
            "basic"  -> BasicSecuritySchemeObject.id,
            "apiKey" -> ApiKeySecuritySchemeObject.id
          ))
          .withObjectRange(Seq(
            BasicSecuritySchemeObject.id,
            ApiKeySecuritySchemeObject.id
          ))

      ))

    val ContactObject = NodeMapping()
      .withId("#/ContactObject")
      .withName("ContactObject")
      .withNodeTypeMapping(OrganizationModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/ContactObject/name")
          .withName("name")
          .withNodePropertyMapping(OrganizationModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/ContactObject/url")
          .withName("url")
          .withNodePropertyMapping(OrganizationModel.Url.value.iri())
          .withLiteralRange(xsdUri.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/ContactObject/email")
          .withName("email")
          .withNodePropertyMapping(OrganizationModel.Email.value.iri())
          .withLiteralRange(xsdString.iri())

      ))

    val LicenseObject = NodeMapping()
      .withId("#/LicenseObject")
      .withName("LicenseObject")
      .withNodeTypeMapping(LicenseModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/LicenseObject/name")
          .withName("name")
          .withMinCount(1)
          .withNodePropertyMapping(LicenseModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/LicenseObject/url")
          .withName("url")
          .withNodePropertyMapping(LicenseModel.Url.value.iri())
          .withLiteralRange(xsdUri.iri()),
      ))

    val BasicSecuritySchemeObject = NodeMapping()
      .withId("#/BasicSecurityScheme")
      .withName("BasicSecurityScheme")
      .withNodeTypeMapping(ParametrizedSecuritySchemeModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/BasicSecurityScheme/name")
          .withName("schemeName")
          .withMinCount(1)
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/BasicSecurityScheme/type")
          .withName("type")
          .withMinCount(1)
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Scheme.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/BasicSecurityScheme/description")
          .withName("description")
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Description.value.iri())
          .withLiteralRange(xsdString.iri())

      ))

    val ApiKeySecuritySchemeObject = NodeMapping()
      .withId("#/ApiKeySecurityScheme")
      .withName("ApiKeySecurityScheme")
      .withNodeTypeMapping(ParametrizedSecuritySchemeModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/ApiKeySecurityScheme/name")
          .withName("schemeName")
          .withMinCount(1)
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/ApiKeySecurityScheme/type")
          .withName("type")
          .withMinCount(1)
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Scheme.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/ApiKeySecurityScheme/description")
          .withName("description")
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Description.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/ApiKeySecurityScheme/Settings/Name")
          .withName("name")
          .withMinCount(1)
          .withNodePropertyMapping(ApiKeySettingsModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(OAS20DialectLocation + "#/ApiKeySecurityScheme/Settings/In")
          .withName("in")
          .withMinCount(1)
          .withEnum(Seq(
            "query",
            "header"
          ))
          .withNodePropertyMapping(ApiKeySettingsModel.In.value.iri())
          .withLiteralRange(xsdString.iri()),

      ))


    val PathObject = NodeMapping()
      .withId("#/PathObject")
      .withName("PathObject")
      .withNodeTypeMapping(EndPointModel.`type`.head.iri())
      .withPropertiesMapping(Seq(
/*
        PropertyMapping()
          .withId(OAS20DialectLocation + "#/LicenseObject/name")
          .withName("name")
          .withMinCount(1)
          .withNodePropertyMapping(LicenseModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),
*/
      ))
  }

  // Dialect
  val OAS20Dialect = Dialect()
    .withName("OAS")
    .withVersion("2.0")
    .withLocation(OAS20DialectLocation)
    .withId(OAS20DialectLocation)
    .withDeclares(Seq(
      OAS20DialectNodes.WebAPIObject
    ))
}
