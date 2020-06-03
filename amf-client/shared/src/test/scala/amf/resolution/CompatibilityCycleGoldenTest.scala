package amf.resolution

import amf.core.remote.{Oas20, RamlYamlHint}
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

}
