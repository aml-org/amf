package amf.spec.raml

import amf.common.AMFToken.{MapToken, SequenceToken, StringToken}
import amf.common.core.Strings
import amf.common.{AMFAST, AMFToken}
import amf.domain.Annotation.{ExplicitField, Inferred}
import amf.domain.{Annotations, CreativeWork}
import amf.metadata.shape._
import amf.model.{AmfArray, AmfScalar}
import amf.shape.RamlTypeDefMatcher.matchType
import amf.shape.TypeDef.{ArrayType, ObjectType, UndefinedType}
import amf.shape._
import amf.spec.Declarations

import scala.collection.mutable

case class RamlTypeParser(entry: EntryNode, adopt: Shape => Unit, declarations: Declarations) {

  def parse(): Option[Shape] = {
    val name = entry.key.content.unquote

    val ahead = lookAhead()

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

  private def detect(property: Either[AMFAST, Entries]): TypeDef = property match {
    case Left(node) =>
      node.`type` match {
        case StringToken   => matchType(node.content.unquote)
        case SequenceToken => ObjectType
      }
    case Right(entries) =>
      detectTypeOrSchema(entries)
        .orElse(detectProperties(entries))
        .orElse(detectItems(entries))
        .getOrElse(UndefinedType)
  }

  private def detectProperties(entries: Entries) = {
    entries.key("properties").map(_ => ObjectType)
  }

  private def detectItems(entries: Entries) = {
    entries.key("items").map(_ => ArrayType)
  }

  private def detectTypeOrSchema(entries: Entries) = {
    entries
      .key("type")
      .orElse(entries.key("schema"))
      .map(e =>
        e.value.`type` match {
          case StringToken =>
            val t = e.value.content.unquote
            val f = entries.key("(format)").map(_.value.content.unquote).getOrElse("")
            matchType(t, f)
          case SequenceToken => ObjectType
          case MapToken      => ObjectType
          case _             => UndefinedType
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

  def parseArrayType(name: String, ahead: Either[AMFAST, Entries]): Shape = {
    val shape = ahead match {
      case Right(entries) =>
        DataArrangementParser(name, entry, entries, (shape: Shape) => adopt(shape), declarations).parse()
      case _ => ArrayShape(entry.ast).withName(name)
    }
    shape
  }

  private def parseObjectType(name: String, ahead: Either[AMFAST, Entries], declarations: Declarations): Shape = {
    val shape = NodeShape(entry.ast).withName(name)
    adopt(shape)
    ahead match {
      case Right(entries) => NodeShapeParser(shape, entries, declarations).parse()
      case Left(_)        => shape
    }
  }

  def lookAhead(): Either[AMFAST, Entries] = {
    entry.value.`type` match {
      case StringToken | SequenceToken => Left(entry.value)
      case MapToken                    => Right(Entries(entry.value))
      case _                           => throw new RuntimeException("no value detected in look ahead")
    }
  }
}

case class RamlTypesParser(ast: AMFAST, adopt: Shape => Unit, declarations: Declarations) {
  def parse(): Seq[Shape] = {
    Entries(ast).entries.values
      .flatMap(entry => RamlTypeParser(entry, adopt, declarations).parse())
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

case class DataArrangementParser(name: String,
                                 entry: EntryNode,
                                 entries: Entries,
                                 adopt: Shape => Unit,
                                 declarations: Declarations) {

  def lookAhead(): Either[TupleShape, ArrayShape] = {
    entries.key("(tuple)") match {
      case Some(tuplesEntry) =>
        tuplesEntry.ast.`type` match {
          // this is a sequence, we need to create a tuple
          case AMFToken.SequenceToken => Left(TupleShape(entry.ast).withName(name))
          // not an array regular array parsing
          case _ => throw new Exception("Tuples must have a list of types")

        }
      case None => Right(ArrayShape(entry.ast).withName(name))
    }
  }

  def parse(): Shape = {
    lookAhead() match {
      case Left(tuple)  => TupleShapeParser(tuple, entries, adopt, declarations).parse()
      case Right(array) => ArrayShapeParser(array, entries, adopt, declarations).parse()
    }
  }

}

case class ArrayShapeParser(override val shape: ArrayShape,
                            entries: Entries,
                            adopt: Shape => Unit,
                            declarations: Declarations)
    extends ShapeParser() {

  override def parse(): Shape = {
    adopt(shape)

    super.parse()

    entries.key("type", _ => shape.add(ExplicitField()))

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

case class TupleShapeParser(shape: TupleShape, entries: Entries, adopt: Shape => Unit, declarations: Declarations)
    extends ShapeParser() {

  override def parse(): Shape = {
    adopt(shape)

    super.parse()

    entries.key("type", _ => shape.add(ExplicitField()))

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
            RamlTypeParser(entry._1, items => items.adopted(shape.id + "/items/" + entry._2), declarations).parse())
          .toSeq
        shape.withItems(items.filter(_.isDefined).map(_.get))
      }
    )

    shape
  }
}

case class NodeShapeParser(shape: NodeShape, entries: Entries, declarations: Declarations) extends ShapeParser() {
  override def parse(): NodeShape = {

    super.parse()

    entries.key("type", _ => shape.add(ExplicitField())) // todo lexical of type?? new annotation?

    entries.key(
      "type",
      entry => {
        entry.value.`type` match {
          case StringToken if entry.value.content.unquote != "object" =>
            shape.set(NodeShapeModel.Inherits,
                      AmfArray(Seq(declarations.shapes(entry.value.content.unquote)), Annotations(entry.value)),
                      entry.annotations())
          case SequenceToken =>
            val inherits = ArrayNode(entry.value)
              .strings()
              .scalars
              .map(scalar => declarations.shapes(scalar.toString))

            shape.set(NodeShapeModel.Inherits, AmfArray(inherits, Annotations(entry.value)), entry.annotations())
          case MapToken =>
            RamlTypeParser(entry, shape => shape.adopted(shape.id), declarations)
              .parse()
              .foreach(s => shape.set(NodeShapeModel.Inherits, s, entry.annotations()))
          case _ =>
            shape.add(ExplicitField()) // TODO store annotation in dataType field.
        }
      }
    )

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
          PropertiesParser(entry.value, shape.withProperty, declarations).parse()
        shape.set(NodeShapeModel.Properties, AmfArray(properties, Annotations(entry.value)), entry.annotations())
      }
    )

    val properties = mutable.ListMap[String, PropertyShape]()
    shape.properties.foreach(p => properties += (p.name -> p))

    entries.key(
      "(dependencies)",
      entry => {
        val dependencies: Seq[PropertyDependencies] =
          ShapeDependenciesParser(entry.value, properties).parse()
        shape.set(NodeShapeModel.Dependencies, AmfArray(dependencies, Annotations(entry.value)), entry.annotations())
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

case class PropertiesParser(ast: AMFAST, producer: String => PropertyShape, declarations: Declarations) {

  def parse(): Seq[PropertyShape] = {
    Entries(ast).entries.values
      .map(entry => PropertyShapeParser(entry, producer, declarations).parse())
      .toSeq
  }
}

case class PropertyShapeParser(entry: EntryNode, producer: String => PropertyShape, declarations: Declarations) {

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

    RamlTypeParser(entry, shape => shape.adopted(property.id), declarations)
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
