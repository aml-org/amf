package amf.validation

import amf.apicontract.client.scala.{AMFBaseUnitClient, AMFConfiguration, APIConfiguration}
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class GraphParsingValidationTest extends AsyncFunSuite with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private def basePath: String = "file://amf-cli/shared/src/test/resources/graphs/"

  test("Validate parsing api with context with expanded term definitions") {
    val path: String = s"$basePath/link-target-map/api.source.jsonld"
    run(path)
  }

  test("Validate parsing api with link target maps") {
    val path: String = s"$basePath/link-target-map/api.source.jsonld"
    run(path)
  }

  test("Validate parsing compacted id fields correctly applying base - flattened source") {
    val path: String = s"$basePath/recursive-api.flattened.jsonld"
    run(path)
  }

  test("Validate parsing compacted id fields correctly applying base - expanded source") {
    val path: String = s"$basePath/recursive-api.expanded.jsonld"
    run(path)
  }

  test("Validate parsing expanded uri fields") {
    val path: String = s"$basePath/recursive-api-full-uris.expanded.jsonld"
    run(path)
  }

  test("Validate parsing api with @base and absolute IRIs - flattened") {
    val path: String = s"$basePath/base-and-absolute-iris/api.source.flattened.jsonld"
    run(path)
  }

  test("Validate parsing api with @base and absolute IRIs - expanded") {
    val path: String = s"$basePath/base-and-absolute-iris/api.source.expanded.jsonld"
    run(path)
  }

  test("Validate parsing annotations with compact URIs") {
    val path: String = s"$basePath/annotations-compact/api.source.jsonld"
    run(path)
  }

  test("Validate parsing annotations with expanded URIs") {
    val path: String = s"$basePath/annotations-expanded/api.source.jsonld"
    run(path)
  }

  test("Validate parsing of non scalar annotations") {
    val path: String = s"$basePath/annotations-non-scalar/api.source.jsonld"
    run(path)
  }

  test("Validate parsing of '@type' with scalar value") {
    val path: String = s"$basePath/type-scalar-value.jsonld"
    run(path)
  }

  protected def run(path: String): Future[Assertion] = {
    val config: AMFConfiguration  = APIConfiguration.API()
    val client: AMFBaseUnitClient = config.baseUnitClient()
    client.parse(path).map(result => assert(result.conforms))
  }

}
