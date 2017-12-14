package amf.maker

import amf.common.AmfObjectTestMatcher
import amf.core.model.document.{Document, Fragment}
import amf.core.model.domain.{AmfObject, DomainElement}
import amf.core.remote._
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.domain.shapes.models.NodeShape
import amf.plugins.domain.shapes.models.DomainExtensions._
import amf.plugins.domain.webapi.models.WebApi
import org.scalatest.{Assertion, AsyncFunSuite, Succeeded}

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  */
class ReferencesMakerTest extends AsyncFunSuite with PlatformSecrets with AmfObjectTestMatcher {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Data type fragment test raml") {
    val rootDocument = "file://amf-client/shared/src/test/resources/references/data-type-fragment.reference.raml"
    assertFixture(rootDocument, RamlYamlHint)
  }

  test("Data type fragment test oas") {
    val rootDocument = "file://amf-client/shared/src/test/resources/references/data-type-fragment.json"
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
        AmfObjectMatcher(withoutLocation(rootExpected)).assert(withoutLocation(actual))
        actual.references.zipWithIndex foreach {
          case (actualRef, index) =>
            AmfObjectMatcher(withoutLocation(rootExpected.references(index))).assert(withoutLocation(actualRef))
        }
        Succeeded
      })
  }

  def withoutLocation(e: AmfObject): AmfObject = {
    e.fields.remove(amf.core.metamodel.document.DocumentModel.Location)
    e
  }

  case class UnitsCreator(spec: Vendor) {

    val (file, fragmentFile, minCount) = spec match {
      case Raml => ("data-type-fragment.reference.raml", "person.raml", 1)
      case _    => ("data-type-fragment.json", "person.json", 1)
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
        .withLocation("file://amf-client/shared/src/test/resources/references/fragments/" + fragmentFile)
        .withId("file://amf-client/shared/src/test/resources/references/fragments/" + fragmentFile)
        .withEncodes(person)
    }

    val usesDataType: Document = {

      Document()
        .withId("/Users/hernan.najles/mulesoft/amf/amf-client/shared/src/test/resources/references/" + file)
        .withLocation("/Users/hernan.najles/mulesoft/amf/amf-client/shared/src/test/resources/references/" + file)
        .withEncodes(
          WebApi()
            .withId("amf-client/shared/src/test/resources/references/" + file + "#/web-api")
            .withName("API")
            .withVersion("1.0"))
        .withReferences(Seq(dataTypeFragment))
        .withDeclares(Seq(person.link("fragments/" + fragmentFile).asInstanceOf[NodeShape].withName("person")))
    }
  }
}
