package amf.validation

import amf.apicontract.client.scala.WebAPIConfiguration
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.internal.remote.Syntax.{Json, Syntax, Yaml}
import amf.core.internal.remote._
import amf.core.internal.unsafe.PlatformSecrets
import org.mulesoft.common.test.Tests.checkDiff
import org.scalatest.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

class ValidationProfilesCycle extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://amf-cli/shared/src/test/resources/vocabularies2/production/validation/"

  private def cycle(exampleFile: String, hint: Hint, syntax: Syntax, target: Vendor): Future[String] = {
    val config = WebAPIConfiguration
      .WebAPI()
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
      .withRenderOptions(RenderOptions().withoutFlattenedJsonLd)
    for {

      clientWithValidation <- config.withCustomValidationsEnabled().map(_.createClient())
      bu                   <- clientWithValidation.parse(basePath + exampleFile).map(_.bu)
      r                    <- Future.successful(clientWithValidation.render(bu, target.mediaType))
    } yield r
  }

  test("Loading and serializing validations") {
    val expectedFile = "validation_profile_example_gold.yaml"
    val exampleFile  = "validation_profile_example.yaml"
    val expected: Future[String] =
      platform.fetchContent(basePath + expectedFile, AMFGraphConfiguration.predefined()).map(_.stream.toString)
    cycle(exampleFile, VocabularyYamlHint, Yaml, Aml).zip(expected).map(checkDiff)
  }

  test("prefixes can be loaded") {
    val expectedFile          = "validation_profile_prefixes.yaml.jsonld" // TODO: delete this when deprecating legacy json-ld emitter
    val expectedFlattenedFile = "validation_profile_prefixes.yaml.flattened.jsonld"
    val exampleFile           = "validation_profile_prefixes.yaml"
    val expected: Future[String] =
      platform.fetchContent(basePath + expectedFile, AMFGraphConfiguration.predefined()).map(_.stream.toString)
    val expectedFlattened: Future[String] = platform
      .fetchContent(basePath + expectedFlattenedFile, AMFGraphConfiguration.predefined())
      .map(_.stream.toString)
    cycle(exampleFile, VocabularyYamlHint, Json, Amf).zip(expected).map(checkDiff)
  }

  test("Prefixes can be parsed") {
    val expectedFile = "validation_profile_prefixes.yaml"
    val exampleFile  = "validation_profile_prefixes.yaml.jsonld"
    val expected: Future[String] =
      platform.fetchContent(basePath + expectedFile, AMFGraphConfiguration.predefined()).map(_.stream.toString)
    cycle(exampleFile, AmfJsonHint, Yaml, Aml).zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with inplace definition of encodes") {
    val expectedFile = "validation_profile_example_gold.yaml"
    val exampleFile  = "validation_profile_example.yaml"
    val expected: Future[String] =
      platform.fetchContent(basePath + expectedFile, AMFGraphConfiguration.predefined()).map(_.stream.toString)
    cycle(exampleFile, VocabularyYamlHint, Yaml, Aml).zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with inplace definition of range") {
    val expectedFile = "validation_profile_example_gold.yaml"
    val exampleFile  = "validation_profile_example.yaml"
    val expected: Future[String] =
      platform.fetchContent(basePath + expectedFile, AMFGraphConfiguration.predefined()).map(_.stream.toString)
    cycle(exampleFile, VocabularyYamlHint, Yaml, Aml).zip(expected).map(checkDiff)
  }

  test("Loading and serializing validations with union type") {
    val expectedFile = "validation_profile_example_gold.yaml"
    val exampleFile  = "validation_profile_example.yaml"
    val expected: Future[String] =
      platform.fetchContent(basePath + expectedFile, AMFGraphConfiguration.predefined()).map(_.stream.toString)
    cycle(exampleFile, VocabularyYamlHint, Yaml, Aml).zip(expected).map(checkDiff)
  }

}
