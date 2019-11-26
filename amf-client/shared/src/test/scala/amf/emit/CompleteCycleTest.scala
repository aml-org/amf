package amf.emit

import amf.core.remote._
import amf.facades.Validation
import amf.io.FunSuiteCycleTests

class CompleteCycleTest extends FunSuiteCycleTests {

  override val basePath = "amf-client/shared/src/test/resources/upanddown/"
  val base08Path        = "amf-client/shared/src/test/resources/upanddown/raml08/"
  val referencesPath    = "amf-client/shared/src/test/resources/references/"
  val productionPath    = "amf-client/shared/src/test/resources/production/"
  val validationsPath   = "amf-client/shared/src/test/resources/validations/"
  val apiPath           = "amf-client/shared/src/test/resources/api/"

  test("Full oas to oas test") {
    cycle("full-example.json", OasJsonHint)
  }

  test("Full raml to oas test") {
    cycle("full-example.raml", "full-example.raml.json", RamlYamlHint, Oas)
  }

  test("Full oas to raml test") {
    cycle("full-example.json", "full-example.json.raml", OasJsonHint, Raml)
  }

  test("Default response to raml test") {
    cycle("default-response.json", "default-response.json.raml", OasJsonHint, Raml)
  }

  test("Default response to raml test 2") {
    cycle("default-response.json", OasJsonHint)
  }

  test("Parse default response example form raml from oas test") {
    cycle("default-response.json.raml", RamlYamlHint)
  }

  test("Full oas to amf test") {
    cycle("full-example.json", "full-example.json.jsonld", OasJsonHint, Amf)
  }

  test("Orphan extensions oas to amf test") {
    cycle("orphan_extensions.json", "orphan_extensions.jsonld", OasJsonHint, Amf)
  }

  test("Orphan extensions amf to oas test") {
    cycle("orphan_extensions.jsonld", "orphan_extensions.json", AmfJsonHint, Oas)
  }

  test("Traits and resourceTypes oas to amf test") {
    cycle("traits-resource-types.json", "traits-resource-types.json.jsonld", OasJsonHint, Amf)
  }

  test("Traits and resourceTypes oas to oas test") {
    cycle("traits-resource-types.json", OasJsonHint)
  }

  test("Traits and resourceTypes raml to oas test") {
    cycle("traits-resource-types.raml", "traits-resource-types.raml.json", RamlYamlHint, Oas)
  }

//  test("Full cycle raml to amf test") {
//    cycle("api.raml", RamlYamlHint, Amf)
//  }

  test("Basic cycle for amf") {
    cycle("basic.jsonld", AmfJsonHint)
  }

  test("Basic cycle for oas") {
    cycle("basic.json", OasJsonHint)
  }

  test("Basic oas to amf test") {
    cycle("basic.json", "basic.json.jsonld", OasJsonHint, Amf)
  }

  test("Basic amf(oas) to oas test") {
    cycle("basic.json.jsonld", "basic.json", AmfJsonHint, Oas)
  }

  test("Basic raml to oas test") {
    cycle("basic.raml", "basic.raml.json", RamlYamlHint, Oas)
  }

  test("Basic oas to raml test") {
    cycle("basic.json", "basic.json.raml", OasJsonHint, Raml)
  }

  test("Complete amf to amf test") {
    cycle("complete.jsonld", AmfJsonHint)
  }

  test("Complete raml to oas test") {
    cycle("complete.raml", "complete.json", RamlYamlHint, Oas)
  }

  test("Complete oas to amf test") {
    cycle("complete.json", "complete.json.jsonld", OasJsonHint, Amf)
  }

  test("Complete oas to raml test") {
    cycle("complete.json", "complete.raml", OasJsonHint, Raml)
  }

  test("Complete oas to oas test") {
    cycle("complete.json", OasJsonHint)
  }

  test("Complete amf(oas) to oas test") {
    cycle("complete.json.jsonld", "complete.json", AmfJsonHint, Oas)
  }

  test("Endpoints amf to amf test") {
    cycle("endpoints.jsonld", AmfJsonHint)
  }

  test("Endpoints raml to oas test") {
    cycle("endpoints.raml", "endpoints.json", RamlYamlHint, Oas)
  }

  test("Endpoints oas to raml test") {
    cycle("endpoints.json", "endpoints.json.raml", OasJsonHint, Raml)
  }

