package amf.render
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, Raml10YamlHint}
import amf.resolution.ResolutionTest
import scala.concurrent.ExecutionContext

class GovernanceModeTest extends ResolutionTest {

  override val defaultPipeline: String                     = PipelineId.Editing
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  override val basePath: String                            = "amf-cli/shared/src/test/resources/render/"

  test("Generate flattened jsonld with Governance Mode for RAML") {
    cycle(
      "api.raml",
      "api.flattened.jsonld",
      Raml10YamlHint,
      target = AmfJsonHint,
      basePath + "governance-mode/raml/",
      renderOptions = Some(RenderOptions().withPrettyPrint.withGovernanceMode)
    )
  }

  test("Generate flattened jsonld with Governance Mode for Async") {
    cycle(
      "api.yaml",
      "api.flattened.jsonld",
      Raml10YamlHint,
      target = AmfJsonHint,
      basePath + "governance-mode/async/",
      renderOptions = Some(RenderOptions().withPrettyPrint.withGovernanceMode)
    )
  }

  test("Generate flattened jsonld with Governance Mode for OAS") {
    cycle(
      "api.yaml",
      "api.flattened.jsonld",
      Raml10YamlHint,
      target = AmfJsonHint,
      basePath + "governance-mode/oas/",
      renderOptions = Some(RenderOptions().withPrettyPrint.withGovernanceMode)
    )
  }

  test("Generate expanded jsonld with Governance Mode for RAML") {
    cycle(
      "api.raml",
      "api.expanded.jsonld",
      Raml10YamlHint,
      target = AmfJsonHint,
      basePath + "governance-mode/raml/",
      renderOptions = Some(RenderOptions().withPrettyPrint.withGovernanceMode.withoutFlattenedJsonLd)
    )
  }

  test("Generate expanded jsonld with Governance Mode for Async") {
    cycle(
      "api.yaml",
      "api.expanded.jsonld",
      Raml10YamlHint,
      target = AmfJsonHint,
      basePath + "governance-mode/async/",
      renderOptions = Some(RenderOptions().withPrettyPrint.withGovernanceMode.withoutFlattenedJsonLd)
    )
  }

  test("Generate expanded jsonld with Governance Mode for OAS") {
    cycle(
      "api.yaml",
      "api.expanded.jsonld",
      Raml10YamlHint,
      target = AmfJsonHint,
      basePath + "governance-mode/oas/",
      renderOptions = Some(RenderOptions().withPrettyPrint.withGovernanceMode.withoutFlattenedJsonLd)
    )
  }
}
