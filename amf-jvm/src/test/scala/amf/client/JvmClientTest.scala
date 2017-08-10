package amf.client

import amf.model.{Document, WebApi}
import amf.remote.OasJsonHint
import amf.unsafe.PlatformSecrets
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.ExecutionContext
import org.scalatest.Matchers._

/**
  *
  */
class JvmClientTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test from stream generation") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .flatMap(stream => {
        val eventualUnit = new JvmClient().generateAsyncFromStream(stream.stream.toString, OasJsonHint)
        eventualUnit
      })
      .map(bu => {
        assertWebApi(bu.asInstanceOf[Document].encodes, webApiBare)
      })
  }

  test("test from file generation") {
    new JvmClient()
      .generateAsyncFromFile("file://shared/src/test/resources/clients/bare.json", OasJsonHint)
      .map(bu => {
        assertWebApi(bu.asInstanceOf[Document].encodes, webApiBare)
      })
  }

  test("test from stream complete generation") {
    platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .flatMap(stream => {
        new JvmClient().generateAsyncFromStream(stream.stream.toString, OasJsonHint)
      })
      .map(bu => {
        assertWebApi(bu.asInstanceOf[Document].encodes, webApiAdvanced)
      })
  }

  test("test from file complete generation") {
    new JvmClient()
      .generateAsyncFromFile("file://shared/src/test/resources/clients/advanced.json", OasJsonHint)
      .map(bu => {
        assertWebApi(bu.asInstanceOf[Document].encodes, webApiAdvanced)
      })
  }

  def assertWebApi(actual: WebApi, expected: WebApi): Assertion = {
    actual should be(expected)
    succeed
  }

}