  test("Endpoints oas to amf test") {
    cycle("endpoints.json", "endpoints.json.jsonld", OasJsonHint, Amf)
  }

  test("Endpoints oas to oas test") {
    cycle("endpoints.json", OasJsonHint)
  }

  test("Endpoints amf(oas) to oas test") {
    cycle("endpoints.json.jsonld", "endpoints.json", AmfJsonHint, Oas)
  }

  test("Complete with operations raml to oas test") {
    cycle("complete-with-operations.raml", "complete-with-operations.json", RamlYamlHint, Oas)
  }

  test("Complete with operations oas to raml test") {
    cycle("complete-with-operations.json", "complete-with-operations.json.raml", OasJsonHint, Raml)
  }

  test("Complete with operations oas to oas test") {
    cycle("complete-with-operations.json", OasJsonHint)
  }

  test("Complete with request oas to raml test") {
    cycle("operation-request.json", "operation-request.json.raml", OasJsonHint, Raml)
  }

  test("Complete with request raml to oas test") {
    cycle("operation-request.raml", "operation-request.raml.json", RamlYamlHint, Oas)
  }

  test("Complete with response oas to raml test") {
    cycle("operation-response.json", "operation-response.raml", OasJsonHint, Raml)
  }

  test("Complete with response oas to oas test") {
    cycle("operation-response.json", OasJsonHint)
  }

  test("Complete with response raml to oas test") {
    cycle("operation-response.raml", "operation-response.raml.json", RamlYamlHint, Oas)
  }

  test("Complete with parameter references oas to oas test") {
    cycle("parameters.json", OasJsonHint)
  }

  test("Complete with formData parameter references oas to amf test") {
    cycle("formDataParameters.json", "formDataParameters.jsonld", OasJsonHint, Amf)
  }

  test("Complete with formData parameter references amf to oas test") {
    cycle("formDataParameters.jsonld", "formDataParameters.json", AmfJsonHint, Oas)
  }

  test("Complete with multiple formData parameters oas to amf test") {
    cycle("formdata-parameters-multiple.yaml", "formdata-parameters-multiple.jsonld", OasYamlHint, Amf)
  }

  test("Complete with formData parameter references oas to oas test") {
    cycle("formDataParameters.json", OasJsonHint)
  }

  test("Complete with formData parameter references oas to raml test") {
    cycle("formDataParameters.json", "formDataParameters.raml", OasJsonHint, Raml)
  }

  test("Complete with parameter references oas to amf test") {
    cycle("parameters.json", "parameters.json.jsonld", OasJsonHint, Amf)
  }

  test("Complete with parameter references amf to oas test") {
    cycle("parameters.json.jsonld", "parameters.json", AmfJsonHint, Oas)
  }

  test("Complete with parameter references oas to raml test") {
    cycle("parameters.json", "parameters.raml", OasJsonHint, Raml)
  }

  test("Complete with payloads raml to oas test") {
    cycle("payloads.raml", "payloads.raml.json", RamlYamlHint, Oas)
  }

  test("Complete with payloads oas to oas test") {
    cycle("payloads.json", OasJsonHint)
  }

  test("Complete with payloads oas to raml test") {
    cycle("payloads.json", "payloads.json.raml", OasJsonHint, Raml)
  }

  test("Types amf(raml) to amf test") {
    cycle("types.raml.jsonld", "types.raml.jsonld", AmfJsonHint, Amf)
  }

  test("Types implicit & explicit oas to oas test") {
    cycle("explicit-&-implicit-type-object.json", OasJsonHint)
  }

  test("Types implicit & explicit raml to oas test") {
    cycle("explicit-&-implicit-type-object.raml", "explicit-&-implicit-type-object.raml.json", RamlYamlHint, Oas)
  }

  test("Types implicit & explicit oas to raml test") {
    cycle("explicit-&-implicit-type-object.json", "explicit-&-implicit-type-object.json.raml", OasJsonHint, Raml)
  }

  test("Types dependency oas to oas test") {
    cycle("types-dependency.json", OasJsonHint)
  }

  test("Types dependency raml to oas test") {
    cycle("types-dependency.raml", "types-dependency.raml.json", RamlYamlHint, Oas)
  }

