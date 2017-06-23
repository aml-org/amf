package amf.compiler

import amf.client.Handler
import amf.lexer.Token
import amf.parser.{ASTNode, Document}
import amf.remote.{Cache, OasJsonHint}
import amf.unsafe.PlatformSecrets
import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.Matchers._

import scala.util.{Failure, Success, Try}

/**
  * Created by hernan.najles on 6/23/17.
  */
class AmfCompilerTest extends FunSuite with PlatformSecrets {

  private def callback(handler: Handler, url: String)(t: Try[ASTNode[_ <: Token]]) = t match {
    case Success(value)     => handler.success(Document(value, url))
    case Failure(exception) => handler.error(exception)
  }

  test("test simple import ok") {

    AMFCompiler("file://shared/src/test/resources/input.json", platform, Option(OasJsonHint))
      .build()
      .onComplete(callback(
        new Handler {
          override def success(doc: Document): Unit = {
            println("Successfully parsed. Type `:ast` or `:generate json` or `:generate yaml`")
            //TODO assert what?
          }

          override def error(exception: Throwable): Unit = {
            fail(exception.getMessage)
          }
        },
        ""
      ))

  }

  test("test cyclic reference in imports") {

    AMFCompiler("file://shared/src/test/resources/inputWithCycle.json", platform, Option(OasJsonHint))
      .build()
      .onComplete(callback(
        new Handler {
          override def success(doc: Document): Unit = {
            println("Successfully parsed. Type `:ast` or `:generate json` or `:generate yaml`")

          }

          override def error(exception: Throwable): Unit = {
            exception.getMessage should be("Url has cycles(file://shared/src/test/resources/inputWithCycle.json)")
          }
        },
        ""
      ))
  }

  test("test cache some import twice") {
    val cache = new TestCache()
    AMFCompiler("file://shared/src/test/resources/inputWithDuplicateImports.json",
                platform,
                Option(OasJsonHint),
                cache = Some(cache))
      .build()
      .onComplete(callback(
        new Handler {
          override def success(doc: Document): Unit = {
            println("Successfully parsed. Type `:ast` or `:generate json` or `:generate yaml`")
            cache.assertCacheSize(2)
          }

          override def error(exception: Throwable): Unit = {
            fail(exception)
          }
        },
        ""
      ))
  }

  test("test cache two diff imports") {
    val cache = new TestCache()
    AMFCompiler("file://shared/src/test/resources/input.json", platform, Option(OasJsonHint), cache = Some(cache))
      .build()
      .onComplete(callback(
        new Handler {
          override def success(doc: Document): Unit = {
            println("Successfully parsed. Type `:ast` or `:generate json` or `:generate yaml`")
            cache.assertCacheSize(3)
          }

          override def error(exception: Throwable): Unit = {
            fail(exception)
          }
        },
        ""
      ))
  }

  class TestCache extends Cache {
    def assertCacheSize(expectedSize: Int): Unit = {
      map.size should be(expectedSize)
    }
  }
}
