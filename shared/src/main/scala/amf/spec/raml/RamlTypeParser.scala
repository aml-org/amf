package amf.spec.raml

import amf.common.core.Strings
import amf.common.{AMFAST, AMFToken}
import amf.domain.Annotation.{ExplicitField, Inferred}
import amf.domain.{Annotations, CreativeWork}
import amf.metadata.shape._
import amf.model.{AmfArray, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.shape.RamlTypeDefMatcher.matchType
import amf.shape.TypeDef.{ArrayType, ObjectType, UndefinedType}
import amf.shape._
import org.yaml.model.{YMap, YMapEntry, YScalar, YSequence}

import scala.collection.mutable

case class RamlTypeParser(entry: YMapEntry, adopt: Shape => Unit) {

  def parse(): Option[Shape] = {
    val name = entry.key.value.scalar.text

    val ahead = lookAhead()

    detect(ahead) match {
      case ObjectType =>
        Some(parseObjectType(name, ahead))
      case ArrayType =>
        Some(parseArrayType(name, ahead))
      case typeDef if typeDef.isScalar =>
        Some(parseScalarType(name, typeDef, ahead))
      case _ => None
    }
  }

  private def detect(property: Either[YScalar, YMap]): TypeDef = property match {
    case Left(scalar) => matchType(scalar.text.unquote)
    case Right(map) =>
      detectTypeOrSchema(map)
        .orElse(detectProperties(map))
        .orElse(detectItems(map))
        .getOrElse(UndefinedType)
  }

  private def detectProperties(map: YMap) = {
    map.key("properties").map(_ => ObjectType)
  }

  private def detectItems(map: YMap) = {
    map.key("items").map(_ => ArrayType)
  }

  private def detectTypeOrSchema(map: YMap) = {
    map
      .key("type")
      .orElse(map.key("schema"))
      .map(e => {
        val t = e.value.value.scalar.text.unquote
        val f = map.key("(format)").map(_.value.value.scalar.text.unquote).getOrElse("")
        matchType(t, f)
      })
  }

  private def parseScalarType(name: String, typeDef: TypeDef, ahead: Either[YScalar, YMap]): Shape = {
    val shape = ScalarShape(entry).withName(name)
    adopt(shape)
    ahead match {
      case Left(scalar) =>
        shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef), Annotations(scalar)))
      case Right(map) => ScalarShapeParser(typeDef, shape, map).parse()
    }
  }

  def parseArrayType(name: String, ahead: Either[YScalar, YMap]): Shape = {
    val shape = ahead match {
      case Right(map) => DataArrangementParser(name, entry, map, (shape: Shape) => adopt(shape)).parse()
      case _          => ArrayShape(entry).withName(name)
    }
    shape
  }

  private def parseObjectType(name: String, ahead: Either[YScalar, YMap]): Shape = {
    val shape = NodeShape(entry).withName(name)
    adopt(shape)
    ahead match {
      case Right(map) => NodeShapeParser(shape, map).parse()
      case Left(_)    => shape
    }
  }

  private def lookAhead(): Either[YScalar, YMap] = {
    val value = entry.value.value
    value match {
      case scalar: YScalar => Left(scalar)
      case map: YMap       => Right(map)
      case _               => throw new RuntimeException(s"Expected map or scalar but found: $value")
    }
  }
}

case class RamlTypesParser(ast: Option[YMap], adopt: Shape => Unit) {
  def parse(): Seq[Shape] = ast match {
    case Some(map) => map.entries.flatMap(entry => RamlTypeParser(entry, adopt).parse())
    case None      => Seq()
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

    //We don't need to parse (format) extention because in oas must not be emitted, and in raml will be emitted.

    map.key("multipleOf", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.MultipleOf, value.integer(), Annotations(entry))
    })

    shape
  }
}

case class DataArrangementParser(name: String, entry: YMapEntry, map: YMap, adopt: Shape => Unit) {

  def lookAhead(): Either[TupleShape, ArrayShape] = {
    map.key("(tuple)") match {
      case Some(tuplesEntry) =>
        tuplesEntry.value.value match {
          // this is a sequence, we need to create a tuple
          case _ : YSequence => Left(TupleShape(entry).withName(name))
          // not an array regular array parsing
          case _ => throw new Exception("Tuples must have a list of types")

        }
      case None => Right(ArrayShape(entry).withName(name))
    }
  }

  def parse(): Shape = {
    lookAhead() match {
      case Left(tuple)  => TupleShapeParser(tuple, map, adopt).parse()
      case Right(array) => ArrayShapeParser(array, map, adopt).parse()
    }
  }

}

