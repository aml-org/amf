package amf.maker

import amf.client.environment.WebAPIConfiguration
import amf.common.AmfObjectTestMatcher
import amf.compiler.CompilerTestBuilder
import amf.core.client.scala.model.document.{Document, Fragment}
import amf.core.client.scala.model.domain.AmfObject
import amf.core.internal.remote._
import amf.plugins.document.apicontract.model.DataTypeFragment
import amf.plugins.domain.apicontract.models.api.WebApi

import org.scalatest.{Assertion, AsyncFunSuite, Succeeded}

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  */
class ReferencesMakerTest extends AsyncFunSuite with CompilerTestBuilder with AmfObjectTestMatcher {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Data type fragment test raml") {
    val rootDocument = "file://amf-cli/shared/src/test/resources/references/data-type-fragment.reference.raml"
    assertFixture(rootDocument, Raml10YamlHint)
  }

  test("Data type fragment test oas") {
    val rootDocument = "file://amf-cli/shared/src/test/resources/references/data-type-fragment.json"
    assertFixture(rootDocument, Oas20JsonHint)
  }

  private def assertFixture(rootFile: String, hint: Hint): Future[Assertion] = {

    val rootExpected = UnitsCreator(hint.vendor).usesDataType
    val amfConfig    = WebAPIConfiguration.WebAPI()
    build(rootFile, hint, amfConfig, None)
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
    e.fields.removeField(amf.core.internal.metamodel.document.DocumentModel.Location)
    e
  }

  case class UnitsCreator(spec: Vendor) {

    val (file, fragmentFile, minCount, recursive) = spec match {
      case Raml10 => ("data-type-fragment.reference.raml", "person.raml", 1, false)
      case _      => ("data-type-fragment.json", "person.json", 1, true)
    }

    private val person: NodeShape = {
      val shape = NodeShape().withName("type").withClosed(false)
      shape
        .withProperty("name")
        .withPath("http://a.ml/vocabularies/data#name")
        .withMinCount(minCount)
        .withScalarSchema("name")
        .withDataType("http://www.w3.org/2001/XMLSchema#string")
      shape
    }

    private val dataTypeFragment: Fragment = {
      DataTypeFragment()
        .withLocation("file://amf-cli/shared/src/test/resources/references/fragments/" + fragmentFile)
        .withId("file://amf-cli/shared/src/test/resources/references/fragments/" + fragmentFile)
        .withEncodes(person)
        .withRoot(false)
    }

    val usesDataType: Document = {

      val personLink = person.link("fragments/" + fragmentFile).asInstanceOf[NodeShape].withName("person")
      if (recursive) personLink.withSupportsRecursion(true)
      val api = WebApi()
        .withId("amf-cli/shared/src/test/resources/references/" + file + "#/web-api")
        .withName("API")
        .withVersion("1.0")
      if (spec == Oas20 || spec == Oas30) api.withEndPoints(Nil)
      Document()
        .withId("amf-cli/shared/src/test/resources/references/" + file)
        .withLocation("amf-cli/shared/src/test/resources/references/" + file)
        .withEncodes(api)
        .withReferences(Seq(dataTypeFragment))
        .withDeclares(Seq(personLink))
        .withRoot(true)
    }
  }
}
