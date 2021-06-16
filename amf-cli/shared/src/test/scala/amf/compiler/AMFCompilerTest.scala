package amf.compiler

import amf.apicontract.client.scala.config.{AMFConfiguration, AsyncAPIConfiguration, WebAPIConfiguration}
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.common.validation.Raml10Profile
import amf.core.client.scala.errorhandling.{DefaultErrorHandler, IgnoringErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.parse.document.{SyamlParsedDocument, UnspecifiedReference}
import amf.core.client.scala.validation.AMFValidator
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.remote.Syntax.{Syntax, Yaml}
import amf.core.internal.remote._
import org.scalatest.Matchers._
import org.scalatest.{Assertion, AsyncFunSuite}
import org.yaml.model.{YMap, YMapEntry}

import scala.concurrent.ExecutionContext

class AMFCompilerTest extends AsyncFunSuite with CompilerTestBuilder {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  override def defaultConfig: AMFConfiguration =
    super.defaultConfig.withErrorHandlerProvider(() => IgnoringErrorHandler)
  test("Api (raml)") {
    build("file://amf-cli/shared/src/test/resources/tck/raml-1.0/Api/test003/api.raml", Raml10YamlHint) map assertDocument
  }

  test("Vocabulary") {
    build("file://amf-cli/shared/src/test/resources/vocabularies2/production/raml_doc.yaml", VocabularyYamlHint) map {
      _ should not be null
    }
  }

  test("Api (oas)") {
    build("file://amf-cli/shared/src/test/resources/tck/raml-1.0/Api/test003/api.openapi", Oas20JsonHint) map assertDocument
  }

  test("Api (amf)") {
    build("file://amf-cli/shared/src/test/resources/tck/raml-1.0/Api/test003/api.jsonld", AmfJsonHint) map assertDocument
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
          .withErrorHandlerProvider(() => UnhandledErrorHandler), // TODO ARM then default should not throw exception?
        None
      )
    } map { ex =>
      assert(ex.getMessage.contains(
        s"Cyclic found following references file://amf-cli/shared/src/test/resources/reference-itself.raml -> file://amf-cli/shared/src/test/resources/reference-itself.raml"))
    }
  }

  test("Cache duplicate imports") {
    val cache = new TestCache()
    build("file://amf-cli/shared/src/test/resources/input-duplicate-includes.json", Oas20JsonHint, cache = Some(cache)) map {
      _ =>
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
    val eh = DefaultErrorHandler()
    val amfConfig =
      WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()).withErrorHandlerProvider(() => eh)
    build("file://amf-cli/shared/src/test/resources/non-exists-include.raml", Raml10YamlHint, amfConfig, None)
      .flatMap(bu => {
        AMFValidator.validate(bu, Raml10Profile, amfConfig)
      })
      .map(r => {
        assert(!r.conforms)
        assert(r.results.lengthCompare(2) == 0)
        assert(
          r.results.head.message
            .contains("amf-cli/shared/src/test/resources/nonExists.raml"))
        assert(
          r.results.head.message
            .contains("such file or directory")) // temp, assert better the message for js and jvm
      })
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
      assert(ex.getMessage.contains(
        s"Cyclic found following references file://amf-cli/shared/src/test/resources/input-cycle.${syntax.extension} -> file://amf-cli/shared/src/test/resources/includes/include-cycle.${syntax.extension} -> file://amf-cli/shared/src/test/resources/input-cycle.${syntax.extension}"))
    }
  }

  private class TestCache extends Cache {
    def assertCacheSize(expectedSize: Int): Assertion = {
      if (size != expectedSize) {
        cache.foreach {
          case (a, b) =>
            println(s"$a -> ${System.identityHashCode(b)}")
        }
      }
      size should be(expectedSize)
    }
  }
}
