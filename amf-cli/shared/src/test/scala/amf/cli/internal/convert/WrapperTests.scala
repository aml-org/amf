package amf.cli.internal.convert

import _root_.org.scalatest.{Assertion, Matchers}
import amf.aml.client.platform.model.document.Vocabulary
import amf.aml.client.platform.model.domain.{ClassTerm, DatatypePropertyTerm, PropertyTerm}
import amf.apicontract.client.common.ProvidedMediaType
import amf.apicontract.client.platform.model.document.TraitFragment
import amf.apicontract.client.platform.model.domain.{Operation, Parameter}
import amf.apicontract.client.platform.model.domain.api.{Api, WebApi}
import amf.apicontract.client.platform.{
  AMFConfiguration,
  AsyncAPIConfiguration,
  OASConfiguration,
  RAMLConfiguration,
  WebAPIConfiguration
}
import amf.apicontract.client.scala.model.domain.CorrelationId
import amf.apicontract.client.scala.render.ApiDomainElementEmitter
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.core.client.common.remote.Content
import amf.core.client.common.validation.{Oas30Profile, Raml10Profile, ValidationMode}
import amf.core.client.platform.config.{JSONSchemaVersions, RenderOptions, ShapeRenderOptions}
import amf.core.client.platform.model.document.{BaseUnit, DeclaresModel, Document}
import amf.core.client.platform.model.domain.{ArrayNode, CustomDomainProperty, Linkable, ObjectNode, ScalarNode, Shape}
import amf.core.client.scala.model.domain.{
  ArrayNode => InternalArrayNode,
  ObjectNode => InternalObjectNode,
  ScalarNode => InternalScalarNode
}
import amf.core.client.platform.parse.AMFParser
import amf.core.internal.remote._
import amf.io.{FileAssertionTest, MultiJsonldAsyncFunSuite}
import org.yaml.builder.JsonOutputBuilder
import amf.core.client.scala.model.document.{Document => InternalDocument}
import amf.core.client.scala.resource.ResourceLoader
import amf.core.client.platform.resource.{ResourceNotFound, ResourceLoader => ClientResourceLoader}
import amf.core.client.scala.errorhandling.DefaultErrorHandler
import amf.core.client.scala.exception.UnsupportedVendorException
import amf.core.client.scala.model.domain.extensions.{DomainExtension => InternalDomainExtension}
import amf.shapes.client.platform.model.domain.{AnyShape, NodeShape, ScalarShape, SchemaShape}
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.client.scala.vocabulary.Namespace
import amf.core.client.scala.vocabulary.Namespace.Xsd
import amf.core.internal.convert.CoreClientConverters.ClientFuture
import amf.core.internal.convert.CoreClientConverters._
import amf.core.internal.resource.{ClientResourceLoaderAdapter, StringResourceLoader}
import amf.shapes.client.platform.render.JsonSchemaShapeRenderer
import org.mulesoft.common.test.Diff

import scala.concurrent.{ExecutionContext, Future}

