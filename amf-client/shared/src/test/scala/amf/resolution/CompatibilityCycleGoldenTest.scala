package amf.resolution

import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline

import scala.concurrent.ExecutionContext

class CompatibilityCycleGoldenTest extends ResolutionTest {

  override val defaultPipelineToUse: String                = ResolutionPipeline.COMPATIBILITY_PIPELINE
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  override def basePath: String = "amf-client/shared/src/test/resources/compatibility/"

  test("Identical RAML inherited examples are removed in OAS 2.0") {
    cycle("raml10/inherited-examples.raml",
          "cycled-apis/oas20/inherited-examples.json",
          RamlYamlHint,
          Oas20,
          transformWith = Some(Oas20))
  }

  test("declared type is extracted to definitions facet") {
    cycle("raml10/reusing-declared-type.raml",
          "cycled-apis/oas20/reusing-declared-type.json",
          RamlYamlHint,
          Oas20,
          transformWith = Some(Oas20))
  }

  test("RAML operations without names do not generate null_0 operationIds in OAS 3.0") {
    cycle("raml10/no-operation-name.raml",
          "cycled-apis/oas30/no-operation-name.json",
          RamlYamlHint,
          Oas30,
          transformWith = Some(Oas30))
  }

  test("RAML operations without names do not generate null_0 operationIds in OAS 2.0") {
    cycle("raml10/no-operation-name.raml",
          "cycled-apis/oas20/no-operation-name.json",
          RamlYamlHint,
          Oas20,
          transformWith = Some(Oas20))
  }

  test("OAS operation documentation is transformed to RAML") {
    cycle("oas30/documentation-in-operation.json",
          "cycled-apis/raml/documentation-in-operation.raml",
          OasJsonHint,
          Raml10,
          transformWith = Some(Raml10))
  }

  test("OAS tag documentation is transformed to RAML with generator count") {
    cycle(
      "invalid-apis/documentation-tags-with-no-name.json",
      "cycled-apis/raml/documentation-tags-with-no-name.raml",
      OasJsonHint,
      Raml10,
      transformWith = Some(Raml10)
    )
  }

  test("OAS 3.0 Callbacks are inlined and unused ones are removed") {
    cycle("oas30/component-callbacks.json",
          "cycled-apis/raml/oas3-callbacks.raml",
          OasYamlHint,
          Raml10,
          transformWith = Some(Raml10))
  }

  test("OAS 2.0 Module emitter emits swagger: 2.0 key") {
    cycle("invalid-apis/library.raml",
          "cycled-apis/oas20/library.json",
          RamlYamlHint,
          Oas20,
          transformWith = Some(Oas20))
  }

  test("OAS 3.0 Module emitter emits openapi: 3.0.0 key") {
    cycle("invalid-apis/library.raml",
          "cycled-apis/oas30/library.json",
          RamlYamlHint,
          Oas30,
          transformWith = Some(Oas30))
  }

  test("Unused OAS 3.0 examples are deleted and used ones inlined") {
    cycle("oas30/oas-unused-examples-deleted.json",
          "cycled-apis/raml/oas-unused-examples-deleted.raml",
          OasYamlHint,
          Raml,
          transformWith = Some(Raml10))
  }

  test("OAS 3.0 examples are translated correctly to Raml example and examples") {
    cycle("oas30/oas-anonymous-named-examples.json",
          "cycled-apis/raml/oas-anonymous-named-examples.raml",
          OasYamlHint,
          Raml,
          transformWith = Some(Raml10))
  }
}
