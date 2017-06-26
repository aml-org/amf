package amf.remote

import amf.unsafe.PlatformSecrets
import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
  * Created by martin.gutierrez on 6/22/17.
  */
class ContextTest extends FunSuite with PlatformSecrets {

  test("Test context URL resolutions") {
    val c1 = Context(platform, "http://localhost:3000/input.yaml")
    c1.resolve("include.yaml") should be("http://localhost:3000/include.yaml")
    c1.resolve("nested/include.yaml") should be("http://localhost:3000/nested/include.yaml")

    val c2 = Context(platform, "http://localhost:3000/path/input.yaml")
    c2.resolve("include.yaml") should be("http://localhost:3000/path/include.yaml")
    c2.resolve("nested/include.yaml") should be("http://localhost:3000/path/nested/include.yaml")

    val c3 = Context(platform, "file://input.yaml")
    c3.resolve("include.yaml") should be("file://include.yaml")

    val c4 = Context(platform, "file://path/input.yaml")
    c4.resolve("include.yaml") should be("file://path/include.yaml")

    // Update to support nested includes
    val c11 = c1.update("relative/include.yaml")
    c11.resolve("other.yaml") should be("http://localhost:3000/relative/other.yaml")

    val c31 = c3.update("relative/include.yaml")
    c31.resolve("other.yaml") should be("file://relative/other.yaml")
  }

  test("Absolute include") {
    val httpContext = Context(platform, "http://localhost:3000/input.yaml")
    httpContext.root should be("http://localhost:3000/input.yaml")
    httpContext.current should be("http://localhost:3000/input.yaml")
    httpContext.resolve("include.yaml") should be("http://localhost:3000/include.yaml")

    val fileContext = Context(platform, "file://path/input.yaml")
    fileContext.root should be("file://path/input.yaml")
    fileContext.current should be("file://path/input.yaml")
    fileContext.resolve("include.yaml") should be("file://path/include.yaml")
  }

  test("Include two levels") {
    val c  = Context(platform, "http://localhost:3000/some/input.yaml")
    val c2 = c.update("/intermediate/inter.raml")

    c2.root should be("http://localhost:3000/some/input.yaml")
    c2.current should be("http://localhost:3000/some/intermediate/inter.raml")

    val c3ToRoot   = c2.update("/relative-to-root.raml")
    val c3ToParent = c2.update("relative-to-parent.raml")

    c3ToParent.history should contain theSameElementsInOrderAs List(
      "http://localhost:3000/some/input.yaml",
      "http://localhost:3000/some/intermediate/inter.raml",
      "http://localhost:3000/some/intermediate/relative-to-parent.raml"
    )
    c3ToRoot.history should contain theSameElementsInOrderAs List("http://localhost:3000/some/input.yaml",
                                                                  "http://localhost:3000/some/intermediate/inter.raml",
                                                                  "http://localhost:3000/some/relative-to-root.raml")
  }

  test("Resolve file path") {
    val c  = Context(platform, "file://localhost:3000/some/input.yaml")
    val c2 = c.update("../intermediate/inter.raml")

    c2.root should be("file://localhost:3000/some/input.yaml")
    c2.current should be("file://localhost:3000/intermediate/inter.raml")
  }

  test("Resolve http path") {
    val c  = Context(platform, "http://localhost:3000/some/input.yaml")
    val c2 = c.update("../intermediate/inter.raml")

    c2.root should be("http://localhost:3000/some/input.yaml")
    c2.current should be("http://localhost:3000/intermediate/inter.raml")
  }

  test("Mapping resolutions") {
    val c  = Context(platform, "file://localhost:3000/some/input.yaml")
    val c2 = c.update("level2/level3/inter.raml")
    val c3 = c2.update("./inter2.raml")
    val c4 = c3.update("../..//input.yaml")

    c2.root should be("file://localhost:3000/some/input.yaml")
    c2.current should be("file://localhost:3000/some/level2/level3/inter.raml")

    c3.root should be("file://localhost:3000/some/input.yaml")
    c3.current should be("file://localhost:3000/some/level2/level3/inter2.raml")

    c4.root should be("file://localhost:3000/some/input.yaml")
    c4.current should be("file://localhost:3000/some/input.yaml")

  }

  test("Cyclic references") {
    val c  = Context(platform, "http://localhost:3000/some/input.yaml")
    val c2 = c.update("../intermediate/inter.raml")
    val c3 = c.update("../some/input.yaml")

    c3.hasCycles should be(true)
  }
}
