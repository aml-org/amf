package amf.parser

import amf.apicontract.client.scala.RAMLConfiguration
import amf.apicontract.client.scala.model.domain.Parameter
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.domain.{RecursiveShape, Shape}
import amf.core.internal.remote.{Amf, AmfJsonHint, Raml10YamlHint}
import amf.io.FunSuiteCycleTests
import amf.shapes.client.scala.model.domain.ScalarShape
import org.scalatest.Matchers

class Raml10ParserTest extends FunSuiteCycleTests with Matchers {

  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/raml10/"

  multiGoldenTest("Type with json schema in it's type facet has an inheritance to it",
                  "type-with-json-schema-in-type-facet.%s") { config =>
    cycle(
      "type-with-json-schema-in-type-facet.raml",
      config.golden,
      Raml10YamlHint,
      AmfJsonHint,
      renderOptions = Some(config.renderOptions.withSourceMaps.withPrettyPrint)
    )
  }

  multiGoldenTest("Nillable types in params are parsed", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      AmfJsonHint,
      renderOptions = Some(config.renderOptions.withSourceMaps.withPrettyPrint),
      directory = s"${basePath}nillable-type-in-parameter/",
      eh = Some(UnhandledErrorHandler)
    )
  }

  test("Cloned parameters shouldn't link to recursive shapes") {
    val client  = RAMLConfiguration.RAML10().baseUnitClient()
    val apiPath = s"file://${basePath}cloned-param-schema-isnt-recursive-shape/api.raml"
    client.parseDocument(apiPath).map { result =>
      val shapes = result.document.iterator().toList.collect { case shape: Shape => shape }.flatMap(_.inherits)
      all(shapes) should (not be a[RecursiveShape])
      shapes.size shouldBe 5
    }
  }
}
