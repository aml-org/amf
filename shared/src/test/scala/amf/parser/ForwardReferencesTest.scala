package amf.parser

import amf.compiler.AMFCompiler
import amf.io.BuildCycleTests
import amf.remote._
import amf.unsafe.PlatformSecrets
import amf.validation.Validation
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

/**
  * Created by pedro.colunga on 10/10/17.
  */
class ForwardReferencesTest extends AsyncFunSuite with PlatformSecrets {

  private val basePath = "file://shared/src/test/resources/upanddown/"

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Test reference not found exception on property shape") {
    recoverToSucceededIf[Exception] {
      AMFCompiler(basePath + "forward-references-types-error.raml", platform, RamlYamlHint, Validation(platform))
        .build()
    }
  }

  test("Test reference not found exception on expression") {
    recoverToSucceededIf[Exception] {
      AMFCompiler(basePath + "forward-references-types-error-expression.raml", platform, RamlYamlHint, Validation(platform))
        .build()
    }
  }
}
