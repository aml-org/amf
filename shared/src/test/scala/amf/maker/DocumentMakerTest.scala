package amf.maker

import amf.compiler.AMFCompiler
import amf.document.Document
import amf.domain.WebApi
import amf.remote.{Hint, RamlYamlHint}
import amf.shape.NodeShape
import org.scalatest.{Assertion, Succeeded}

import scala.concurrent.Future

/**
  * Test class for documents
  */
class DocumentMakerTest extends WebApiMakerTest {

  test("Raml declared types ") {
    val api = WebApi()
      .withName("test types")
      .withDescription("empty api only for test types")

    val person = NodeShape()
      .withName("Person")
      .withClosed(false)

    person
      .withProperty("name")
      .withMinCount(1)
      .withScalarSchema("name")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    person
      .withProperty("description")
      .withMinCount(1)
      .withScalarSchema("description")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    person
      .withProperty("age")
      .withMinCount(1)
      .withScalarSchema("age")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    val address = person
      .withProperty("address")
      .withMinCount(1)
      .withObjectRange("address")
    address
      .withClosed(false)
      .withProperty("street")
      .withMinCount(1)
      .withScalarSchema("street")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    address
      .withProperty("number")
      .withMinCount(1)
      .withScalarSchema("number")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    val document = Document()
      .withEncodes(api)
      .withDeclares(Seq(person))

    assertFixture(document, "declared-types.raml", RamlYamlHint)
  }

  test("Raml inherits declared types ") {
    val api = WebApi()
      .withName("test types")
      .withDescription("empty api only for test types")

    val human = NodeShape()
      .withName("Human")
      .withClosed(false)

    human
      .withProperty("name")
      .withMinCount(1)
      .withScalarSchema("name")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    human
      .withProperty("description")
      .withMinCount(1)
      .withScalarSchema("description")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    human
      .withProperty("age")
      .withMinCount(1)
      .withScalarSchema("age")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    val person = NodeShape()
      .withName("Person")
      .withClosed(false)
    person
      .withProperty("omnipotent")
      .withMinCount(1)
      .withScalarSchema("omnipotent")
      .withDataType("http://www.w3.org/2001/XMLSchema#boolean")
    person.withInherits(Seq(human))

    val address = person
      .withProperty("address")
      .withMinCount(1)
      .withObjectRange("address")
    address
      .withClosed(false)
      .withProperty("street")
      .withMinCount(1)
      .withScalarSchema("street")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    address
      .withProperty("number")
      .withMinCount(1)
      .withScalarSchema("number")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    val document = Document()
      .withEncodes(api)
      .withDeclares(Seq(human, person))

    assertFixture(document, "inherits-declared-types.raml", RamlYamlHint)
  }

  private def assertFixture(expected: Document, file: String, hint: Hint): Future[Assertion] = {

    AMFCompiler(basePath + file, platform, hint)
      .build()
      .map { unit =>
        val actual = unit.asInstanceOf[Document]
        AmfObjectMatcher(expected).assert(actual)
        Succeeded
      }
  }

}
