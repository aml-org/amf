package amf.maker

import amf.client.environment.WebAPIConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.AmfArray
import amf.core.client.scala.validation.AMFValidationResult
import amf.core.internal.remote._
import amf.facades.Validation
import amf.plugins.domain.apicontract.metamodel.api.WebApiModel
import amf.plugins.domain.apicontract.models.api.WebApi

import org.scalatest.{Assertion, Succeeded}

import scala.concurrent.Future

/**
  * Test class for documents
  */
class DocumentMakerTest extends WebApiMakerTest {

  test("Raml declared types ") {
    val doc = documentWithTypes(Raml10)
      .withLocation("file://amf-cli/shared/src/test/resources/maker/declared-types.raml")
    assertFixture(doc, "declared-types.raml", Raml10YamlHint)
  }

  test("Oas declared types ") {
    val doc = documentWithTypes(Oas20)
      .withLocation("file://amf-cli/shared/src/test/resources/maker/declared-types.json")

    doc.encodes.set(WebApiModel.EndPoints, AmfArray(Seq()))

    assertFixture(doc, "declared-types.json", Oas20JsonHint)
  }

  test("Raml inherits declared types ") {
    val doc = documentWithInheritsTypes(Raml10)
      .withLocation("file://amf-cli/shared/src/test/resources/maker/inherits-declared-types.raml")
    assertFixture(doc, "inherits-declared-types.raml", Raml10YamlHint)
  }

  test("Oas inherits declared types ") {
    val doc = documentWithInheritsTypes(Oas20)
      .withLocation("file://amf-cli/shared/src/test/resources/maker/inherits-declared-types.json")
    assertFixture(doc, "inherits-declared-types.json", Oas20YamlHint)
  }

  private def assertFixture(expected: Document, file: String, hint: Hint): Future[Assertion] = {
    val config = WebAPIConfiguration.WebAPI().withErrorHandlerProvider(() => IgnoreErrorHandler)
    Validation(platform).flatMap { v =>
      config.createClient().parse(basePath + file).map { unit =>
        val actual = unit.bu.asInstanceOf[Document]
        AmfObjectMatcher(expected).assert(actual)
        Succeeded
      }
    }
  }
  object IgnoreErrorHandler extends AMFErrorHandler {

    override def report(result: AMFValidationResult): Unit = {}

    override def getResults: List[AMFValidationResult] = Nil
  }

  private def documentWithTypes(vendor: Vendor): Document = {

    val minCount = vendor match {
      case _: Oas => 0
      case _      => 1
    }

    val person = NodeShape()
      .withName("Person")
      .withClosed(false)

    person
      .withProperty("name")
      .withPath("http://a.ml/vocabularies/data#name")
      .withMinCount(minCount)
      .withScalarSchema("name")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    person
      .withProperty("description")
      .withPath("http://a.ml/vocabularies/data#description")
      .withMinCount(minCount)
      .withScalarSchema("description")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    person
      .withProperty("age")
      .withPath("http://a.ml/vocabularies/data#age")
      .withMinCount(minCount)
      .withScalarSchema("age")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    val address = person
      .withProperty("address")
      .withPath("http://a.ml/vocabularies/data#address")
      .withMinCount(minCount)
      .withObjectRange("address")
    address
      .withClosed(false)
      .withProperty("street")
      .withPath("http://a.ml/vocabularies/data#street")
      .withMinCount(minCount)
      .withScalarSchema("street")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    address
      .withProperty("number")
      .withPath("http://a.ml/vocabularies/data#number")
      .withMinCount(minCount)
      .withScalarSchema("number")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    document().withDeclares(Seq(person))

  }

  private def document(): Document = {
    amf.core.client.scala.model.domain.extensions.PropertyShape().withScalarSchema("hey")
    val api = WebApi()
      .withName("test types")
      .withDescription("empty api only for test types")

    val document = Document()
      .withEncodes(api)
      .withRoot(true)
    document
  }

  private def documentWithInheritsTypes(vendor: Vendor) = {
    val minCount = vendor match {
      case _: Oas => 0
      case _      => 1
    }

    val id = vendor match {
      case _: Oas =>
        "file://amf-cli/shared/src/test/resources/maker/inherits-declared-types.json#/declarations/types/Human"
      case _ =>
        "file://amf-cli/shared/src/test/resources/maker/inherits-declared-types.raml#/declarations/types/Human"
    }

    val linkLabel = vendor match {
      case _: Oas => "#/definitions/Human"
      case _      => "Human"
    }

    val human = NodeShape()
      .withId(id)
      .withName("Human")
      .withClosed(false)

    human
      .withProperty("name")
      .withPath("http://a.ml/vocabularies/data#name")
      .withMinCount(minCount)
      .withScalarSchema("name")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    human
      .withProperty("description")
      .withPath("http://a.ml/vocabularies/data#description")
      .withMinCount(minCount)
      .withScalarSchema("description")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    human
      .withProperty("age")
      .withPath("http://a.ml/vocabularies/data#age")
      .withMinCount(minCount)
      .withScalarSchema("age")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    val person = NodeShape()
      .withName("Person")
      .withClosed(false)
    person
      .withProperty("omnipotent")
      .withPath("http://a.ml/vocabularies/data#omnipotent")
      .withMinCount(minCount)
      .withScalarSchema("omnipotent")
      .withDataType("http://www.w3.org/2001/XMLSchema#boolean")
    person.withInherits(Seq(human.link(linkLabel).asInstanceOf[AnyShape].withName("Human")))

    val address = person
      .withProperty("address")
      .withPath("http://a.ml/vocabularies/data#address")
      .withMinCount(minCount)
      .withObjectRange("address")
    address
      .withClosed(false)
      .withProperty("street")
      .withPath("http://a.ml/vocabularies/data#street")
      .withMinCount(minCount)
      .withScalarSchema("street")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    address
      .withProperty("number")
      .withPath("http://a.ml/vocabularies/data#number")
      .withMinCount(minCount)
      .withScalarSchema("number")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    document().withDeclares(Seq(human, person))

  }
}
