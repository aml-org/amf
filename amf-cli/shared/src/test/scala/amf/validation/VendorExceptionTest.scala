package amf.validation

import amf.apicontract.client.scala.{OASConfiguration, RAMLConfiguration}
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.exception.{UnsupportedDomainForDocumentException, UnsupportedSyntaxForDocumentException}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.unsafe.PlatformSecrets
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

// This suite is to test that AMF throws an UnsupportedMediaTypeException when the master API is an empty file
class VendorExceptionTest extends AsyncFunSuite with PlatformSecrets {

  val basePath                                             = "file://amf-cli/shared/src/test/resources/validations/"
  implicit override def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  test("Empty RAML master API file to test spec exception") {
    recoverToSucceededIf[UnsupportedSyntaxForDocumentException] {
      parse(basePath + "/empty-raml.raml", OASConfiguration.OAS().withErrorHandlerProvider(() => UnhandledErrorHandler))
    }
  }

  test("Empty OAS json master API file to test spec exception") {
    recoverToSucceededIf[UnsupportedSyntaxForDocumentException] {
      parse(
        basePath + "/empty-oas.json",
        RAMLConfiguration.RAML().withErrorHandlerProvider(() => UnhandledErrorHandler)
      )
    }
  }

  test("Empty OAS yaml master API file to test spec exception") {
    recoverToSucceededIf[UnsupportedSyntaxForDocumentException] {
      parse(
        basePath + "/empty-oas.yaml",
        RAMLConfiguration.RAML().withErrorHandlerProvider(() => UnhandledErrorHandler)
      )
    }
  }

  test("Parse RAML with oas config to test domain exception") {
    recoverToSucceededIf[UnsupportedDomainForDocumentException] {
      parse(
        basePath + "/empty-usage-uses.raml",
        OASConfiguration.OAS().withErrorHandlerProvider(() => UnhandledErrorHandler)
      )
    }
  }
  def parse(url: String, config: AMFGraphConfiguration): Future[BaseUnit] = {
    val client = config.baseUnitClient()
    client.parse(url).map(_.baseUnit)
  }

}
