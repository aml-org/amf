package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.parser.UnhandledErrorHandler
import amf.core.remote.Syntax.Yaml
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.emit.AMFRenderer
import amf.facades.Validation
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.resolution.pipelines.AmfEditingPipeline
import amf.plugins.document.webapi.{Oas20Plugin, Oas30Plugin, Raml08Plugin, Raml10Plugin}

import scala.concurrent.{ExecutionContext, Future}

class EditingResolutionTest extends FunSuiteCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val extendsPath     = "amf-client/shared/src/test/resources/resolution/extends/"
  val productionPath  = "amf-client/shared/src/test/resources/production/"
  val resolutionPath  = "amf-client/shared/src/test/resources/resolution/"
  val cyclePath       = "amf-client/shared/src/test/resources/upanddown/"
  val referencesPath  = "amf-client/shared/src/test/resources/references/"
  val validationsPath = "amf-client/shared/src/test/resources/validations/"

  test("API with recursive shapes") {
    cycle("recursive3.raml", "recursive3.editing.jsonld", RamlYamlHint, Amf, productionPath)
  }
  test("Simple extends resolution to Raml") {
    cycle("simple-merge.raml", "simple-merge.editing.jsonld", RamlYamlHint, Amf, extendsPath)
  }

  test("Types resolution to Raml") {
    cycle("data.raml", "data.editing.jsonld", RamlYamlHint, Amf, extendsPath)
  }

  test("Example1 resolution to Raml") {
    cycle("example1.yaml", "example1.resolved.yaml", OasYamlHint, Oas20, resolutionPath, syntax = Some(Yaml))
  }

  test("Include type resolution to Raml") {
    cycle("simple_example_type.raml", "simple_example_type.resolved.jsonld", RamlYamlHint, Amf, cyclePath)
  }

  test("Test data type fragment resolution to Amf") {
    cycle("data-type-fragment.reference.raml",
          "data-type-fragment.reference.resolved.jsonld",
          RamlYamlHint,
          Amf,
          referencesPath)
  }

  test("Test union arrays") {
    cycle("union_arrays.raml", "union_arrays.resolved.jsonld", RamlYamlHint, Amf, cyclePath)
  }

  test("Exchange issueNil API resolution to Amf") {
    cycle("api.raml", "api.resolved.jsonld", RamlYamlHint, Amf, validationsPath + "examples/inline-named-examples/")
  }

  test("Location in annotation of Trait declared in lib") {
    cycle("api.raml", "api.resolved.jsonld", RamlYamlHint, Amf, productionPath + "lib-trait-location/")
  }

  test("Test merge examples in local against declared type") {
    cycle("merge-examples.raml", "merge-examples.resolved.raml", RamlYamlHint, Raml, resolutionPath + "examples/")
  }

  test("Test extension merging") {
    cycle("input.raml",
          "input.resolved.jsonld",
          RamlYamlHint,
          Amf,
          "amf-client/shared/src/test/resources/resolution/extension/traits/")
  }

  test("Unresolved shape") {
    Validation(platform)
      .map(_.withEnabledValidation(true))
      .flatMap(
        v =>
          cycle("unresolved-shape.raml",
                "unresolved-shape.raml.jsonld",
                RamlYamlHint,
                Amf,
                resolutionPath,
                validation = Some(v)))
  }

  test("Test url shortener with external references") {
    cycle("api.raml",
          "api.resolved.jsonld",
          RamlYamlHint,
          Amf,
          resolutionPath + "externalfragment/test-links-with-references/")
  }

  test("Test tracked examples annotations parent shortened") {
    cycle("payloads-examples-resolution.raml",
          "payloads-examples-resolution.resolved.jsonld",
          RamlYamlHint,
          Amf,
          resolutionPath)
  }

  test("Test recursive annotations of extension provenance") {
    cycle("api.raml", "api.resolved.jsonld", RamlYamlHint, Amf, resolutionPath + "recursive-extension-provenance/")
  }

  test("Test url shortener at example (dynamic)") {
    cycle("examples-shortener.raml", "examples-shortener.resolved.jsonld", RamlYamlHint, Amf, resolutionPath)
  }

  test("Test double declared included type") {
    cycle("api.raml", "api.resolved.jsonld", RamlYamlHint, Amf, resolutionPath + "double-declare-type/")
  }

  test("Test declared type from library") {
    cycle("api.raml", "api.resolved.jsonld", RamlYamlHint, Amf, resolutionPath + "declared-from-library/")
  }

  test("Test union of declared elements") {
    cycle("api.raml", "api.raml.resolved.jsonld", RamlYamlHint, Amf, resolutionPath + "union-of-declarations/")
  }

  test("Check for stack overflow in event api") {
    cycle("api.raml", "api.jsonld", RamlYamlHint, Amf, productionPath + "event-api/")
  }

  test("Test tracked examples in oas responses") {
    cycle("oas-multiple-example.json", "oas-multiple-example.resolved.jsonld", OasJsonHint, Amf, productionPath)
  }

  test("Parse correctly non-AMF graph JSON-LD example") {
    cycle("api.raml", "api.jsonld", RamlYamlHint, Amf, resolutionPath + "jsonld-example/")
  }

  test("Root mediaType propagation should also adopt tracked-element annotation") {
    cycle("root-mediatype-propagation.raml",
          "root-mediatype-propagation.jsonld",
          RamlYamlHint,
          Amf,
          validationsPath + "root-mediatype-propagation/")
  }

  test("Propagate tracked-element to linked examples") {
    cycle("tracked-to-linked.raml",
          "tracked-to-linked.jsonld",
          RamlYamlHint,
          Amf,
          validationsPath + "tracked-to-linked/")
  }

  test("Adopt tracked-element when merging abstract declarations") {
    cycle("tracked-from-resource-type.raml",
          "tracked-from-resource-type.jsonld",
          RamlYamlHint,
          Amf,
          validationsPath + "tracked-from-resource-type/")
  }

  test("Auto generated payload name annotation in raml") {
    cycle("auto-generated-schema-name.raml",
          "auto-generated-schema-name.jsonld",
          RamlYamlHint,
          Amf,
          validationsPath + "auto-generated-schema-name/")
  }

  test("Auto generated payload name annotation in oas") {
    cycle("auto-generated-schema-name-oas.yaml",
          "auto-generated-schema-name-oas.jsonld",
          OasYamlHint,
          Amf,
          validationsPath + "auto-generated-schema-name/")
  }

  test("Auto generated payload name annotation with default mediaType") {
    cycle(
      "auto-generated-schema-name-with-default.raml",
      "auto-generated-schema-name-with-default.jsonld",
      RamlYamlHint,
      Amf,
      validationsPath + "auto-generated-schema-name/"
    )
  }

  test("Declared type union with inherit array link") {
    cycle("union-type-array.raml", "union-type-array.jsonld", RamlYamlHint, Amf, validationsPath)
  }

  test("Tracked oas examples") {
    cycle(
      "tracked-oas-examples.json",
      "tracked-oas-examples.jsonld",
      OasJsonHint,
      Amf,
      validationsPath + "tracked-oas-examples/"
    )
  }

  test("Keep schema name in body link schema") {
    cycle("body-link-name.raml", "body-link-name.jsonld", RamlYamlHint, Amf, validationsPath + "body-link-name/")
  }

  test("Union type defined under composing types, with one type defined as closed") {
    cycle(
      "additional-prop-and-defined-after.raml",
      "additional-prop-and-defined-after.jsonld",
      RamlYamlHint,
      Amf,
      productionPath + "union-type-with-composing-closed-type/"
    )
  }

  test("Union type defined before composing types, with one type defined as closed") {
    cycle(
      "additional-prop-and-defined-before.raml",
      "additional-prop-and-defined-before.jsonld",
      RamlYamlHint,
      Amf,
      productionPath + "union-type-with-composing-closed-type/"
    )
  }

  test("Tracking equal example in different endpoints") {
    cycle(
      "dup-name-example-tracking.raml",
      "dup-name-example-tracking.jsonld",
      RamlYamlHint,
      Amf,
      validationsPath + "dup-name-example-tracking/"
    )
  }

  test("Library with types") {
    cycle("lib.raml", "lib.jsonld", RamlYamlHint, Amf, productionPath + "lib-types/")
  }

  test("Union of arrays") {
    cycle("api.raml", "api.jsonld", RamlYamlHint, Amf, resolutionPath + "union-of-arrays/")
  }

  test("Inheritance provenance annotation from declaration") {
    cycle("api.raml", "api.jsonld", RamlYamlHint, Amf, validationsPath + "inheritance-provenance/from-declaration/")
  }

  test("Inheritance provenance annotation with recursive inheritance") {
    cycle("api.raml",
          "api.jsonld",
          RamlYamlHint,
          Amf,
          validationsPath + "inheritance-provenance/with-recursive-inheritance/")
  }

  test("Resolved link annotation with types") {
    cycle("api.raml", "api.jsonld", RamlYamlHint, Amf, validationsPath + "resolved-link-annotation/")
  }

  test("Inheritance provenance annotation with regular inheritance") {
    cycle("api.raml",
          "api.jsonld",
          RamlYamlHint,
          Amf,
          validationsPath + "inheritance-provenance/with-regular-inheritance/")
  }

  test("Inheritance provenance annotation with library") {
    cycle("api.raml", "api.jsonld", RamlYamlHint, Amf, validationsPath + "inheritance-provenance/with-library/")
  }

  test("Recursion in inheritance with resource type - Properties") {
    cycle("recursion-inheritance-properties.raml",
          "recursion-inheritance-properties.jsonld",
          RamlYamlHint,
          Amf,
          validationsPath)
  }

  test("Recursion in inheritance with resource type - Array") {
    Validation(platform)
      .flatMap { validation =>
        cycle(
          "recursion-inheritance-array.raml",
          "recursion-inheritance-array.jsonld",
          RamlYamlHint,
          Raml08,
          validationsPath,
          validation = Some(validation.withEnabledValidation(true))
        )
      }
  }

  test("Generate jsonld with sourcemaps") {
    cycle(
      "payloads-examples-resolution.raml",
      "../validations/jsonld-compact-uris/no-raw-source-maps-compact-uris.jsonld",
      RamlYamlHint,
      Amf,
      resolutionPath
    )
  }

  test("Parsing compacted jsonld using context of compact uris") {
    cycle(
      "no-raw-source-maps-compact-uris.jsonld",
      "parsed-result.jsonld",
      AmfJsonHint,
      Amf,
      validationsPath + "jsonld-compact-uris/"
    )
  }

  test("Annotated scalar node with no value should default to an empty node") {
    cycle(
      "optional-scalar-value.raml",
      "optional-scalar-value.jsonld",
      RamlYamlHint,
      Amf,
      validationsPath + "optional-scalar-value/"
    )
  }

  test("Union type containing array type") {
    cycle(
      "union-type-containg-array.raml",
      "union-type-containg-array.jsonld",
      RamlYamlHint,
      Amf,
      validationsPath + "union-type-containg-array/"
    )
  }

  test("Resolution of server objects") {
    cycle(
      "overriding-server-object.json",
      "overriding-server-object-resolved.json",
      OasJsonHint,
      Oas30,
      cyclePath + "oas3/"
    )
  }

  test("Overriding parameters in operation") {
    cycle(
      "overriding-parameters.json",
      "overriding-param-output.json",
      OasJsonHint,
      Oas30,
      cyclePath + "oas3/basic-parameters/"
    )
  }

  test("Summary and description from path applied to operations") {
    cycle(
      "description-applied-to-operations.json",
      "description-applied-to-operations-editing.json",
      OasJsonHint,
      Oas30,
      cyclePath + "oas3/summary-description-in-path/"
    )
  }

  // This test hangs diff
  ignore("Emission of API with JSON Schema's schema as references") {
    cycle("api.raml", "api.jsonld", RamlYamlHint, Amf, resolutionPath + "stackoverflow-case/")
  }

  /*
  test("Exchange experience API resolution to Amf") {
    cycle("api.v1.raml", "api.v1.resolved.jsonld", RamlYamlHint, Amf, productionPath + "exchange-experience-api-1.0.1-raml/")
  }

  ignore("Github API resolution to Raml") {
    cycle("api.raml", "api.yaml.jsonld", RamlYamlHint, Amf, productionPath + "github-api-1.0.0-raml/")
  }

  test("Google API resolution to Raml") {
    cycle("googleapis.compredictionv1.2swagger.raml", "googleapis.compredictionv1.2swagger.raml", RamlYamlHint, Amf, productionPath)
  }

  test("Financial API resolution to Raml") {
    cycle("infor-financial-api.raml", "infor-financial-api.yaml.jsonld", RamlYamlHint, Amf, productionPath + "financial-api/")
  }
   */

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit =
    config.target match {
      case Raml08        => Raml08Plugin.resolve(unit, UnhandledErrorHandler, ResolutionPipeline.EDITING_PIPELINE)
      case Raml | Raml10 => Raml10Plugin.resolve(unit, UnhandledErrorHandler, ResolutionPipeline.EDITING_PIPELINE)
      case Oas30         => Oas30Plugin.resolve(unit, UnhandledErrorHandler, ResolutionPipeline.EDITING_PIPELINE)
      case Oas | Oas20   => Oas20Plugin.resolve(unit, UnhandledErrorHandler, ResolutionPipeline.EDITING_PIPELINE)
      case Amf           => AmfEditingPipeline.unhandled.resolve(unit)
      case target        => throw new Exception(s"Cannot resolve $target")
    }

  override def render(unit: BaseUnit, config: CycleConfig, useAmfJsonldSerialization: Boolean): Future[String] = {
    new AMFRenderer(unit,
                    config.target,
                    RenderOptions().withSourceMaps.withRawSourceMaps.withCompactUris.withPrettyPrint,
                    config.syntax).renderToString
  }

  override val basePath: String = ""
}
