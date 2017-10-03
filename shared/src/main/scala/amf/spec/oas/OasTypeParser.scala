package amf.spec.oas

import amf.domain.Annotation.{ExplicitField, Inferred}
import amf.domain.{Annotations, CreativeWork}
import amf.metadata.shape._
import amf.model.{AmfArray, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.shape.OasTypeDefMatcher.matchType
import amf.shape.TypeDef._
import amf.shape._
import amf.spec.Declarations
import amf.spec.common.BaseSpecParser._
import org.yaml.model.{YMap, YMapEntry, YNode, YPart, YScalar, YSequence}

import scala.collection.mutable

/**
  * OpenAPI Type Parser.
  */
case class OasTypeParser(ast: YPart, name: String, map: YMap, adopt: Shape => Unit, declarations: Declarations) {
  def parse(): Option[Shape] = {

    detect() match {
      case UnionType =>
        Some(parseUnionType())
      case ObjectType =>
        Some(parseObjectType())
      case AnyType =>
        Some(parseAnyType())
      case ArrayType =>
        Some(parseArrayType())
      case typeDef if typeDef.isScalar =>
        Some(parseScalarType(typeDef))
      case _ => None
    }
  }

  private def detect(): TypeDef =
    detectType()
      .orElse(detectProperties())
      .orElse(detectAnyOf())
      .getOrElse(if (map.entries.isEmpty) AnyType else UndefinedType)

  private def detectProperties(): Option[TypeDef.ObjectType.type] = {
    map.key("properties").orElse(map.key("allOf")).map(_ => ObjectType)
  }

  private def detectAnyOf(): Option[TypeDef.UnionType.type] = {
    map.key("anyOf").map(_ => UnionType)
  }

  private def detectType(): Option[TypeDef] = {
    map
      .key("type")
      .map(e => {
        val t = e.value.value.toScalar.text
        val f = map.key("format").map(_.value.value.toScalar.text).getOrElse("")
        matchType(t, f)
      })
  }

  private def parseScalarType(typeDef: TypeDef): Shape = {
    val parsed = typeDef match {
      case NilType => NilShape(ast).withName(name)
      case FileType =>
        val shape = FileShape(ast).withName(name)
        FileShapeParser(typeDef, shape, map).parse()
      case _        =>
        val shape = ScalarShape(ast).withName(name)
        ScalarShapeParser(typeDef, shape, map).parse()
    }
    adopt(parsed)
    parsed
  }

  private def parseAnyType(): Shape = {
    val shape = AnyShape(ast).withName(name)
    adopt(shape)
    shape
  }

  private def parseArrayType(): Shape = {
    DataArrangementParser(name, ast, map, (shape: Shape) => adopt(shape), declarations).parse()
  }

  private def parseObjectType(): Shape = {
    val shape = NodeShape(ast).withName(name)
    adopt(shape)
    NodeShapeParser(shape, map, declarations).parse()
  }

  private def parseUnionType(): Shape = {
    UnionShapeParser(map, name, declarations).parse()
  }
}

object OasTypeParser {
  def apply(entry: YMapEntry, adopt: Shape => Unit, declarations: Declarations): OasTypeParser =
    OasTypeParser(entry, entry.key.value.toScalar.text, entry.value.value.toMap, adopt, declarations)
}

trait CommonScalarParsingLogic {
  def parseScalar(map: YMap, shape: Shape): Unit = {
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

    map.key("exclusiveMinimum", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.ExclusiveMinimum, value.string(), Annotations(entry))
    })

    map.key("exclusiveMaximum", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.ExclusiveMaximum, value.string(), Annotations(entry))
    })

    map.key("format", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.Format, value.string(), Annotations(entry))
    })

    map.key("multipleOf", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.MultipleOf, value.integer(), Annotations(entry))
    })

  }
}
case class ScalarShapeParser(typeDef: TypeDef, shape: ScalarShape, map: YMap) extends ShapeParser() with CommonScalarParsingLogic {
  override def parse(): ScalarShape = {
    super.parse()
    map
      .key("type")
      .fold(
        shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), Annotations() += Inferred()))(
        entry => shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), Annotations(entry)))

    parseScalar(map, shape)
    shape
  }
}

case class UnionShapeParser(override val map: YMap, name: String, declarations: Declarations) extends ShapeParser() {
  override val shape = UnionShape(Annotations(map)).withName(name)

  override def parse(): UnionShape = {
    super.parse()

    map.key("anyOf", { entry =>
      entry.value.value match {
        case seq: YSequence =>
          val unionNodes = seq.nodes.zipWithIndex.map { case (node, index) =>
            val entry = YMapEntry(YNode(YScalar(s"item$index", true, node.range)), node)
            OasTypeParser(entry, item => item.adopted(shape.id + "/items/" + index), declarations).parse()
          }.filter(_.isDefined)
            .map(_.get)
          shape.setArray(UnionShapeModel.AnyOf, unionNodes, Annotations(seq))
        case _ => throw new Exception("Unions are built from multiple shape nodes")
      }
    })

    shape
  }
}


