package amf.wrapper

import _root_.org.scalatest.{Assertion, AsyncFunSuite}
import amf._
import amf.client.convert.WebApiClientConverters._
import amf.client.model.document.{BaseUnit, Document, TraitFragment}
import amf.client.model.domain.WebApi
import amf.core.unsafe.PlatformSecrets

import scala.concurrent.ExecutionContext

class JsWrapperTests extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Parsing raml 1.0 detect test") {
    AMF.init().toFuture.flatMap { _ =>
      val parser = new RamlParser()
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").toFuture
    } flatMap { assertBaseUnit(_, "file://amf-client/shared/src/test/resources/api/zencoder.raml") }
  }

  test("Parsing raml 0.8 detect test") {
    AMF.init().toFuture.flatMap { _ =>
      val parser = new RamlParser()
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder08.raml").toFuture
    } flatMap { assertBaseUnit(_, "file://amf-client/shared/src/test/resources/api/zencoder08.raml") }
  }

  test("Parsing raml 1.0 test") {
    AMF.init().toFuture.flatMap { _ =>
      val parser = new Raml10Parser()
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").toFuture
    } flatMap { assertBaseUnit(_, "file://amf-client/shared/src/test/resources/api/zencoder.raml") }
  }

  test("Parsing raml 0.8 test") {
    AMF.init().toFuture.flatMap { _ =>
      val parser = new Raml08Parser()
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder08.raml").toFuture
    } flatMap { assertBaseUnit(_, "file://amf-client/shared/src/test/resources/api/zencoder08.raml") }
  }

  test("Generation test") {
    AMF.init().toFuture.flatMap { _ =>
      val parser = new RamlParser()
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").toFuture
    } flatMap { baseUnit =>
      assert(new Raml10Generator().generateString(baseUnit) != "") // TODO: test this properly
      assert(new Oas20Generator().generateString(baseUnit) != "")
      assert(new AmfGraphGenerator().generateString(baseUnit) != "")
    }
  }

  test("Resolution test") {
    AMF.init().toFuture flatMap { _ =>
      val parser = new RamlParser()
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").toFuture
    } flatMap { baseUnit =>
      AMF.validate(baseUnit, ProfileNames.RAML).toFuture.map { report =>
        (baseUnit, report)
      }
    } flatMap {
      case (baseUnit, report) =>
        assert(report.conforms)
        AMF
          .loadValidationProfile("file://amf-client/shared/src/test/resources/api/validation/custom-profile.raml")
          .toFuture
          .map { _ =>
            baseUnit
          }
    } flatMap { baseUnit =>
      AMF.validate(baseUnit, "Banking").toFuture
    } flatMap { custom =>
      assert(!custom.conforms)
    }
  }

  /*  test("Vocabularies test") {
    AMF.init().toFuture flatMap { _ =>
      AMF.registerDialect("file://amf-client/shared/src/test/resources/api/dialects/eng-demos.raml").toFuture
    } flatMap { _ =>
      val parser = new Raml10Parser()
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/examples/libraries/demo.raml").toFuture
    } flatMap { baseUnit =>
      AMF.validate(baseUnit, "Eng Demos 0.1").toFuture.map { report =>
        (report, baseUnit)
      }
    } flatMap {
      case (report, baseUnit) =>
        assert(report.conforms)
        AMF.registerNamespace("eng-demos", "http://mulesoft.com/vocabularies/eng-demos#")
        val elem     = baseUnit.asInstanceOf[Document].encodes
        val speakers = elem.getObjectByPropertyId("eng-demos:speakers")
        assert(speakers.toSeq.nonEmpty)
    }
  }*/

  test("world-music-test") {
    amf.plugins.features.AMFValidation.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().toFuture flatMap { _ =>
      val parser = amf.Core.parser("RAML 1.0", "application/yaml")
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/production/world-music-api/api.raml").toFuture
    } flatMap { model =>
      val locations = model.references().asInternal.map(_.location)
      assert(!locations.contains(null))
    }
  }

  test("banking-api-test") {
    amf.plugins.features.AMFValidation.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().toFuture flatMap { _ =>
      val parser = amf.Core.parser("RAML 1.0", "application/yaml")
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/production/banking-api/api.raml").toFuture
    } flatMap { model =>
      val refs = model.references().asInternal
      assert(!refs.map(_.location).contains(null))
      val traitModel    = refs.find(ref => ref.location.endsWith("traits.raml")).head
      val traitRefs     = traitModel.references
      val firstFragment = traitRefs.head
      assert(firstFragment.location != null)
      assert(firstFragment.asInstanceOf[amf.plugins.document.webapi.model.TraitFragment].encodes != null)
      assert(!traitRefs.map(_.location).contains(null))
    }
  }

  test("banking-api-test 2") {
    amf.plugins.features.AMFValidation.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().toFuture flatMap { _ =>
      val parser = amf.Core.parser("RAML 1.0", "application/yaml")
      parser
        .parseFileAsync("file://amf-client/shared/src/test/resources/production/banking-api/traits/traits.raml")
        .toFuture
    } flatMap { traitModel =>
      val traitRefs     = traitModel.references().asInternal
      val firstFragment = traitRefs.head
      assert(firstFragment.location != null)
      assert(firstFragment.asInstanceOf[amf.plugins.document.webapi.model.TraitFragment].encodes != null)
      assert(!traitRefs.map(_.location).contains(null))
    }
  }

  /*
  test("Vocabulary generation") {
    amf.plugins.document.Vocabularies.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().toFuture flatMap { _ =>
      val vocab = new amf.client.model.domain.Vocabulary()
      vocab
        .withBase("http://test.com/vocab#")
        .withVersion("1.0")
        .withUsage("Just a small sample vocabulary")
        .withExternals(
          Seq(
            new amf.client.model.domain.ExternalVocabularyImport()
              .withName("other")
              .withUri("http://test.com/vocabulary/other#")
          ).toJSArray)
        .withUses(
          Seq(
            new amf.client.model.domain.VocabularyImport()
              .withName("raml-doc")
              .withUri("http://raml.org/vocabularies/doc#")
          ).toJSArray)

      val doc = new amf.client.model.document.Document()
      doc.withLocation("test_vocab.raml")
      doc.withEncodes(vocab)

      val readVocab = amf.model.domain.Vocabulary(doc.encodes.asInstanceOf[DomainEntity])
      assert(readVocab.base() == vocab.base())
      assert(Option(readVocab.base()).isDefined)
      assert(readVocab.usage() == vocab.usage())
      assert(Option(readVocab.usage()).isDefined)
      assert(readVocab.version() == vocab.version())
      assert(Option(readVocab.version()).isDefined)

      val propertyTerm = new amf.client.model.domain.PropertyTerm()
        .withId("http://raml.org/vocabularies/doc#test")
        .withRange(Seq("http://www.w3.org/2001/XMLSchema#string").toJSArray)

      val classTerm = new amf.client.model.domain.ClassTerm()
        .withId("http://test.com/vocab#Class")
        .withDescription("A sample class")
        .withDisplayName("Class")
        .withTermExtends("http://test.com/vocabulary/other#Class")
        .withProperties(Seq("http://raml.org/vocabularies/doc#test").toJSArray)

      vocab
        .withClassTerms(
          Seq(
            classTerm
          ).toJSArray)
        .withPropertyTerms(
          Seq(
            propertyTerm
          ).toJSArray)

      val generator = amf.Core.generator("RAML Vocabulary", "application/yaml")
      val text      = generator.generateString(doc)
      assert(
        text ==
          """#%RAML 1.0 Vocabulary
          |base: http://test.com/vocab#
          |version: 1.0
          |usage: Just a small sample vocabulary
          |external:
          |  other: http://test.com/vocabulary/other#
          |uses:
          |  raml-doc: http://raml.org/vocabularies/doc#
          |classTerms:
          |  Class:
          |    displayName: Class
          |    description: A sample class
          |    extends: other.Class
          |    properties: raml-doc.test
          |propertyTerms:
          |  raml-doc.test:
          |    range: string
          |""".stripMargin)
    }
  }
   */

  /*
  test("vocabularies parsing") {
    amf.plugins.document.Vocabularies.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().toFuture flatMap { _ =>
      val parser = amf.Core.parser("RAML Vocabularies", "application/yaml")
      parser.parseFileAsync("file://vocabularies/vocabularies/raml_shapes.raml").toFuture
    } map { parsed =>
      val vocabulary                           = amf.model.domain.Vocabulary(parsed.asInstanceOf[Document].encodes.asInstanceOf[DomainEntity])
      val acc: mutable.HashMap[String, String] = new mutable.HashMap()
      for {
        property <- vocabulary.propertyTerms().toSeq
        range    <- property.range().toSeq
      } yield {
        acc.put(property.getId(), range)
      }
      assert(acc.size == 14)
    }
  }
   */

  /*
  test("Vocabularies parsing raml_doc") {
    amf.plugins.document.Vocabularies.register()
    amf.plugins.document.WebApi.register()
    val res = amf.Core.init().toFuture flatMap { _ =>
      val parser = amf.Core.parser("RAML Vocabularies", "application/yaml")
      val f: Future[amf.client.model.document.BaseUnit] =
        parser.parseFileAsync("file://vocabularies/vocabularies/raml_doc.raml").toFuture
      f
    }

    res map { parsed: amf.client.model.document.BaseUnit =>
      assert(parsed.isInstanceOf[amf.client.model.document.Document])
    }
  }
   */

  test("Parsing text document with base url") {
    val spec = """#%RAML 1.0
                 |
                 |title: tes
                 |version: 0.1
                 |
                 |/test:
                 |  get:
                 |    responses:
                 |      200:
                 |        body:
                 |          application/json:
                 |            properties:
                 |              a: string"""

    val baseUrl = "http://test.com/myApp"
    amf.plugins.features.AMFValidation.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().toFuture flatMap { _ =>
      val parser = amf.Core.parser("RAML 1.0", "application/yaml")
      parser.parseStringAsync(baseUrl, spec).toFuture
    } flatMap { model =>
      assert(model.location.startsWith(baseUrl))
      assert(model.asInstanceOf[Document].encodes.id.startsWith(baseUrl))
    }
  }

  test("Parsing text document with base url only with domain") {
    val spec = """#%RAML 1.0
                 |
                 |title: tes
                 |version: 0.1
                 |
                 |/test:
                 |  get:
                 |    responses:
                 |      200:
                 |        body:
                 |          application/json:
                 |            properties:
                 |              a: string"""

    val baseUrl = "http://test.com"
    amf.plugins.features.AMFValidation.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().toFuture flatMap { _ =>
      val parser = amf.Core.parser("RAML 1.0", "application/yaml")
      parser.parseStringAsync(baseUrl, spec).toFuture
    } flatMap { model =>
      assert(model.location.startsWith(baseUrl))
      assert(model.asInstanceOf[Document].encodes.id.startsWith(baseUrl))
    }
  }

  private def assertBaseUnit(baseUnit: BaseUnit, expectedLocation: String): Assertion = {
    assert(baseUnit.location == expectedLocation)

    val api = baseUnit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
    assert(api != null)

    val endpoints = api.endPoints.asInternal
    val endpoint  = endpoints.head
    assert(endpoint.path.is("/v3.5/path"))
    assert(endpoints.size == 1)
    assert(endpoint.operations.size == 1)
    val post = endpoint.operations.head
    assert(post.method.is("get"))
    assert(post.request.payloads.size == 1)
    assert(post.request.payloads.head.mediaType.is("application/json"))
    assert(post.request.payloads.head.schema.getTypeIds().contains("http://www.w3.org/ns/shacl#ScalarShape"))
    assert(post.request.payloads.head.schema.getTypeIds().contains("http://www.w3.org/ns/shacl#Shape"))
    assert(
      post.request.payloads.head.schema
        .getTypeIds()
        .contains("http://raml.org/vocabularies/shapes#Shape"))
    assert(
      post.request.payloads.head.schema
        .getTypeIds()
        .contains("http://raml.org/vocabularies/document#DomainElement"))

    assert(
      post.responses.head.payloads.head.schema
        .asInstanceOf[amf.plugins.domain.shapes.models.ScalarShape]
        .dataType
        .is("http://www.w3.org/2001/XMLSchema#string"))
    assert(
      post.request.payloads.head.schema
        .asInstanceOf[amf.plugins.domain.shapes.models.ScalarShape]
        .dataType
        .is("http://www.w3.org/2001/XMLSchema#string"))
    assert(post.responses.head.statusCode.is("200"))
  }
}
