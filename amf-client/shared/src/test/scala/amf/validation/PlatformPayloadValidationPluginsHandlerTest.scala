package amf.validation

import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.{BaseUnit, Module}
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote.RamlYamlHint
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, Validation}
import amf.internal.environment.Environment
import amf.plugins.document.webapi.resolution.pipelines.AmfResolutionPipeline
import amf.plugins.domain.shapes.models.AnyShape
import org.scalatest.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

class PlatformPayloadValidationPluginsHandlerTest extends AsyncFunSuite with PlatformSecrets {

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
                             RamlYamlHint,
                             eh = UnhandledParserErrorHandler).build()
      validator <- Future {
        val shape = findShape(library, "A")
        shape.payloadValidator("application/json").get
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
                             RamlYamlHint,
                             eh = UnhandledParserErrorHandler).build()
      validator <- Future {
        val shape = findShape(library, "B")
        shape.payloadValidator("application/json").get
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
                             RamlYamlHint,
                             eh = UnhandledParserErrorHandler).build()
      validator <- Future {
        val resolved = new AmfResolutionPipeline().transform(library, UnhandledErrorHandler)
        val shape    = findShape(resolved, "D")
        shape.payloadValidator("application/json").get
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
                             RamlYamlHint,
                             eh = UnhandledParserErrorHandler).build()
      validator <- Future {
        val resolved = new AmfResolutionPipeline().transform(library, UnhandledErrorHandler)
        val shape    = findShape(resolved, "D")
        shape.payloadValidator("application/json", Environment()).get
      }
      invalid <- validator.isValid("application/wadus", "{\"a\": 10, \"d\": \"10\", \"kind\":\"D\"}").map(!_)
    } yield {
      assert(invalid)
    }
  }
}
