package amf.iterator

import amf.client.environment.{AsyncAPIConfiguration, WebAPIConfiguration}
import amf.compiler.CompilerTestBuilder
import amf.core.annotations.DomainExtensionAnnotation
import amf.core.metamodel.domain.common.DescriptionField
import amf.core.model.document.Document
import amf.core.model.document.FieldsFilter.Local
import amf.core.model.domain.extensions.DomainExtension
import amf.core.model.domain.{AmfObject, AmfScalar}
import amf.core.parser.{Annotations, Fields}
import amf.core.remote.Raml10YamlHint
import amf.core.traversal.iterator.{AmfElementStrategy, DomainElementStrategy, IdCollector, InstanceCollector}
import amf.plugins.domain.apicontract.models.Parameter
import amf.plugins.domain.apicontract.models.api.WebApi
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class IteratorTest extends AsyncFunSuite with CompilerTestBuilder {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val api = buildApi()

  test("Iterating api with complete iterator returns all element") {
    val elements = AmfElementStrategy.iterator(List(api), IdCollector()).toList
    assert(elements.length == 23)
  }

  test("Iterating api with domain element iterator returns domain elements") {
    val elements = DomainElementStrategy.iterator(List(api), IdCollector()).toList
    assert(elements.length == 6)
  }

  test("Collect first using domain element iterator") {
    val maybeElement: Option[Parameter] = DomainElementStrategy.iterator(List(api), IdCollector()).collectFirst {
      case param: Parameter => param
    }
    assert(maybeElement.isDefined)
    assert(maybeElement.map(_.name.value()).getOrElse("") == "parameter name")
  }

  test("Collect amf scalar using amf element iterator") {
    val domainExtensionsInScalarsAnnotations =
      api
        .iterator(strategy = AmfElementStrategy, fieldsFilter = Local)
        .collect {
          case scalar: AmfScalar if scalar.annotations.find(classOf[DomainExtensionAnnotation]).isDefined =>
            scalar.annotations
              .collect[DomainExtensionAnnotation] {
                case domainAnnotation: DomainExtensionAnnotation => domainAnnotation
              }
              .map(_.extension)
        }
        .toSeq
        .flatten
    assert(domainExtensionsInScalarsAnnotations.size == 1)
  }

  test("Full api with complete iterator") {
    build(
      "file://amf-cli/shared/src/test/resources/validations/annotations/allowed-targets/allowed-targets.raml",
      Raml10YamlHint,
      WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()),
      None
    ) map (
        baseUnit => {
          val iterator = AmfElementStrategy.iterator(List(baseUnit.asInstanceOf[Document]), IdCollector())
          val elements = iterator.toList
          assert(elements.size == 338)
        }
    )
  }

  test("Full api with complete iterator using instance collector") {
    build(
      "file://amf-cli/shared/src/test/resources/validations/annotations/allowed-targets/allowed-targets.raml",
      Raml10YamlHint,
      WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()),
      None
    ) map (
        baseUnit => {
          val iterator = AmfElementStrategy.iterator(List(baseUnit.asInstanceOf[Document]), InstanceCollector())
          val elements = iterator.toList
          val duplicates = elements
            .groupBy {
              case a: AmfObject => a.id
              case _            => "other"
            }
            .filter { case (k, vales) => vales.size > 1 }
          assert(elements.size == 350)
        }
    )
  }

  test("Full api with domain element iterator") {
    build(
      "file://amf-cli/shared/src/test/resources/validations/annotations/allowed-targets/allowed-targets.raml",
      Raml10YamlHint,
      WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()),
      None
    ) map (
        baseUnit => {
          val iterator = DomainElementStrategy.iterator(List(baseUnit.asInstanceOf[Document]), IdCollector())
          val elements = iterator.toList
          assert(elements.size == 98)
        }
    )
  }

  test("Full api with domain element iterator using instance collector") {
    build(
      "file://amf-cli/shared/src/test/resources/validations/annotations/allowed-targets/allowed-targets.raml",
      Raml10YamlHint,
      WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()),
      None
    ) map (
        baseUnit => {
          val iterator = DomainElementStrategy.iterator(List(baseUnit.asInstanceOf[Document]), InstanceCollector())
          val elements = iterator.toList
          assert(elements.size == 102)
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

    val domainExtension = DomainExtension().withName("a domain extension")
    response.set(DescriptionField.Description,
                 AmfScalar("a description", Annotations(DomainExtensionAnnotation(domainExtension))))
    new Document(Fields(), Annotations()).withEncodes(api)
  }

}
