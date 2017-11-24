package amf.spec.declaration

import amf.domain.Annotation.{toString => _, _}
import amf.domain.{Annotations, CreativeWork, Value}
import amf.metadata.shape._
import amf.model.{AmfArray, AmfScalar}
import amf.parser.{YMapOps, YNodeLikeOps, YScalarYRead}
import amf.shape.TypeDef._
import amf.shape._
import amf.spec.common.{ArrayNode, ShapeExtensionParser, ValueNode}
import amf.spec.domain.RamlExamplesParser
import amf.spec.raml._
import amf.spec.{ParserContext, SearchScope}
import amf.vocabulary.Namespace
import org.yaml.model._
import org.yaml.parser.YamlParser
import org.yaml.render.YamlRender

import scala.collection.mutable

object RamlTypeParser {
  def apply(ast: YMapEntry,
            adopt: Shape => Shape,
            isAnnotation: Boolean = false,
            defaultType: DefaultType = StringDefaultType)(implicit ctx: ParserContext): RamlTypeParser =
    new RamlTypeParser(ast, ast.key, ast.value, adopt, isAnnotation, defaultType)(ctx.toRaml)
}

trait RamlTypeSyntax {
  def parseWellKnownTypeRef(ramlType: String): Shape = {
    ramlType match {
      case "nil" | "" | "null" => NilShape()
      case "any"               => AnyShape()
      case "string"            => ScalarShape().withDataType((Namespace.Xsd + "string").iri())
      case "integer"           => ScalarShape().withDataType((Namespace.Xsd + "integer").iri())
      case "number"            => ScalarShape().withDataType((Namespace.Xsd + "float").iri())
      case "boolean"           => ScalarShape().withDataType((Namespace.Xsd + "boolean").iri())
      case "datetime"          => ScalarShape().withDataType((Namespace.Xsd + "dateTime").iri())
      case "datetime-only"     => ScalarShape().withDataType((Namespace.Xsd + "dateTime").iri())
      case "time-only"         => ScalarShape().withDataType((Namespace.Xsd + "time").iri())
      case "date-only"         => ScalarShape().withDataType((Namespace.Xsd + "date").iri())
      case "array"             => ArrayShape()
      case "object"            => NodeShape()
      case "union"             => UnionShape()
    }
  }
  def wellKnownType(str: String): Boolean =
    if (str.indexOf("|") > -1 || str.indexOf("[") > -1 || str.indexOf("{") > -1 || str.indexOf("]") > -1 || str
          .indexOf("}") > -1) {
      false
    } else RamlTypeDefMatcher.matchType(str, default = UndefinedType) != UndefinedType
}

// Default RAML types
abstract class DefaultType {
  val typeDef: TypeDef
}
// By default, string si the default type
object StringDefaultType extends DefaultType {
  override val typeDef = TypeDef.StrType
}
// In a body or body / application/json context it its any
object AnyDefaultType extends DefaultType {
  override val typeDef = TypeDef.AnyType
}

