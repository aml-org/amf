package amf.spec.oas

import amf.common.AMFToken.SequenceToken
import amf.common.core.Strings
import amf.common.{AMFAST, AMFToken}
import amf.domain.Annotation.{ExplicitField, Inferred}
import amf.domain.{Annotations, CreativeWork}
import amf.metadata.shape._
import amf.model.{AmfArray, AmfScalar}
import amf.shape.OasTypeDefMatcher.matchType
import amf.shape.TypeDef.{ArrayType, ObjectType, UndefinedType}
import amf.shape._
import amf.spec.Declarations

import scala.collection.mutable

/**
  * OpenAPI Type Parser.
  */
case class OasTypeParser(entry: KeyValueNode, adopt: Shape => Unit, declarations: Declarations) {
  def parse(): Option[Shape] = {
    val name = entry.key.content.unquote

    val entries = Entries(entry.value)

    detect(entries) match {
      case ObjectType =>
        Some(parseObjectType(name, entries))
      case ArrayType =>
        Some(parseArrayType(name, entries))
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
    entries.key("properties").orElse(entries.key("allOf")).map(_ => ObjectType)
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

  private def parseArrayType(name: String, entries: Entries): Shape = {
    DataArrangementParser(name, entry, entries, (shape: Shape) => adopt(shape), declarations).parse()
  }

  private def parseObjectType(name: String, entries: Entries): Shape = {
    val shape = NodeShape(entry.ast).withName(name)
    adopt(shape)
    NodeShapeParser(shape, entries, declarations).parse()
  }
}

case class OasTypesParser(ast: AMFAST, adopt: Shape => Unit, declarations: Declarations) {
  def parse(): Seq[Shape] = {
    Entries(ast).entries.values
      .flatMap(entry => OasTypeParser(entry, adopt, declarations).parse())
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

    entries.key("exclusiveMinimum", entry => {
      val value = ValueNode(entry.value)
      shape.set(ScalarShapeModel.ExclusiveMinimum, value.string(), entry.annotations())
    })

    entries.key("exclusiveMaximum", entry => {
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

case class DataArrangementParser(name: String,
                                 entry: KeyValueNode,
                                 entries: Entries,
                                 adopt: Shape => Unit,
                                 declarations: Declarations) {

  def lookAhead(): Option[Either[TupleShape, ArrayShape]] = {
    entries.key("items") match {
      case Some(itemsEntry) =>
        itemsEntry.ast.`type` match {
          // this is a sequence, we need to create a tuple
          case AMFToken.SequenceToken => Some(Left(TupleShape(entry.ast).withName(name)))
          // not an array regular array parsing
          case _ => Some(Right(ArrayShape(entry.ast).withName(name)))

        }
      case None => None
    }
  }

  def parse(): Shape = {
    lookAhead() match {
      case None               => throw new Exception("Cannot parse data arrangement shape")
      case Some(Left(tuple))  => TupleShapeParser(tuple, entries, adopt, declarations).parse()
      case Some(Right(array)) => ArrayShapeParser(array, entries, adopt, declarations).parse()
    }
  }

}

case class TupleShapeParser(shape: TupleShape, entries: Entries, adopt: Shape => Unit, declarations: Declarations)
    extends ShapeParser() {

  override def parse(): Shape = {
    adopt(shape)

    super.parse()

    entries.key("minItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.MinItems, value.integer(), entry.annotations())
    })

    entries.key("maxItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.MaxItems, value.integer(), entry.annotations())
    })

    entries.key("uniqueItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.UniqueItems, value.boolean(), entry.annotations())
    })

    entries.key(
      "items",
      entry => {
        val items = Entries(entry.ast).entries.values.zipWithIndex
          .map(entry =>
            OasTypeParser(entry._1, items => items.adopted(shape.id + "/items/" + entry._2), declarations).parse())
          .toSeq
        shape.withItems(items.filter(_.isDefined).map(_.get))
      }
    )

    shape
  }
}

case class ArrayShapeParser(shape: ArrayShape, entries: Entries, adopt: Shape => Unit, declarations: Declarations)
    extends ShapeParser() {
  override def parse(): Shape = {
    adopt(shape)

    super.parse()

    entries.key("minItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.MinItems, value.integer(), entry.annotations())
    })

    entries.key("maxItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.MaxItems, value.integer(), entry.annotations())
    })

    entries.key("uniqueItems", entry => {
      val value = ValueNode(entry.value)
      shape.set(ArrayShapeModel.UniqueItems, value.boolean(), entry.annotations())
    })

    val finalShape = for {
      itemsEntry <- entries.key("items")
      item       <- OasTypeParser(itemsEntry, items => items.adopted(shape.id + "/items"), declarations).parse()
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

case class NodeShapeParser(shape: NodeShape, entries: Entries, declarations: Declarations) extends ShapeParser() {
  override def parse(): NodeShape = {

    super.parse()

    entries.key("type", _ => shape.add(ExplicitField())) // todo lexical of type?? new annotation?

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

    entries
      .key("required")
      .filter(_.value.`type` == SequenceToken)
      .foreach(entry => {
        val value = ArrayNode(entry.value)
        requiredFields = value.strings().scalars.map(_.value.toString)
      })

    entries.key(
      "properties",
      entry => {
        val properties: Seq[PropertyShape] =
          PropertiesParser(entry.value, shape.withProperty, requiredFields, declarations).parse()
        shape.set(NodeShapeModel.Properties, AmfArray(properties, Annotations(entry.value)), entry.annotations())
      }
    )

    val properties = mutable.ListMap[String, PropertyShape]()
    shape.properties.foreach(p => properties += (p.name -> p))

    entries.key(
      "dependencies",
      entry => {
        val dependencies: Seq[PropertyDependencies] =
          ShapeDependenciesParser(entry.value, properties).parse()
        shape.set(NodeShapeModel.Dependencies, AmfArray(dependencies, Annotations(entry.value)), entry.annotations())
      }
    )

    entries.key(
      "allOf",
      entry => {
        val inherits = AllOfParser(ArrayNode(entry.value), declarations, s => s.adopted(shape.id)).parse()

        shape.set(NodeShapeModel.Inherits, AmfArray(inherits, Annotations(entry.value)), entry.annotations())
      }
    )

    shape
  }
}

case class AllOfParser(array: ArrayNode, declarations: Declarations, adopt: Shape => Unit) {
  def parse(): Seq[Shape] =
    array.values.flatMap(map =>
      declarationsRef(Entries(map)).orElse(OasTypeParser(MapNode(map), adopt, declarations).parse()))

  private def declarationsRef(entries: Entries): Option[Shape] = {
    entries.key("$ref").map(entry => declarations.shapes(entry.value.content.unquote.stripPrefix("#/definitions/")))
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

case class PropertiesParser(ast: AMFAST,
                            producer: String => PropertyShape,
                            requiredFields: Seq[String],
                            declarations: Declarations) {

  def parse(): Seq[PropertyShape] = {
    Entries(ast).entries.values
      .map(entry => PropertyShapeParser(entry, producer, requiredFields, declarations).parse())
      .toSeq
  }
}

case class PropertyShapeParser(entry: EntryNode,
                               producer: String => PropertyShape,
                               requiredFields: Seq[String],
                               declarations: Declarations) {

  def parse(): PropertyShape = {

    val name     = entry.key.content.unquote
    val required = requiredFields.contains(name)

    val property = producer(name)
      .add(Annotations(entry.ast))
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

    entries.key(
      "xml",
      entry => {
        val xmlSerializer: XMLSerializer = XMLSerializerParser(shape.name, entry.value).parse()
        shape.set(ShapeModel.XMLSerialization, xmlSerializer, entry.annotations())
      }
    )

    shape
  }
}
