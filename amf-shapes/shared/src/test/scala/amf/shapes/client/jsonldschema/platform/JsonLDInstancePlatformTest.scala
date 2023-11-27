package amf.shapes.client.jsonldschema.platform

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.convert.NativeOps
import amf.shapes.client.platform.config.JsonLDSchemaConfiguration
import amf.shapes.client.platform.model.document.JsonLDInstanceDocument
import org.scalatest.matchers.should.Matchers

trait JsonLDInstancePlatformTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with NativeOps with Matchers {

  private val basePath = "amf-shapes/shared/src/test/resources/jsonld-schema/gcl/"

  test("Test parse and get jsonls instance") {
    val configuration = JsonLDSchemaConfiguration.JsonLDSchema()

    for {
      schema <- configuration.baseUnitClient().parseJsonLDSchema(s"file://${basePath}schemav1.json").asFuture
      instanceResult <- configuration
        .withJsonLDSchema(schema.jsonDocument)
        .baseUnitClient()
        .parse(s"file://${basePath}instance1.yaml")
        .asFuture
    } yield {
      val instance = instanceResult.baseUnit.asInstanceOf[JsonLDInstanceDocument]
      instance.encodes.asSeq.size shouldBe 1
    }

  }

}
