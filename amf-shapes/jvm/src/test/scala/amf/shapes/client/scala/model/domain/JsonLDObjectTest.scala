package amf.shapes.client.scala.model.domain

import amf.core.client.platform.model.domain.DomainElement
import amf.core.internal.convert.NativeOpsFromJvm
import amf.shapes.client.platform.model.domain.jsonldinstance.JsonLDObject
import amf.shapes.client.scala.config.JsonLDSchemaConfiguration
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

class JsonLDObjectTest extends AsyncFunSuite with NativeOpsFromJvm with Matchers {

  private val BASE      = "http://a.ml/test#"
  private val innerKey1 = BASE + "innerKey1"
  private val innerKey2 = BASE + "innerKey2"

  private val innerKey3 = BASE + "innerKey3"
  private val innerKey4 = BASE + "innerKey4"

  private val obj1 = new JsonLDObject()
    .withProperty(innerKey1, "a")
    .withProperty(innerKey2, 1)

  private val obj2 = new JsonLDObject()
    .withProperty(BASE + "innerKey3", "b")
    .withProperty(BASE + "innerKey4", 2)

  test("Test set and get object property") {
    JsonLDSchemaConfiguration.JsonLDSchema()
    val objJKey    = BASE + "key1"
    val dObject    = new JsonLDObject().withProperty(objJKey, obj1)
    val collection = dObject.graph().getObjectByProperty(objJKey).native
    collection.size() shouldBe (1)
    assertInnerKey1(collection.get(0))
  }

//  test("Test set and get object collection property") {
//    JsonLDSchemaConfiguration.JsonLDSchema()
//    val objKey  = BASE + "key2"
//    val dObject = new JsonLDObject().withObjPropertyCollection(objKey, java.util.List.of(obj1, obj2))
//    val value   = dObject.graph().getObjectByProperty(objKey).native
//    value.size() shouldBe (2)
//
//    val headElement = value.get(0)
//    assertInnerKey1(headElement)
//
//    val innerKey3Value = value.get(1).graph().scalarByProperty(innerKey3).native
//    innerKey3Value.size() shouldBe (1)
//    innerKey3Value.get(0) shouldBe ("b")
//
//  }

  private def assertInnerKey1(element: DomainElement): Assertion = {
    element.isInstanceOf[JsonLDObject] shouldBe (true)
    val innerKey1Value = element.graph().scalarByProperty(innerKey1).native
    innerKey1Value.size() shouldBe (1)
    innerKey1Value.get(0) shouldBe ("a")
  }
}
