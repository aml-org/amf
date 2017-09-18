package amf.dialects

import amf.client.GenerationOptions
import amf.common.Tests.checkDiff
import amf.compiler.AMFCompiler
import amf.dumper.AMFDumper
import amf.remote._
import amf.unsafe.PlatformSecrets
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class VocabularyGenerationTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath="file://shared/src/test/resources/vocabularies/"

  def assertCycle(source: String, golden: String, hint: Hint, target: Vendor): Future[Assertion] = {
    val expected = platform
      .resolve(basePath + golden, None)
      .map(_.stream.toString)

    val actual = AMFCompiler(basePath + source, platform, hint)
      .build()
      .flatMap(unit =>
        new AMFDumper(unit, target, target.defaultSyntax, GenerationOptions()).dumpToString)

    actual
      .zip(expected)
      .map(checkDiff)

  }
//  def write(tuple: (String, String)): Assertion = tuple match {
//    case (actual, expected) =>
//      platform.write("file://shared/src/test/resources/vocabularies/validation_dialect.json",actual)
//      Succeeded
//  }
//  def assertWrite(source: String, golden: String, hint: Hint, target: Vendor): Future[Assertion] = {
//    val expected = platform
//      .resolve(basePath + golden, None)
//      .map(_.stream.toString)
//
//    val actual = AMFCompiler(basePath + source, platform, hint)
//      .build()
//      .flatMap(unit =>
//        new AMFDumper(unit, target, target.defaultSyntax, GenerationOptions()).dumpToString)
//
//    actual
//      .zip(expected)
//      .map(write)
//
//  }

  test("Parse Vocabulary") {
    assertCycle("raml_async.raml","raml_async.json",AmfJsonHint, Amf);
  }

  test("Store Vocabulary") {
    assertCycle("raml_async.raml","raml_async-gold.raml",AmfJsonHint, Raml);
  }
  test("Parse Dialect") {
    assertCycle("validation_dialect.raml","validation_dialect.json",AmfJsonHint, Amf);
  }

  test("Store Dialect") {
    assertCycle("validation_dialect.raml","validation_dialect-gold.raml",AmfJsonHint, Raml);
  }

  test("Store Dialect 2") {
    assertCycle("mule_config_dialect.raml","mule_config_dialect_gold.raml",AmfJsonHint, Raml);
  }
}
