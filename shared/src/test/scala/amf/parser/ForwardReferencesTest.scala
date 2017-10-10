package amf.parser

import amf.io.TmpTests
import org.scalatest.AsyncFunSuite
import amf.client.GenerationOptions
import amf.common.Tests.checkDiff
import amf.compiler.AMFCompiler
import amf.dumper.AMFDumper
import amf.io.TmpTests
import amf.remote._
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.ExecutionContext

/**
  * Created by pedro.colunga on 10/10/17.
  */
class ForwardReferencesTest extends AsyncFunSuite with TmpTests {

  val basePath = "file://shared/src/test/resources/upanddown/"

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Test reference not found exception on property shape") {
    recoverToSucceededIf[Exception] {
      AMFCompiler(basePath + "forward-references-types-error.raml", platform, RamlYamlHint)
        .build()
    }
  }

  test("Test reference not found exception on expression") {
    recoverToSucceededIf[Exception] {
      AMFCompiler(basePath + "forward-references-types-error-expression.raml", platform, RamlYamlHint)
        .build()
    }
  }
}
