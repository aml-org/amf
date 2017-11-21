package amf.parser

import amf.compiler.AMFCompiler
import amf.remote._
import amf.unsafe.PlatformSecrets
import amf.validation.{AMFValidationResult, Validation}
import org.scalatest.Matchers._
import org.scalatest.{AsyncFunSuite, Succeeded}

import scala.concurrent.ExecutionContext

/**
  * Created by pedro.colunga on 10/10/17.
  */
class ForwardReferencesTest extends AsyncFunSuite with PlatformSecrets {

  private val basePath       = "file://shared/src/test/resources/upanddown/"
  private val referencesPath = "file://shared/src/test/resources/references/"

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Test reference not found exception on property shape") {
    validate(
      basePath + "forward-references-types-error.raml",
      undefined => {
        undefined.level should be("Violation")
        undefined.message should be(
          "Unresolved reference UndefinedType from root context file://shared/src/test/resources/upanddown/forward-references-types-error.raml")
        undefined.position.map(_.range) should be(Some(Range((8, 14), (8, 27))))
      }
    )
  }

  test("Test reference not found exception on expression") {
    validate(
      basePath + "forward-references-types-error-expression.raml",
      undefined => {
        undefined.level should be("Violation")
        undefined.message should be(
          "Unresolved reference UndefinedType from root context file://shared/src/test/resources/upanddown/forward-references-types-error-expression.raml")
        undefined.position.map(_.range) should be(Some(Range((8, 14), (8, 40))))
      },
      _ => {
        // todo duplicate error?
      }
    )
  }

  test("Test reference not found exception on array") {
    validate(
      basePath + "forward-references-types-error-array.raml",
      undefined => {
        undefined.level should be("Violation")
        undefined.message should be(
          "Unresolved reference UndefinedType from root context file://shared/src/test/resources/upanddown/forward-references-types-error-array.raml")
        undefined.position.map(_.range) should be(Some(Range((3, 26), (3, 39))))
      }
    )
  }

  test("Test complex contexts") {
    validate(
      referencesPath + "contexts/api.raml",
      a => {
        a.level should be("Violation")
        a.message should be(
          "Unresolved reference A from root context file://shared/src/test/resources/references/contexts/library.raml")
        a.position.map(_.range) should be(Some(Range((4, 5), (4, 6))))
      },
      c => {
        c.level should be("Violation")
        c.message should be(
          "Unresolved reference C from root context file://shared/src/test/resources/references/contexts/api.raml")
        c.position.map(_.range) should be(Some(Range((6, 5), (6, 6))))
      }
    )
  }

  private def validate(file: String, fixture: (AMFValidationResult => Unit)*) = {
    val validation = Validation(platform)
    AMFCompiler(file, platform, RamlYamlHint, validation)
      .build()
      .map { _ =>
        val report = validation.aggregatedReport
        report.size should be(fixture.size)
        fixture.zip(report).foreach {
          case (fn, result) => fn(result)
        }
        Succeeded
      }
  }

}
