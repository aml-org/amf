package amf.client

import amf.common.Tests._
import amf.framework.unsafe.PlatformSecrets
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

    expected.map(e => checkDiff(new OasGenerator().generateString(unitBare) -> e))
  }

  test("Test raml generator string-async") {
    val expected = platform
      .resolve("file://shared/src/test/resources/clients/bare.raml", None)
      .map(content => content.stream.toString)

    expected.map(e => checkDiff(new RamlGenerator().generateString(unitBare) -> e))
  }

  test("Test amf generator string-async") {
    val expected = platform
      .resolve("file://shared/src/test/resources/clients/bare.jsonld", None)
      .map(content => content.stream.toString)

    expected.map(e => checkDiff(new AmfGenerator().generateString(unitBare) -> e))
  }

  test("Test oas generator string-async (complete spec)") {
    val expected = platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .map(content => content.stream.toString)

    expected.map(e => checkDiff(new OasGenerator().generateString(unitAdvanced) -> e))
  }
}
