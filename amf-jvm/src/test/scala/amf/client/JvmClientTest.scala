package amf.client

import amf.common.AmfObjectTestMatcher
import amf.model.{Document, WebApi}
import amf.remote.OasJsonHint
import amf.unsafe.PlatformSecrets
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.ExecutionContext
import org.scalatest.Matchers._

import scala.compat.java8.FutureConverters

/**
  *
  */
class JvmClientTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest with AmfObjectTestMatcher {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test from stream generation") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .flatMap(stream => {
        FutureConverters.toScala(new JvmClient().generateAsyncFromStream(stream.stream.toString, OasJsonHint))
      })
      .map(bu => {
        AmfObjectMatcher(webApiBare.element).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  test("test from file generation") {
    FutureConverters
      .toScala(
        new JvmClient()
          .generateAsyncFromFile("file://shared/src/test/resources/clients/bare.json", OasJsonHint))
      .map(bu => {
        AmfObjectMatcher(webApiBare.element).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  test("test from stream complete generation") {
    platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .flatMap(stream => {
        FutureConverters.toScala(new JvmClient().generateAsyncFromStream(stream.stream.toString, OasJsonHint))
      })
      .map(bu => {
        AmfObjectMatcher(webApiAdvanced.element).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  test("test from file complete generation") {
    FutureConverters
      .toScala(
        new JvmClient()
          .generateAsyncFromFile("file://shared/src/test/resources/clients/advanced.json", OasJsonHint))
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
