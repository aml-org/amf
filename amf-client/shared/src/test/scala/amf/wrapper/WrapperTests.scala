package amf.wrapper

import amf.client.AMF
import amf.client.convert.NativeOps
import amf.client.convert.WebApiClientConverters._
import amf.client.environment.{DefaultEnvironment, Environment}
import amf.client.model.document._
import amf.client.model.domain._
import amf.client.parse._
import amf.client.remote.Content
import amf.client.render._
import amf.client.resolve.Raml10Resolver
import amf.client.resource.ResourceLoader
import amf.common.Diff
import amf.core.vocabulary.Namespace
import amf.plugins.document.Vocabularies
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

trait WrapperTests extends AsyncFunSuite with Matchers with NativeOps {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val zencoder      = "file://amf-client/shared/src/test/resources/api/zencoder.raml"
  private val zencoder08    = "file://amf-client/shared/src/test/resources/api/zencoder08.raml"
  private val demosDialect  = "file://amf-client/shared/src/test/resources/api/dialects/eng-demos.raml"
  private val demos2Dialect = "file://amf-client/shared/src/test/resources/api/dialects/eng-demos-2.raml"
  private val demosInstance = "file://amf-client/shared/src/test/resources/api/examples/libraries/demo.raml"
  private val security      = "file://amf-client/shared/src/test/resources/upanddown/unnamed-security-scheme.raml"
  private val music         = "file://amf-client/shared/src/test/resources/production/world-music-api/api.raml"
  private val banking       = "file://amf-client/shared/src/test/resources/production/banking-api/api.raml"
  private val amflight =
    "file://amf-client/shared/src/test/resources/production/american-flight-api-2.0.1-raml/api.raml"
  private val defaultValue = "file://amf-client/shared/src/test/resources/api/shape-default.raml"
  private val traits       = "file://amf-client/shared/src/test/resources/production/banking-api/traits/traits.raml"
  private val profile      = "file://amf-client/shared/src/test/resources/api/validation/custom-profile.raml"
  //  private val banking       = "file://amf-client/shared/src/test/resources/api/banking.raml"
  private val raml_doc = "file://vocabularies/vocabularies/raml_doc.raml"
  private val scalarAnnotations =
    "file://amf-client/shared/src/test/resources/org/raml/parser/annotation/scalar-nodes/input.raml"

