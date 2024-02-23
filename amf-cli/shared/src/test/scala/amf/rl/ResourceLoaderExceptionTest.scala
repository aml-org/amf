package amf.rl

import amf.apicontract.client.scala.RAMLConfiguration
import amf.core.client.common.remote.Content
import amf.core.client.scala.exception.AmfUnhandledException
import amf.core.client.scala.resource.ResourceLoader
import org.mulesoft.antlrast.unsafe.PlatformSecrets
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class ResourceLoaderExceptionTest extends AsyncFunSuite with Matchers with PlatformSecrets{

  private val main =
    s"""#%RAML 1.0
       |title: something
       |
       |resourceTypes:
       |    myResource: !include fragment.raml
       |
       |/top:
       |    type: myResource""".stripMargin

  private val fragment =
    """#%RAML 1.0 ResourceType
      |
      |get:
      |    responses:
      |        200:
      |""".stripMargin

  private val rootPath = "/folder/name.raml"
  private val fragmentPath = "/folder/fragment.raml"

  class CustomUnhandledException(message: String) extends AmfUnhandledException(message)

  case class ExceptionResourceLoader() extends ResourceLoader {

    private var fileNameException: String = ""

    private val index = Map[String, String](rootPath -> main, fragmentPath -> fragment)

    override def fetch(resource: String): Future[Content] =
      if (resource == fileNameException) {
        throw new CustomUnhandledException(s"Exception with file $resource")
      } else {
        Future.successful(new Content(index(resource), resource))
      }

    override def accepts(resource: String): Boolean = true

    def withFileNameException(fn: String): ExceptionResourceLoader = {
      fileNameException = fn
      this
    }
  }

  private val baseConfiguration = RAMLConfiguration.RAML10()
  private val baseResourceLoader: ExceptionResourceLoader = ExceptionResourceLoader()

  test("Test thrown unhandled exception in main file") {
    val configuration = baseConfiguration.withResourceLoaders(List(baseResourceLoader.withFileNameException(rootPath)))
    assertThrows[CustomUnhandledException](configuration.baseUnitClient().parse(rootPath))
  }

  if (platform.name == "jvm") {
    test("Test thrown unhandled exception in fragment file") {
      val configuration = baseConfiguration.withResourceLoaders(List(baseResourceLoader.withFileNameException(fragmentPath)))
      ScalaFutures.whenReady(configuration.baseUnitClient().parse(rootPath).failed) {
        e =>
          e shouldBe a [CustomUnhandledException]
      }
    }
  }

}
