package amf.spec.raml

import amf.domain.Annotation.{ExplicitField, Inferred}
import amf.domain.{Annotations, CreativeWork}
import amf.metadata.shape._
import amf.model.{AmfArray, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.shape.RamlTypeDefMatcher.matchType
import amf.shape.TypeDef.{ArrayType, NilType, ObjectType, UndefinedType}
import amf.shape._
import amf.spec.Declarations
import amf.spec.common.BaseSpecParser._
import org.yaml.model._

import scala.collection.mutable

case class RamlTypeParser(entry: YMapEntry, adopt: Shape => Unit, declarations: Declarations) {

  def parse(): Option[Shape] = {
    val name = entry.key.value.toScalar.text

    val ahead = entry.value.value

    detect(ahead) match {
      case ObjectType =>
        Some(parseObjectType(name, ahead, declarations))
      case ArrayType =>
        Some(parseArrayType(name, ahead))
      case typeDef if typeDef.isScalar =>
        Some(parseScalarType(name, typeDef, ahead))
      case _ => None
    }
  }

  private def detect(property: YValue): TypeDef = property match {
    case scalar: YScalar => matchType(scalar.text)
    case _: YSequence    => ObjectType
    case map: YMap =>
      detectTypeOrSchema(map)
        .orElse(detectProperties(map))
        .orElse(detectItems(map))
        .getOrElse(UndefinedType)
  }

  private def detectProperties(map: YMap): Option[TypeDef] = {
    map.key("properties").map(_ => ObjectType)
  }

  private def detectItems(map: YMap): Option[TypeDef] = {
    map.key("items").map(_ => ArrayType)
  }

  private def detectTypeOrSchema(map: YMap) = {
    map
      .key("type")
      .orElse(map.key("schema"))
      .map(e =>
        e.value.value match {
          case scalar: YScalar =>
            val t = scalar.text
            val f = map.key("(format)").map(_.value.value.toScalar.text).getOrElse("")
            matchType(t, f)
          case _: YSequence | _: YMap => ObjectType
          case _                      => UndefinedType
      })
  }

  private def parseScalarType(name: String, typeDef: TypeDef, ahead: YValue): Shape = {
    if (typeDef.isNil) {
      NilShape(entry).withName(name)
    } else {
      val shape = ScalarShape(entry).withName(name)
      adopt(shape)
      ahead match {
        case map: YMap => ScalarShapeParser(typeDef, shape, map).parse()
        case value =>
          shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef), Annotations(value)))
      }
    }
  }

  def parseArrayType(name: String, ahead: YValue): Shape = {
    val shape = ahead match {
      case map: YMap => DataArrangementParser(name, entry, map, (shape: Shape) => adopt(shape), declarations).parse()
      case _         => ArrayShape(entry).withName(name)
    }
    shape
  }

  private def parseObjectType(name: String, ahead: YValue, declarations: Declarations): Shape = {
    val shape = NodeShape(entry).withName(name)
    adopt(shape)
    ahead match {
      case map: YMap => NodeShapeParser(shape, map, declarations).parse()
      case _         => shape
    }
  }

}

case class ScalarShapeParser(typeDef: TypeDef, shape: ScalarShape, map: YMap) extends ShapeParser() {
  override def parse(): ScalarShape = {

    super.parse()

    map
      .key("type")
      .fold(
        shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), Annotations() += Inferred()))(
        entry => shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), Annotations(entry)))

    map.key("pattern", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.Pattern, value.string(), Annotations(entry))
    })

    map.key("minLength", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.MinLength, value.integer(), Annotations(entry))
    })

    map.key("maxLength", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.MaxLength, value.integer(), Annotations(entry))
    })

    map.key("minimum", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.Minimum, value.string(), Annotations(entry))
    })

    map.key("maximum", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.Maximum, value.string(), Annotations(entry))
    })

    map.key("(exclusiveMinimum)", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.ExclusiveMinimum, value.string(), Annotations(entry))
    })

    map.key("(exclusiveMaximum)", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.ExclusiveMaximum, value.string(), Annotations(entry))
    })

    map.key("format", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.Format, value.string(), Annotations(entry))
    })

    // We don't need to parse (format) extension because in oas must not be emitted, and in raml will be emitted.

    map.key("multipleOf", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.MultipleOf, value.integer(), Annotations(entry))
    })

    shape
  }
}