  test("Types dependency oas to raml test") {
    cycle("types-dependency.json", "types-dependency.json.raml", OasJsonHint, Raml)
  }

  test("Types dependency oas to amf test") {
    cycle("types-dependency.json", "types-dependency.json.jsonld", OasJsonHint, Amf)
  }

  ignore("Types dependency amf(oas) to oas test") {
    cycle("types-dependency.json.jsonld", "types-dependency.json", AmfJsonHint, Oas)
  }

  test("Types declarations oas to oas test") {
    cycle("declarations-small.json", OasJsonHint)
  }

  test("Types all facets oas to oas test") {
    cycle("types-facet.json", OasJsonHint)
  }

  test("Types all facets oas to raml test") {
    cycle("types-facet.json", "types-facet.json.raml", OasJsonHint, Raml)
  }

  test("Types all facets oas to jsonld test") {
    cycle("types-facet.json", "types-facet.json.jsonld", OasJsonHint, Amf)
  }

  test("Types all facets jsonld to oas test") {
    cycle("types-facet.json.jsonld", "types-facet.json.jsonld.json", AmfJsonHint, Oas)
  }

  test("Annotations in Scalars raml to oas") {
    cycle("annotations-scalars.raml", "annotations-scalars.json", RamlYamlHint, Oas)
  }

  test("Petstore oas to raml") {
    cycle("petstore/petstore.json", "petstore/petstore.raml", OasJsonHint, Raml10)
  }

  test("Petstore oas to jsonld") {
    cycle("petstore/petstore.json", "petstore/petstore.jsonld", OasJsonHint, Amf)
  }

  test("Annotations jsonld to jsonld test") {
    cycle("annotations.raml.jsonld", "annotations.raml.jsonld", AmfJsonHint, Amf)
  }

  test("Annotations oas to jsonld test") {
    cycle("annotations.json", "annotations.json.jsonld", OasJsonHint, Amf)
  }

  test("Annotations oas to oas test") {
    cycle("annotations.json", OasJsonHint)
  }

  test("Types all types oas to oas test") {
    cycle("all-type-types.json", OasJsonHint)
  }

  test("Types all types raml to oas test") {
    cycle("all-type-types.raml", "all-type-types.raml.json", RamlYamlHint, Oas)
  }

  test("Types all types oas to raml test") {
    cycle("all-type-types.json", "all-type-types.json.raml", OasJsonHint, Raml)
  }

  test("Test libraries oas to oas") {
    cycle("libraries.json", OasJsonHint, referencesPath)
  }

  test("Test libraries oas to amf") {
    cycle("libraries.json", "libraries.json.jsonld", OasJsonHint, Amf, referencesPath)
  }

  test("Test libraries amf to oas") {
    cycle("libraries.json.jsonld", "libraries.json", AmfJsonHint, Oas, referencesPath)
  }

  test("Test data type fragment amf to amf") {
    cycle("data-type-fragment.raml.jsonld", AmfJsonHint, referencesPath)
  }

  test("Test data type fragment oas to oas") {
    cycle("data-type-fragment.json", OasJsonHint, referencesPath)
  }

  test("Test data type fragment oas to amf") {
    cycle("data-type-fragment.json", "data-type-fragment.json.jsonld", OasJsonHint, Amf, referencesPath)
  }

  test("Test data type fragment amf to oas") {
    cycle("data-type-fragment.json.jsonld", "data-type-fragment.json", AmfJsonHint, Oas, referencesPath)
  }

  // todo what we do when library file name changes changes on dump
  ignore("Test libraries raml to oas") {
    cycle("libraries.raml", "libraries.json.json", RamlYamlHint, Oas, referencesPath)
  }

  ignore("Test libraries oas to raml") {
    cycle("libraries.json", "libraries.raml.raml", OasJsonHint, Raml, referencesPath)
  }

  ignore("Overlay fragment oas to amf") {
    cycle("overlay.json", "overlay.json.jsonld", OasJsonHint, Amf, referencesPath + "extensions/")
  }

  ignore("Overlay fragment oas to oas") {
    cycle("overlay.json", "overlay.json.json", OasJsonHint, Oas, referencesPath + "extensions/")
  }

  ignore("Extension fragment oas to amf") {
    cycle("extension.json", "extension.json.jsonld", OasJsonHint, Amf, referencesPath + "extensions/")
  }