trait WrapperTests extends MultiJsonldAsyncFunSuite with Matchers with NativeOps with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val banking       = "file://amf-cli/shared/src/test/resources/production/raml10/banking-api/api.raml"
  private val zencoder      = "file://amf-cli/shared/src/test/resources/api/zencoder.raml"
  private val oas3          = "file://amf-cli/shared/src/test/resources/api/oas3.json"
  private val async2        = "file://amf-cli/shared/src/test/resources/api/async2.yaml"
  private val zencoder08    = "file://amf-cli/shared/src/test/resources/api/zencoder08.raml"
  private val music         = "file://amf-cli/shared/src/test/resources/production/world-music-api/api.raml"
  private val demosDialect  = "file://amf-cli/shared/src/test/resources/api/dialects/eng-demos.raml"
  private val demos2Dialect = "file://amf-cli/shared/src/test/resources/api/dialects/eng-demos-2.raml"
  private val demosInstance = "file://amf-cli/shared/src/test/resources/api/examples/libraries/demo.raml"
  private val security      = "file://amf-cli/shared/src/test/resources/upanddown/unnamed-security-scheme.raml"
  private val amflight =
    "file://amf-cli/shared/src/test/resources/production/raml10/american-flight-api-2.0.1-raml.ignore/api.raml"
  private val defaultValue = "file://amf-cli/shared/src/test/resources/api/shape-default.raml"
  private val profile      = "file://amf-cli/shared/src/test/resources/api/validation/custom-profile.raml"
  //  private val banking       = "file://amf-cli/shared/src/test/resources/api/banking.raml"
  private val apiWithSpaces =
    "file://amf-cli/shared/src/test/resources/api/api-with-spaces/space in path api/api.raml"
  private val apiWithIncludesWithSpaces =
    "file://amf-cli/shared/src/test/resources/api/api-with-includes-with-spaces/api.raml"
  private val scalarAnnotations =
    "file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/scalar-nodes/input.raml"
  private val recursiveAdditionalProperties =
    "file://amf-cli/shared/src/test/resources/recursive/recursive-additional-properties.yaml"
  private val knowledgeGraphServiceApi =
    "file://amf-cli/shared/src/test/resources/production/knowledge-graph-service-api-1.0.13-raml/kg.raml"

  def config(): AMFConfiguration = WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20())
  def testVocabulary(file: String, numClasses: Int, numProperties: Int): Future[Assertion] = {

    for {
      unit <- config().documentClient().parseVocabulary(file).asFuture
    } yield {
      val declarations = unit.vocabulary.declares.asSeq

      val classes    = declarations.collect { case term: ClassTerm    => term }
      val properties = declarations.collect { case prop: PropertyTerm => prop }

      assert(classes.size == numClasses)
      assert(properties.size == numProperties)
    }
  }

  test("Parsing raml 1.0 test (detect)") {
    for {
      unit <- config().documentClient().parse(zencoder).asFuture
    } yield {
      assertBaseUnit(unit.baseUnit, zencoder)
    }
  }

  test("Parsing raml 0.8 test (detect)") {
    for {
      unit <- config().documentClient().parse(zencoder08).asFuture
    } yield {
      assertBaseUnit(unit.baseUnit, zencoder08)
    }
  }

  test("Parsing raml 1.0 test") {
    for {
      unit <- config().documentClient().parse(zencoder, Raml10.mediaType).asFuture
    } yield {
      assertBaseUnit(unit.baseUnit, zencoder)
    }
  }

  test("Parsing raml 0.8 test") {
    for {
      unit <- config().documentClient().parse(zencoder08, Raml08.mediaType).asFuture
    } yield {
      assertBaseUnit(unit.baseUnit, zencoder08)
    }
  }

  test("Parsing default value string") {
    for {
      unit <- RAMLConfiguration.RAML().documentClient().parse(defaultValue).asFuture
    } yield {
      val declares = unit.baseUnit.asInstanceOf[DeclaresModel].declares.asSeq
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
      unit <- AMFParser.parseContent(doc, ProvidedMediaType.Raml10, RAMLConfiguration.RAML()).asFuture
    } yield {
      val webApi = unit.baseUnit._internal.asInstanceOf[InternalDocument].encodes
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
      unit <- AMFParser.parseContent(doc, ProvidedMediaType.Oas20Yaml, OASConfiguration.OAS20()).asFuture
    } yield {
      val webApi = unit.baseUnit._internal.asInstanceOf[InternalDocument].encodes
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
      unit <- AMFParser.parseContent(doc, ProvidedMediaType.Oas20Yaml, OASConfiguration.OAS20()).asFuture
    } yield {
      val webApi = unit.baseUnit._internal.asInstanceOf[InternalDocument].encodes
      webApi.fields.get(WebApiModel.Name).toString shouldBe expected
    }
  }

  test("Render / parse test RAML 0.8") {
    val configuration = RAMLConfiguration.RAML08()
    val client        = configuration.documentClient()
    for {
      unit   <- client.parse(zencoder08).asFuture
      output <- Future.successful(client.render(unit.baseUnit, Raml08.mediaType))
      result <- AMFParser.parseContent(output, configuration).asFuture
    } yield {
      assertBaseUnit(result.baseUnit, "http://a.ml/amf/default_document")
    }
  }

  test("Render / parse test RAML 1.0") {
    val configuration = RAMLConfiguration.RAML10()
    val client        = configuration.documentClient()

    for {
      unit   <- client.parse(zencoder).asFuture
      output <- Future.successful(client.render(unit.baseUnit, Raml10.mediaType))
      result <- AMFParser.parseContent(output, configuration).asFuture
    } yield {
      assertBaseUnit(result.baseUnit, "http://a.ml/amf/default_document")
    }
  }

  test("Source vendor RAML 1.0") {
    for {
      unit <- RAMLConfiguration.RAML().documentClient().parse(zencoder).asFuture
    } yield {
      unit.baseUnit.sourceVendor.asOption should be(Some(Raml10))
    }
  }

  test("Render / parse test OAS 2.0") {
    val configuration = config()
    val client        = configuration.documentClient()
    for {
      unit   <- client.parse(zencoder).asFuture
      output <- Future.successful(client.render(unit.baseUnit, Oas20.mediaType))
      result <- AMFParser.parseContent(output, configuration).asFuture
    } yield {
      assertBaseUnit(result.baseUnit, "http://a.ml/amf/default_document")
    }
  }

  test("Render / parse test OAS 3.0") {
    val client = OASConfiguration.OAS30().documentClient()
    for {
      unit   <- client.parse(oas3).asFuture
      output <- Future.successful(client.render(unit.baseUnit, Oas30.mediaType))
    } yield {
      output should include("openIdConnectUrl")
    }
  }

  test("Render / parse test Async 2.0") {
    val client = AsyncAPIConfiguration.Async20().documentClient()
    for {
      unit   <- client.parse(async2).asFuture
      output <- Future.successful(client.render(unit.baseUnit, AsyncApi20.mediaType))
    } yield {
      output should include("Correlation ID Example")
    }
  }

  test("Render / parse test AMF") {
    val configuration = RAMLConfiguration.RAML()
    val client        = configuration.documentClient()
    for {
      unit   <- client.parse(zencoder).asFuture
      output <- Future.successful(client.render(unit.baseUnit, Amf.mediaType))
      result <- AMFParser.parseContent(output, configuration).asFuture
    } yield {
      assertBaseUnit(result.baseUnit, "http://a.ml/amf/default_document")
    }
  }

  test("Resolution test") {
    val client = RAMLConfiguration.RAML().documentClient()
    for {
      unit     <- client.parse(zencoder).asFuture
      resolved <- Future.successful(client.transform(unit.baseUnit))
      report   <- client.validate(resolved.baseUnit, Raml10Profile).asFuture
    } yield {
      assert(report.conforms)
    }
  }

  test("Raml to oas security scheme after resolution") {
    val client = WebAPIConfiguration.WebAPI().documentClient()
    for {
      unit     <- client.parse(security).asFuture
      resolved <- Future.successful(client.transform(unit.baseUnit).baseUnit)
      output   <- Future.successful(client.render(resolved, Oas20.mediaType))
    } yield {
      assert(!output.isEmpty)
    }
  }

  test("world-music-test") {
    val client = config().documentClient()
    for {
      parseResult    <- client.parse(music, Raml10.mediaType).asFuture
      validateResult <- client.validate(parseResult.baseUnit, Raml10Profile).asFuture
    } yield {
      val parseReport = AMFValidationReport.unknownProfile(parseResult._internal)
      val report      = parseReport.merge(validateResult._internal)
      assert(!parseResult.baseUnit.references().asSeq.map(_.location).contains(null))
      assert(report.conforms)
    }
  }

  test("Scalar Annotations") {
    val client = config().documentClient()
    for {
      unit <- client.parse(scalarAnnotations, Raml10.mediaType + "+yaml").asFuture
    } yield {
      val api         = unit.baseUnit.asInstanceOf[Document].encodes.asInstanceOf[Api[_]]
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

    val client = config().documentClient()
    val render = client.render(vocab, Vendor.AML.mediaType)
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
    val baseUrl = "http://test.com/"
    testParseStringWithBaseUrl(baseUrl)
  }

  test("Parsing text document with base url (with include, without trailing slash)") {
    val baseUrl = "file://amf-cli/shared/src/test/resources/includes"
    testParseStringWithBaseUrlAndInclude(baseUrl)
  }

  test("Parsing text document with base url (with include and trailing slash)") {
    val baseUrl = "file://amf-cli/shared/src/test/resources/includes/"
    testParseStringWithBaseUrlAndInclude(baseUrl)
  }

  test("Parsing text document with base url (with include and file name)") {
    val baseUrl = "file://amf-cli/shared/src/test/resources/includes/api.raml"
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

    case class TestResourceLoader() extends ClientResourceLoader {

      override def fetch(resource: String): ClientFuture[Content] =
        Future.successful(new Content(person, resource)).asClient

      override def accepts(resource: String): Boolean = resource == include
    }

    val config = RAMLConfiguration.RAML10().withResourceLoader(TestResourceLoader().asInstanceOf[ClientLoader])
    for {
      unit <- config.documentClient().parseContent(input, Raml10.mediaType + "+yaml").asFuture
    } yield {
      unit.baseUnit shouldBe a[Document]
      val declarations = unit.baseUnit.asInstanceOf[Document].declares.asSeq
      declarations should have size 1
    }
  }

  test("Network error report has position and location") {
    val uri = "file://amf-cli/shared/src/test/resources/compiler/network-error/api.raml"

    case class DummyHttpResourceLoader() extends ResourceLoader {
      override def fetch(resource: String): Future[Content] = {
        try {
          throw new Exception("Dummy error!")
        } catch {
          case e: Exception => throw NetworkError(e)
        }
      }

      /** Accepts specified resource. */
      override def accepts(resource: String): Boolean = resource.startsWith("http")
    }

    val fileLoader: ResourceLoader    = platform.loaders().filter(x => x.accepts("file://")).head
    val loaders: List[ResourceLoader] = List(DummyHttpResourceLoader(), fileLoader)
    val config = RAMLConfiguration
      .RAML()
      .withResourceLoaders(loaders.asClient)
    val client = config.documentClient()
    for {
      parseResult <- client.parse(uri).asFuture
    } yield {
      parseResult.conforms shouldBe false
      val networkError = parseResult.results.asSeq.head
      networkError.message should include("Network Error:")
      networkError.position should not be None
      networkError.location should not be None
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

      override def fetch(resource: String): Future[Content] =
        Future.successful(new Content(input, resource))

      override def accepts(resource: String): Boolean = true
    }

    val loaders: List[ResourceLoader] = List(BadIRIResourceLoader())
    val config = RAMLConfiguration
      .RAML()
      .withResourceLoaders(loaders.asClient)
    val client = config.documentClient()

    for {
      parseResult  <- client.parse(name, ProvidedMediaType.Raml10).asFuture
      parseResult2 <- client.parse(name2, ProvidedMediaType.Raml10).asFuture
    } yield {
      parseResult.baseUnit shouldBe a[Document]
      parseResult.baseUnit.id should be("file://api.raml")
      parseResult2.baseUnit.id should be("file://api2")
    }
  }

  test("Generate to doc builder") {
    val input = s"""
                       |#%RAML 1.0
                       |title: Environment test
                       |version: 32.0.7
        """.stripMargin

    val builder = JsonOutputBuilder()
    val client  = RAMLConfiguration.RAML10().documentClient()
    for {
      unit   <- client.parseContent(input, Raml10.mediaType + "+yaml").asFuture
      result <- Future.successful(client.renderGraphToBuilder(unit.baseUnit, builder))
    } yield {
      result.toString should include("\"http://a.ml/vocabularies/core#version\"")
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

      override def fetch(resource: String): Future[Content] = {
        val f =
          if (resource.endsWith("api.raml")) Future.successful(new Content(input, resource))
          else
            Future.failed(new ResourceNotFound(s"Cannot find resource $resource"))

        f
      }

      override def accepts(resource: String): Boolean = true
    }
    val loaders: List[ResourceLoader] = List(ForFailResourceLoader())
    val config                        = RAMLConfiguration.RAML().withResourceLoaders(loaders.asClient)

    for {
      parseResult <- config.documentClient().parse(name).asFuture
    } yield {
      parseResult.conforms should be(false)
      parseResult.results.asSeq
        .exists(_.message.equals("Cannot find resource not-exists.raml")) should be(true)
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

    case class TestResourceLoader() extends ResourceLoader {
      override def fetch(resource: String): Future[Content] =
        Future.successful(new Content(person, resource))

      override def accepts(resource: String): Boolean = resource == include
    }

    case class FailingResourceLoader(msg: String) extends ResourceLoader {
      override def fetch(resource: String): Future[Content] =
        Future.failed[Content](new Exception(msg))

      override def accepts(resource: String): Boolean = true
    }

    val loaders: List[ResourceLoader] = List(TestResourceLoader(),
                                             FailingResourceLoader("Unreachable network"),
                                             FailingResourceLoader("Invalid protocol"))
    val config = RAMLConfiguration.RAML10().withResourceLoaders(loaders.asClient)

    for {
      unit <- config.documentClient().parseContent(input, Raml10.mediaType + "+yaml").asFuture
    } yield {
      unit.baseUnit shouldBe a[Document]
      val declarations = unit.baseUnit.asInstanceOf[Document].declares.asSeq
      declarations should have size 1
    }
  }

  test("Missing converter error") {
    val client = RAMLConfiguration.RAML().documentClient()
    for {
      unit     <- client.parse(amflight).asFuture
      resolved <- Future.successful(client.transform(unit.baseUnit))
    } yield {
      val webapi = resolved.baseUnit.asInstanceOf[Document].encodes.asInstanceOf[Api[_]]
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
    val client = RAMLConfiguration.RAML().documentClient()
    for {
      unit      <- client.parseContent(api, Raml10.mediaType + "+yaml").asFuture
      removed   <- removeFields(unit.baseUnit)
      generated <- Future.successful(client.render(removed, Raml10.mediaType))
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
    val client    = OASConfiguration.OAS20().documentClient()
    val doc       = buildBasicApi()
    val generated = client.render(doc, Oas20.mediaType + "+yaml")
    val deltas    = Diff.ignoreAllSpace.diff(expected, generated)
    if (deltas.nonEmpty) fail("Expected and golden are different: " + Diff.makeString(deltas))
    else succeed
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
    val client    = OASConfiguration.OAS20().documentClient()
    val doc       = buildApiWithTypeTarget()
    val generated = client.render(doc, Oas20.mediaType + "+yaml")
    val deltas    = Diff.ignoreAllSpace.diff(expected, generated)
    if (deltas.nonEmpty) fail("Expected and golden are different: " + Diff.makeString(deltas))
    else succeed
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
      doc <- RAMLConfiguration.RAML10().documentClient().parseContent(api, Raml10.mediaType + "+yaml").asFuture
    } yield {

      val seq = doc.baseUnit.asInstanceOf[Document].encodes.asInstanceOf[Api[_]].endPoints.asSeq.head.operations.asSeq
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
    val api: Api[_] = new WebApi().withName("test swagger entry")

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
      .asInstanceOf[Api[_]]
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
      parseResult <- RAMLConfiguration.RAML10().documentClient().parseContent(api, Raml10.mediaType + "+yaml").asFuture
    } yield {
      val webApi = parseResult.baseUnit.asInstanceOf[Document].encodes.asInstanceOf[Api[_]]
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
      parseResult <- RAMLConfiguration.RAML10().documentClient().parseContent(api, ProvidedMediaType.Raml10).asFuture
    } yield {
      val nodeShape = parseResult.baseUnit.asInstanceOf[Document].declares.asSeq.head.asInstanceOf[NodeShape]
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
    val client = RAMLConfiguration.RAML10().documentClient()
    for {
      parseResult     <- client.parseContent(api, ProvidedMediaType.Raml10).asFuture
      transformResult <- Future { client.transform(parseResult.baseUnit) }
    } yield {
      val pathParamters: List[Parameter] = transformResult.baseUnit
        .asInstanceOf[Document]
        .encodes
        .asInstanceOf[Api[_]]
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
    val client = RAMLConfiguration.RAML().documentClient()
    for {
      unit     <- client.parseContent(api, ProvidedMediaType.Raml10).asFuture
      resolved <- Future { client.transform(unit.baseUnit) }
    } yield {
      val baseParameters: Seq[Parameter] =
        resolved.baseUnit.asInstanceOf[Document].encodes.asInstanceOf[Api[_]].servers.asSeq.head.variables.asSeq

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
    val client = RAMLConfiguration.RAML08().documentClient()
    for {
      parseResult <- client.parseContent(api, Raml08.mediaType + "+yaml").asFuture
      _           <- Future { client.transform(parseResult.baseUnit) }
    } yield {
      val shape: Shape = parseResult.baseUnit
        .asInstanceOf[Document]
        .encodes
        .asInstanceOf[Api[_]]
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
    val webApi = unit.asInstanceOf[Document].encodes.asInstanceOf[Api[_]]
    webApi.description.remove()
    val operation: Operation = webApi.endPoints.asSeq.head.operations.asSeq.head
    operation.graph().remove("http://a.ml/vocabularies/apiContract#returns")

    webApi.graph().remove("http://a.ml/vocabularies/core#license")
    unit
  }

  private def resourceLoaderFor(url: String, content: String): ResourceLoader = {
    new ResourceLoader {
      override def fetch(resource: String): Future[Content] =
        Future.successful(new Content(content, url, None))
      override def accepts(resource: String): Boolean = resource == url
    }
  }

  private def testParseStringWithBaseUrl(baseUrl: String): Future[Assertion] = {
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
    val loaders: List[ResourceLoader] = List(resourceLoaderFor(baseUrl, spec))
    val configuration                 = config().withResourceLoaders(loaders.asClient)

    for {
      result <- configuration.documentClient().parse(baseUrl, Raml10.mediaType).asFuture
    } yield {
      val unit = result.baseUnit
      assert(unit.location.startsWith(baseUrl))
      val encodes = unit.asInstanceOf[Document].encodes
      assert(encodes.id.startsWith(baseUrl))
      assert(encodes.asInstanceOf[Api[_]].name.is("Some title"))
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
    val client =
      config().withResourceLoader(ClientResourceLoaderAdapter(resourceLoaderFor(baseUrl, spec))).documentClient()
    for {
      unit <- client.parse(baseUrl).asFuture
    } yield {
      val res = client.transform(unit.baseUnit).baseUnit
      val gen = client.render(res, ProvidedMediaType.Raml10)
      gen should not include ("!include")
      gen should include("type: string")
    }
  }

  private def assertBaseUnit(baseUnit: BaseUnit, expectedLocation: String): Assertion = {
    assert(baseUnit.location == expectedLocation)
    val api       = baseUnit.asInstanceOf[Document].encodes.asInstanceOf[Api[_]]
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
    val factory = RAMLConfiguration.RAML().payloadValidatorFactory()
    for {
      shape <- Future {
        new ScalarShape()
          .withDataType("http://www.w3.org/2001/XMLSchema#string")
          .withName("test")
          .withId("api.raml/#/webapi/schema1")
      }
      report <- factory
        .createFor(shape, "application/yaml", ValidationMode.StrictValidationMode)
        .validate(payload)
        .asFuture
    } yield {
      assert(report.conforms)
    }
  }

  test("Generate unit with source maps") {
    val options = new RenderOptions().withSourceMaps
    val client  = RAMLConfiguration.RAML10().withRenderOptions(options).documentClient()
    for {
      unit   <- client.parse(banking).asFuture // could be use a smaller api for this test?
      jsonld <- Future.successful(client.render(unit.baseUnit))
    } yield {
      jsonld should include("[(1,0)-(252,0)]")
    }
  }

  test("Generate unit without source maps") {
    val options = new RenderOptions().withoutSourceMaps
    val client  = RAMLConfiguration.RAML10().withRenderOptions(options).documentClient()
    for {
      unit   <- client.parse(banking).asFuture // could be use a smaller api for this test?
      jsonld <- Future.successful(client.render(unit.baseUnit))
    } yield {
      jsonld should not include "[(3,0)-(252,0)]"
    }
  }

  test("Generate unit with compact uris") {
    val options = new RenderOptions().withCompactUris.withSourceMaps
    val client  = RAMLConfiguration.RAML10().withRenderOptions(options).documentClient()
    for {
      unit   <- client.parse(banking).asFuture
      jsonld <- Future.successful(client.render(unit.baseUnit))
    } yield {
      jsonld should include("@context")
    }
  }

  test("banking-api-test") {
    val client = RAMLConfiguration.RAML().documentClient()
    for {
      unit <- client.parse(banking).asFuture
    } yield {
      val references = unit.baseUnit.references().asSeq
      assert(!references.map(_.location).contains(null))
      val traits = references.find(ref => ref.location.endsWith("traits.raml")).head.references().asSeq
      val first  = traits.head
      assert(first.location != null)
      assert(first.asInstanceOf[TraitFragment].encodes != null)
      assert(!traits.map(_.location).contains(null))
    }
  }

  test("Parsing external xml shape") {
    val client = RAMLConfiguration.RAML().documentClient()
    for {
      unit <- client.parse("file://amf-cli/shared/src/test/resources/production/raml10/xsdschema/api.raml").asFuture
    } yield {
      val location: Option[String] =
        unit.baseUnit.asInstanceOf[Document].declares.asSeq.head.asInstanceOf[SchemaShape].location.asOption
      location.isDefined should be(true)
      location.get should be("file://amf-cli/shared/src/test/resources/production/raml10/xsdschema/schema.xsd")

    }
  }

  test("Parsing external xml example") {
    val client = RAMLConfiguration.RAML().documentClient()
    for {
      unit <- client.parse("file://amf-cli/shared/src/test/resources/production/raml10/xsdexample/api.raml").asFuture
    } yield {
      val location: Option[String] = unit.baseUnit
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
      location.get should be("file://amf-cli/shared/src/test/resources/production/raml10/xsdexample/example.xsd")
    }
  }

  test("Parsing external xml with inner ref annotation") {
    val client = RAMLConfiguration.RAML().documentClient()
    for {
      unit <- client
        .parse("file://amf-cli/shared/src/test/resources/production/raml10/xsdschema-withfragmentref/api.raml")
        .asFuture
    } yield {
      val shape = unit.baseUnit
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
    val client = RAMLConfiguration.RAML().documentClient()
    for {
      unit <- client
        .parse("file://amf-cli/shared/src/test/resources/production/raml10/jsonschema-apiwithfragmentref/api.raml")
        .asFuture
    } yield {
      val shape = unit.baseUnit
        .asInstanceOf[Document]
        .declares
        .asSeq
        .head
        .asInstanceOf[AnyShape]
      shape.annotations().fragmentName().asOption.get should be("/definitions/address")
    }
  }

  test("Test validate with typed enum amf pair method") {
    val client = RAMLConfiguration.RAML10().documentClient()
    for {
      unit <- client.parse(scalarAnnotations).asFuture
      v    <- client.validate(unit.baseUnit, Raml10Profile).asFuture
    } yield {
      assert(v.conforms && unit.conforms)
    }
  }

  // in fact the change were do it at parsing time (abstract declaration parser). I change the hashmap for a list map of the properties to preserve order, so this test could be parse and dump but i wanna be sure that nobody will change the resolved params order in any other place.
  test("Test query parameters order") {
    val client = RAMLConfiguration.RAML08().documentClient()
    for {
      unit <- client.parse("file://amf-cli/shared/src/test/resources/clients/params-order.raml").asFuture
      v    <- Future.successful(client.transform(unit.baseUnit))
    } yield {
      val seq = v.baseUnit
        .asInstanceOf[Document]
        .encodes
        .asInstanceOf[Api[_]]
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
    val url    = "http://location.com/myfile"
    val client = RAMLConfiguration.RAML10().documentClient()
    for {
      doc <- Future {
        val ns            = (Namespace.Xsd + "string").iri()
        val doc: Document = new Document()
        doc._internal.withId("http://location.com/myfile")
        val shape = new ScalarShape().withName("scalarDeclared").withDataType(ns)
        doc.withDeclaredElement(shape)
        val wa = new WebApi().withName("test")
        doc.withEncodes(wa)
        val annotationType =
          new CustomDomainProperty()
            .withName("forDescribedBy")
            .withId("http://location.com/myfile#/declarations/annotations/forDescribedBy")
            .withSchema(new ScalarShape().withName("scalarName").withDataType(ns))
        doc.withDeclaredElement(annotationType)
        val annotation = InternalDomainExtension()
          .withExtension(new ScalarNode("extension", ns)._internal)
          .withDefinedBy(annotationType._internal)
          .withName(annotationType.name.value())
        shape.withCustomDomainProperties(Seq(annotation).asClient)
        doc
      }
      s       <- Future.successful(client.render(doc, ProvidedMediaType.Raml10))
      loaders <- Future.successful(List(StringResourceLoader(url, s)))
      withLoader <- Future.successful(
        client
          .getConfiguration()
          .withResourceLoaders(loaders.asInstanceOf[List[ResourceLoader]].asClient)
          .documentClient())
      parsed <- withLoader.parse(url).asFuture
    } yield {
      val buildedProp: CustomDomainProperty =
        doc.declares.asSeq.collectFirst({ case s: Shape => s.customDomainProperties.asSeq.head.definedBy }).get

      val parsedProp: CustomDomainProperty = parsed.baseUnit
        .asInstanceOf[Document]
        .declares
        .asSeq
        .collectFirst({ case s: Shape => s.customDomainProperties.asSeq.head.definedBy })
        .get
      parsedProp.id should be(buildedProp.id)
    }
  }

  test("Test search tracked example") {
    val client = RAMLConfiguration.RAML10().documentClient()
    for {
      a <- client
        .parse("file://amf-cli/shared/src/test/resources/resolution/payloads-examples-resolution.raml")
        .asFuture
    } yield {
      val r = client.transform(a.baseUnit)
      val operations =
        r.baseUnit.asInstanceOf[Document].encodes.asInstanceOf[Api[_]].endPoints.asSeq.head.operations.asSeq
      val getOp = operations.find(_.method.value().equals("get")).get
      val option = getOp.request.payloads.asSeq.head.schema
        .asInstanceOf[AnyShape]
        .trackedExample(
          "file://amf-cli/shared/src/test/resources/resolution/payloads-examples-resolution.raml#/web-api/end-points/%2Fendpoint1/get/request/application%2Fjson")
        .asOption
      option.isDefined should be(true)
      option.get.annotations().isTracked should be(true)

      val getPost = operations.find(_.method.value().equals("post")).get
      val shape   = getPost.request.payloads.asSeq.head.schema.asInstanceOf[AnyShape]
      val option2 = shape
        .trackedExample(
          "file://amf-cli/shared/src/test/resources/resolution/payloads-examples-resolution.raml#/web-api/end-points/%2Fendpoint1/get/request/application%2Fjson")
        .asOption
      option2.isDefined should be(true)
      option2.get.annotations().isTracked should be(true)

      shape.examples.asSeq
        .find(_.id.equals(
          "file://amf-cli/shared/src/test/resources/resolution/payloads-examples-resolution.raml#/declarations/types/A/example/declared"))
        .head
        .annotations()
        .isTracked should be(false)
    }
  }

  test("Test accessor to double parsed field") {
    val client = RAMLConfiguration.RAML10().documentClient()
    for {
      unit <- client.parse("file://amf-cli/shared/src/test/resources/clients/double-field.raml").asFuture
    } yield {
      val shape = unit.baseUnit.asInstanceOf[Document].declares.asSeq.head.asInstanceOf[ScalarShape]
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

    import amf.apicontract.internal.convert.ApiClientConverters._

    case class TestResourceLoader() extends ResourceLoader {
      override def fetch(resource: String): Future[Content] =
        Future.successful(new Content(lib, resource, Some("text/plain")))

      override def accepts(resource: String): Boolean = true
    }

    val loaders: List[ResourceLoader] = List(TestResourceLoader())

    val client = RAMLConfiguration.RAML10().withResourceLoaders(loaders.asClient).documentClient()

    for {
      unit <- client.parseContent(input, ProvidedMediaType.Raml10).asFuture
      v    <- client.validate(unit.baseUnit, Raml10Profile).asFuture
    } yield {
      v.conforms should be(true)
      val declarations = unit.baseUnit.asInstanceOf[Document].declares.asSeq
      declarations should have size 1
    }
  }

  test("Test yaml swagger 2.0 api with json parser") {

    recoverToSucceededIf[UnsupportedVendorException] {
      val client = OASConfiguration.OAS20().documentClient()
      client
        .parse("file://amf-cli/shared/src/test/resources/clients/oas20-yaml.yaml", ProvidedMediaType.Oas20Json)
        .asFuture
        .map { _ =>
          succeed
        }
    }

  }

  test("Test path resolution OAS for 'file:///' prefix") {
    val file    = platform.fs.syncFile("amf-cli/shared/src/test/resources/clients/toupdir-include/spec/swagger.json")
    val absPath = getAbsolutePath(file.path)
    val client  = OASConfiguration.OAS20().documentClient()
    for {
      unit   <- client.parse(absPath).asFuture
      report <- client.validate(unit.baseUnit, Raml10Profile).asFuture
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

    val clientUnit = BaseUnitMatcher.asClient(document)
    val clientDoc  = clientUnit.asInstanceOf[Document]
    clientDoc.encodes.isInstanceOf[ScalarNode] shouldBe true
    val clientArray = clientDoc.declares.asSeq.head.asInstanceOf[ArrayNode]
    clientArray.isInstanceOf[ArrayNode] shouldBe true
    clientArray
      .asInstanceOf[ArrayNode]
      .members
      .asSeq
      .head
      .isInstanceOf[ScalarNode] shouldBe true
    clientArray
      .asInstanceOf[ArrayNode]
      .members
      .asSeq(1)
      .isInstanceOf[ObjectNode] shouldBe true
  }

  test("Test Json relative ref with absolute path as input") {
    val file =
      platform.fs.syncFile("amf-cli/shared/src/test/resources/production/json-schema-relative-ref/api.raml")
    val absPath = getAbsolutePath(file.path)
    val client  = RAMLConfiguration.RAML10().documentClient()
    for {
      unit   <- client.parse(absPath).asFuture
      report <- client.validate(unit.baseUnit, Raml10Profile).asFuture
    } yield {
      assert(report.conforms && unit.conforms)
    }
  }

  test("Test quoted default value") {
    val file = "file://amf-cli/shared/src/test/resources/validations/default-with-quotes.raml"
    for {
      unit <- RAMLConfiguration.RAML10().documentClient().parse(file).asFuture.map(_.baseUnit)
    } yield {
      assert(
        unit.asInstanceOf[Document].declares.asSeq.head.asInstanceOf[Shape].defaultValueStr.value() == "A default")
    }
  }

  test("Test emission of json schema of a shape with file type") {
    val api    = """#%RAML 1.0
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
    val client = RAMLConfiguration.RAML10().documentClient()
    for {
      unit     <- client.parseContent(api).asFuture
      resolved <- Future(client.transformEditing(unit.baseUnit, ProvidedMediaType.Raml10))
      report   <- client.validate(unit.baseUnit, Raml10Profile).asFuture
      json <- Future {
        val shape = resolved.baseUnit.asInstanceOf[DeclaresModel].declares.asSeq.head.asInstanceOf[NodeShape]
        JsonSchemaShapeRenderer.buildJsonSchema(shape,
                                                client
                                                  .getConfiguration())
      }
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
    val api    = "file://amf-cli/shared/src/test/resources/validations/recursive-types.raml"
    val client = RAMLConfiguration.RAML10().documentClient()
    for {
      unit     <- client.parse(api).asFuture
      resolved <- Future(client.transformEditing(unit.baseUnit, ProvidedMediaType.Raml10))
    } yield {
      val golden = """{
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
      val shape  = resolved.baseUnit.asInstanceOf[Document].declares.asSeq(2).asInstanceOf[NodeShape]
      val generated = JsonSchemaShapeRenderer.buildJsonSchema(
        shape,
        client
          .getConfiguration()
          .withRenderOptions(
            new RenderOptions().withShapeRenderOptions(new ShapeRenderOptions().withoutCompactedEmission)))
      assert(generated == golden)
    }
  }

  // This test is here because of I need to resolve with default and then validate
  test("Test json schema emittion of recursive union shape") {
    val file   = "file://amf-cli/shared/src/test/resources/validations/recursion-union.raml"
    val client = RAMLConfiguration.RAML08().documentClient()
    for {
      unit     <- client.parse(file).asFuture
      resolved <- Future.successful(client.transform(unit.baseUnit))
      report   <- client.validate(resolved.baseUnit, Raml10Profile).asFuture
    } yield {
      assert(report.conforms)
    }
  }

  test("Test external fragment that includes a reference") {
    val file = "file://amf-cli/shared/src/test/resources/resolution/ex-frag-with-refs/api.raml"
    for {
      unit <- RAMLConfiguration.RAML10().documentClient().parse(file).asFuture.map(_.baseUnit)
    } yield {
      // Check that the external fragment has references
      assert(unit.references().asSeq.head.references().asSeq.nonEmpty)
    }
  }

  test("Test uri references to external reference from external reference are not encoded") {
    for {
      unit <- RAMLConfiguration.RAML08().documentClient().parse(apiWithSpaces).asFuture.map(_.baseUnit)
    } yield {
      val units      = unit.references().asSeq
      val references = units.flatMap(x => x.references().asSeq)
      (units.size + references.size) shouldBe 3
    }
  }

  test("Test JSON Schema emission without documentation") {
    val api =
      """#%RAML 1.0
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
    val client = RAMLConfiguration.RAML10().documentClient()
    for {
      unit     <- client.parseContent(api).asFuture
      resolved <- Future.successful(client.transformEditing(unit.baseUnit, ProvidedMediaType.Raml10))
      report   <- client.validate(resolved.baseUnit, Raml10Profile).asFuture
    } yield {
      val expectedSchema =
        """{
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
      val options = new ShapeRenderOptions().withoutDocumentation
      val shape = resolved.baseUnit
        .asInstanceOf[Document]
        .declares
        .asSeq
        .head
        .asInstanceOf[AnyShape]
      val schema = JsonSchemaShapeRenderer.buildJsonSchema(
        shape,
        client
          .getConfiguration()
          .withRenderOptions(new RenderOptions().withShapeRenderOptions(options)))
      assert(report.conforms)
      assert(schema == expectedSchema)
    }
  }

  test("Test non existent resource types") {
    val file   = "file://amf-cli/shared/src/test/resources/validations/resource_types/non-existent-include.raml"
    val client = RAMLConfiguration.RAML10().documentClient()
    for {
      unit <- client.parse(file).asFuture
    } yield {
      assert(!unit.conforms)
    }
  }

  test("Resource type merging of identical types referenced differently") {
    val file   = "file://amf-cli/shared/src/test/resources/validations/rt-type-merging/api.raml"
    val client = RAMLConfiguration.RAML10().documentClient()
    for {
      unit     <- client.parse(file).asFuture
      resolved <- Future.successful(client.transformEditing(unit.baseUnit, ProvidedMediaType.Raml10))
      report   <- client.validate(resolved.baseUnit, Raml10Profile).asFuture
    } yield {
      assert(report.conforms && unit.conforms && resolved.conforms)
    }
  }

  test("Test non existent traits") {
    val file   = "file://amf-cli/shared/src/test/resources/validations/traits/non-existent-include.raml"
    val client = RAMLConfiguration.RAML10().documentClient()
    for {
      unit <- client.parse(file).asFuture
    } yield {
      assert(!unit.conforms)
    }
  }

  test("Test resolution error with resolve stage") {
    val api    = """#%RAML 1.0
                |title: API
                |
                |types:
                |  SomeType:
                |    type: SomeType
                |""".stripMargin
    val client = RAMLConfiguration.RAML10().documentClient()
    for {
      unit     <- client.parseContent(api).asFuture
      resolved <- Future.successful(client.transformEditing(unit.baseUnit, ProvidedMediaType.Raml10))
    } yield {
      assert(!resolved.conforms)
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
    val client = OASConfiguration.OAS20().documentClient()
    for {
      parsed   <- client.parseContent(api, ProvidedMediaType.Oas20Json).asFuture
      resolved <- Future(client.transformEditing(parsed.baseUnit, ProvidedMediaType.Oas20))
      shape <- {
        Future.successful {
          val declarations = resolved.baseUnit.asInstanceOf[Document].declares.asSeq
          val shape = declarations.find {
            case s: Shape => s.name.value() == "root"
            case _        => false
          }
          shape.get.asInstanceOf[AnyShape]
        }
      }
      report <- {
        val validator = client
          .getConfiguration()
          .payloadValidatorFactory()
          .createFor(shape, "application/json", ValidationMode.StrictValidationMode)
        validator.validate(payload).asFuture
      }
    } yield {
      assert(report.conforms)
    }
  }

  test("Test emission of json schema with specified version") {
    val api    = "file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml"
    val client = AsyncAPIConfiguration.Async20().documentClient()
    for {
      unit     <- client.parse(api).asFuture
      resolved <- Future(client.transformCache(unit.baseUnit, ProvidedMediaType.Async20))
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
      val options = new ShapeRenderOptions().withoutCompactedEmission.withSchemaVersion(JSONSchemaVersions.DRAFT_07)
      val shape =
        resolved.baseUnit
          .asInstanceOf[Document]
          .declares
          .asSeq
          .find(_._internal.id == "file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declarations/types/conditional-subschemas")
          .get
          .asInstanceOf[NodeShape]
      val generated = JsonSchemaShapeRenderer.buildJsonSchema(
        shape,
        client
          .getConfiguration()
          .withRenderOptions(new RenderOptions().withShapeRenderOptions(options)))
      assert(generated == golden)
    }
  }

  test("Resolve KG Service API") {
    val client = RAMLConfiguration.RAML10().documentClient()
    for {
      parsed   <- client.parse(knowledgeGraphServiceApi).asFuture
      resolved <- Future.successful(client.transformEditing(parsed.baseUnit, ProvidedMediaType.Raml10))
    } yield {
      assert(resolved.baseUnit.references().asSeq.forall(_.id != null))
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
    val loaders: List[ResourceLoader] = List(custom)
    val client                        = OASConfiguration.OAS30().withResourceLoaders(loaders.asClient).documentClient()
    for {
      unit   <- client.parseContent(input, ProvidedMediaType.Oas30Yaml).asFuture
      report <- client.validate(unit.baseUnit, Oas30Profile).asFuture
    } yield {
      unit.conforms should be(true)
      report.conforms should be(true)
    }
  }

  test("Test domain element emitter with unknown vendor") {
    val eh = DefaultErrorHandler()
    ApiDomainElementEmitter.emit(InternalArrayNode(), Vendor.PAYLOAD, eh)
    assert(eh.getResults.head.message == "Unknown vendor provided")
  }

  test("Test domain element emitter with unhandled domain element") {
    val eh = DefaultErrorHandler()
    ApiDomainElementEmitter.emit(CorrelationId(), Vendor.RAML10, eh)
    assert(eh.getResults.head.message == "Unhandled domain element for given vendor")
  }

  test("OAS 3.0 Response examples for a same type have different ids") {
    val file =
      "file://amf-cli/shared/src/test/resources/validations/oas3/several-single-examples-for-same-type/api.json"
    val client = OASConfiguration.OAS30().documentClient()
    for {
      unit     <- client.parse(file).asFuture
      resolved <- Future.successful(client.transformEditing(unit.baseUnit, ProvidedMediaType.Oas30))
    } yield {
      val exampleIds =
        resolved.baseUnit
          .asInstanceOf[Document]
          .declares
          .asSeq
          .head
          .asInstanceOf[AnyShape]
          .examples
          .asSeq
          .map(x => x.id)
      exampleIds.toSet should have size 3
    }
  }

  test("Oas and JsonSchema refs don't have double-linking for refs") {
    val file = "file://amf-cli/shared/src/test/resources/validations/oas2/double-linking.yaml"
    for {
      unit <- OASConfiguration.OAS20().documentClient().parse(file).asFuture.map(_.baseUnit)
    } yield {
      val personProperties = unit.asInstanceOf[Document].declares.asSeq.head.asInstanceOf[NodeShape].properties.asSeq
      personProperties(1).range.linkTarget.asOption.get.asInstanceOf[Linkable].isLink shouldBe false
      personProperties(2).range.linkTarget.asOption.get.asInstanceOf[Linkable].isLink shouldBe false
    }
  }

  test("Avoid duplicate errors from invalid json in parsing and extends resolution") {
    val api    = "file://amf-cli/shared/src/test/resources/validations/raml/invalid-json-example-included/api.raml"
    val client = RAMLConfiguration.RAML10().documentClient()
    for {
      parsed   <- client.parse(api).asFuture
      resolved <- Future.successful(client.transformEditing(parsed.baseUnit, ProvidedMediaType.Raml10))
    } yield {
      assert(!parsed.conforms)
      assert(parsed.results.asSeq.size == 1)
      assert(resolved.conforms)
    }
  }

//
//  // todo: move to common (file system)
  def getAbsolutePath(path: String): String
}
