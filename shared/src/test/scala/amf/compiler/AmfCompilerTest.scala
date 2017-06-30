package amf.compiler

import amf.common.AMFToken.Root
import amf.common.{AMFASTLink, AMFToken}
import amf.parser._
import amf.remote._
import amf.unsafe.PlatformSecrets
import org.scalatest.{Assertion, AsyncFlatSpec}
import org.scalatest.Matchers._

import scala.concurrent.ExecutionContext

/**
  * Created by hernan.najles on 6/23/17.
  */
class AmfCompilerTest extends AsyncFlatSpec with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  "test simple import" should "succeed " in {

    val eventualTuple = AMFCompiler("file://shared/src/test/resources/input.json", platform, Option(OasJsonHint))
      .build()

    eventualTuple map { t =>
      t._1.`type` should be(Root)
    }
  }

  "reference in imports with cycles" should "fail with message" in {

    val eventualTuple = recoverToExceptionIf[Exception] {
      AMFCompiler("file://shared/src/test/resources/inputWithCycle.json", platform, Option(OasJsonHint))
        .build()
    }
    eventualTuple map { ex =>
      assert(
        ex.getMessage ==
          "Url has cycles(file://shared/src/test/resources/inputWithCycle.json)")
    }

  }

  "test cache some import twice" should "succeed" in {
    val cache = new TestCache()
    val eventualTuple = AMFCompiler("file://shared/src/test/resources/inputWithDuplicateImports.json",
                                    platform,
                                    Option(OasJsonHint),
                                    cache = Some(cache))
      .build()

    eventualTuple map { _ =>
      cache.assertCacheSize(2)
    }
  }

  "test cache two diff imports" should "succeed" in {
    val cache = new TestCache()
    val eventualTuple =
      AMFCompiler("file://shared/src/test/resources/input.json", platform, Option(OasJsonHint), cache = Some(cache))
        .build()

    eventualTuple map { _ =>
      cache.assertCacheSize(3)
    }
  }

  "test library" should "succeed" in {

    val cache = new TestCache()
    val eventualTuple = AMFCompiler("file://shared/src/test/resources/rootWithLibrary.raml",
                                    platform,
                                    Option(RamlYamlHint),
                                    cache = Some(cache))
      .build()
      .map(a => AMFUnit(a._1, "file://shared/src/test/resources/rootWithLibrary.raml", Document, a._2))
    eventualTuple map { doc =>
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
      link.target.`type` should be(Module)
      link.target.root.children.length should be(1)
      link.target.root.children.head.`type` should be(AMFToken.SequenceToken)
      link.target.root.children.head.children.length should be(2)
    }

  }

  class TestCache extends Cache {
    def assertCacheSize(expectedSize: Int): Assertion = {
      size should be(expectedSize)
    }
  }
}
