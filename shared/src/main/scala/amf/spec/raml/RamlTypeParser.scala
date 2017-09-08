package amf.spec.raml

import amf.common.AMFAST
import amf.common.AMFToken.{MapToken, StringToken}
import amf.common.Strings._
import amf.domain.Annotation.{ExplicitField, Inferred}
import amf.domain.{Annotations, CreativeWork}
import amf.metadata.shape.{NodeShapeModel, PropertyShapeModel, ScalarShapeModel, ShapeModel}
import amf.model.{AmfArray, AmfScalar}
import amf.shape.RamlTypeDefMatcher.matchType
import amf.shape.TypeDef.{ObjectType, UndefinedType}
import amf.shape._

case class RamlTypeParser(entry: EntryNode, adopt: Shape => Unit) {
  def parse(): Option[Shape] = {
    val name = entry.key.content.unquote

    val ahead = lookAhead()

    detect(ahead) match {
      case ObjectType =>
        Some(parseObjectType(name, ahead))
      case typeDef if typeDef.isScalar =>
        Some(parseScalarType(name, typeDef, ahead))
      case _ => None
    }
  }

  private def detect(property: Either[AMFAST, Entries]): TypeDef = property match {
    case Left(node) => matchType(node.content.unquote)
    case Right(entries) =>
      detectTypeOrSchema(entries)
        .orElse(detectProperties(entries))
        .getOrElse(UndefinedType)
  }

  private def detectProperties(entries: Entries) = {
    entries.key("properties").map(_ => ObjectType)
  }

  private def detectTypeOrSchema(entries: Entries) = {
    entries
      .key("type")
      .orElse(entries.key("schema"))
      .map(e => {
        val t = e.value.content.unquote
        val f = entries.key("(format)").map(_.value.content.unquote).getOrElse("")
        matchType(t, f)
      })
  }

  private def parseScalarType(name: String, typeDef: TypeDef, ahead: Either[AMFAST, Entries]): Shape = {
    val shape = ScalarShape(entry.ast).withName(name)
    adopt(shape)
    ahead match {
      case Left(node) =>
        shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef), Annotations(node)))
      case Right(entries) => ScalarShapeParser(typeDef, shape, entries).parse()
    }
  }

  private def parseObjectType(name: String, ahead: Either[AMFAST, Entries]): Shape = {
    val shape = NodeShape(entry.ast).withName(name)
    adopt(shape)
    ahead match {
      case Right(entries) => NodeShapeParser(shape, entries).parse()
      case Left(_)        => shape
    }
  }

  def lookAhead(): Either[AMFAST, Entries] = {
    entry.value.`type` match {
      case StringToken => Left(entry.value)
      case MapToken    => Right(Entries(entry.value))
      case _           => throw new RuntimeException("no value detected in look a head")
    }
  }
}

case class RamlTypesParser(ast: AMFAST, adopt: Shape => Unit) {
  def parse(): Seq[Shape] = {
    Entries(ast).entries.values
      .flatMap(entry => RamlTypeParser(entry, adopt).parse())
      .toSeq
  }
}

case class ScalarShapeParser(typeDef: TypeDef, shape: ScalarShape, entries: Entries) extends ShapeParser() {
  override def parse(): ScalarShape = {

    super.parse()

    entries
      .key("type")
      .fold(
        shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), Annotations() += Inferred()))(
        entry => shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), entry.annotations()))

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

    //We don't need to parse (format) extention because in oas must not be emitted, and in raml will be emitted.

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

    entries.key("type", entry => shape.add(ExplicitField())) // todo lexical of type?? new annotation?

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

    entries.key("discriminatorValue", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.DiscriminatorValue, value.string(), entry.annotations())
    })

    entries.key("(readOnly)", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.ReadOnly, value.boolean(), entry.annotations())
    })

    entries.key(
      "properties",
      entry => {
        val properties: Seq[PropertyShape] =
          PropertiesParser(entry.value, shape.withProperty).parse()
        shape.set(NodeShapeModel.Properties, AmfArray(properties, Annotations(entry.value)), entry.annotations())
      }
    )

    shape
  }
}

case class PropertiesParser(ast: AMFAST, producer: String => PropertyShape) {

  def parse(): Seq[PropertyShape] = {
    Entries(ast).entries.values
      .map(entry => PropertyShapeParser(entry, producer).parse())
      .toSeq
  }
}

case class PropertyShapeParser(entry: EntryNode, producer: String => PropertyShape) {

  def parse(): PropertyShape = {

    val name     = entry.key.content.unquote
    val property = producer(name).add(Annotations(entry.ast))
    val entries  = Entries(entry.value)

    entries.key(
      "required",
      entry => {
        val required = ValueNode(entry.value).boolean().value.asInstanceOf[Boolean]
        property.set(PropertyShapeModel.MinCount,
                     AmfScalar(if (required) 1 else 0),
                     entry.annotations() += ExplicitField())
      }
    )

    if (property.fields.entry(PropertyShapeModel.MinCount).isEmpty) {
      val required = !name.endsWith("?")

      property.set(PropertyShapeModel.MinCount, if (required) 1 else 0)
      property.set(PropertyShapeModel.Name, if (required) name else name.stripSuffix("?")) // TODO property id is using a name that is not final.
    }

    // todo path

    RamlTypeParser(entry, shape => shape.adopted(property.id))
      .parse()
      .foreach(range => property.set(PropertyShapeModel.Name, range.name).set(PropertyShapeModel.Range, range))

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

    entries.key("displayName", entry => {
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
      "(externalDocs)",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value).parse()
        shape.set(ShapeModel.Documentation, creativeWork, entry.annotations())
      }
    )

    shape
  }
}
