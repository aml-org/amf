package amf.maker

import amf.common.AmfObjectTestMatcher
import amf.compiler.CompilerTestBuilder
import amf.core.model.document.{Document, Fragment}
import amf.core.model.domain.AmfObject
import amf.core.remote._
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.domain.shapes.models.DomainExtensions._
import amf.plugins.domain.shapes.models.NodeShape
import amf.plugins.domain.webapi.models.api.WebApi
import org.scalatest.{Assertion, AsyncFunSuite, Succeeded}

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  */
class ReferencesMakerTest extends AsyncFunSuite with CompilerTestBuilder with AmfObjectTestMatcher {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Data type fragment test raml") {
    val rootDocument = "file://amf-client/shared/src/test/resources/references/data-type-fragment.reference.raml"
    assertFixture(rootDocument, Raml10YamlHint)
  }

  test("Data type fragment test oas") {
    val rootDocument = "file://amf-client/shared/src/test/resources/references/data-type-fragment.json"
    assertFixture(rootDocument, Oas20JsonHint)
  }

  private def assertFixture(rootFile: String, hint: Hint): Future[Assertion] = {

    val rootExpected = UnitsCreator(hint.vendor).usesDataType

    build(rootFile, hint)
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
    e.fields.removeField(amf.core.metamodel.document.DocumentModel.Location)
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
        .withLocation("file://amf-client/shared/src/test/resources/references/fragments/" + fragmentFile)
        .withId("file://amf-client/shared/src/test/resources/references/fragments/" + fragmentFile)
        .withEncodes(person)
        .withRoot(false)
    }

    val usesDataType: Document = {

      val personLink = person.link("fragments/" + fragmentFile).asInstanceOf[NodeShape].withName("person")
      if (recursive) personLink.withSupportsRecursion(true)
      val api = WebApi()
        .withId("amf-client/shared/src/test/resources/references/" + file + "#/web-api")
        .withName("API")
        .withVersion("1.0")
      if (spec == Oas20 || spec == Oas30) api.withEndPoints(Nil)
      Document()
        .withId("amf-client/shared/src/test/resources/references/" + file)
        .withLocation("amf-client/shared/src/test/resources/references/" + file)
        .withEncodes(api)
        .withReferences(Seq(dataTypeFragment))
        .withDeclares(Seq(personLink))
        .withRoot(true)
    }
  }
}
