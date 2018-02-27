package amf.wrapper

import _root_.org.scalatest.{Assertion, AsyncFunSuite}
import amf._
import amf.core.unsafe.PlatformSecrets
import amf.model.document.{BaseUnit, Document, TraitFragment}
import amf.model.domain.{DomainEntity, ScalarShape, WebApi}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext

class WrapperTests extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Parsing autodetect raml 1.0 test") {
    AMF.init().get()
    val parser = new RamlParser()
    assertBaseUnit(
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").get(),
      "file://amf-client/shared/src/test/resources/api/zencoder.raml"
    )
  }

  test("Parsing autodetect raml 0.8 test") {
    AMF.init().get()
    val parser = new RamlParser()
    assertBaseUnit(
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder08.raml").get(),
      "file://amf-client/shared/src/test/resources/api/zencoder08.raml"
    )
  }

  test("Parsing raml 1.0 test") {
    AMF.init().get()
    val parser = new Raml10Parser()
    assertBaseUnit(
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").get(),
      "file://amf-client/shared/src/test/resources/api/zencoder.raml"
    )
  }

  test("Parsing raml 0.8 test") {
    AMF.init().get()
    val parser = new RamlParser()
    assertBaseUnit(
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder08.raml").get(),
      "file://amf-client/shared/src/test/resources/api/zencoder08.raml"
    )
  }

  test("Parsing refs test") {
    AMF.init().get()
    val parser   = new RamlParser()
    val baseUnit = parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/banking.raml").get()
    assert(baseUnit.references().size() == 2)
    assert(Option(baseUnit.references().get(0).location).isDefined)
  }

  test("Generation test") {
    AMF.init().get()
    val parser   = new RamlParser()
    val baseUnit = parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").get()
    assert(new Raml10Generator().generateString(baseUnit) != "") // TODO: test this properly
    assert(new Oas20Generator().generateString(baseUnit) != "")
    assert(new AmfGraphGenerator().generateString(baseUnit) != "")
  }

  test("Validation test") {
    AMF.init().get()
    val parser   = new RamlParser()
    val baseUnit = parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").get()
    val report   = AMF.validate(baseUnit, "RAML").get()
    assert(report.conforms)
    AMF.loadValidationProfile("file://amf-client/shared/src/test/resources/api/validation/custom-profile.raml").get()
    val custom = AMF.validate(baseUnit, "Banking").get()
    assert(!custom.conforms)
  }

  test("Resolution test") {
    AMF.init().get()
    val parser           = new RamlParser()
    val baseUnit         = parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").get()
    val resolvedBaseUnit = AMF.resolveRaml10(baseUnit) // TODO: test this properly
    val report           = AMF.validate(resolvedBaseUnit, "RAML").get()
    assert(report.conforms)
  }

  test("Vocabularies test") {
    AMF.init().get()

    AMF.registerDialect("file://amf-client/shared/src/test/resources/api/dialects/eng-demos.raml").get()

    val parser = new RamlParser()
    val baseUnit =
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/examples/libraries/demo.raml").get()

    val report = AMF.validate(baseUnit, "Eng Demos 0.1").get()
    assert(report.conforms)

    AMF.registerNamespace("eng-demos", "http://mulesoft.com/vocabularies/eng-demos#")
    val elem     = baseUnit.asInstanceOf[Document].encodes
    val speakers = elem.getObjectByPropertyId("eng-demos:speakers")
    assert(speakers.size() > 0)
  }

  test("Raml to oas secutiry scheme pos resolution") {
    AMF.init().get()
    val parser = new RamlParser()

    val baseUnit =
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/upanddown/unnamed-security-scheme.raml").get()
    val resolver = new Raml10Resolver()
    resolver.resolve(baseUnit)
    val generator = new Oas20Generator()
    val str       = generator.generateString(baseUnit)
    assert(!str.isEmpty)
  }

  test("world-music-test") {
    amf.plugins.features.AMFValidation.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().get()
    val parser = amf.Core.parser("RAML 1.0", "application/yaml")
    val model =
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/production/world-music-api/api.raml").get()
    assert(!model.references().asScala.map(_.location).contains(null))
  }

  test("banking-api-test") {
    amf.plugins.features.AMFValidation.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().get()
    val parser = amf.Core.parser("RAML 1.0", "application/yaml")
    val model =
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/production/banking-api/api.raml").get()
    assert(!model.references().asScala.map(_.location).contains(null))
    val traitModel    = model.references().asScala.find(ref => ref.location.endsWith("traits.raml")).head
    val traitRefs     = traitModel.references()
    val firstFragment = traitRefs.asScala.head
    assert(firstFragment.location != null)
    assert(firstFragment.asInstanceOf[TraitFragment].encodes != null)
    assert(!traitRefs.asScala.map(_.location).contains(null))
  }

  test("Vocabulary generation") {
    amf.plugins.document.Vocabularies.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().get()

    val vocab = new amf.model.domain.Vocabulary()
    vocab
      .withBase("http://test.com/vocab#")
      .withVersion("1.0")
      .withUsage("Just a small sample vocabulary")
      .withExternals(
        Seq(
          new amf.model.domain.ExternalVocabularyImport()
            .withName("other")
            .withUri("http://test.com/vocabulary/other#")
        ).asJava)
      .withUses(
        Seq(
          new amf.model.domain.VocabularyImport()
            .withName("raml-doc")
            .withUri("http://raml.org/vocabularies/doc#")
        ).asJava)

    val doc = new amf.model.document.Document()
    doc.withLocation("test_vocab.raml")
    doc.withEncodes(vocab)

    val readVocab = amf.model.domain.Vocabulary(doc.encodes.asInstanceOf[DomainEntity])
    assert(readVocab.base() == vocab.base())
    assert(Option(readVocab.base()).isDefined)
    assert(readVocab.usage() == vocab.usage())
    assert(Option(readVocab.usage()).isDefined)
    assert(readVocab.version() == vocab.version())
    assert(Option(readVocab.version()).isDefined)

    val propertyTerm = new amf.model.domain.PropertyTerm()
      .withId("http://raml.org/vocabularies/doc#test")
      .withRange(Seq("http://www.w3.org/2001/XMLSchema#string").asJava)

    val classTerm = new amf.model.domain.ClassTerm()
      .withId("http://test.com/vocab#Class")
      .withDescription("A sample class")
      .withDisplayName("Class")
      .withTermExtends("http://test.com/vocabulary/other#Class")
      .withProperties(Seq("http://raml.org/vocabularies/doc#test").asJava)

    vocab
      .withClassTerms(
        Seq(
          classTerm
        ).asJava)
      .withPropertyTerms(
        Seq(
          propertyTerm
        ).asJava)

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

  test("vocabularies parsing ranges") {
    amf.plugins.document.Vocabularies.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().get()

    val parser                               = amf.Core.parser("RAML Vocabularies", "application/yaml")
    val parsed                               = parser.parseFileAsync("file://vocabularies/vocabularies/raml_shapes.raml").get()
    val vocabulary                           = amf.model.domain.Vocabulary(parsed.asInstanceOf[Document].encodes.asInstanceOf[DomainEntity])
    val acc: mutable.HashMap[String, String] = new mutable.HashMap()
    for {
      property <- vocabulary.propertyTerms().asScala
      range    <- property.range().asScala
    } yield {
      acc.put(property.getId(), range)
    }

    assert(acc.size == 14)
  }

  test("Vocabularies parsing raml_doc") {
    amf.plugins.document.Vocabularies.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().get()

    val parser     = amf.Core.parser("RAML Vocabularies", "application/yaml")
    val parsed     = parser.parseFileAsync("file://vocabularies/vocabularies/raml_doc.raml").get()
    val vocabulary = amf.model.domain.Vocabulary(parsed.asInstanceOf[Document].encodes.asInstanceOf[DomainEntity])
    assert(vocabulary != null)
  }

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
    amf.Core.init().get()
    val parser = amf.Core.parser("RAML 1.0", "application/yaml")
    val model  = parser.parseStringAsync(baseUrl, spec).get()
    assert(model.location.startsWith(baseUrl))
    assert(model.asInstanceOf[Document].encodes.getId().startsWith(baseUrl))
  }

  test("Parsing text document with base url just containing the domain") {
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
    amf.Core.init().get()
    val parser = amf.Core.parser("RAML 1.0", "application/yaml")
    val model  = parser.parseStringAsync(baseUrl, spec).get()
    assert(model.location.startsWith(baseUrl))
    assert(model.asInstanceOf[Document].encodes.getId().startsWith(baseUrl))
  }

  private def assertBaseUnit(baseUnit: BaseUnit, expectedLocation: String): Assertion = {
    assert(baseUnit.location == expectedLocation)
    val api      = baseUnit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
    val endpoint = api.endPoints.get(0)
    assert(endpoint.path == "/v3.5/path")
    assert(api.endPoints.size() == 1)
    assert(endpoint.operations.size() == 1)
    val post = endpoint.operations.get(0)
    assert(post.method == "get")
    assert(post.request.payloads.size() == 1)
    assert(post.request.payloads.get(0).mediaType == "application/json")
    assert(post.request.payloads.get(0).schema.getTypeIds().contains("http://www.w3.org/ns/shacl#ScalarShape"))
    assert(post.request.payloads.get(0).schema.getTypeIds().contains("http://www.w3.org/ns/shacl#Shape"))
    assert(post.request.payloads.get(0).schema.getTypeIds().contains("http://raml.org/vocabularies/shapes#Shape"))
    assert(
      post.request.payloads.get(0).schema.getTypeIds().contains("http://raml.org/vocabularies/document#DomainElement"))
    assert(
      post.responses
        .get(0)
        .payloads
        .get(0)
        .schema
        .asInstanceOf[ScalarShape]
        .dataType == "http://www.w3.org/2001/XMLSchema#string")
    assert(
      post.request.payloads
        .get(0)
        .schema
        .asInstanceOf[ScalarShape]
        .dataType == "http://www.w3.org/2001/XMLSchema#string")
    assert(post.responses.get(0).statusCode == "200")
  }
}
