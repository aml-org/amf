package amf.resolution

import amf.core.client.common.transform._
import amf.core.internal.remote._

class CompatibilityCycleGoldenTest extends ResolutionTest {

  override val defaultPipeline: String = PipelineId.Compatibility

  override def basePath: String = "amf-cli/shared/src/test/resources/compatibility/"

  test("Identical RAML inherited examples are removed in OAS 2.0") {
    cycle(
      "raml10/inherited-examples.raml",
      "cycled-apis/oas20/inherited-examples.json",
      Raml10YamlHint,
      Oas20JsonHint,
      transformWith = Some(Oas20)
    )
  }

  test("declared type is extracted to definitions facet") {
    cycle(
      "raml10/reusing-declared-type.raml",
      "cycled-apis/oas20/reusing-declared-type.json",
      Raml10YamlHint,
      Oas20JsonHint,
      transformWith = Some(Oas20)
    )
  }

  test("RAML operations without names do not generate null_0 operationIds in OAS 3.0") {
    cycle(
      "raml10/no-operation-name.raml",
      "cycled-apis/oas30/no-operation-name.json",
      Raml10YamlHint,
      Oas30JsonHint,
      transformWith = Some(Oas30)
    )
  }

  test("RAML operations without names do not generate null_0 operationIds in OAS 2.0") {
    cycle(
      "raml10/no-operation-name.raml",
      "cycled-apis/oas20/no-operation-name.json",
      Raml10YamlHint,
      Oas20JsonHint,
      transformWith = Some(Oas20)
    )
  }

  test("OAS operation documentation is transformed to RAML") {
    cycle(
      "oas30/documentation-in-operation.json",
      "cycled-apis/raml/documentation-in-operation.raml",
      Oas30JsonHint,
      Raml10YamlHint,
      transformWith = Some(Raml10)
    )
  }

  test("OAS tag documentation is transformed to RAML with generator count") {
    cycle(
      "invalid-apis/documentation-tags-with-no-name.json",
      "cycled-apis/raml/documentation-tags-with-no-name.raml",
      Oas20JsonHint,
      Raml10YamlHint,
      transformWith = Some(Raml10)
    )
  }

  test("OAS 3.0 Callbacks are inlined and unused ones are removed") {
    cycle(
      "oas30/component-callbacks.json",
      "cycled-apis/raml/oas3-callbacks.raml",
      Oas30YamlHint,
      Raml10YamlHint,
      transformWith = Some(Raml10)
    )
  }

  test("OAS 2.0 Module emitter emits swagger: 2.0 key") {
    cycle(
      "invalid-apis/library.raml",
      "cycled-apis/oas20/library.json",
      Raml10YamlHint,
      Oas20JsonHint,
      transformWith = Some(Oas20)
    )
  }

  test("OAS 3.0 Module emitter emits openapi: 3.0.0 key") {
    cycle(
      "invalid-apis/library.raml",
      "cycled-apis/oas30/library.json",
      Raml10YamlHint,
      Oas30JsonHint,
      transformWith = Some(Oas30)
    )
  }

  test("Unused OAS 3.0 examples are deleted and used ones inlined") {
    cycle(
      "oas30/oas-unused-examples-deleted.json",
      "cycled-apis/raml/oas-unused-examples-deleted.raml",
      Oas30YamlHint,
      Raml10YamlHint,
      transformWith = Some(Raml10)
    )
  }

  test("OAS 3.0 examples are translated correctly to Raml example and examples") {
    cycle(
      "oas30/oas-anonymous-named-examples.json",
      "cycled-apis/raml/oas-anonymous-named-examples.raml",
      Oas30YamlHint,
      Raml10YamlHint,
      transformWith = Some(Raml10)
    )
  }

  test("OAS 2.0 security schemes translated to Raml") {
    cycle(
      "oas20/security-definitions.json",
      "cycled-apis/raml/oas2-security-definitions.raml",
      Oas20YamlHint,
      Raml10YamlHint,
      transformWith = Some(Raml10)
    )
  }

  test("OAS 3.0 security schemes translated to Raml") {
    cycle(
      "oas30/security-definitions.json",
      "cycled-apis/raml/oas3-security-definitions.raml",
      Oas30YamlHint,
      Raml10YamlHint,
      transformWith = Some(Raml10)
    )
  }

  test("Raml security schemes translated to OAS 2.0") {
    cycle(
      "raml10/apikey-settings.raml",
      "cycled-apis/oas20/raml-security-definitions.json",
      Raml10YamlHint,
      Oas20JsonHint,
      transformWith = Some(Oas20)
    )
  }

