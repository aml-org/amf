package amf.validation

import amf.client.environment.RAMLConfiguration
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
    val config = RAMLConfiguration.RAML10().withErrorHandlerProvider(() => UnhandledErrorHandler)
    val client = config.createClient()
    for {
      validation <- Validation(platform)
      library    <- client.parse(basePath + "payload_validation_shapes.raml").map(_.bu)
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
    val config = RAMLConfiguration.RAML10().withErrorHandlerProvider(() => UnhandledErrorHandler)
    val client = config.createClient()
    for {
      validation <- Validation(platform)
      library    <- client.parse(basePath + "payload_validation_shapes.raml").map(_.bu)
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
    val config = RAMLConfiguration.RAML10().withErrorHandlerProvider(() => UnhandledErrorHandler)
    val client = config.createClient()
    for {
      validation <- Validation(platform)
      library    <- client.parse(basePath + "payload_validation_shapes.raml").map(_.bu)
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
    val config = RAMLConfiguration.RAML().withErrorHandlerProvider(() => UnhandledErrorHandler)
    val client = config.createClient()
    for {
      validation <- Validation(platform)
      library    <- client.parse(basePath + "payload_validation_shapes.raml").map(_.bu)
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