case class DataArrangementParser(name: String, ast: YPart, map: YMap, adopt: Shape => Unit, declarations: Declarations) {

  def lookAhead(): Either[TupleShape, ArrayShape] = {
    map.key("(tuple)") match {
      case Some(entry) =>
        entry.value.value match {
          // this is a sequence, we need to create a tuple
          case _: YSequence => Left(TupleShape(ast).withName(name))
          // not an array regular array parsing
          case _ => throw new Exception("Tuples must have a list of types")

        }
      case None => Right(ArrayShape(ast).withName(name))
    }
  }

  def parse(): Shape = {
    lookAhead() match {
      case Left(tuple)  => TupleShapeParser(tuple, map, adopt, declarations).parse()
      case Right(array) => ArrayShapeParser(array, map, adopt, declarations).parse()
    }
  }

}

case class ArrayShapeParser(override val shape: ArrayShape,
                            map: YMap,
                            adopt: Shape => Unit,
                            declarations: Declarations)
    extends ShapeParser() {

  override def parse(): Shape = {
    adopt(shape)

    super.parse()

    map.key("type", _ => shape.add(ExplicitField()))

    map.key("minItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.MinItems, value.integer(), Annotations(entry))
    })

    map.key("maxItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.MaxItems, value.integer(), Annotations(entry))
    })

    map.key("uniqueItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.UniqueItems, value.boolean(), Annotations(entry))
    })

    val finalShape = for {
      itemsEntry <- map.key("items")
      item       <- RamlTypeParser(itemsEntry, items => items.adopted(shape.id + "/items"), declarations).parse()
    } yield {
      item match {
        case array: ArrayShape   => shape.withItems(array).toMatrixShape
        case matrix: MatrixShape => shape.withItems(matrix).toMatrixShape
        case other: Shape        => shape.withItems(other)
      }
    }

    finalShape match {
      case Some(parsed: Shape) => parsed
      case None                => throw new Exception("Cannot parse data arrangement shape")
    }
  }
}

case class TupleShapeParser(shape: TupleShape, map: YMap, adopt: Shape => Unit, declarations: Declarations)
    extends ShapeParser() {

  override def parse(): Shape = {
    adopt(shape)

    super.parse()

    map.key("type", _ => shape.add(ExplicitField()))

    map.key("minItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.MinItems, value.integer(), Annotations(entry))
    })

    map.key("maxItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.MaxItems, value.integer(), Annotations(entry))
    })

    map.key("uniqueItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.UniqueItems, value.boolean(), Annotations(entry))
    })

    map.key(
      "items",
      entry => {
        val items = entry.value.value.toMap.entries.zipWithIndex
          .map {
            case (elem, index) =>
              RamlTypeParser(elem, item => item.adopted(shape.id + "/items/" + index), declarations).parse()
          }
        shape.withItems(items.filter(_.isDefined).map(_.get))
      }
    )

    shape
  }
}

