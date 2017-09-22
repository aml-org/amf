package amf.validation
import amf.common.Tests.checkDiff

import amf.client.GenerationOptions
import amf.dumper.AMFDumper
import amf.remote.Syntax.Yaml
import amf.remote.Raml
import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

class ValidationTest extends AsyncFunSuite with PlatformSecrets  {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath="file://shared/src/test/resources/vocabularies/"

  test("WOA Loading and serializing validations") {
    val validation = Validation(platform)
    val expected = platform.resolve(basePath + "validation_profile_example_gold.raml", None).map(_.stream.toString)
    val actual = validation.loadValidationDialect(basePath + "validation_dialect_fixed.raml")
    .flatMap(unit =>
        validation.loadValidationProfile(basePath + "validation_profile_example.raml"));
    actual.flatMap({ unit =>
      AMFDumper(unit, Raml, Yaml, GenerationOptions()).dumpToString
    }).zip(expected)
      .map(checkDiff)
  }


  test("Loading and serializing validations with inplace definition of encodes") {
    val validation = Validation(platform)
    val expected = platform.resolve(basePath + "validation_profile_example_gold.raml", None).map(_.stream.toString)
    val actual = validation.loadValidationDialect(basePath + "validation_dialect_fixed2.raml")
      .flatMap(unit =>
        validation.loadValidationProfile(basePath + "validation_profile_example.raml"));
    actual.flatMap(unit=>new AMFDumper(unit, Raml, Yaml, GenerationOptions()).dumpToString)
      .zip(expected)
      .map(checkDiff)
    assert(true)
  }

  test("Loading and serializing validations with inplace definition of range") {
    val validation = Validation(platform)
    val expected = platform.resolve(basePath + "validation_profile_example_gold.raml", None).map(_.stream.toString)
    val actual = validation.loadValidationDialect(basePath + "validation_dialect_fixed3.raml")
      .flatMap(unit =>
        validation.loadValidationProfile(basePath + "validation_profile_example.raml"));
    actual.flatMap(unit=>new AMFDumper(unit, Raml, Yaml, GenerationOptions()).dumpToString)
      .zip(expected)
      .map(checkDiff)
    assert(true)
  }

  test("Load dialect") {

    val validation = Validation(platform)
    try {
      for {
        _ <- validation.loadValidationDialect(basePath + "validation_dialect_fixed.raml")
        parsed <- validation.loadValidationProfile(basePath + "validation_profile_example.raml")
      } yield {
        println("LOADED!!!")
        assert(parsed != null)

      }
    } catch {
      case e:Exception => {
        e.printStackTrace()
        assert(e != null)
      }
    }
  }
}
