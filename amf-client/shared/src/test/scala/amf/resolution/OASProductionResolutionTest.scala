package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.services.RuntimeResolver

class OASProductionResolutionTest extends ResolutionTest {
  override val basePath = "amf-client/shared/src/test/resources/production/"
  val completeCyclePath = "amf-client/shared/src/test/resources/upanddown/"

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    if (config.target.equals(Amf) && config.transformWith.isEmpty)
      RuntimeResolver.resolve(Vendor.OAS20.name, unit, TransformationPipeline.DEFAULT_PIPELINE, UnhandledErrorHandler)
    else super.transform(unit, config)
  }

  multiGoldenTest("OAS Response parameters resolution", "oas_response_declaration.resolved.%s") { config =>
    cycle("oas_response_declaration.yaml",
          config.golden,
          Oas20YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          directory = completeCyclePath)
  }

  multiGoldenTest("OAS with foward references in definitions", "oas_foward_definitions.resolved.%s") { config =>
    cycle("oas_foward_definitions.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          directory = completeCyclePath)
  }

  multiGoldenTest("OAS with external fragment reference in upper folder", "api.resolved.%s") { config =>
    cycle("master/master.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          directory = completeCyclePath + "oas-fragment-ref/")
  }

  multiGoldenTest("OAS complex example", "api.resolved.%s") { config =>
    cycle("spec/swagger.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          directory = basePath + "oas-complex-example/")
  }

  multiGoldenTest("OAS examples test", "oas-example.json.%s") { config =>
    cycle("oas-example.json", config.golden, Oas20JsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("OAS multiple examples test", "oas-multiple-example.json.%s") { config =>
    cycle("oas-multiple-example.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("OAS XML payload test", "oas20/xml-payload.json.%s") { config =>
    cycle("oas20/xml-payload.json",
          config.golden,
          Oas20YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Summary and description from path applied to operations",
                  "description-applied-to-operations-resolution.%s") { config =>
    cycle(
      "description-applied-to-operations.json",
      config.golden,
      Oas30JsonHint,
      target = Amf,
      renderOptions = Some(config.renderOptions),
      directory = completeCyclePath + "oas3/summary-description-in-path/",
      transformWith = Some(Oas30)
    )
  }
}
