package amf.resolution

import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote.{Amf, Raml08, RamlYamlHint}

class Raml08ResolutionTest extends RamlResolutionTest {
  override val basePath: String =
    "amf-client/shared/src/test/resources/resolution/08/"
  val productionPath: String =
    "amf-client/shared/src/test/resources/production/"

  test("Resolve WebForm 08 Types test") {
    cycle("mincount-webform-types.raml", "mincount-webform-types.resolved.raml", RamlYamlHint, Raml08)
  }

  test("Resolve Min and Max in header 08 test") {
    cycle("min-max-in-header.raml", "min-max-in-header.resolved.raml", RamlYamlHint, Raml08)
  }

  test("Test failing with exception") {
    recoverToExceptionIf[Exception] {
      cycle("wrong-key.raml", "wrong-key.raml", RamlYamlHint, Raml08, eh = Some(UnhandledParserErrorHandler))
    }.map { ex =>
      assert(ex.getMessage.contains(s"Message: Property 'errorKey' not supported in a ${Raml08.name} webApi node"))
    }
  }

  test("Test empty trait in operations") {
    cycle("empty-is-operation-endpoint.raml", "empty-is-operation-endpoint.raml.raml", RamlYamlHint, Raml08)
  }

  test("Test included schema") {
    cycle("api.raml", "api.raml.raml", RamlYamlHint, Raml08, basePath + "included-schema/")
  }

  multiGoldenTest("Test included schema and example", "api.%s") { config =>
    cycle("api.raml",
          config.golden,
          RamlYamlHint,
          target = Amf,
          directory = basePath + "included-schema-and-example/",
          renderOptions = Some(config.renderOptions))
  }

  test("Test json_schemasa refs") {
    cycle("json_schemas.raml", "json_schemas.resolved.raml", RamlYamlHint, Raml08)
  }
}
