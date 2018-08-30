package amf.emit

import amf.core.remote._
import amf.facades.Validation
import amf.io.BuildCycleTests

class CompleteCycleTest extends BuildCycleTests {

  override val basePath = "amf-client/shared/src/test/resources/upanddown/"
  val base08Path        = "amf-client/shared/src/test/resources/upanddown/raml08/"
  val referencesPath    = "amf-client/shared/src/test/resources/references/"
  val productionPath    = "amf-client/shared/src/test/resources/production/"
  val validationsPath   = "amf-client/shared/src/test/resources/validations/"
  val apiPath           = "amf-client/shared/src/test/resources/api/"

  test("Full raml to raml test") {
    cycle("full-example.raml", "full-example.raml.raml", RamlYamlHint, Raml)
  }

  test("Full oas to oas test") {
    cycle("full-example.json", OasJsonHint)
  }

  test("Full raml to oas test") {
    cycle("full-example.raml", "full-example.raml.json", RamlYamlHint, Oas)
  }

  test("Full oas to raml test") {
    cycle("full-example.json", "full-example.json.raml", OasJsonHint, Raml)
  }

  test("Full raml to amf test") {
    cycle("full-example.raml", "full-example.raml.jsonld", RamlYamlHint, Amf)
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

  test("Traits and resourceTypes raml to amf test") {
    cycle("traits-resource-types.raml", "traits-resource-types.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Traits and resourceTypes raml to raml test") {
    cycle("traits-resource-types.raml", RamlYamlHint)
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

  test("Traits and resourceTypes with multiple variable transformations raml to raml test") {
    cycle("resource-type-multi-transformation.raml", RamlYamlHint)
  }

  test("Traits and resourceTypes with complex variables raml to raml test") {
    cycle("resource-type-complex-variables.raml", RamlYamlHint)
  }

//  test("Full cycle raml to amf test") {
//    cycle("full-example.raml", RamlYamlHint, Amf)
//  }

  test("Basic cycle for amf") {
    cycle("basic.jsonld", AmfJsonHint)
  }

  test("Basic cycle for raml") {
    cycle("basic.raml", RamlYamlHint)
  }

  test("Basic cycle for oas") {
    cycle("basic.json", OasJsonHint)
  }

  test("Basic raml to amf test") {
    cycle("basic.raml", "basic.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Basic oas to amf test") {
    cycle("basic.json", "basic.json.jsonld", OasJsonHint, Amf)
  }

  test("Basic amf(raml) to raml test") {
    cycle("basic.raml.jsonld", "basic.raml", AmfJsonHint, Raml)
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

  test("Complete raml to amf test") {
    cycle("complete.raml", "complete.raml.jsonld", RamlYamlHint, Amf)
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

  test("Complete raml to raml test") {
    cycle("complete.raml", RamlYamlHint)
  }

  test("Complete oas to oas test") {
    cycle("complete.json", OasJsonHint)
  }

  test("Complete amf(raml) to raml test") {
    cycle("complete.raml.jsonld", "complete.raml", AmfJsonHint, Raml)
  }

  test("Complete amf(oas) to oas test") {
    cycle("complete.json.jsonld", "complete.json", AmfJsonHint, Oas)
  }

  test("Endpoints amf to amf test") {
    cycle("endpoints.jsonld", AmfJsonHint)
  }

  test("Endpoints raml to amf test") {
    cycle("endpoints.raml", "endpoints.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Endpoints raml to oas test") {
    cycle("endpoints.raml", "endpoints.json", RamlYamlHint, Oas)
  }

  test("Endpoints raml to raml test") {
    cycle("endpoints.raml", RamlYamlHint)
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

  test("Endpoints amf(raml) to raml test") {
    cycle("endpoints.raml.jsonld", "endpoints.raml", AmfJsonHint, Raml)
  }

  test("Endpoints amf(oas) to oas test") {
    cycle("endpoints.json.jsonld", "endpoints.json", AmfJsonHint, Oas)
  }

  test("Complete with operations raml to oas test") {
    cycle("complete-with-operations.raml", "complete-with-operations.json", RamlYamlHint, Oas)
  }

  test("Complete with operations raml to raml test") {
    cycle("complete-with-operations.raml", RamlYamlHint)
  }

  test("Complete with operations oas to raml test") {
    cycle("complete-with-operations.json", "complete-with-operations.json.raml", OasJsonHint, Raml)
  }

  test("Complete with operations oas to oas test") {
    cycle("complete-with-operations.json", OasJsonHint)
  }

  test("Complete with request raml to raml test") {
    cycle("operation-request.raml", "operation-request.raml.raml", RamlYamlHint, Raml)
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

  test("Complete with response raml to raml test") {
    cycle("operation-response.raml", RamlYamlHint)
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

  test("Complete with parameter references raml to amf test") {
    cycle("parameters.raml", "parameters.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Complete with payloads raml to raml test") {
    cycle("payloads.raml", "payloads.raml.raml", RamlYamlHint, Raml)
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

  test("Children endpoints amf to raml test") {
    cycle("banking-api.raml.jsonld", "banking-api.jsonld.raml", AmfJsonHint, Raml)
  }

  test("Children endpoints raml to raml test") {
    cycle("banking-api.raml", "banking-api.raml.raml", RamlYamlHint, Raml)
  }

  test("Children endpoints raml to amf test") {
    cycle("banking-api.raml", "banking-api.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Types raml to amf test") {
    cycle("types.raml", "types.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Types amf(raml) to amf test") {
    cycle("types.raml.jsonld", "types.raml.jsonld", AmfJsonHint, Amf)
  }

  test("Types implicit & explicit raml to raml test") {
    cycle("explicit-&-implicit-type-object.raml", RamlYamlHint)
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

  test("Types dependency raml to raml test") {
    cycle("types-dependency.raml", RamlYamlHint)
  }

  test("Types problems raml to amf") {
    cycle("types_problems.raml", "types_problems.jsonld", RamlYamlHint, Amf)
  }

  test("Types problems amf to jsonld") {
    cycle("types_problems.jsonld", "types_problems.jsonld.raml", AmfJsonHint, Raml)
  }

  test("Types problems2 raml to amf") {
    cycle("types_problems2.raml", "types_problems2.jsonld", RamlYamlHint, Amf)
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

  test("Types dependency raml to amf test") {
    cycle("types-dependency.raml", "types-dependency.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Types dependency oas to amf test") {
    cycle("types-dependency.json", "types-dependency.json.jsonld", OasJsonHint, Amf)
  }

  ignore("Types dependency amf(raml) to raml test") {
    cycle("types-dependency.raml.jsonld", "types-dependency.raml", AmfJsonHint, Raml)
  }

  ignore("Types dependency amf(oas) to oas test") {
    cycle("types-dependency.json.jsonld", "types-dependency.json", AmfJsonHint, Oas)
  }

  test("Types declarations oas to oas test") {
    cycle("declarations-small.json", OasJsonHint)
  }

  test("Types declarations raml to raml test") {
    cycle("declarations-small.raml", RamlYamlHint)
  }

  test("Types all facets raml to raml test") {
    cycle("types-facet.raml", RamlYamlHint)
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

  test("Types all facets raml to oas test") {
    cycle("types-facet.raml", "types-facet.raml.json", RamlYamlHint, Oas)
  }

  test("Types all facets raml to jsonld test") {
    cycle("types-facet.raml", "types-facet.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Types all facets jsonld to raml test") {
    cycle("types-facet.raml.jsonld", "types-facet.raml.jsonld.raml", AmfJsonHint, Raml)
  }

  test("Types all types raml to raml test") {
    cycle("all-type-types.raml", RamlYamlHint)
  }

  test("Multiple inheritance raml to raml") {
    cycle("multiple-inheritance.raml", "multiple-inheritance.raml.raml", RamlYamlHint, Raml)
  }

  test("Annotations raml to raml test") {
    cycle("annotations.raml", RamlYamlHint)
  }

  test("Annotations Full raml to raml test") {
    cycle("annotations-full.raml", RamlYamlHint)
  }

  test("Annotations in Scalars raml to raml test") {
    cycle("annotations-scalars.raml", RamlYamlHint)
  }

  test("Annotations in Scalars raml to jsonld test") {
    cycle("annotations-scalars.raml", "annotations-scalars.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Annotations in Scalars raml to raml") {
    cycle("annotations-scalars.raml", "annotations-scalars.raml.raml", RamlYamlHint, Raml)
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

  test("Annotations in Scalars jsonld to raml test") {
    cycle("annotations-scalars.raml.jsonld", "annotations-scalars.jsonld.raml", AmfJsonHint, Raml)
  }

  test("Annotations raml to jsonld test") {
    cycle("annotations.raml", "annotations.raml.jsonld", RamlYamlHint, Amf)
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

  test("Test libraries raml to raml") {
    cycle("libraries.raml", RamlYamlHint, referencesPath)
  }

  test("Test multiple aliases libraries raml to raml") {
    cycle("libraries-3-alias.raml", RamlYamlHint, referencesPath)
  }

  test("Test multiple aliases libraries raml to amf") {
    cycle("libraries-3-alias.raml", "libraries-3-alias.raml.jsonld", RamlYamlHint, Amf, referencesPath)
  }

  test("Test multiple aliases libraries amf to raml") {
    cycle("libraries-3-alias.raml.jsonld", "libraries-3-alias.raml", AmfJsonHint, Raml, referencesPath)
  }

  test("Test libraries oas to oas") {
    cycle("libraries.json", OasJsonHint, referencesPath)
  }

  test("Test libraries raml to amf") {
    cycle("libraries.raml", "libraries.raml.jsonld", RamlYamlHint, Amf, referencesPath)
  }

  test("Test libraries amf to raml") {
    cycle("libraries.raml.jsonld", "libraries.raml", AmfJsonHint, Raml, referencesPath)
  }

  test("Test libraries oas to amf") {
    cycle("libraries.json", "libraries.json.jsonld", OasJsonHint, Amf, referencesPath)
  }

  test("Test libraries amf to oas") {
    cycle("libraries.json.jsonld", "libraries.json", AmfJsonHint, Oas, referencesPath)
  }

  test("Test fragment usage raml to amf") {
    cycle("fragment_usage.raml", "fragment_usage.jsonld", RamlYamlHint, Amf)
  }

  test("Test fragment usage amf to raml") {
    cycle("fragment_usage.jsonld", "fragment_usage.raml", AmfJsonHint, Raml)
  }

  test("Test boolean in key raml to amf") {
    cycle("boolean_in_key.raml", "boolean_in_key.jsonld", RamlYamlHint, Amf)
  }

  test("Test boolean in key amf to raml") {
    cycle("boolean_in_key.jsonld", "boolean_in_key.raml", AmfJsonHint, Raml)
  }

  test("Test missing example raml to raml") {
    cycle("missing_example.raml", "missing_example.raml.raml", RamlYamlHint, Raml)
  }

  test("Test data type fragment raml to raml") {
    cycle("data-type-fragment.raml", RamlYamlHint, referencesPath)
  }

  test("Test data type fragment amf to raml") {
    cycle("data-type-fragment.raml.jsonld", "data-type-fragment.raml", AmfJsonHint, Raml, referencesPath)
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

  test("resource type fragment raml to raml") {
    cycle("resource-type-fragment.raml", RamlYamlHint, referencesPath)
  }

  test("trait fragment raml to raml") {
    cycle("trait-fragment.raml", RamlYamlHint, referencesPath)
  }

  // todo what we do when library file name changes changes on dump
  ignore("Test libraries raml to oas") {
    cycle("libraries.raml", "libraries.json.json", RamlYamlHint, Oas, referencesPath)
  }

  ignore("Test libraries oas to raml") {
    cycle("libraries.json", "libraries.raml.raml", OasJsonHint, Raml, referencesPath)
  }

  test("Extension fragment raml to amf") {
    cycle("extension.raml", "extension.raml.jsonld", RamlYamlHint, Amf, referencesPath + "extensions/")
  }

  test("Extension fragment raml to raml") {
    cycle("extension.raml", "extension.raml.raml", RamlYamlHint, Raml, referencesPath + "extensions/")
  }

  test("Overlay fragment raml to amf") {
    cycle("overlay.raml", "overlay.raml.jsonld", RamlYamlHint, Amf, referencesPath + "extensions/")
  }

  test("Overlay fragment raml to raml") {
    cycle("overlay.raml", "overlay.raml.raml", RamlYamlHint, Raml, referencesPath + "extensions/")
  }

  test("Test libraries references in delares raml to raml") {
    cycle("lib-alias-reference.raml", RamlYamlHint, referencesPath)
  }

  test("Overlay fragment oas to amf") {
    cycle("overlay.json", "overlay.json.jsonld", OasJsonHint, Amf, referencesPath + "extensions/")
  }

  test("Overlay fragment oas to oas") {
    cycle("overlay.json", "overlay.json.json", OasJsonHint, Oas, referencesPath + "extensions/")
  }

  test("Extension fragment oas to amf") {
    cycle("extension.json", "extension.json.jsonld", OasJsonHint, Amf, referencesPath + "extensions/")
  }

  test("Extension fragment oas to oas") {
    cycle("extension.json", "extension.json.json", OasJsonHint, Oas, referencesPath + "extensions/")
  }

  test("Extension fragment jsonld to oas") {
    cycle("extension.json.jsonld", "extension.json.json", AmfJsonHint, Oas, referencesPath + "extensions/")
  }

  test("Overlay fragment jsonld to oas") {
    cycle("overlay.json.jsonld", "overlay.json.json", AmfJsonHint, Oas, referencesPath + "extensions/")
  }

  test("Extension fragment jsonld to raml") {
    cycle("extension.raml.jsonld", "extension.raml.raml", AmfJsonHint, Raml, referencesPath + "extensions/")
  }

  test("Overlay fragment jsonld to raml") {
    cycle("overlay.raml.jsonld", "overlay.raml.raml", AmfJsonHint, Raml, referencesPath + "extensions/")
  }

  test("More types raml to raml test") {
    cycle("more-types.raml", "more-types.raml.raml", RamlYamlHint, Raml)
  }

  test("More types raml to oas test") {
    cycle("more-types.raml", "more-types.raml.json", RamlYamlHint, Oas)
  }

  test("Types forward references raml to raml test") {
    cycle("forward-references-types.raml", "forward-references-types.raml.raml", RamlYamlHint, Raml)
  }

  test("Types forward references oas to oas test") {
    cycle("forward-references-types.json", OasJsonHint)
  }

  test("Types forward inherits references raml to raml test") {
    cycle("forward-inherits-reference.raml", "forward-inherits-reference.raml", RamlYamlHint, Raml)
  }

  test("Empty union fixed problem raml to jsonld test") {
    cycle("empty_union.raml", "empty_union.jsonld", RamlYamlHint, Amf)
  }

  test("Schema types raml to amf test") {
    cycle("externals.raml", "externals.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Schema types jsonld to raml test") {
    cycle("externals.raml.jsonld", "externals.raml.jsonld.raml", AmfJsonHint, Raml)
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

  test("Schema wrapped in array test") {
    cycle("missing_payload.raml", "missing_payload.jsonld", RamlYamlHint, Amf, base08Path)
  }

  test("Closed node for 0.8 web form test") {
    cycle("closed_web_form.raml", "closed_web_form.json", RamlYamlHint, Oas20, base08Path)
  }

  test("Closed node for 0.8 empty payload schema test") {
    cycle("empty_payload.raml", "empty_payload.jsonld", RamlYamlHint, Amf, base08Path)
  }

  test("Default value test") {
    cycle("default_value.raml", "default_value.jsonld", RamlYamlHint, Amf, base08Path)
  }

  test("Parsing missing payload value") {
    cycle("json_schema_array.raml", "json_schema_array.raml.jsonld", RamlYamlHint, Amf, base08Path)
  }

  test("Trailing space test") {
    cycle("americanflightapi.raml", "americanflightapi.raml.jsonld", RamlYamlHint, Amf, base08Path)
  }

  test("Number title test") {
    cycle("number_title.raml", "number_title.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Security schemes raml to amf") {
    cycle("security.raml", "security.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Security schemes raml to raml") {
    cycle("declared-security-schemes.raml", RamlYamlHint)
  }

  test("Security schemes oas to amf") {
    cycle("security.json", "security.json.jsonld", OasJsonHint, Amf)
  }

  test("Security schemes oas to oas") {
    cycle("security.json", OasJsonHint)
  }

  test("SecuredBy raml to raml") {
    cycle("secured-by.raml", RamlYamlHint)
  }

  test("SecuredBy raml to amf") {
    cycle("secured-by.raml", "secured-by.jsonld", RamlYamlHint, Amf)
  }

  test("SecuredBy amf to raml") {
    cycle("secured-by.jsonld", "secured-by.jsonld.raml", AmfJsonHint, Raml)
  }

  test("SecuredBy oas to oas") {
    cycle("secured-by.json", OasJsonHint)
  }

  test("QueryString raml to raml") {
    cycle("query-string.raml", "query-string.raml.raml", RamlYamlHint, Raml)
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

  test("QueryString raml to amf") {
    cycle("query-string.raml", "query-string.raml.jsonld", RamlYamlHint, Amf)
  }

  test("QueryString amf to raml") {
    cycle("query-string.raml.jsonld", "query-string.raml.raml", AmfJsonHint, Raml)
  }

  test("Security with QueryString raml to raml") {
    cycle("security-with-query-string.raml", RamlYamlHint)
  }

  test("Security with QueryString raml to amf") {
    cycle("security-with-query-string.raml", "security-with-query-string.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Security with QueryString amf to raml") {
    cycle("security-with-query-string.raml.jsonld", "security-with-query-string.raml", AmfJsonHint, Raml)
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

  test("Example raml to raml") {
    cycle("examples.raml", RamlYamlHint)
  }

  test("Array Example raml to raml") {
    cycle("array-example.raml", "array-example.raml.raml", RamlYamlHint, Raml)
  }

  test("Example oas to oas") {
    cycle("examples.json", OasJsonHint)
  }

  test("Example raml to amf") {
    cycle("examples.raml", "examples.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Example amf to raml") {
    cycle("examples.raml.jsonld", "examples.raml", AmfJsonHint, Raml)
  }

  test("Example json to amf") {
    cycle("examples.json", "examples.json.jsonld", OasJsonHint, Amf)
  }

  test("Example amf to json") {
    cycle("examples.json.jsonld", "examples.jsonld.json", AmfJsonHint, Oas)
  }

  test("Fragment Named Example raml to raml") {
    cycle("named-example.raml", RamlYamlHint, referencesPath)
  }

  test("Fragment Named Example oas to oas") {
    cycle("named-example.json", OasJsonHint, referencesPath)
  }

  test("External Fragment raml to amf") {
    cycle("jukebox-api.raml", "jukebox-api.jsonld", RamlYamlHint, Amf, referencesPath)
  }

  test("External Fragment raml to raml") {
    cycle("jukebox-api.raml", "jukebox-api.raml.raml", RamlYamlHint, Raml, referencesPath)
  }

  test("External Fragment amf to raml") {
    cycle("jukebox-api.jsonld", "jukebox-api.raml.raml", AmfJsonHint, Raml, referencesPath)
  }

  test("Facets raml to raml") {
    cycle("type-facets.raml", RamlYamlHint)
  }

  test("Facets raml to amf") {
    cycle("type-facets.raml", "type-facets.jsonld", RamlYamlHint, Amf)
  }

  test("Facets amf to raml") {
    cycle("type-facets.jsonld", "type-facets.raml", AmfJsonHint, Raml)
  }

  test("Facets raml to oas") {
    cycle("type-facets.raml", "type-facets.json", RamlYamlHint, Oas)
  }

  test("Facets oas to amf") {
    cycle("type-facets.json", "type-facets.json.jsonld", OasJsonHint, Amf)
  }

  test("Annotations with type expressions raml to raml") {
    cycle("annotations-type-expressions.raml", "annotations-type-expressions.raml.raml", RamlYamlHint, Raml)
  }

  test("Parsing default types raml to amf") {
    cycle("default-types.raml", "default-types.jsonld", RamlYamlHint, Amf)
  }

  test("Parsing default types amf to raml") {
    cycle("default-types.jsonld", "default-types.raml", AmfJsonHint, Raml)
  }

  test("Parsing example of matrixShape raml to amf") {
    cycle("matrix.raml", "matrix.jsonld", RamlYamlHint, Amf)
  }

  test("Parsing oas shape with description oas to amf") {
    cycle("shapes-with-items.json", "shapes-with-items.jsonld", OasJsonHint, Amf)
  }

  test("Parsing example fragment raml to raml") {
    cycle("example-fragment.raml", "example-fragment.raml", RamlYamlHint, Raml)
  }

  test("Parsing example of matrixShape amf to raml") {
    cycle("matrix.jsonld", "matrix.raml.raml", AmfJsonHint, Raml)
  }

  test("Parsing example of multiple inheritance raml to amf") {
    cycle("multiple-inheritance-2.raml", "multiple-inheritance-2.jsonld", RamlYamlHint, Amf)
  }

  test("Parsing example of multiple inheritance amf to raml") {
    cycle("multiple-inheritance-2.jsonld", "multiple-inheritance-2.raml.raml", AmfJsonHint, Raml)
  }

  test("Type closed true raml to amf") {
    cycle("type-closed-true.raml", "type-closed-true.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Type closed true amf to raml") {
    cycle("type-closed-true.raml.jsonld", "type-closed-true.raml", AmfJsonHint, Raml)
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

  test("Multiple media types test") {
    cycle("multiple-media-types.raml", "multiple-media-types.raml.raml", RamlYamlHint, Raml)
  }

  test("References raml to amf") {
    cycle("with_references.raml", "with_references.raml.jsonld", RamlYamlHint, Amf)
  }

  test("References raml to raml") {
    cycle("with_references.raml", RamlYamlHint)
  }

  test("References raml to oas") {
    cycle("with_references.raml", "with_references.json", RamlYamlHint, Oas)
  }

  test("References oas to oas") {
    cycle("with_references.json", OasJsonHint)
  }

  test("References oas to amf") {
    cycle("with_references.json", "with_references.json.jsonld", OasJsonHint, Amf)
  }

  test("Car oas to oas") {
    cycle("somecars.json", "somecars.json", OasJsonHint, Oas)
  }

  test("Car oas to raml") {
    cycle("somecars.json", "somecars.raml", OasJsonHint, Raml)
  }

  test("konst1 raml to amf") {
    cycle("konst1.raml", "konst1.jsonld", RamlYamlHint, Amf)
  }

  test("konst1 raml to raml") {
    cycle("konst1.raml", "konst1.raml.raml", RamlYamlHint, Raml)
  }

  test("konst1 amf to raml") {
    cycle("konst1.jsonld", "konst1.jsonld.raml", AmfJsonHint, Raml)
  }

  test("konst1 raml to oas") {
    cycle("konst1.raml", "konst1.json", RamlYamlHint, Oas)
  }

  test("konst1 oas to raml") {
    cycle("konst1.json", "konst1.json.raml", OasJsonHint, Raml)
  }

  test("Enum inheritance raml to amf") {
    cycle("enum-inheritance.raml", "enum-inheritance.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Enum inheritance amf to raml") {
    cycle("enum-inheritance.raml.jsonld", "enum-inheritance.jsonld.raml", AmfJsonHint, Raml)
  }

  test("Types declarations with square bracket") {
    cycle("type-declared-with-square-bracket.raml", "type-declared-with-square-bracket.raml.raml", RamlYamlHint, Raml)
  }

  test("API with implicit parameters raml to amf") {
    cycle("users_accounts.raml", "users_accounts.jsonld", RamlYamlHint, Amf)
  }

  test("API with implicit parameters amf to raml") {
    cycle("users_accounts.jsonld", "users_accounts.raml", AmfJsonHint, Raml)
  }

  test("API with implicit object definition APIMF-376") {
    cycle("implicitly-defined-object.raml", "implicitly-defined-object.raml.raml", RamlYamlHint, Raml)
  }

  test("API with uriParameters in endpoint raml to raml") {
    cycle("define-uri-parameters.raml", "define-uri-parameters.raml", RamlYamlHint, Raml08)
  }

  test("Message for model objects not supported in 08") {
    cycle("array-of-node.raml", "array-of-node-unsupported08.raml", RamlYamlHint, Raml08)
  }

  test("JSON Schema with [{}]") {
    cycle("array-of-node.raml", "array-of-node-unsupported08.raml", RamlYamlHint, Raml08)
  }

  test("Single Array Value") {
    cycle("single-array-value.raml", RamlYamlHint)
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

  test("Generic number entries validation") {
    cycle("generic-number-entries.raml", RamlYamlHint)
  }

  test("Numeric facets raml to raml") {
    cycle("numeric-facets.raml", "numeric-facets.raml.raml", RamlYamlHint, Raml)
  }

  test("Numeric facets raml to oas") {
    cycle("numeric-facets.raml", "numeric-facets.json", RamlYamlHint, Oas)
  }

  test("Numeric facets raml to jsonld") {
    cycle("numeric-facets.raml", "numeric-facets.jsonld", RamlYamlHint, Amf)
  }

  test("Numeric facets jsonld to raml") {
    cycle("numeric-facets.jsonld", "numeric-facets.jsonld.raml", AmfJsonHint, Raml)
  }

  test("Numeric facets jsonld to oas") {
    cycle("numeric-facets.jsonld", "numeric-facets.jsonld.json", AmfJsonHint, Oas)
  }

  test("Repeated include xsd") {
    cycle("include-repeated.raml", "include-repeated.raml.raml", RamlYamlHint, Raml)
  }

  test("Path with empty spaces") {
    cycle("bells with spaces.raml", "bells with spaces.jsonld", RamlYamlHint, Amf)
  }

  test("File types single value") {
    cycle("file_types.raml", "file_types.jsonld", RamlYamlHint, Amf)
  }

  test("int uri param raml to amf") {
    cycle("int_uri_param.raml", "int_uri_param.jsonld", RamlYamlHint, Amf)
  }

  test("int uri param raml to raml") {
    cycle("int_uri_param.raml", "int_uri_param.raml.raml", RamlYamlHint, Raml)
  }

  test("file type expression raml to amf") {
    cycle("file_type_expression.raml", "file_type_expression.jsonld", RamlYamlHint, Amf)
  }

  test("type nil shortcut raml to amf") {
    cycle("type_nil_shortcut.raml", "type_nil_shortcut.jsonld", RamlYamlHint, Amf)
  }

  test("xsd example raml to amf") {
    cycle("basic_with_xsd.raml", "basic_with_xsd.jsonld", RamlYamlHint, Amf)
  }

  test("production from exchange raml to amf") {
    cycle("locations-api.raml", "locations-api.jsonld", RamlYamlHint, Amf, productionPath + "locations-api/")
  }

  ignore("Test api_6109_ver_10147 example raml to amf") {
    cycle("api.raml", "api.jsonld", RamlYamlHint, Amf, productionPath + "api_6109_ver_10147/")
  }

  ignore("Test suez-delivery-collection example raml to amf") {
    cycle("api.raml",
          "api.jsonld",
          RamlYamlHint,
          Amf,
          productionPath + "s-suez-delivery-collection-api-1.0.0-fat-raml/")
  }

  test("Test definitons-loops example raml to amf") {
    cycle("input.raml", "input.jsonld", RamlYamlHint, Amf, productionPath + "definitions-loops/")
  }

  test("Test definitons-loops crossfile raml to amf") {
    cycle("crossfiles.raml", "crossfiles.jsonld", RamlYamlHint, Amf, productionPath + "definitions-loops/")
  }

  test("Test financial-api/othercases example raml to amf") {
    cycle("api.raml", "api.raml.jsonld", RamlYamlHint, Amf, productionPath + "othercases/jsonschema/")
  }

  test("production from exchange raml to raml") {
    cycle("locations-api.raml", "locations-api.raml", RamlYamlHint, Raml, productionPath + "locations-api/")
  }

  test("Example issue nil raml to amf") {
    cycle("api.raml", "api.jsonld", RamlYamlHint, Amf, validationsPath + "examples/inline-named-examples/")
  }

  test("Examples usage in raml 1.0 spec raml to amf") {
    cycle("spec_examples_example.raml", "spec_examples_example.jsonld", RamlYamlHint, Amf, productionPath)
  }

  test("Example in included type fragment") {
    cycle("simple_example_type.raml", "simple_example_type.jsonld", RamlYamlHint, Amf)
  }

  test("Description in XML Schema shape") {
    cycle("simple_xml_schema.raml", "simple_xml_schema.jsonld", RamlYamlHint, Amf)
  }

  test("Response declaration oas to amf") {
    cycle("oas_response_declaration.yaml", "oas_response_declaration.jsonld", OasYamlHint, Amf)
  }

  test("Forward reference in shape with custom properties") {
    cycle("forward-shape-custom-properties.raml", "forward-shape-custom-properties.raml.raml", RamlYamlHint, Raml)
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

  test("Trait with string quoted data node raml to raml") {
    cycle("trait-string-quoted-node.raml", "trait-string-quoted-node.raml.raml", RamlYamlHint, Raml)
  }

  test("Bad fragment named examples") {
    cycle("api.raml", "api.raml.raml", RamlYamlHint, Raml, basePath + "sapi-customer/")
  }

  test("Test parse references with [] in path") {
    cycle("api.raml", "api.raml.raml", RamlYamlHint, Raml, basePath + "chars[]infilespaths/")
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

  test("Matrix shape id") {
    cycle("matrix-id.raml", "matrix-id.jsonld", RamlYamlHint, Amf)
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

  test("File type detection") {
    cycle("file-detection.raml", "file-detection.raml", RamlYamlHint, Raml)
  }

  test("FormData multiple parameters oas to oas") {
    cycle("form-data-params.json", "form-data-params.json", OasJsonHint, Oas)
  }

  test("FormData multiple parameters oas to amf") {
    cycle("form-data-params.json", "form-data-params.jsonld", OasJsonHint, Amf)
  }

  test("Boolean value in example") {
    cycle("boolean-value-in-example.raml", "boolean-value-in-example.raml", RamlYamlHint, Raml)
  }

  test("arrayTypes raml to oas") {
    cycle("array_items.raml", "array_items.json", RamlYamlHint, Oas)
  }

  test("lock-unlock example raml to oas") {
    cycle("lockUnlockStats.raml", "lockUnlockStats.jsonld", RamlYamlHint, Amf, productionPath + "lock-unlock/")
  }

  test("PatternProperties JSON Schema oas to raml") {
    cycle("oasPatternProperties.yaml", "oasPatternProperties.raml", OasYamlHint, Raml)
  }

  test("Test description property in example body") {
    cycle("example-key-map-without-value.raml", RamlYamlHint)
  }

  test("Test more than one base uri param") {
    cycle("base-uri-params-implicit.raml", "base-uri-params-implicit.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Test enums raml to raml") {
    cycle("enums.raml", "enums.raml.raml", RamlYamlHint, Raml, basePath + "enums/")
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

  test("Test enums raml-amf to raml") {
    cycle("enums.raml.jsonld", "enums.jsonld.raml", AmfJsonHint, Raml, basePath + "enums/")
  }

  test("Test schema lexical information position") {
    cycle("schema-position.raml", "schema-position.jsonld", RamlYamlHint, Amf, base08Path)
  }

  test("Test parse empty included external fragment") {
    cycle("api.raml", "api.raml.raml", RamlYamlHint, Raml, basePath + "empty-external-fragment/")
  }
}