case class RamlTypeParser(ast: YPart,
                          name: String,
                          node: YNode,
                          adopt: Shape => Shape,
                          isAnnotation: Boolean,
                          defaultType: DefaultType)(implicit val ctx: ParserContext)
    extends RamlSpecParser {

  def parse(): Option[Shape] = {

    val info: Option[TypeDef] =
      RamlTypeDetection(node,
                        "",
                        node.toOption[YMap].flatMap(m => m.key("(format)").map(_.value.toString())),
                        defaultType)
    val result = info.map {
      case XMLSchemaType                         => parseXMLSchemaExpression(ast.asInstanceOf[YMapEntry])
      case JSONSchemaType                        => parseJSONSchemaExpression(ast.asInstanceOf[YMapEntry])
      case TypeExpressionType                    => parseTypeExpression()
      case UnionType                             => parseUnionType()
      case ObjectType | FileType | UndefinedType => parseObjectType()
      case ArrayType                             => parseArrayType()
      case AnyType                               => parseAnyType()
      case typeDef if typeDef.isScalar           => parseScalarType(typeDef)
      case MultipleMatch                         => parseScalarType(StrType)
    }

    // Add 'inline' annotation for shape
    result
      .map(shape =>
        node.value match {
          case _: YScalar if !info.contains(MultipleMatch) =>
            shape.add(InlineDefinition()) // case of only one field (ej required) and multiple shape matches (use default string)
          case _ => shape
      })

    // custom facet properties
    parseCustomShapeFacetInstances(result)

    result
  }

  // These are the actual custom facets, just regular properties in the AST map that have been
  // defined through the 'facets' properties in this shape or in base shape.
  // The shape definitions are parsed in the common parser of all shapes, not here.
  private def parseCustomShapeFacetInstances(shapeResult: Option[Shape]): Unit = {
    node.value match {
      case map: YMap if shapeResult.isDefined => ShapeExtensionParser(shapeResult.get, map, ctx).parse()
      case _                                  => // ignore if it is not a map or we haven't been able to parse a shape
    }

  }

  private def parseXMLSchemaExpression(entry: YMapEntry): Shape = {
    entry.value.tagType match {
      case YType.Map =>
        entry.value.as[YMap].key("type") match {
          case Some(typeEntry: YMapEntry) if typeEntry.value.toOption[YScalar].isDefined =>
            val shape =
              SchemaShape().withRaw(typeEntry.value.as[YScalar].text).withMediaType("application/xml")
            shape.withName(entry.key)
            adopt(shape)
            shape
          case _ =>
            val shape = SchemaShape()
            adopt(shape)
            ctx.violation(shape.id, "Cannot parse XML Schema expression out of a non string value", entry.value)
            shape
        }
      case YType.Seq =>
        val shape = SchemaShape()
        adopt(shape)
        ctx.violation(shape.id, "Cannot parse XML Schema expression out of a non string value", entry.value)
        shape
      case _ =>
        val shape = SchemaShape().withRaw(entry.value.as[YScalar].text).withMediaType("application/xml")
        shape.withName(entry.key)
        adopt(shape)
        shape
    }
  }

  private def parseJSONSchemaExpression(entry: YMapEntry): Shape = {
    val text = entry.value.tagType match {
      case YType.Map =>
        entry.value.as[YMap].key("type") match {
          case Some(typeEntry: YMapEntry) if typeEntry.value.toOption[YScalar].isDefined =>
            typeEntry.value.as[YScalar].text
          case _ =>
            val shape = SchemaShape()
            adopt(shape)
            ctx.violation(shape.id, "Cannot parse XML Schema expression out of a non string value", entry.value)
            ""
        }
      case YType.Seq =>
        val shape = SchemaShape()
        adopt(shape)
        ctx.violation(shape.id, "Cannot parse XML Schema expression out of a non string value", entry.value)
        ""
      case _ => entry.value.as[YScalar].text
    }

    val schemaAst   = YamlParser(text).parse(keepTokens = true)
    val schemaEntry = YMapEntry(entry.key, schemaAst.head.asInstanceOf[YDocument].node)
    OasTypeParser(schemaEntry, (shape) => adopt(shape)).parse() match {
      case Some(shape) =>
        shape.annotations += ParsedJSONSchema(text)
        shape
      case None =>
        val shape = SchemaShape()
        adopt(shape)
        ctx.violation(shape.id, "Cannot parse JSON Schema", entry)
        shape
    }
  }

  private def parseTypeExpression(): Shape = {
    node.value match {
      case expression: YScalar =>
        RamlTypeExpressionParser(adopt, Some(node.value)).parse(expression.text).get

      case _: YMap => parseObjectType()
    }
  }

  private def parseScalarType(typeDef: TypeDef): Shape = {
    if (typeDef.isNil) {
      val nilShape = NilShape(ast).withName(name)
      adopt(nilShape)
      nilShape
    } else {
      val shape = ScalarShape(ast).withName(name)
      adopt(shape)
      node
        .to[YMap] match { // todo review with pedro: in this case use either? or use toOption fold (empty)(default) (more examples bellow)
        case Right(map) => ScalarShapeParser(typeDef, shape, map).parse()
        case Left(_) =>
          shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef), Annotations(node.value)))
      }
    }
  }

  private def parseAnyType(): Shape = {
    val shape = AnyShape(ast).withName(name)
    adopt(shape)
    shape
  }

  def parseArrayType(): Shape = {
    val shape = node.to[YMap] match {
      case Right(map) => DataArrangementParser(name, ast, map, (shape: Shape) => adopt(shape)).parse()
      case Left(_)    => ArrayShape(ast).withName(name)
    }
    shape
  }

  private def parseUnionType(): UnionShape = {
    UnionShapeParser(node.as[YMap], adopt).parse()
  }

  private def parseObjectType(): Shape = {
    if (isFileType) {
      val shape = FileShapeParser(node.as[YMap]).parse()
      adopt(shape)
      shape
    } else {
      val shape = NodeShape(ast).withName(name)
      adopt(shape)

      node.tagType match {
        case YType.Map =>
          NodeShapeParser(shape, node.as[YMap])
            .parse() // I have to do the adopt before parser children shapes. Other way the children will not have the father id
        case YType.Seq =>
          InheritanceParser(ast.asInstanceOf[YMapEntry], shape).parse()
          shape
        case _ if node.toOption[YScalar].isDefined =>
          val refTuple = ctx.link(node) match {
            case Left(key) =>
              (key, ctx.declarations.findType(key, SearchScope.Fragments))
            case _ =>
              val text = node.as[YScalar].text
              (text, ctx.declarations.findType(text, SearchScope.Named))
          }

          refTuple match {
            case (text: String, Some(s)) => s.link(text, Annotations(node.value)).asInstanceOf[Shape].withName(name)
            case (text: String, _) =>
              val shape = UnresolvedShape(text, node).withName(name)
              adopt(shape)
              shape
          }
      }
    }
  }

  private def isFileType: Boolean = {
    node.to[YMap] match {
      case Right(map) =>
        map
          .key("type")
          .exists { entry: YMapEntry =>
            entry.value.to[YScalar] match {
              case Right(scalar) =>
                scalar.text == "file"
              case _ => false
            }
          }
      case Left(_) => false
    }
  }

  trait CommonScalarParsingLogic {
    def parseOASFields(map: YMap, shape: Shape): Unit = {
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

      map.key("(exclusiveMinimum)", entry => {
        val value = ValueNode(entry.value)
        shape.set(ScalarShapeModel.ExclusiveMinimum, value.text(), Annotations(entry))
      })

      map.key("(exclusiveMaximum)", entry => {
        val value = ValueNode(entry.value)
        shape.set(ScalarShapeModel.ExclusiveMaximum, value.text(), Annotations(entry))
      })
    }
  }

  case class ScalarShapeParser(typeDef: TypeDef, shape: ScalarShape, map: YMap)
      extends ShapeParser
      with CommonScalarParsingLogic {

    override def parse(): ScalarShape = {
      super.parse()

      parseOASFields(map, shape)

      map
        .key("type")
        .fold(shape
          .set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), Annotations() += Inferred()))(
          entry => shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), Annotations(entry)))

      map.key("minimum", entry => {
        val value = ValueNode(entry.value)
        shape.set(ScalarShapeModel.Minimum, value.text(), Annotations(entry))
      })

      map.key("maximum", entry => {
        val value = ValueNode(entry.value)
        shape.set(ScalarShapeModel.Maximum, value.text(), Annotations(entry))
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

      val syntaxType = Option(shape.dataType).getOrElse("#shape").split("#").last match {
        case "integer" | "float" => "numberScalarShape"
        case "string"            => "stringScalarShape"
        case "dateTime"          => "dateScalarShape"
        case _                   => "shape"
      }

      ctx.closedRamlTypeShape(shape, map, syntaxType, isAnnotation)

      shape
    }
  }

  case class UnionShapeParser(override val map: YMap, adopt: (Shape) => Shape) extends ShapeParser {
    override val shape = UnionShape(Annotations(map))

    override def parse(): UnionShape = {
      adopt(shape)
      super.parse()

      map.key(
        "anyOf", { entry =>
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val unionNodes = seq.zipWithIndex
                .map {
                  case (unionNode, index) =>
                    RamlTypeParser(unionNode,
                                   s"item$index",
                                   unionNode,
                                   item => item.adopted(shape.id + "/items/" + index),
                                   isAnnotation,
                                   StringDefaultType).parse()
                }
                .filter(_.isDefined)
                .map(_.get)
              shape.setArray(UnionShapeModel.AnyOf, unionNodes, Annotations(entry.value))

            case _ =>
              ctx.violation(shape.id, "Unions are built from multiple shape nodes", entry)
          }
        }
      )

      ctx.closedRamlTypeShape(shape, map, "unionShape", isAnnotation)

      shape
    }
  }

  case class FileShapeParser(override val map: YMap) extends ShapeParser with CommonScalarParsingLogic {
    override val shape = FileShape(Annotations(map))

    override def parse(): FileShape = {
      super.parse()
      parseOASFields(map, shape)

      map.key(
        "fileTypes", { entry =>
          entry.value.tagType match {
            case YType.Seq =>
              val value = ArrayNode(entry.value)
              shape.set(FileShapeModel.FileTypes, value.strings(), Annotations(entry.value))
            case _ =>
          }
        }
      )

      map.key("(minimum)", entry => {
        val value = ValueNode(entry.value)
        shape.set(ScalarShapeModel.Minimum, value.string(), Annotations(entry))
      })

      map.key("(maximum)", entry => {
        val value = ValueNode(entry.value)
        shape.set(ScalarShapeModel.Maximum, value.string(), Annotations(entry))
      })

      map.key("(format)", entry => {
        val value = ValueNode(entry.value)
        shape.set(ScalarShapeModel.Format, value.string(), Annotations(entry))
      })

      // We don't need to parse (format) extension because in oas must not be emitted, and in raml will be emitted.

      map.key("(multipleOf)", entry => {
        val value = ValueNode(entry.value)
        shape.set(ScalarShapeModel.MultipleOf, value.integer(), Annotations(entry))
      })

      ctx.closedRamlTypeShape(shape, map, "fileShape", isAnnotation)

      shape
    }
  }

  case class DataArrangementParser(name: String, ast: YPart, map: YMap, adopt: Shape => Unit) {

    def lookAhead(): Either[TupleShape, ArrayShape] = {
      map.key("(tuple)") match {
        case Some(entry) =>
          entry.value.to[Seq[YNode]] match {
            // this is a sequence, we need to create a tuple
            case Right(_) => Left(TupleShape(ast).withName(name))
            // not an array regular array parsing
            case _ =>
              val tuple = TupleShape(ast).withName(name)
              ctx.violation(tuple.id, "Tuples must have a list of types", ast)
              Left(tuple)
          }
        case None => Right(ArrayShape(ast).withName(name))
      }
    }

    def parse(): Shape = {
      lookAhead() match {
        case Left(tuple)  => TupleShapeParser(tuple, map, adopt).parse()
        case Right(array) => ArrayShapeParser(array, map, adopt).parse()
      }
    }

  }

  case class ArrayShapeParser(override val shape: ArrayShape, map: YMap, adopt: Shape => Unit) extends ShapeParser {

    override def parse(): Shape = {
      adopt(shape)

      super.parse()

      map.key("uniqueItems", entry => {
        val value = ValueNode(entry.value)
        shape.set(ArrayShapeModel.UniqueItems, value.boolean(), Annotations(entry))
      })

      val finalShape = (for {
        itemsEntry <- map.key("items")
        item       <- RamlTypeParser(itemsEntry, items => items.adopted(shape.id + "/items")).parse()
      } yield {
        item match {
          case array: ArrayShape   => shape.withItems(array).toMatrixShape
          case matrix: MatrixShape => shape.withItems(matrix).toMatrixShape
          case other: Shape        => shape.withItems(other)
        }
      }).orElse(arrayShapeTypeFromInherits())

      finalShape match {
        case Some(parsed: Shape) =>
          ctx.closedRamlTypeShape(parsed, map, "arrayShape", isAnnotation)
          parsed
        case None =>
          ctx.violation(shape.id, "Cannot parse data arrangement shape", map)
          shape
      }
    }

    private def arrayShapeTypeFromInherits(): Option[Shape] = {
      val maybeShape = shape.inherits.headOption.map {
        case matrix: MatrixShape => matrix.items
        case tuple: TupleShape   => tuple.items.head
        case array: ArrayShape   => array.items
      }
      maybeShape.map {
        case _: ArrayShape  => shape.toMatrixShape
        case _: MatrixShape => shape.toMatrixShape
        case _: Shape       => shape
      }
    }
  }

  case class TupleShapeParser(shape: TupleShape, map: YMap, adopt: Shape => Unit) extends ShapeParser {

    override def parse(): Shape = {
      adopt(shape)

      super.parse()

      parseInheritance()

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
          val items = entry.value
            .as[YMap]
            .entries
            .zipWithIndex
            .map {
              case (elem, index) =>
                RamlTypeParser(elem, item => item.adopted(shape.id + "/items/" + index)).parse()
            }
          shape.withItems(items.filter(_.isDefined).map(_.get))
        }
      )

      ctx.closedRamlTypeShape(shape, map, "arrayShape", isAnnotation)

      shape
    }
  }

  case class InheritanceParser(entry: YMapEntry, shape: Shape) extends RamlTypeSyntax {
    def parse(): Unit = {
      entry.value.tagType match {

        case YType.Seq =>
          val inherits = ArrayNode(entry.value)
            .strings()
            .scalars
            .map { scalar =>
              scalar.toString match {
                case s if RamlTypeDefMatcher.TypeExpression.unapply(s).isDefined =>
                  RamlTypeExpressionParser(adopt).parse(s).get
                case s if wellKnownType(s) =>
                  parseWellKnownTypeRef(s)
                case s =>
                  ctx.declarations.findType(s, SearchScope.Named) match {
                    case Some(ancestor) =>
                      ancestor
                    case _ =>
                      val unresolvedShape: UnresolvedShape = UnresolvedShape(s, entry.value).withName(s)
                      ctx.declarations += unresolvedShape
                      unresolvedShape
                  }
              }
            }

          shape.set(ShapeModel.Inherits, AmfArray(inherits, Annotations(entry.value)), Annotations(entry))

        case YType.Map =>
          RamlTypeParser(entry, shape => shape.adopted(shape.id))
            .parse()
            .foreach(s =>
              shape.set(ShapeModel.Inherits, AmfArray(Seq(s), Annotations(entry.value)), Annotations(entry)))

        case _ if RamlTypeDefMatcher.TypeExpression.unapply(entry.value.as[YScalar].text).isDefined =>
          RamlTypeParser(entry, shape => shape.adopted(shape.id))
            .parse()
            .foreach(s =>
              shape.set(ShapeModel.Inherits, AmfArray(Seq(s), Annotations(entry.value)), Annotations(entry)))

        case _ if !wellKnownType(entry.value.as[YScalar].text) =>
          val text = entry.value.as[YScalar].text
          // it might be a named type
          // only search for named ref, ex Person: !include. We dont handle inherits from an anonymous type like type: !include
          ctx.declarations.findType(text, SearchScope.Named) match {
            case Some(ancestor) =>
              shape.set(ShapeModel.Inherits, AmfArray(Seq(ancestor), Annotations(entry.value)), Annotations(entry))
            case _ =>
              val unresolvedShape: UnresolvedShape = UnresolvedShape(text, entry.value).withName(text)
              ctx.declarations += unresolvedShape
              shape.set(ShapeModel.Inherits,
                        AmfArray(Seq(unresolvedShape), Annotations(entry.value)),
                        Annotations(entry))
          }

        case _ =>
          shape.add(ExplicitField()) // TODO store annotation in dataType field.
      }
    }
  }

  case class NodeShapeParser(shape: NodeShape, map: YMap) extends ShapeParser {
    override def parse(): NodeShape = {

      super.parse()

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
          entry.value.toOption[YMap] match {
            case Some(m) =>
              val properties: Seq[PropertyShape] =
                PropertiesParser(m, shape.withProperty).parse()
              shape.set(NodeShapeModel.Properties, AmfArray(properties, Annotations(entry.value)), Annotations(entry))
            case _ => // Empty properties node.
          }
        }
      )

      val properties = mutable.ListMap[String, PropertyShape]()
      shape.properties.foreach(p => properties += (p.name -> p))

      map.key(
        "(dependencies)",
        entry => {
          val dependencies: Seq[PropertyDependencies] =
            ShapeDependenciesParser(entry.value.as[YMap], properties).parse()
          shape.set(NodeShapeModel.Dependencies, AmfArray(dependencies, Annotations(entry.value)), Annotations(entry))
        }
      )

      ctx.closedRamlTypeShape(shape, map, "nodeShape", isAnnotation)

      shape
    }
  }

  case class PropertiesParser(ast: YMap, producer: String => PropertyShape) {

    def parse(): Seq[PropertyShape] = {
      ast.entries
        .map(entry => PropertyShapeParser(entry, producer).parse())
    }
  }

  case class PropertyShapeParser(entry: YMapEntry, producer: String => PropertyShape) {

    def parse(): PropertyShape = {

      val name: String = entry.key
      val property     = producer(name).add(Annotations(entry))

      var explicitRequired: Option[Value] = None
      entry.value.to[YMap] match {
        case Right(map) =>
          map.key(
            "required",
            entry => {
              val required = ValueNode(entry.value).boolean().value.asInstanceOf[Boolean]
              explicitRequired = Some(Value(AmfScalar(required), Annotations(entry) += ExplicitField()))
              property.set(PropertyShapeModel.MinCount,
                           AmfScalar(if (required) 1 else 0),
                           Annotations(entry) += ExplicitField())
            }
          )
        case _ =>
      }

      if (property.fields.?(PropertyShapeModel.MinCount).isEmpty) {
        val required = !name.endsWith("?")

        property.set(PropertyShapeModel.MinCount, if (required) 1 else 0)
        property.set(PropertyShapeModel.Name, if (required) name else name.stripSuffix("?")) // TODO property id is using a name that is not final.
      }

      property.set(PropertyShapeModel.Path, (Namespace.Data + entry.key.as[YScalar].text).iri())

      RamlTypeParser(entry, shape => shape.adopted(property.id))
        .parse()
        .foreach { range =>
          if (explicitRequired.isDefined) {
            range.fields.setWithoutId(ShapeModel.RequiredShape,
                                      explicitRequired.get.value,
                                      explicitRequired.get.annotations)
          }

          if (entry.value.tagType == YType.Null) {
            range.annotations += SynthesizedField()
          }

          property.set(PropertyShapeModel.Range, range)
        }

      property
    }
  }

  abstract class ShapeParser extends RamlTypeSyntax {

    val shape: Shape
    val map: YMap

    def parse(): Shape = {

      parseInheritance()

      map.key("displayName", entry => {
        val value = ValueNode(entry.value)
        shape.set(ShapeModel.DisplayName, value.string(), Annotations(entry))
      })

      map.key("description", entry => {
        val value = ValueNode(entry.value)
        shape.set(ShapeModel.Description, value.string(), Annotations(entry))
      })

      map.key(
        "default",
        entry => {
          entry.value.tagType match {
            case YType.Map | YType.Seq =>
              shape.set(ShapeModel.Default,
                        AmfScalar(YamlRender.render(entry.value), Annotations(entry.value)),
                        Annotations(entry))
            case _ =>
              val value = ValueNode(entry.value)
              shape.set(ShapeModel.Default, value.text(), Annotations(entry))
          }
        }
      )

      map.key("enum", entry => {
        val value = ArrayNode(entry.value)
        shape.set(ShapeModel.Values, value.strings(), Annotations(entry))
      })

      map.key("minItems", entry => {
        val value = ValueNode(entry.value)
        shape.set(ArrayShapeModel.MinItems, value.integer(), Annotations(entry))
      })

      map.key("maxItems", entry => {
        val value = ValueNode(entry.value)
        shape.set(ArrayShapeModel.MaxItems, value.integer(), Annotations(entry))
      })

      map.key(
        "(externalDocs)",
        entry => {
          val creativeWork: CreativeWork = OasCreativeWorkParser(entry.value.as[YMap]).parse()
          shape.set(ShapeModel.Documentation, creativeWork, Annotations(entry))
        }
      )

      map.key(
        "xml",
        entry => {
          val xmlSerializer: XMLSerializer =
            XMLSerializerParser(shape.name, entry.value.as[YMap]).parse()
          shape.set(ShapeModel.XMLSerialization, xmlSerializer, Annotations(entry))
        }
      )

      // Custom shape property definitions, not instances, those are parsed at the end of the parsing process
      map.key(
        "facets",
        entry => {
          val properties: Seq[PropertyShape] =
            PropertiesParser(entry.value.as[YMap], shape.withCustomShapePropertyDefinition).parse()
        }
      )

      val examples = RamlExamplesParser(map, "example", "examples").parse()
      if (examples.nonEmpty)
        shape.setArray(ShapeModel.Examples, examples)

      shape
    }

    protected def parseInheritance(): Unit = {
      map.key(
        "type",
        entry => InheritanceParser(entry, shape).parse()
      )
    }
  }

}
