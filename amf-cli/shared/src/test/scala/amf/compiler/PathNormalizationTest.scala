package amf.compiler

import amf.apicontract.client.scala.WebAPIConfiguration
import amf.core.client.common.remote.Content
import amf.core.client.scala.resource.ResourceLoader
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.remote.FileNotFound
import org.mulesoft.common.test.AsyncBeforeAndAfterEach

import scala.concurrent.Future

class PathNormalizationTest extends AsyncBeforeAndAfterEach with AsyncFunSuiteWithPlatformGlobalExecutionContext {

  test("Reference file located outside of root directory") {
    val rootUrl = "file:///api.raml"
    val rl = ContentResourceLoader(
      Map(
        rootUrl ->
          """
        |#%RAML 1.0
        |title: reduced-root-path-spec
        |
        |types:
        |  valid: !include ./dataType.raml
        |  invalid1: !include ./../dataType.raml
        |  invalid2: !include ./../../../dataType.raml
        |""".stripMargin
      )
    )

    val client = WebAPIConfiguration.WebAPI().withResourceLoaders(List(rl)).baseUnitClient()
    client.parse(rootUrl).map { _ =>
      val actual = rl.requestedUrls
      val expected =
        List("file:///api.raml", "file:///dataType.raml", "file:///../dataType.raml", "file:///../../../dataType.raml")
      assert(actual == expected)
    }
  }

  case class ContentResourceLoader(contents: Map[String, String]) extends ResourceLoader {

    var requestedUrls: List[String] = List()

    override def fetch(resource: String): Future[Content] = {
      requestedUrls = requestedUrls :+ resource
      contents
        .get(resource)
        .map(content => {
          Future.successful(new Content(content, resource))
        })
        .getOrElse(throw FileNotFound(new RuntimeException(s"Couldn't find resource $resource")))
    }

    override def accepts(resource: String): Boolean = true
  }
}
