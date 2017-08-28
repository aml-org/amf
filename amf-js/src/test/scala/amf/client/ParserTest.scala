package amf.client

import amf.common.AmfObjectTestMatcher
import amf.model.{BaseUnit, Document, WebApi}
import amf.unsafe.PlatformSecrets
import org.scalatest.Matchers._
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.ExecutionContext
import scala.scalajs.js.Promise

/**
  *
  */
class ParserTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest with AmfObjectTestMatcher {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test from stream generation oas") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .flatMap(stream => {
        val eventualUnit = new OasParser().parseStringAsync(stream.stream.toString)
        eventualUnit.toFuture
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
        val eventualUnit = new RamlParser().parseStringAsync(stream.stream.toString)
        eventualUnit.toFuture
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
        val eventualUnit = new AmfParser().parseStringAsync(stream.stream.toString)
        eventualUnit.toFuture
      })
      .map(bu => {
        AmfObjectMatcher(webApiBare.element).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  test("test from file generation") {
    new OasParser()
      .parseFileAsync("file://shared/src/test/resources/clients/bare.json")
      .toFuture
      .map(bu => {
        AmfObjectMatcher(webApiBare.element).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  test("test from stream complete generation") {
    platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .flatMap(stream => {
        val value1: Promise[BaseUnit] = new OasParser().parseStringAsync(stream.stream.toString)
        value1.toFuture
      })
      .map(bu => {
        AmfObjectMatcher(webApiAdvanced.element).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  test("test from file complete generation") {
    new OasParser()
      .parseFileAsync("file://shared/src/test/resources/clients/advanced.json")
      .toFuture
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
