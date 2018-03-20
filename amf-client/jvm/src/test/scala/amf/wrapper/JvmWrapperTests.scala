package amf.wrapper

import _root_.org.scalatest.{Assertion, AsyncFunSuite}
import amf._
import amf.client.convert.WebApiClientConverters._
import amf.client.model.document.{BaseUnit, DialectInstance, Document, Vocabulary}
import amf.client.model.domain.{PropertyTerm, WebApi}
import amf.client.parse.{Raml10Parser, RamlParser}
import amf.client.render.{AmfGraphRenderer, Oas20Renderer, Raml10Renderer}
import amf.client.resolve.Raml10Resolver
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.vocabularies.model.domain.ClassTerm

import scala.concurrent.ExecutionContext

class JvmWrapperTests extends AsyncFunSuite with PlatformSecrets {

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
    val refs     = baseUnit.references().asInternal
    assert(refs.size == 2)
    assert(Option(refs.head.location).isDefined)
  }

  test("Generation test") {
    AMF.init().get()
    val parser   = new RamlParser()
    val baseUnit = parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").get()
    assert(new Raml10Renderer().generateString(baseUnit) != "") // TODO: test this properly
    assert(new Oas20Renderer().generateString(baseUnit) != "")
    assert(new AmfGraphRenderer().generateString(baseUnit) != "")
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
    val elem = baseUnit.asInstanceOf[DialectInstance].encodes
    assert(elem.definedBy().nodetypeMapping.value() == "http://mulesoft.com/vocabularies/eng-demos#Presentation")
    assert(elem.getTypeUris().asInternal.contains("http://mulesoft.com/vocabularies/eng-demos#Presentation"))
    // TODO: fix this getter
    // val res = elem.getDialectObjectsByPropertyId("eng-demos:speakers").asInternal
    assert(elem.getObjectByPropertyId("eng-demos:speakers") != null)
  }

  test("Raml to oas security scheme pos resolution") {
    AMF.init().get()
    val parser = new RamlParser()

    val baseUnit =
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/upanddown/unnamed-security-scheme.raml").get()
    val resolver = new Raml10Resolver()
    resolver.resolve(baseUnit)
    val generator = new Oas20Renderer()
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
    assert(!model.references().asInternal.map(_.location).contains(null))

//    val report   = AMF.validate(model, "RAML", "RAML").get()
//    assert(report.conforms)
  }

  test("banking-api-test") {
    amf.plugins.features.AMFValidation.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().get()
    val parser = amf.Core.parser("RAML 1.0", "application/yaml")
    val model =
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/production/banking-api/api.raml").get()
    val refs = model.references().asInternal
    assert(!refs.map(_.location).contains(null))
    val traitModel    = refs.find(ref => ref.location.endsWith("traits.raml")).head
    val traitRefs     = traitModel.references
    val firstFragment = traitRefs.head
    assert(firstFragment.location != null)
    assert(firstFragment.asInstanceOf[amf.plugins.document.webapi.model.TraitFragment].encodes != null)
    assert(!traitRefs.map(_.location).contains(null))
  }

  test("Vocabulary generation") {
    // TODO: complete setters
    amf.plugins.document.Vocabularies.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().get()

    val vocab = new amf.client.model.document.Vocabulary()
    vocab
      .withBase("http://test.com/vocab#")
      .withLocation("test_vocab.raml")
      .withName("Vocab")
      //.withVersion("1.0")
      .withUsage("Just a small sample vocabulary")
    /*
      .withExternals(
        Seq(
          new amf.client.model.domain.External()
            .withAlias("other")
            .withBase("http://test.com/vocabulary/other#")
        ))
      .withUses(
        Seq(
          new amf.client.model.domain.VocabularyReference()
            .withAlias("raml-doc")
            .withReference("http://raml.org/vocabularies/doc#")
        ))
     */
    assert(vocab.base.option().isDefined)
    assert(vocab.base.value() == "http://test.com/vocab#")
    assert(vocab.description.option().isDefined)
    assert(vocab.description.value() == "Just a small sample vocabulary")

    val propertyTerm = new amf.client.model.domain.DatatypePropertyTerm()
      .withId("http://raml.org/vocabularies/doc#test")
    //.withRange(Seq("http://www.w3.org/2001/XMLSchema#string").asClient)

    val classTerm = new amf.client.model.domain.ClassTerm()
      .withId("http://test.com/vocab#Class")
      .withDescription("A sample class")
      .withDisplayName("Class")
    //.withSubClassOf(Seq("http://test.com/vocabulary/other#Class").asClient)
    //.withProperties(Seq("http://raml.org/vocabularies/doc#test").asClient)

    vocab.withDeclaredElement(classTerm).withDeclaredElement(propertyTerm)

    val generator = amf.Core.generator("RAML Vocabulary", "application/yaml")
    val text      = generator.generateString(vocab)
    assert(
      text ==
        """#%RAML 1.0 Vocabulary
          |base: http://test.com/vocab#
          |vocabulary: Vocab
          |usage: Just a small sample vocabulary
          |classTerms:
          |  Class:
          |    displayName: Class
          |    description: A sample class
          |propertyTerms:
          |  test:
          |""".stripMargin)
    /*
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
   */
  }

  /*
  TODO: Fix setters
  test("vocabularies parsing ranges") {
    amf.plugins.document.Vocabularies.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().get()

    val parser                               = amf.Core.parser("RAML Vocabularies", "application/yaml")
    val parsed                               = parser.parseFileAsync("file://vocabularies/vocabularies/raml_shapes.raml").get()
    val vocabulary                           = parsed.asInstanceOf[Vocabulary]
    val acc: mutable.HashMap[String, String] = new mutable.HashMap()
    for {
      objectProperties   <- vocabulary.objectPropertyTerms()
      dataTypeProperties <- vocabulary.datatypePropertyTerms()
    } yield {
      acc.put(property.getId(), range)
    }

    assert(acc.size == 14)
  }
   */

  test("Vocabularies parsing raml_doc") {
    amf.plugins.document.Vocabularies.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().get()

    val parser = amf.Core.parser("RAML Vocabularies", "application/yaml")
    val parsed = parser.parseFileAsync("file://vocabularies/vocabularies/raml_doc.raml").get()
    val terms = parsed.asInstanceOf[Vocabulary].declares.asInternal.collect {
      case term: ClassTerm    => term
      case prop: PropertyTerm => prop
    }
    assert(terms.size == 15)
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
    assert(model.asInstanceOf[Document].encodes.id.startsWith(baseUrl))
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
    assert(model.asInstanceOf[Document].encodes.id.startsWith(baseUrl))
  }

  private def assertBaseUnit(baseUnit: BaseUnit, expectedLocation: String): Assertion = {
    assert(baseUnit.location == expectedLocation)
    val api       = baseUnit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
    val endPoints = api.endPoints.asInternal
    val endpoint  = endPoints.head
    assert(endpoint.path.is("/v3.5/path"))
    assert(endPoints.size == 1)
    assert(endpoint.operations.size == 1)
    val post = endpoint.operations.head
    assert(post.method.is("get"))
    assert(post.request.payloads.size == 1)
    val first = post.request.payloads.head
    assert(first.mediaType.is("application/json"))
    assert(first.schema.getTypeIds().contains("http://www.w3.org/ns/shacl#ScalarShape"))
    assert(first.schema.getTypeIds().contains("http://www.w3.org/ns/shacl#Shape"))
    assert(first.schema.getTypeIds().contains("http://raml.org/vocabularies/shapes#Shape"))
    assert(first.schema.getTypeIds().contains("http://raml.org/vocabularies/document#DomainElement"))
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
