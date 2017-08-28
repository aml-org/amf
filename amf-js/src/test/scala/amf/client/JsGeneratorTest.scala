package amf.client

import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers._

import scala.concurrent.ExecutionContext

/**
  *
  */
class JsGeneratorTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test to oas stream dump") {
    val futureResult = new OasGenerator().generateStringAsync(unitBare).toFuture

    platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .map(content => content.stream.toString)
      .zip(futureResult)
      .map(t => {
        t._1 should be(t._2)
      })
  }

  test("test to raml stream dump") {
    val futureResult = new RamlGenerator().generateStringAsync(unitBare).toFuture

    platform
      .resolve("file://shared/src/test/resources/clients/bare.raml", None)
      .map(content => content.stream.toString)
      .zip(futureResult)
      .map(t => {
        t._1 should be(t._2)
      })
  }

  test("test to amf stream dump") {
    val futureResult = new AmfGenerator().generateStringAsync(unitBare).toFuture

    platform
      .resolve("file://shared/src/test/resources/clients/bare.jsonld", None)
      .map(content => content.stream.toString)
      .zip(futureResult)
      .map(t => {
        t._1 should be(t._2)
      })
  }

  test("test to stream dump complete") {
    val futureResult = new OasGenerator().generateStringAsync(unitAdvanced).toFuture

    platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .map(content => content.stream.toString)
      .zip(futureResult)
      .map(t => {
        t._1 should be(t._2)
      })
  }

}
