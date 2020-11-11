package amf.convert

import _root_.org.scalatest.{Assertion, Matchers}
import amf._
import amf.client.AMF
import amf.client.convert.CoreClientConverters._
import amf.client.convert.NativeOps
import amf.client.environment.{DefaultEnvironment, Environment}
import amf.client.model.document._
import amf.client.model.domain._
import amf.plugins.domain.webapi.models.{CorrelationId => InternalCorrelationId}
import amf.client.parse._
import amf.client.remote.Content
import amf.client.render.{Renderer, _}
import amf.client.resolve.{Async20Resolver, Oas20Resolver, Oas30Resolver, Raml08Resolver, Raml10Resolver}
import amf.client.resource.{ResourceLoader, ResourceNotFound}
import amf.common.Diff
import amf.core.errorhandling.StaticErrorCollector
import amf.core.exception.UnsupportedVendorException
import amf.core.model.document.{Document => InternalDocument}
import amf.core.model.domain.{
  ArrayNode => InternalArrayNode,
  ObjectNode => InternalObjectNode,
  ScalarNode => InternalScalarNode
}
import amf.core.parser.Range
import amf.core.remote.{Aml, Oas20, Raml10, Vendor}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.Xsd
import amf.io.{FileAssertionTest, MultiJsonldAsyncFunSuite}
import amf.internal.environment.{Environment => InternalEnvironment}
import amf.internal.resource.StringResourceLoader
import amf.plugins.document.Vocabularies
import amf.plugins.document.webapi.parser.spec.common.emitters.WebApiDomainElementEmitter
import amf.plugins.domain.webapi.metamodel.WebApiModel
import org.mulesoft.common.io.{LimitReachedException, LimitedStringBuffer}
import org.yaml.builder.JsonOutputBuilder

import scala.concurrent.{ExecutionContext, Future}

trait WrapperTests extends MultiJsonldAsyncFunSuite with Matchers with NativeOps with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val banking       = "file://amf-client/shared/src/test/resources/production/raml10/banking-api/api.raml"
  private val zencoder      = "file://amf-client/shared/src/test/resources/api/zencoder.raml"
  private val oas3          = "file://amf-client/shared/src/test/resources/api/oas3.json"
  private val async2        = "file://amf-client/shared/src/test/resources/api/async2.yaml"
  private val zencoder08    = "file://amf-client/shared/src/test/resources/api/zencoder08.raml"
  private val music         = "file://amf-client/shared/src/test/resources/production/world-music-api/api.raml"
  private val demosDialect  = "file://amf-client/shared/src/test/resources/api/dialects/eng-demos.raml"
  private val demos2Dialect = "file://amf-client/shared/src/test/resources/api/dialects/eng-demos-2.raml"
  private val demosInstance = "file://amf-client/shared/src/test/resources/api/examples/libraries/demo.raml"
  private val security      = "file://amf-client/shared/src/test/resources/upanddown/unnamed-security-scheme.raml"
  private val amflight =
    "file://amf-client/shared/src/test/resources/production/raml10/american-flight-api-2.0.1-raml.ignore/api.raml"
  private val defaultValue = "file://amf-client/shared/src/test/resources/api/shape-default.raml"
  private val profile      = "file://amf-client/shared/src/test/resources/api/validation/custom-profile.raml"
  //  private val banking       = "file://amf-client/shared/src/test/resources/api/banking.raml"
  private val apiWithSpaces =
    "file://amf-client/shared/src/test/resources/api/api-with-spaces/space in path api/api.raml"
  private val scalarAnnotations =
    "file://amf-client/shared/src/test/resources/org/raml/parser/annotation/scalar-nodes/input.raml"
  private val recursiveAdditionalProperties =
    "file://amf-client/shared/src/test/resources/recursive/recursive-additional-properties.yaml"
  private val knowledgeGraphServiceApi =
    "file://amf-client/shared/src/test/resources/production/knowledge-graph-service-api-1.0.13-raml/kg.raml"

  def testVocabulary(file: String, numClasses: Int, numProperties: Int): Future[Assertion] = {
    for {
      _    <- AMF.init().asFuture
      unit <- amf.Core.parser(Aml.name, "application/yaml").parseFileAsync(file).asFuture
    } yield {
      val declarations = unit.asInstanceOf[Vocabulary].declares.asSeq

      val classes    = declarations.collect { case term: ClassTerm    => term }
      val properties = declarations.collect { case prop: PropertyTerm => prop }

      assert(classes.size == numClasses)
      assert(properties.size == numProperties)
    }
  }

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

  test("Parsing default value string") {
    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseFileAsync(defaultValue).asFuture
    } yield {
      val declares = unit.asInstanceOf[DeclaresModel].declares.asSeq
      assert(declares.size == 1)
      assert(declares.head.isInstanceOf[NodeShape])
      val shape = declares.head.asInstanceOf[NodeShape]

      shape.defaultValueStr.value() shouldBe "name: roman\nlastname: riquelme\nage: 39"
      assert(shape.defaultValue.isInstanceOf[ObjectNode])
    }
  }

  test("Node value uses unescaped strings in RAML") {
    val expected = "The tag's name. This is typically a version (e.g., \"v0.0.1\")."
    val doc =
      """
        | #%RAML 1.0
        | title: 'The tag''s name. This is typically a version (e.g., "v0.0.1").'
        | baseUri: https://elmdv.symc.symantec.com/keybank/v1
        | version: 1.0
        |""".stripMargin
    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseStringAsync(doc).asFuture
    } yield {
      val webApi = unit._internal.asInstanceOf[InternalDocument].encodes
      webApi.fields.get(WebApiModel.Name).toString shouldBe expected
    }
  }

  test("Node value uses unescaped strings in OAS 20 YAML") {
    val expected = "The tag's name. This is typically a version (e.g., \"v0.0.1\")."
    val doc =
      """
        | swagger: "2.0"
        | info:
        |   title: 'The tag''s name. This is typically a version (e.g., "v0.0.1").'
        |   version: 1.0
        | paths: {}
        |""".stripMargin
    for {
      _    <- AMF.init().asFuture
      unit <- new Oas20YamlParser().parseStringAsync(doc).asFuture
    } yield {
      val webApi = unit._internal.asInstanceOf[InternalDocument].encodes
      webApi.fields.get(WebApiModel.Name).toString shouldBe expected
    }
  }

  test("Node value with double \\ should be unescaped") {
    val expected = """\\"""
    val doc =
      """
        | swagger: "2.0"
        | info:
        |   title: "\\\\"
        |   version: 1.0
        | paths: {}
        |""".stripMargin
    for {
      _    <- AMF.init().asFuture
      unit <- new Oas20YamlParser().parseStringAsync(doc).asFuture
    } yield {
      val webApi = unit._internal.asInstanceOf[InternalDocument].encodes
      webApi.fields.get(WebApiModel.Name).toString shouldBe expected
    }
  }

  test("Render / parse test RAML 0.8") {
    for {
      _      <- AMF.init().asFuture
      unit   <- new RamlParser().parseFileAsync(zencoder08).asFuture
      output <- new Raml08Renderer().generateString(unit).asFuture
      result <- new Raml08Parser().parseStringAsync(output).asFuture
    } yield {
      assertBaseUnit(result, "http://a.ml/amf/default_document")
    }
  }

  test("Render / parse test RAML 1.0") {
    for {
      _      <- AMF.init().asFuture
      unit   <- new RamlParser().parseFileAsync(zencoder).asFuture
      output <- new Raml10Renderer().generateString(unit).asFuture
      result <- new Raml10Parser().parseStringAsync(output).asFuture
    } yield {
      assertBaseUnit(result, "http://a.ml/amf/default_document")
    }
  }

  test("Source vendor RAML 1.0") {
    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseFileAsync(zencoder).asFuture
    } yield {
      unit.sourceVendor.asOption should be(Some(Raml10))
    }
  }

  test("Render / parse test OAS 2.0") {
    for {
      _      <- AMF.init().asFuture
      unit   <- new RamlParser().parseFileAsync(zencoder).asFuture
      output <- new Oas20Renderer().generateString(unit).asFuture
      result <- new Oas20Parser().parseStringAsync(output).asFuture
    } yield {
      assertBaseUnit(result, "http://a.ml/amf/default_document")
    }
  }

  test("Render / parse test OAS 3.0") {
    for {
      _      <- AMF.init().asFuture
      unit   <- new Oas30Parser().parseFileAsync(oas3).asFuture
      output <- new Oas30Renderer().generateString(unit).asFuture
    } yield {
      output should include("openIdConnectUrl")
    }
  }

  test("Render / parse test Async 2.0") {
    for {
      _      <- AMF.init().asFuture
      unit   <- new Async20Parser().parseFileAsync(async2).asFuture
      output <- new Async20Renderer().generateString(unit).asFuture
    } yield {
      output should include("Correlation ID Example")
    }
  }

  test("Render / parse test AMF") {
    for {
      _      <- AMF.init().asFuture
      unit   <- new RamlParser().parseFileAsync(zencoder).asFuture
      output <- new AmfGraphRenderer().generateString(unit).asFuture
      result <- new AmfGraphParser().parseStringAsync(output).asFuture
    } yield {
      assertBaseUnit(result, "http://a.ml/amf/default_document")
    }
  }

  test("Validation test") {
    for {
      _           <- AMF.init().asFuture
      unit        <- new RamlParser().parseFileAsync(zencoder).asFuture
      report      <- AMF.validate(unit, RamlProfile, AMFStyle).asFuture
      profileName <- AMF.loadValidationProfile(profile).asFuture
      custom      <- AMF.validate(unit, profileName, AMFStyle).asFuture
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
      report   <- AMF.validate(resolved, RamlProfile, AMFStyle).asFuture
    } yield {
      assert(report.conforms)
    }
  }

  test("Vocabularies test") {
    for {
      _           <- AMF.init().asFuture
      dialectName <- AMF.registerDialect(demosDialect).asFuture
      unit        <- new Aml10Parser().parseFileAsync(demosInstance).asFuture
      report      <- AMF.validate(unit, ProfileName("Eng Demos 0.1"), AMFStyle).asFuture
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
      unit        <- new Aml10Parser().parseFileAsync(demosInstance).asFuture
      report      <- AMF.validate(unit, ProfileName("Eng Demos 0.1"), AMFStyle).asFuture
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
      unit   <- amf.Core.parser(Raml10.name, "application/yaml").parseFileAsync(music).asFuture
      report <- AMF.validate(unit, RamlProfile, RAMLStyle).asFuture
    } yield {
      assert(!unit.references().asSeq.map(_.location).contains(null))
      assert(report.conforms)
    }
  }

  test("Scalar Annotations") {
    for {
      _    <- AMF.init().asFuture
      unit <- amf.Core.parser(Raml10.name, "application/yaml").parseFileAsync(scalarAnnotations).asFuture
    } yield {
      val api         = unit.asInstanceOf[Document].encodes.asInstanceOf[Api]
      val annotations = api.name.annotations().custom().asSeq
      annotations should have size 1
      val annotation = annotations.head
      annotation.name.value() should be("foo")
      annotation.extension.asInstanceOf[ScalarNode].value.value() should be("annotated title")
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
            .withReference("http://a.ml/vocabularies/doc#")
        ).toClient)*/

    assert(vocab.base.option.asOption.isDefined)
    assert(vocab.base.is("http://test.com/vocab#"))
    assert(vocab.description.option.asOption.isDefined)
    assert(vocab.description.is("Just a small sample vocabulary"))

    val propertyTerm = new DatatypePropertyTerm()
      .withId("http://a.ml/vocabularies/doc#test")
      .withRange("http://www.w3.org/2001/XMLSchema#string")

    val classTerm = new ClassTerm()
      .withId("http://test.com/vocab#Class")
      .withDescription("A sample class")
      .withDisplayName("Class")
