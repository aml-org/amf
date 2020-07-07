package amf.resolution

import amf.core.model.document.Document
import amf.core.remote._
import amf.plugins.document.webapi.resolution.pipelines.AmfEditingPipeline

class ProductionResolutionTest extends RamlResolutionTest {
  override val basePath = "amf-client/shared/src/test/resources/production/"
  val completeCyclePath = "amf-client/shared/src/test/resources/upanddown/"
  val validationPath    = "amf-client/shared/src/test/resources/validations/"
  val resolutionPath    = "amf-client/shared/src/test/resources/resolution/"
  val productionRaml10  = "amf-client/shared/src/test/resources/production/raml10/"
  val productionRaml08  = "amf-client/shared/src/test/resources/production/raml08/"

  multiGoldenTest("Test declared type with facet added", "add-facet.raml.%s") { config =>
    cycle("add-facet.raml",
          config.golden,
          RamlYamlHint,
          renderOptions = Some(config.renderOptions),
          target = Amf,
          directory = basePath + "inherits-resolution-declares/")
  }

  multiGoldenTest("Test inline type from includes", "test-ramlfragment.raml.%s") { config =>
    cycle(
      "test-ramlfragment.raml",
      config.golden,
      RamlYamlHint,
      renderOptions = Some(config.renderOptions),
      target = Amf,
      directory = basePath + "inherits-resolution-declares/"
    )
  }

  test("Resolves googleapis.compredictionv1.2swagger.raml") {
    cycle("googleapis.compredictionv1.2swagger.raml",
          "googleapis.compredictionv1.2swagger.raml.resolved.raml",
          RamlYamlHint,
          Raml)
  }

  multiGoldenTest("Resolves googleapis.compredictionv1.2swagger.raml to jsonld",
                  "googleapis.compredictionv1.2swagger.raml.resolved.%s") { config =>
    cycle("googleapis.compredictionv1.2swagger.raml",
          config.golden,
          RamlYamlHint,
          renderOptions = Some(config.renderOptions),
          target = Amf)
  }

  multiGoldenTest("azure_blob_service raml to jsonld", "microsoft_azure_blob_service.raml.resolved.%s") { config =>
    cycle("microsoft_azure_blob_service.raml",
          config.golden,
          RamlYamlHint,
          renderOptions = Some(config.renderOptions),
          target = Amf)
  }

  test("test definition_loops input") {
    cycle("api.raml",
          "crossfiles2.resolved.raml",
          RamlYamlHint,
          Raml,
          productionRaml08 + "definitions-loops-crossfiles2/")
  }

