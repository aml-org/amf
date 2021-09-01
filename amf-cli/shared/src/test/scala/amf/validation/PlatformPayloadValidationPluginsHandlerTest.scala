package amf.validation

import amf.apicontract.client.scala.RAMLConfiguration
import amf.apicontract.internal.transformation.AmfTransformationPipeline
import amf.client.validation.PayloadValidationUtils
import amf.core.client.common.validation.StrictValidationMode
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Module}
import amf.core.client.scala.transform.TransformationPipelineRunner
import amf.core.internal.remote.Mimes
import amf.core.internal.remote.Mimes._
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.scala.model.domain.AnyShape
import org.scalatest.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

class PlatformPayloadValidationPluginsHandlerTest
    extends AsyncFunSuite
    with PlatformSecrets
    with PayloadValidationUtils {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath          = "file://amf-cli/shared/src/test/resources/validations/"
  val APPLICATION_WADUS = "application/wadus"

  def findShape(library: BaseUnit, name: String): AnyShape = {
    val found = library.asInstanceOf[Module].declares.find { e =>
      e.isInstanceOf[AnyShape] && e.asInstanceOf[AnyShape].name.value() == name
    }
    found.get.asInstanceOf[AnyShape]
  }

  test("Validation logic, standard shape") {
    val config = RAMLConfiguration.RAML10().withErrorHandlerProvider(() => UnhandledErrorHandler)
    val client = config.baseUnitClient()
    for {
      library <- client.parse(basePath + "payload_validation_shapes.raml").map(_.baseUnit)
      validator <- Future.successful {
        val shape = findShape(library, "A")
        config.elementClient().payloadValidatorFor(shape, `application/json`, StrictValidationMode)
      }
      valid   <- validator.validate("{\"a\": 10}").map(_.conforms)
      invalid <- validator.validate("{\"a\": \"10\"}").map(r => !r.conforms)
    } yield {
      assert(valid)
      assert(invalid)
    }
  }

  test("Validation logic, file shape always validate") {
    val config = RAMLConfiguration.RAML10().withErrorHandlerProvider(() => UnhandledErrorHandler)
    val client = config.baseUnitClient()
    for {
      library <- client.parse(basePath + "payload_validation_shapes.raml").map(_.baseUnit)
      validator <- Future.successful {
        val shape = findShape(library, "B")
        config.elementClient().payloadValidatorFor(shape, `application/json`, StrictValidationMode)
      }
      valid <- validator.validate("wadus").map(_.conforms)
    } yield {
      assert(valid)
    }
  }

  test("Validation logic, polymorphic shapes") {
    val config = RAMLConfiguration.RAML10().withErrorHandlerProvider(() => UnhandledErrorHandler)
    val client = config.baseUnitClient()
    for {
      library <- client.parse(basePath + "payload_validation_shapes.raml").map(_.baseUnit)
      validator <- Future.successful {
        val resolved = TransformationPipelineRunner(UnhandledErrorHandler).run(library, AmfTransformationPipeline())
        val shape    = findShape(resolved, "D")
        config.elementClient().payloadValidatorFor(shape, `application/json`, StrictValidationMode)
      }
      valid <- validator.validate("{\"a\": 10, \"d\": \"10\", \"kind\":\"D\"}").map(_.conforms)
    } yield {
      assert(valid)
    }
  }

  test("Exception if unsupported media type") {
    val config = RAMLConfiguration.RAML().withErrorHandlerProvider(() => UnhandledErrorHandler)
    val client = config.baseUnitClient()
    for {
      library <- client.parse(basePath + "payload_validation_shapes.raml").map(_.baseUnit)
      validator <- Future.successful {
        val resolved = TransformationPipelineRunner(UnhandledErrorHandler).run(library, AmfTransformationPipeline())
        val shape    = findShape(resolved, "D")
        config.elementClient().payloadValidatorFor(shape, APPLICATION_WADUS, StrictValidationMode)
      }
      invalid <- validator.validate("{\"a\": 10, \"d\": \"10\", \"kind\":\"D\"}").map(r => !r.conforms)
    } yield {
      assert(invalid)
    }
  }
}
