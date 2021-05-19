package amf.parser

import amf.client.parse.DefaultErrorHandler
import amf.core.parser.Range
import amf.core.remote.Raml10YamlHint
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.AMFValidationResult
import amf.facades.{AMFCompiler, Validation}
import org.scalatest.Matchers._
import org.scalatest.{AsyncFunSuite, Succeeded}

import scala.concurrent.ExecutionContext

/**
  * Created by pedro.colunga on 10/10/17.
  */
class ForwardReferencesTest extends AsyncFunSuite with PlatformSecrets {

  private val referencesPath = "file://amf-client/shared/src/test/resources/references/"
  private val basePath       = "file://amf-client/shared/src/test/resources/upanddown/"

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Test reference not found exception on property shape") {
    validate(
      basePath + "forward-references-types-error.raml",
      undefined => {
        undefined.severityLevel should be("Violation")
        undefined.message should be("Unresolved reference 'UndefinedType'")
        undefined.location should be(
          Some("file://amf-client/shared/src/test/resources/upanddown/forward-references-types-error.raml"))
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
          Some("file://amf-client/shared/src/test/resources/upanddown/forward-references-types-error-expression.raml"))
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
          Some("file://amf-client/shared/src/test/resources/upanddown/forward-references-types-error-array.raml"))
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
        a.location should be(Some("file://amf-client/shared/src/test/resources/references/contexts/library.raml"))
        a.position.map(_.range) should be(Some(Range((4, 5), (4, 6))))
      },
      c => {
        c.severityLevel should be("Violation")
        c.message should be("Unresolved reference 'C'")
        c.location should be(Some("file://amf-client/shared/src/test/resources/references/contexts/nested.raml"))
        c.position.map(_.range) should be(Some(Range((6, 5), (6, 6))))
      }
    )
  }

  private def validate(file: String, fixture: (AMFValidationResult => Unit)*) = {
    val eh = DefaultErrorHandler()
    Validation(platform).flatMap { _ =>
      AMFCompiler(file, platform, Raml10YamlHint, eh = eh)
        .build()
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

}
