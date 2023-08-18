package amf.shapes.client.scala.model.domain.jsonldinstance

import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.{Field, Obj, Type}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.jsonldschema.JsonLDEntityModel
import amf.shapes.internal.spec.jsonldschema.parser.JsonPath
import org.mulesoft.common.time.SimpleDateTime

object JsonLDObject {
  def empty(model: JsonLDEntityModel, path: JsonPath): JsonLDObject =
    new JsonLDObject(Fields(), Annotations(), model, path)
}

case class JsonLDObject(
    override val fields: Fields,
    override val annotations: Annotations,
    private var model: JsonLDEntityModel,
    path: JsonPath
) extends DomainElement
    with JsonLDElement {
  override def meta: Obj = model

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId = s"/${path.toString}"

  implicit class FieldBuilder(property: String) {

    def toObjField(meta: Obj): Field = Field(meta, ValueType(property))

    def toObjListField: Field = Field(Type.Array(Type.ObjType), ValueType(property))
    def toStrField: Field     = Field(Type.Str, ValueType(property))

    def toStrListField: Field = Field(Type.Array(Type.Str), ValueType(property))

    def toIntField: Field = Field(Type.Int, ValueType(property))

    def toIntListField: Field = Field(Type.Array(Type.Int), ValueType(property))
    def toBoolField: Field    = Field(Type.Bool, ValueType(property))

    def toBoolListField: Field = Field(Type.Array(Type.Bool), ValueType(property))
    def toFloatField: Field    = Field(Type.Float, ValueType(property))
    def toDoubleField: Field   = Field(Type.Double, ValueType(property))

    def toFloatListField: Field = Field(Type.Array(Type.Float), ValueType(property))
  }

  private def buildString(value: String) = new JsonLDScalar(value, DataTypes.String)

  private def buildDateTime(value: SimpleDateTime) = new JsonLDScalar(value, DataTypes.DateTime)
  private def buildDate(value: SimpleDateTime)     = new JsonLDScalar(value, DataTypes.Date)

  private def buildInteger(value: Int) = new JsonLDScalar(value, DataTypes.Integer)

  private def buildBoolean(value: Boolean) = new JsonLDScalar(value, DataTypes.Boolean)
  private def buildFloat(value: Float)     = new JsonLDScalar(value, DataTypes.Float)
  private def buildDouble(value: Double)   = new JsonLDScalar(value, DataTypes.Double)

  private def buildArray(values: Seq[JsonLDElement]) = JsonLDArray(values)

  private def updateModel(field: Field) = {
    if (model.fields.contains(field)) model // preserve initial order
    else model.copy(fields = model.fields :+ field)
  }
  private def updateModelAndSet(field: Field, element: JsonLDElement) = {
    model = updateModel(field)
    set(field, element)
  }

  def withProperty(property: String, value: String): JsonLDObject =
    updateModelAndSet(property.toStrField, buildString(value))

  def withDateOnlyProperty(property: String, value: SimpleDateTime): JsonLDObject =
    updateModelAndSet(property.toStrField, buildDate(value))
  def withDateTimeProperty(property: String, value: SimpleDateTime): JsonLDObject =
    updateModelAndSet(property.toStrField, buildDateTime(value))

  def withProperty(property: String, value: Int): JsonLDObject =
    updateModelAndSet(property.toIntField, buildInteger(value))

  def withProperty(property: String, value: Float): JsonLDObject =
    updateModelAndSet(property.toFloatField, buildFloat(value))

  def withProperty(property: String, value: Double): JsonLDObject =
    updateModelAndSet(property.toDoubleField, buildDouble(value))

  def withProperty(property: String, value: Boolean): JsonLDObject =
    updateModelAndSet(property.toBoolField, buildBoolean(value))

  def withProperty(property: String, value: JsonLDObject): JsonLDObject =
    updateModelAndSet(property.toObjField(value.meta), value)

  def withStringPropertyCollection(property: String, values: Seq[String]): JsonLDObject =
    updateModelAndSet(property.toStrListField, buildArray(values.map(buildString)))

  def withIntPropertyCollection(property: String, values: Seq[Int]): JsonLDObject =
    updateModelAndSet(property.toIntListField, buildArray(values.map(buildInteger)))

  def withFloatPropertyCollection(property: String, values: Seq[Float]): JsonLDObject =
    updateModelAndSet(property.toFloatListField, buildArray(values.map(buildFloat)))

  def withBoolPropertyCollection(property: String, values: Seq[Boolean]): JsonLDObject =
    updateModelAndSet(property.toBoolListField, buildArray(values.map(buildBoolean)))

  def withObjPropertyCollection(property: String, values: Seq[JsonLDObject]): JsonLDObject =
    updateModelAndSet(property.toObjListField, buildArray(values))

  def removeProperty(property: String): JsonLDObject = {
    // update model
    val newModelFields = model.fields.filterNot(f => f.value.iri() == property)
    model = model.copy(fields = newModelFields)

    // update fields
    fields.remove(property)

    this
  }
}
