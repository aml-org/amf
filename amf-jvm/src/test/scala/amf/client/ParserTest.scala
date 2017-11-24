package amf.client

import amf.ProfileNames
import amf.common.AmfObjectTestMatcher
import amf.core.client.GenerationOptions
import amf.model.{Document, Module, WebApi}
import amf.framework.unsafe.PlatformSecrets
import org.scalatest.Matchers._
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.compat.java8.FutureConverters
import scala.concurrent.ExecutionContext

/**
  *
  */
class ParserTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest with AmfObjectTestMatcher {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test from stream generation oas") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .flatMap(stream => {
        FutureConverters.toScala(new OasParser().parseStringAsync(stream.stream.toString))
      }) map {
      case d: Document =>
        AmfObjectMatcher(webApiBare.element).assert(d.encodes.element)
        succeed
    }
  }

  test("test from stream generation raml") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.raml", None)
      .flatMap(stream => {
        FutureConverters.toScala(new RamlParser().parseStringAsync(stream.stream.toString))
      }) map {
      case d: Document =>
        AmfObjectMatcher(webApiBare.element.withBasePath("/api")).assert(d.encodes.element)
        succeed
    }
  }

  test("test from stream generation amf") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.jsonld", None)
      .flatMap(stream => {
        FutureConverters.toScala(new AmfParser().parseStringAsync(stream.stream.toString))
      } map {
        case d: Document =>
          AmfObjectMatcher(webApiBare.element).assert(d.encodes.element)
          succeed
      })
  }

  test("test from file generation") {
    FutureConverters
      .toScala(
        new OasParser()
          .parseFileAsync("file://shared/src/test/resources/clients/bare.json")) map {
      case d: Document =>
        AmfObjectMatcher(webApiBare.element).assert(d.encodes.element)
        succeed
    }
  }

  test("test from stream complete generation") {
    platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .flatMap(stream => {
        FutureConverters.toScala(new OasParser().parseStringAsync(stream.stream.toString))
      }) map {
      case d: Document =>
        AmfObjectMatcher(webApiAdvanced.element).assert(d.encodes.element)
        succeed
    }
  }

  test("test from file complete generation") {
    FutureConverters
      .toScala(
        new OasParser()
          .parseFileAsync("file://shared/src/test/resources/clients/advanced.json")) map {
      case d: Document =>
        AmfObjectMatcher(webApiAdvanced.element).assert(d.encodes.element)
        succeed
    }
  }

  test("test from library file complete generation") {
    FutureConverters
      .toScala(
        new RamlParser()
          .parseFileAsync("file://shared/src/test/resources/clients/libraries.raml")) map {
      case d: Document =>
        d.references.get(0) match {
          case m: Module =>
            AmfObjectMatcher(moduleBare.model).assert(m.model)
            succeed
          case _ => fail("unexpected type")
        }
    }
  }

  test("Validation model interface") {
    val examplesPath = "file://shared/src/test/resources/validations/"
    val parser       = new RamlParser()
    val unit         = parser.parseFileAsync(examplesPath + "library/nested.raml").get()
    val report       = parser.reportValidation(ProfileNames.RAML).get()
    assert(!report.conforms)
    assert(report.results.length == 1)
  }

  test("Custom validation model interface") {
    val examplesPath = "file://shared/src/test/resources/validations/"
    val parser       = new RamlParser()
    val unit         = parser.parseFileAsync(examplesPath + "banking/api.raml").get()
    val report       = parser.reportCustomValidation("Banking", examplesPath + "banking/profile.raml").get()
    assert(!report.conforms)
    assert(report.results.nonEmpty)

  }

  test("Dialects regeneration tests") {
    platform.dialectsRegistry.registerDialect("file://shared/src/test/resources/vocabularies/amc2/dialect.raml") map {
      parsedDialect =>
        new RamlParser()
          .parseFileAsync("file://shared/src/test/resources/vocabularies/amc2/example.raml", platform)
          .get
    } map { model =>
      new AmfGenerator().generateString(model, GenerationOptions())
    } map { jsonld =>
      new AmfParser().parseStringAsync(jsonld, platform).get
    } map { parsedModel =>
      new RamlGenerator().generateString(parsedModel)
    } map { generatedRaml =>
      val expected =
        """#%Mule Agent Configuration 0.1
          |transports:
          |  websocket.transport:
          |    security:
          |      keyStorePassword: exampleNs/123
          |      keyStoreAlias: agent
          |      keyStoreAliasPassword: exampleNs/124
          |services:
          |  mule.agent.jmx.publisher.service: !include reusable-jmx-publisher.raml
          |""".stripMargin
      assert(generatedRaml == expected)
    }

  }

  test("Dialects regeneration tests with external JSON-LD") {
    platform.dialectsRegistry.registerDialect("file://shared/src/test/resources/vocabularies/amc2/dialect.raml") map {
      parsedDialect =>
        new RamlParser()
          .parseFileAsync("file://shared/src/test/resources/vocabularies/amc2/example.raml", platform)
          .get
    } map { _ =>
      val externalJsonld =
        "[{\"@id\":\"file:///tmp/amc/example.raml\",\"http://raml.org/vocabularies/document#references\":[{\"@id\":\"file:///tmp/amc/reusable-jmx-publisher.raml\",\"http://raml.org/vocabularies/document#encodes\":[{\"@id\":\"file:///tmp/amc/reusable-jmx-publisher.raml#\",\"http://mulesoft.com/vocabularies/mule-runtime#timeUnit\":[{\"@value\":\"SECONDS\"}],\"http://mulesoft.com/vocabularies/mule-runtime#frequency\":[{\"@value\":100}],\"http://mulesoft.com/vocabularies/mule-runtime#enabledComponent\":[{\"@value\":true}],\"http://mulesoft.com/vocabularies/mule-runtime#componentId\":[{\"@value\":\"mule.agent.jmx.publisher.service\"}],\"@type\":[\"http://mulesoft.com/vocabularies/mule-runtime#JmxPublisherService\"]}],\"@type\":[\"http://raml.org/vocabularies/document#Unit\",\"http://raml.org/vocabularies/document#Fragment\",\"http://raml.org/vocabularies/document#DialectNode\"]}],\"http://raml.org/vocabularies/document#encodes\":[{\"@id\":\"file:///tmp/amc/example.raml#\",\"http://mulesoft.com/vocabularies/mule-runtime#transport\":[{\"@id\":\"file:///tmp/amc/example.raml#websocket.transport\",\"http://mulesoft.com/vocabularies/mule-runtime#keystore\":[{\"@id\":\"file:///tmp/amc/example.raml#websocket.transport/security\",\"http://mulesoft.com/vocabularies/mule-runtime#keyStorePassword\":[{\"@value\":\"exampleNs/123\"}],\"http://mulesoft.com/vocabularies/mule-runtime#keyAliasPassword\":[{\"@value\":\"exampleNs/124\"}],\"http://mulesoft.com/vocabularies/mule-runtime#keyAlias\":[{\"@value\":\"agent\"}],\"@type\":[\"http://mulesoft.com/vocabularies/mule-runtime#KeyStore\"]}],\"http://mulesoft.com/vocabularies/mule-runtime#componentId\":[{\"@value\":\"websocket.transport\"}],\"@type\":[\"http://mulesoft.com/vocabularies/mule-runtime#WebSocketsTransport\"]}],\"http://mulesoft.com/vocabularies/mule-runtime#supportsService\":[{\"@id\":\"file:///tmp/amc/example.raml#mule.agent.jmx.publisher.service\",\"http://mulesoft.com/vocabularies/mule-runtime#timeUnit\":[{\"@value\":\"SECONDS\"}],\"http://mulesoft.com/vocabularies/mule-runtime#frequency\":[{\"@value\":100}],\"http://mulesoft.com/vocabularies/mule-runtime#enabledComponent\":[{\"@value\":true}],\"http://mulesoft.com/vocabularies/mule-runtime#componentId\":[{\"@value\":\"mule.agent.jmx.publisher.service\"}],\"@type\":[\"http://mulesoft.com/vocabularies/mule-runtime#JmxPublisherService\"]}],\"@type\":[\"http://mulesoft.com/vocabularies/mule-runtime#Agent\"]}],\"@type\":[\"http://raml.org/vocabularies/document#Unit\",\"http://raml.org/vocabularies/document#Module\",\"http://raml.org/vocabularies/document#Fragment\",\"http://raml.org/vocabularies/document#Document\"]}]"
      new AmfParser().parseStringAsync(externalJsonld, platform).get
    } map { parsedModel =>
      new RamlGenerator().generateString(parsedModel)
    } map { generatedRaml =>
      val expected =
        """#%Mule Agent Configuration 0.1
          |transports:
          |  websocket.transport:
          |    security:
          |      keyStorePassword: exampleNs/123
          |      keyStoreAlias: agent
          |      keyStoreAliasPassword: exampleNs/124
          |services:
          |  mule.agent.jmx.publisher.service:
          |    enabled: true
          |    frequency: 100
          |    frequencyTimeUnit: SECONDS
          |""".stripMargin
      assert(generatedRaml == expected)
    }
  }

  /*
  test("Command line test") {
    val args = Array(
      "validate",
      "-in",
      "RAML",
      "-ds",
      "file:///Users/antoniogarrote/Development/vocabularies/k8/dialects/pod.raml",
      "file:///Users/antoniogarrote/Development/vocabularies/k8/examples/pod.raml"
    )
    Main.main(args)
    assert(true)
  }
   */

  def assertModule(actual: Module, expected: Module): Assertion = {
    actual should be(expected)
    succeed
  }

  def assertWebApi(actual: WebApi, expected: WebApi): Assertion = {
    actual should be(expected)
    succeed
  }

}
