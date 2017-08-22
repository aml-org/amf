package amf.client

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
class JvmClientTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  ignore("test from stream generation") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .flatMap(stream => {
        FutureConverters.toScala(new JvmClient().generateAsyncFromStream(stream.stream.toString, OasJsonHint))
      })
      .map(bu => {
        assertWebApi(bu.asInstanceOf[Document].encodes, webApiBare)
      })
  }

  ignore("test from file generation") {
    FutureConverters
      .toScala(
        new JvmClient()
          .generateAsyncFromFile("file://shared/src/test/resources/clients/bare.json", OasJsonHint))
      .map(bu => {
        assertWebApi(bu.asInstanceOf[Document].encodes, webApiBare)
      })
  }

  ignore("test from stream complete generation") {
    platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .flatMap(stream => {
        FutureConverters.toScala(new JvmClient().generateAsyncFromStream(stream.stream.toString, OasJsonHint))
      })
      .map(bu => {
        assertWebApi(bu.asInstanceOf[Document].encodes, webApiAdvanced)
      })
  }

  ignore("test from file complete generation") {
    FutureConverters
      .toScala(
        new JvmClient()
          .generateAsyncFromFile("file://shared/src/test/resources/clients/advanced.json", OasJsonHint))
      .map(bu => {
        assertWebApi(bu.asInstanceOf[Document].encodes, webApiAdvanced)
      })
  }

  def assertWebApi(actual: WebApi, expected: WebApi): Assertion = {
    actual should be(expected)
    succeed
  }

}
