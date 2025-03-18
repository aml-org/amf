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
      outputMime: String = Mimes.`application/yaml`,
      validate: Boolean = true
  ): Future[String] = {
    val clientFrom = configFor(fromSpec).baseUnitClient()
    clientFrom.parse(basePath + path) flatMap { parseResult =>
      val clientTo        = configFor(toSpec).baseUnitClient()
      val transformResult = clientTo.transform(parseResult.baseUnit, pipelineId)
      if (!validate) {
        Future.successful(clientTo.render(transformResult.baseUnit, outputMime))
      } else {
        clientTo.validate(transformResult.baseUnit) map { validationResult =>
          validationResult.results.size shouldBe 0
          clientTo.render(transformResult.baseUnit, outputMime)
        }
      }
    }
  }

  test("Test RAML type array to oas 2") {
    compatibility("raml10/type-array.raml", RAML10, OAS20) map { output =>
      output.nonEmpty shouldBe true
      assert(output.contains("items:"))
    }
  }

  test("Test no empty request in RAML to OAS conversion 1") {
    compatibility("raml10/empty-body-1.raml", RAML10, OAS30, validate = false) map { output =>
      assert(output.nonEmpty)
      assert(!output.contains("content: {}"))
    }
  }

  test("Test no empty request in RAML to OAS conversion 2") {
    compatibility("raml10/empty-body-2.raml", RAML10, OAS30, validate = false) map { output =>
      assert(output.nonEmpty)
      assert(!output.contains("content: {}"))
    }
  }
}
