package amf.shapes.client.platform.model.domain.jsonldinstance

import amf.core.client.platform.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDObject => InternalJsonLDObject}
import amf.shapes.internal.domain.metamodel.jsonldschema.JsonLDEntityModel
import amf.shapes.internal.spec.jsonldschema.parser.JsonPath
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class JsonLDObject(override private[amf] val _internal: InternalJsonLDObject) extends DomainElement with JsonLDElement {
  @JSExportTopLevel("JsonLDObject")
  def this() = this(InternalJsonLDObject.empty(JsonLDEntityModel(Nil, Nil, JsonPath.empty), JsonPath.empty))
  def componentId: String = _internal.componentId

  def withProperty(property: String, value: String): JsonLDObject = _internal.withProperty(property, value)

  def withProperty(property: String, value: Integer): JsonLDObject = _internal.withProperty(property, value)

  def withProperty(property: String, value: Float): JsonLDObject = _internal.withProperty(property, value)

  def withProperty(property: String, value: Boolean): JsonLDObject = _internal.withProperty(property, value)

  def withProperty(property: String, value: JsonLDObject): JsonLDObject = _internal.withProperty(property, value)

  def withStringPropertyCollection(property: String, values: Seq[String]): JsonLDObject =
    _internal.withStringPropertyCollection(property, values)

  def withIntPropertyCollection(property: String, values: Seq[Integer]): JsonLDObject =
    _internal.withIntPropertyCollection(property, values)

  def withFloatPropertyCollection(property: String, values: Seq[Float]): JsonLDObject =
    _internal.withFloatPropertyCollection(property, values)

  def withBoolPropertyCollection(property: String, values: Seq[Boolean]): JsonLDObject =
    _internal.withBoolPropertyCollection(property, values)

  def withObjPropertyCollection(property: String, values: Seq[JsonLDObject]): JsonLDObject =
    _internal.withObjPropertyCollection(property, values)
}
