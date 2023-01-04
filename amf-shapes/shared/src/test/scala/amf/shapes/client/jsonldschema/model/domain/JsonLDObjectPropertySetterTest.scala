package amf.shapes.client.jsonldschema.model.domain

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.{Field, Type}
import amf.core.internal.metamodel.domain.common.DescribedElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import amf.shapes.internal.domain.metamodel.jsonldschema.JsonLDEntityModel
import amf.shapes.internal.spec.jsonldschema.parser.JsonPath
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

class JsonLDObjectPropertySetterTest extends AsyncFunSuite with Matchers {

  val base = "http://a.ml/jsonld#"
  val model =
    JsonLDEntityModel(List(ValueType(s"${base}Object")), List(DescribedElementModel.Description), JsonPath.empty)
  val initialObj = JsonLDObject(
    Fields().set("", DescribedElementModel.Description, AmfScalar("test description")),
    Annotations(),
    model,
    JsonPath.empty
  )

  test("Test add string property") {
    val uri     = s"${base}name"
    val value   = "Juan Roman"
    val updated = initialObj.withProperty(uri, value)
    testValue(uri, value, updated)
  }

  test("Test add integer property") {
    val uri     = s"${base}age"
    val value   = 33
    val updated = initialObj.withProperty(uri, value)
    testValue(uri, value, updated)
  }

  test("Test add float property") {
    val uri          = s"${base}distance"
    val value: Float = 33.5f
    val updated      = initialObj.withProperty(uri, value)
    testValue(uri, value, updated)
  }

  test("Test add bool property") {
    val uri     = s"${base}present"
    val value   = true
    val updated = initialObj.withProperty(uri, value)
    testValue(uri, value, updated)
  }

  test("Test add object property") {
    val uri     = s"${base}present"
    val value   = JsonLDObject.empty(JsonLDEntityModel(Nil, Nil, JsonPath.empty), JsonPath.empty)
    val updated = initialObj.withProperty(uri, value)
    testValue(uri, value, updated)
  }

  test("Test add str list property") {
    val uri     = s"${base}siblings"
    val value   = List("Kevin", "Francis")
    val updated = initialObj.withStringPropertyCollection(uri, value)
    testCollection(uri, value, updated)
  }

  test("Test add int list property") {
    val uri              = s"${base}measures"
    val value: List[Int] = List(1, 2)
    val updated          = initialObj.withIntPropertyCollection(uri, value)
    testCollection(uri, value, updated)
  }

  test("Test add float list property") {
    val uri                = s"${base}surface"
    val value: List[Float] = List(1.2f, 2.1f)
    val updated            = initialObj.withFloatPropertyCollection(uri, value)
    testCollection(uri, value, updated)
  }

  test("Test add bool list property") {
    val uri                  = s"${base}flags"
    val value: List[Boolean] = List(true, false)
    val updated              = initialObj.withBoolPropertyCollection(uri, value)
    testCollection(uri, value, updated)
  }

  test("Test add obj list property") {
    val uri = s"${base}nodes"
    val value: List[JsonLDObject] = List(
      JsonLDObject.empty(JsonLDEntityModel(Nil, Nil, JsonPath.empty), JsonPath.empty),
      JsonLDObject.empty(JsonLDEntityModel(Nil, Nil, JsonPath.empty), JsonPath.empty)
    )
    val updated = initialObj.withObjPropertyCollection(uri, value)
    testCollection(uri, value, updated)
  }

  private def testValue(uri: String, value: Any, updated: JsonLDObject) = {
    testObj(updated, uri)
    if (value.isInstanceOf[JsonLDObject]) updated.graph.getObjectByProperty(uri).head shouldBe value
    else updated.graph.scalarByProperty(uri).head shouldBe value
  }

  private def testObj(updated: JsonLDObject, uri: String) = {
    assert(!initialObj.graph.containsProperty(uri))
    assert(updated.graph.containsProperty(uri))
    assert(!initialObj.model.fields.contains(Field(Type.Str, ValueType(uri))))
    assert(updated.model.fields.contains(Field(Type.Str, ValueType(uri))))
  }

  private def testCollection(uri: String, values: Seq[Any], updated: JsonLDObject) = {
    testObj(updated, uri)
    if (values.head.isInstanceOf[JsonLDObject]) {
      updated.graph.getObjectByProperty(uri).head shouldBe values.head
      updated.graph.getObjectByProperty(uri).size shouldBe values.size
    } else {
      updated.graph.scalarByProperty(uri).head.asInstanceOf[AmfScalar].value shouldBe values.head
      updated.graph.scalarByProperty(uri).size shouldBe values.size
    }
  }
}
