package amf.parser

import amf.aml.client.scala.model.document.{Dialect, Vocabulary}
import amf.apicontract.client.scala.{AMFConfiguration, APIConfiguration}
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.remote.Spec
import amf.core.internal.remote.Spec._
import amf.graphql.client.scala.GraphQLConfiguration
import amf.graphqlfederation.client.scala.GraphQLFederationConfiguration
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class ParseContentTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  val basePath = "amf-cli/shared/src/test/resources/configuration/"

  test("RAML 1.0 parsed by content should return a Document") {
    run("raml10-api.raml", RAML10)
  }

  test("OAS 3.0 YAML parsed by content should return a Document") {
    run("oas30-api.yaml", OAS30)
  }

  test("OAS 3.0 JSON  parsed by content should return a Document") {
    run("oas30-api.json", OAS30)
  }

  test("OAS 2.0 YAML  parsed by content should return a Document") {
    run("oas20-api.yaml", OAS20)
  }

  test("OAS 2.0 JSON  parsed by content should return a Document") {
    run("oas20-api.json", OAS20)
  }

  test("ASYNC API 2.0 YAML  parsed by content should return a Document") {
    run("async-api.yaml", ASYNC20)
  }

  test("ASYNC API 2.0 JSON parsed by content should return a Document") {
    run("async-api.json", ASYNC20)
  }

  test("AML Dialect parsed by content should return a Document") {
    run("dialect.yaml", AML, expectDialect)
  }

  test("AML Vocabulary parsed by content should return a Document") {
    run("vocabulary.yaml", AML, expectVocabulary)
  }

  test("GraphQL parsed by content should return a Document") {
    run("graphql-api.graphql", GRAPHQL, config = GraphQLConfiguration.GraphQL())
  }

  test("GraphQLFederation parsed by content should return a Document") {
    run(
      "graphql-federation-api.graphql",
      GRAPHQL_FEDERATION,
      config = GraphQLFederationConfiguration.GraphQLFederation()
    )
  }

  private def run(
      apiName: String,
      expectedSpec: Spec,
      expectType: BaseUnit => Assertion = expectDocument,
      config: AMFConfiguration = APIConfiguration.API()
  ): Future[Assertion] = {
    val content = readContent(basePath + apiName)
    config.baseUnitClient().parseContent(content).map { result =>
      result.sourceSpec shouldEqual expectedSpec
      result.baseUnit.sourceSpec shouldEqual Some(expectedSpec)
      expectType(result.baseUnit)
    }
  }

  private def expectDocument(unit: BaseUnit)   = unit shouldBe a[Document]
  private def expectDialect(unit: BaseUnit)    = unit shouldBe a[Dialect]
  private def expectVocabulary(unit: BaseUnit) = unit shouldBe a[Vocabulary]

  private def readContent(path: String): String = {
    platform.fs.syncFile(path).read().toString
  }
}
