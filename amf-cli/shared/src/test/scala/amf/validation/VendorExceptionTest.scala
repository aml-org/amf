package amf.validation

import amf.client.environment.{OASConfiguration, RAMLConfiguration}
import amf.client.remod.AMFGraphConfiguration
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.exception.UnsupportedMediaTypeException
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.core.unsafe.PlatformSecrets
import amf.facades.Validation
import org.scalatest.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

// This suite is to test that AMF throws an UnsupportedMediaTypeException when the master API is an empty file
class VendorExceptionTest extends AsyncFunSuite with PlatformSecrets {

  val basePath                                             = "file://amf-client/shared/src/test/resources/validations/"
  implicit override def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  test("Empty RAML master API file to test vendor exception") {
    recoverToSucceededIf[UnsupportedMediaTypeException] {
      parse(basePath + "/empty-raml.raml",
            Raml10.mediaType,
            RAMLConfiguration.RAML().withErrorHandlerProvider(() => UnhandledErrorHandler))
    }
  }

  test("Empty OAS json master API file to test vendor exception") {
    recoverToSucceededIf[UnsupportedMediaTypeException] {
      parse(basePath + "/empty-oas.json",
            Oas20.mediaType,
            OASConfiguration.OAS().withErrorHandlerProvider(() => UnhandledErrorHandler))
    }
  }

  test("Empty OAS yaml master API file to test vendor exception") {
    recoverToSucceededIf[UnsupportedMediaTypeException] {
      parse(basePath + "/empty-oas.yaml",
            Oas20.mediaType,
            OASConfiguration.OAS().withErrorHandlerProvider(() => UnhandledErrorHandler))
    }
  }

  def parse(url: String, mediaType: String, config: AMFGraphConfiguration): Future[BaseUnit] = {
    val client = config.createClient()
    Validation(platform).flatMap { _ =>
      client.parse(url, mediaType).map(_.bu)
    }
  }

}
