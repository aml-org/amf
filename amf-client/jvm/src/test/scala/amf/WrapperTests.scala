package amf

import amf.core.unsafe.PlatformSecrets
import amf.model.document.{Document, TraitFragment}
import amf.model.domain.{ScalarShape, WebApi}
import amf.plugins.document.vocabularies.registries.PlatformDialectRegistry
import org.scalatest.AsyncFunSuite

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

class WrapperTests extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Parsing test") {
    AMF.init().get()
    val parser   = new Raml10Parser()
    val baseUnit = parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").get()
    assert(baseUnit.location == "file://amf-client/shared/src/test/resources/api/zencoder.raml")
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

  test("Parsing refs test") {
    AMF.init().get()
    val parser   = new Raml10Parser()
    val baseUnit = parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/banking.raml").get()
    assert(baseUnit.references().size() == 2)
    assert(Option(baseUnit.references().get(0).location).isDefined)
  }

  test("Generation test") {
    AMF.init().get()
    val parser   = new Raml10Parser()
    val baseUnit = parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").get()
    assert(new Raml10Generator().generateString(baseUnit) != "") // TODO: test this properly
    assert(new Oas20Generator().generateString(baseUnit) != "")
    assert(new AmfGraphGenerator().generateString(baseUnit) != "")
  }

  test("Validation test") {
    AMF.init().get()
    val parser   = new Raml10Parser()
    val baseUnit = parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").get()
    val report   = AMF.validate(baseUnit, "RAML").get()
    assert(report.conforms)
    AMF.loadValidationProfile("file://amf-client/shared/src/test/resources/api/validation/custom-profile.raml").get()
    val custom = AMF.validate(baseUnit, "Banking").get()
    assert(!custom.conforms)
  }

  test("Resolution test") {
    AMF.init().get()
    val parser           = new Raml10Parser()
    val baseUnit         = parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").get()
    val resolvedBaseUnit = AMF.resolveRaml10(baseUnit) // TODO: test this properly
    val report           = AMF.validate(resolvedBaseUnit, "RAML").get()
    assert(report.conforms)
  }

  test("Vocabularies test") {
    AMF.init().get()

    AMF.registerDialect("file://amf-client/shared/src/test/resources/api/dialects/eng-demos.raml").get()

    val parser = new Raml10Parser()
    val baseUnit =
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/examples/libraries/demo.raml").get()

    PlatformDialectRegistry
    val report = AMF.validate(baseUnit, "Eng Demos 0.1").get()
    assert(report.conforms)

    AMF.registerNamespace("eng-demos", "http://mulesoft.com/vocabularies/eng-demos#")
    val elem     = baseUnit.asInstanceOf[Document].encodes
    val speakers = elem.getObjectByPropertyId("eng-demos:speakers")
    assert(speakers.size() > 0)
  }

  test("Raml to oas secutiry scheme pos resolution") {
    AMF.init().get()
    val parser = new Raml10Parser()

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
    val model = parser.parseFileAsync("file://amf-client/shared/src/test/resources/production/world-music-api/api.raml").get()
    assert(!model.references().asScala.map(_.location).contains(null))
  }

  test("banking-api-test") {
    amf.plugins.features.AMFValidation.register()
    amf.plugins.document.WebApi.register()
    amf.Core.init().get()
    val parser = amf.Core.parser("RAML 1.0", "application/yaml")
    val model = parser.parseFileAsync("file://amf-client/shared/src/test/resources/production/banking-api/api.raml").get()
    assert(!model.references().asScala.map(_.location).contains(null))
    val traitModel = model.references().asScala.find( ref => ref.location.endsWith("traits.raml") ).head
    val traitRefs = traitModel.references()
    val firstFragment = traitRefs.asScala.head
    assert(firstFragment.location != null)
    assert(firstFragment.asInstanceOf[TraitFragment].encodes != null)
    assert(!traitRefs.asScala.map(_.location).contains(null))
  }

}
