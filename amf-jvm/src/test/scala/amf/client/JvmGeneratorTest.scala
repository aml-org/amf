package amf.client

import amf.remote.Oas
import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext
import org.scalatest.Matchers._

import scala.compat.java8.FutureConverters

/**
  *
  */
class JvmGeneratorTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test to stream dump") {
    val futureResult = FutureConverters.toScala(new JvmGenerator().generateToStringAsync(unitBare, Oas))

    platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .map(content => content.stream.toString)
      .zip(futureResult)
      .map(t => {
        t._1 should be(t._2)
      })
  }

  test("test to stream dump complete") {
    val futureResult = FutureConverters.toScala(new JvmGenerator().generateToStringAsync(unitAdvanced, Oas))

    platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .map(content => content.stream.toString)
      .zip(futureResult)
      .map(t => {
        t._1 should be(t._2)
      })
  }

}
