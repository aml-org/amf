package amf.client

import amf.common.Tests._
import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite

import scala.compat.java8.FutureConverters
import scala.concurrent.ExecutionContext

/**
  *
  */
class GeneratorTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Test oas generator string-async") {
    val expected = platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .map(content => content.stream.toString)

    FutureConverters
      .toScala(new OasGenerator().generateString(unitBare))
      .zip(expected)
      .map(checkDiff)
  }

  test("Test raml generator string-async") {
    val expected = platform
      .resolve("file://shared/src/test/resources/clients/bare.raml", None)
      .map(content => content.stream.toString)

    FutureConverters
      .toScala(new RamlGenerator().generateString(unitBare))
      .zip(expected)
      .map(checkDiff)
  }

  test("Test amf generator string-async") {
    val expected = platform
      .resolve("file://shared/src/test/resources/clients/bare.jsonld", None)
      .map(content => content.stream.toString)

    FutureConverters
      .toScala(new AmfGenerator().generateString(unitBare))
      .zip(expected)
      .map(checkDiff)
  }

  test("Test oas generator string-async (complete spec)") {
    val expected = platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .map(content => content.stream.toString)

    FutureConverters
      .toScala(new OasGenerator().generateString(unitAdvanced))
      .zip(expected)
      .map(checkDiff)
  }
}