  test("Parsing raml 1.0 test (detect)") {
    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseFileAsync(zencoder).asFuture
    } yield {
      assertBaseUnit(unit, zencoder)
    }
  }

  test("Parsing raml 0.8 test (detect)") {
    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseFileAsync(zencoder08).asFuture
    } yield {
      assertBaseUnit(unit, zencoder08)
    }
  }

  test("Parsing raml 1.0 test") {
    for {
      _    <- AMF.init().asFuture
      unit <- new Raml10Parser().parseFileAsync(zencoder).asFuture
    } yield {
      assertBaseUnit(unit, zencoder)
    }
  }

  test("Parsing raml 0.8 test") {
    for {
      _    <- AMF.init().asFuture
      unit <- new Raml08Parser().parseFileAsync(zencoder08).asFuture
    } yield {
      assertBaseUnit(unit, zencoder08)
    }
  }

  test("Parsing refs test") {
    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseFileAsync(banking).asFuture
    } yield {
      val refs = unit.references().asSeq
      assert(refs.size == 4)
      assert(refs.head.location.endsWith("traits/content-cacheable.raml"))
    }
  }

  test("Parsing default value string") {
    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseFileAsync(defaultValue).asFuture
    } yield {
      val declares = unit.asInstanceOf[DeclaresModel].declares.asSeq
      assert(declares.size == 1)
      assert(declares.head.isInstanceOf[NodeShape])
      val shape = declares.head.asInstanceOf[NodeShape]
      assert(
        shape.defaultValueStr
          .value()
          .equals("\n      name: roman\n      lastname: riquelme\n      age: 39".stripMargin))
      assert(shape.defaultValue.isInstanceOf[ObjectNode])
    }
  }

  test("Render / parse test RAML 0.8") {
    for {
      _      <- AMF.init().asFuture
      unit   <- new RamlParser().parseFileAsync(zencoder08).asFuture
      output <- new Raml08Renderer().generateString(unit).asFuture
      result <- new Raml08Parser().parseStringAsync(output).asFuture
    } yield {
      assertBaseUnit(result, "http://raml.org/amf/default_document")
    }
  }

  test("Render / parse test RAML 1.0") {
    for {
      _      <- AMF.init().asFuture
      unit   <- new RamlParser().parseFileAsync(zencoder).asFuture
      output <- new Raml10Renderer().generateString(unit).asFuture
      result <- new Raml10Parser().parseStringAsync(output).asFuture
    } yield {
      assertBaseUnit(result, "http://raml.org/amf/default_document")
    }
  }

  test("Render / parse test OAS 2.0") {
    for {
      _      <- AMF.init().asFuture
      unit   <- new RamlParser().parseFileAsync(zencoder).asFuture
      output <- new Oas20Renderer().generateString(unit).asFuture
      result <- new Oas20Parser().parseStringAsync(output).asFuture
    } yield {
      assertBaseUnit(result, "http://raml.org/amf/default_document")
    }
  }

  test("Render / parse test AMF") {
    for {
      _      <- AMF.init().asFuture
      unit   <- new RamlParser().parseFileAsync(zencoder).asFuture
      output <- new AmfGraphRenderer().generateString(unit).asFuture
      result <- new AmfGraphParser().parseStringAsync(output).asFuture
    } yield {
      assertBaseUnit(result, "http://raml.org/amf/default_document")
    }
  }

  test("Validation test") {
    for {
      _           <- AMF.init().asFuture
      unit        <- new RamlParser().parseFileAsync(zencoder).asFuture
      report      <- AMF.validate(unit, "RAML").asFuture
      profileName <- AMF.loadValidationProfile(profile).asFuture
      custom      <- AMF.validate(unit, profileName).asFuture
    } yield {
      assert(report.conforms)
      assert(!custom.conforms)
    }
  }

  test("Resolution test") {
    for {
      _        <- AMF.init().asFuture
      unit     <- new RamlParser().parseFileAsync(zencoder).asFuture
      resolved <- Future.successful(AMF.resolveRaml10(unit))
      report   <- AMF.validate(resolved, "RAML").asFuture
    } yield {
      assert(report.conforms)
    }
  }

  test("Vocabularies test") {
    for {
      _           <- AMF.init().asFuture
      dialectName <- AMF.registerDialect(demosDialect).asFuture
      unit        <- new RamlParser().parseFileAsync(demosInstance).asFuture
      report      <- AMF.validate(unit, "Eng Demos 0.1").asFuture
    } yield {
      AMF.registerNamespace("eng-demos", "http://mulesoft.com/vocabularies/eng-demos#")
      val elem = unit.asInstanceOf[DialectInstance].encodes
      assert(elem.definedBy().nodetypeMapping.is("http://mulesoft.com/vocabularies/eng-demos#Presentation"))
      assert(elem.getTypeUris().asSeq.contains("http://mulesoft.com/vocabularies/eng-demos#Presentation"))
      // TODO: fix this getter
//       val res = elem.getDialectObjectsByPropertyId("eng-demos:speakers").asInternal
//      assert(elem.graph().getObjectByPropertyId("eng-demos:speakers").size > 0) // todo ???
      assert(elem.getObjectPropertyUri("eng-demos:speakers").asSeq.size == 2)
    }
  }

  test("Custom Vocabularies test") {
    case class CustomLoader() extends ResourceLoader {
      private val url = "vocab:eng-demos-2.raml"
      private val stream =
        """
          |#%RAML 1.0 Vocabulary
          |
          |# Name of the vocabulary
          |vocabulary: Eng Demos
          |
          |usage: Engineering Demonstrations @ MuleSoft
          |
          |# Namespace for the vocabulary (must be a URI prefix)
          |# All terms in the vocabulary will be URIs in this namespace
          |base: http://mulesoft.com/vocabularies/eng-demos#
          |
          |external:
          |  schema-org: http://schema.org/
          |
          |classTerms:
          |
          |  # URI for this term: http://mulesoft.com/vocabularies/eng-demos#Presentation
          |  Presentation:
          |    displayName: Presentation
          |    description: Product demonstrations
          |    properties:
          |      - showcases
          |      - speakers
          |      - demoDate
          |
          |  Speaker:
          |    displayName: Speaker
          |    description: Product demonstration presenter
          |    extends: schema-org.Person
          |    properties:
          |      - nickName
          |
          |  schema-org.Product:
          |    displayName: Product
          |    description: The product being showcased
          |    properties:
          |      - resources
          |
          |
          |propertyTerms:
          |
          |  # scalar range, datatype property
          |  # URI for this term: http://mulesoft.com/vocabularies/eng-demos#nickName
          |  nickName:
          |    displayName: nick
          |    description: nick name of the speaker
          |    range: string
          |    extends: schema-org.alternateName
          |
          |  showcases:
          |    displayName: showcases
          |    description: Product being showcased in a presentation
          |    range: schema-org.Product
          |
          |  speakers:
          |    displayName: speakers
          |    description: list of speakers
          |    range: Speaker
          |
          |  resources:
          |    displayName: resources
          |    description: list of materials about the showcased product
          |    range: string
          |
          |  semantic-version:
          |    displayName: semantic version
          |    description: 'semantic version standard: M.m.r'
          |    extends: schema-org.version
          |    range: string
          |
          |  demoDate:
          |    displayName: demo date
          |    description: day the demo took place
          |    extends: schema-org.dateCreated
          |    range: date
          |
          |  isRecorded:
          |    displayName: is recorded
          |    description: notifies if this demo was recorded
          |    range: boolean
          |
          |  code:
          |    displayName: code
          |    description: product code
          |    range: string
          |    extends: schema-org.name
        """.stripMargin

      /** Fetch specified resource and return associated content. Resource should have benn previously accepted. */
      override def fetch(resource: String): ClientFuture[Content] = Future { new Content(stream, url) }.asClient

      /** Accepts specified resource. */
      override def accepts(resource: String): Boolean = resource == url
    }

    val env = DefaultEnvironment().add(CustomLoader().asInstanceOf[ClientLoader])

    for {
      _           <- AMF.init().asFuture
      dialectName <- Vocabularies.registerDialect(demos2Dialect, env).asFuture
      unit        <- new RamlParser().parseFileAsync(demosInstance).asFuture
      report      <- AMF.validate(unit, "Eng Demos 0.1").asFuture
    } yield {
      AMF.registerNamespace("eng-demos", "http://mulesoft.com/vocabularies/eng-demos#")
      val elem = unit.asInstanceOf[DialectInstance].encodes
      assert(elem.definedBy().nodetypeMapping.is("http://mulesoft.com/vocabularies/eng-demos#Presentation"))
      assert(elem.getTypeUris().asSeq.contains("http://mulesoft.com/vocabularies/eng-demos#Presentation"))
      assert(elem.getObjectPropertyUri("eng-demos:speakers").asSeq.size == 2)
    }
  }

  test("Raml to oas security scheme after resolution") {
    for {
      _      <- AMF.init().asFuture
      unit   <- new RamlParser().parseFileAsync(security).asFuture
      _      <- Future.successful(new Raml10Resolver().resolve(unit))
      output <- new Oas20Renderer().generateString(unit).asFuture
    } yield {
      assert(!output.isEmpty)
    }
  }

  test("world-music-test") {
    for {
      _      <- AMF.init().asFuture
      unit   <- amf.Core.parser("RAML 1.0", "application/yaml").parseFileAsync(music).asFuture
      report <- AMF.validate(unit, "RAML", "RAML").asFuture
    } yield {
      assert(!unit.references().asSeq.map(_.location).contains(null))
      assert(report.conforms)
    }
  }

  test("banking-api-test") {
    for {
      _    <- AMF.init().asFuture
      unit <- amf.Core.parser("RAML 1.0", "application/yaml").parseFileAsync(banking).asFuture
    } yield {
      val references = unit.references().asSeq
      assert(!references.map(_.location).contains(null))
      val traits = references.find(ref => ref.location.endsWith("traits.raml")).head.references().asSeq
      val first  = traits.head
      assert(first.location != null)
      assert(first.asInstanceOf[TraitFragment].encodes != null)
      assert(!traits.map(_.location).contains(null))
    }
  }

  test("banking-api-traits-test") {
    for {
      _    <- AMF.init().asFuture
      unit <- amf.Core.parser("RAML 1.0", "application/yaml").parseFileAsync(traits).asFuture
    } yield {
      val references = unit.references().asSeq
      val first      = references.head
      assert(first.location != null)
      assert(first.asInstanceOf[TraitFragment].encodes != null)
      assert(!references.map(_.location).contains(null))
    }
  }

  test("Scalar Annotations") {
    for {
      _    <- AMF.init().asFuture
      unit <- amf.Core.parser("RAML 1.0", "application/yaml").parseFileAsync(scalarAnnotations).asFuture
    } yield {
      val api         = unit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
      val annotations = api.name.annotations().custom().asSeq
      annotations should have size 1
      val annotation = annotations.head
      annotation.name.value() should be("foo")
      annotation.extension.asInstanceOf[ScalarNode].value should be("annotated title")
    }
  }

  test("Vocabulary generation") {

//    import amf.client.convert.VocabulariesClientConverter._ //todo uncomment to import *.asClient

    val vocab = new Vocabulary()
    vocab
      .withName("Vocab")
      .withBase("http://test.com/vocab#")
      .withLocation("test_vocab.raml")
      .withUsage("Just a small sample vocabulary")
    /*.withExternals(
        Seq(
          new External()
            .withAlias("other")
            .withBase("http://test.com/vocabulary/other#")
        ).toClient)
      .withUsages( // todo withImports?
        Seq(
          new VocabularyReference()
            .withAlias("raml-doc")
            .withReference("http://raml.org/vocabularies/doc#")
        ).toClient)*/

    assert(vocab.base.option.asOption.isDefined)
    assert(vocab.base.is("http://test.com/vocab#"))
    assert(vocab.description.option.asOption.isDefined)
    assert(vocab.description.is("Just a small sample vocabulary"))

    val propertyTerm = new DatatypePropertyTerm()
      .withId("http://raml.org/vocabularies/doc#test")
      .withRange("http://www.w3.org/2001/XMLSchema#string")

    val classTerm = new ClassTerm()
      .withId("http://test.com/vocab#Class")
      .withDescription("A sample class")
      .withDisplayName("Class")
//      .withSubClassOf(Seq("http://test.com/vocabulary/other#Class").asClient)
//      .withProperties(Seq("http://raml.org/vocabularies/doc#test").asClient)

    vocab.withDeclaredElement(classTerm).withDeclaredElement(propertyTerm)

    for {
      _      <- AMF.init().asFuture
      render <- amf.Core.generator("RAML Vocabulary", "application/yaml").generateString(vocab).asFuture
    } yield {
      render should be(
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
          |    range: string
          |""".stripMargin
      )
    }
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
    for {
      _    <- AMF.init().asFuture
      unit <- amf.Core.parser("RAML Vocabularies", "application/yaml").parseFileAsync(raml_doc).asFuture
    } yield {
      val declarations = unit.asInstanceOf[Vocabulary].declares.asSeq

      val classes    = declarations.collect { case term: ClassTerm    => term }
      val properties = declarations.collect { case prop: PropertyTerm => prop }

      assert(classes.size == 15)
      assert(properties.size == 13)
    }
  }

  test("Parsing text document with base url") {
    val baseUrl = "http://test.com/myApp"
    testParseStringWithBaseUrl(baseUrl)
  }

  test("Parsing text document with base url (domain only)") {
    val baseUrl = "http://test.com"
    testParseStringWithBaseUrl(baseUrl)
  }

  test("Environment test") {
    val include = "amf://types/Person.raml"

    val input = s"""
      |#%RAML 1.0
      |title: Environment test
      |types:
      |  Person: !include $include
    """.stripMargin

    val person = """
      |#%RAML 1.0 DataType
      |type: object
      |properties:
      |  name: string
    """.stripMargin

    case class TestResourceLoader() extends ResourceLoader {

      import amf.client.convert.WebApiClientConverters._

      override def fetch(resource: String): ClientFuture[Content] =
        Future.successful(new Content(person, resource)).asClient

      override def accepts(resource: String): Boolean = resource == include
    }

    val environment = Environment.empty().add(TestResourceLoader().asInstanceOf[ClientLoader])

    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser(environment).parseStringAsync(input).asFuture
    } yield {
      unit shouldBe a[Document]
      val declarations = unit.asInstanceOf[Document].declares.asSeq
      declarations should have size 1
    }
  }

  test("Environment fallback test") {
    val include = "amf://types/Person.raml"

    val input = s"""
       |#%RAML 1.0
       |title: Environment test
       |types:
       |  Person: !include $include
    """.stripMargin

    val person = """
       |#%RAML 1.0 DataType
       |type: object
       |properties:
       |  name: string
     """.stripMargin

    import amf.client.convert.WebApiClientConverters._

    case class TestResourceLoader() extends ResourceLoader {
      override def fetch(resource: String): ClientFuture[Content] =
        Future.successful(new Content(person, resource)).asClient

      override def accepts(resource: String): Boolean = resource == include
    }

    case class FailingResourceLoader(msg: String) extends ResourceLoader {
      override def fetch(resource: String): ClientFuture[Content] =
        Future.failed[Content](new Exception(msg)).asClient
    }

    val environment = Environment
      .empty()
      .add(TestResourceLoader().asInstanceOf[ClientLoader])
      .add(FailingResourceLoader("Unreachable network").asInstanceOf[ClientLoader])
      .add(FailingResourceLoader("Invalid protocol").asInstanceOf[ClientLoader])

    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser(environment).parseStringAsync(input).asFuture
    } yield {
      unit shouldBe a[Document]
      val declarations = unit.asInstanceOf[Document].declares.asSeq
      declarations should have size 1
    }
  }

  test("Generate unit with source maps") {
    val options = new RenderOptions().withSourceMaps

    for {
      _      <- AMF.init().asFuture
      unit   <- amf.Core.parser("RAML 1.0", "application/yaml").parseFileAsync(banking).asFuture
      jsonld <- amf.Core.generator("AMF Graph", "application/ld+json").generateString(unit, options).asFuture
    } yield {
      jsonld should include("[(3,0)-(252,0)]")
    }
  }

  test("Generate unit without source maps") {
    val options = new RenderOptions().withoutSourceMaps

    for {
      _      <- AMF.init().asFuture
      unit   <- amf.Core.parser("RAML 1.0", "application/yaml").parseFileAsync(banking).asFuture
      jsonld <- amf.Core.generator("AMF Graph", "application/ld+json").generateString(unit, options).asFuture
    } yield {
      jsonld should not include "[(3,0)-(252,0)]"
    }
  }

  test("Missing converter error") {
    val options = new RenderOptions().withoutSourceMaps

    for {
      _        <- AMF.init().asFuture
      unit     <- amf.Core.parser("RAML 1.0", "application/yaml").parseFileAsync(amflight).asFuture
      resolved <- Future.successful(AMF.resolveRaml10(unit))
    } yield {
      val webapi = resolved.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
      webapi.endPoints.asSeq.foreach { ep =>
        ep.operations.asSeq.foreach { op =>
          op.responses.asSeq.foreach { resp =>
            resp.payloads.asSeq.foreach { payload =>
              payload.schema
            }
          }
        }
      }
      assert(true)
    }
  }

  test("Build shape without default value") {

    val shape = new ScalarShape()
    shape.withDataType("string")
    shape.withName("name")

    assert(shape.defaultValue == null)
  }

  test("Remove field and dump") {
    val api =
      """
        |#%RAML 1.0
        |title: this should remain
        |description: remove
        |license:
        | url: removeUrl
        | name: test
        |/endpoint1:
        | get:
        |   responses:
        |     200:
      """.stripMargin

    val excepted =
      """
        |#%RAML 1.0
        |title: this should remain
        |/endpoint1:
        | get: {}""".stripMargin
    for {
      _         <- AMF.init().asFuture
      unit      <- new RamlParser().parseStringAsync(api).asFuture
      removed   <- removeFields(unit)
      generated <- AMF.raml10Generator().generateString(removed).asFuture
    } yield {
      val deltas = Diff.ignoreAllSpace.diff(excepted, generated)
      if (deltas.nonEmpty) fail("Expected and golden are different: " + Diff.makeString(deltas))
      else succeed
    }
  }

  test("Test dynamic types") {
    val api =
      """
        |#%RAML 1.0
        |title: this should remain
        |description: remove
        |license:
        | url: removeUrl
        | name: test
        |/endpoint1:
        | get:
        |   responses:
        |     200:
        |       body:
        |        application/json:
        |           properties:
        |             name: string
        |             lastname: string
        |           example:
        |             name: roman
        |             lastname: riquelme
        |
      """.stripMargin

    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseStringAsync(api).asFuture
    } yield {
      val webApi = unit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
      val dataNode = webApi.endPoints.asSeq.head.operations.asSeq.head.responses.asSeq.head.payloads.asSeq.head.schema
        .asInstanceOf[AnyShape]
        .examples
        .asSeq
        .head
        .structuredValue
      assert(dataNode._internal.dynamicTypes().nonEmpty)
      assert(dataNode._internal.dynamicTypes().head.contains((Namespace.Data + "Object").iri()))
    }
  }

  private def removeFields(unit: BaseUnit): Future[BaseUnit] = Future {
    val webApi = unit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
    webApi.description.remove()
    val operation: Operation = webApi.endPoints.asSeq.head.operations.asSeq.head
    operation.graph().remove("http://www.w3.org/ns/hydra/core#returns")

    webApi.graph().remove("http://schema.org/license")
    unit
  }

  private def testParseStringWithBaseUrl(baseUrl: String) = {
    val spec =
      """#%RAML 1.0
        |
        |title: Some title
        |version: 0.1
        |
        |/test:
        |  get:
        |    responses:
        |      200:
        |        body:
        |          application/json:
        |            properties:
        |              a: string""".stripMargin

    for {
      _    <- AMF.init().asFuture
      unit <- amf.Core.parser("RAML 1.0", "application/yaml").parseStringAsync(baseUrl, spec).asFuture
    } yield {
      assert(unit.location.startsWith(baseUrl))
      val encodes = unit.asInstanceOf[Document].encodes
      assert(encodes.id.startsWith(baseUrl))
      assert(encodes.asInstanceOf[WebApi].name.is("Some title"))
    }

  }

  private def assertBaseUnit(baseUnit: BaseUnit, expectedLocation: String): Assertion = {
    assert(baseUnit.location == expectedLocation)
    val api       = baseUnit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
    val endPoints = api.endPoints.asSeq
    assert(endPoints.size == 1)

    val endpoint = endPoints.head
    assert(endpoint.path.is("/v3.5/path"))
    val ops = endpoint.operations.asSeq
    assert(ops.size == 1)
    val post = ops.head
    assert(post.method.is("get"))
    val payloads = post.request.payloads.asSeq
    assert(payloads.size == 1)

    val first = payloads.head
    assert(first.mediaType.is("application/json"))

    val typeIds = first.schema.graph().types().asSeq
    assert(typeIds.contains("http://raml.org/vocabularies/shapes#ScalarShape"))
    assert(typeIds.contains("http://www.w3.org/ns/shacl#Shape"))
    assert(typeIds.contains("http://raml.org/vocabularies/shapes#Shape"))
    assert(typeIds.contains("http://raml.org/vocabularies/document#DomainElement"))

    val responses = post.responses.asSeq
    assert(
      responses.head.payloads.asSeq.head.schema
        .asInstanceOf[ScalarShape]
        .dataType
        .is("http://www.w3.org/2001/XMLSchema#string"))

    assert(
      payloads.head.schema
        .asInstanceOf[ScalarShape]
        .dataType
        .is("http://www.w3.org/2001/XMLSchema#string"))

    assert(responses.head.statusCode.is("200"))
  }
}
