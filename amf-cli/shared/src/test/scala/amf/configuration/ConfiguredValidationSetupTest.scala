package amf.configuration

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.common.validation.{Async20Profile, Oas20Profile, Oas30Profile, Raml08Profile, Raml10Profile}
import amf.core.client.scala.model.document.Document

import scala.concurrent.ExecutionContext

class ConfiguredValidationSetupTest extends ConfigurationSetupTest {

  implicit override val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val model = Document()
    .withId("someId")
    .withEncodes(
      WebApi()
        .withName("something")
    )

  test("Model without source spec can be validated with RAML 1.0 config") {
    raml10Config.baseUnitClient().validate(model).map { report =>
      report.profile shouldEqual Raml10Profile
    }
  }

  test("Model without source spec can be validated with RAML 0.8 config") {
    raml08Config.baseUnitClient().validate(model).map { report =>
      report.profile shouldEqual Raml08Profile
    }
  }

  test("Model without source spec can be validated with OAS 2.0 config") {
    oas20Config.baseUnitClient().validate(model).map { report =>
      report.profile shouldEqual Oas20Profile
    }
  }

  test("Model without source spec can be validated with OAS 3.0 config") {
    oas30Config.baseUnitClient().validate(model).map { report =>
      report.profile shouldEqual Oas30Profile
    }
  }

  test("Model without source spec can be validated with ASYNC 2.0 config") {
    async20Config.baseUnitClient().validate(model).map { report =>
      report.profile shouldEqual Async20Profile
    }
  }
}
