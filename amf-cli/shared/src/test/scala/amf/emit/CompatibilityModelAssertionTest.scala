package amf.emit

import amf.core.client.common.transform.PipelineId
import amf.core.internal.remote.Spec._
import amf.core.internal.remote.{Mimes, Spec}
import amf.core.io.FileAssertionTest
import amf.testing.ConfigProvider.configFor
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class CompatibilityModelAssertionTest extends FileAssertionTest with Matchers {

  private val basePath = "file://amf-cli/shared/src/test/resources/compatibility/"

  def compatibility(
      path: String,
      fromSpec: Spec,
      toSpec: Spec,
      pipelineId: String = PipelineId.Compatibility,
      outputMime: String = Mimes.`application/yaml`
  ): Future[String] = {
    val clientFrom = configFor(fromSpec).baseUnitClient()
    clientFrom.parse(basePath + path) map { parseResult =>
      val clientTo        = configFor(toSpec).baseUnitClient()
      val transformResult = clientTo.transform(parseResult.baseUnit, pipelineId)
      clientTo.render(transformResult.baseUnit, outputMime)
    }
  }

  test("Test no empty request in RAML to OAS conversion 1") {
    compatibility("raml10/empty-body-1.raml", RAML10, OAS30) map { output =>
      assert(output.nonEmpty)
      assert(!output.contains("content: {}"))
    }
  }

  test("Test no empty request in RAML to OAS conversion 2") {
    compatibility("raml10/empty-body-2.raml", RAML10, OAS30) map { output =>
      assert(output.nonEmpty)
      assert(!output.contains("content: {}"))
    }
  }
}
