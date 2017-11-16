package amf.parser

import amf.compiler.AMFCompiler
import amf.remote._
import amf.unsafe.PlatformSecrets
import amf.validation.{SeverityLevels, Validation}
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers._

import scala.concurrent.ExecutionContext

/**
  * Created by pedro.colunga on 10/10/17.
  */
class ForwardReferencesTest extends AsyncFunSuite with PlatformSecrets {

  private val basePath = "file://shared/src/test/resources/upanddown/"

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Test reference not found exception on property shape") {
    val validation = Validation(platform)
    AMFCompiler(basePath + "forward-references-types-error.raml", platform, RamlYamlHint, validation)
      .build()
      .map { _ =>
        validation.aggregatedReport should not be empty
        validation.aggregatedReport.head.level should be(SeverityLevels.VIOLATION)
        validation.aggregatedReport.head.message should be("Could not resolve shape: UndefinedType")
      }
  }

  test("Test reference not found exception on expression") {
    val validation = Validation(platform)
    AMFCompiler(basePath + "forward-references-types-error-expression.raml", platform, RamlYamlHint, validation)
      .build()
      .map { _ =>
        validation.aggregatedReport should not be empty
        validation.aggregatedReport.head.level should be(SeverityLevels.VIOLATION)
        validation.aggregatedReport.head.message should be("Could not resolve shape: UndefinedType")
      }
  }
}
