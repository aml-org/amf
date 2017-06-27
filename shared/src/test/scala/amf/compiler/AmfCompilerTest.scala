package amf.compiler

import amf.client.Handler
import amf.common.{AMFASTLink, AMFToken}
import amf.lexer.Token
import amf.parser._
import amf.remote.{Cache, OasJsonHint, RamlYamlHint}
import amf.unsafe.PlatformSecrets
import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.Matchers._

import scala.util.{Failure, Success, Try}

/**
  * Created by hernan.najles on 6/23/17.
  */
class AmfCompilerTest extends FunSuite with PlatformSecrets {

  private def callback(handler: Handler, url: String, containerType: ContainerType)(t: Try[ASTNode[_ <: Token]]) =
    t match {
      case Success(value)     => handler.success(Container(value, url, containerType))
      case Failure(exception) => handler.error(exception)
    }

  test("test simple import ok") {

    AMFCompiler("file://shared/src/test/resources/input.json", platform, Option(OasJsonHint))
      .build()
      .onComplete(callback(
        new Handler {
          override def success(doc: Container): Unit = {
            println("Successfully parsed. Type `:ast` or `:generate json` or `:generate yaml`")
            //TODO assert what?
          }

          override def error(exception: Throwable): Unit = {
            fail(exception.getMessage)
          }
        },
        "",
        Document
      ))

  }

  test("test cyclic reference in imports") {

    AMFCompiler("file://shared/src/test/resources/inputWithCycle.json", platform, Option(OasJsonHint))
      .build()
      .onComplete(callback(
        new Handler {
          override def success(doc: Container): Unit = {
            println("Successfully parsed. Type `:ast` or `:generate json` or `:generate yaml`")

          }

          override def error(exception: Throwable): Unit = {
            exception.getMessage should be("Url has cycles(file://shared/src/test/resources/inputWithCycle.json)")
          }
        },
        "",
        Document
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
          override def success(doc: Container): Unit = {
            println("Successfully parsed. Type `:ast` or `:generate json` or `:generate yaml`")
            cache.assertCacheSize(2)
          }

          override def error(exception: Throwable): Unit = {
            fail(exception)
          }
        },
        "",
        Document
      ))
  }

  test("test cache two diff imports") {
    val cache = new TestCache()
    AMFCompiler("file://shared/src/test/resources/input.json", platform, Option(OasJsonHint), cache = Some(cache))
      .build()
      .onComplete(callback(
        new Handler {
          override def success(doc: Container): Unit = {
            println("Successfully parsed. Type `:ast` or `:generate json` or `:generate yaml`")
            cache.assertCacheSize(3)
          }

          override def error(exception: Throwable): Unit = {
            fail(exception)
          }
        },
        "",
        Document
      ))
  }

  test("test library") {

    val cache = new TestCache()
    AMFCompiler("file://shared/src/test/resources/rootWithLibrary.raml",
                platform,
                Option(RamlYamlHint),
                cache = Some(cache))
      .build()
      .onComplete(callback(
        new Handler {
          override def success(doc: Container): Unit = {
            println("Successfully parsed. Type `:ast` or `:generate json` or `:generate yaml`")
            doc.`type` should be(Document)
            val root = doc.root
            root.children.size should be(1)
            val bodyMap = root.children.head
            bodyMap.children.size should be(2)
            val usesEntry = bodyMap.children(1)
            usesEntry.children.length should be(2)
            usesEntry.children.head.content should be("uses")
            val libraryMap = usesEntry.children(1)
            libraryMap.children.length should be(2)
            val libraryEntry = libraryMap.children.head
            libraryEntry.children.size should be(2)

            libraryEntry.children(1) shouldBe a[AMFASTLink]
            val linkNode = libraryEntry.children(1)
            val link     = linkNode.asInstanceOf[AMFASTLink]
            link.target.`type` should be(Library)
            link.target.root.children.length should be(1)
            link.target.root.children.head.`type` should be(AMFToken.SequenceToken)
            link.target.root.children.head.children.length should be(2)
          }

          override def error(exception: Throwable): Unit = {
            fail(exception)
          }
        },
        "",
        Document
      ))
  }

  class TestCache extends Cache {
    def assertCacheSize(expectedSize: Int): Unit = {
      map.size should be(expectedSize)
    }
  }
}
