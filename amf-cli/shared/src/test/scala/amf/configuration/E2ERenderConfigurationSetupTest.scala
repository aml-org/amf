package amf.configuration

import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.client.scala.model.domain.api.{AsyncApi, WebApi}
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.internal.remote.Mimes._
import amf.io.FileAssertionTest

import java.time.Instant

class E2ERenderConfigurationSetupTest extends ConfigurationSetupTest with FileAssertionTest {

  val usedMediaTypes = Seq(`application/ld+json`, `application/yaml`, `application/json`)
  val someUnusedMediaTypes = Seq(`application/raml+yaml`, `application/asyncapi`, `application/swagger+json`)

  case class ExpectedRenderCase(config: AMFConfiguration, model: BaseUnit, mediaTypeAndGolden: (String, String))
  case class ErrorRenderCase(config: AMFConfiguration, model: BaseUnit, mediaType: String)

  val renderCases: Seq[Any] = Seq(
    generateRenderCases(apiConfig, asyncModel, Seq((`application/ld+json`, "async-model.jsonld"))),
    generateRenderCases(apiConfig, webApiModel, Seq((`application/ld+json`, "webapi-model.jsonld"))),
    generateRenderCases(webApiConfig, asyncModel, Seq((`application/ld+json`, "async-model.jsonld"))),
    generateRenderCases(webApiConfig, webApiModel, Seq((`application/ld+json`, "webapi-model.jsonld"))),
    generateRenderCases(ramlConfig, webApiModel, Seq((`application/ld+json`, "webapi-model.jsonld"))),
    generateRenderCases(ramlConfig, asyncModel, Seq((`application/ld+json`, "async-model.jsonld"))),
    generateRenderCases(raml08Config, webApiModel, Seq((`application/ld+json`, "webapi-model.jsonld"), (`application/yaml`, "raml08-api.raml"))),
    generateRenderCases(raml10Config, webApiModel, Seq((`application/ld+json`, "webapi-model.jsonld"), (`application/yaml`, "raml10-api.raml"))),
    generateRenderCases(oasConfig, webApiModel, Seq((`application/ld+json`, "webapi-model.jsonld"))),
    generateRenderCases(oasConfig, asyncModel, Seq((`application/ld+json`, "async-model.jsonld"))),
    generateRenderCases(oas20Config, webApiModel, Seq((`application/ld+json`, "webapi-model.jsonld"), (`application/yaml`, "oas20-api.yaml"), (`application/json`, "oas20-api.json"))),
    generateRenderCases(oas30Config, webApiModel, Seq((`application/ld+json`, "webapi-model.jsonld"), (`application/yaml`, "oas30-api.yaml"), (`application/json`, "oas30-api.json"))),
  ).flatten

  renderCases.foreach {
    case e: ExpectedRenderCase =>
      val asyncOrWebApi = if (e.model.asInstanceOf[Document].encodes.isInstanceOf[WebApi]) "WebApi" else "AsyncApi"
      test(s"Test - config ${configNames(e.config)} with ${e.mediaTypeAndGolden._1} and ${asyncOrWebApi} renders as expected") {
        val client = e.config.baseUnitClient()
        val path = basePath + e.mediaTypeAndGolden._2
        val generated = client.render(e.model, e.mediaTypeAndGolden._1)
        writeTemporaryFile(path)(generated).flatMap(f => assertDifferences(f, path))
      }
    case e: ErrorRenderCase =>
      val client = e.config.baseUnitClient()
      val asyncOrWebApi = if (e.model.asInstanceOf[Document].encodes.isInstanceOf[WebApi]) "WebApi" else "AsyncApi"
      test(s"Test - config ${configNames(e.config)} with ${e.mediaType} and ${asyncOrWebApi} throws exception on render") {
        assertThrows[Exception] {
          client.render(e.model, e.mediaType)
        }
      }
  }

  def generateRenderCases(config: AMFConfiguration, model: BaseUnit, mediaTypeAndGoldens: Seq[(String, String)]): Seq[Any] = {
    val expectedCases = mediaTypeAndGoldens.map(tuple => ExpectedRenderCase(config, model, tuple))
    val allMediaTypes = someUnusedMediaTypes ++ usedMediaTypes
    val errorMediaTypes = allMediaTypes.diff(mediaTypeAndGoldens.map(_._1))
    val errorCases = errorMediaTypes.map(media => ErrorRenderCase(config, model, media))
    expectedCases ++ errorCases
  }

  def webApiModel: BaseUnit = {
    val api = WebApi()
      .withName("Example API")
      .withVersion("1.0")
      .withEndPoints(Seq.empty)
      .withId("api")
    Document().withId("root").withEncodes(api)
  }

  def asyncModel: BaseUnit = {
    val api = AsyncApi()
      .withName("Example API")
      .withVersion("1.0")
      .withEndPoints(Seq.empty)
      .withId("api")
    Document().withId("root").withEncodes(api)
  }
}
