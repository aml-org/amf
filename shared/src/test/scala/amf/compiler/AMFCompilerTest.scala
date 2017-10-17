package amf.compiler

import amf.document.{BaseUnit, Document}
import amf.domain.WebApi
import amf.exception.CyclicReferenceException
import amf.parser.{YMapOps, YValueOps}
import amf.remote.Syntax.{Json, Syntax, Yaml}
import amf.remote._
import amf.unsafe.PlatformSecrets
import org.scalatest.Matchers._
import org.scalatest.{Assertion, AsyncFunSuite}
import org.yaml.model.YMapEntry

import scala.concurrent.ExecutionContext

/**
  *
  */
class AMFCompilerTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Api (raml)") {
    AMFCompiler("file://shared/src/test/resources/tck/raml-1.0/Api/test003/api.raml", platform, RamlYamlHint)
      .build() map assertDocument
  }

  test("Vocabulary") {
    AMFCompiler("file://shared/src/test/resources/vocabularies/raml_doc.raml", platform, RamlYamlHint)
      .build()
      .onComplete(unit => {
        assert(unit.isSuccess)
      })

    true shouldBe (true)
  }

  test("Api (oas)") {
    AMFCompiler("file://shared/src/test/resources/tck/raml-1.0/Api/test003/api.openapi", platform, OasJsonHint)
      .build() map assertDocument
  }

  test("Api (amf)") {
    AMFCompiler("file://shared/src/test/resources/tck/raml-1.0/Api/test003/api.jsonld", platform, AmfJsonHint)
      .build() map assertDocument
  }

  test("Simple import") {
    AMFCompiler("file://shared/src/test/resources/input.json", platform, OasJsonHint)
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
                OasJsonHint,
                cache = Some(cache))
      .build() map { _ =>
      cache.assertCacheSize(2)
    }
  }

  test("Cache different imports") {
    val cache = new TestCache()
    AMFCompiler("file://shared/src/test/resources/input.json", platform, OasJsonHint, cache = Some(cache))
      .build() map { _ =>
      cache.assertCacheSize(3)
    }
  }

  test("Libraries (raml)") {
    AMFCompiler("file://shared/src/test/resources/modules.raml", platform, RamlYamlHint)
      .root() map {
      case Root(root, _, references, _, _) =>
        val body = root.document.value.get.toMap
        body.entries.size should be(2)
        assertUses(body.key("uses").get, references.map(_.baseUnit))
    }
  }

  test("Libraries (oas)") {
    AMFCompiler("file://shared/src/test/resources/modules.json", platform, OasJsonHint)
      .root() map {
      case Root(root, _, references, _, _) =>
        val body = root.document.value.get.toMap
        body.entries.size should be(3)
        assertUses(body.key("x-uses").get, references.map(_.baseUnit))
    }
  }

  private def assertDocument(unit: BaseUnit): Assertion = unit match {
    case d: Document =>
      d.encodes.asInstanceOf[WebApi].host should be("api.example.com")
      d.encodes.asInstanceOf[WebApi].name should be("test")
  }

  private def assertUses(uses: YMapEntry, references: Seq[BaseUnit]) = {
    uses.key.value.toScalar.text should include("uses")

    val libraries = uses.value.value.toMap

    libraries.map.values.foreach(value => {
      value.asString should include("libraries")
    })

    libraries.entries.length should be(references.size)
  }

  private def assertCycles(syntax: Syntax, hint: Hint) = {
    recoverToExceptionIf[CyclicReferenceException] {
      AMFCompiler(s"file://shared/src/test/resources/input-cycle.${syntax.extension}", platform, hint)
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
