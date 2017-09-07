package amf.spec.oas

import amf.common.AMFAST
import amf.common.Strings._
import amf.domain.Annotation.ExplicitField
import amf.domain.{Annotations, CreativeWork}
import amf.metadata.shape.{NodeShapeModel, PropertyShapeModel, ScalarShapeModel, ShapeModel}
import amf.model.{AmfArray, AmfScalar}
import amf.shape.OasTypeDefMatcher.matchType
import amf.shape.TypeDef.{ObjectType, UndefinedType}
import amf.shape._

case class OasTypeParser(entry: KeyValueNode, adopt: Shape => Unit) {
  def parse(): Option[Shape] = {
    val name = entry.key.content.unquote

    // todo required (name, etc)
    // todo path

    val entries = Entries(entry.value)

    detect(entries) match {
      case ObjectType =>
        Some(parseObjectType(name, entries))
      case typeDef if typeDef.isScalar =>
        Some(parseScalarType(name, typeDef, entries))
      case _ => None
    }
  }

  private def detect(entries: Entries): TypeDef =
    detectType(entries)
      .orElse(detectProperties(entries))
      .getOrElse(if (entries.entries.isEmpty) ObjectType else UndefinedType)

  private def detectProperties(entries: Entries): Option[TypeDef.ObjectType.type] = {
    entries.key("properties").map(_ => ObjectType)
  }

  private def detectType(entries: Entries): Option[TypeDef] = {
    entries
      .key("type")
      .map(e => {
        val t = e.value.content.unquote
        val f = entries.key("format").map(_.value.content.unquote).getOrElse("")
        matchType(t, f)
      })
  }

  private def parseScalarType(name: String, typeDef: TypeDef, entries: Entries): Shape = {
    val shape = ScalarShape(entry.ast).withName(name)
    adopt(shape)
    ScalarShapeParser(typeDef, shape, entries).parse()
  }

  private def parseObjectType(name: String, entries: Entries): Shape = {
    val shape = NodeShape(entry.ast).withName(name)
    adopt(shape)
    NodeShapeParser(shape, entries).parse()
  }
}

case class OasTypesParser(ast: AMFAST, adopt: Shape => Unit) {
  def parse(): Seq[Shape] = {
    Entries(ast).entries.values
      .flatMap(entry => OasTypeParser(entry, adopt).parse())
      .toSeq
  }
}

case class ScalarShapeParser(typeDef: TypeDef, shape: ScalarShape, entries: Entries) extends ShapeParser() {
  override def parse(): ScalarShape = {

    super.parse()

    shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef))) // todo annotations (TypeDefNode?)

    entries.key("pattern", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.Pattern, value.string(), entry.annotations())
    })

    entries.key("minLength", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.MinLength, value.integer(), entry.annotations())
    })

    entries.key("maxLength", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.MaxLength, value.integer(), entry.annotations())
    })

    entries.key("minimum", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.Minimum, value.string(), entry.annotations())
    })

    entries.key("maximum", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.Maximum, value.string(), entry.annotations())
    })

    entries.key("(exclusiveMinimum)", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.ExclusiveMinimum, value.string(), entry.annotations())
    })

    entries.key("(exclusiveMaximum)", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.ExclusiveMaximum, value.string(), entry.annotations())
    })

    entries.key("format", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.Format, value.string(), entry.annotations())
    })

    entries.key("multipleOf", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.MultipleOf, value.integer(), entry.annotations())
    })

    shape
  }
}

case class NodeShapeParser(shape: NodeShape, entries: Entries) extends ShapeParser() {
  override def parse(): NodeShape = {

    super.parse()

    entries.key("minProperties", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.MinProperties, value.integer(), entry.annotations())
    })

    entries.key("maxProperties", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.MaxProperties, value.integer(), entry.annotations())
    })

    shape.set(NodeShapeModel.Closed, value = false)

    entries.key("additionalProperties", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.Closed, value.negated(), entry.annotations() += ExplicitField())
    })

    entries.key("discriminator", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.Discriminator, value.string(), entry.annotations())
    })

    entries.key("x-discriminator-value", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.DiscriminatorValue, value.string(), entry.annotations())
    })

    entries.key("readOnly", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.ReadOnly, value.boolean(), entry.annotations())
    })

    var requiredFields = Seq[String]()

    entries.key("required", entry => {
      val value = ArrayNode(entry.value)
      requiredFields = value.strings().values.map(_.asInstanceOf[AmfScalar].value.toString)
    })

    entries.key(
      "properties",
      entry => {
        val properties: Seq[PropertyShape] =
          PropertiesParser(entry.value, shape.withProperty, requiredFields).parse()
        shape.set(NodeShapeModel.Properties, AmfArray(properties, Annotations(entry.value)), entry.annotations())
      }
    )

    shape
  }
}

case class PropertiesParser(ast: AMFAST, producer: String => PropertyShape, requiredFields: Seq[String]) {

  def parse(): Seq[PropertyShape] = {
    Entries(ast).entries.values
      .map(entry => PropertyShapeParser(entry, producer, requiredFields).parse())
      .toSeq
  }
}

case class PropertyShapeParser(entry: EntryNode, producer: String => PropertyShape, requiredFields: Seq[String]) {

  def parse(): PropertyShape = {

    val name     = entry.key.content.unquote
    val required = requiredFields.contains(name)

    val property = producer(name)
      .add(Annotations(entry.ast))
      .set(PropertyShapeModel.MinCount, if (required) 1 else 0)

    // todo path

    OasTypeParser(entry, shape => shape.adopted(property.id))
      .parse()
      .foreach(property.set(PropertyShapeModel.Range, _))

    property
  }
}

case class Property(var typeDef: TypeDef = UndefinedType) {
  def withTypeDef(value: TypeDef): Unit = typeDef = value
}

abstract class ShapeParser() {

  val shape: Shape
  val entries: Entries

  def parse(): Shape = {

    entries.key("title", entry => {
      val value = ValueNode(entry.value)
      shape.set(ShapeModel.DisplayName, value.string(), entry.annotations())
    })

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      shape.set(ShapeModel.Description, value.string(), entry.annotations())
    })

    entries.key("default", entry => {
      val value = ValueNode(entry.value)
      shape.set(ShapeModel.Default, value.string(), entry.annotations())
    })

    entries.key("enum", entry => {
      val value = ArrayNode(entry.value)
      shape.set(ShapeModel.Values, value.strings(), entry.annotations())
    })

    entries.key(
      "externalDocs",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value).parse()
        shape.set(ShapeModel.Documentation, creativeWork, entry.annotations())
      }
    )

    shape
  }
}