  ignore("Extension fragment oas to oas") {
    cycle("extension.json", "extension.json.json", OasJsonHint, Oas, referencesPath + "extensions/")
  }

  test("Extension fragment jsonld to oas") {
    cycle("extension.json.jsonld", "extension.json.json", AmfJsonHint, Oas, referencesPath + "extensions/")
  }

  test("Overlay fragment jsonld to oas") {
    cycle("overlay.json.jsonld", "overlay.json.json", AmfJsonHint, Oas, referencesPath + "extensions/")
  }

  test("More types raml to oas test") {
    cycle("more-types.raml", "more-types.raml.json", RamlYamlHint, Oas)
  }

  test("Types forward references oas to oas test") {
    cycle("forward-references-types.json", OasJsonHint)
  }

  test("Schema types raml to oas test") {
    cycle("externals.raml", "externals.json", RamlYamlHint, Oas)
  }

  test("Schema types oas to amf test") {
    cycle("externals.json", "externals.json.jsonld", OasJsonHint, Amf)
  }

  test("Schema types amf to oas test") {
    cycle("externals.json.jsonld", "externals.json.jsonld.json", AmfJsonHint, Oas)
  }

  test("Closed node for 0.8 web form test") {
    cycle("closed_web_form.raml", "closed_web_form.json", RamlYamlHint, Oas20, base08Path)
  }

  test("Security schemes oas to amf") {
    cycle("security.json", "security.json.jsonld", OasJsonHint, Amf)
  }

  test("Security schemes oas to oas") {
    cycle("security.json", OasJsonHint)
  }

  test("SecuredBy oas to oas") {
    cycle("secured-by.json", OasJsonHint)
  }

  test("QueryString oas to oas") {
    cycle("query-string.json", OasJsonHint)
  }

  test("QueryString oas to amf") {
    cycle("query-string.json", "query-string.json.jsonld", OasJsonHint, Amf)
  }

  test("QueryString amf to oas") {
    cycle("query-string.json.jsonld", "query-string.json", AmfJsonHint, Oas)
  }

  test("Security with QueryString oas to oas") {
    cycle("security-with-query-string.json", OasJsonHint)
  }

  test("Security with QueryString oas to amf") {
    cycle("security-with-query-string.json", "security-with-query-string.json.jsonld", OasJsonHint, Amf)
  }

  test("Security with QueryString amf to oas") {
    cycle("security-with-query-string.json.jsonld", "security-with-query-string.json", AmfJsonHint, Oas)
  }

  test("Example oas to oas") {
    cycle("examples.json", OasJsonHint)
  }

  test("Example json to amf") {
    cycle("examples.json", "examples.json.jsonld", OasJsonHint, Amf)
  }

  test("Example amf to json") {
    cycle("examples.json.jsonld", "examples.jsonld.json", AmfJsonHint, Oas)
  }

  test("Fragment Named Example oas to oas") {
    cycle("named-example.json", OasJsonHint, referencesPath)
  }

  test("Facets raml to oas") {
    cycle("type-facets.raml", "type-facets.json", RamlYamlHint, Oas)
  }

  test("Facets oas to amf") {
    cycle("type-facets.json", "type-facets.json.jsonld", OasJsonHint, Amf)
  }

  test("Parsing oas shape with description oas to amf") {
    cycle("shapes-with-items.json", "shapes-with-items.jsonld", OasJsonHint, Amf)
  }

  test("OAS descriptions for responses area added automatically raml to oas") {
    cycle("missing_oas_description.raml", "missing_oas_description.json", RamlYamlHint, Oas)
  }

  test("OAS descriptions for responses area added automatically oas to raml") {
    cycle("missing_oas_description.json", "missing_oas_description.json.raml", OasJsonHint, Raml)
  }

  test("SecurityScheme without name raml to oas") {
    cycle("unnamed-security-scheme.raml", "unnamed-security-scheme.raml.json", RamlYamlHint, Oas)
  }

  test("References raml to oas") {
    cycle("with_references.raml", "with_references.json", RamlYamlHint, Oas)
  }

  ignore("References oas to oas") {
    cycle("with_references.json", OasJsonHint)
  }

  ignore("References oas to amf") {
    cycle("with_references.json", "with_references.json.jsonld", OasJsonHint, Amf)
  }

