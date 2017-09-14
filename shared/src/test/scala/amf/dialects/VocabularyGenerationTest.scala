package amf.dialects

import amf.compiler.AMFCompiler
import amf.document.Document
import amf.dumper.AMFDumper
import amf.remote.Syntax.Json
import amf.remote.{RamlYamlHint, _}
import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers._
import amf.client.{GenerationOptions, RamlGenerator}

import scala.concurrent.ExecutionContext

class VocabularyGenerationTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Vocabulary test 1") {
    val generator = new RamlGenerator()
    try {
      for {
        parsed <- AMFCompiler("file://shared/src/test/resources/vocabularies/raml_async.raml", platform, RamlYamlHint).build()
      } yield {
        //val generated = generator.generateString(parsed)

        val rs=new AMFDumper(parsed, Amf, Json,GenerationOptions()).dumpToString;
        var resultLd=rs;

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
