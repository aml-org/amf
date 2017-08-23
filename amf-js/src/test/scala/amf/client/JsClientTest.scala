package amf.client

import amf.common.AmfObjectTestMatcher
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
class JsClientTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest with AmfObjectTestMatcher {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  ignore("test from stream generation") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .flatMap(stream => {
        val eventualUnit = new JsClient().generateAsyncFromStream(stream.stream.toString, OasJsonHint)
        eventualUnit.toFuture
      })
      .map(bu => {
        AmfObjectMatcher(webApiBare.element).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  ignore("test from file generation") {
    new JsClient()
      .generateAsyncFromFile("file://shared/src/test/resources/clients/bare.json", OasJsonHint)
      .toFuture
      .map(bu => {
        AmfObjectMatcher(webApiBare.element).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  ignore("test from stream complete generation") {
    platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .flatMap(stream => {
        val value1: Promise[BaseUnit] = new JsClient().generateAsyncFromStream(stream.stream.toString, OasJsonHint)
        value1.toFuture
      })
      .map(bu => {
        AmfObjectMatcher(webApiAdvanced.element).assert(bu.asInstanceOf[Document].encodes.element)
        succeed
      })
  }

  ignore("test from file complete generation") {
    new JsClient()
      .generateAsyncFromFile("file://shared/src/test/resources/clients/advanced.json", OasJsonHint)
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