//      .withSubClassOf(Seq("http://test.com/vocabulary/other#Class").asClient)
//      .withProperties(Seq("http://a.ml/vocabularies/doc#test").asClient)

    vocab.withDeclaredElement(classTerm).withDeclaredElement(propertyTerm)

    for {
      _      <- AMF.init().asFuture
      render <- amf.Core.generator("RAML Vocabulary", "application/yaml").generateString(vocab).asFuture
    } yield {
      render should be(
        """#%Vocabulary 1.0
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
        |  raml-doc: http://a.ml/vocabularies/doc#
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

  test("Parsing text document with base url") {
    val baseUrl = "http://test.com/myApp"
    testParseStringWithBaseUrl(baseUrl)
  }

  test("Parsing text document with base url (domain only)") {
    val baseUrl = "http://test.com"
    testParseStringWithBaseUrl(baseUrl)
  }

  test("Parsing text document with base url (with include, without trailing slash)") {
    val baseUrl = "file://amf-client/shared/src/test/resources/includes"
    testParseStringWithBaseUrlAndInclude(baseUrl)
  }

  test("Parsing text document with base url (with include and trailing slash)") {
    val baseUrl = "file://amf-client/shared/src/test/resources/includes/"
    testParseStringWithBaseUrlAndInclude(baseUrl)
  }

  test("Parsing text document with base url (with include and file name)") {
    val baseUrl = "file://amf-client/shared/src/test/resources/includes/api.raml"
    testParseStringWithBaseUrlAndInclude(baseUrl)
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

  test("Environment returning bad uri test") {
    val name = "api.raml"

    val input = s"""
                   |#%RAML 1.0
                   |title: Environment test
    """.stripMargin

    val name2 = "api2"

    case class BadIRIResourceLoader() extends ResourceLoader {

      import amf.client.convert.WebApiClientConverters._

      override def fetch(resource: String): ClientFuture[Content] =
        Future.successful(new Content(input, resource)).asClient

      override def accepts(resource: String): Boolean = true
    }

    val environment = Environment.empty().add(BadIRIResourceLoader().asInstanceOf[ClientLoader])

    for {
      _     <- AMF.init().asFuture
      unit  <- new RamlParser(environment).parseFileAsync(name).asFuture
      unit2 <- new RamlParser(environment).parseFileAsync(name2).asFuture
    } yield {
      unit shouldBe a[Document]
      unit.id should be("file://api.raml")
      unit2.id should be("file://api2")
    }
  }

  test("Generate to writer and exit") {
    val input = s"""
                   |#%RAML 1.0
                   |title: Environment test
                   |version: 32.0.7
    """.stripMargin

    val buffer = LimitedStringBuffer(450)
    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseStringAsync(input).asFuture
      e    <- new AmfGraphRenderer().generateToWriter(unit, buffer).asFuture.failed
    } yield {
      e shouldBe a[LimitReachedException]

      buffer.toString() should include("http://a.ml/vocabularies/document#RootDomainElement")
    }
  }

  test("Generate to doc builder") {
    val input = s"""
                   |#%RAML 1.0
                   |title: Environment test
                   |version: 32.0.7
    """.stripMargin

    val builder = JsonOutputBuilder()
    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseStringAsync(input).asFuture
      e    <- new AmfGraphRenderer().generateToBuilder(unit, builder).asFuture
    } yield {
      builder.result.toString should include("\"http://a.ml/vocabularies/core#version\"")
    }
  }

  test("Environment resource not loaded exception") {
    val name = "api.raml"

    val input = s"""
                   |#%RAML 1.0
                   |title: Environment test
                   |types:
                   |  A: !include not-exists.raml
    """.stripMargin

    case class ForFailResourceLoader() extends ResourceLoader {

      import amf.client.convert.WebApiClientConverters._

      override def fetch(resource: String): ClientFuture[Content] = {
        val f =
          if (resource.endsWith("api.raml")) Future.successful(new Content(input, resource))
          else
            Future.failed(new ResourceNotFound(s"Cannot find resource $resource"))

        f.asClient
      }

      override def accepts(resource: String): Boolean = true
    }

    val environment = Environment.empty().add(ForFailResourceLoader().asInstanceOf[ClientLoader])

    for {
      _      <- AMF.init().asFuture
      unit   <- new RamlParser(environment).parseFileAsync(name).asFuture
      report <- AMF.validate(unit, Raml10Profile, RAMLStyle).asFuture
    } yield {
      report.conforms should be(false)
      report.results.asSeq.exists(_.message.equals("Cannot find resource not-exists.raml")) should be(true)
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

  test("Missing converter error") {
    val options = new RenderOptions().withoutSourceMaps

    for {
      _        <- AMF.init().asFuture
      unit     <- amf.Core.parser(Raml10.name, "application/yaml").parseFileAsync(amflight).asFuture
      resolved <- Future.successful(AMF.resolveRaml10(unit))
    } yield {
      val webapi = resolved.asInstanceOf[Document].encodes.asInstanceOf[Api]
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

  test("Test swagger 2.0 entry generation in yaml") {
    val expected =
      """
        |swagger: "2.0"
        |info:
        | title: test swagger entry
        | version: "1.0"
        |paths:
        | /endpoint:
        |  get:
        |    responses:
        |      "200":
        |       description: a descrip""".stripMargin
    for {
      _   <- AMF.init().asFuture
      doc <- Future { buildBasicApi() }
      generated <- new Renderer(Oas20.name, "application/yaml", None)
        .generateString(doc)
        .asFuture
    } yield {
      val deltas = Diff.ignoreAllSpace.diff(expected, generated)
      if (deltas.nonEmpty) fail("Expected and golden are different: " + Diff.makeString(deltas))
      else succeed
    }
  }

  test("Test swagger ref generation in yaml") {
    val expected =
      """|swagger: "2.0"
         |info:
         |  title: test swagger entry
         |  version: "1.0"
         |paths:
         |  /endpoint:
         |    get:
         |      parameters:
         |        -
         |          x-amf-mediaType: application/json
         |          in: body
         |          name: someName
         |          schema:
         |            $ref: "#/definitions/person"
         |      responses:
         |        "200":
         |          description: a descrip
         |definitions:
         |  person:
         |    type: object
         |    properties:
         |      name:
         |        type: string""".stripMargin
    for {
      _   <- AMF.init().asFuture
      doc <- Future { buildApiWithTypeTarget() }
      generated <- new Renderer(Oas20.name, "application/yaml", None)
        .generateString(doc)
        .asFuture
    } yield {
      val deltas = Diff.ignoreAllSpace.diff(expected, generated)
      if (deltas.nonEmpty) fail("Expected and golden are different: " + Diff.makeString(deltas))
      else succeed
    }
  }

  test("Test any shape default empty") {
    val api =
      """
        |#%RAML 1.0
        |title: test swagger entry
        |/endpoint:
        |   get:
        |     body:
        |       application/json:
        |   post:
        |     body:
        |       application/json:
        |           example: |
        |             { "name": "roman", "lastname": "riquelme"}
        |   put:
        |     body:
        |       application/json:
        |           type: any
        |           example: |
        |             { "name": "roman", "lastname": "riquelme"}
        |   patch:
        |     body:
        |       application/json:
        |           type: object
        |           example: |
        |             { "name": "roman", "lastname": "riquelme"}
        |   delete:
        |     body:
        |       application/json:
        |           type: string""".stripMargin
    for {
      _   <- AMF.init().asFuture
      doc <- AMF.ramlParser().parseStringAsync(api).asFuture
    } yield {

      val seq = doc.asInstanceOf[Document].encodes.asInstanceOf[Api].endPoints.asSeq.head.operations.asSeq
      def assertDefault(method: String, expected: Boolean) =
        seq
          .find(_.method.value().equals(method))
          .get
          .request
          .payloads
          .asSeq
          .head
          .schema
          .asInstanceOf[AnyShape]
          .isDefaultEmpty should be(expected)

      assertDefault("get", expected = true)
      assertDefault("post", expected = true)
      assertDefault("put", expected = false)
      assertDefault("patch", expected = false)
      assertDefault("delete", expected = false)

    }
  }

  private def buildBasicApi() = {
    val api: Api = new Api().withName("test swagger entry")

    api.withEndPoint("/endpoint").withOperation("get").withResponse("200").withDescription("a descrip")
    new Document().withEncodes(api)

  }

  private def buildApiWithTypeTarget() = {
    val doc = buildBasicApi()

    val shape     = new ScalarShape().withDataType((Xsd + "string").iri())
    val nodeShape = new NodeShape().withName("person")
    nodeShape.withProperty("name").withRange(shape)
    doc.withDeclaredElement(nodeShape)

    val linked: NodeShape = nodeShape.link(Some("#/definitions/person"))
    linked.withName("Person")
    doc.encodes
      .asInstanceOf[Api]
      .endPoints
      .asSeq
      .head
      .operations
      .asSeq
      .head
      .withRequest()
      .withPayload("application/json")
      .withName("someName")
      .withSchema(linked)
    doc
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
      val webApi = unit.asInstanceOf[Document].encodes.asInstanceOf[Api]
      val dataNode = webApi.endPoints.asSeq.head.operations.asSeq.head.responses.asSeq.head.payloads.asSeq.head.schema
        .asInstanceOf[AnyShape]
        .examples
        .asSeq
        .head
        .structuredValue
      assert(dataNode._internal.meta.`type`.head.iri() == (Namespace.Data + "Object").iri())
    }
  }

  test("Test name in property shape") {
    val api =
      """
        |#%RAML 1.0
        |title: this should remain
        |
        |types:
        | person:
        |   properties:
        |     name: string
      """.stripMargin

    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseStringAsync(api).asFuture
    } yield {
      val nodeShape = unit.asInstanceOf[Document].declares.asSeq.head.asInstanceOf[NodeShape]
      nodeShape.properties.asSeq.head.name.value() should be("name")
    }
  }

  test("Test order of uri parameter") {
    val api =
      """
        |#%RAML 1.0
        |---
        |title: RAML 1.0 Uri param
        |version: v1
        |
        |/part:
        |  get:
        |  /{uriParam1}:
        |    uriParameters:
        |      uriParam1:
        |        type: integer
        |    get:
        |    /{uriParam2}:
        |      uriParameters:
        |        uriParam2:
        |          type: number
        |      get:
        |      /{uriParam3}:
        |        uriParameters:
        |          uriParam3:
        |            type: boolean
        |        get:
      """.stripMargin

    for {
      _        <- AMF.init().asFuture
      unit     <- new RamlParser().parseStringAsync(api).asFuture
      resolved <- Future { new Raml10Resolver().resolve(unit) }
    } yield {
      val pathParamters: List[Parameter] = resolved
        .asInstanceOf[Document]
        .encodes
        .asInstanceOf[Api]
        .endPoints
        .asSeq
        .last
        .parameters
        .asSeq
        .filter(_.binding.value().equals("path"))
        .toList

      assert(pathParamters.head.name.value().equals("uriParam1"))
      assert(pathParamters(1).name.value().equals("uriParam2"))
      assert(pathParamters(2).name.value().equals("uriParam3"))

    }
  }

  test("Test order of base uri parameter ") {
    val api =
      """
        |#%RAML 1.0
        |---
        |title: RAML 1.0 Uri param
        |version: v1
        |baseUri: https://www.example.com/api/{v1}/{v2}
        |
        |baseUriParameters:
        |  v2:
        |  v1:
        |    type: string""".stripMargin

    for {
      _        <- AMF.init().asFuture
      unit     <- new RamlParser().parseStringAsync(api).asFuture
      resolved <- Future { new Raml10Resolver().resolve(unit) }
    } yield {
      val baseParameters: Seq[Parameter] =
        resolved.asInstanceOf[Document].encodes.asInstanceOf[Api].servers.asSeq.head.variables.asSeq

      assert(baseParameters.head.name.value().equals("v1"))
      assert(baseParameters(1).name.value().equals("v2"))

    }
  }

  test("Test order of raml 08 form parameters ") {
    val api =
      """
        |#%RAML 0.8
        |---
        |title: RAML 1.0 Uri param
        |version: v1
        |
        |/multipart:
        |  post:
        |    body:
        |      multipart/form-data:
        |        formParameters:
        |          first:
        |            type: string
        |            required: true
        |          second:
        |            type: string
        |            default: segundo
        |          third:
        |            type: boolean
        |    responses:
        |      201: ~
      """.stripMargin

    for {
      _        <- AMF.init().asFuture
      unit     <- new RamlParser().parseStringAsync(api).asFuture
      resolved <- Future { new Raml08Resolver().resolve(unit) }
    } yield {
      val shape: Shape = unit
        .asInstanceOf[Document]
        .encodes
        .asInstanceOf[Api]
        .endPoints
        .asSeq
        .head
        .operations
        .asSeq
        .head
        .request
        .payloads
        .asSeq
        .head
        .schema

      assert(shape.isInstanceOf[NodeShape])
      val properties = shape.asInstanceOf[NodeShape].properties.asSeq
      assert(properties.head.name.value().equals("first"))
      assert(properties(1).name.value().equals("second"))
      assert(properties(2).name.value().equals("third"))
    }
  }

  private def removeFields(unit: BaseUnit): Future[BaseUnit] = Future {
    val webApi = unit.asInstanceOf[Document].encodes.asInstanceOf[Api]
    webApi.description.remove()
    val operation: Operation = webApi.endPoints.asSeq.head.operations.asSeq.head
    operation.graph().remove("http://a.ml/vocabularies/apiContract#returns")

    webApi.graph().remove("http://a.ml/vocabularies/core#license")
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
      unit <- amf.Core.parser(Raml10.name, "application/yaml").parseStringAsync(baseUrl, spec).asFuture
    } yield {
      assert(unit.location.startsWith(baseUrl))
      val encodes = unit.asInstanceOf[Document].encodes
      assert(encodes.id.startsWith(baseUrl))
      assert(encodes.asInstanceOf[Api].name.is("Some title"))
    }
  }

  private def testParseStringWithBaseUrlAndInclude(baseUrl: String) = {
    val spec =
      """#%RAML 1.0
        |title: Some title
        |
        |/test:
        |  get:
        |    body:
        |      application/json:
        |        type: !include include1.json""".stripMargin

    for {
      _    <- AMF.init().asFuture
      unit <- AMF.raml10Parser().parseStringAsync(baseUrl, spec).asFuture
      res  <- Future.successful(AMF.resolveRaml10(unit))
      gen  <- AMF.raml10Generator().generateString(res).asFuture
    } yield {
      gen should not include ("!include")
      gen should include("type: string")
    }
  }

  private def assertBaseUnit(baseUnit: BaseUnit, expectedLocation: String): Assertion = {
    assert(baseUnit.location == expectedLocation)
    val api       = baseUnit.asInstanceOf[Document].encodes.asInstanceOf[Api]
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
    assert(typeIds.contains("http://a.ml/vocabularies/shapes#ScalarShape"))
    assert(typeIds.contains("http://www.w3.org/ns/shacl#Shape"))
    assert(typeIds.contains("http://a.ml/vocabularies/shapes#Shape"))
    assert(typeIds.contains("http://a.ml/vocabularies/document#DomainElement"))

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

  test("Test validate payload with invalid iri") {
    val payload = """test payload""".stripMargin
    for {
      _ <- AMF.init().asFuture
      shape <- Future {
        new ScalarShape()
          .withDataType("http://www.w3.org/2001/XMLSchema#string")
          .withName("test")
          .withId("api.raml/#/webapi/schema1")
      }
      report <- shape.asInstanceOf[AnyShape].validate(payload).asFuture
    } yield {
      assert(report.conforms)
    }
  }

  test("Generate unit with source maps") {
    val options = new RenderOptions().withSourceMaps

    for {
      _ <- AMF.init().asFuture
      unit <- amf.Core
        .parser(Raml10.name, "application/yaml")
        .parseFileAsync(banking)
        .asFuture // could be use a smaller api for this test?
      jsonld <- amf.Core.generator("AMF Graph", "application/ld+json").generateString(unit, options).asFuture
    } yield {
      jsonld should include("[(1,0)-(252,0)]")
    }
  }

  test("Generate unit without source maps") {
    val options = new RenderOptions().withoutSourceMaps

    for {
      _      <- AMF.init().asFuture
      unit   <- amf.Core.parser(Raml10.name, "application/yaml").parseFileAsync(banking).asFuture
      jsonld <- amf.Core.generator("AMF Graph", "application/ld+json").generateString(unit, options).asFuture
    } yield {
      jsonld should not include "[(3,0)-(252,0)]"
    }
  }

  test("Generate unit with compact uris") {
    val options = new RenderOptions().withCompactUris.withSourceMaps

    for {
      _      <- AMF.init().asFuture
      unit   <- amf.Core.parser(Raml10.name, "application/yaml").parseFileAsync(banking).asFuture
      jsonld <- amf.Core.generator("AMF Graph", "application/ld+json").generateString(unit, options).asFuture
    } yield {
      jsonld should include("@context")
    }
  }

  multiGoldenTest("Generate unit with compact uris and external file",
                  "file://amf-client/shared/src/test/resources/resolution/external-data-type/api.%s") { config =>
    val apiPath = "file://amf-client/shared/src/test/resources/resolution/external-data-type/api.raml"
    val golden  = config.golden

    // TODO migrate to render options converter
    var options = new RenderOptions().withCompactUris.withSourceMaps.withPrettyPrint
    if (config.renderOptions.isFlattenedJsonLd) {
      options = options.withFlattenedJsonLd
    } else {
      options = options.withoutFlattenedJsonLd
    }

    for {
      _      <- AMF.init().asFuture
      parsed <- amf.Core.parser(Raml10.name, "application/yaml").parseFileAsync(apiPath).asFuture
//      resolved <- Future.successful(amf.Core.resolver(Raml10.name).resolve(parsed, ResolutionPipeline.DEFAULT_PIPELINE))
      jsonLd <- amf.Core.generator("AMF Graph", "application/ld+json").generateString(parsed, options).asFuture
      actual <- writeTemporaryFile(golden)(jsonLd)
      r      <- assertDifferences(actual, golden)
    } yield {
      r
    }
  }

  test("banking-api-test") {
    for {
      _    <- AMF.init().asFuture
      unit <- amf.Core.parser(Raml10.name, "application/yaml").parseFileAsync(banking).asFuture
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

  test("Parsing external xml shape") {
    for {
      _ <- AMF.init().asFuture
      unit <- new RamlParser()
        .parseFileAsync("file://amf-client/shared/src/test/resources/production/raml10/xsdschema/api.raml")
        .asFuture
    } yield {
      val location: Option[String] =
        unit.asInstanceOf[Document].declares.asSeq.head.asInstanceOf[SchemaShape].location.asOption
      location.isDefined should be(true)
      location.get should be("file://amf-client/shared/src/test/resources/production/raml10/xsdschema/schema.xsd")

    }
  }

  test("Parsing external xml example") {
    for {
      _ <- AMF.init().asFuture
      unit <- new RamlParser()
        .parseFileAsync("file://amf-client/shared/src/test/resources/production/raml10/xsdexample/api.raml")
        .asFuture
    } yield {
      val location: Option[String] = unit
        .asInstanceOf[Document]
        .declares
        .asSeq
        .head
        .asInstanceOf[AnyShape]
        .examples
        .asSeq
        .head
        .location
        .asOption
      location.isDefined should be(true)
      location.get should be("file://amf-client/shared/src/test/resources/production/raml10/xsdexample/example.xsd")
    }
  }

  test("Parsing external xml with inner ref annotation") {
    for {
      _ <- AMF.init().asFuture
      unit <- new RamlParser()
        .parseFileAsync(
          "file://amf-client/shared/src/test/resources/production/raml10/xsdschema-withfragmentref/api.raml")
        .asFuture
    } yield {
      val shape = unit
        .asInstanceOf[Document]
        .declares
        .asSeq
        .head
        .asInstanceOf[AnyShape]
      shape.isInstanceOf[SchemaShape] should be(true)
      shape.annotations().fragmentName().asOption.get should be("address")
    }
  }

  test("Parsing external json with inner ref annotation") {
    for {
      _ <- AMF.init().asFuture
      unit <- new RamlParser()
        .parseFileAsync(
          "file://amf-client/shared/src/test/resources/production/raml10/jsonschema-apiwithfragmentref/api.raml")
        .asFuture
    } yield {
      val shape = unit
        .asInstanceOf[Document]
        .declares
        .asSeq
        .head
        .asInstanceOf[AnyShape]
      shape.annotations().fragmentName().asOption.get should be("/definitions/address")
    }
  }

  test("Test validate with typed enum amf pair method") {
    for {
      _ <- AMF.init().asFuture
      unit <- new Raml10Parser()
        .parseFileAsync(scalarAnnotations)
        .asFuture
      v <- AMF.validate(unit, RamlProfile, RamlProfile.messageStyle).asFuture
    } yield {
      assert(v.conforms)
    }
  }

  // in fact the change were do it at parsing time (abstract declaration parser). I change the hashmap for a list map of the properties to preserve order, so this test could be parse and dump but i wanna be sure that nobody will change the resolved params order in any other place.
  test("Test query parameters order") {
    for {
      _ <- AMF.init().asFuture
      unit <- new Raml08Parser()
        .parseFileAsync("file://amf-client/shared/src/test/resources/clients/params-order.raml")
        .asFuture
      v <- Future.successful(new Raml08Resolver().resolve(unit))
    } yield {
      val seq = v
        .asInstanceOf[Document]
        .encodes
        .asInstanceOf[Api]
        .endPoints
        .asSeq
        .head
        .operations
        .asSeq
        .head
        .request
        .queryParameters
        .asSeq
      seq.head.name.value() should be("code")
      seq(1).name.value() should be("size")
      seq(2).name.value() should be("color")
      seq(3).name.value() should be("description")

    }
  }

  // extract to some kind of client tests in another proyect?
  test("Test custom domain property id after parse") {

    for {
      _ <- AMF.init().asFuture
      doc <- Future {
        val ns            = (Namespace.Xsd + "string").iri()
        val doc: Document = new Document()
        doc._internal.withId("http://location.com/myfile")
        val shape = new ScalarShape().withName("scalarDeclared").withDataType(ns)
        doc.withDeclaredElement(shape)
        val wa = new Api().withName("test")
        doc.withEncodes(wa)
        val annotationType =
          new CustomDomainProperty()
            .withName("forDescribedBy")
            .withId("http://location.com/myfile#/declarations/annotations/forDescribedBy")
            .withSchema(new ScalarShape().withName("scalarName").withDataType(ns))
        doc.withDeclaredElement(annotationType)
        val annotation = amf.core.model.domain.extensions
          .DomainExtension()
          .withExtension(new ScalarNode("extension", ns)._internal)
          .withDefinedBy(annotationType._internal)
          .withName(annotationType.name.value())
        shape.withCustomDomainProperties(Seq(annotation).asClient)
        doc
      }
      s      <- new Raml10Renderer().generateString(doc).asFuture
      parsed <- new Raml10Parser().parseStringAsync("http://location.com/myfile", s).asFuture
    } yield {
      val buildedProp: CustomDomainProperty =
        doc.declares.asSeq.collectFirst({ case s: Shape => s.customDomainProperties.asSeq.head.definedBy }).get

      val parsedProp: CustomDomainProperty = parsed
        .asInstanceOf[Document]
        .declares
        .asSeq
        .collectFirst({ case s: Shape => s.customDomainProperties.asSeq.head.definedBy })
        .get
      parsedProp.id should be(buildedProp.id)
    }
  }

  ignore("Handle 404 status code while fetching included file") { // ignored due error in jenkins, review this
    for {
      _ <- AMF.init().asFuture
      a <- AMF
        .raml08Parser()
        .parseFileAsync(
          "file://amf-client/shared/src/test/resources/parser-results/raml/error/not-existing-http-include.raml")
        .asFuture
      r <- AMF.validate(a, Raml08Profile, RAMLStyle).asFuture
    } yield {
      r.conforms should be(false)
      val seq = r.results.asSeq
      seq.size should be(2)
      val statusCode = seq.head
      statusCode.level should be("Violation")

      // hack to avoid that this test fail when you don't have internet connection.If you have internet, the a.ml domain will return an 404 error,
      // but if you dont have internet connection, you will not reach the a.ml host, so it will be an unknown host exception violation.

      statusCode.message should (endWith("Unexpected status code '404' for resource 'https://a.ml/notexists'") or
        endWith("Network Error: a.ml") or
        endWith("java.net.SocketTimeoutException: connect timed out") or
        endWith(
          "javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target"))
      statusCode.position should be(Range((6, 10), (6, 41)))

      val unresolvedRef = seq.last
      unresolvedRef.level should be("Violation")
      unresolvedRef.message should startWith("Unresolved reference 'https://a.ml/notexists'")
      unresolvedRef.position should be(Range((6, 10), (6, 41)))
    }
  }

  test("Test search tracked example") {
    for {
      _ <- AMF.init().asFuture
      a <- AMF
        .raml10Parser()
        .parseFileAsync("file://amf-client/shared/src/test/resources/resolution/payloads-examples-resolution.raml")
        .asFuture
    } yield {
      val r          = new Raml10Resolver().resolve(a)
      val operations = r.asInstanceOf[Document].encodes.asInstanceOf[Api].endPoints.asSeq.head.operations.asSeq
      val getOp      = operations.find(_.method.value().equals("get")).get
      val option = getOp.request.payloads.asSeq.head.schema
        .asInstanceOf[AnyShape]
        .trackedExample(
          "file://amf-client/shared/src/test/resources/resolution/payloads-examples-resolution.raml#/web-api/end-points/%2Fendpoint1/get/request/application%2Fjson")
        .asOption
      option.isDefined should be(true)
      option.get.annotations().isTracked should be(true)

      val getPost = operations.find(_.method.value().equals("post")).get
      val shape   = getPost.request.payloads.asSeq.head.schema.asInstanceOf[AnyShape]
      val option2 = shape
        .trackedExample(
          "file://amf-client/shared/src/test/resources/resolution/payloads-examples-resolution.raml#/web-api/end-points/%2Fendpoint1/get/request/application%2Fjson")
        .asOption
      option2.isDefined should be(true)
      option2.get.annotations().isTracked should be(true)

      shape.examples.asSeq
        .find(_.id.equals(
          "file://amf-client/shared/src/test/resources/resolution/payloads-examples-resolution.raml#/declarations/types/A/example/declared"))
        .head
        .annotations()
        .isTracked should be(false)
    }
  }

  test("Test accessor to double parsed field") {
    for {
      _ <- AMF.init().asFuture
      unit <- new Raml10Parser()
        .parseFileAsync("file://amf-client/shared/src/test/resources/clients/double-field.raml")
        .asFuture
    } yield {
      val shape = unit.asInstanceOf[Document].declares.asSeq.head.asInstanceOf[ScalarShape]
      shape.minimum.value() should be(1.1)
      succeed
    }
  }

  test("Test invalid mime type at lib include") {
    val include = "amf://types/Person.raml"

    val input = s"""
                   |#%RAML 1.0
                   |title: test
                   |uses:
                   |  lib: http://mylib.com
                   |types:
                   |  Person: lib.A
    """.stripMargin

    val lib = """|#%RAML 1.0 Library
                 |types:
                 |  A:
                 |    properties:
                 |      name: string
                 """.stripMargin

    import amf.client.convert.WebApiClientConverters._

    case class TestResourceLoader() extends ResourceLoader {
      override def fetch(resource: String): ClientFuture[Content] =
        Future.successful(new Content(lib, resource, Some("text/plain"))).asClient

      override def accepts(resource: String): Boolean = true
    }

    val environment = Environment
      .empty()
      .add(TestResourceLoader().asInstanceOf[ClientLoader])

    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser(environment).parseStringAsync(input).asFuture
      v    <- AMF.validate(unit, Raml10Profile, RAMLStyle).asFuture
    } yield {
      v.conforms should be(true)
      val declarations = unit.asInstanceOf[Document].declares.asSeq
      declarations should have size 1
    }
  }

  test("Test yaml swagger 2.0 api") {

    val environment = DefaultEnvironment()

    for {
      _ <- AMF.init().asFuture
      unit <- new Oas20YamlParser(environment)
        .parseFileAsync("file://amf-client/shared/src/test/resources/clients/oas20-yaml.yaml")
        .asFuture
    } yield {
      val location: String = unit.location
      assert(location != "")
    }
  }

  test("Test yaml swagger 2.0 api with json parser") {

    val environment = DefaultEnvironment()
    recoverToSucceededIf[UnsupportedVendorException] {
      AMF.init().asFuture.flatMap { _ =>
        new Oas20Parser(environment)
          .parseFileAsync("file://amf-client/shared/src/test/resources/clients/oas20-yaml.yaml")
          .asFuture
          .map { _ =>
            succeed
          }
      }
    }

  }

  test("Test path resolution OAS for 'file:///' prefix") {
    val file    = platform.fs.syncFile("amf-client/shared/src/test/resources/clients/toupdir-include/spec/swagger.json")
    val absPath = getAbsolutePath(file.path)
    for {
      _      <- AMF.init().asFuture
      unit   <- new Oas20Parser().parseFileAsync(absPath).asFuture
      report <- AMF.validate(unit, RamlProfile, AMFStyle).asFuture
    } yield {
      assert(report.conforms)
    }
  }

  test("Test data nodes client convertions") {
    val scalarNode: InternalScalarNode =
      InternalScalarNode("myValue", Some((Namespace.Xsd + "String").iri())).withId("amf://id2")
    val scalarNode2: InternalScalarNode =
      InternalScalarNode("myValue2", Some((Namespace.Xsd + "String").iri())).withId("amf://id4")
    val arrayNode: InternalArrayNode = InternalArrayNode().withId("amf://id3")
    arrayNode.addMember(scalarNode2)
    val objectNode: InternalObjectNode = InternalObjectNode().withId("amf://id2").addProperty("myProp1", arrayNode)
    arrayNode.addMember(objectNode)
    val document: InternalDocument = InternalDocument()
      .withId("amf://id1")
      .withLocation("http://local.com")
      .withEncodes(scalarNode)
      .withDeclares(Seq(arrayNode))

    for {
      _ <- AMF.init().asFuture
    } yield {
      val clientUnit = BaseUnitMatcher.asClient(document)
      val clientDoc  = clientUnit.asInstanceOf[amf.client.model.document.Document]
      clientDoc.encodes.isInstanceOf[amf.client.model.domain.ScalarNode] shouldBe true
      val clientArray = clientDoc.declares.asSeq.head.asInstanceOf[ArrayNode]
      clientArray.isInstanceOf[amf.client.model.domain.ArrayNode] shouldBe true
      clientArray
        .asInstanceOf[amf.client.model.domain.ArrayNode]
        .members
        .asSeq
        .head
        .isInstanceOf[ScalarNode] shouldBe true
      clientArray
        .asInstanceOf[amf.client.model.domain.ArrayNode]
        .members
        .asSeq(1)
        .isInstanceOf[ObjectNode] shouldBe true

    }
  }

  test("Test Json relative ref with absolute path as input") {
    val file =
      platform.fs.syncFile("amf-client/shared/src/test/resources/production/json-schema-relative-ref/api.raml")
    val absPath = getAbsolutePath(file.path)

    for {
      _      <- AMF.init().asFuture
      unit   <- new RamlParser().parseFileAsync(absPath).asFuture
      report <- AMF.validate(unit, Raml10Profile, AMFStyle).asFuture
    } yield {
      assert(report.conforms)
    }
  }

  test("Test quoted default value") {
    val file = "file://amf-client/shared/src/test/resources/validations/default-with-quotes.raml"
    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseFileAsync(file).asFuture
    } yield {
      assert(
        unit.asInstanceOf[Document].declares.asSeq.head.asInstanceOf[Shape].defaultValueStr.value() == "A default")
    }
  }

  test("Test emission of json schema of a shape with file type") {
    val api = """#%RAML 1.0
                |
                |title: test
                |
                |types:
                |  SomeType:
                |    type: object
                |    properties:
                |      a: integer
                |      b:
                |        type: file
                |        fileTypes: ['image/jpeg', 'image/png']
                |        example: I'm a file""".stripMargin
    for {
      _        <- AMF.init().asFuture
      unit     <- new RamlParser().parseStringAsync(api).asFuture
      resolved <- Future(new Raml10Resolver().resolve(unit, ResolutionPipeline.EDITING_PIPELINE))
      report   <- AMF.validateResolved(unit, Raml10Profile, AMFStyle).asFuture
      json <- Future(
        resolved.asInstanceOf[DeclaresModel].declares.asSeq.head.asInstanceOf[NodeShape].buildJsonSchema())
    } yield {
      val golden = """{
                     |  "$schema": "http://json-schema.org/draft-04/schema#",
                     |  "$ref": "#/definitions/SomeType",
                     |  "definitions": {
                     |    "SomeType": {
                     |      "type": "object",
                     |      "additionalProperties": true,
                     |      "required": [
                     |        "a",
                     |        "b"
                     |      ],
                     |      "properties": {
                     |        "a": {
                     |          "type": "integer"
                     |        },
                     |        "b": {
                     |          "type": "string",
                     |          "example": "I'm a file"
                     |        }
                     |      }
                     |    }
                     |  }
                     |}
                     |""".stripMargin
      assert(report.conforms)
      assert(json == golden)
    }
  }

  test("Test emission of json schema of a shape with a recursive type") {
    val api = "file://amf-client/shared/src/test/resources/validations/recursive-types.raml"
    for {
      _        <- AMF.init().asFuture
      unit     <- new RamlParser().parseFileAsync(api).asFuture
      resolved <- Future(new Raml10Resolver().resolve(unit, ResolutionPipeline.EDITING_PIPELINE))
    } yield {
      val golden    = """{
                        |  "$schema": "http://json-schema.org/draft-04/schema#",
                        |  "$ref": "#/definitions/C",
                        |  "definitions": {
                        |    "C": {
                        |      "type": "object",
                        |      "additionalProperties": true,
                        |      "properties": {
                        |        "c1": {
                        |          "items": {
                        |            "$ref": "#/definitions/A"
                        |          },
                        |          "type": "array"
                        |        }
                        |      }
                        |    },
                        |    "A": {
                        |      "type": "object",
                        |      "additionalProperties": true,
                        |      "properties": {
                        |        "a1": {
                        |          "type": "object",
                        |          "additionalProperties": true,
                        |          "properties": {
                        |            "c1": {
                        |              "items": {
                        |                "$ref": "#/definitions/A"
                        |              },
                        |              "type": "array"
                        |            }
                        |          }
                        |        }
                        |      }
                        |    }
                        |  }
                        |}
                        |""".stripMargin
      val generated = resolved.asInstanceOf[Document].declares.asSeq(2).asInstanceOf[NodeShape].buildJsonSchema()
      assert(generated == golden)
    }
  }

  // This test is here because of I need to resolve with default and then validate
  test("Test json schema emittion of recursive union shape") {
    val file = "file://amf-client/shared/src/test/resources/validations/recursion-union.raml"
    for {
      _        <- AMF.init().asFuture
      unit     <- new RamlParser().parseFileAsync(file).asFuture
      resolved <- Future.successful(AMF.resolveRaml10(unit))
      report   <- AMF.validateResolved(resolved, RamlProfile, AMFStyle).asFuture
    } yield {
      assert(report.conforms)
    }
  }

  test("Test external fragment that includes a reference") {
    val file = "file://amf-client/shared/src/test/resources/resolution/ex-frag-with-refs/api.raml"
    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseFileAsync(file).asFuture
    } yield {
      // Check that the external fragment has references
      assert(unit.references().asSeq.head.references().asSeq.nonEmpty)
    }
  }

  test("Test uri references to external reference from external reference are not encoded") {
    for {
      _    <- AMF.init().asFuture
      unit <- new RamlParser().parseFileAsync(apiWithSpaces).asFuture
    } yield {
      val units      = unit.references().asSeq
      val references = units.flatMap(x => x.references().asSeq)
      (units.size + references.size) shouldBe 3
    }
  }

  test("Test JSON Schema emission without documentation") {
    val api = """#%RAML 1.0
                |title: test json schema
                |
                |types:
                |  A:
                |    type: object
                |    description: this is a test of documentation description
                |    displayName: this is a test of display name
                |    properties:
                |      a1:
                |        type: string
                |        description: the first property
                |        examples:
                |          a: I am a string
                |          b: I am other string
                |          c: I am another string
                |      a2:
                |        type: integer
                |        description: the second property
                |        example: 1
                |      a3:
                |        type: number
                |        description: the third property
                |        example: 3.2
                |    example:
                |      a1: blahblahblah
                |      a2: 32
                |      a3: 256.3""".stripMargin
    for {
      _        <- AMF.init().asFuture
      unit     <- new RamlParser().parseStringAsync(api).asFuture
      resolved <- Future.successful(new Raml10Resolver().resolve(unit, ResolutionPipeline.EDITING_PIPELINE))
      report   <- AMF.validateResolved(resolved, RamlProfile, AMFStyle).asFuture
    } yield {
      val expectedSchema = """{
                             |  "$schema": "http://json-schema.org/draft-04/schema#",
                             |  "$ref": "#/definitions/A",
                             |  "definitions": {
                             |    "A": {
                             |      "type": "object",
                             |      "additionalProperties": true,
                             |      "required": [
                             |        "a1",
                             |        "a2",
                             |        "a3"
                             |      ],
                             |      "properties": {
                             |        "a1": {
                             |          "type": "string"
                             |        },
                             |        "a2": {
                             |          "type": "integer"
                             |        },
                             |        "a3": {
                             |          "type": "number"
                             |        }
                             |      }
                             |    }
                             |  }
                             |}
                             |""".stripMargin
      val options        = new ShapeRenderOptions().withoutDocumentation
      val schema = resolved
        .asInstanceOf[Document]
        .declares
        .asSeq
        .head
        .asInstanceOf[AnyShape]
        .buildJsonSchema(options)
      assert(report.conforms)
      assert(schema == expectedSchema)
    }
  }

  test("Test non existent resource types") {
    val file = "file://amf-client/shared/src/test/resources/validations/resource_types/non-existent-include.raml"
    for {
      _        <- AMF.init().asFuture
      unit     <- new RamlParser().parseFileAsync(file).asFuture
      resolved <- Future.successful(new Raml10Resolver().resolve(unit, ResolutionPipeline.EDITING_PIPELINE))
      report   <- AMF.validateResolved(resolved, RamlProfile, AMFStyle).asFuture
    } yield {
      assert(!report.conforms)
    }
  }

  test("Resource type merging of identical types referenced differently") {
    val file = "file://amf-client/shared/src/test/resources/validations/rt-type-merging/api.raml"
    for {
      _        <- AMF.init().asFuture
      unit     <- new RamlParser().parseFileAsync(file).asFuture
      resolved <- Future.successful(new Raml10Resolver().resolve(unit, ResolutionPipeline.EDITING_PIPELINE))
      report   <- AMF.validateResolved(resolved, RamlProfile, AMFStyle).asFuture
    } yield {
      assert(report.conforms)
    }
  }

  test("Test non existent traits") {
    val file = "file://amf-client/shared/src/test/resources/validations/traits/non-existent-include.raml"
    for {
      _        <- AMF.init().asFuture
      unit     <- new RamlParser().parseFileAsync(file).asFuture
      resolved <- Future.successful(new Raml10Resolver().resolve(unit, ResolutionPipeline.EDITING_PIPELINE))
      report   <- AMF.validateResolved(resolved, RamlProfile, AMFStyle).asFuture
    } yield {
      assert(!report.conforms)
    }
  }

  test("Test resolution error with resolve stage") {
    val api = """#%RAML 1.0
                |title: API
                |
                |types:
                |  SomeType:
                |    type: SomeType
                |""".stripMargin
    for {
      _        <- AMF.init().asFuture
      unit     <- new RamlParser().parseStringAsync(api).asFuture
      resolved <- Future(new Raml10Resolver().resolve(unit, ResolutionPipeline.EDITING_PIPELINE))
      report   <- AMF.validateResolved(resolved, Raml10Profile, AMFStyle).asFuture
    } yield {
      assert(!report.conforms)
    }
  }

  test("Test API with recursive type in array items") {
    val api =
      """
        |{
        |  "swagger": "2.0",
        |  "info": {
        |    "title": "api",
        |    "version": "1.0.0"
        |  },
        |  "paths": {},
        |  "definitions": {
        |    "APTransactionType": {
        |      "properties": {
        |        "GLTransaction": {
        |          "items": {
        |            "$ref": "#/definitions/GLTransactionType"
        |          },
        |          "type": "array"
        |        }
        |      },
        |      "type": "object"
        |    },
        |    "ARTransactionType": {
        |      "properties": {
        |        "GLTransaction": {
        |          "items": {
        |            "$ref": "#/definitions/GLTransactionType"
        |          },
        |          "type": "array"
        |        }
        |      },
        |      "type": "object"
        |    },
        |    "GLTransactionType": {
        |      "properties": {
        |        "APTransaction": {
        |          "$ref": "#/definitions/APTransactionType"
        |        },
        |        "ARTransaction": {
        |          "$ref": "#/definitions/ARTransactionType"
        |        }
        |      },
        |      "type": "object"
        |    },
        |    "root": {
        |      "properties": {
        |        "ARTransaction": {
        |          "items": {
        |            "$ref": "#/definitions/ARTransactionType"
        |          },
        |          "type": "array"
        |        }
        |      },
        |      "type": "object"
        |    }
        |  }
        |}
        |""".stripMargin
    val payload =
      """
        |{
        |  "ARTransaction": [
        |    {
        |      "GLTransaction": [
        |        {
        |          "APTransaction": {
        |            "GLTransaction": []
        |          },
        |          "ARTransaction": {
        |            "GLTransaction": []
        |          }
        |        }
        |      ]
        |    }
        |  ]
        |}
        |""".stripMargin
    for {
      _        <- AMF.init().asFuture
      parsed   <- new Oas20Parser().parseStringAsync(api).asFuture
      resolved <- Future(new Oas20Resolver().resolve(parsed, ResolutionPipeline.EDITING_PIPELINE))
      shape <- {
        Future.successful {
          val declarations = resolved.asInstanceOf[Document].declares.asSeq
          val shape = declarations.find {
            case s: Shape => s.name.value() == "root"
            case _        => false
          }
          shape.get.asInstanceOf[AnyShape]
        }
      }
      report <- {
        shape.validate(payload).asFuture
      }
    } yield {
      assert(report.conforms)
    }
  }

  test("Test emission of json schema with specified version") {
    val api = "file://amf-client/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml"
    for {
      _        <- AMF.init().asFuture
      unit     <- new Async20Parser().parseFileAsync(api).asFuture
      resolved <- Future(new Async20Resolver().resolve(unit, ResolutionPipeline.CACHE_PIPELINE))
    } yield {
      val golden  = """{
                        |  "$schema": "http://json-schema.org/draft-07/schema#",
                        |  "$ref": "#/definitions/conditional-subschemas",
                        |  "definitions": {
                        |    "conditional-subschemas": {
                        |      "type": "object",
                        |      "if": {
                        |        "properties": {
                        |          "country": {
                        |            "enum": [
                        |              "United States of America"
                        |            ]
                        |          }
                        |        },
                        |        "type": "object"
                        |      },
                        |      "then": {
                        |        "properties": {
                        |          "postal_code": {
                        |            "pattern": "[0-9]{5}(-[0-9]{4})?",
                        |            "type": "string"
                        |          }
                        |        },
                        |        "type": "object"
                        |      },
                        |      "else": {
                        |        "properties": {
                        |          "postal_code": {
                        |            "pattern": "[A-Z][0-9][A-Z] [0-9][A-Z][0-9]",
                        |            "type": "string"
                        |          }
                        |        },
                        |        "type": "object"
                        |      },
                        |      "examples": [
                        |        {
                        |          "country": "United States of America",
                        |          "postal_code": "dlkfjslfj"
                        |        },
                        |        {
                        |          "country": "United States of America",
                        |          "postal_code": "20500"
                        |        },
                        |        {
                        |          "country": "Canada",
                        |          "postal_code": "K1M 1M4"
                        |        },
                        |        {
                        |          "country": "Canada",
                        |          "postal_code": "K1M NOT"
                        |        }
                        |      ],
                        |      "additionalProperties": true,
                        |      "properties": {
                        |        "country": {
                        |          "enum": [
                        |            "United States of America",
                        |            "Canada"
                        |          ]
                        |        }
                        |      }
                        |    }
                        |  }
                        |}
                        |""".stripMargin
      val options = new ShapeRenderOptions().withSchemaVersion(JSONSchemaVersions.DRAFT_07)
      val generated =
        resolved
          .asInstanceOf[Document]
          .declares
          .asSeq
          .find(_._internal.id == "file://amf-client/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declarations/types/conditional-subschemas")
          .get
          .asInstanceOf[NodeShape]
          .buildJsonSchema(options)
      assert(generated == golden)
    }
  }

  test("Empty static validations collector") {
    val api = """#%RAML 1.0
                |title: API
                |types:
                |  SomeType:
                |    type: string
                |    required: true
                |""".stripMargin
    for {
      _     <- AMF.init().asFuture
      unit1 <- new RamlParser().parseStringAsync(api).asFuture
      unit2 <- new RamlParser().parseStringAsync(api).asFuture
    } yield {
      val validations1 = unit1._internal.parserRun.map(StaticErrorCollector.getRun).getOrElse(Nil)
      val validation2  = unit2._internal.parserRun.map(StaticErrorCollector.getRun).getOrElse(Nil)
      assert(validations1.nonEmpty && validation2.nonEmpty)
      StaticErrorCollector.clean()
      val postCleaning1 = unit1._internal.parserRun.map(StaticErrorCollector.getRun).getOrElse(Nil)
      val postCleaning2 = unit2._internal.parserRun.map(StaticErrorCollector.getRun).getOrElse(Nil)
      assert(postCleaning1.isEmpty && postCleaning2.isEmpty)
    }
  }

  test("Resolve KG Service API") {
    for {
      _        <- AMF.init().asFuture
      parsed   <- new Raml10Parser().parseFileAsync(knowledgeGraphServiceApi).asFuture
      resolved <- Future.successful(new Raml10Resolver().resolve(parsed, ResolutionPipeline.EDITING_PIPELINE))
    } yield {
      assert(resolved.references().asSeq.forall(_.id != null))
    }
  }

  test("Test custom validation to root document") {

    val instance          = "file://amf-client/shared/src/test/resources/custom/example.raml"
    val validationProfile = "file://amf-client/shared/src/test/resources/custom/profile.raml"

    for {
      _          <- AMF.init().asFuture
      unit       <- new RamlParser().parseFileAsync(instance).asFuture
      report     <- AMF.validate(unit, RamlProfile, AMFStyle).asFuture
      newProfile <- AMF.loadValidationProfile(validationProfile).asFuture
      custom     <- AMF.validate(unit, newProfile, AMFStyle).asFuture
    } yield {
      assert(report.conforms && custom.conforms)
    }
  }

  test("Use client defined resource loaders when resolving a recursive unit") {
    val input = """
                   |openapi: "3.0.0"
                   |info:
                   |  version: 1.0.0
                   |  title: api-2
                   |paths: {}
                   |components:
                   |  schemas:
                   |    Author:
                   |      $ref: https://randomurl/
    """.stripMargin

    val custom = StringResourceLoader(
      "https://randomurl/",
      """
        |{
        |  "$schema": "http://json-schema.org/draft-07/schema#",
        |  "title": "Person",
        |  "type": "object",
        |  "properties": {
        |    "recursive": {
        |      "$ref": "https://randomurl/"
        |    }
        |  }
        |}
        """.stripMargin
    )

    val env: Environment = Environment(InternalEnvironment().withLoaders(List(custom)))
    for {
      _      <- AMF.init().asFuture
      unit   <- new Oas30YamlParser(env).parseStringAsync(input).asFuture
      report <- AMF.validate(unit, Oas30Profile, OASStyle).asFuture
    } yield {
      report.conforms should be(true)
    }
  }

  test("Test domain element emitter with unknown vendor") {
    val eh = DefaultErrorHandler()
    WebApiDomainElementEmitter.emit(InternalArrayNode(), Vendor.PAYLOAD, eh)
    assert(eh.getErrors.head.message == "Unknown vendor provided")
  }

  test("Test domain element emitter with unhandled domain element") {
    val eh = DefaultErrorHandler()
    WebApiDomainElementEmitter.emit(InternalCorrelationId(), Vendor.RAML10, eh)
    assert(eh.getErrors.head.message == "Unhandled domain element for given vendor")
  }

  test("OAS 3.0 Response examples for a same type have different ids") {
    val file =
      "file://amf-client/shared/src/test/resources/validations/oas3/several-single-examples-for-same-type/api.json"
    for {
      _        <- AMF.init().asFuture
      unit     <- new Oas30Parser().parseFileAsync(file).asFuture
      resolved <- Future.successful(new Oas30Resolver().resolve(unit, ResolutionPipeline.EDITING_PIPELINE))
    } yield {
      val exampleIds =
        resolved.asInstanceOf[Document].declares.asSeq.head.asInstanceOf[AnyShape].examples.asSeq.map(x => x.id)
      exampleIds.toSet should have size 3
    }
  }

  test("Oas and JsonSchema refs don't have double-linking for refs") {
    val file = "file://amf-client/shared/src/test/resources/validations/oas2/double-linking.yaml"
    for {
      _    <- AMF.init().asFuture
      unit <- new Oas20YamlParser().parseFileAsync(file).asFuture
    } yield {
      val personProperties = unit.asInstanceOf[Document].declares.asSeq.head.asInstanceOf[NodeShape].properties.asSeq
      personProperties(1).range.linkTarget.asOption.get.asInstanceOf[Linkable].isLink shouldBe false
      personProperties(2).range.linkTarget.asOption.get.asInstanceOf[Linkable].isLink shouldBe false
    }
  }

  // todo: move to common (file system)
  def getAbsolutePath(path: String): String
}
