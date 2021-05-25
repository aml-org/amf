package amf.resolution

import amf.client.remod.amfcore.config.RenderOptions
import amf.core.remote.{Amf, Raml10YamlHint}

/**
  *
  */
class ExternalSourceResolutionTest extends ResolutionTest {
  override val basePath = "amf-client/shared/src/test/resources/resolution/externalfragment/"

  multiGoldenTest("Xml schema raml to amf", "xmlschema.raml.%s") { config =>
    cycle("xmlschema.raml", config.golden, Raml10YamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Json schema raml to amf", "jsonschema.raml.%s") { config =>
    cycle("jsonschema.raml", config.golden, Raml10YamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Xml example raml to amf", "xmlexample.raml.%s") { config =>
    cycle("xmlexample.raml", config.golden, Raml10YamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Json example raml to amf", "jsonexample.raml.%s") { config =>
    cycle("jsonexample.raml", config.golden, Raml10YamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
