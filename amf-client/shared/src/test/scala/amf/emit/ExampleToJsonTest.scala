package amf.emit

import amf.core.model.document.{BaseUnit, Document}
import amf.core.remote.RamlYamlHint
import amf.facades.{AMFCompiler, Validation}
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.model.{DataTypeFragment, NamedExampleFragment}
import amf.plugins.domain.shapes.models.AnyShape
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class ExampleToJsonTest extends AsyncFunSuite with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  test("Simple yaml scalar example") {
    cycle("simple-yaml-scalar.raml", "simple-yaml-scalar.json")
  }

  test("Simple yaml scalar example without raw") {
    cycle("simple-yaml-scalar.raml", "simple-yaml-scalar.json", removeRaw = true)
  }

  test("Simple yaml object example") {
    cycle("simple-yaml-object.raml", "simple-yaml-object.json")
  }

  test("Simple yaml object example without raw") {
    cycle("simple-yaml-object.raml", "simple-yaml-object.json", removeRaw = true)
  }

  test("Simple yaml array example") {
    cycle("simple-yaml-array.raml", "simple-yaml-array.json")
  }

  test("Simple yaml array example without raw") {
    cycle("simple-yaml-array.raml", "simple-yaml-array.json", removeRaw = true)
  }

  test("Json object example") {
    cycle("json-object.raml", "json-object-raw.json")
  }

  ignore("Json object example without raw") {
    cycle("json-object.raml", "json-object.json", removeRaw = true)
  }

  test("Json array example") {
    cycle("json-array.raml", "json-array-raw.json")
  }

  ignore("Json array example without raw") {
    cycle("json-array.raml", "json-array.json", removeRaw = true)
  }

  test("Xml example") {
    cycle("xml-example.raml", "xml-example.json")
  }

  private def cycle(source: String, golden: String, removeRaw: Boolean = false): Future[Assertion] = {
    for {
      v       <- Validation(platform)
      unit    <- AMFCompiler(basePath + source, platform, RamlYamlHint, v).build()
      example <- findExample(unit, removeRaw)
      temp    <- writeTemporaryFile(golden)(example.toJson)
      r       <- assertDifferences(temp, goldenPath + golden)
    } yield {
      r
    }
  }

  private def findExample(unit: BaseUnit, removeRaw: Boolean) = unit match {
    case f: Document =>
      val example = f.declares.head.asInstanceOf[AnyShape].examples.head
      if (removeRaw)
        example.raw.remove()
      Future.successful(example)
    case _ => Future.failed(fail("Not a named example fragment"))
  }

  private val basePath: String   = "file://amf-client/shared/src/test/resources/tojson/examples/source/"
  private val goldenPath: String = "amf-client/shared/src/test/resources/tojson/examples/generated/"
}
