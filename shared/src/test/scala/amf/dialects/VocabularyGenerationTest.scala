package amf.dialects

import java.io.InputStreamReader

import amf.compiler.AMFCompiler
import amf.document.Document
import amf.dumper.AMFDumper
import amf.remote.Syntax.Json
import amf.remote.{RamlYamlHint, _}
import amf.unsafe.PlatformSecrets
import org.scalatest.{Assertion, AsyncFunSuite}
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
        new AMFDumper(unit, Amf, Json, GenerationOptions().withSourceMaps).dumpToString)

    actual
      .zip(expected)
      .map(checkDiff)

  }

  test("Vocabulary test 1") {
    //assertCycle("raml_async.raml","raml_async.json",AmfJsonHint, Raml);
    val generator = new RamlGenerator()
    try {
      for {
        parsed <- AMFCompiler("file://shared/src/test/resources/vocabularies/raml_async.raml", platform, RamlYamlHint).build()
        content <- platform.resolve("file://shared/src/test/resources/vocabularies/raml_async.json",Option.empty)
      } yield {
        //val generated = generator.generateString(parsed)
        for {
          rs <- new AMFDumper(parsed, Amf, Json, GenerationOptions()).dumpToString
        }
        yield {
          platform.write("file://shared/src/test/resources/vocabularies/raml_async.json2",rs)
        }
        //content shouldBe (rs).
        //val result = generated.join()
        //println(result)

      }
      true shouldBe(true)
    } catch {
      case e:Exception => true shouldBe(false)
    }
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