  test("Car oas to oas") {
    cycle("somecars.json", "somecars.json", OasJsonHint, Oas)
  }

  test("Car oas to raml") {
    cycle("somecars.json", "somecars.raml", OasJsonHint, Raml)
  }

  test("konst1 raml to oas") {
    cycle("konst1.raml", "konst1.json", RamlYamlHint, Oas)
  }

  test("konst1 oas to raml") {
    cycle("konst1.json", "konst1.json.raml", OasJsonHint, Raml)
  }

  test("Message for model objects not supported in 08") {
    cycle("array-of-node.raml", "array-of-node-unsupported08.raml", RamlYamlHint, Raml08)
  }

  test("JSON Schema with [{}]") {
    cycle("array-of-node.raml", "array-of-node-unsupported08.raml", RamlYamlHint, Raml08)
  }

  test("Declared response") {
    cycle("declared-responses.json", OasJsonHint)
  }

  test("Declared response oas to raml") {
    cycle("declared-responses.json", "declared-responses.json.raml", OasJsonHint, Raml)
  }

  test("Declared response raml to oas") {
    cycle("declared-responses.json.raml", "declared-responses.json", RamlYamlHint, Oas)
  }

  test("Declared response oas to jsonld") {
    cycle("declared-responses.json", "declared-responses.json.jsonld", OasJsonHint, Amf)
  }

  test("Declared response jsonld to oas") {
    cycle("declared-responses.json.jsonld", "declared-responses.jsonld.json", AmfJsonHint, Oas)
  }

  test("Additional properties shape oas to oas") {
    cycle("additional-properties.json", "additional-properties.json", OasJsonHint, Oas)
  }

  test("Additional properties shape oas to raml") {
    cycle("additional-properties.json", "additional-properties.raml", OasJsonHint, Raml)
  }

  test("Additional properties shape raml to oas") {
    cycle("additional-properties.raml", "additional-properties.raml.json", RamlYamlHint, Oas)
  }

  test("Additional properties shape oas to amf") {
    cycle("additional-properties.json", "additional-properties.jsonld", OasJsonHint, Amf)
  }

  test("CollectionFormat shape oas to amf") {
    cycle("collection-format.json", "collection-format.jsonld", OasJsonHint, Amf)
  }

  test("CollectionFormat shape oas to oas") {
    cycle("collection-format.json", "collection-format.json.json", OasJsonHint, Oas)
  }

  test("CollectionFormat shape oas to raml") {
    cycle("collection-format.json", "collection-format.raml", OasJsonHint, Raml)
  }

  test("CollectionFormat shape amf to oas") {
    cycle("collection-format.jsonld", "collection-format.jsonld.json", AmfJsonHint, Oas)
  }

  test("Anonymous and named examples with annotations json to raml") {
    cycle("anonymous-and-named-examples.jsonld", "anonymous-and-named-examples.raml", AmfJsonHint, Raml)
  }

  test("Tags node oas to amf") {
    cycle("tags.json", "tags.jsonld", OasJsonHint, Amf)
  }

  test("Tags node oas to oas") {
    cycle("tags.json", "tags.json.json", OasJsonHint, Oas)
  }

  test("Tags node oas to raml") {
    cycle("tags.json", "tags.raml", OasJsonHint, Raml)
  }

  test("Tags node amf to oas") {
    cycle("tags.jsonld", "tags.json.json", AmfJsonHint, Oas)
  }

  test("Tags node raml to oas") {
    cycle("tags.raml", "tags.json.json", RamlYamlHint, Oas)
  }

  test("Numeric facets raml to oas") {
    cycle("numeric-facets.raml", "numeric-facets.json", RamlYamlHint, Oas)
  }

  test("Numeric facets jsonld to oas") {
    cycle("numeric-facets.jsonld", "numeric-facets.jsonld.json", AmfJsonHint, Oas)
  }

  ignore("Test api_6109_ver_10147 example raml to amf") {
    cycle("api.raml", "api.yaml.jsonld", RamlYamlHint, Amf, productionPath + "api_6109_ver_10147/")
  }

  ignore("Test suez-delivery-collection example raml to amf") {
    cycle("api.raml",
          "api.yaml.jsonld",
          RamlYamlHint,
          Amf,
          productionPath + "s-suez-delivery-collection-api-1.0.0-fat-raml/")
  }

