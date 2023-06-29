package amf.compiler

import amf.apicontract.client.scala.{AMFBaseUnitClient, APIConfiguration}
import amf.core.client.common.remote.Content
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.resource.ResourceLoader
import amf.core.internal.remote.Spec.{OAS20, OAS30, RAML10}
import amf.core.internal.remote.{FileNotFound, Spec}
import amf.core.internal.unsafe.PlatformSecrets
import org.mulesoft.common.test.AsyncBeforeAndAfterEach
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class ApikitApiSyncCasesTest extends AsyncBeforeAndAfterEach with PlatformSecrets with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val base = "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync"

  test("Resource loader shouldn't have absolute path if ref and base aren't absolute") {
    val main = "resource::really-cool-urn:1.0.0:raml:zip:main.raml"
    val builder = new UrnLoaderBuilder(s"$base/ref-base-not-absolute")
      .addMapping(main, "main.raml")
      .addMapping("something.raml")
      .addMapping("examples/something/get-something-response.raml")
      .addMapping("examples/something/get-something-request.raml")
      .addMapping("examples/something/put-something-request.raml")
      .addMapping("examples/common/async-response.raml")
      .addMapping("libraries/resourceTypes.raml")
    val loader = builder.build()
    val client = getClient(loader, RAML10)
    client.parse(main).map { result =>
      result.results should have size 0
    }
  }

  // APIMF-3118
  test("Resource loader should find nested references") {
    val main = "resource::37fab092-be99-4538-b5ce-b004c5439f6d:refexample:1.0.1:oas:zip:example.json"
    val loader = new UrnLoaderBuilder(s"$base/uri-file-prefix")
      .addMapping(main, "example.json")
      .addMapping("exchange.json")
      .addMapping("components/schemas/okResponse.json")
      .addMapping("components/schemas/item.json")
      .addMapping("components/schemas/nested.json")
      .build()
    val client = getClient(loader, OAS30)
    client.parse(main).map { parseResult =>
      parseResult.results should have size 0
    }
  }

  test("Parsing context generated in extends resolution stage for raml traits") {
    val main = "resource::really-cool-urn:1.0.0:raml:zip:townfile.raml"
    val loader = new UrnLoaderBuilder(s"$base/extends-stage-traits")
      .addMapping(main, "townfile.raml")
      .addMapping("some-modules/for-health-check.raml")
      .addMapping("some-modules/trait.raml")
      .build()
    val client = getClient(loader, RAML10)
    for {
      parseResult <- client.parse(main)
      transformResult = client.transform(parseResult.baseUnit, PipelineId.Editing)
    } yield {
      parseResult.results should have size 0
      transformResult.results should have size 0
    }
  }

  // APIMF-3384
  test("should accept content URLs starting with 'jar:'") {
    val main = "resource::com.mycompany:consumer-api:1.0.0:oas:zip:consumer.yaml"
    val loader = new UrnLoaderBuilder(s"$base/jar-protocol")
      .addMapping(main, "consumer.yaml")
      .addMapping("utility.yaml")
      .build()
    val client = getClient(loader, OAS30)
    client.parse(main).map { parseResult =>
      parseResult.results should have size 0
    }
  }

  // APIMF-3600
  test("exchange modules with multiple zip sources") {
    val main = "resource::8fe5354c-e64c-4eaa-addc-b50906a0b48c:se-23375:1.0.1:oas:zip:se-23375.json"
    val loader = new UrnLoaderBuilder(s"$base/exchange-modules")
      .addMapping(main, "se-23375.json")
      .addMapping(
        "exchange_modules/8fe5354c-e64c-4eaa-addc-b50906a0b48c/datamodel-tmforum/1.0.0/4.1.0/Customer/Bucket.schema.json",
        "Bucket.schema.json"
      )
      .build()
    val client: AMFBaseUnitClient = getClient(loader, OAS20)
    client.parse(main).map { parseResult =>
      parseResult.results should have size 0
    }
  }

  // APIMF-3533
  private def getClient(loader: ResourceLoader, spec: Spec) = getConfig(loader, spec).baseUnitClient()
  private def getConfig(loader: ResourceLoader, spec: Spec) = {
    APIConfiguration
      .fromSpec(spec)
      .withResourceLoaders(List(loader))
  }

  test("references to external yaml using './'") {
    val main = "resource::8fe5354c-e64c-4eaa-addc-b50906a0b48c:pure-member:1.0.0:oas:zip:pure-member-portal.yaml"
    val loader = new UrnLoaderBuilder(s"$base/dot-slash-ref")
      .addMapping(main, "pure-member-portal.yaml")
      .addMapping("responses.yaml")
      .build()
    val client = getClient(loader, OAS30)
    client.parse(main).map { parseResult =>
      parseResult.results should have size 0
    }
  }

  class URNResourceLoader(mappings: Map[String, String]) extends ResourceLoader {

    override def fetch(resource: String): Future[Content] = {
      mappings
        .get(resource)
        .map(url => {
          platform
            .fetchContent(url, AMFGraphConfiguration.predefined())
            .map(content => Content(content.stream, resource)) // content result url provided by resource loader
        })
        .getOrElse(throw FileNotFound(new RuntimeException(s"Couldn't find resource $resource")))
    }

    override def accepts(resource: String): Boolean = true
  }

  class UrnLoaderBuilder(
      private val base: String,
      private val internal: mutable.Map[String, String] = mutable.Map.empty[String, String]
  ) {

    def addMapping(urn: String, fs: String): this.type = {
      internal.put(urn, s"$base/$fs")
      this
    }

    def addMapping(path: String): this.type = addMapping(path, path)

    def build() = new URNResourceLoader(internal.toMap)
  }
}
