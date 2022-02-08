package amf.parser

import amf.apicontract.client.scala.RAMLConfiguration
import amf.core.client.common.position.Range
import amf.core.client.scala.errorhandling.DefaultErrorHandler
import amf.core.client.scala.validation.AMFValidationResult
import amf.core.internal.unsafe.PlatformSecrets
import org.scalatest.Succeeded
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

/**
  * Created by pedro.colunga on 10/10/17.
  */
class ForwardReferencesTest extends AsyncFunSuite with Matchers with PlatformSecrets {

  private val referencesPath = "file://amf-cli/shared/src/test/resources/references/"
  private val basePath       = "file://amf-cli/shared/src/test/resources/upanddown/"

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Test reference not found exception on property shape") {
    validate(
      basePath + "forward-references-types-error.raml",
      undefined => {
        undefined.severityLevel should be("Violation")
        undefined.message should be("Unresolved reference 'UndefinedType'")
        undefined.location should be(
          Some("file://amf-cli/shared/src/test/resources/upanddown/forward-references-types-error.raml"))
        undefined.position.map(_.range) should be(Some(Range((8, 14), (8, 27))))
      }
    )
  }

  test("Test reference not found exception on expression") {
    validate(
      basePath + "forward-references-types-error-expression.raml",
      undefined => {
        undefined.severityLevel should be("Violation")
        undefined.message should be("Unresolved reference 'UndefinedType'")
        undefined.location should be(
          Some("file://amf-cli/shared/src/test/resources/upanddown/forward-references-types-error-expression.raml"))
        undefined.position.map(_.range) should be(Some(Range((8, 14), (8, 40))))
      }
    )
  }

  test("Test reference not found exception on array") {
    validate(
      basePath + "forward-references-types-error-array.raml",
      undefined => {
        undefined.severityLevel should be("Violation")
        undefined.message should be("Unresolved reference 'UndefinedType'")
        undefined.location should be(
          Some("file://amf-cli/shared/src/test/resources/upanddown/forward-references-types-error-array.raml"))
        undefined.position.map(_.range) should be(Some(Range((5, 26), (5, 39))))
      }
    )
  }

  test("Test complex contexts") {
    validate(
      referencesPath + "contexts/api.raml",
      a => {
        a.severityLevel should be("Violation")
        a.message should be("Unresolved reference 'A'")
        a.location should be(Some("file://amf-cli/shared/src/test/resources/references/contexts/library.raml"))
        a.position.map(_.range) should be(Some(Range((4, 5), (4, 6))))
      },
      c => {
        c.severityLevel should be("Violation")
        c.message should be("Unresolved reference 'C'")
        c.location should be(Some("file://amf-cli/shared/src/test/resources/references/contexts/nested.raml"))
        c.position.map(_.range) should be(Some(Range((6, 5), (6, 6))))
      }
    )
  }

  private def validate(file: String, fixture: (AMFValidationResult => Unit)*) = {
    val eh     = DefaultErrorHandler()
    val config = RAMLConfiguration.RAML10().withErrorHandlerProvider(() => eh)
    config
      .baseUnitClient()
      .parse(file)
      .map(_.baseUnit)
      .map { _ =>
        val report = eh.getResults.distinct
        if (report.size == fixture.size) {
          fixture.zip(report).foreach {
            case (fn, result) => fn(result)
          }
        } else fail(s"Report and fixture sizes are different!\nActual:\n${report.map(_.toString).mkString("\n")}")
        Succeeded
      }
  }

}
