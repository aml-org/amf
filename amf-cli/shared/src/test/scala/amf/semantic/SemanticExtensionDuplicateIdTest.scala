package amf.semantic

import amf.apicontract.client.scala._
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.traversal.iterator.InstanceCollector
import org.mulesoft.common.collections.FilterType
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

trait DuplicateIdTest extends Matchers {

  def checkDuplicateIds(unit: BaseUnit): Assertion = lookForDuplicates(unit) shouldBe empty

  private def lookForDuplicates(unit: BaseUnit): Set[String] = {
    val idMap: Map[String, List[AmfObject]] = buildIdMap(unit)
    idsWithMoreThanOneAssociatedElement(idMap)
  }

  private def buildIdMap(unit: BaseUnit) = {
    val idMap = unit
      .iterator(visited = InstanceCollector())
      .toSeq
      .filterType[AmfObject]
      .foldLeft(Map[String, List[AmfObject]]()) { (acc, curr) =>
        acc + (curr.id -> acc.get(curr.id).map(x => x ++ List(curr)).getOrElse(List(curr)))
      }
    idMap
  }

  private def idsWithMoreThanOneAssociatedElement(idMap: Map[String, List[AmfObject]]) = {
    idMap.filter { case (_: String, value: List[AmfObject]) =>
      value.size > 1
    }.keySet
  }
}

class SemanticExtensionDuplicateIdTest extends AsyncFunSuite with Matchers with DuplicateIdTest {

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath: String = "amf-cli/shared/src/test/resources/semantic/"

  test("Raml10 - Nested object semantic extensions don't have duplicate ids") {
    check("api-nested-object.raml", RAMLConfiguration.RAML10())
  }

  test("Oas20 - Nested object semantic extensions don't have duplicate ids") {
    check("api-nested-object.oas20.yaml", OASConfiguration.OAS20())
  }

  test("Oas30 - Nested object semantic extensions don't have duplicate ids") {
    check("api-nested-object.oas30.yaml", OASConfiguration.OAS30())
  }

  test("Async20 - Nested object semantic extensions don't have duplicate ids") {
    check("api-nested-object.async.yaml", AsyncAPIConfiguration.Async20())
  }

  private def check(apiPath: String, baseConfig: AMFConfiguration): Future[Assertion] = {
    getConfig("nested-object-dialect.yaml", baseConfig).flatMap { config =>
      val client = config.baseUnitClient()
      client.parse(s"file://${basePath}/${apiPath}").map { parseResult =>
        val transformed = client.transform(parseResult.baseUnit).baseUnit
        checkDuplicateIds(transformed)
      }
    }
  }

  private def getConfig(
      dialect: String,
      baseConfig: AMFConfiguration = APIConfiguration.API()
  ): Future[AMFConfiguration] = {
    baseConfig
      .withRenderOptions(RenderOptions().withPrettyPrint.withCompactUris)
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
      .withDialect(s"file://$basePath" + dialect)
  }
}
