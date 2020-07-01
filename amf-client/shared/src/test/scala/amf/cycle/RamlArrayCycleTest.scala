package amf.cycle

import amf.core.emitter.RenderOptions
import amf.core.remote.{Amf, RamlYamlHint}
import amf.io.{FunSuiteCycleTests, MultiJsonldAsyncFunSuite}

class RamlArrayCycleTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-client/shared/src/test/resources/parser/array-type-expressions/"

  multiGoldenTest("Type expression and explicit array must be parsed similarly", "base-type-array.%s") { config =>
    cycle("base-type-array.raml", config.golden, RamlYamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Array type expression with inheritance", "type-expression-with-inheritance.%s") { config =>
    cycle("type-expression-with-inheritance.raml",
          config.golden,
          RamlYamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Union type expression is parsed similar to explicit union array", "union-type-array.%s") { config =>
    cycle("union-type-array.raml",
          config.golden,
          RamlYamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Matrix type expression and explicit matrix must be parsed similarly", "matrix-type-array.%s") {
    config =>
      cycle("matrix-type-array.raml",
            config.golden,
            RamlYamlHint,
            target = Amf,
            renderOptions = Some(config.renderOptions))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps
}
