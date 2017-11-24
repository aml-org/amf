package amf.client

import amf.common.Tests.checkDiff
import amf.framework.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

/**
  *
  */
class GeneratorTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test to oas stream dump") {
    val expected = new OasGenerator().generateString(unitBare)

    platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .map(content => content.stream.toString)
      .map(actual => checkDiff(actual -> expected))
  }

  test("test to raml stream dump") {
    val expected = new RamlGenerator().generateString(unitBare)

    platform
      .resolve("file://shared/src/test/resources/clients/bare.raml", None)
      .map(content => content.stream.toString)
      .map(actual => checkDiff(actual -> expected))
  }

  test("test to amf stream dump") {
    val expected = new AmfGenerator().generateString(unitBare)

    platform
      .resolve("file://shared/src/test/resources/clients/bare.jsonld", None)
      .map(content => content.stream.toString)
      .map(actual => checkDiff(actual -> expected))
  }

  test("test to stream dump complete") {
    val expected = new OasGenerator().generateString(unitAdvanced)

    platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .map(content => content.stream.toString)
      .map(actual => checkDiff(actual -> expected))
  }
}
