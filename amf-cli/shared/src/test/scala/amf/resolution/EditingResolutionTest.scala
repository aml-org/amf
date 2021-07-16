package amf.resolution

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.common.transform._
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.Syntax.Yaml
import amf.core.internal.remote._

import scala.concurrent.ExecutionContext

class EditingResolutionTest extends ResolutionTest {

  override val defaultPipelineToUse: String = PipelineId.Editing

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val extendsPath     = "amf-cli/shared/src/test/resources/resolution/extends/"
  val productionPath  = "amf-cli/shared/src/test/resources/production/"
  val resolutionPath  = "amf-cli/shared/src/test/resources/resolution/"
  val cyclePath       = "amf-cli/shared/src/test/resources/upanddown/"
  val referencesPath  = "amf-cli/shared/src/test/resources/references/"
  val validationsPath = "amf-cli/shared/src/test/resources/validations/"

  multiGoldenTest("API with recursive shapes", "recursive3.editing.%s") { config =>
    cycle("recursive3.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          productionPath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Simple extends resolution to Raml", "simple-merge.editing.%s") { config =>
    cycle("simple-merge.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          extendsPath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Types resolution to Raml", "data.editing.%s") { config =>
    cycle("data.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          extendsPath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Include type resolution to Raml", "simple_example_type.resolved.%s") { config =>
    cycle("simple_example_type.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          cyclePath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Test data type fragment resolution to Amf", "data-type-fragment.reference.resolved.%s") { config =>
    cycle("data-type-fragment.reference.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          referencesPath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Test union arrays", "union_arrays.resolved.%s") { config =>
    cycle("union_arrays.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          cyclePath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Exchange issueNil API resolution to Amf", "api.resolved.%s") { config =>
    cycle("api.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          validationsPath + "examples/inline-named-examples/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Location in annotation of Trait declared in lib", "api.resolved.%s") { config =>
    cycle("api.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          productionPath + "lib-trait-location/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Test url shortener with external references", "api.resolved.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      resolutionPath + "externalfragment/test-links-with-references/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Test tracked examples annotations parent shortened", "payloads-examples-resolution.resolved.%s") {
    config =>
      cycle("payloads-examples-resolution.raml",
            config.golden,
            Raml10YamlHint,
            target = Amf,
            resolutionPath,
            renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Test extension merging", "input.resolved.%s") { config =>
    cycle(
      "input.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      "amf-cli/shared/src/test/resources/resolution/extension/traits/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Unresolved shape", "unresolved-shape.raml.%s") { config =>
    cycle("unresolved-shape.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          resolutionPath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Test recursive annotations of extension provenance", "api.resolved.%s") { config =>
    cycle("api.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          resolutionPath + "recursive-extension-provenance/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Test url shortener at example (dynamic)", "examples-shortener.resolved.%s") { config =>
    cycle("examples-shortener.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          resolutionPath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Test double declared included type", "api.resolved.%s") { config =>
    cycle("api.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          resolutionPath + "double-declare-type/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Test declared type from library", "api.resolved.%s") { config =>
    cycle("api.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          resolutionPath + "declared-from-library/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Test union of declared elements", "api.raml.resolved.%s") { config =>
    cycle("api.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          resolutionPath + "union-of-declarations/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Check for stack overflow in event api", "api.%s") { config =>
    cycle("api.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          productionPath + "event-api/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Test tracked examples in oas responses", "oas-multiple-example.resolved.%s") { config =>
    cycle("oas-multiple-example.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          productionPath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Parse correctly non-AMF graph JSON-LD example", "api.%s") { config =>
    cycle("api.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          resolutionPath + "jsonld-example/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Root mediaType propagation should also adopt tracked-element annotation",
                  "root-mediatype-propagation.%s") { config =>
    cycle(
      "root-mediatype-propagation.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      validationsPath + "root-mediatype-propagation/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Propagate tracked-element to linked examples", "tracked-to-linked.%s") { config =>
    cycle("tracked-to-linked.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          validationsPath + "tracked-to-linked/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Adopt tracked-element when merging abstract declarations", "tracked-from-resource-type.%s") {
    config =>
      cycle(
        "tracked-from-resource-type.raml",
        config.golden,
        Raml10YamlHint,
        target = Amf,
        validationsPath + "tracked-from-resource-type/",
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("Auto generated payload name annotation in raml", "auto-generated-schema-name.%s") { config =>
    cycle(
      "auto-generated-schema-name.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      validationsPath + "auto-generated-schema-name/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Auto generated payload name annotation in oas", "auto-generated-schema-name-oas.%s") { config =>
    cycle(
      "auto-generated-schema-name-oas.yaml",
      config.golden,
      Oas20YamlHint,
      target = Amf,
      validationsPath + "auto-generated-schema-name/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Auto generated payload name annotation with default mediaType",
                  "auto-generated-schema-name-with-default.%s") { config =>
    cycle(
      "auto-generated-schema-name-with-default.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      validationsPath + "auto-generated-schema-name/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Declared type union with inherit array link", "union-type-array.%s") { config =>
    cycle("union-type-array.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          validationsPath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Tracked oas examples", "tracked-oas-examples.%s") { config =>
    cycle("tracked-oas-examples.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          validationsPath + "tracked-oas-examples/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Keep schema name in body link schema", "body-link-name.%s") { config =>
    cycle("body-link-name.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          validationsPath + "body-link-name/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Union type defined under composing types, with one type defined as closed",
                  "additional-prop-and-defined-after.%s") { config =>
    cycle(
      "additional-prop-and-defined-after.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      productionPath + "union-type-with-composing-closed-type/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Union type defined before composing types, with one type defined as closed",
                  "additional-prop-and-defined-before.%s") { config =>
    cycle(
      "additional-prop-and-defined-before.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      productionPath + "union-type-with-composing-closed-type/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Tracking equal example in different endpoints", "dup-name-example-tracking.%s") { config =>
    cycle(
      "dup-name-example-tracking.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      validationsPath + "dup-name-example-tracking/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Library with types", "lib.%s") { config =>
    cycle("lib.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          productionPath + "lib-types/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Union of arrays", "api.%s") { config =>
    cycle("api.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          resolutionPath + "union-of-arrays/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Inheritance provenance annotation from declaration", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      validationsPath + "inheritance-provenance/from-declaration/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Inheritance provenance annotation with recursive inheritance", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      validationsPath + "inheritance-provenance/with-recursive-inheritance/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Resolved link annotation with types", "api.%s") { config =>
    cycle("api.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          validationsPath + "resolved-link-annotation/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Inheritance provenance annotation with regular inheritance", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      validationsPath + "inheritance-provenance/with-regular-inheritance/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Inheritance provenance annotation with library", "api.%s") { config =>
    cycle("api.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          validationsPath + "inheritance-provenance/with-library/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Recursion in inheritance with resource type - Properties", "recursion-inheritance-properties.%s") {
    config =>
      cycle("recursion-inheritance-properties.raml",
            config.golden,
            Raml10YamlHint,
            target = Amf,
            validationsPath,
            renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Generate jsonld with sourcemaps",
                  "../validations/jsonld-compact-uris/no-raw-source-maps-compact-uris.%s") { config =>
    cycle("payloads-examples-resolution.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          resolutionPath,
          renderOptions = Some(config.renderOptions))
  }

  multiTest("Parsing compacted jsonld using context of compact uris",
            "no-raw-source-maps-compact-uris.%s",
            "parsed-result.%s") { config =>
    cycle(
      config.source,
      config.golden,
      AmfJsonHint,
      target = Amf,
      validationsPath + "jsonld-compact-uris/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Annotated scalar node with no value should default to an empty node", "optional-scalar-value.%s") {
    config =>
      cycle(
        "optional-scalar-value.raml",
        config.golden,
        Raml10YamlHint,
        target = Amf,
        validationsPath + "optional-scalar-value/",
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("Union type containing array type", "union-type-containg-array.%s") { config =>
    cycle(
      "union-type-containg-array.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      validationsPath + "union-type-containg-array/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Tracked examples from parameters and payloads", "parameter-payload-examples.%s") { config =>
    cycle(
      "parameter-payload-examples.json",
      config.golden,
      Oas30JsonHint,
      target = Amf,
      cyclePath + "oas3/parameter-payload-resolution/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Resolving parameter without type doesnt throw NPE", "parameter-without-type.%s") { config =>
    cycle(
      "parameter-without-type.json",
      config.golden,
      Oas20JsonHint,
      target = Amf,
      resolutionPath + "parameter-without-type/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Tracked examples in OAS body parameter with mediatype annotation", "tracked-oas-param-body.%s") {
    config =>
      cycle(
        "tracked-oas-param-body.yaml",
        config.golden,
        Oas20YamlHint,
        target = Amf,
        validationsPath + "tracked-oas-param-body/",
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("Test reference resolution with chained links", "api.%s") { config =>
    cycle("api.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          validationsPath + "links/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Security schemes with requirements", "security-requirements.%s") { config =>
    cycle("security-requirements.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          resolutionPath + "security-requirements/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("tracked element in example defined in resource type", "examples-defined-in-rt.%s") { config =>
    cycle(
      "examples-defined-in-rt.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      resolutionPath + "example-in-resource-type/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("recursivity in additional properties", "recursive-additional-properties.%s") { config =>
    cycle(
      "recursive-additional-properties.yaml",
      config.golden,
      Oas20YamlHint,
      target = Amf,
      s"${resolutionPath}recursive-additional-properties/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("recursivity in additional properties 2", "recursive-additional-properties-2.%s") { config =>
    cycle(
      "recursive-additional-properties-2.yaml",
      config.golden,
      Oas20YamlHint,
      target = Amf,
      s"${resolutionPath}recursive-additional-properties/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("types with properties that must not be extracted to declares", "avoid-extract-to-declares.%s") {
    config =>
      cycle(
        "avoid-extract-to-declares.raml",
        config.golden,
        Raml10YamlHint,
        target = Amf,
        resolutionPath + "links-to-declares-and-references/",
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("jsonld with links to declares and references", "link-to-declares-and-refs-editing.%s") { config =>
    cycle(
      "link-to-declares-and-refs.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      resolutionPath + "links-to-declares-and-references/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("References to message definitions", "message-references.%s") { config =>
    cycle("message-references.yaml",
          config.golden,
          Async20YamlHint,
          target = Amf,
          resolutionPath + "async20/",
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("raml with declared element link of link", "link-of-link/link-of-link.%s") { config =>
    cycle(
      "link-of-link/link-of-link.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      directory = resolutionPath,
      transformWith = Some(Raml10),
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("raml with declared element link of link of link", "link-of-link/link-of-link-of-link.%s") {
    config =>
      cycle(
        "link-of-link/link-of-link-of-link.raml",
        config.golden,
        Raml10YamlHint,
        target = Amf,
        directory = resolutionPath,
        transformWith = Some(Raml10),
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("raml with declared element link of link in api", "link-of-link/in-api/link-of-link-in-api.%s") {
    config =>
      cycle(
        "link-of-link/in-api/link-of-link-in-api.raml",
        config.golden,
        Raml10YamlHint,
        target = Amf,
        directory = resolutionPath,
        transformWith = Some(Raml10),
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("raml with declared element link of link of link in api",
                  "link-of-link/middle-link-in-api/link-of-link-in-api.%s") { config =>
    cycle(
      "link-of-link/middle-link-in-api/link-of-link-in-api.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      directory = resolutionPath,
      transformWith = Some(Raml10),
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Oas declared type alias inheritance with scalar type is valid", "oas-declared-link-of-scalar.%s") {
    config =>
      cycle(
        "oas-declared-link-of-scalar.json",
        config.golden,
        Oas30JsonHint,
        target = Amf,
        directory = resolutionPath,
        transformWith = Some(Oas30),
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("Shared response references in OAS 2.0", "shared-response-reference/oas20/api.%s") { config =>
    cycle(
      "shared-response-reference/oas20/api.yaml",
      config.golden,
      Oas20YamlHint,
      target = Amf,
      directory = resolutionPath,
      transformWith = Some(Oas20),
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Shared response references in OAS 3.0", "shared-response-reference/oas30/api.%s") { config =>
    cycle(
      "shared-response-reference/oas30/api.yaml",
      config.golden,
      Oas30YamlHint,
      target = Amf,
      directory = resolutionPath,
      transformWith = Some(Oas30),
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Shared request body references in OAS 3.0", "shared-request-body-reference/oas30/api.%s") {
    config =>
      cycle(
        "shared-request-body-reference/oas30/api.yaml",
        config.golden,
        Oas30YamlHint,
        target = Amf,
        directory = resolutionPath,
        transformWith = Some(Oas30),
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("Shared examples in OAS 3.0", "shared-oas-30-examples/api.%s") { config =>
    cycle(
      "shared-oas-30-examples/api.yaml",
      config.golden,
      Oas30YamlHint,
      target = Amf,
      directory = resolutionPath,
      transformWith = Some(Oas30),
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Oas 3 autogenerated name in for inlined shapes", "oas3-inlined-shapes.%s") { config =>
    cycle("oas3-inlined-shapes.yaml",
          config.golden,
          Oas30YamlHint,
          target = Amf,
          directory = resolutionPath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Oas 3 resolve request links that have parameters", "request-link-parameters/api.%s") { config =>
    cycle(
      "request-link-parameters/api.yaml",
      config.golden,
      Oas30YamlHint,
      target = Amf,
      directory = resolutionPath,
      transformWith = Some(Oas30),
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Oas 2 recursion detection", "oas-recursion.%s") { config =>
    cycle(
      "oas-recursion.json",
      config.golden,
      Oas20JsonHint,
      target = Amf,
      directory = resolutionPath,
      transformWith = Some(Oas20),
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Resolve links defined in rt and traits before merging", "trait-with-link.%s") { config =>
    cycle("trait-with-link.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          directory = resolutionPath,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Internal json schema link in OAS", "oas-internal-json-schema-link/api.%s") { config =>
    cycle(
      "oas-internal-json-schema-link/api.yaml",
      config.golden,
      Oas20YamlHint,
      target = Amf,
      directory = resolutionPath,
      renderOptions = Some(config.renderOptions),
      eh = Some(UnhandledErrorHandler)
    )
  }

  multiGoldenTest("Avoid autogenerated endpoint param that is defined in operations", "result.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      directory = resolutionPath + "operation-path-parameters/",
      transformWith = Some(Raml10),
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("duplicate id fix for references in external schemas with composition", "result.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      directory = validationsPath + "json-schema-nested-refs/",
      transformWith = Some(Raml10),
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("reference to declared schema from allOf facet", "result.%s") { config =>
    cycle(
      "api.json",
      config.golden,
      Oas30JsonHint,
      target = Amf,
      directory = validationsPath + "ref-from-allof-facet/",
      transformWith = Some(Oas30),
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("multiple references to external schema", "result.%s") { config =>
    cycle(
      "api.json",
      config.golden,
      Oas20JsonHint,
      target = Amf,
      directory = resolutionPath + "multiple-ref-to-external-schema/",
      transformWith = Some(Oas20),
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("binary external fragment with char 'NUL' is emitted as valid json", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      directory = resolutionPath + "binary-fragment/",
      renderOptions = Some(config.renderOptions),
      eh = Some(UnhandledErrorHandler)
    )
  }

  override def render(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): String = {
    val configuration = amfConfig.withRenderOptions(
      amfConfig.options.renderOptions.withRawSourceMaps.withSourceMaps.withCompactUris.withPrettyPrint)
    super.render(unit, config, configuration)
  }

  // This test hangs diff
  ignore("Emission of API with JSON Schema's schema as references") {
    cycle("api.raml", "api.jsonld", Raml10YamlHint, target = Amf, resolutionPath + "stackoverflow-case/")
  }

  // JSON-LD is serialized differently every time
  ignore("KG Service API resolution") {
    cycle(
      "knowledge-graph-service-api-1.0.13-raml/kg.raml",
      "knowledge-graph-service-api-1.0.13-raml/kg.jsonld",
      Raml10YamlHint,
      target = Amf,
      directory = productionPath,
      transformWith = Some(Raml10)
    )
  }

  test("Reduced KG Service API resolution") {
    cycle(
      "knowledge-graph-reduced/api.raml",
      "knowledge-graph-reduced/api.jsonld",
      Raml10YamlHint,
      target = Amf,
      directory = productionPath,
      transformWith = Some(Raml10)
    )
  }

  test("Example1 resolution to Raml") {
    cycle("example1.yaml", "example1.resolved.yaml", Oas20YamlHint, Oas20, resolutionPath, syntax = Some(Yaml))
  }

  test("Test merge examples in local against declared type") {
    cycle("merge-examples.raml", "merge-examples.resolved.raml", Raml10YamlHint, Raml10, resolutionPath + "examples/")
  }

  test("Response with reference to declaration") {
    cycle(
      "reference-response-declaration.json",
      "reference-response-declaration-resolved.json",
      Oas30JsonHint,
      Oas30,
      directory = cyclePath + "oas3/",
      transformWith = Some(Oas30)
    )
  }

  test("Resolution of server objects") {
    cycle("overriding-server-object.json",
          "overriding-server-object-resolved.json",
          Oas30JsonHint,
          Oas30,
          cyclePath + "oas3/")
  }

  test("Overriding parameters in operation") {
    cycle("overriding-parameters.json",
          "overriding-param-output.json",
          Oas30JsonHint,
          Oas30,
          cyclePath + "oas3/basic-parameters/")
  }

  test("Summary and description from path applied to operations") {
    cycle(
      "description-applied-to-operations.json",
      "description-applied-to-operations-editing.json",
      Oas30JsonHint,
      Oas30,
      cyclePath + "oas3/summary-description-in-path/"
    )
  }

  test("Recursion in inheritance with resource type - Array") {
    cycle("recursion-inheritance-array.raml",
          "recursion-inheritance-array.resolved.raml",
          Raml08YamlHint,
          Raml08,
          validationsPath)
  }

  multiGoldenTest("Recursive Tuple scheme", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml08YamlHint,
      target = Amf,
      directory = resolutionPath + "recursive-tuple/",
      transformWith = Some(Raml08),
      renderOptions = Some(config.renderOptions),
      eh = Some(UnhandledErrorHandler)
    )
  }

  test("OAS 3.0 API with type definitions referencing each other default") {
    cycle(
      "type-definitions-with-refs.json",
      "type-definitions-with-refs-with-compact.json",
      Oas30JsonHint,
      Oas30,
      directory = cyclePath + "oas3/",
      transformWith = Some(Oas30)
    )
  }

  test("OAS 2.0 API with type definitions referencing each other default emission") {
    cycle(
      "type-definitions-with-refs.json",
      "type-definitions-with-refs-with-compact.json",
      Oas20JsonHint,
      Oas20,
      directory = cyclePath + "cycle/oas20/json/",
      transformWith = Some(Oas20)
    )
  }

  test("OAS 3.0 API with type definitions referencing each other without compact emission") {
    cycle(
      "type-definitions-with-refs.json",
      "type-definitions-with-refs-no-compact.json",
      Oas30JsonHint,
      Oas30,
      directory = cyclePath + "oas3/",
      renderOptions = Some(RenderOptions().withoutCompactedEmission),
      transformWith = Some(Oas30)
    )
  }

  test("OAS 2.0 API with type definitions referencing each other without compact emission") {
    cycle(
      "type-definitions-with-refs.json",
      "type-definitions-with-refs-no-compact.json",
      Oas20JsonHint,
      Oas20,
      directory = cyclePath + "cycle/oas20/json/",
      renderOptions = Some(RenderOptions().withoutCompactedEmission),
      transformWith = Some(Oas20)
    )
  }

  multiGoldenTest("Operation query and header parameters with same name", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      directory = resolutionPath + "raml-query-and-header-params/",
      transformWith = Some(Raml10),
      renderOptions = Some(config.renderOptions),
      eh = Some(UnhandledErrorHandler)
    )
  }

  multiGoldenTest("Raml properties have encoded uris as shacl:path", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      Amf,
      directory = resolutionPath + "encoded-uris-in-properties/",
      transformWith = Some(Raml10),
      eh = Some(UnhandledErrorHandler)
    )
  }

  test("Additional properties are inlined after resolution") {
    cycle(
      "api.yaml",
      "output.json",
      Oas30YamlHint,
      Oas30,
      directory = s"$cyclePath/oas3/reffed-additional-properties/",
      transformWith = Some(Oas30),
      eh = Some(UnhandledErrorHandler)
    )
  }

  multiGoldenTest("Resolved extension shouldn't lose Root field", "api.%s") { config =>
    cycle(
      "extension.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      directory = s"$extendsPath/to-jsonld-and-back/",
      transformWith = Some(Raml10),
      renderOptions = Option(config.renderOptions),
      eh = Some(UnhandledErrorHandler)
    )
  }

  multiSourceTest("Resolved extension shouldn't lose Root field to RAML", "api.%s") { config =>
    cycle(
      config.source,
      "cycled-api.raml",
      AmfJsonHint,
      target = Raml10,
      directory = s"$extendsPath/to-jsonld-and-back/",
      transformWith = Some(Raml10),
      eh = Some(UnhandledErrorHandler)
    )
  }

  multiGoldenTest("Merge recursive JSON Schemas RAML 1.0", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      directory = resolutionPath + "merge-recursive-json-schemas-raml10/",
      transformWith = Some(Raml10),
      renderOptions = Some(config.renderOptions),
      eh = Some(UnhandledErrorHandler)
    )
  }

  multiGoldenTest("Merge recursive JSON Schemas RAML 0.8", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml08YamlHint,
      target = Amf,
      directory = resolutionPath + "merge-recursive-json-schemas-raml08/",
      transformWith = Some(Raml08),
      renderOptions = Some(config.renderOptions),
      eh = Some(UnhandledErrorHandler)
    )
  }

  // Merged inherit tests - Non ignored tests work
  {
    // Fails w/Stack overflow
    multiGoldenTest("Merge inlined recursive JSON Schemas RAML 1.0", "api.%s", ignored = true) { config =>
      cycle(
        "api.raml",
        config.golden,
        Raml10YamlHint,
        target = Amf,
        directory = resolutionPath + "merge-inlined-recursive-json-schemas/",
        transformWith = Some(Raml10),
        renderOptions = Some(config.renderOptions),
        eh = Some(UnhandledErrorHandler)
      )
    }

    // Fails w/ class cast exception in shape normalization
    /**
      * Traversal:
      *   SomeSchema (@id: ../endpoints/../schema/schema
      *     property: causes (@id: ../endpoints/../schema/schema/property/causes)
      *       range: array (@id: ../endpoints/../schema/schema/property/causes/array/causes)
      *         items: SomeSchema (@id: ...#/definitions/SomeSchema) <- Recursive shape should be detected here!
      *           property: causes (@id: ../endpoints/../schema/schema/property/causes) <- But instead here
      */
    multiGoldenTest("Merge recursive JSON Schema fragments RAML 1.0", "api.%s", ignored = true) { config =>
      cycle(
        "api.raml",
        config.golden,
        Raml10YamlHint,
        target = Amf,
        directory = resolutionPath + "merge-recursive-json-schema-fragments/",
        transformWith = Some(Raml10),
        renderOptions = Some(config.renderOptions),
        eh = Some(UnhandledErrorHandler)
      )
    }

    multiGoldenTest("Merge inherits RAML 1.0", "api.%s") { config =>
      cycle(
        "api.raml",
        config.golden,
        Raml10YamlHint,
        target = Amf,
        directory = resolutionPath + "merge-inherits/",
        transformWith = Some(Raml10),
        renderOptions = Some(config.renderOptions),
        eh = Some(UnhandledErrorHandler)
      )
    }

    multiGoldenTest("Merge recursive inherits RAML 1.0", "api.%s") { config =>
      cycle(
        "api.raml",
        config.golden,
        Raml10YamlHint,
        target = Amf,
        directory = resolutionPath + "merge-recursive-inherits/",
        transformWith = Some(Raml10),
        renderOptions = Some(config.renderOptions),
        eh = Some(UnhandledErrorHandler)
      )
    }

    multiGoldenTest("Merge inherits with JSON Schema fragments in RAML 1.0", "api.%s") { config =>
      cycle(
        "api.raml",
        config.golden,
        Raml10YamlHint,
        target = Amf,
        directory = resolutionPath + "merge-inherits-json-schema-fragments/",
        transformWith = Some(Raml10),
        renderOptions = Some(config.renderOptions),
        eh = Some(UnhandledErrorHandler)
      )
    }

    multiGoldenTest("OAS 3.0 discriminators", "api.%s") { config =>
      cycle(
        "api.yaml",
        config.golden,
        Oas30YamlHint,
        target = Amf,
        directory = resolutionPath + "oas30-discriminator/",
        transformWith = Some(Oas30),
        renderOptions = Some(config.renderOptions),
        eh = Some(UnhandledErrorHandler)
      )
    }

    multiGoldenTest("OAS 3.0 discriminators from JSON-LD", "api.target.%s") { config =>
      cycle(
        "api.source.flattened.jsonld",
        config.golden,
        AmfJsonHint,
        target = Amf,
        directory = resolutionPath + "oas30-discriminator-json-ld/",
        transformWith = Some(Oas30),
        renderOptions = Some(config.renderOptions)
      )
    }

    multiGoldenTest("OAS 3.0 discriminators invalid mapping", "api.%s") { config =>
      cycle(
        "api.yaml",
        config.golden,
        Oas30YamlHint,
        target = Amf,
        directory = resolutionPath + "oas30-discriminator-invalid-mapping/",
        transformWith = Some(Oas30),
        renderOptions = Some(config.renderOptions)
      )
    }

  }

  override val basePath: String = ""
}
