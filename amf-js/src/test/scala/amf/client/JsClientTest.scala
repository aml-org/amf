package amf.client

import amf.model.{BaseUnit, Document, WebApi}
import amf.remote.OasJsonHint
import amf.unsafe.PlatformSecrets
import org.scalatest.Matchers._
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.ExecutionContext
import scala.scalajs.js.Promise

/**
  *
  */
class JsClientTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test from stream generation") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .flatMap(stream => {
        val eventualUnit = new JsClient().generateAsyncFromStream(stream.stream.toString, OasJsonHint)
        eventualUnit.toFuture
      })
      .map(bu => {
        assertWebApi(bu.asInstanceOf[Document].encodes, webApiBare)
      })
  }

  test("test from file generation") {
    new JsClient()
      .generateAsyncFromFile("file://shared/src/test/resources/clients/bare.json", OasJsonHint)
      .toFuture
      .map(bu => {
        assertWebApi(bu.asInstanceOf[Document].encodes, webApiBare)
      })
  }

  test("test from stream complete generation") {
    platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .flatMap(stream => {
        val value1: Promise[BaseUnit] = new JsClient().generateAsyncFromStream(stream.stream.toString, OasJsonHint)
        value1.toFuture
      })
      .map(bu => {
        assertWebApi(bu.asInstanceOf[Document].encodes, webApiAdvanced)
      })
  }

  test("test from file complete generation") {
    new JsClient()
      .generateAsyncFromFile("file://shared/src/test/resources/clients/advanced.json", OasJsonHint)
      .toFuture
      .map(bu => {
        assertWebApi(bu.asInstanceOf[Document].encodes, webApiAdvanced)
      })
  }

  def assertWebApi(actual: WebApi, expected: WebApi): Assertion = {
    actual should be(expected)
    succeed
  }

}