  test("Response declaration oas to amf") {
    cycle("oas_response_declaration.yaml", "oas_response_declaration.jsonld", OasYamlHint, Amf)
  }

  test("Invalid parameter binding oas to oas") {
    Validation(platform)
      .flatMap { validation =>
        cycle("invalid-parameter-binding.json",
              "invalid-parameter-binding.json.json",
              OasYamlHint,
              Oas,
              validation = Some(validation.withEnabledValidation(true)))
      }
  }

  test("Invalid parameter binding oas to amf") {
    Validation(platform)
      .flatMap { validation =>
        cycle("invalid-parameter-binding.json",
              "invalid-parameter-binding.jsonld",
              OasYamlHint,
              Amf,
              validation = Some(validation.withEnabledValidation(true)))
      }
  }

  test("Test parse types") {
    cycle("shapes.raml", "shapes.json", RamlYamlHint, Oas, apiPath + "types/")
  }

  test("Invalid body parameter oas to oas") {
    Validation(platform)
      .flatMap { validation =>
        cycle("invalid-body-parameter.json",
              "invalid-body-parameter.json.json",
              OasYamlHint,
              Oas,
              validation = Some(validation.withEnabledValidation(true)))
      }
  }

  test("Invalid baseUriParameters without baseUri") {
    Validation(platform)
      .flatMap { validation =>
        cycle("no-base-uri.raml",
              "no-base-uri.raml",
              RamlYamlHint,
              Raml,
              validation = Some(validation.withEnabledValidation(true)))
      }
  }

  test("FormData multiple parameters oas to oas") {
    cycle("form-data-params.json", "form-data-params.json", OasJsonHint, Oas)
  }

  test("FormData multiple parameters oas to amf") {
    cycle("form-data-params.json", "form-data-params.jsonld", OasJsonHint, Amf)
  }

  test("arrayTypes raml to oas") {
    cycle("array_items.raml", "array_items.json", RamlYamlHint, Oas)
  }

  test("PatternProperties JSON Schema oas to raml") {
    cycle("oasPatternProperties.yaml", "oasPatternProperties.raml", OasYamlHint, Raml)
  }

  test("Test enums raml to oas") {
    cycle("enums.raml", "enums.json", RamlYamlHint, Oas, basePath + "enums/")
  }

  test("Test enums oas to oas") {
    cycle("enums.json", "enums.json.json", OasJsonHint, Oas, basePath + "enums/")
  }

  test("Test enums oas to raml") {
    cycle("enums.json", "enums.json.raml", OasJsonHint, Raml, basePath + "enums/")
  }

  test("Test enums raml to amf") {
    cycle("enums.json", "enums.raml.jsonld", OasJsonHint, Amf, basePath + "enums/")
  }

  test("Test nil example raml to raml") {
    cycle("nil-example.raml", "nil-example.raml.raml", RamlYamlHint, Raml)
  }

  test("Test nil example raml generated to itself") {
    cycle("nil-example.raml.raml", "nil-example.raml.raml", RamlYamlHint, Raml)
  }

  test("Security requirement OAS to OAS") {
    cycle("api-with-security-requirement.json", "api-with-security-requirement.json", OasJsonHint, Oas20, validationsPath + "oas-security/")
  }

  test("Security requirements OAS to OAS") {
    cycle("api-with-security-requirements.json", "api-with-security-requirements.json", OasJsonHint, Oas20, validationsPath + "oas-security/")
  }

  test("Security requirements OAS to JSONLD") {
    cycle("api-with-security-requirements.json", "api-with-security-requirements.jsonld", OasJsonHint, Amf, validationsPath + "oas-security/")
  }

  /**
  * Please do not add more test for raml to raml or to jsonld in here. You can use Raml10CycleTestByDirectory or Raml08CycleTestByDirectory.
  * Check CycleTestByDirectory for information off how to use.
  *
  * Raml10 to raml10 -> Raml10CycleTestByDirectory suit
  * Raml08 to Raml08 -> Raml08CycleTestByDirectory suit
  * Production :
  *   * Raml10 to raml10 -> ProductionRaml10CycleTestByDirectory suit
  *   * Raml08 to Raml08 -> ProductionRaml08CycleTestByDirectory suit
  *
  * Oas20 to Oas20 -> Oas20CycleTestByDirectory
  */

}
