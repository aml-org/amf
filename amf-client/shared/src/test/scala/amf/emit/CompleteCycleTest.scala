package amf.emit

import amf.core.emitter.RenderOptions
import amf.core.remote.{AmfJsonHint, _}
import amf.io.FunSuiteCycleTests

class CompleteCycleTest extends FunSuiteCycleTests {

  override val basePath = "amf-client/shared/src/test/resources/upanddown/"
  val base08Path        = "amf-client/shared/src/test/resources/upanddown/raml08/"
  val baseRaml10Path    = "amf-client/shared/src/test/resources/upanddown/raml10/"
  val referencesPath    = "amf-client/shared/src/test/resources/references/"
  val productionPath    = "amf-client/shared/src/test/resources/production/"
  val validationsPath   = "amf-client/shared/src/test/resources/validations/"
  val apiPath           = "amf-client/shared/src/test/resources/api/"
  val parserResultPath  = "amf-client/shared/src/test/resources/parser-results/"
  val oasPath           = "amf-client/shared/src/test/resources/validations/oas2/"

  multiGoldenTest("Full oas to amf test", "full-example.json.%s") { config =>
    cycle("full-example.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Orphan extensions oas to amf test", "orphan_extensions.%s") { config =>
    cycle("orphan_extensions.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Traits and resourceTypes oas to amf test", "traits-resource-types.json.%s") { config =>
    cycle("traits-resource-types.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Basic oas to amf test", "basic.json.%s") { config =>
    cycle("basic.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Oas generates endpoint although path is invalid",
                  "cycled/invalid-endpoint-path-still-parses.json.%s") { config =>
    cycle("invalid-endpoint-path-still-parses.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          directory = oasPath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Complete oas to amf test", "complete.json.%s") { config =>
    cycle("complete.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Complete with formData parameter references oas to amf test", "formDataParameters.%s") { config =>
    cycle("formDataParameters.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Complete with multiple formData parameters oas to amf test", "formdata-parameters-multiple.%s") {
    config =>
      cycle("formdata-parameters-multiple.yaml",
            config.golden,
            Oas20YamlHint,
            target = Amf,
            renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Complete with parameter references oas to amf test", "parameters.json.%s") { config =>
    cycle("parameters.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Types dependency oas to amf test", "types-dependency.json.%s") { config =>
    cycle("types-dependency.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Types all facets oas to jsonld test", "types-facet.json.%s") { config =>
    cycle("types-facet.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Petstore oas to jsonld", "petstore/petstore.%s") { config =>
    cycle("petstore/petstore.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Annotations oas to jsonld test", "annotations.json.%s") { config =>
    cycle("annotations.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Test libraries oas to amf", "libraries.json.%s") { config =>
    cycle("libraries.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          directory = referencesPath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Test data type fragment oas to amf", "data-type-fragment.json.%s") { config =>
    cycle("data-type-fragment.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          directory = referencesPath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Schema types oas to amf test", "externals.json.%s") { config =>
    cycle("externals.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Security schemes oas to amf", "security.json.%s") { config =>
    cycle("security.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("QueryString oas to amf", "query-string.json.%s") { config =>
    cycle("query-string.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Security with QueryString oas to amf", "security-with-query-string.json.%s") { config =>
    cycle("security-with-query-string.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Example json to amf", "examples.json.%s") { config =>
    cycle("examples.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Facets oas to amf", "type-facets.json.%s") { config =>
    cycle("type-facets.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Raml operation param schema duplicate ids", "api.%s") { config =>
    cycle("api.raml",
          config.golden,
          Raml10YamlHint,
          Amf,
          renderOptions = Some(config.renderOptions),
          directory = baseRaml10Path + "duplicate-id-in-param-types/")
  }

  multiGoldenTest("Parsing oas shape with description oas to amf", "shapes-with-items.%s") { config =>
    cycle("shapes-with-items.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  ignore("References oas to amf") {
    cycle("with_references.json", "with_references.json.jsonld", Oas20JsonHint, target = Amf)
  }

  multiGoldenTest("Declared response oas to jsonld", "declared-responses.json.%s") { config =>
    cycle("declared-responses.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Additional properties shape oas to amf", "additional-properties.%s") { config =>
    cycle("additional-properties.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("CollectionFormat shape oas to amf", "collection-format.%s") { config =>
    cycle("collection-format.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Tags node oas to amf", "tags.%s") { config =>
    cycle("tags.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Response declaration oas to amf", "oas_response_declaration.%s") { config =>
    cycle("oas_response_declaration.yaml",
          config.golden,
          Oas20YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("FormData multiple parameters oas to amf", "form-data-params.%s") { config =>
    cycle("form-data-params.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Test enums raml to amf", "enums.raml.%s") { config =>
    cycle("enums.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          directory = s"${basePath}enums/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Security requirements OAS to JSONLD", "api-with-security-requirements.%s") { config =>
    cycle(
      "api-with-security-requirements.json",
      config.golden,
      Oas20JsonHint,
      target = Amf,
      directory = s"${validationsPath}oas-security/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Raml10 parses Draft 7 when specified", "raml-reference-draft-7.%s") { config =>
    cycle(
      "raml-reference-draft-7.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      directory = baseRaml10Path,
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Raml10 parses Draft 4 when version is not specified", "raml-default-schema-version.%s") { config =>
    cycle(
      "raml-default-schema-version.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      directory = baseRaml10Path,
      renderOptions = Some(config.renderOptions)
    )
  }

  // TODO migrate to multiGoldenTest
  test("File type cycle") {
    cycle("file-type.raml", "file-type.json", Raml10YamlHint, target = Amf)
  }

  multiTest("Types amf(raml) to amf test", "types.raml.%s", "types.raml.%s") { config =>
    cycle(config.source, config.golden, AmfJsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiTest("Annotations jsonld to jsonld test", "annotations.raml.%s", "annotations.raml.%s") { config =>
    cycle(config.source, config.golden, AmfJsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  test("Full oas to oas test") {
    cycle("full-example.json", Oas20JsonHint)
  }

  test("Full raml to oas test") {
    cycle("full-example.raml", "full-example.raml.json", Raml10YamlHint, target = Oas20)
  }

  test("Full oas to raml test") {
    cycle("full-example.json", "full-example.json.raml", Oas20JsonHint, target = Raml10)
  }

  test("Default response to raml test") {
    cycle("default-response.json", "default-response.json.raml", Oas20JsonHint, target = Raml10)
  }

  test("Default response to raml test 2") {
    cycle("default-response.json", Oas20JsonHint)
  }

  test("Parse default response example form raml from oas test") {
    cycle("default-response.json.raml", Raml10YamlHint)
  }

  test("Traits and resourceTypes oas to oas test") {
    cycle("traits-resource-types.json", Oas20JsonHint)
  }

  test("Traits and resourceTypes raml to oas test") {
    cycle("traits-resource-types.raml", "traits-resource-types.raml.json", Raml10YamlHint, target = Oas20)
  }

  multiTest("Basic cycle for amf", "basic.%s", "basic.%s") { config =>
    cycle(config.source, config.golden, AmfJsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  test("Basic cycle for oas") {
    cycle("basic.json", Oas20JsonHint)
  }

  multiSourceTest("Basic amf(oas) to oas test", "basic.json.%s") { config =>
    cycle(config.source, "basic.json", AmfJsonHint, target = Oas20)
  }

  test("Basic raml to oas test") {
    cycle("basic.raml", "basic.raml.json", Raml10YamlHint, target = Oas20)
  }

  test("Basic oas to raml test") {
    cycle("basic.json", "basic.json.raml", Oas20JsonHint, target = Raml10)
  }

  multiTest("Complete amf to amf test", "complete.%s", "complete.%s") { config =>
    cycle(config.source, config.golden, AmfJsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  test("Complete raml to oas test") {
    cycle("complete.raml", "complete.raml.json", Raml10YamlHint, target = Oas20)
  }

  test("Complete oas to raml test") {
    cycle("complete.json", "complete.raml", Oas20JsonHint, target = Raml10)
  }

  test("Complete oas to oas test") {
    cycle("complete.json", Oas20JsonHint)
  }

  multiSourceTest("Complete amf(oas) to oas test", "complete.json.%s") { config =>
    cycle(config.source, "complete.json", AmfJsonHint, target = Oas20)
  }

  multiTest("Endpoints amf to amf test", "endpoints.%s", "endpoints.%s") { config =>
    cycle(config.source, config.golden, AmfJsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  test("Endpoints raml to oas test") {
    cycle("endpoints.raml", "endpoints.raml.json", Raml10YamlHint, target = Oas20)
  }

  multiGoldenTest("Endpoints oas to amf test", "endpoints.json.%s") { config =>
    cycle("endpoints.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  test("Endpoints oas to raml test") {
    cycle("endpoints.json", "endpoints.json.raml", Oas20JsonHint, target = Raml10)
  }

  test("Endpoints oas to oas test") {
    cycle("endpoints.json", Oas20JsonHint)
  }

  multiSourceTest("Endpoints amf(oas) to oas test", "endpoints.json.%s") { config =>
    cycle(config.source, "endpoints.json", AmfJsonHint, target = Oas20)
  }

  test("Complete with operations raml to oas test") {
    cycle("complete-with-operations.raml", "complete-with-operations.json", Raml10YamlHint, target = Oas20)
  }

  test("Complete with operations oas to raml test") {
    cycle("complete-with-operations.json", "complete-with-operations.json.raml", Oas20JsonHint, target = Raml10)
  }

  test("Complete with operations oas to oas test") {
    cycle("complete-with-operations.json", Oas20JsonHint)
  }

  test("Complete with request oas to raml test") {
    cycle("operation-request.json", "operation-request.json.raml", Oas20JsonHint, target = Raml10)
  }

  test("Complete with request raml to oas test") {
    cycle("operation-request.raml", "operation-request.raml.json", Raml10YamlHint, target = Oas20)
  }

  test("Complete with response oas to raml test") {
    cycle("operation-response.json", "operation-response.raml", Oas20JsonHint, target = Raml10)
  }

  test("Complete with response oas to oas test") {
    cycle("operation-response.json", Oas20JsonHint)
  }

  test("Complete with response raml to oas test") {
    cycle("operation-response.raml", "operation-response.raml.json", Raml10YamlHint, target = Oas20)
  }

  test("Complete with parameter references oas to oas test") {
    cycle("parameters.json", Oas20JsonHint)
  }

  multiSourceTest("Orphan extensions amf to oas test", "orphan_extensions.%s") { config =>
    cycle(config.source, "orphan_extensions.json", AmfJsonHint, target = Oas20)
  }

  multiSourceTest("Complete with parameter references amf to oas test", "parameters.json.%s") { config =>
    cycle(config.source, "parameters.json", AmfJsonHint, target = Oas20)
  }

  multiSourceTest("Complete with formData parameter references amf to oas test", "formDataParameters.%s") { config =>
    cycle(config.source, "formDataParameters.json", AmfJsonHint, target = Oas20)
  }

  ignore("Types dependency amf(oas) to oas test") {
    cycle("types-dependency.json.jsonld", "types-dependency.json", AmfJsonHint, Oas20)
  }

  multiSourceTest("Types all facets jsonld to oas test", "types-facet.json.%s") { config =>
    cycle(config.source, "types-facet.json.jsonld.json", AmfJsonHint, target = Oas20)
  }

  multiSourceTest("Test libraries amf to oas", "libraries.json.%s") { config =>
    cycle(config.source, "libraries.json", AmfJsonHint, target = Oas20, directory = referencesPath)
  }

  multiTest("Test data type fragment amf to amf", "data-type-fragment.raml.%s", "data-type-fragment.raml.%s") {
    config =>
      cycle(config.source,
            config.golden,
            AmfJsonHint,
            target = Amf,
            directory = referencesPath,
            renderOptions = Some(config.renderOptions))
  }

  multiSourceTest("Test data type fragment amf to oas", "data-type-fragment.json.%s") { config =>
    cycle(config.source, "data-type-fragment.json", AmfJsonHint, target = Oas20, directory = referencesPath)
  }

  // TODO: migrate to multiSourceTest
  test("Extension fragment jsonld to oas") {
    cycle("extension.json.jsonld",
          "extension.json.json",
          AmfJsonHint,
          target = Oas20,
          directory = s"${referencesPath}extensions/")
  }

  // TODO: migrate to multiSourceTest
  test("Overlay fragment jsonld to oas") {
    cycle("overlay.json.jsonld",
          "overlay.json.json",
          AmfJsonHint,
          target = Oas20,
          directory = s"${referencesPath}extensions/")
  }

  multiSourceTest("Schema types amf to oas test", "externals.json.%s") { config =>
    cycle(config.source, "externals.json.jsonld.json", AmfJsonHint, target = Oas20)
  }

  multiSourceTest("QueryString amf to oas", "query-string.json.%s") { config =>
    cycle(config.source, "query-string.json", AmfJsonHint, target = Oas20)
  }

  multiSourceTest("Security with QueryString amf to oas", "security-with-query-string.json.%s") { config =>
    cycle(config.source, "security-with-query-string.json", AmfJsonHint, target = Oas20)
  }

  multiSourceTest("Example amf to json", "examples.json.%s") { config =>
    cycle(config.source, "examples.jsonld.json", AmfJsonHint, target = Oas20)
  }

  multiSourceTest("Declared response jsonld to oas", "declared-responses.json.%s") { config =>
    cycle(config.source, "declared-responses.jsonld.json", AmfJsonHint, target = Oas20)
  }

  multiSourceTest("CollectionFormat shape amf to oas", "collection-format.%s") { config =>
    cycle(config.source, "collection-format.jsonld.json", AmfJsonHint, target = Oas20)
  }

  // TODO: migrate to multiSourceTest
  test("Anonymous and named examples with annotations json to raml") {

    cycle("anonymous-and-named-examples.jsonld", "anonymous-and-named-examples.raml", AmfJsonHint, target = Raml10)
  }

  multiSourceTest("Tags node amf to oas", "tags.%s") { config =>
    cycle(config.source, "tags.json.json", AmfJsonHint, target = Oas20)
  }

  // TODO: migrate to multiSourceTest
  test("Numeric facets jsonld to oas") {
    cycle("numeric-facets.jsonld", "numeric-facets.jsonld.json", AmfJsonHint, target = Oas20)
  }

  test("Complete with formData parameter references oas to oas test") {
    cycle("formDataParameters.json", Oas20JsonHint)
  }

  test("Complete with formData parameter references oas to raml test") {
    cycle("formDataParameters.json", "formDataParameters.raml", Oas20JsonHint, target = Raml10)
  }

  test("Complete with parameter references oas to raml test") {
    cycle("parameters.json", "parameters.raml", Oas20JsonHint, target = Raml10)
  }

  test("Complete with payloads raml to oas test") {
    cycle("payloads.raml", "payloads.raml.json", Raml10YamlHint, target = Oas20)
  }

  test("Complete with payloads oas to oas test") {
    cycle("payloads.json", Oas20JsonHint)
  }

  test("Complete with payloads oas to raml test") {
    cycle("payloads.json", "payloads.json.raml", Oas20JsonHint, target = Raml10)
  }

  test("Types implicit & explicit oas to oas test") {
    cycle("explicit-&-implicit-type-object.json", Oas20JsonHint)
  }

  test("Types implicit & explicit raml to oas test") {
    cycle("explicit-&-implicit-type-object.raml",
          "explicit-&-implicit-type-object.raml.json",
          Raml10YamlHint,
          target = Oas20)
  }

  test("Types implicit & explicit oas to raml test") {
    cycle("explicit-&-implicit-type-object.json",
          "explicit-&-implicit-type-object.json.raml",
          Oas20JsonHint,
          target = Raml10)
  }

  test("Types dependency oas to oas test") {
    cycle("types-dependency.json", Oas20JsonHint)
  }

  test("Types dependency raml to oas test") {
    cycle("types-dependency.raml", "types-dependency.raml.json", Raml10YamlHint, target = Oas20)
  }

  test("Types dependency oas to raml test") {
    cycle("types-dependency.json", "types-dependency.json.raml", Oas20JsonHint, target = Raml10)
  }

  test("Types declarations oas to oas test") {
    cycle("declarations-small.json", Oas20JsonHint)
  }

  test("Types all facets oas to oas test") {
    cycle("types-facet.json", Oas20JsonHint)
  }

  test("Types all facets oas to raml test") {
    cycle("types-facet.json", "types-facet.json.raml", Oas20JsonHint, target = Raml10)
  }

  test("Annotations in Scalars raml to oas") {
    cycle("annotations-scalars.raml", "annotations-scalars.json", Raml10YamlHint, target = Oas20)
  }

  test("Petstore oas to raml") {
    cycle("petstore/petstore.json", "petstore/petstore.raml", Oas20JsonHint, target = Raml10)
  }

  test("Annotations oas to oas test") {
    cycle("annotations.json", Oas20JsonHint)
  }

  test("Types all types oas to oas test") {
    cycle("all-type-types.json", Oas20JsonHint)
  }

  test("Types all types raml to oas test") {
    cycle("all-type-types.raml", "all-type-types.raml.json", Raml10YamlHint, target = Oas20)
  }

  test("Types all types oas to raml test") {
    cycle("all-type-types.json", "all-type-types.json.raml", Oas20JsonHint, target = Raml10)
  }

  test("Test libraries oas to oas") {
    cycle("libraries.json", Oas20JsonHint, directory = referencesPath)
  }

  test("Test data type fragment oas to oas") {
    cycle("data-type-fragment.json", Oas20JsonHint, directory = referencesPath)
  }

  // todo what we do when library file name changes changes on dump
  ignore("Test libraries raml to oas") {
    cycle("libraries.raml", "libraries.json.json", Raml10YamlHint, target = Oas20, directory = referencesPath)
  }

  ignore("Test libraries oas to raml") {
    cycle("libraries.json", "libraries.raml.raml", Oas20JsonHint, target = Raml10, directory = referencesPath)
  }

  ignore("Overlay fragment oas to amf") {
    cycle(
      "overlay.json",
      "overlay.json.jsonld",
      Oas20JsonHint,
      target = Amf,
      directory = s"${referencesPath}extensions/"
    )
  }

  ignore("Overlay fragment oas to oas") {
    cycle("overlay.json",
          "overlay.json.json",
          Oas20JsonHint,
          target = Oas20,
          directory = s"${referencesPath}extensions/")
  }

  ignore("Extension fragment oas to amf") {
    cycle("extension.json",
          "extension.json.jsonld",
          Oas20JsonHint,
          target = Amf,
          directory = s"${referencesPath}extensions/")
  }

  ignore("Extension fragment oas to oas") {
    cycle("extension.json",
          "extension.json.json",
          Oas20JsonHint,
          target = Oas20,
          directory = s"${referencesPath}extensions/")
  }

  test("More types raml to oas test") {
    cycle("more-types.raml", "more-types.raml.json", Raml10YamlHint, target = Oas20)
  }

  test("Types forward references oas to oas test") {
    cycle("forward-references-types.json", Oas20JsonHint)
  }

  test("Schema types raml to oas test") {
    cycle("externals.raml", "externals.json", Raml10YamlHint, target = Oas20)
  }

  test("Closed node for 0.8 web form test") {
    cycle("closed_web_form.raml", "closed_web_form.json", Raml08YamlHint, target = Oas20, directory = base08Path)
  }

  test("Security schemes oas to oas") {
    cycle("security.json", Oas20JsonHint)
  }

  test("SecuredBy oas to oas") {
    cycle("secured-by.json", Oas20JsonHint)
  }

  test("QueryString oas to oas") {
    cycle("query-string.json", Oas20JsonHint)
  }

  test("Security with QueryString oas to oas") {
    cycle("security-with-query-string.json", Oas20JsonHint)
  }

  test("Example oas to oas") {
    cycle("examples.json", Oas20JsonHint)
  }

  test("Fragment Named Example oas to oas") {
    cycle("named-example.json", Oas20JsonHint, directory = referencesPath)
  }

  test("Facets raml to oas") {
    cycle("type-facets.raml", "type-facets.json", Raml10YamlHint, target = Oas20)
  }

  test("OAS descriptions for responses area added automatically raml to oas") {
    cycle("missing_oas_description.raml", "missing_oas_description.json", Raml10YamlHint, target = Oas20)
  }

  test("OAS descriptions for responses area added automatically oas to raml") {
    cycle("missing_oas_description.json", "missing_oas_description.json.raml", Oas20JsonHint, target = Raml10)
  }

  test("SecurityScheme without name raml to oas") {
    cycle("unnamed-security-scheme.raml", "unnamed-security-scheme.raml.json", Raml10YamlHint, target = Oas20)
  }

  test("References raml to oas") {
    cycle("with_references.raml", "with_references.json", Raml10YamlHint, target = Oas20)
  }

  ignore("References oas to oas") {
    cycle("with_references.json", Oas20JsonHint)
  }

  test("Car oas to oas") {
    cycle("somecars.json", "somecars.json", Oas20JsonHint, target = Oas20)
  }

  test("Car oas to raml") {
    cycle("somecars.json", "somecars.raml", Oas20JsonHint, target = Raml10)
  }

  test("konst1 raml to oas") {
    cycle("konst1.raml", "konst1.json", Raml10YamlHint, target = Oas20)
  }

  test("konst1 oas to raml") {
    cycle("konst1.json", "konst1.json.raml", Oas20JsonHint, target = Raml10)
  }

  test("Message for model objects not supported in 08") {
    cycle("array-of-node.raml", "array-of-node-unsupported08.raml", Raml10YamlHint, target = Raml08)
  }

  test("JSON Schema with [{}]") {
    cycle("array-of-node.raml", "array-of-node-unsupported08.raml", Raml10YamlHint, target = Raml08)
  }

  test("Declared response") {
    cycle("declared-responses.json", Oas20JsonHint)
  }

  test("Declared response oas to raml") {
    cycle("declared-responses.json", "declared-responses.json.raml", Oas20JsonHint, target = Raml10)
  }

  test("Declared response raml to oas") {
    cycle("declared-responses.json.raml", "declared-responses.json", Raml10YamlHint, target = Oas20)
  }

  test("Additional properties shape oas to oas") {
    cycle("additional-properties.json", "additional-properties.json", Oas20JsonHint, target = Oas20)
  }

  test("Additional properties shape oas to raml") {
    cycle("additional-properties.json", "additional-properties.raml", Oas20JsonHint, target = Raml10)
  }

  test("Additional properties shape raml to oas") {
    cycle("additional-properties.raml", "additional-properties.raml.json", Raml10YamlHint, target = Oas20)
  }

  test("CollectionFormat shape oas to oas") {
    cycle("collection-format.json", "collection-format.json.json", Oas20JsonHint, target = Oas20)
  }

  test("CollectionFormat shape oas to raml") {
    cycle("collection-format.json", "collection-format.raml", Oas20JsonHint, target = Raml10)
  }

  test("Date format oas to raml") {
    cycle("date-format.json", "date-format.raml", Oas20JsonHint, target = Raml10)
  }

  test("Tags node oas to oas") {
    cycle("tags.json", "tags.json.json", Oas20JsonHint, target = Oas20)
  }

  test("Tags node oas to raml") {
    cycle("tags.json", "tags.raml", Oas20JsonHint, target = Raml10)
  }

  test("Tags node raml to oas") {
    cycle("tags.raml", "tags.json.json", Raml10YamlHint, target = Oas20)
  }

  test("Numeric facets raml to oas") {
    cycle("numeric-facets.raml", "numeric-facets.json", Raml10YamlHint, target = Oas20)
  }

  test("Test parse types") {
    cycle("shapes.raml", "shapes.json", Raml10YamlHint, target = Oas20, directory = s"${apiPath}types/")
  }

  test("FormData multiple parameters oas to oas") {
    cycle("form-data-params.json", "form-data-params.json", Oas20JsonHint, target = Oas20)
  }

  test("arrayTypes raml to oas") {
    cycle("array_items.raml", "array_items.json", Raml10YamlHint, target = Oas20)
  }

  test("PatternProperties JSON Schema oas to raml") {
    cycle("oasPatternProperties.yaml", "oasPatternProperties.raml", Oas20YamlHint, target = Raml10)
  }

  test("Test enums raml to oas") {
    cycle("enums.raml", "enums.json", Raml10YamlHint, target = Oas20, directory = s"${basePath}enums/")
  }

  test("Test enums oas to oas") {
    cycle("enums.json", "enums.json.json", Oas20JsonHint, target = Oas20, directory = s"${basePath}enums/")
  }

  test("Test enums oas to raml") {
    cycle("enums.json", "enums.json.raml", Oas20JsonHint, target = Raml10, directory = s"${basePath}enums/")
  }

  test("Test nil example raml to raml") {
    cycle("nil-example.raml", "nil-example.raml.raml", Raml10YamlHint, target = Raml10)
  }

  test("Test nil example raml generated to itself") {
    cycle("nil-example.raml.raml", "nil-example.raml.raml", Raml10YamlHint, target = Raml10)
  }

  test("Security requirement OAS to OAS") {
    cycle("api-with-security-requirement.json",
          "api-with-security-requirement.json",
          Oas20JsonHint,
          target = Oas20,
          directory = s"${validationsPath}oas-security/")
  }

  test("Security requirements OAS to OAS") {
    cycle("api-with-security-requirements.json",
          "api-with-security-requirements.json",
          Oas20JsonHint,
          target = Oas20,
          directory = s"${validationsPath}oas-security/")
  }

  test("Description parameters in OAS") {
    cycle("api-with-param-description.json",
          "api-with-param-description.json",
          Oas20JsonHint,
          target = Oas20,
          directory = s"${parserResultPath}oas/")
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
  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
