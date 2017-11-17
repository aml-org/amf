package amf.parser

import amf.ProfileNames
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
  private val referencesPath = "file://shared/src/test/resources/references/"

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Test reference not found exception on property shape") {
    val validation = Validation(platform)
    AMFCompiler(basePath + "forward-references-types-error.raml", platform, RamlYamlHint, validation)
      .build()
      .map { _ =>
        validation.aggregatedReport should not be empty
        validation.aggregatedReport.head.level should be(SeverityLevels.VIOLATION)
        validation.aggregatedReport.head.message should be("Unresolved reference UndefinedType from root context file://shared/src/test/resources/upanddown/forward-references-types-error.raml")
      }
  }

  test("Test reference not found exception on expression") {
    val validation = Validation(platform)
    AMFCompiler(basePath + "forward-references-types-error-expression.raml", platform, RamlYamlHint, validation)
      .build()
      .map { _ =>
        validation.aggregatedReport should not be empty
        validation.aggregatedReport.head.level should be(SeverityLevels.VIOLATION)
        validation.aggregatedReport.head.message should be("Unresolved reference UndefinedType from root context file://shared/src/test/resources/upanddown/forward-references-types-error-expression.raml")
      }
  }

  test("Test complex contexts") {
    val validation = Validation(platform)
    AMFCompiler(referencesPath + "contexts/api.raml", platform, RamlYamlHint, validation)
      .build()
      .flatMap { model =>
        validation.validate(model, ProfileNames.RAML)
      }.map { report =>
      assert(!report.conforms)
      assert(report.results.length == 2)
      assert(report.results.head.message == "Unresolved reference A from root context file://shared/src/test/resources/references/contexts/library.raml")
      assert(report.results.last.message == "Unresolved reference C from root context file://shared/src/test/resources/references/contexts/api.raml")
    }
  }
}
