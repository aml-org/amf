package amf.client

import amf.ProfileNames
import amf.common.AmfObjectTestMatcher
import amf.model.{BaseUnit, Document, Module, WebApi}
import amf.unsafe.PlatformSecrets
import amf.validation.Validation
import org.scalatest.Matchers._
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.ExecutionContext
import scala.scalajs.js.Promise

/**
  *
  */
class ParserTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest with AmfObjectTestMatcher {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test from stream generation oas") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.json", None)
      .flatMap(stream => {
        val eventualUnit = new OasParser().parseStringAsync(stream.stream.toString)
        eventualUnit.toFuture
      }) map {
      case d: Document =>
        AmfObjectMatcher(webApiBare.element).assert(d.encodes.element)
        succeed
    }
  }

  test("test from stream generation raml") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.raml", None)
      .flatMap(stream => {
        val eventualUnit = new RamlParser().parseStringAsync(stream.stream.toString)
        eventualUnit.toFuture
      }) map {
      case d: Document =>
        AmfObjectMatcher(webApiBare.element.withBasePath("/api")).assert(d.encodes.element)
        succeed
    }
  }

  test("test from stream generation amf") {
    platform
      .resolve("file://shared/src/test/resources/clients/bare.jsonld", None)
      .flatMap(stream => {
        val eventualUnit = new AmfParser().parseStringAsync(stream.stream.toString)
        eventualUnit.toFuture
      }) map {
      case d: Document =>
        AmfObjectMatcher(webApiBare.element).assert(d.encodes.element)
        succeed
    }
  }

  test("test from file generation") {
    new OasParser()
      .parseFileAsync("file://shared/src/test/resources/clients/bare.json")
      .toFuture map {
      case d: Document =>
        AmfObjectMatcher(webApiBare.element).assert(d.encodes.element)
        succeed
    }
  }

  test("test from stream complete generation") {
    platform
      .resolve("file://shared/src/test/resources/clients/advanced.json", None)
      .flatMap(stream => {
        val value1: Promise[BaseUnit] = new OasParser().parseStringAsync(stream.stream.toString)
        value1.toFuture
      }) map {
      case d: Document =>
        AmfObjectMatcher(webApiAdvanced.element).assert(d.encodes.element)
        succeed
    }
  }

  test("test from file complete generation") {
    new OasParser()
      .parseFileAsync("file://shared/src/test/resources/clients/advanced.json")
      .toFuture map {
      case d: Document =>
        AmfObjectMatcher(webApiAdvanced.element).assert(d.encodes.element)
        succeed
    }
  }

  test("test from library file complete generation") {
    new RamlParser()
      .parseFileAsync("file://shared/src/test/resources/clients/libraries.raml")
      .toFuture map {
      case d: Document =>
        d.references.head match {
          case m: Module =>
            AmfObjectMatcher(moduleBare.model).assert(m.model)
            succeed
          case _ => fail("unexpected type")
        }
    }
  }

  test("Validation model interface") {
    val examplesPath = "file://shared/src/test/resources/validations/"
    val parser = new RamlParser()
    for {
      model  <- parser.parseFileAsync(examplesPath + "library/nested.raml").toFuture
      report <- parser.reportValidation(ProfileNames.RAML).toFuture
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("Custom validation model interface") {
    val examplesPath = "file://shared/src/test/resources/validations/"

    val parser = new RamlParser()
    for {
      model  <- parser.parseFileAsync(examplesPath + "banking/api.raml").toFuture
      report <- parser.reportCustomValidation("Banking", examplesPath + "banking/profile.raml").toFuture
    } yield {
      assert(!report.conforms)
      assert(report.results.nonEmpty)
    }
  }

  def assertWebApi(actual: WebApi, expected: WebApi): Assertion = {
    actual should be(expected)
    succeed
  }

  def assertModule(actual: Module, expected: Module): Assertion = {
    actual should be(expected)
    succeed
  }

}
