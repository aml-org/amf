package amf.document.apicontract.validation.payload

import amf.apicontract.internal.validation.payload.collector.EnumInShapesCollector
import amf.core.client.scala.model.domain.ScalarNode
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ValidationCandidateCollectorTest extends AnyFunSuite with Matchers {

  test("generation of validation candidates for enums in shape") {
    val shape = NodeShape().withId("id")
    shape.withValues(List(ScalarNode("value 1", None), ScalarNode("value 1", None)))

    val candidates = EnumInShapesCollector.collect(shape)

    val candidateShape = candidates.head.shape

    candidates.size shouldBe 2
    // shape that is use for validation should not contain enum values to avoid performance issues
    candidateShape.values shouldBe empty
    // original shape must not be modified, its enum value should still be defined
    shape.values should not be empty

  }
}
