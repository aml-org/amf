package amf.compiler

import amf.common.{AMFAST, AMFASTLink, AMFToken}
import amf.exception.CyclicReferenceException
import amf.parser._
import amf.remote.Syntax.{Json, Syntax, Yaml}
import amf.remote._
import amf.unsafe.PlatformSecrets
import org.scalatest.Matchers._
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.ExecutionContext

/**
  *
  */
class AMFCompilerTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Simple import") {
    AMFCompiler("file://shared/src/test/resources/input.json", platform, Option(OasJsonHint))
      .build() map {
      _ should not be null
    }
  }

  test("Reference in imports with cycles (json)") {
    assertCycles(Json, OasJsonHint)
  }

  test("Reference in imports with cycles (yaml)") {
    assertCycles(Yaml, RamlYamlHint)
  }

  test("Cache duplicate imports") {
    val cache = new TestCache()
    AMFCompiler("file://shared/src/test/resources/input-duplicate-includes.json",
                platform,
                Option(OasJsonHint),
                cache = Some(cache))
      .build() map { _ =>
      cache.assertCacheSize(2)
    }
  }

  test("Cache different imports") {
    val cache = new TestCache()
    AMFCompiler("file://shared/src/test/resources/input.json", platform, Option(OasJsonHint), cache = Some(cache))
      .build() map { _ =>
      cache.assertCacheSize(3)
    }
  }

  test("Libraries (raml)") {
    val cache = new TestCache()
    AMFCompiler("file://shared/src/test/resources/modules.raml", platform, Option(RamlYamlHint), cache = Some(cache))
      .build() map {
      case (root, _) =>
        root.children.size should be(1)
        val bodyMap = root.children.head
        bodyMap.children.size should be(2)
        val uses = bodyMap.children(1)
        assertUses(uses)
    }
  }

  test("Libraries (oas)") {
    val cache = new TestCache()
    AMFCompiler("file://shared/src/test/resources/modules.json", platform, Option(OasJsonHint), cache = Some(cache))
      .build() map {
      case (root, _) =>
        root.children.size should be(1)
        val bodyMap = root.children.head
        bodyMap.children.size should be(3)
        val uses = bodyMap.children(2)
        assertUses(uses)
    }
  }

  private def assertUses(uses: AMFAST) = {
    uses.children.length should be(2)
    uses.children.head.content should include("uses")
    val libraries = uses.children(1)
    libraries.children.length should be(2)

    val library = libraries.children.head
    library.children.size should be(2)
    library.children(1) shouldBe a[AMFASTLink]
    val link = library.children(1).asInstanceOf[AMFASTLink]
    link.target.`type` should be(Module)
    link.target.root.children.length should be(1)
    link.target.root.children.head.`type` should be(AMFToken.SequenceToken)
    link.target.root.children.head.children.length should be(2)
  }

  private def assertCycles(syntax: Syntax, hint: Hint) = {
    recoverToExceptionIf[CyclicReferenceException] {
      AMFCompiler(s"file://shared/src/test/resources/input-cycle.${syntax.extension}", platform, Option(hint))
        .build()
    } map { ex =>
      assert(ex.getMessage ==
        s"Cyclic found following references file://shared/src/test/resources/input-cycle.${syntax.extension} -> file://shared/src/test/resources/includes/include-cycle.${syntax.extension} -> file://shared/src/test/resources/input-cycle.${syntax.extension}")
    }
  }

  private class TestCache extends Cache {
    def assertCacheSize(expectedSize: Int): Assertion = {
      size should be(expectedSize)
    }
  }
}