case class DataArrangementParser(name: String, ast: YPart, map: YMap, adopt: Shape => Unit, declarations: Declarations) {

  def lookAhead(): Option[Either[TupleShape, ArrayShape]] = {
    map.key("items") match {
      case Some(entry) =>
        entry.value.value match {
          // this is a sequence, we need to create a tuple
          case _: YSequence => Some(Left(TupleShape(ast).withName(name)))
          // not an array regular array parsing
          case _ => Some(Right(ArrayShape(ast).withName(name)))

        }
      case None => None
    }
  }

  def parse(): Shape = {
    lookAhead() match {
      case None               => throw new Exception("Cannot parse data arrangement shape")
      case Some(Left(tuple))  => TupleShapeParser(tuple, map, adopt, declarations).parse()
      case Some(Right(array)) => ArrayShapeParser(array, map, adopt, declarations).parse()
    }
  }

}

case class TupleShapeParser(shape: TupleShape, map: YMap, adopt: Shape => Unit, declarations: Declarations)
    extends ShapeParser() {

  override def parse(): Shape = {
    adopt(shape)

    super.parse()

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
              OasTypeParser(elem, item => item.adopted(item.id + "/items/" + index), declarations).parse()
          }
        shape.withItems(items.filter(_.isDefined).map(_.get))
      }
    )

    shape
  }
}

case class ArrayShapeParser(shape: ArrayShape, map: YMap, adopt: Shape => Unit, declarations: Declarations)
    extends ShapeParser() {
  override def parse(): Shape = {
    adopt(shape)

    super.parse()

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
      entry <- map.key("items")
      item  <- OasTypeParser(entry, items => items.adopted(shape.id + "/items"), declarations).parse()
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

case class NodeShapeParser(shape: NodeShape, map: YMap, declarations: Declarations) extends ShapeParser() {
  override def parse(): NodeShape = {

    super.parse()

    map.key("type", _ => shape.add(ExplicitField())) // todo lexical of type?? new annotation?

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

    map.key("x-discriminator-value", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.DiscriminatorValue, value.string(), Annotations(entry))
    })

    map.key("readOnly", entry => {
      val value = ValueNode(entry.value)
      shape.set(NodeShapeModel.ReadOnly, value.boolean(), Annotations(entry))
    })

    var requiredFields = Seq[String]()

    map
      .key("required")
      .filter(_.value.value.isInstanceOf[YSequence])
      .foreach(entry => {
        val value = ArrayNode(entry.value.value.toSequence)
        requiredFields = value.strings().scalars.map(_.value.toString)
      })

    map.key(
      "properties",
      entry => {
        val properties: Seq[PropertyShape] =
          PropertiesParser(entry.value.value.toMap, shape.withProperty, requiredFields, declarations).parse()
        shape.set(NodeShapeModel.Properties, AmfArray(properties, Annotations(entry.value)), Annotations(entry))
      }
    )

    val properties = mutable.ListMap[String, PropertyShape]()
    shape.properties.foreach(p => properties += (p.name -> p))

    map.key(
      "dependencies",
      entry => {
        val dependencies: Seq[PropertyDependencies] =
          ShapeDependenciesParser(entry.value.value.toMap, properties).parse()
        shape.set(NodeShapeModel.Dependencies, AmfArray(dependencies, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key(
      "allOf",
      entry => {
        val inherits = AllOfParser(entry.value.value.toSequence, declarations, s => s.adopted(shape.id)).parse()

        shape.set(NodeShapeModel.Inherits, AmfArray(inherits, Annotations(entry.value)), Annotations(entry))
      }
    )

    shape
  }
}

case class AllOfParser(array: YSequence, declarations: Declarations, adopt: Shape => Unit) {
  def parse(): Seq[Shape] =
    array.values.flatMap(map =>
      declarationsRef(map.toMap).orElse(OasTypeParser(map, "", map.toMap, adopt, declarations).parse()))

  private def declarationsRef(entries: YMap): Option[Shape] = {
    entries
      .key("$ref")
      .map(entry => declarations.shapes(entry.value.value.toScalar.text.stripPrefix("#/definitions/")))
  }
}

case class PropertiesParser(map: YMap,
                            producer: String => PropertyShape,
                            requiredFields: Seq[String],
                            declarations: Declarations) {
  def parse(): Seq[PropertyShape] = {
    map.entries.map(entry => PropertyShapeParser(entry, producer, requiredFields, declarations).parse())
  }
}

case class PropertyShapeParser(entry: YMapEntry,
                               producer: String => PropertyShape,
                               requiredFields: Seq[String],
                               declarations: Declarations) {

  def parse(): PropertyShape = {

    val name     = entry.key.value.toScalar.text
    val required = requiredFields.contains(name)

    val property = producer(name)
      .add(Annotations(entry))
      .set(PropertyShapeModel.MinCount, AmfScalar(if (required) 1 else 0), Annotations() += ExplicitField())

    // todo path

    OasTypeParser(entry, shape => shape.adopted(property.id), declarations)
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
  val map: YMap

  def parse(): Shape = {

    map.key("title", entry => {
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
      "externalDocs",
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

case class FileShapeParser(typeDef: TypeDef, shape: FileShape, map: YMap) extends ShapeParser() with CommonScalarParsingLogic {
  override def parse(): Shape = {
    super.parse()

    parseScalar(map, shape)

    map.key("x-fileTypes", {
      entry => entry.value.value match {
        case seq: YSequence =>
          val value = ArrayNode(seq)
          shape.set(FileShapeModel.FileTypes, value.strings(), Annotations(seq))
      }
    })

    shape
  }
}
