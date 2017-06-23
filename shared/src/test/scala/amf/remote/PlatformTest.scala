package amf.remote

import java.util.Date

import amf.common.ListAssertions
import amf.unsafe.PlatformSecrets
import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext.Implicits.global

class PlatformTest extends FunSuite with ListAssertions with PlatformSecrets {
  test("File") {
    val path = "file://shared/src/test/resources/input.yaml"

    platform
      .resolve(path, None)
      .map(stream => {
        val content = stream.toString

        assert(
          content equals
            """|a: 1
                 |b: !include includes/include1.yaml
                 |c:
                 |  - 2
                 |d: !include includes/include2.yaml""".stripMargin)

        assert(stream.stream.sourceName equals "shared/src/test/resources/input.yaml")
      })
  }

  test("Large Json") {
    val path = "file://shared/src/test/resources/17kJson.json"

    platform
      .resolve(path, None)
      .map(stream => {
        val content = stream.toString

        assert(content.length equals 531247)

        assert(stream.stream.sourceName equals "shared/src/test/resources/17kJson.json")
      })
  }

  /*'http {
        val path = "http://amf.us-2.evennode.com/input.yaml"

        platform.resolve(path, None).map(stream => {
            val content = stream.toString

            assert(content equals
              """|a: 1
                 |b: !include http://amf.us-2.evennode.com/include1.yaml/4000
                 |c:
                 |  - 2
                 |d: !include http://amf.us-2.evennode.com/include2.yaml/4000""".stripMargin)
        })
    }*/

  test("Write") {
    val path = "file:///tmp/" + new Date().getTime
    println("About to write output file to: " + path)
    platform.write(path, "{\n\"name\" : \"Jason Bourne\"\n}")
  }
}
