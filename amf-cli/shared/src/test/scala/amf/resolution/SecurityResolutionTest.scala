package amf.resolution

import amf.client.remod.amfcore.config.RenderOptions
import amf.core.remote.{Amf, Oas20, Oas20JsonHint, Raml10, Raml10YamlHint}

class SecurityResolutionTest extends ResolutionTest {

  override val basePath = "amf-cli/shared/src/test/resources/resolution/security/"

  multiGoldenTest("Security resolution raml to AMF", "security.raml.%s") { config =>
    cycle("security.raml", config.golden, Raml10YamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Security resolution oas to AMF", "security.json.%s") { config =>
    cycle("security.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
