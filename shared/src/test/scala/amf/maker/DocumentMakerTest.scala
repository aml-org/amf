package amf.maker

import amf.compiler.AMFCompiler
import amf.document.Document
import amf.domain.WebApi
import amf.remote._
import amf.shape.NodeShape
import amf.validation.Validation
import org.scalatest.{Assertion, Succeeded}

import scala.concurrent.Future

/**
  * Test class for documents
  */
class DocumentMakerTest extends WebApiMakerTest {

  test("Raml declared types ") {

    assertFixture(documentWithTypes(Raml), "declared-types.raml", RamlYamlHint)
  }

  test("Oas declared types ") {

    assertFixture(documentWithTypes(Oas), "declared-types.json", OasJsonHint)
  }

  test("Raml inherits declared types ") {

    assertFixture(documentWithInheritsTypes(Raml), "inherits-declared-types.raml", RamlYamlHint)
  }

  test("Oas inherits declared types ") {

    assertFixture(documentWithInheritsTypes(Oas), "inherits-declared-types.json", OasYamlHint)
  }

  private def assertFixture(expected: Document, file: String, hint: Hint): Future[Assertion] = {

    AMFCompiler(basePath + file, platform, hint, Validation(platform))
      .build()
      .map { unit =>
        val actual = unit.asInstanceOf[Document]
        AmfObjectMatcher(expected).assert(actual)
        Succeeded
      }
  }

  private def documentWithTypes(vendor: Vendor): Document = {

    val minCount = vendor match {
      case Oas => 0
      case _   => 1
    }

    val person = NodeShape()
      .withName("Person")
      .withClosed(false)

    person
      .withProperty("name")
      .withPath("http://raml.org/vocabularies/data#name")
      .withMinCount(minCount)
      .withScalarSchema("name")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    person
      .withProperty("description")
      .withPath("http://raml.org/vocabularies/data#description")
      .withMinCount(minCount)
      .withScalarSchema("description")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    person
      .withProperty("age")
      .withPath("http://raml.org/vocabularies/data#age")
      .withMinCount(minCount)
      .withScalarSchema("age")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    val address = person
      .withProperty("address")
      .withPath("http://raml.org/vocabularies/data#address")
      .withMinCount(minCount)
      .withObjectRange("address")
    address
      .withClosed(false)
      .withProperty("street")
      .withPath("http://raml.org/vocabularies/data#street")
      .withMinCount(minCount)
      .withScalarSchema("street")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    address
      .withProperty("number")
      .withPath("http://raml.org/vocabularies/data#number")
      .withMinCount(minCount)
      .withScalarSchema("number")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    document().withDeclares(Seq(person))

  }

  private def document(): Document = {
    val api = WebApi()
      .withName("test types")
      .withDescription("empty api only for test types")

    val document = Document()
      .withEncodes(api)
    document
  }

  private def documentWithInheritsTypes(vendor: Vendor) = {
    val minCount = vendor match {
      case Oas => 0
      case _   => 1
    }

    val human = NodeShape()
      .withName("Human")
      .withClosed(false)

    human
      .withProperty("name")
      .withPath("http://raml.org/vocabularies/data#name")
      .withMinCount(minCount)
      .withScalarSchema("name")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    human
      .withProperty("description")
      .withPath("http://raml.org/vocabularies/data#description")
      .withMinCount(minCount)
      .withScalarSchema("description")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    human
      .withProperty("age")
      .withPath("http://raml.org/vocabularies/data#age")
      .withMinCount(minCount)
      .withScalarSchema("age")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    val person = NodeShape()
      .withName("Person")
      .withClosed(false)
    person
      .withProperty("omnipotent")
      .withPath("http://raml.org/vocabularies/data#omnipotent")
      .withMinCount(minCount)
      .withScalarSchema("omnipotent")
      .withDataType("http://www.w3.org/2001/XMLSchema#boolean")
    person.withInherits(Seq(human))

    val address = person
      .withProperty("address")
      .withPath("http://raml.org/vocabularies/data#address")
      .withMinCount(minCount)
      .withObjectRange("address")
    address
      .withClosed(false)
      .withProperty("street")
      .withPath("http://raml.org/vocabularies/data#street")
      .withMinCount(minCount)
      .withScalarSchema("street")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    address
      .withProperty("number")
      .withPath("http://raml.org/vocabularies/data#number")
      .withMinCount(minCount)
      .withScalarSchema("number")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    document().withDeclares(Seq(human, person))

  }
}
