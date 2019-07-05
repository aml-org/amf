package amf.iterator

import amf.compiler.CompilerTestBuilder
import amf.core.iterator.{AmfElementIterator, CompleteIterator, DomainElementIterator}
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.AmfElement
import amf.core.parser.{Annotations, Fields}
import amf.core.remote.{AmfJsonHint, RamlYamlHint}
import amf.plugins.domain.webapi.models.{EndPoint, Parameter, WebApi}
import org.scalatest.{AsyncFunSuite, FunSuite, Matchers}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

class IteratorTest extends AsyncFunSuite with CompilerTestBuilder {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val api = buildApi()

  test("Iterating api with complete iterator returns all element") {
    new CompleteIterator(api)
    val elements = new CompleteIterator(api).toList
    assert(elements.length == 21)
  }

  test("Iterating api with domain element iterator returns domain elements") {
    val elements = new DomainElementIterator(api).toList
    assert(elements.length == 6)
  }

  test("Collect first in amf element using domain element iterator") {
    val maybeElement: Option[Parameter] = api.collectFirst() {
      case param: Parameter => param
    }
    assert(maybeElement.isDefined)
    assert(maybeElement.map(_.name.value()).getOrElse("") == "parameter name")
  }

  test("Full api with complete iterator") {
    build("file://amf-client/shared/src/test/resources/validations/annotations/allowed-targets/allowed-targets.raml",
          RamlYamlHint) map (
        baseUnit => {
          val iterator = new CompleteIterator(baseUnit.asInstanceOf[Document])
          val elements = iterator.toList
//        val diff = elements.diff(elements.distinct)
          assert(elements.size == 335)
        }
    )
  }

  test("Full api with domain element iterator") {
    build("file://amf-client/shared/src/test/resources/validations/annotations/allowed-targets/allowed-targets.raml",
          RamlYamlHint) map (
        baseUnit => {
          val iterator = new DomainElementIterator(baseUnit.asInstanceOf[Document])
          val elements = iterator.toList
          val diff     = elements.diff(elements.distinct)
          assert(elements.size == 98)
        }
    )
  }

  private def buildApi(): Document = {
    val api: WebApi   = new WebApi(Fields(), Annotations()).withName("test swagger entry")
    val endpoint      = api.withEndPoint("/endpoint")
    val operation     = endpoint.withOperation("get")
    val response      = operation.withResponse("200")
    val otherEndpoint = api.withEndPoint("/other")
    val parameter     = otherEndpoint.withParameter("parameter name")
    parameter.withBinding("file")
    response.withDescription("a descrip")
    new Document(Fields(), Annotations()).withEncodes(api)
  }

}
