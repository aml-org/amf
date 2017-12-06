package amf.emit

import amf.core.remote._
import amf.io.BuildCycleTests

class CompleteCycleTest extends BuildCycleTests {

  override val basePath = "amf-client/shared/src/test/resources/upanddown/"
  val referencesPath    = "amf-client/shared/src/test/resources/references/"

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

  test("Complete with parameter references oas to amf test") {
    cycle("parameters.json", "parameters.json.jsonld", OasJsonHint, Amf)
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

  test("Types all facets raml to oas test") {
    cycle("types-facet.raml", "types-facet.raml.json", RamlYamlHint, Oas)
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

  test("Example json to amf") {
    cycle("examples.json", "examples.json.jsonld", OasJsonHint, Amf)
  }

  test("Example amf to raml") {
    cycle("examples.raml.jsonld", "examples.jsonld.raml", AmfJsonHint, Raml)
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
}
