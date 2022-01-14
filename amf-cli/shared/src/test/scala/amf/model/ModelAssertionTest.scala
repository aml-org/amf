package amf.model

import amf.apicontract.client.scala.APIConfiguration
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.internal.annotations.InferredProperty
import amf.core.internal.remote.{Mimes, Spec}
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class ModelAssertionTest extends AsyncFunSuite with Matchers {

  val base = "file://amf-cli/shared/src/test/resources/model/"

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Inferred property annotation survived transformation") {
    parse(Spec.RAML10, base + "raml-with-inferred-json-schema-property.raml", PipelineId.Editing).map { unit =>
      assertInferredProperty(unit)
    }
  }

  test("Inferred property annotation is serialized to jsonld and then parsed") {
    cycle(Spec.RAML10, base + "raml-with-inferred-json-schema-property.raml", PipelineId.Editing).map { unit =>
      assertInferredProperty(unit)
    }
  }

  private def assertInferredProperty(unit: BaseUnit): Assertion = {
    val properties = unit
      .asInstanceOf[Document]
      .encodes
      .asInstanceOf[WebApi]
      .endPoints
      .head
      .operations
      .head
      .responses
      .head
      .payloads
      .head
      .schema
      .asInstanceOf[NodeShape]
      .properties

    val propertyB = properties.find(p => p.name.value() == "B").get
    val propertyA = properties.find(p => p.name.value() == "A").get

    propertyB.annotations.contains(classOf[InferredProperty]) shouldBe true
    propertyB.annotations.isInferredProperty shouldBe true

    propertyA.annotations.contains(classOf[InferredProperty]) shouldBe false
    propertyA.annotations.isInferredProperty shouldBe false
  }

  private def parse(spec: Spec, url: String): Future[BaseUnit] = {
    config(spec).baseUnitClient().parse(url).map(_.baseUnit)
  }

  private def parse(spec: Spec, url: String, pipeline: String): Future[BaseUnit] = {
    parse(spec, url).map { unit =>
      config(spec).baseUnitClient().transform(unit, pipeline).baseUnit
    }
  }

  private def cycle(spec: Spec, url: String, pipeline: String): Future[BaseUnit] = {
    parse(spec, url).flatMap { unit =>
      val client          = config(spec).baseUnitClient()
      val transformedUnit = client.transform(unit, pipeline).baseUnit
      val jsonld          = client.render(transformedUnit, Mimes.`application/ld+json`)
      client.parseContent(jsonld).map(_.baseUnit)
    }
  }

  private def config(spec: Spec) = {
    APIConfiguration
      .fromSpec(spec)
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
  }
}
