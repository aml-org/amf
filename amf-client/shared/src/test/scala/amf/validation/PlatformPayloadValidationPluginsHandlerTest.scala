package amf.validation

import amf.client.remod.AMFGraphConfiguration
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.client.validation.PayloadValidationUtils
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.{BaseUnit, Module}
import amf.core.remote.Raml10YamlHint
import amf.core.resolution.pipelines.TransformationPipelineRunner
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.document.webapi.resolution.pipelines.AmfTransformationPipeline
import amf.plugins.domain.shapes.models.AnyShape
import amf.remod.ShapePayloadValidatorFactory
import org.scalatest.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

class PlatformPayloadValidationPluginsHandlerTest
    extends AsyncFunSuite
    with PlatformSecrets
    with PayloadValidationUtils {

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
      library <- AMFCompiler(basePath + "payload_validation_shapes.raml",
                             platform,
                             Raml10YamlHint,
                             eh = UnhandledErrorHandler).build()
      validator <- Future {
        val shape = findShape(library, "A")
        ShapePayloadValidatorFactory.createPayloadValidator(
          shape,
          new ValidationConfiguration(AMFGraphConfiguration.predefined()))
      }
      valid   <- validator.isValid("application/json", "{\"a\": 10}")
      invalid <- validator.isValid("application/json", "{\"a\": \"10\"}").map(!_)
    } yield {
      assert(valid)
      assert(invalid)
    }
  }

  test("Validation logic, file shape always validate") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(basePath + "payload_validation_shapes.raml",
                             platform,
                             Raml10YamlHint,
                             eh = UnhandledErrorHandler).build()
      validator <- Future {
        val shape = findShape(library, "B")
        ShapePayloadValidatorFactory.createPayloadValidator(
          shape,
          new ValidationConfiguration(AMFGraphConfiguration.predefined()))
      }
      valid <- validator.isValid("application/json", "wadus")
    } yield {
      assert(valid)
    }
  }

  test("Validation logic, polymorphic shapes") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(basePath + "payload_validation_shapes.raml",
                             platform,
                             Raml10YamlHint,
                             eh = UnhandledErrorHandler).build()
      validator <- Future {
        val resolved = TransformationPipelineRunner(UnhandledErrorHandler).run(library, AmfTransformationPipeline())
        val shape    = findShape(resolved, "D")
        ShapePayloadValidatorFactory.createPayloadValidator(
          shape,
          new ValidationConfiguration(AMFGraphConfiguration.predefined()))
      }
      valid <- validator.isValid("application/json", "{\"a\": 10, \"d\": \"10\", \"kind\":\"D\"}")
    } yield {
      assert(valid)
    }
  }

  test("Exception if unsupported media type") {
    for {
      validation <- Validation(platform)
      library <- AMFCompiler(basePath + "payload_validation_shapes.raml",
                             platform,
                             Raml10YamlHint,
                             eh = UnhandledErrorHandler).build()
      validator <- Future {
        val resolved = TransformationPipelineRunner(UnhandledErrorHandler).run(library, AmfTransformationPipeline())
        val shape    = findShape(resolved, "D")
        ShapePayloadValidatorFactory.createPayloadValidator(
          shape,
          new ValidationConfiguration(AMFGraphConfiguration.predefined()))
      }
      invalid <- validator.isValid("application/wadus", "{\"a\": 10, \"d\": \"10\", \"kind\":\"D\"}").map(!_)
    } yield {
      assert(invalid)
    }
  }
}
