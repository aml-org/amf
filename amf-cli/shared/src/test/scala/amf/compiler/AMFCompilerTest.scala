package amf.compiler

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.{AMFConfiguration, RAMLConfiguration}
import amf.core.client.scala.errorhandling.{DefaultErrorHandler, IgnoringErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.parse.document.{SyamlParsedDocument, UnspecifiedReference}
import amf.core.client.scala.validation.AMFValidator
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.remote.Syntax.{Syntax, Yaml}
import amf.core.internal.remote._
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers
import org.yaml.model.{YMap, YMapEntry}

class AMFCompilerTest extends AsyncFunSuite with Matchers with CompilerTestBuilder {

  override def defaultConfig: AMFConfiguration =
    super.defaultConfig.withErrorHandlerProvider(() => IgnoringErrorHandler)
  test("Api (raml)") {
    build(
      "file://amf-cli/shared/src/test/resources/tck/raml-1.0/Api/test003/api.raml",
      Raml10YamlHint
    ) map assertDocument
  }

  test("Vocabulary") {
    build("file://amf-cli/shared/src/test/resources/vocabularies2/production/raml_doc.yaml", VocabularyYamlHint) map {
      _ should not be null
    }
  }

  test("Api (oas)") {
    build(
      "file://amf-cli/shared/src/test/resources/tck/raml-1.0/Api/test003/api.openapi",
      Oas20JsonHint
    ) map assertDocument
  }

  test("Api (amf)") {
    build(
      "file://amf-cli/shared/src/test/resources/tck/raml-1.0/Api/test003/api.jsonld",
      AmfJsonHint
    ) map assertDocument
  }

  test("Simple import") {
    build("file://amf-cli/shared/src/test/resources/input.json", Oas20JsonHint) map {
      _ should not be null
    }
  }

  test("Reference in imports with cycles (yaml)") {
    assertCycles(Yaml, Raml10YamlHint)
  }

  test("Simple cicle (yaml)") {
    recoverToExceptionIf[Exception] {

      build(
        s"file://amf-cli/shared/src/test/resources/reference-itself.raml",
        Raml10YamlHint,
        defaultConfig
          .withErrorHandlerProvider(() => UnhandledErrorHandler),
        None
      )
    } map { ex =>
      assert(
        ex.getMessage.contains(
          s"Cyclic found following references file://amf-cli/shared/src/test/resources/reference-itself.raml -> file://amf-cli/shared/src/test/resources/reference-itself.raml"
        )
      )
    }
  }

  test("Cache duplicate imports") {
    val cache = new TestCache()
    build(
      "file://amf-cli/shared/src/test/resources/input-duplicate-includes.json",
      Oas20JsonHint,
      cache = Some(cache)
    ) map { _ =>
      cache.assertCacheSize(2)
    }
  }

  test("Cache different imports") {
    val cache = new TestCache()
    build("file://amf-cli/shared/src/test/resources/input.json", Oas20JsonHint, cache = Some(cache)) map { _ =>
      cache.assertCacheSize(3)
    }
  }

  test("Libraries (raml)") {
    compiler("file://amf-cli/shared/src/test/resources/modules.raml", Raml10YamlHint).root() map {
      case Root(root, _, _, references, UnspecifiedReference, _) =>
        val body = root.asInstanceOf[SyamlParsedDocument].document.as[YMap]
        body.entries.size should be(2)
        assertUses(body.key("uses").get, references.map(_.unit))
      case Root(root, _, _, refKind, _, _) => throw new Exception(s"Unespected type of referenceKind parsed $refKind")
    }
  }

  test("Libraries (oas)") {
    compiler("file://amf-cli/shared/src/test/resources/modules.json", Oas20JsonHint).root() map {
      case Root(root, _, _, references, UnspecifiedReference, _) =>
        val body = root.asInstanceOf[SyamlParsedDocument].document.as[YMap]
        body.entries.size should be(3)
        assertUses(body.key("x-amf-uses").get, references.map(_.unit))
      case Root(root, _, _, refKind, _, _) => throw new Exception(s"Unespected type of referenceKind parsed $refKind")
    }
  }

  test("Non existing included file") {
    val eh        = DefaultErrorHandler()
    val amfConfig = RAMLConfiguration.RAML10().withErrorHandlerProvider(() => eh)
    build("file://amf-cli/shared/src/test/resources/non-exists-include.raml", Raml10YamlHint, amfConfig, None)
      .flatMap(bu => {
        AMFValidator.validate(bu, amfConfig)
      })
      .map(r => {
        assert(!r.conforms)
        assert(r.results.lengthCompare(2) == 0)
        assert(
          r.results.head.message
            .contains("amf-cli/shared/src/test/resources/nonExists.raml")
        )
        assert(
          r.results.head.message
            .contains("such file or directory")
        ) // temp, assert better the message for js and jvm
      })
  }

  // APIMF-3402
  test("internal and external ref should be called the same") {
    build("file://amf-cli/shared/src/test/resources/resolution/external-schema-ref/api.yaml", Raml10YamlHint) map {
      bu =>
        val webapi = bu.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
        webapi.endPoints.foreach(endpoint => {
          val name = endpoint.operations.head.request.payloads.head.schema.name.value()
          name shouldBe "payPlanschema"
        })
        webapi should not be null
    }
  }

  private def assertDocument(unit: BaseUnit): Assertion = unit match {
    case d: Document =>
      d.encodes.asInstanceOf[WebApi].servers.headOption.map(_.url.value()).getOrElse("") should be("api.example.com")
      d.encodes.asInstanceOf[WebApi].name.value() should be("test")
  }

  private def assertUses(uses: YMapEntry, references: Seq[BaseUnit]) = {
    uses.key.as[String] should include("uses")

    val libraries = uses.value.as[YMap]

    libraries.map.values.foreach(value => {
      val s: String = value
      s should include("libraries")
    })

    libraries.entries.length should be(references.size)
  }

  private def assertCycles(syntax: Syntax, hint: Hint) = {
    recoverToExceptionIf[Exception] {
      build(
        s"file://amf-cli/shared/src/test/resources/input-cycle.${syntax.extension}",
        hint,
        defaultConfig.withErrorHandlerProvider(() => UnhandledErrorHandler),
        None
      )
    } map { ex =>
      assert(
        ex.getMessage.contains(
          s"Cyclic found following references file://amf-cli/shared/src/test/resources/input-cycle.${syntax.extension} -> file://amf-cli/shared/src/test/resources/includes/include-cycle.${syntax.extension} -> file://amf-cli/shared/src/test/resources/input-cycle.${syntax.extension}"
        )
      )
    }
  }

  private class TestCache extends Cache {
    def assertCacheSize(expectedSize: Int): Assertion = {
      if (size != expectedSize) {
        cache.foreach { case (a, b) =>
          println(s"$a -> ${System.identityHashCode(b)}")
        }
      }
      size should be(expectedSize)
    }
  }
}
