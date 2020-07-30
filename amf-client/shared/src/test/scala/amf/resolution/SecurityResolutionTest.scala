package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.remote.{Amf, OasJsonHint, RamlYamlHint}

class SecurityResolutionTest extends ResolutionTest {

  override val basePath = "amf-client/shared/src/test/resources/resolution/security/"

  multiGoldenTest("Security resolution raml to AMF", "security.raml.%s") { config =>
    cycle("security.raml", config.golden, RamlYamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Security resolution oas to AMF", "security.json.%s") { config =>
    cycle("security.json", config.golden, OasJsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
