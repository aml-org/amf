package amf.resolution

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{Amf, Raml10, Raml10YamlHint}

/**
  *
  */
class ExternalSourceResolutionTest extends ResolutionTest {
  override val basePath = "amf-cli/shared/src/test/resources/resolution/externalfragment/"

  multiGoldenTest("Xml schema raml to amf", "xmlschema.raml.%s") { config =>
    cycle("xmlschema.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("Json schema raml to amf", "jsonschema.raml.%s") { config =>
    cycle("jsonschema.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("Xml example raml to amf", "xmlexample.raml.%s") { config =>
    cycle("xmlexample.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("Json example raml to amf", "jsonexample.raml.%s") { config =>
    cycle("jsonexample.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
