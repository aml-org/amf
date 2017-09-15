package amf.dialects

import java.io.InputStreamReader

import amf.compiler.AMFCompiler
import amf.document.Document
import amf.dumper.AMFDumper
import amf.remote.Syntax.Json
import amf.remote.{RamlYamlHint, _}
import amf.unsafe.PlatformSecrets
import org.scalatest.{Assertion, AsyncFunSuite, Succeeded}
import org.scalatest.Matchers._
import amf.client.{GenerationOptions, RamlGenerator}
import amf.common.Tests.checkDiff

import scala.concurrent.{ExecutionContext, Future, Promise}

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
  def write(tuple: (String, String)): Assertion = tuple match {
    case (actual, expected) =>
      platform.write("file://shared/src/test/resources/vocabularies/validation_dialect.json",actual)
      Succeeded
  }
  def assertWrite(source: String, golden: String, hint: Hint, target: Vendor): Future[Assertion] = {
    val expected = platform
      .resolve(basePath + golden, None)
      .map(_.stream.toString)

    val actual = AMFCompiler(basePath + source, platform, hint)
      .build()
      .flatMap(unit =>
        new AMFDumper(unit, target, target.defaultSyntax, GenerationOptions()).dumpToString)

    actual
      .zip(expected)
      .map(write)

  }

  test("Parse Vocabulary") {
    assertCycle("raml_async.raml","raml_async.json",AmfJsonHint, Amf);
  }

  test("Store Vocabulary") {
    assertCycle("raml_async.raml","raml_async-gold.raml",AmfJsonHint, Raml);
  }
  test("Parse Dialect") {
    assertWrite("validation_dialect.raml","validation_dialect.json",AmfJsonHint, Amf);
  }

  test("Store Dialect") {
    assertCycle("validation_dialect.raml","validation_dialect-gold.raml",AmfJsonHint, Raml);
  }
//  test("Vocabulary test 00") {
//    val generator = new RamlGenerator()
//    try {
//      for {
//        parsed <- AMFCompiler("file://shared/src/test/resources/vocabularies/raml_async2.raml", platform, RamlYamlHint).build()
//      } yield {
//        val doc = Document(parsed.asInstanceOf[amf.document.Document])
//        val generated = generator.generateString(doc)
//        val rs=new AMFDumper(doc.unit, Amf, Json,GenerationOptions()).dumpToString;
//        var resultLd=rs;
//        //val result = generated.join()
//        //println(result)
//        //println(resultLd.)
//        //print("A")
//      }
//      true shouldBe(true)
//    } catch {
//      case e:Exception => true shouldBe(false)
//    }
//  }

//  test("Vocabulary test 2") {
//    val generator = new AmfGenerator()
//    try {
//      for {
//        parsed <- AMFCompiler("file://shared/src/test/resources/vocabularies/raml_async.raml", platform, RamlYamlHint).build()
//      } yield {
//        val doc = Document(parsed.asInstanceOf[amf.document.Document])
//        val generated = generator.generateString(doc)
//        val result = generated.join()
//        // println(result)
//      }
//      true shouldBe(true)
//    } catch {
//      case e:Exception => true shouldBe(false)
//    }
//  }
//
//  test("Dialect test 1") {
//    val generator = new AmfGenerator()
//    try {
//      for {
//        parsed <- DialectParser("file://shared/src/test/resources/vocabularies/mule_config.raml", "file://shared/src/test/resources/vocabularies/muleconfig.raml").process()
//      } yield {
//        println(parsed)
//        true shouldBe(true)
//      }
//    } catch {
//      case e:Exception => true shouldBe(false)
//    }
//  }
//
//  test("Dialect test 2") {
//    val generator = new AmfGenerator()
//    try {
//      for {
//        parsed <- DialectParser("file://shared/src/test/resources/vocabularies/mule_config.raml", "file://shared/src/test/resources/vocabularies/muleconfig.raml").toJsonld()
//      } yield {
//        println(parsed)
//        true shouldBe(true)
//      }
//    } catch {
//      case e:Exception => true shouldBe(false)
//    }
//  }

}
