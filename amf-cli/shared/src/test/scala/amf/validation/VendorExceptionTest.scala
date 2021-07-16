package amf.validation

import amf.apicontract.client.scala.{OASConfiguration, RAMLConfiguration}
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.exception.UnsupportedMediaTypeException
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.{Oas20, Raml10}
import amf.core.internal.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

// This suite is to test that AMF throws an UnsupportedMediaTypeException when the master API is an empty file
class VendorExceptionTest extends AsyncFunSuite with PlatformSecrets {

  val basePath                                             = "file://amf-cli/shared/src/test/resources/validations/"
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
    val client = config.baseUnitClient()
    client.parse(url, mediaType).map(_.baseUnit)
  }

}
