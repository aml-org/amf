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

  test("test to file dump") {
    val futureResult = FutureConverters.toScala(
      new JvmGenerator()
        .generateToFileAsync(unitBare, "file://amf-jvm/target/test-output/jvm-generator-test/test.json", Oas))

    futureResult.flatMap(r => {
      val expected = platform
        .resolve("file://shared/src/test/resources/clients/bare.json", None)

      val actual = platform
        .resolve("file://amf-jvm/target/test-output/jvm-generator-test/test.json", None)
      expected
        .zip(actual)
        .map(t => {
          t._1.stream.toString should be(t._2.stream.toString)
        })
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
