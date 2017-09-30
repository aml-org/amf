package amf.emit

import amf.client.GenerationOptions
import amf.common.Tests.checkDiff
import amf.compiler.AMFCompiler
import amf.dumper.AMFDumper
import amf.io.TmpTests
import amf.remote._
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class CompleteCycleTest extends AsyncFunSuite with TmpTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath       = "file://shared/src/test/resources/upanddown/"
  val referencesPath = "file://shared/src/test/resources/references/"

  test("Full raml to raml test") {
    assertCycle("full-example.raml", "full-example.raml.raml", RamlYamlHint, Raml)
  }

  test("Full oas to oas test") {
    assertCycle("full-example.json", "full-example.json", OasJsonHint, Oas)
  }

  test("Full raml to oas test") {
    assertCycle("full-example.raml", "full-example.raml.json", RamlYamlHint, Oas)
  }

  test("Full oas to raml test") {
    assertCycle("full-example.json", "full-example.json.raml", OasJsonHint, Raml)
  }

  test("Full raml to amf test") {
    assertCycle("full-example.raml", "full-example.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Full oas to amf test") {
    assertCycle("full-example.json", "full-example.json.jsonld", OasJsonHint, Amf)
  }

  test("Traits and resourceTypes raml to amf test") {
    assertCycle("traits-resource-types.raml", "traits-resource-types.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Traits and resourceTypes raml to raml test") {
    assertCycle("traits-resource-types.raml", "traits-resource-types.raml", RamlYamlHint, Raml)
  }

  test("Traits and resourceTypes oas to amf test") {
    assertCycle("traits-resource-types.json", "traits-resource-types.json.jsonld", OasJsonHint, Amf)
  }

  test("Traits and resourceTypes oas to oas test") {
    assertCycle("traits-resource-types.json", "traits-resource-types.json", OasJsonHint, Oas)
  }

  test("Traits and resourceTypes raml to oas test") {
    assertCycle("traits-resource-types.raml", "traits-resource-types.raml.json", RamlYamlHint, Oas)
  }

//  test("Full cycle raml to amf test") {
//    cycle("full-example.raml", RamlYamlHint, Amf)
//  }

  test("Basic cycle for amf") {
    cycle("basic.jsonld", AmfJsonHint, Amf)
  }

  test("Basic cycle for raml") {
    cycle("basic.raml", RamlYamlHint, Raml)
  }

  test("Basic cycle for oas") {
    cycle("basic.json", OasJsonHint, Oas)
  }

  test("Basic raml to amf test") {
    assertCycle("basic.raml", "basic.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Basic oas to amf test") {
    assertCycle("basic.json", "basic.json.jsonld", OasJsonHint, Amf)
  }

  test("Basic amf(raml) to raml test") {
    assertCycle("basic.raml.jsonld", "basic.raml", AmfJsonHint, Raml)
  }

  test("Basic amf(oas) to oas test") {
    assertCycle("basic.json.jsonld", "basic.json", AmfJsonHint, Oas)
  }

  test("Basic raml to oas test") {
    assertCycle("basic.raml", "basic.raml.json", RamlYamlHint, Oas)
  }

  test("Basic oas to raml test") {
    assertCycle("basic.json", "basic.json.raml", OasJsonHint, Raml)
  }

  test("Complete amf to amf test") {
    assertCycle("complete.jsonld", "complete.jsonld", AmfJsonHint, Amf)
  }

  test("Complete raml to amf test") {
    assertCycle("complete.raml", "complete.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Complete raml to oas test") {
    assertCycle("complete.raml", "complete.json", RamlYamlHint, Oas)
  }

  test("Complete oas to amf test") {
    assertCycle("complete.json", "complete.json.jsonld", OasJsonHint, Amf)
  }

  test("Complete oas to raml test") {
    assertCycle("complete.json", "complete.raml", OasJsonHint, Raml)
  }

  test("Complete raml to raml test") {
    assertCycle("complete.raml", "complete.raml", RamlYamlHint, Raml)
  }

  test("Complete oas to oas test") {
    assertCycle("complete.json", "complete.json", OasJsonHint, Oas)
  }

  test("Complete amf(raml) to raml test") {
    assertCycle("complete.raml.jsonld", "complete.raml", AmfJsonHint, Raml)
  }

  test("Complete amf(oas) to oas test") {
    assertCycle("complete.json.jsonld", "complete.json", AmfJsonHint, Oas)
  }

  test("Endpoints amf to amf test") {
    assertCycle("endpoints.jsonld", "endpoints.jsonld", AmfJsonHint, Amf)
  }

  test("Endpoints raml to amf test") {
    assertCycle("endpoints.raml", "endpoints.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Endpoints raml to oas test") {
    assertCycle("endpoints.raml", "endpoints.json", RamlYamlHint, Oas)
  }

  test("Endpoints raml to raml test") {
    assertCycle("endpoints.raml", "endpoints.raml", RamlYamlHint, Raml)
  }

  test("Endpoints oas to raml test") {
    assertCycle("endpoints.json", "endpoints.json.raml", OasJsonHint, Raml)
  }

  test("Endpoints oas to amf test") {
    assertCycle("endpoints.json", "endpoints.json.jsonld", OasJsonHint, Amf)
  }

  test("Endpoints oas to oas test") {
    assertCycle("endpoints.json", "endpoints.json", OasJsonHint, Oas)
  }

  test("Endpoints amf(raml) to raml test") {
    assertCycle("endpoints.raml.jsonld", "endpoints.raml", AmfJsonHint, Raml)
  }

  test("Endpoints amf(oas) to oas test") {
    assertCycle("endpoints.json.jsonld", "endpoints.json", AmfJsonHint, Oas)
  }

  test("Complete with operations raml to oas test") {
    assertCycle("complete-with-operations.raml", "complete-with-operations.json", RamlYamlHint, Oas)
  }

  test("Complete with operations raml to raml test") {
    assertCycle("complete-with-operations.raml", "complete-with-operations.raml", RamlYamlHint, Raml)
  }

  test("Complete with operations oas to raml test") {
    assertCycle("complete-with-operations.json", "complete-with-operations.json.raml", OasJsonHint, Raml)
  }

  test("Complete with operations oas to oas test") {
    assertCycle("complete-with-operations.json", "complete-with-operations.json", OasJsonHint, Oas)
  }

  test("Complete with request raml to raml test") {
    assertCycle("operation-request.raml", "operation-request.raml.raml", RamlYamlHint, Raml)
  }

  test("Complete with request oas to raml test") {
    assertCycle("operation-request.json", "operation-request.json.raml", OasJsonHint, Raml)
  }

  test("Complete with request raml to oas test") {
    assertCycle("operation-request.raml", "operation-request.raml.json", RamlYamlHint, Oas)
  }

  test("Complete with response oas to raml test") {
    assertCycle("operation-response.json", "operation-response.raml", OasJsonHint, Raml)
  }

  test("Complete with response oas to oas test") {
    assertCycle("operation-response.json", "operation-response.json", OasJsonHint, Oas)
  }

  test("Complete with response raml to raml test") {
    assertCycle("operation-response.raml", "operation-response.raml", RamlYamlHint, Raml)
  }

  test("Complete with response raml to oas test") {
    assertCycle("operation-response.raml", "operation-response.raml.json", RamlYamlHint, Oas)
  }

  test("Complete with payloads raml to raml test") {
    assertCycle("payloads.raml", "payloads.raml.raml", RamlYamlHint, Raml)
  }

  test("Complete with payloads raml to oas test") {
    assertCycle("payloads.raml", "payloads.raml.json", RamlYamlHint, Oas)
  }

  test("Complete with payloads oas to oas test") {
    assertCycle("payloads.json", "payloads.json", OasJsonHint, Oas)
  }

  test("Complete with payloads oas to raml test") {
    assertCycle("payloads.json", "payloads.json.raml", OasJsonHint, Raml)
  }

  test("Children endpoints amf to raml test") {
    assertCycle("banking-api.raml.jsonld", "banking-api.jsonld.raml", AmfJsonHint, Raml)
  }

  test("Children endpoints raml to raml test") {
    assertCycle("banking-api.raml", "banking-api.raml.raml", RamlYamlHint, Raml)
  }

  test("Children endpoints amf to amf test") {
    assertCycle("banking-api.raml", "banking-api.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Types raml to amf test") {
    assertCycle("types.raml", "types.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Types amf(raml) to amf test") {
    assertCycle("types.raml.jsonld", "types.raml.jsonld", AmfJsonHint, Amf)
  }

  test("Types implicit & explicit raml to raml test") {
    assertCycle("explicit-&-implicit-type-object.raml", "explicit-&-implicit-type-object.raml", RamlYamlHint, Raml)
  }

  test("Types implicit & explicit oas to oas test") {
    assertCycle("explicit-&-implicit-type-object.json", "explicit-&-implicit-type-object.json", OasJsonHint, Oas)
  }

  test("Types implicit & explicit raml to oas test") {
    assertCycle("explicit-&-implicit-type-object.raml", "explicit-&-implicit-type-object.raml.json", RamlYamlHint, Oas)
  }

  test("Types implicit & explicit oas to raml test") {
    assertCycle("explicit-&-implicit-type-object.json", "explicit-&-implicit-type-object.json.raml", OasJsonHint, Raml)
  }

  test("Types dependency raml to raml test") {
    assertCycle("types-dependency.raml", "types-dependency.raml", RamlYamlHint, Raml)
  }

  test("Types dependency oas to oas test") {
    assertCycle("types-dependency.json", "types-dependency.json", OasJsonHint, Oas)
  }

  test("Types dependency raml to oas test") {
    assertCycle("types-dependency.raml", "types-dependency.raml.json", RamlYamlHint, Oas)
  }

  test("Types dependency oas to raml test") {
    assertCycle("types-dependency.json", "types-dependency.json.raml", OasJsonHint, Raml)
  }

  test("Types dependency raml to amf test") {
    assertCycle("types-dependency.raml", "types-dependency.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Types dependency oas to amf test") {
    assertCycle("types-dependency.json", "types-dependency.json.jsonld", OasJsonHint, Amf)
  }

  ignore("Types dependency amf(raml) to raml test") {
    assertCycle("types-dependency.raml.jsonld", "types-dependency.raml", AmfJsonHint, Raml)
  }

  ignore("Types dependency amf(oas) to oas test") {
    assertCycle("types-dependency.json.jsonld", "types-dependency.json", AmfJsonHint, Oas)
  }

  test("Types declarations oas to oas test") {
    assertCycle("declarations-small.json", "declarations-small.json", OasJsonHint, Oas)
  }

  test("Types declarations raml to raml test") {
    assertCycle("declarations-small.raml", "declarations-small.raml", RamlYamlHint, Raml)
  }

  test("Types all facets raml to raml test") {
    assertCycle("types-facet.raml", "types-facet.raml", RamlYamlHint, Raml)
  }

  test("Types all facets oas to oas test") {
    assertCycle("types-facet.json", "types-facet.json", OasJsonHint, Oas)
  }

  test("Types all facets oas to raml test") {
    assertCycle("types-facet.json", "types-facet.json.raml", OasJsonHint, Raml)
  }

  test("Types all facets raml to oas test") {
    assertCycle("types-facet.raml", "types-facet.raml.json", RamlYamlHint, Oas)
  }

  test("Types all types raml to raml test") {
    assertCycle("all-type-types.raml", "all-type-types.raml.raml", RamlYamlHint, Raml)
  }

  test("Annotations raml to raml test") {
    assertCycle("annotations.raml", "annotations.raml", RamlYamlHint, Raml)
  }

  test("Annotations raml to jsonld test") {
    assertCycle("annotations.raml", "annotations.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Annotations jsonld to jsonld test") {
    assertCycle("annotations.raml.jsonld", "annotations.raml.jsonld", AmfJsonHint, Amf)
  }

  test("Annotations oas to jsonld test") {
    assertCycle("annotations.json", "annotations.json.jsonld", OasJsonHint, Amf)
  }

  test("Annotations oas to oas test") {
    assertCycle("annotations.json", "annotations.json", OasJsonHint, Oas)
  }

  test("Types all types oas to oas test") {
    assertCycle("all-type-types.json", "all-type-types.json.json", OasJsonHint, Oas)
  }

  test("Types all types raml to oas test") {
    assertCycle("all-type-types.raml", "all-type-types.raml.json", RamlYamlHint, Oas)
  }

  test("Types all types oas to raml test") {
    assertCycle("all-type-types.json", "all-type-types.json.raml", OasJsonHint, Raml)
  }

  test("Test libraries raml to raml") {
    assertCycle("libraries.raml", "libraries.raml.raml", RamlYamlHint, Raml, referencesPath)
  }

  test("Test libraries oas to oas") {
    assertCycle("libraries.json", "libraries.json.json", OasJsonHint, Oas, referencesPath)
  }

  test("Test libraries raml to amf") {
    assertCycle("libraries.raml", "libraries.raml.jsonld", RamlYamlHint, Amf, referencesPath)
  }

  test("Test libraries amf to raml") {
    assertCycle("libraries.raml.jsonld", "libraries.raml.raml", AmfJsonHint, Raml, referencesPath)
  }

  test("Test libraries oas to amf") {
    assertCycle("libraries.json", "libraries.json.jsonld", OasJsonHint, Amf, referencesPath)
  }

  test("Test libraries amf to oas") {
    assertCycle("libraries.json.jsonld", "libraries.json.json", AmfJsonHint, Oas, referencesPath)
  }

  test("Test data type fragment raml to raml") {
    assertCycle("data-type-fragment.raml", "data-type-fragment.raml", AmfJsonHint, Raml, referencesPath)
  }

  test("Test data type fragment amf to raml") {
    assertCycle("data-type-fragment.raml.jsonld", "data-type-fragment.raml", AmfJsonHint, Raml, referencesPath)
  }

  test("Test data type fragment amf to amf") {
    assertCycle("data-type-fragment.raml.jsonld", "data-type-fragment.raml.jsonld", AmfJsonHint, Amf, referencesPath)
  }
//
//  test("Test data type fragment amf to amf") {
//    assertCycle("data-type-fragment.raml.jsonld", "data-type-fragment.raml.jsonld", AmfJsonHint, Amf, referencesPath)
//  }

  // todo what we do when library file name changes changes on dump
  ignore("Test libraries raml to oas") {
    assertCycle("libraries.raml", "libraries.json.json", RamlYamlHint, Oas, referencesPath)
  }

  ignore("Test libraries oas to raml") {
    assertCycle("libraries.json", "libraries.raml.raml", OasJsonHint, Raml, referencesPath)
  }

  def assertCycle(source: String,
                  golden: String,
                  hint: Hint,
                  target: Vendor,
                  path: String = basePath): Future[Assertion] = {
    val expected = platform
      .resolve(path + golden, None)
      .map(_.stream.toString)

    val actual = AMFCompiler(path + source, platform, hint)
      .build()
      .flatMap(unit =>
        new AMFDumper(unit, target, target.defaultSyntax, GenerationOptions().withSourceMaps).dumpToString)

    actual
      .zip(expected)
      .map(checkDiff)

  }

  def cycle(source: String, hint: Hint, target: Vendor): Future[Assertion] = {
    AMFCompiler(basePath + source, platform, hint)
      .build()
      .flatMap(new AMFDumper(_, target, target.defaultSyntax, GenerationOptions().withSourceMaps).dumpToString)
      .flatMap(content => {
        val file = tmp(source + ".tmp")
        platform.write("file://" + file, content).map((_, content))
      })
      .flatMap({
        case (path, actual) =>
          platform
            .resolve(basePath + source, None)
            .map(expected => checkDiff(actual, path, expected.stream.toString, expected.url))
      })
  }
}
