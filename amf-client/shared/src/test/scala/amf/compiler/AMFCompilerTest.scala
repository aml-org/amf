package amf.compiler

import amf.{RAMLStyle, Raml10Profile}
import amf.client.plugins.{AMFFeaturePlugin, AMFPlugin}
import amf.client.remote.Content
import amf.core.Root
import amf.core.model.document.{BaseUnit, Document}
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.parser.{UnspecifiedReference, _}
import amf.core.remote.Syntax.{Syntax, Yaml}
import amf.core.remote._
import amf.core.services.RuntimeCompiler
import amf.facades.Validation
import amf.plugins.domain.webapi.models.api.WebApi
import org.scalatest.Matchers._
import org.scalatest.{Assertion, AsyncFunSuite}
import org.yaml.model.{YMap, YMapEntry}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class AMFCompilerTest extends AsyncFunSuite with CompilerTestBuilder {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Api (raml)") {
    build("file://amf-client/shared/src/test/resources/tck/raml-1.0/Api/test003/api.raml", Raml10YamlHint) map assertDocument
  }

  test("Vocabulary") {
    build("file://amf-client/shared/src/test/resources/vocabularies2/production/raml_doc.yaml", VocabularyYamlHint) map {
      _ should not be null
    }
  }

  test("Api (oas)") {
    build("file://amf-client/shared/src/test/resources/tck/raml-1.0/Api/test003/api.openapi", Oas20JsonHint) map assertDocument
  }

  test("Api (amf)") {
    build("file://amf-client/shared/src/test/resources/tck/raml-1.0/Api/test003/api.jsonld", AmfJsonHint) map assertDocument
  }

  test("Simple import") {
    build("file://amf-client/shared/src/test/resources/input.json", Oas20JsonHint) map {
      _ should not be null
    }
  }

  test("Reference in imports with cycles (yaml)") {
    assertCycles(Yaml, Raml10YamlHint)
  }

  test("Simple cicle (yaml)") {
    recoverToExceptionIf[Exception] {
      Validation(platform)
        .flatMap(
          v =>
            build(s"file://amf-client/shared/src/test/resources/reference-itself.raml",
                  Raml10YamlHint,
                  validation = Some(v),
                  eh = Some(UnhandledParserErrorHandler)))
    } map { ex =>
      assert(ex.getMessage.contains(
        s"Cyclic found following references file://amf-client/shared/src/test/resources/reference-itself.raml -> file://amf-client/shared/src/test/resources/reference-itself.raml"))
    }
  }

  test("Cache duplicate imports") {
    val cache = new TestCache()
    build("file://amf-client/shared/src/test/resources/input-duplicate-includes.json",
          Oas20JsonHint,
          cache = Some(cache)) map { _ =>
      cache.assertCacheSize(2)
    }
  }

  test("Cache different imports") {
    val cache = new TestCache()
    build("file://amf-client/shared/src/test/resources/input.json", Oas20JsonHint, cache = Some(cache)) map { _ =>
      cache.assertCacheSize(3)
    }
  }

  test("Libraries (raml)") {
    compiler("file://amf-client/shared/src/test/resources/modules.raml", Raml10YamlHint)
      .flatMap(_.root()) map {
      case Root(root, _, _, references, UnspecifiedReference, _) =>
        val body = root.asInstanceOf[SyamlParsedDocument].document.as[YMap]
        body.entries.size should be(2)
        assertUses(body.key("uses").get, references.map(_.unit))
      case Root(root, _, _, refKind, _, _) => throw new Exception(s"Unespected type of referenceKind parsed $refKind")
    }
  }

  test("Libraries (oas)") {
    compiler("file://amf-client/shared/src/test/resources/modules.json", Oas20JsonHint)
      .flatMap(_.root()) map {
      case Root(root, _, _, references, UnspecifiedReference, _) =>
        val body = root.asInstanceOf[SyamlParsedDocument].document.as[YMap]
        body.entries.size should be(3)
        assertUses(body.key("x-amf-uses").get, references.map(_.unit))
      case Root(root, _, _, refKind, _, _) => throw new Exception(s"Unespected type of referenceKind parsed $refKind")
    }
  }

  test("Non existing included file") {
    Validation(platform)
      .flatMap(v => {

        build("file://amf-client/shared/src/test/resources/non-exists-include.raml",
              Raml10YamlHint,
              validation = Some(v))
          .flatMap(bu => {
            v.validate(bu, Raml10Profile, RAMLStyle)
          })
      })
      .map(r => {
        assert(!r.conforms)
        assert(r.results.lengthCompare(2) == 0)
        assert(
          r.results.last.message
            .contains("amf-client/shared/src/test/resources/nonExists.raml"))
        assert(
          r.results.last.message
            .contains("such file or directory")) // temp, assert better the message for js and jvm
      })
  }

  test("Feature plugin test") {
    val url = "file://amf-client/shared/src/test/resources/tck/raml-1.0/Api/test003/api.raml"
    amf.core.AMF.init()
    object FeaturePlugin extends AMFFeaturePlugin {

      override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = Future { this }

      var invocations: mutable.ListBuffer[String] = mutable.ListBuffer()

      override def dependencies(): Seq[AMFPlugin] = Nil

      override val ID: String = "Test Feature Plugin"

      override def onBeginParsingInvocation(url: String, mediaType: Option[String]): Unit = {
        invocations += "begin_parsing_invocation"
      }

      override def onBeginDocumentParsing(url: String, content: Content, referenceKind: ReferenceKind): Content = {
        invocations += "begin_document_parsing"
        content
      }

      override def onSyntaxParsed(url: String, ast: ParsedDocument): ParsedDocument = {
        invocations += "syntax_parsed"
        ast
      }

      override def onModelParsed(url: String, unit: BaseUnit): BaseUnit = {
        invocations += "model_parsed"
        unit
      }

      override def onFinishedParsingInvocation(url: String, unit: BaseUnit): BaseUnit = {
        invocations += "finished_parsing"
        unit
      }
    }
    amf.core.AMF.registerPlugin(FeaturePlugin)
    FeaturePlugin.init() flatMap { _ =>
      RuntimeCompiler(url, Some("application/yaml"), Some(Raml10.name), Context(platform), cache = Cache()) map { _ =>
        val allPhases = Seq("begin_parsing_invocation",
                            "begin_document_parsing",
                            "syntax_parsed",
                            "model_parsed",
                            "finished_parsing").foldLeft(true) {
          case (acc, phase) =>
            acc && FeaturePlugin.invocations.contains(phase)
        }
        assert(allPhases)
      }
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
      Validation(platform)
        .flatMap(v => {
          build(s"file://amf-client/shared/src/test/resources/input-cycle.${syntax.extension}",
                hint,
                validation = Some(v),
                eh = Some(UnhandledParserErrorHandler))
        })
    } map { ex =>
      assert(ex.getMessage.contains(
        s"Cyclic found following references file://amf-client/shared/src/test/resources/input-cycle.${syntax.extension} -> file://amf-client/shared/src/test/resources/includes/include-cycle.${syntax.extension} -> file://amf-client/shared/src/test/resources/input-cycle.${syntax.extension}"))
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
