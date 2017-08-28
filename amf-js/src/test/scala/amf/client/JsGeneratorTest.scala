package amf.client

import amf.remote.Oas
import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers._

import scala.concurrent.ExecutionContext

/**
  *
  */
class JsGeneratorTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test to stream dump") {
    val futureResult = new Generator().generateStringAsync(unitBare, Oas).toFuture

    platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .map(content => content.stream.toString)
      .zip(futureResult)
      .map(t => {
        t._1 should be(t._2)
      })
  }

  test("test to stream dump complete") {
    val futureResult = new Generator().generateStringAsync(unitAdvanced, Oas).toFuture

    platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .map(content => content.stream.toString)
      .zip(futureResult)
      .map(t => {
        t._1 should be(t._2)
      })
  }

}
