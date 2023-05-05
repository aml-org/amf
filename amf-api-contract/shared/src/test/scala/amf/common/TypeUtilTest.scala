package amf.common

import amf.apicontract.client.platform.common.TypeIRI
import amf.apicontract.client.scala.common.TypeUtil
import amf.shapes.client.scala.model.domain.{NilShape, NodeShape, ScalarShape}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class TypeUtilTest extends AnyFunSuite with Matchers {

  test("Test TypeUtil NodeShape") {
    TypeUtil.isTypeOf(NodeShape(), TypeIRI.NodeShape) shouldBe true
  }

  test("Test TypeUtil ScalarShape") {
    TypeUtil.isTypeOf(ScalarShape(), TypeIRI.ScalarShape) shouldBe true
  }

  test("Test TypeUtil NilShape") {
    TypeUtil.isTypeOf(NilShape(), TypeIRI.ScalarShape) shouldBe false
    TypeUtil.isTypeOf(NilShape(), TypeIRI.Shape) shouldBe true
    TypeUtil.isTypeOf(NilShape(), TypeIRI.NilShape) shouldBe true
    TypeUtil.isTypeOf(NilShape(), "http://a.ml/vocabularies/shapes#NilShape") shouldBe true
  }

}