case class NodeShapeParser(shape: NodeShape, map: YMap, declarations: Declarations) extends ShapeParser() {
  override def parse(): NodeShape = {

    super.parse()

    map.key("type", _ => shape.add(ExplicitField())) // todo lexical of type?? new annotation?

    map.key(
      "type",
      entry => {
        entry.value.value match {
          case scalar: YScalar if scalar.text != "object" =>
            shape.set(NodeShapeModel.Inherits,
                      AmfArray(Seq(declarations.shapes(scalar.text)), Annotations(entry.value)),
                      Annotations(entry))
          case sequence: YSequence =>
            val inherits = ArrayNode(sequence)
              .strings()
              .scalars
              .map(scalar => declarations.shapes(scalar.toString))

            shape.set(NodeShapeModel.Inherits, AmfArray(inherits, Annotations(entry.value)), Annotations(entry))
          case _: YMap =>
            RamlTypeParser(entry, shape => shape.adopted(shape.id), declarations)
              .parse()
              .foreach(s => shape.set(NodeShapeModel.Inherits, s, Annotations(entry)))
          case _ =>
            shape.add(ExplicitField()) // TODO store annotation in dataType field.
        }
      }
    )

    map.key("minProperties", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.MinProperties, value.integer(), Annotations(entry))
    })

    map.key("maxProperties", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.MaxProperties, value.integer(), Annotations(entry))
    })

    shape.set(NodeShapeModel.Closed, value = false)

    map.key("additionalProperties", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.Closed, value.negated(), Annotations(entry) += ExplicitField())
    })

    map.key("discriminator", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.Discriminator, value.string(), Annotations(entry))
    })

    map.key("discriminatorValue", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.DiscriminatorValue, value.string(), Annotations(entry))
    })

    map.key("(readOnly)", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.ReadOnly, value.boolean(), Annotations(entry))
    })

    map.key(
      "properties",
      entry => {
        val properties: Seq[PropertyShape] =
          PropertiesParser(entry.value.value.toMap, shape.withProperty, declarations).parse()
        shape.set(NodeShapeModel.Properties, AmfArray(properties, Annotations(entry.value)), Annotations(entry))
      }
    )

    val properties = mutable.ListMap[String, PropertyShape]()
    shape.properties.foreach(p => properties += (p.name -> p))

    map.key(
      "(dependencies)",
      entry => {
        val dependencies: Seq[PropertyDependencies] =
          ShapeDependenciesParser(entry.value.value.toMap, properties).parse()
        shape.set(NodeShapeModel.Dependencies, AmfArray(dependencies, Annotations(entry.value)), Annotations(entry))
      }
    )

    shape
  }
}

case class PropertiesParser(ast: YMap, producer: String => PropertyShape, declarations: Declarations) {

  def parse(): Seq[PropertyShape] = {
    ast.entries
      .map(entry => PropertyShapeParser(entry, producer, declarations).parse())
  }
}

case class PropertyShapeParser(entry: YMapEntry, producer: String => PropertyShape, declarations: Declarations) {

  def parse(): PropertyShape = {

    val name     = entry.key.value.toScalar.text
    val property = producer(name).add(Annotations(entry))

    entry.value.value match {
      case map: YMap =>
        map.key(
          "required",
          entry => {
            val required = ValueNode(entry.value).boolean().value.asInstanceOf[Boolean]
            property.set(PropertyShapeModel.MinCount,
                         AmfScalar(if (required) 1 else 0),
                         Annotations(entry) += ExplicitField())
          }
        )
      case _ =>
    }

    if (property.fields.entry(PropertyShapeModel.MinCount).isEmpty) {
      val required = !name.endsWith("?")

      property.set(PropertyShapeModel.MinCount, if (required) 1 else 0)
      property.set(PropertyShapeModel.Name, if (required) name else name.stripSuffix("?")) // TODO property id is using a name that is not final.
    }

    // todo path

    RamlTypeParser(entry, shape => shape.adopted(property.id), declarations)
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
  val map: YMap

  def parse(): Shape = {

    map.key("displayName", entry => {
      val value = ValueNode(entry.value)
      shape.set(ShapeModel.DisplayName, value.string(), Annotations(entry))
    })

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      shape.set(ShapeModel.Description, value.string(), Annotations(entry))
    })

    map.key("default", entry => {
      val value = ValueNode(entry.value)
      shape.set(ShapeModel.Default, value.string(), Annotations(entry))
    })

    map.key("enum", entry => {
      val value = ArrayNode(entry.value.value.toSequence)
      shape.set(ShapeModel.Values, value.strings(), Annotations(entry))
    })

    map.key(
      "(externalDocs)",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value.value.toMap).parse()
        shape.set(ShapeModel.Documentation, creativeWork, Annotations(entry))
      }
    )

    map.key(
      "xml",
      entry => {
        val xmlSerializer: XMLSerializer = XMLSerializerParser(shape.name, entry.value.value.toMap).parse()
        shape.set(ShapeModel.XMLSerialization, xmlSerializer, Annotations(entry))
      }
    )

    shape
  }
}
