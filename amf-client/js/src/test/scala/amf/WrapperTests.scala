package amf

import amf.core.unsafe.PlatformSecrets
import amf.model.document.Document
import amf.model.domain.{ScalarShape, WebApi}
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class WrapperTests extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Parsing test") {
    AMF.init().toFuture.flatMap { _ =>
      val parser = new Raml10Parser()
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").toFuture
    } flatMap { baseUnit =>
      assert(baseUnit.location == "file://amf-client/shared/src/test/resources/api/zencoder.raml")

      val api = baseUnit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
      assert(api != null)

      val endpoint = api.endPoints.toSeq.head
      assert(endpoint.path == "/v3.5/path")
      assert(api.endPoints.size == 1)
      assert(endpoint.operations.size == 1)
      val post = endpoint.operations.toSeq.head
      assert(post.method == "get")
      assert(post.request.payloads.size == 1)
      assert(post.request.payloads.toSeq.head.mediaType == "application/json")
      assert(post.request.payloads.toSeq.head.schema.getTypeIds().toSeq.contains("http://www.w3.org/ns/shacl#ScalarShape"))
      assert(post.request.payloads.toSeq.head.schema.getTypeIds().toSeq.contains("http://www.w3.org/ns/shacl#Shape"))
      assert(post.request.payloads.toSeq.head.schema.getTypeIds().toSeq.contains("http://raml.org/vocabularies/shapes#Shape"))
      assert(post.request.payloads.toSeq.head.schema.getTypeIds().toSeq.contains("http://raml.org/vocabularies/document#DomainElement"))

      assert(post.responses.toSeq.head.payloads.toSeq.head.schema.asInstanceOf[ScalarShape].dataType == "http://www.w3.org/2001/XMLSchema#string")
      assert(post.request.payloads.toSeq.head.schema.asInstanceOf[ScalarShape].dataType == "http://www.w3.org/2001/XMLSchema#string")
      assert(post.responses.toSeq.head.statusCode == "200")
    }
  }

  test("Generation test") {
    AMF.init().toFuture.flatMap { _ =>
      val parser = new Raml10Parser()
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").toFuture
    } flatMap { baseUnit =>
      assert(new Raml10Generator().generateString(baseUnit) != "") // TODO: test this properly
      assert(new Oas20Generator().generateString(baseUnit) != "")
      assert(new AmfGraphGenerator().generateString(baseUnit) != "")
    }
  }

  test("Resolution test") {
    AMF.init().toFuture flatMap {_ =>
      val parser = new Raml10Parser()
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/zencoder.raml").toFuture
    } flatMap { baseUnit =>
      AMF.validate(baseUnit, "RAML").toFuture.map{ report => (baseUnit, report) }
    } flatMap { case(baseUnit, report) =>
      assert(report.conforms)
      AMF.loadValidationProfile("file://amf-client/shared/src/test/resources/api/validation/custom-profile.raml").toFuture.map { _ => baseUnit }
    } flatMap { baseUnit =>
      AMF.validate(baseUnit, "Banking").toFuture
    } flatMap { custom =>
      assert(!custom.conforms)
    }
  }

  test("Vocabularies test") {
    AMF.init().toFuture flatMap { _ =>
      AMF.registerDialect("file://amf-client/shared/src/test/resources/api/dialects/eng-demos.raml").toFuture
    } flatMap { _ =>
      val parser = new Raml10Parser()
      parser.parseFileAsync("file://amf-client/shared/src/test/resources/api/examples/libraries/demo.raml").toFuture
    } flatMap { baseUnit =>
      AMF.validate(baseUnit, "Eng Demos 0.1").toFuture.map { report => (report, baseUnit)}
    } flatMap { case (report, baseUnit) =>
      assert(report.conforms)
      AMF.registerNamespace("eng-demos", "http://mulesoft.com/vocabularies/eng-demos#")
      val elem = baseUnit.asInstanceOf[Document].encodes
      val speakers = elem.getObjectByPropertyId("eng-demos:speakers")
      assert(speakers.toSeq.nonEmpty)
    }
  }
}