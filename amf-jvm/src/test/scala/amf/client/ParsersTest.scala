package amf.client

import amf.common.AmfObjectTestMatcher
import amf.model.{Document, WebApi}
import amf.unsafe.PlatformSecrets
import org.scalatest.Matchers._
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.compat.java8.FutureConverters
import scala.concurrent.ExecutionContext

/**
  *
  */
class ParsersTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest with AmfObjectTestMatcher {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test from stream generation oas") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .flatMap(stream => {
        FutureConverters.toScala(new OasParser().parseStringAsync(stream.stream.toString))
      })
      .map(bu => {
        AmfObjectMatcher(webApiBare.element).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  test("test from stream generation raml") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.raml", None)
      .flatMap(stream => {
        FutureConverters.toScala(new RamlParser().parseStringAsync(stream.stream.toString))
      })
      .map(bu => {
        AmfObjectMatcher(webApiBare.element.withBasePath("/api")).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  test("test from stream generation amf") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.jsonld", None)
      .flatMap(stream => {
        FutureConverters.toScala(new AmfParser().parseStringAsync(stream.stream.toString))
      })
      .map(bu => {
        AmfObjectMatcher(webApiBare.element).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  test("test from file generation") {
    FutureConverters
      .toScala(
        new OasParser()
          .parseFileAsync("file://shared/src/test/resources/clients/bare.json"))
      .map(bu => {
        AmfObjectMatcher(webApiBare.element).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  test("test from stream complete generation") {
    platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .flatMap(stream => {
        FutureConverters.toScala(new OasParser().parseStringAsync(stream.stream.toString))
      })
      .map(bu => {
        AmfObjectMatcher(webApiAdvanced.element).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  test("test from file complete generation") {
    FutureConverters
      .toScala(
        new OasParser()
          .parseFileAsync("file://shared/src/test/resources/clients/advanced.json"))
      .map(bu => {
        AmfObjectMatcher(webApiAdvanced.element).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  def assertWebApi(actual: WebApi, expected: WebApi): Assertion = {
    actual should be(expected)
    succeed
  }

}
