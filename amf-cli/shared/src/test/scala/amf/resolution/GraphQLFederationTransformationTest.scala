package amf.resolution

import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.internal.remote.{AmfJsonHint, GraphQLFederationHint}
import amf.graphqlfederation.client.scala.GraphQLFederationConfiguration
import amf.graphqlfederation.internal.spec.transformation.GraphQLFederationIntrospectionPipeline
import amf.parser.GraphQLFederationFunSuiteCycleTests
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, ScalarShape, UnionShape}
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class GraphQLFederationTransformationTest extends GraphQLFederationFunSuiteCycleTests with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://amf-cli/shared/src/test/resources/graphql-federation/tck/apis/transformation/"

  test("Types _Any, _FieldSet, _Entity and _Service should be in introspected schema") {
    transformed("introspected-types.graphql").map { doc: Document =>
      val findInDeclares: String => Option[DomainElement] = findShapeWithName(doc.declares.toList, _)
      findInDeclares("_Any").get shouldBe a[ScalarShape]
      findInDeclares("_FieldSet").get shouldBe a[ScalarShape]
      findInDeclares("_Entity").get shouldBe a[UnionShape]
      findInDeclares("_Service").get shouldBe a[NodeShape]
    }
  }

  test("@external, @key, @provides, @requires directives should be in introspected schema") {
    transformed("introspected-types.graphql").map { doc: Document =>
      val findInDeclares: String => Option[DomainElement] = findShapeWithName(doc.declares.toList, _)
      findInDeclares("external").get shouldBe a[CustomDomainProperty]
      findInDeclares("key").get shouldBe a[CustomDomainProperty]
      findInDeclares("provides").get shouldBe a[CustomDomainProperty]
      findInDeclares("requires").get shouldBe a[CustomDomainProperty]
    }
  }

  test("_Entity introspected type should be a union of elements with key") {
    transformed("introspected-types.graphql").map { doc: Document =>
      val findInDeclares: String => Option[DomainElement] = findShapeWithName(doc.declares.toList, _)
      val _entity                                         = findInDeclares("_Entity").get.asInstanceOf[UnionShape]
      _entity.anyOf should have size 2
      val findInEntity = findShapeWithName(_entity.anyOf, _)
      findInEntity("Cat").get.asInstanceOf[NodeShape].keys should not be empty
      findInEntity("Dog").get.asInstanceOf[NodeShape].keys should not be empty
      findInDeclares("Romagnoli").get.asInstanceOf[NodeShape].keys shouldBe empty
    }
  }

  test("Introspected types to json-ld") {
    cycle(
      "introspected-types.graphql",
      "introspected-types.jsonld",
      GraphQLFederationHint,
      AmfJsonHint,
      directory = basePath.stripPrefix("file://"),
      renderOptions = Some(RenderOptions().withPrettyPrint.withCompactUris.withCompactedEmission)
    )
  }

  test("Introspection should add _entities and _service endpoints to existing") {
    transformed("introspected-endpoints.graphql").map { doc =>
      val endpoints = doc.encodes.asInstanceOf[Api].endPoints
      endpoints should have size 3
      endpoints.find(_.name.value() == "Query._entities") shouldBe a[Some[_]]
      endpoints.find(_.name.value() == "Query._service") shouldBe a[Some[_]]
    }
  }

  private def findShapeWithName(shapes: Seq[DomainElement], name: String): Option[DomainElement] = {
    shapes.find {
      case elem: CustomDomainProperty => elem.name.value() == name
      case elem: AnyShape             => elem.name.value() == name
      case _                          => false
    }
  }

  private def transformed(path: String): Future[Document] = {
    val client = config
    for {
      parsed    <- client.parseDocument(basePath + path)
      transform <- Future.successful { client.transform(parsed.document, GraphQLFederationIntrospectionPipeline.name) }
    } yield {
      transform.baseUnit.asInstanceOf[Document]
    }
  }

  private def config = {
    GraphQLFederationConfiguration
      .GraphQLFederation()
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
      .baseUnitClient()
  }

  /** Method for transforming parsed unit. Override if necessary. */
  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    GraphQLFederationConfiguration
      .GraphQLFederation()
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
      .baseUnitClient()
      .transform(unit, GraphQLFederationIntrospectionPipeline.name)
      .baseUnit
  }
}
