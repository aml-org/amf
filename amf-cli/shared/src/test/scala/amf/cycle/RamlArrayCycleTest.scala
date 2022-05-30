package amf.cycle

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, Raml10YamlHint}
import amf.io.FunSuiteCycleTests

class RamlArrayCycleTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/parser/"
  val arrayExpression           = "array-type-expressions/"
  val unions                    = "union-expressions/"
  val unionsLib                 = "union-with-lib/"

  multiGoldenTest(
    "Type expression and explicit array must be parsed similarly",
    s"${arrayExpression}base-type-array.%s"
  ) { config =>
    cycle(
      s"${arrayExpression}base-type-array.raml",
      config.golden,
      Raml10YamlHint,
      target = AmfJsonHint,
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Array type expression with inheritance", s"${arrayExpression}type-expression-with-inheritance.%s") {
    config =>
      cycle(
        s"${arrayExpression}type-expression-with-inheritance.raml",
        config.golden,
        Raml10YamlHint,
        target = AmfJsonHint,
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest(
    "Union type expression is parsed similar to explicit union array",
    s"${arrayExpression}union-type-array.%s"
  ) { config =>
    cycle(
      s"${arrayExpression}union-type-array.raml",
      config.golden,
      Raml10YamlHint,
      target = AmfJsonHint,
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest(
    "Matrix type expression and explicit matrix must be parsed similarly",
    s"${arrayExpression}matrix-type-array.%s"
  ) { config =>
    cycle(
      s"${arrayExpression}matrix-type-array.raml",
      config.golden,
      Raml10YamlHint,
      target = AmfJsonHint,
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Unions don't lose right side declaration links", s"${unions}union-right-declaration.%s") { config =>
    cycle(
      s"${unions}union-right-declaration.raml",
      config.golden,
      Raml10YamlHint,
      target = AmfJsonHint,
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Unions with child declared elements have different ids", s"${unions}child-declaration-links.%s") {
    config =>
      cycle(
        s"${unions}child-declaration-links.raml",
        config.golden,
        Raml10YamlHint,
        target = AmfJsonHint,
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("Unions with types from lib", s"${unionsLib}api.%s") { config =>
    cycle(
      s"${unionsLib}api.raml",
      config.golden,
      Raml10YamlHint,
      target = AmfJsonHint,
      renderOptions = Some(config.renderOptions)
    )
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
