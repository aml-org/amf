package amf.resolution

import amf.core.client.common.transform._
import amf.core.internal.remote._

import scala.concurrent.ExecutionContext

class ShapeNormalizationCornerCasesTest extends ResolutionTest {

  override val defaultPipeline: String                     = PipelineId.Editing
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  override val basePath: String = "amf-cli/shared/src/test/resources/resolution/shape-normalization/"

  multiGoldenTest("Resolve cyclic inheritance", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      target = AmfJsonHint,
      directory = basePath + "cyclic-inheritance/",
      renderOptions = Some(config.renderOptions.withPrettyPrint),
      transformWith = Some(Raml10)
    )
  }

  multiGoldenTest("Resolve cycle though union members", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      target = AmfJsonHint,
      directory = basePath + "cyclic-union-members/",
      renderOptions = Some(config.renderOptions.withPrettyPrint),
      transformWith = Some(Raml10)
    )
  }

}
