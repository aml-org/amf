package amf.validation

import amf.core.exception.UnsupportedMediaTypeException
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote._
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, Validation}
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

// This suite is to test that AMF throws an UnsupportedMediaTypeException when the master API is an empty file
class VendorExceptionTest extends AsyncFunSuite with PlatformSecrets {

  val basePath                                             = "file://amf-client/shared/src/test/resources/validations/"
  implicit override def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  test("Empty RAML master API file to test vendor exception") {
    recoverToSucceededIf[UnsupportedMediaTypeException] {
      Validation(platform).flatMap { validation =>
        AMFCompiler(basePath + "/empty-raml.raml", platform, RamlYamlHint, eh = UnhandledParserErrorHandler).build()
      }
    }
  }

  test("Empty OAS json master API file to test vendor exception") {
    recoverToSucceededIf[UnsupportedMediaTypeException] {
      Validation(platform).flatMap { validation =>
        AMFCompiler(basePath + "/empty-oas.json", platform, OasJsonHint, eh = UnhandledParserErrorHandler).build()
      }
    }
  }

  test("Empty OAS yaml master API file to test vendor exception") {
    recoverToSucceededIf[UnsupportedMediaTypeException] {
      Validation(platform).flatMap { validation =>
        AMFCompiler(basePath + "/empty-oas.yaml", platform, OasYamlHint, eh = UnhandledParserErrorHandler).build()
      }
    }
  }
}
