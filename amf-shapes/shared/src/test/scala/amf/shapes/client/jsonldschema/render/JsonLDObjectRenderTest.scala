package amf.shapes.client.jsonldschema.render

import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.domain.common.DescribedElementModel
import amf.shapes.client.scala.config.JsonLDSchemaConfiguration
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import amf.shapes.internal.domain.metamodel.jsonldschema.JsonLDEntityModel
import amf.shapes.internal.spec.jsonldschema.parser.JsonPath
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers
import org.yaml.render.YamlRender

class JsonLDObjectRenderTest extends AsyncFunSuite with Matchers {

  val base = "http://a.ml/jsonld#"

  val model =
    JsonLDEntityModel(List(ValueType(s"${base}Object")), List(DescribedElementModel.Description), JsonPath.empty)

  val innerObjKey = s"${base}keyObj"
  val rootKey     = base + "key"

  val model2 =
    JsonLDEntityModel(List(ValueType(s"${base}Object2")), List(DescribedElementModel.Description), JsonPath.empty)
  val obj = JsonLDObject
    .empty(model, JsonPath.empty)
    .withProperty(rootKey, "value")
    .withProperty(innerObjKey, JsonLDObject.empty(model, JsonPath.empty).withProperty(s"${base}innerObj", "ab"))

  val expected =
    """key: value
      |keyObj:
      |  innerObj: ab""".stripMargin
  test("serialize jsonldObject") {
    val node   = JsonLDSchemaConfiguration.JsonLDSchema().elementClient().renderElement(obj, Nil)
    val result = YamlRender.render(node)
    result shouldBe (expected)
  }

  test("serialize mutated inner jsonldObject") {
    val updatedString =
      """key: value2
        |keyObj:
        |  innerObj: ab
        |  innerObjKey2: 2""".stripMargin

    val innerObj = obj.graph.getObjectByProperty(innerObjKey).collectFirst({ case obj: JsonLDObject => obj }).head
    obj.withProperty(innerObjKey, innerObj.withProperty(s"${base}innerObjKey2", 2)).withProperty(rootKey, "value2")

    val node   = JsonLDSchemaConfiguration.JsonLDSchema().elementClient().renderElement(obj, Nil)
    val result = YamlRender.render(node)
    result shouldBe (updatedString)
  }
}
