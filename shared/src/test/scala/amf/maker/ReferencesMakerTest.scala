package amf.maker

import amf.common.AmfObjectTestMatcher
import amf.compiler.AMFCompiler
import amf.framework.model.document.{Document, Fragment}
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.domain.shapes.models.NodeShape
import amf.plugins.domain.webapi.models.WebApi
import amf.remote._
import amf.framework.unsafe.PlatformSecrets
import amf.framework.remote._
import amf.validation.Validation
import org.scalatest.{Assertion, AsyncFunSuite, Succeeded}

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  */
class ReferencesMakerTest extends AsyncFunSuite with PlatformSecrets with AmfObjectTestMatcher {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Data type fragment test raml") {
    val file         = "data-type-fragment.raml"
    val rootDocument = "file://shared/src/test/resources/references/data-type-fragment.raml"
    assertFixture(rootDocument, RamlYamlHint)
  }

  test("Data type fragment test oas") {
    val file         = "data-type-fragment.json"
    val rootDocument = "file://shared/src/test/resources/references/data-type-fragment.json"
    assertFixture(rootDocument, OasJsonHint)
  }

  private def assertFixture(rootFile: String, hint: Hint): Future[Assertion] = {

    val rootExpected = UnitsCreator(hint.vendor).usesDataType

    AMFCompiler(rootFile, platform, hint, Validation(platform))
      .build()
      .map({
        case actual: Document => actual
      })
      .map({ actual =>
        AmfObjectMatcher(rootExpected).assert(actual)
        actual.references.zipWithIndex foreach {
          case (actualRef, index) =>
            AmfObjectMatcher(rootExpected.references(index)).assert(actualRef)
        }
        Succeeded
      })
  }

  case class UnitsCreator(spec: Vendor) {

    val (file, fragmentFile, minCount) = spec match {
      case Raml => ("data-type-fragment.raml", "person.raml", 1)
      case _    => ("data-type-fragment.json", "person.json", 0)
    }

    private val person: NodeShape = {
      val shape = NodeShape().withName("type").withClosed(false)
      shape
        .withProperty("name")
        .withPath("http://raml.org/vocabularies/data#name")
        .withMinCount(minCount)
        .withScalarSchema("name")
        .withDataType("http://www.w3.org/2001/XMLSchema#string")
      shape
    }

    private val dataTypeFragment: Fragment = {
      DataTypeFragment()
        .withId("file://shared/src/test/resources/references/fragments/" + fragmentFile)
        .withEncodes(person)
    }

    val usesDataType: Document = {

      Document()
        .withId("/Users/hernan.najles/mulesoft/amf/shared/src/test/resources/references/" + file)
        .withEncodes(WebApi().withId("shared/src/test/resources/references/" + file + "#/web-api"))
        .withReferences(Seq(dataTypeFragment))
        .withDeclares(Seq(person.link("fragments/" + fragmentFile).asInstanceOf[NodeShape].withName("person")))
    }
  }
}