case class ArrayShapeParser(override val shape: ArrayShape, map: YMap, adopt: Shape => Unit) extends ShapeParser() {

  override def parse(): Shape = {
    adopt(shape)

    super.parse()

    map.key("type", entry => shape.add(ExplicitField()))

    map.key("minItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.MinItems, value.integer(), entry.annotations())
    })

    map.key("maxItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.MaxItems, value.integer(), entry.annotations())
    })

    map.key("uniqueItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.UniqueItems, value.boolean(), entry.annotations())
    })

    val finalShape = for {
      itemsEntry <- map.key("items")
      item       <- RamlTypeParser(itemsEntry, items => items.adopted(shape.id + "/items")).parse()
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

case class TupleShapeParser(shape: TupleShape, map: YMap, adopt: Shape => Unit) extends ShapeParser() {

  override def parse(): Shape = {
    adopt(shape)

    super.parse()

    map.key("type", entry => shape.add(ExplicitField()))

    map.key("minItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.MinItems, value.integer(), entry.annotations())
    })

    map.key("maxItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.MaxItems, value.integer(), entry.annotations())
    })

    map.key("uniqueItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.UniqueItems, value.boolean(), entry.annotations())
    })

    map.key(
      "items",
      entry => {
        val items = Entries(entry.ast).entries.values.zipWithIndex
          .map(entry => RamlTypeParser(entry._1, items => items.adopted(shape.id + "/items/" + entry._2)).parse())
          .toSeq
        shape.withItems(items.filter(_.isDefined).map(_.get))
      }
    )

    shape
  }
}

case class NodeShapeParser(shape: NodeShape, map: YMap) extends ShapeParser() {
  override def parse(): NodeShape = {

    super.parse()

    map.key("type", entry => shape.add(ExplicitField())) // todo lexical of type?? new annotation?

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
          PropertiesParser(entry.value, shape.withProperty).parse()
        shape.set(NodeShapeModel.Properties, AmfArray(properties, Annotations(entry.value)), Annotations(entry))
      }
    )

    val properties = mutable.ListMap[String, PropertyShape]()
    shape.properties.foreach(p => properties += (p.name -> p))

    map.key(
      "(dependencies)",
      entry => {
        val dependencies: Seq[PropertyDependencies] =
          ShapeDependenciesParser(entry.value, properties).parse()
        shape.set(NodeShapeModel.Dependencies, AmfArray(dependencies, Annotations(entry.value)), Annotations(entry))
      }
    )

    shape
  }
}

case class ShapeDependenciesParser(ast: AMFAST, properties: mutable.ListMap[String, PropertyShape]) {
  def parse(): Seq[PropertyDependencies] = {
    Entries(ast).entries.values
      .flatMap(entry => NodeDependencyParser(entry, properties).parse())
      .toSeq
  }
}

case class NodeDependencyParser(entry: EntryNode, properties: mutable.ListMap[String, PropertyShape]) {
  def parse(): Option[PropertyDependencies] = {

    properties
      .get(entry.key.content.unquote)
      .map(p => {
        val targets = buildTargets()
        PropertyDependencies(entry.ast)
          .set(PropertyDependenciesModel.PropertySource, AmfScalar(p.id), entry.annotations())
          .set(PropertyDependenciesModel.PropertyTarget, AmfArray(targets), Annotations(entry.value))
      })
  }

  private def buildTargets(): Seq[AmfScalar] = {
    ArrayNode(entry.value)
      .strings()
      .scalars
      .flatMap(
        v =>
          properties
            .get(v.value.toString)
            .map(p => AmfScalar(p.id, v.annotations)))
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

case class XMLSerializerParser(defaultName: String, ast: AMFAST) {
  def parse(): XMLSerializer = {
    val xmlSerializer = XMLSerializer(ast)
      .set(XMLSerializerModel.Attribute, value = false)
      .set(XMLSerializerModel.Wrapped, value = false)
      .set(XMLSerializerModel.Name, defaultName)
    val entries = Entries(ast)

    entries.key(
      "attribute",
      entry => {
        val value = ValueNode(entry.value)
        xmlSerializer.set(XMLSerializerModel.Attribute, value.boolean(), entry.annotations() += ExplicitField())
      }
    )

    entries.key("wrapped", entry => {
      val value = ValueNode(entry.value)
      xmlSerializer.set(XMLSerializerModel.Wrapped, value.boolean(), entry.annotations() += ExplicitField())
    })

    entries.key("name", entry => {
      val value = ValueNode(entry.value)
      xmlSerializer.set(XMLSerializerModel.Name, value.string(), entry.annotations() += ExplicitField())
    })

    entries.key("namespace", entry => {
      val value = ValueNode(entry.value)
      xmlSerializer.set(XMLSerializerModel.Namespace, value.string(), entry.annotations())
    })

    entries.key("prefix", entry => {
      val value = ValueNode(entry.value)
      xmlSerializer.set(XMLSerializerModel.Prefix, value.string(), entry.annotations())
    })

    xmlSerializer
  }
}

abstract class ShapeParser() {

  val shape: Shape
  val map: YMap

  def parse(): Shape = {

    map.key("displayName", entry => {
      val value = ValueNode(entry.value)
      shape.set(ShapeModel.DisplayName, value.string(), entry.annotations())
    })

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      shape.set(ShapeModel.Description, value.string(), entry.annotations())
    })

    map.key("default", entry => {
      val value = ValueNode(entry.value)
      shape.set(ShapeModel.Default, value.string(), entry.annotations())
    })

    map.key("enum", entry => {
      val value = ArrayNode(entry.value)
      shape.set(ShapeModel.Values, value.strings(), entry.annotations())
    })

    map.key(
      "(externalDocs)",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value).parse()
        shape.set(ShapeModel.Documentation, creativeWork, entry.annotations())
      }
    )

    map.key(
      "xml",
      entry => {
        val xmlSerializer: XMLSerializer = XMLSerializerParser(shape.name, entry.value).parse()
        shape.set(ShapeModel.XMLSerialization, xmlSerializer, entry.annotations())
      }
    )

    shape
  }
}
