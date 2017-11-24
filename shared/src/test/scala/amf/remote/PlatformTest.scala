package amf.remote

import java.util.Date

import amf.common.ListAssertions
import amf.framework.remote.Mimes.`APPLICATION/YAML`
import amf.core.unsafe.PlatformSecrets
import amf.framework.remote.Content
import org.scalatest.{Assertion, AsyncFunSuite}
import org.scalatest.Matchers._

import scala.concurrent.{ExecutionContext, Future}

class PlatformTest extends AsyncFunSuite with ListAssertions with PlatformSecrets {

  override implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  test("File") {
    platform
      .resolve("file://shared/src/test/resources/input.yaml", None) map {
      case Content(content, url, mime) =>
        mime should contain(`APPLICATION/YAML`)

        content.toString should be
        """|a: 1
                     |b: !include includes/include1.yaml
                     |c:
                     |  - 2
                     |d: !include includes/include2.yaml""".stripMargin

        content.sourceName should be("shared/src/test/resources/input.yaml")
    }
  }

  ignore("http") {
    val path = "http://amf.us-2.evennode.com/input.yaml"

    platform
      .resolve(path, None)
      .map(stream => {
        val content = stream.toString

        assert(
          content equals
            """|a: 1
                 |b: !include http://amf.us-2.evennode.com/include1.yaml/4000
                 |c:
                 |  - 2
                 |d: !include http://amf.us-2.evennode.com/include2.yaml/4000""".stripMargin)
      })
  }

  test("Path resolution") {
    Future.successful({
      val url = "file://shared/src/test/resources/input.yaml"

      platform.resolvePath(url) should be(url)
      platform.resolvePath("file://shared/src/test/resources/ignored/../input.yaml") should be(url)
    })
  }

  ignore("Write") {
    val path = "file:///tmp/" + new Date().getTime
    platform.write(path, "{\n\"name\" : \"Jason Bourne\"\n}").map[Assertion](unit => path should not be null)
  }
}