  test("Raml security schemes translated to OAS 3.0") {
    cycle(
      "raml10/apikey-settings.raml",
      "cycled-apis/oas30/raml-security-definitions.json",
      Raml10YamlHint,
      Oas30JsonHint,
      transformWith = Some(Oas30)
    )
  }

  test("OAS 3.0 nullable schemas are translated with union expression to raml") {
    cycle(
      "oas30/nullable-fields.json",
      "cycled-apis/raml/oas-nullable-fields.raml",
      Oas30YamlHint,
      Raml10YamlHint,
      transformWith = Some(Raml10)
    )
  }

  test("Declarations added in union as type expression to not collide with previously defined types") {
    cycle(
      "oas30/nullable-fields-with-declared-types.json",
      "cycled-apis/raml/nullable-fields-with-declared-types.raml",
      Oas30YamlHint,
      Raml10YamlHint,
      transformWith = Some(Raml10)
    )
  }

  test("Base accepts and content-type are propagated to payloads") {
    cycle(
      "oas20/propagate-base-produces-and-consumes.json",
      "cycled-apis/raml/propagate-base-produces-and-consumes.raml",
      Oas20YamlHint,
      Raml10YamlHint,
      transformWith = Some(Raml10)
    )
  }

  test("Accepts and content-type are propagated to payloads with override") {
    cycle(
      "oas20/propagate-produces-and-consumes-with-overrides.json",
      "cycled-apis/raml/propagate-produces-and-consumes-with-overrides.raml",
      Oas20YamlHint,
      Raml10YamlHint,
      transformWith = Some(Raml10)
    )
  }

  test("Consumes at root level in OAS 2.0 API") {
    cycle(
      "oas20/consumes-global.json",
      "cycled-apis/raml/consumes-global.raml",
      Oas20JsonHint,
      Raml10YamlHint,
      transformWith = Some(Raml10)
    )
  }

  test("Raml with file request payload converted to oas with valid consumes mediatype") {
    cycle(
      "raml10/request-with-type-file-in-body.raml",
      "cycled-apis/oas20/request-with-type-file-in-body.json",
      Raml10YamlHint,
      Oas20JsonHint,
      transformWith = Some(Oas20)
    )
  }

  test("OAS 2.0 with multiple operations in endpoint each one with uri parameter") {
    cycle(
      "oas20/operations-uri-param.json",
      "cycled-apis/raml/operations-uri-param.raml",
      Oas20JsonHint,
      Raml10YamlHint,
      transformWith = Some(Raml10)
    )
  }

  test("OAS 2.0 with multiple operations in endpoint each one with uri parameter cycle back") {
    cycle(
      "cycled-apis/raml/operations-uri-param.raml",
      "cycled-apis/oas20/operations-uri-param.json",
      Raml10YamlHint,
      Oas20JsonHint,
      transformWith = Some(Oas20)
    )
  }

  test("Raml with security scheme from library translated to OAS 2.0") {
    cycle(
      "raml10/security-scheme-from-library.raml",
      "cycled-apis/oas20/security-scheme-from-library.json",
      Raml10YamlHint,
      Oas20JsonHint,
      transformWith = Some(Oas20)
    )
  }

  test("Raml with security scheme from library translated to OAS 3.0") {
    cycle(
      "raml10/security-scheme-from-library.raml",
      "cycled-apis/oas30/security-scheme-from-library.json",
      Raml10YamlHint,
      Oas30JsonHint,
      transformWith = Some(Oas30)
    )
  }

  // W-17759271
  test("Raml with file upload parameter to OAS 2.0") {
    cycle(
      "raml10/raml-files-to-oas.raml",
      "cycled-apis/oas20/oas2-file-param.yaml",
      Raml10YamlHint,
      Oas20YamlHint,
      transformWith = Some(Oas20)
    )
  }

  // W-17759271
  test("Raml with file upload parameter to OAS 3.0") {
    cycle(
      "raml10/raml-files-to-oas.raml",
      "cycled-apis/oas30/oas3-file-param.yaml",
      Raml10YamlHint,
      Oas30YamlHint,
      transformWith = Some(Oas30)
    )
  }

  test("Raml with file types in various places to OAS 3.0") {
    cycle(
      "raml10/raml-file-types.raml",
      "cycled-apis/oas30/oas3-file-types.yaml",
      Raml10YamlHint,
      Oas30YamlHint,
      transformWith = Some(Oas30)
    )
  }
}