  multiGoldenTest("Types with unions raml to AMF", "unions-example.raml.%s") { config =>
    cycle("unions-example.raml", config.golden, RamlYamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Examples in header of type union", "example-in-union.raml.%s") { config =>
    cycle("example-in-union.raml",
          config.golden,
          RamlYamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  test("Complex types raml to raml") {
    cycle("complex_types.raml", "complex_types.resolved.raml", RamlYamlHint, Raml)
  }

  test("sales-order example") {
    cycle("sales-order-api.raml", "sales-order-api.resolved.raml", RamlYamlHint, Raml, basePath + "order-api/")
  }

  test("american-flights-api example") {
    cycle("api.raml",
          "american-flights-api.resolved.raml",
          RamlYamlHint,
          Raml,
          productionRaml10 + "american-flights-api/")
  }

  test("Test trait resolution null pointer exception test") {
    cycle("e-bo.raml", "e-bo.resolved.raml", RamlYamlHint, Raml, basePath + "reference-api/")
  }

  test("Test lib trait resolution with type defined in lib") {
    cycle("api.raml", "api.resolved.raml", RamlYamlHint, Raml, basePath + "lib-trait-type-resolution/")
  }

  test("test resource type") {
    cycle("input.raml",
          "input.resolved.raml",
          RamlYamlHint,
          Raml,
          "amf-client/shared/src/test/resources/org/raml/api/v10/library-references-absolute/")
  }

  test("test resource type non string scalar parameter example") {
    cycle(
      "input.raml",
      "input.resolved.raml",
      RamlYamlHint,
      Raml,
      "amf-client/shared/src/test/resources/org/raml/parser/resource-types/non-string-scalar-parameter/"
    )
  }

  test("test problem inclusion parent test") {
    cycle("input.raml", "input.resolved.raml", RamlYamlHint, Raml, basePath + "include-parent/")
  }

  test("test overlay documentation") {
    cycle("overlay.raml", "api.resolved.raml", RamlYamlHint, Raml, basePath + "overlay-documentation/")
  }

  // TODO: diff of final result is too slow
  ignore("test api_6109_ver_10147") {
    cycle("api.raml", "api.resolved.raml", RamlYamlHint, Raml, basePath + "api_6109_ver_10147/")
  }

  test("test bad tabulation at end flow map of traits definitions") {
    cycle("healthcare.raml", "healthcare.resolved.raml", RamlYamlHint, Raml, basePath + "healthcare/")
  }

  test("test trait with quoted string example var") {
    cycle("trait-string-quoted-node.raml",
          "trait-string-quoted-node.resolved.raml",
          RamlYamlHint,
          Raml,
          completeCyclePath)
  }

  test("test nullpointer in resolution") {
    cycle("api.raml", "api.resolved.raml", RamlYamlHint, Raml, validationPath + "retail-api/")
  }

  test("Test resolve inherited array without items") {
    cycle("inherits-array-without-items.raml",
          "inherits-array-without-items.resolved.raml",
          RamlYamlHint,
          Raml,
          basePath + "types/")
  }

  test("Test resolve resource type with '$' char in variable value") {
    cycle("invalid-regexp-char-in-variable.raml",
          "invalid-regexp-char-in-variable.resolved.raml",
          RamlYamlHint,
          Raml,
          basePath)
  }

  test("Test type resolution with property override") {
    cycle("property-override.raml", "property-override.resolved.raml", RamlYamlHint, Raml, basePath + "types/")
  }

  test("Test endpoints are not removed") {
    val source     = "api.raml"
    val golden     = ""
    val hint       = RamlYamlHint
    val target     = Amf
    val directory  = productionRaml10 + "demo-api/"
    val syntax     = None
    val validation = None

    val config                    = CycleConfig(source, golden, hint, target, directory, syntax, None)
    val useAmfJsonldSerialization = true

    for {
      simpleModel <- build(config, validation, useAmfJsonldSerialization).map(AmfEditingPipeline.unhandled.resolve(_))
      a           <- render(simpleModel, config, useAmfJsonldSerialization)
      doubleModel <- build(config, validation, useAmfJsonldSerialization).map(AmfEditingPipeline.unhandled.resolve(_))
      _           <- render(doubleModel, config, useAmfJsonldSerialization)
      b           <- render(doubleModel, config, useAmfJsonldSerialization)
    } yield {
      val simpleDeclares =
        simpleModel.asInstanceOf[Document].declares
      val doubleDeclares =
        doubleModel.asInstanceOf[Document].declares
      writeTemporaryFile("demo-api1.jsonld")(a)
      writeTemporaryFile("demo-api2.jsonld")(b)
      assert(simpleDeclares.length == doubleDeclares.length)
    }
  }

  test("Test example inheritance in type declaration with simple inheritance") {
    cycle("api.raml", "api.raml.resolved", RamlYamlHint, Raml, basePath + "simple-inheritance-example/")
  }

  test("Test example inheritance in type declaration with simple chained inheritance") {
    cycle("api.raml", "api.raml.resolved", RamlYamlHint, Raml, basePath + "simple-inheritance-chained-example/")
  }

  test("Test example inheritance in type declaration with link") {
    cycle("api.raml", "api.raml.resolved", RamlYamlHint, Raml, basePath + "simple-inheritance-link-example/")
  }

  // TODO migrate to multiGoldenTest
  test("Test union type anyOf name values") {
    cycle("api.raml", "api.raml.resolved", RamlYamlHint, Amf, basePath + "union-type/")
  }

  // TODO migrate to multiGoldenTest
  test("Test complex recursions in type inheritance 1") {
    cycle("healthcare_reduced_v1.raml", "healthcare_reduced_v1.raml.resolved", RamlYamlHint, Amf, validationPath)
  }

  // TODO migrate to multiGoldenTest
  test("Test complex recursions in type inheritance 2") {
    cycle("healthcare_reduced_v2.raml", "healthcare_reduced_v2.raml.resolved", RamlYamlHint, Amf, validationPath)
  }

  // TODO migrate to multiGoldenTest
  test("Test resource type parameters ids") {
    cycle("rt-parameters.raml", "rt-parameters.raml.resolved", RamlYamlHint, Amf, validationPath)
  }

  // TODO migrate to multiGoldenTest
  test("Test nil type with additional facets") {
    cycle("nil-type.raml", "nil-type.raml.resolved", RamlYamlHint, Amf, validationPath)
  }

  multiGoldenTest("Test first enum value and default value witha applied trait have different ids",
                  "enum-id-with-applied-trait/golden.%s") { config =>
    cycle("enum-id-with-applied-trait/api.raml",
          config.golden,
          RamlYamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("jsonld with links to declares and references", "link-to-declares-and-refs-default.%s") { config =>
    cycle(
      "link-to-declares-and-refs.raml",
      config.golden,
      RamlYamlHint,
      target = Amf,
      renderOptions = Some(config.renderOptions),
      directory = resolutionPath + "links-to-declares-and-references/"
    )
  }
}
