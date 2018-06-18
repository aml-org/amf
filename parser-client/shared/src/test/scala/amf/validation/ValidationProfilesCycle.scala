package amf.validation

import amf.common.Tests.checkDiff
import amf.core.emitter.RenderOptions
import amf.core.remote.Syntax.{Json, Syntax, Yaml}
import amf.core.remote._
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, AMFRenderer, Validation}
import org.scalatest.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

class ValidationProfilesCycle extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://parser-client/shared/src/test/resources/vocabularies2/production/validation/"

  private def cycle(exampleFile: String, hint: Hint, syntax: Syntax, target: Vendor): Future[String] = {
    Validation(platform).flatMap(v => {
      v.loadValidationDialect().map(_ => v)
    }) flatMap { v =>
      AMFCompiler(basePath + exampleFile, platform, hint, v, None, None).build()
    } flatMap {
      AMFRenderer(_, target, syntax, RenderOptions()).renderToString
    }
  }

  test("Loading and serializing validations") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile).map(_.stream.toString)
    cycle(exampleFile, RamlYamlHint, Yaml, Raml).zip(expected).map(checkDiff)
  }

  test("prefixes can be loaded") {
    val expectedFile             = "validation_profile_prefixes.raml.jsonld"
    val exampleFile              = "validation_profile_prefixes.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile).map(_.stream.toString)
    val validation               = Validation(platform)
    cycle(exampleFile, RamlYamlHint, Json, Amf).zip(expected).map(checkDiff)
  }

  test("Prefixes can be parsed") {
    val expectedFile             = "validation_profile_prefixes.raml"
    val exampleFile              = "validation_profile_prefixes.raml.jsonld"
    val expected: Future[String] = platform.resolve(basePath + expectedFile).map(_.stream.toString)
    val validation               = Validation(platform)
    cycle(exampleFile, AmfJsonHint, Yaml, Raml).zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with inplace definition of encodes") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile).map(_.stream.toString)
    val validation               = Validation(platform)
    cycle(exampleFile, RamlYamlHint, Yaml, Raml).zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with inplace definition of range") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val validation               = Validation(platform)
    val expected: Future[String] = platform.resolve(basePath + expectedFile).map(_.stream.toString)
    cycle(exampleFile, RamlYamlHint, Yaml, Raml).zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with union type") {
    val expectedFile             = "validation_profile_example_gold.raml"
    val exampleFile              = "validation_profile_example.raml"
    val validation               = Validation(platform)
    val expected: Future[String] = platform.resolve(basePath + expectedFile).map(_.stream.toString)
    cycle(exampleFile, RamlYamlHint, Yaml, Raml).zip(expected).map(checkDiff)
  }

}
