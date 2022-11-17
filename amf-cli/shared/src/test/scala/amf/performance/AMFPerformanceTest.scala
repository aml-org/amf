package amf.performance

import amf.apicontract.client.scala.{AMFBaseUnitClient, AMFConfiguration, OASConfiguration, RAMLConfiguration}
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.graphql.client.scala.GraphQLConfiguration
import org.antlr.v4.runtime.misc.Utils.writeFile
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class AMFPerformanceTest extends AsyncFunSuite with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath                        = "file://amf-cli/shared/src/test/resources/validations"
  val ro: RenderOptions               = RenderOptions().withCompactUris.withPrettyPrint.withSourceMaps
  val graphqlConfig: AMFConfiguration = GraphQLConfiguration.GraphQL().withRenderOptions(ro)
  val ramlConfig: AMFConfiguration    = RAMLConfiguration.RAML10().withRenderOptions(ro)
  val ramlClient: AMFBaseUnitClient   = ramlConfig.baseUnitClient()
  val raml08Config: AMFConfiguration  = RAMLConfiguration.RAML08().withRenderOptions(ro)
  val raml08Client: AMFBaseUnitClient = raml08Config.baseUnitClient()
  val oasConfig: AMFConfiguration     = OASConfiguration.OAS30().withRenderOptions(ro)
  val oasClient: AMFBaseUnitClient    = oasConfig.baseUnitClient()

  private def time[R](activity: String = "Elapsed")(block: => R): R = {
    val t0     = System.currentTimeMillis()
    val result = block // call-by-namez
    val t1     = System.currentTimeMillis()
    println(s"$activity time: " + (t1 - t0) + "ms")
    result
  }

  private def await[R](eventualResult: Future[R]): R = {
    Await.result(eventualResult, Duration.Inf)
  }

  private def writeFile(filePath: String, content: String) = {
    Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8))
  }

  private def memoryInUse() = {
    val mb         = 1024 * 1024
    val runtime    = Runtime.getRuntime
    val usedMemory = (runtime.totalMemory - runtime.freeMemory) / mb
    usedMemory
  }

  private def size(activity: String, content: String): Unit = {
    println(s"$activity has ${content.length}")
  }

  test("CRI original") {
    val ramlApi = s"$basePath/raml/big-example-original/financialforce-bs-sapi.raml"
    val parseResult = time("parsing") {
      await(ramlClient.parse(ramlApi))
    }
    val transformResult = time("transform") {
      ramlClient.transform(parseResult.baseUnit, PipelineId.Editing)
    }
    val render = time("render transform") {
      ramlClient.render(transformResult.baseUnit, "application/ld+json")
    }
    render.isEmpty shouldBe false
  }

  test("CRI minified") {
    val testPath = s"$basePath/raml/big-example"
    val ramlApi  = s"$testPath/api-min.raml"
    val jsonld   = s"$testPath/result-min.jsonld".substring("file://".length)

    val parseResult = time("parsing") { await(ramlClient.parse(ramlApi)) }

    val transformResult = time("transform") {
      ramlClient.transform(parseResult.baseUnit, PipelineId.Editing)
    }
    val render = time("render transform") {
      ramlClient.render(transformResult.baseUnit, "application/ld+json")
    }
    writeFile(jsonld, render)
    size("json-ld", render)
    render.isEmpty shouldBe false
  }
}
