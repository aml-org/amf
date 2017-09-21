package amf.client

import amf.common.Tests
import amf.common.Tests.checkDiff
import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers._

import scala.concurrent.ExecutionContext

/**
  *
  */
class GeneratorTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test to oas stream dump") {
    val futureResult = new OasGenerator().generateString(unitBare).toFuture

    platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .map(content => content.stream.toString)
      .zip(futureResult)
      .map(checkDiff)
  }

  test("test to raml stream dump") {
    val futureResult = new RamlGenerator().generateString(unitBare).toFuture

    platform
      .resolve("file://shared/src/test/resources/clients/bare.raml", None)
      .map(content => content.stream.toString)
      .zip(futureResult)
      .map(checkDiff)
  }

  test("test to amf stream dump") {
    val futureResult = new AmfGenerator().generateString(unitBare).toFuture

    platform
      .resolve("file://shared/src/test/resources/clients/bare.jsonld", None)
      .map(content => content.stream.toString)
      .zip(futureResult)
      .map(checkDiff)
  }

  test("test to stream dump complete") {
    val futureResult = new OasGenerator().generateString(unitAdvanced).toFuture

    platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .map(content => content.stream.toString)
      .zip(futureResult)
      .map(checkDiff)
  }

}
