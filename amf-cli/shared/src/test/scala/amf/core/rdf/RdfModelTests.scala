package amf.core.rdf

import amf.core.internal.unsafe.PlatformSecrets
import org.scalatest.FunSuite

class RdfModelTests extends FunSuite with PlatformSecrets {

  test("Manipulation of triples should invalidate cache") {
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
