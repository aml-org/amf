package amf.client

import amf.common.Tests.checkDiff
import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite

import scala.compat.java8.FutureConverters
import scala.concurrent.ExecutionContext

/**
  *
  */
class GeneratorTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test to oas stream dump") {
    val expected = platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .map(content => content.stream.toString)

    FutureConverters
      .toScala(new OasGenerator().generateStringAsync(unitBare))
      .zip(expected)
      .map(checkDiff)
  }

  test("test to raml stream dump") {
    val expected = platform
      .resolve("file://shared/src/test/resources/clients/bare.raml", None)
      .map(content => content.stream.toString)

    FutureConverters
      .toScala(new RamlGenerator().generateStringAsync(unitBare))
      .zip(expected)
      .map(checkDiff)
  }

  test("test to amf stream dump") {
    val expected = platform
      .resolve("file://shared/src/test/resources/clients/bare.jsonld", None)
      .map(content => content.stream.toString)

    FutureConverters
      .toScala(new AmfGenerator().generateStringAsync(unitBare))
      .zip(expected)
      .map(checkDiff)
  }

//  test("test to file dump") {
//    val futureResult = FutureConverters.toScala(
//      new OasGenerator()
//        .generateFileAsync(unitBare, "file://amf-jvm/target/test-output/jvm-generator-test/test.json"))
//
//    futureResult.flatMap(r => {
//      val expected = platform
//        .resolve("file://shared/src/test/resources/clients/bare.json", None)
//
//      val actual = platform
//        .resolve("file://amf-jvm/target/test-output/jvm-generator-test/test.json", None)
//      expected
//        .zip(actual)
//        .map(t => {
//          t._1.stream.toString should be(t._2.stream.toString)
//        })
//    })
//  }

  test("test to stream dump complete") {
    val expected = platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .map(content => content.stream.toString)

    FutureConverters
      .toScala(new OasGenerator().generateStringAsync(unitAdvanced))
      .zip(expected)
      .map(checkDiff)
  }
}
