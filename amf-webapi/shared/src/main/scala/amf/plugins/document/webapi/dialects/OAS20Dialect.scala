package amf.plugins.document.webapi.dialects

import amf.core.annotations.Aliases
import amf.core.metamodel.document.DocumentModel
import amf.core.metamodel.domain.{ModelVocabularies, ShapeModel}
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.XsdTypes._
import amf.plugins.document.vocabularies.model.document.{Dialect, Vocabulary}
import amf.plugins.document.vocabularies.model.domain._
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.metamodel.security._


object OAS20Dialect {

  // This will be used to mark collapsed nodes, like WebAPIObject and InfoObject merged into the WebAPI node in the model
  val OwlSameAs = (Namespace.Owl + "sameAs").iri()

  // Marking syntactic fields in the AST that are not directly mapped to properties in the mdoel
  val ImplicitField = (Namespace.Meta + "implicit").iri()

  // Base location for all information in the OAS20 dialect
  val DialectLocation = "file://vocabularies/dialects/oas20.yaml"

  // Recursive, we need this to be able to refer to ourselves
  val SchemaObjectId = "#/declarations/SchemaObject"

  // Nodes
  object DialectNodes {

    def commonDataShapesProperties(nodeId: String) = {
      Seq(
        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/format")
          .withName("format")
          .withNodePropertyMapping(ScalarShapeModel.Format.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/items")
          .withName("items")
          .withNodePropertyMapping(ArrayShapeModel.Items.value.iri())
          .withObjectRange(Seq(
            SchemaObjectId
          )),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/collectionFormat")
          .withName("collectionFormat")
          .withNodePropertyMapping(ArrayShapeModel.CollectionFormat.value.iri())
          .withEnum(Seq(
            "csv",
            "ssv",
            "tsv",
            "pipes",
            "multi"
          ))
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/default")
          .withName("default")
          .withNodePropertyMapping(ShapeModel.Default.value.iri())
          .withLiteralRange(xsdAnyType.iri()),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/maximume")
          .withName("maximum")
          .withNodePropertyMapping(ScalarShapeModel.Maximum.value.iri())
          .withLiteralRange(amlNumber.iri()),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/exclusiveMaximum")
          .withName("exclusiveMaximum")
          .withNodePropertyMapping(ScalarShapeModel.ExclusiveMaximum.value.iri())
          .withLiteralRange(amlNumber.iri()),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/minimum")
          .withName("minimum")
          .withNodePropertyMapping(ScalarShapeModel.Minimum.value.iri())
          .withLiteralRange(amlNumber.iri()),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/exclusiveMinimum")
          .withName("exclusiveMinimum")
          .withNodePropertyMapping(ScalarShapeModel.ExclusiveMinimum.value.iri())
          .withLiteralRange(amlNumber.iri()),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/maxLength")
          .withName("maxLength")
          .withNodePropertyMapping(ScalarShapeModel.MaxLength.value.iri())
          .withLiteralRange(xsdInteger.iri()),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/minLength")
          .withName("minLength")
          .withNodePropertyMapping(ScalarShapeModel.MinLength.value.iri())
          .withLiteralRange(xsdInteger.iri()),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/pattern")
          .withName("pattern")
          .withNodePropertyMapping(ScalarShapeModel.Pattern.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/maxItems")
          .withName("maxItems")
          .withNodePropertyMapping(ArrayShapeModel.MaxItems.value.iri())
          .withLiteralRange(xsdInteger.iri()),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/minItems")
          .withName("minItems")
          .withNodePropertyMapping(ArrayShapeModel.MinItems.value.iri())
          .withLiteralRange(xsdInteger.iri()),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/uniqueItems")
          .withName("uniqueItems")
          .withNodePropertyMapping(ArrayShapeModel.UniqueItems.value.iri())
          .withLiteralRange(xsdBoolean.iri()),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/multipleOf")
          .withName("multipleOf")
          .withNodePropertyMapping(ScalarShapeModel.MultipleOf.value.iri())
          .withLiteralRange(amlNumber.iri()),

        PropertyMapping()
          .withId(DialectLocation +s"#/declarations/${nodeId}/enum")
          .withName("enum")
          .withNodePropertyMapping(ShapeModel.Values.value.iri())
          .withLiteralRange(xsdAnyType.iri()),
      )
    }

    val ExampleObject = NodeMapping()
      .withId("#/declarations/ExampleObject")
      .withName("ExampleObject")
      .withNodeTypeMapping(ExampleModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ExampleObject/mediaType")
          .withNodePropertyMapping(ExampleModel.MediaType.value.iri())
          .withName("mediaType")
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ExampleObject/raw")
          .withNodePropertyMapping(ExampleModel.Raw.value.iri())
          .withName("raw")
          .withLiteralRange(xsdString.iri())

      ))

    val XMLObject = NodeMapping()
      .withId("#/declarations/XMLObject")
      .withName("XMLObject")
      .withNodeTypeMapping(XMLSerializerModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/XMLObject/name")
          .withNodePropertyMapping(XMLSerializerModel.Name.value.iri())
          .withName("name")
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/XMLObject/namespace")
          .withNodePropertyMapping(XMLSerializerModel.Namespace.value.iri())
          .withName("namespace")
          .withLiteralRange(amlLink.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/XMLObject/prefix")
          .withNodePropertyMapping(XMLSerializerModel.Prefix.value.iri())
          .withName("prefix")
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/XMLObject/attribute")
          .withNodePropertyMapping(XMLSerializerModel.Attribute.value.iri())
          .withName("attribute")
          .withLiteralRange(xsdBoolean.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/XMLObject/wrapped")
          .withNodePropertyMapping(XMLSerializerModel.Wrapped.value.iri())
          .withName("wrapped")
          .withLiteralRange(xsdBoolean.iri())

      ))

    val ExternalDocumentationObject = NodeMapping()
      .withId("#/declarations/ExternalDocumentationObject")
      .withName("ExternalDocumentationObject")
      .withNodeTypeMapping(CreativeWorkModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ExternalDocumentationObject/description")
          .withName("description")
          .withNodePropertyMapping(CreativeWorkModel.Description.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ExternalDocumentationObject/url")
          .withName("url")
          .withMinCount(1)
          .withNodePropertyMapping(CreativeWorkModel.Url.value.iri())
          .withLiteralRange(xsdString.iri())

      ))

    val TagObject = NodeMapping()
      .withId("#/declarations/TagObject")
      .withName("TagObject")
      .withNodeTypeMapping(TagModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/TagObject/name")
          .withNodePropertyMapping(TagModel.Name.value.iri())
          .withName("name")
          .withMinCount(1)
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/TagObject/description")
          .withName("description")
          .withNodePropertyMapping(TagModel.Description.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/TagObject/externalDocs")
          .withName("externalDocs")
          .withNodePropertyMapping(TagModel.Documentation.value.iri())
          .withObjectRange(Seq(
            ExternalDocumentationObject.id
          ))

      ))

    val ParameterObject = NodeMapping()
      .withId("#/declarations/ParameterObject")
      .withName("ParameterObject")
      .withNodeTypeMapping(ParameterModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ParameterObject/name")
          .withName("declarationName")
          .withMinCount(1)
          .withNodePropertyMapping(ParameterModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ParameterObject/description")
          .withName("description")
          .withNodePropertyMapping(ParameterModel.Description.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ParameterObject/required")
          .withName("required")
          .withMinCount(1)
          .withNodePropertyMapping(ParameterModel.Required.value.iri())
          .withLiteralRange(xsdBoolean.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ParameterObject/parameterName")
          .withName("name")
          .withMinCount(1)
          .withNodePropertyMapping(ParameterModel.ParameterName.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ParameterObject/binding")
          .withName("in")
          .withMinCount(1)
          .withEnum(Seq(
            "query",
            "header",
            "path",
            "formData"
          ))
          .withNodePropertyMapping(ParameterModel.Binding.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ParameterObject/Shape/type")
          .withName("type")
          .withMinCount(1)
          .withEnum(Seq(
            "string",
            "number",
            "integer",
            "boolean",
            "array",
            "file"
          ))
          .withNodePropertyMapping(ImplicitField)
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ParameterObject/format")
          .withName("format")
          .withNodePropertyMapping(ScalarShapeModel.Format.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ParameterObject/allowEmptyValue")
          .withName("allowEmptyValue")
          .withNodePropertyMapping(ParameterModel.AllowEmptyValue.value.iri())
          .withLiteralRange(xsdBoolean.iri())
      ) ++ commonDataShapesProperties("ParameterObject"))

    val ResponseObject = NodeMapping()
      .withId("#/declarations/ResponseObject")
      .withName("ResponseObject")
      .withNodeTypeMapping(ResponseModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ResponseObject/description")
          .withName("description")
          .withMinCount(1)
          .withNodePropertyMapping(ResponseModel.Description.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ResponseObject/schema")
          .withName("schema")
          .withNodePropertyMapping(ResponseModel.Payloads.value.iri())
          .withObjectRange(Seq(
            SchemaObjectId
          )),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ResponseObject/headers")
          .withName("headers")
          .withNodePropertyMapping(ResponseModel.Headers.value.iri())
          .withMapKeyProperty(ParameterModel.Name.value.iri())
          .withObjectRange(Seq(
            ParameterObject.id
          )),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ResponseObject/examples")
          .withName("examples")
          .withMapKeyProperty(ExampleModel.MediaType.value.iri())
          .withMapValueProperty(ExampleModel.Raw.value.iri())
          .withNodePropertyMapping(ResponseModel.Examples.value.iri())
          .withObjectRange(Seq(
            ExampleObject.id
          ))

      ))

    val SchemaObject = NodeMapping()
      .withId(SchemaObjectId)
      .withName("SchemaObject")
      .withNodeTypeMapping(ShapeModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/SchemaObject/title")
          .withName("title")
          .withMinCount(1)
          .withNodePropertyMapping(ShapeModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/SchemaObject/description")
          .withName("description")
          .withMinCount(1)
          .withNodePropertyMapping(ShapeModel.Description.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/SchemaObject/required")
          .withName("required")
          .withNodePropertyMapping(PropertyShapeModel.MinCount.value.iri())
          .withLiteralRange(xsdBoolean.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/SchemaObject/readOnly")
          .withName("readOnly")
          .withNodePropertyMapping(PropertyShapeModel.ReadOnly.value.iri())
          .withLiteralRange(xsdBoolean.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/SchemaObject/discriminator")
          .withName("discriminator")
          .withNodePropertyMapping(NodeShapeModel.Discriminator.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/SchemaObject/Shape/type")
          .withName("type")
          .withMinCount(1)
          .withEnum(Seq(
            "object",
            "string",
            "number",
            "integer",
            "boolean",
            "array",
            "file"
          ))
          .withNodePropertyMapping(ImplicitField)
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/SchemaObject/xml")
          .withName("xml")
          .withNodePropertyMapping(AnyShapeModel.XMLSerialization.value.iri())
          .withObjectRange(Seq(
            XMLObject.id
          )),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/SchemaObject/externalDocs")
          .withName("externalDocs")
          .withNodePropertyMapping(AnyShapeModel.Documentation.value.iri())
          .withObjectRange(Seq(
            ExternalDocumentationObject.id
          )),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/SchemaObject/example")
          .withName("example")
          .withNodePropertyMapping(AnyShapeModel.Examples.value.iri())
          .withLiteralRange(amlAnyNode.iri())

      ) ++ commonDataShapesProperties("SchemaObject"))

    val BodyParameterObject = NodeMapping()
      .withId("#/declarations/BodyParameterObject")
      .withName("BodyParameterObject")
      .withNodeTypeMapping(ParameterModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/BodyParameterObject/name")
          .withName("declarationName")
          .withMinCount(1)
          .withNodePropertyMapping(ParameterModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/BodyParameterObject/description")
          .withName("description")
          .withNodePropertyMapping(ParameterModel.Description.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/BodyParameterObject/required")
          .withName("required")
          .withMinCount(1)
          .withNodePropertyMapping(ParameterModel.Required.value.iri())
          .withLiteralRange(xsdBoolean.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/BodyParameterObject/parameterName")
          .withName("name")
          .withMinCount(1)
          .withNodePropertyMapping(ParameterModel.ParameterName.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/BodyParameterObject/binding")
          .withName("in")
          .withMinCount(1)
          .withEnum(Seq(
            "body"
          ))
          .withNodePropertyMapping(ParameterModel.Binding.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/BodyParameterObject/schema")
          .withName("schema")
          .withMinCount(1)
          .withNodePropertyMapping(ParameterModel.Schema.value.iri())
          .withObjectRange(Seq(
            SchemaObject.id
          ))
      ))

    val ScopeObject = NodeMapping()
      .withId("#/declarations/ScopeObject")
      .withName("ScopeObject")
      .withNodeTypeMapping(ScopeModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ScopeObject/name")
          .withName("name")
          .withMinCount(1)
          .withNodePropertyMapping(ScopeModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ScopeObject/description")
          .withName("description")
          .withNodePropertyMapping(ScopeModel.Description.value.iri())
          .withLiteralRange(xsdString.iri()),

      ))

    val ApiKeySecuritySchemeObject = NodeMapping()
      .withId("#/declarations/ApiKeySecurityScheme")
      .withName("ApiKeySecurityScheme")
      .withNodeTypeMapping(ParametrizedSecuritySchemeModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ApiKeySecurityScheme/name")
          .withName("schemeName")
          .withMinCount(1)
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ApiKeySecurityScheme/type")
          .withName("type")
          .withMinCount(1)
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Scheme.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ApiKeySecurityScheme/description")
          .withName("description")
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Description.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ApiKeySecurityScheme/Settings/Name")
          .withName("name")
          .withMinCount(1)
          .withNodePropertyMapping(ApiKeySettingsModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ApiKeySecurityScheme/Settings/In")
          .withName("in")
          .withMinCount(1)
          .withEnum(Seq(
            "query",
            "header"
          ))
          .withNodePropertyMapping(ApiKeySettingsModel.In.value.iri())
          .withLiteralRange(xsdString.iri()),

      ))

    val Oauth2SecuritySchemeObject = NodeMapping()
      .withId("#/declarations/Oauth2SecurityScheme")
      .withName("Oauth2SecurityScheme")
      .withNodeTypeMapping(ParametrizedSecuritySchemeModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/Oauth2SecurityScheme/name")
          .withName("name")
          .withMinCount(1)
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/Oauth2SecurityScheme/description")
          .withName("description")
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Description.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/Oauth2Securit/type")
          .withName("type")
          .withMinCount(1)
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Scheme.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/Oauth2SecurityScheme/flow")
          .withName("flow")
          .withMinCount(1)
          .withNodePropertyMapping(OAuth2SettingsModel.Flow.value.iri())
          .withEnum(Seq(
            "implicit",
            "password",
            "application",
            "accessCode"
          ))
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/Oauth2SecurityScheme/authorizationUrl")
          .withName("authorizationUrl")
          .withNodePropertyMapping(OAuth2SettingsModel.AuthorizationUri.value.iri())
          .withLiteralRange(xsdUri.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/Oauth2SecurityScheme/tokenUrl")
          .withName("tokenUrl")
          .withNodePropertyMapping(OAuth2SettingsModel.AccessTokenUri.value.iri())
          .withLiteralRange(xsdUri.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/Oauth2SecurityScheme/scopes")
          .withName("scopes")
          .withNodePropertyMapping(OAuth2SettingsModel.Scopes.value.iri())
          .withMapKeyProperty(ScopeModel.Name.value.iri())
          .withMapKeyProperty(ScopeModel.Description.value.iri())
          .withObjectRange(Seq(ScopeObject.id))

      ))

    val BasicSecuritySchemeObject = NodeMapping()
      .withId("#/declarations/BasicSecurityScheme")
      .withName("BasicSecurityScheme")
      .withNodeTypeMapping(ParametrizedSecuritySchemeModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/BasicSecurityScheme/name")
          .withName("schemeName")
          .withMinCount(1)
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/BasicSecurityScheme/type")
          .withName("type")
          .withMinCount(1)
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Scheme.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/BasicSecurityScheme/description")
          .withName("description")
          .withNodePropertyMapping(ParametrizedSecuritySchemeModel.Description.value.iri())
          .withLiteralRange(xsdString.iri())

      ))

    val OperationObject = NodeMapping()
      .withId("#/declarations/OperationObject")
      .withName("OperationObject")
      .withNodeTypeMapping(OperationModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/OperationObject/tags")
          .withName("tags")
          .withAllowMultiple(true)
          .withNodePropertyMapping(OperationModel.Tags.value.iri())
          .withObjectRange(Seq(
            TagObject.id
          )),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/OperationObject/summary")
          .withName("summary")
          .withNodePropertyMapping(OperationModel.Summary.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/OperationObject/description")
          .withName("description")
          .withNodePropertyMapping(OperationModel.Description.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/OperationObject/externalDocs")
          .withName("externalDocs")
          .withNodePropertyMapping(OperationModel.Documentation.value.iri())
          .withObjectRange(Seq(
            ExternalDocumentationObject.id
          )),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/OperationObject/operationId")
          .withName("operationId")
          .withNodePropertyMapping(OperationModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/OperationObject/consumes")
          .withName("consumes")
          .withNodePropertyMapping(OperationModel.Accepts.value.iri())
          .withAllowMultiple(true)
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/OperationObject/produces")
          .withName("produces")
          .withNodePropertyMapping(OperationModel.ContentType.value.iri())
          .withAllowMultiple(true)
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/OperationObject/Request/parameters")
          .withName("parameters")
          .withNodePropertyMapping(RequestModel.UriParameters.value.iri())
          .withAllowMultiple(true)
          .withObjectRange(Seq(
            ParameterObject.id
          )),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/OperationObject/responses")
          .withName("responses")
          .withNodePropertyMapping(OperationModel.Responses.value.iri())
          .withMinCount(1)
          .withMapKeyProperty(ResponseModel.StatusCode.value.iri())
          .withObjectRange(Seq(
            ResponseObject.id
          )),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/OperationObject/schemes")
          .withName("schemes")
          .withNodePropertyMapping(OperationModel.Schemes.value.iri())
          .withEnum(Seq("ws", "wss", "http", "https"))
          .withAllowMultiple(true)
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/OperationObject/deprecated")
          .withName("deprecated")
          .withNodePropertyMapping(OperationModel.Deprecated.value.iri())
          .withLiteralRange(xsdBoolean.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/OperationObject/security")
          .withName("security")
          .withNodePropertyMapping(OperationModel.Security.value.iri())
          .withAllowMultiple(true)
          .withMapKeyProperty(ParametrizedSecuritySchemeModel.Name.value.iri())
          .withTypeDiscriminatorName("type")
          .withTypeDiscriminator(Map(
            BasicSecuritySchemeObject.id -> "basic",
            ApiKeySecuritySchemeObject.id -> "apiKey",
            Oauth2SecuritySchemeObject.id -> "oauth2"
          ))
          .withObjectRange(Seq(
            BasicSecuritySchemeObject.id,
            ApiKeySecuritySchemeObject.id,
            Oauth2SecuritySchemeObject.id
          ))
      ))

    val PathItemObject = NodeMapping()
      .withId("#/declarations/PathItemObject")
      .withName("PathItemObject")
      .withNodeTypeMapping(EndPointModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/PathItem/name")
          .withName("name")
          .withMinCount(1)
          .withPattern("^\\/.*$")
          .withNodePropertyMapping(EndPointModel.Path.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/PathItem/get")
          .withName("get")
          .withNodePropertyMapping(EndPointModel.Operations.value.iri())
          .withObjectRange(Seq(OperationObject.id)),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/PathItem/put")
          .withName("put")
          .withNodePropertyMapping(EndPointModel.Operations.value.iri())
          .withObjectRange(Seq(OperationObject.id)),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/PathItem/post")
          .withName("post")
          .withNodePropertyMapping(EndPointModel.Operations.value.iri())
          .withObjectRange(Seq(OperationObject.id)),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/PathItem/delete")
          .withName("delete")
          .withNodePropertyMapping(EndPointModel.Operations.value.iri())
          .withObjectRange(Seq(OperationObject.id)),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/PathItem/options")
          .withName("options")
          .withNodePropertyMapping(EndPointModel.Operations.value.iri())
          .withObjectRange(Seq(OperationObject.id)),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/PathItem/head")
          .withName("head")
          .withNodePropertyMapping(EndPointModel.Operations.value.iri())
          .withObjectRange(Seq(OperationObject.id)),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/PathItem/patch")
          .withName("patch")
          .withNodePropertyMapping(EndPointModel.Operations.value.iri())
          .withObjectRange(Seq(OperationObject.id)),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/PathItem/parameters")
          .withName("parameter")
          .withNodePropertyMapping(EndPointModel.Parameters.value.iri())
          .withAllowMultiple(true)
          .withObjectRange(Seq(
            ParameterObject.id,
            BodyParameterObject.id
          )),

      ))

    val LicenseObject = NodeMapping()
      .withId("#/declarations/LicenseObject")
      .withName("LicenseObject")
      .withNodeTypeMapping(LicenseModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/LicenseObject/name")
          .withName("name")
          .withMinCount(1)
          .withNodePropertyMapping(LicenseModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/LicenseObject/url")
          .withName("url")
          .withNodePropertyMapping(LicenseModel.Url.value.iri())
          .withLiteralRange(xsdUri.iri()),
      ))


    val ContactObject = NodeMapping()
      .withId("#/declarations/ContactObject")
      .withName("ContactObject")
      .withNodeTypeMapping(OrganizationModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ContactObject/name")
          .withName("name")
          .withNodePropertyMapping(OrganizationModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ContactObject/url")
          .withName("url")
          .withNodePropertyMapping(OrganizationModel.Url.value.iri())
          .withLiteralRange(xsdUri.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/ContactObject/email")
          .withName("email")
          .withNodePropertyMapping(OrganizationModel.Email.value.iri())
          .withLiteralRange(xsdString.iri())

      ))


    val InfoObject = NodeMapping()
      .withId(DialectLocation + "#/declarations/InfoObject")
      .withName("InfoObject")
      .withNodeTypeMapping(WebApiModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/InfoObject/title")
          .withName("title")
          .withNodePropertyMapping(WebApiModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/InfoObject/description")
          .withName("description")
          .withNodePropertyMapping(WebApiModel.Name.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/InfoObject/termsOfService")
          .withName("termsOfService")
          .withNodePropertyMapping(WebApiModel.TermsOfService.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/InfoObject/version")
          .withName("version")
          .withNodePropertyMapping(WebApiModel.Version.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/InfoObject/contact")
          .withName("contact")
          .withNodePropertyMapping(WebApiModel.Provider.value.iri())
          .withObjectRange(Seq(
            ContactObject.id
          )),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/InfoObject/license")
          .withName("license")
          .withNodePropertyMapping(WebApiModel.License.value.iri())
          .withObjectRange(Seq(
            LicenseObject.id
          ))

      ))

    val WebAPIObject = NodeMapping()
      .withId(DialectLocation + "#/declarations/WebAPIObject")
      .withName("WebAPIObject")
      .withNodeTypeMapping(WebApiModel.`type`.head.iri())
      .withPropertiesMapping(Seq(

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/WebAPIObject/implicit/swagger")
          .withName("swagger")
          .withMinCount(1)
          .withNodePropertyMapping(ImplicitField)
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/WebAPIObject/info")
          .withName("info")
          .withMinCount(1)
          .withNodePropertyMapping(OwlSameAs)
          .withObjectRange(Seq(
            InfoObject.id
          )),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/WebAPIObject/Servers/url_host")
          .withName("host")
          .withNodePropertyMapping(ServerModel.Url.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/WebAPIObject/Servers/url_basePath")
          .withName("basePath")
          .withNodePropertyMapping(ServerModel.Url.value.iri())
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/WebAPIObject/schemes")
          .withName("schemes")
          .withNodePropertyMapping(WebApiModel.Schemes.value.iri())
          .withEnum(Seq("ws", "wss", "http", "https"))
          .withAllowMultiple(true)
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/WebAPIObject/consumes")
          .withName("consumes")
          .withNodePropertyMapping(WebApiModel.Accepts.value.iri())
          .withAllowMultiple(true)
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/WebAPIObject/produces")
          .withName("produces")
          .withNodePropertyMapping(WebApiModel.ContentType.value.iri())
          .withAllowMultiple(true)
          .withLiteralRange(xsdString.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/InfoObject/securityDefinitions")
          .withName("securityDefinitions")
          .withNodePropertyMapping(DocumentModel.Declares.value.iri())
          .withAllowMultiple(true)
          .withMapKeyProperty(ParametrizedSecuritySchemeModel.Name.value.iri())
          .withTypeDiscriminatorName("type")
          .withTypeDiscriminator(Map(
            BasicSecuritySchemeObject.id -> "basic",
            ApiKeySecuritySchemeObject.id -> "apiKey",
            Oauth2SecuritySchemeObject.id -> "oauth2"
          ))
          .withObjectRange(Seq(
            BasicSecuritySchemeObject.id,
            ApiKeySecuritySchemeObject.id,
            Oauth2SecuritySchemeObject.id
          )),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/security")
          .withName("security")
          .withNodePropertyMapping(WebApiModel.Security.value.iri())
          .withLiteralRange(amlAnyNode.iri()),

        PropertyMapping()
          .withId(DialectLocation + "#/declarations/WebAPIObject/paths")
          .withName("paths")
          .withMinCount(1)
          .withNodePropertyMapping(WebApiModel.EndPoints.value.iri())
          .withMapKeyProperty(EndPointModel.Path.value.iri())
          .withAllowMultiple(true)
          .withObjectRange(Seq(
            PathItemObject.id
          ))

      ))

  }


  // Dialect
  val dialect = {
    val d = Dialect()
      .withId(DialectLocation)
      .withName("OAS")
      .withVersion("2.0")
      .withLocation(DialectLocation)
      .withId(DialectLocation)
      .withDeclares(Seq(
        DialectNodes.WebAPIObject,
        DialectNodes.InfoObject,
        DialectNodes.BodyParameterObject,
        DialectNodes.BasicSecuritySchemeObject,
        DialectNodes.LicenseObject,
        DialectNodes.SchemaObject,
        DialectNodes.ParameterObject,
        DialectNodes.ResponseObject,
        DialectNodes.PathItemObject,
        DialectNodes.Oauth2SecuritySchemeObject,
        DialectNodes.ScopeObject,
        DialectNodes.ApiKeySecuritySchemeObject,
        DialectNodes.ContactObject,
        DialectNodes.ExampleObject,
        DialectNodes.ExternalDocumentationObject,
        DialectNodes.OperationObject,
        DialectNodes.TagObject,
        DialectNodes.XMLObject
      ))
      .withDocuments(
        DocumentsModel()
          .withId(DialectLocation + "#/documents")
          .withRoot(
            DocumentMapping()
              .withId(DialectLocation + "#/documents/root")
              .withEncoded(DialectNodes.WebAPIObject.id)
              .withDeclaredNodes(Seq(
                PublicNodeMapping()
                  .withId(DialectLocation + "#/documents/definitions")
                  .withName("definitions")
                  .withMappedNode(DialectNodes.SchemaObject.id),
                PublicNodeMapping()
                  .withId(DialectLocation + "#/documents/parameters")
                  .withName("parameters")
                  .withMappedNode(DialectNodes.ParameterObject.id),
                PublicNodeMapping()
                  .withId(DialectLocation + "#/documents/responses")
                  .withName("responses")
                  .withMappedNode(DialectNodes.ResponseObject.id)
              ))
          )
      )

    d.withExternals(Seq(

      External()
        .withId(DialectLocation + "#/externals/schema-org")
        .withAlias("schema-org")
        .withBase(Namespace.Schema.base),

      External()
        .withId(DialectLocation + "#/externals/shacl")
        .withAlias("shacl")
        .withBase(Namespace.Shacl.base),

      External()
        .withId(DialectLocation + "#/externals/hydra")
        .withAlias("hydra")
        .withBase(Namespace.Hydra.base),

      External()
        .withId(DialectLocation + "#/externals/meta")
        .withAlias("meta")
        .withBase(Namespace.Meta.base),

      External()
        .withId(DialectLocation + "#/externals/owl")
        .withAlias("owl")
        .withBase(Namespace.Owl.base)

    ))

    val vocabularies = Seq(
      ModelVocabularies.AmlDoc,
      ModelVocabularies.Http,
      ModelVocabularies.Shapes,
      ModelVocabularies.Meta,
      ModelVocabularies.Security
    )
    d.annotations += Aliases(vocabularies.map { vocab =>
      (vocab.alias, (vocab.base, vocab.filename))
    }.toSet)

   d.withReferences(vocabularies.map { vocab =>
     Vocabulary()
       .withLocation(vocab.filename)
       .withId(vocab.filename)
       .withBase(vocab.base)
   })

    d
  }

  def apply(): Dialect = dialect
}
