package amf.validation

import amf.core.model.document.{BaseUnit, Module}
import amf.core.remote.RamlYamlHint
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.document.webapi.resolution.pipelines.AmfResolutionPipeline
import amf.plugins.domain.shapes.models.AnyShape
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class PlatformPayloadValidatorTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://amf-client/shared/src/test/resources/validations/"

  def findShape(library: BaseUnit, name: String): AnyShape = {
    val found = library.asInstanceOf[Module].declares.find { e =>
      e.isInstanceOf[AnyShape] && e.asInstanceOf[AnyShape].name.value() == name
    }
    found.get.asInstanceOf[AnyShape]
  }

  test("Validation logic, standard shape") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(basePath + "payload_validation_shapes.raml", platform, RamlYamlHint, validation).build()
    } yield {
      val shape     = findShape(library, "A")
      val validator = shape.payloadValidator()

      assert(validator.fastValidation("application/json", "{\"a\": 10}"))
      assert(!validator.fastValidation("application/json", "{\"a\": \"10\"}"))
    }
  }

  test("Validation logic, file shape always validate") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(basePath + "payload_validation_shapes.raml", platform, RamlYamlHint, validation).build()
    } yield {
      val shape     = findShape(library, "B")
      val validator = shape.payloadValidator()

      assert(validator.fastValidation("application/json", "wadus"))
    }
  }

  test("Validation logic, polymorphic shapes") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(basePath + "payload_validation_shapes.raml", platform, RamlYamlHint, validation).build()
    } yield {
      val resolved  = new AmfResolutionPipeline(library).resolve()
      val shape     = findShape(resolved, "D")
      val validator = shape.payloadValidator()

      assert(validator.fastValidation("application/json", "{\"a\": 10, \"d\": \"10\", \"kind\":\"D\"}"))
    }
  }

  test("Exception if unsupported media type") {
    for {
      validation <- Validation(platform)
      library    <- AMFCompiler(basePath + "payload_validation_shapes.raml", platform, RamlYamlHint, validation).build()
    } yield {
      val resolved  = new AmfResolutionPipeline(library).resolve()
      val shape     = findShape(resolved, "D")
      val validator = shape.payloadValidator()

      try {
        validator.fastValidation("application/wadus", "{\"a\": 10, \"d\": \"10\", \"kind\":\"D\"}")
        assert(false)
      } catch {
        case _: Exception => assert(true)
      }
    }
  }

}
