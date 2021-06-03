package amf.core.rdf

import amf.core.AMF
import amf.core.unsafe.PlatformSecrets
import amf.facades.Validation
import amf.plugins.features.validation.custom.AMFValidatorPlugin
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class RdfModelTests extends AsyncFunSuite with PlatformSecrets {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Manipulation of triples should invalidate cache") {
    for {
      _ <- Validation(platform)
    } yield {
      val model   = platform.rdfFramework.get.emptyRdfModel()
      val subject = "http://test.com/a"
      model.addTriple(subject, "http://test.com/p", "test", None)
      val resBefore = model.findNode(subject)
      assert(resBefore.isDefined)
      assert(resBefore.get.getKeys().size == 1)
      model.addTriple(subject, "http://test.com/pp", "test", None)
      val resAfter = model.findNode(subject)
      assert(resAfter.isDefined)
      assert(resAfter.get.getKeys().size == 2)

    }
  }
}
