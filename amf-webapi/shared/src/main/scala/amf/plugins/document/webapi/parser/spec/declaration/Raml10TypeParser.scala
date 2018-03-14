package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.{DefaultNode, ExplicitField, SourceAST, SynthesizedField}
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{AmfArray, AmfElement, AmfScalar, Shape}
import amf.core.parser.{Annotations, Value, _}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.annotations._
import amf.plugins.document.webapi.contexts.{
  Raml08WebApiContext,
  Raml10WebApiContext,
  RamlWebApiContext,
  WebApiContext
}
import amf.plugins.document.webapi.parser.RamlTypeDefMatcher
import amf.plugins.document.webapi.parser.RamlTypeDefMatcher.{JSONSchema, XMLSchema}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, ShapeExtensionParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.domain.{RamlExamplesParser, RamlSingleExampleParser}
import amf.plugins.document.webapi.parser.spec.raml.{RamlSpecParser, RamlTypeExpressionParser}
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models.TypeDef._
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.parser.XsdTypeDefMapping
import org.yaml.model.{YPart, _}
import org.yaml.parser.YamlParser
import org.yaml.render.YamlRender

import scala.collection.mutable

object Raml10TypeParser {
  def apply(ast: YMapEntry,
            adopt: Shape => Shape,
            isAnnotation: Boolean = false,
            defaultType: DefaultType = StringDefaultType)(implicit ctx: WebApiContext): Raml10TypeParser =
    new Raml10TypeParser(ast, ast.key, ast.value, adopt, isAnnotation, defaultType)(
      new Raml10WebApiContext(ctx, Some(ctx.declarations)))

  def parse(adopt: Shape => Shape, isAnnotation: Boolean = false, defaultType: DefaultType = StringDefaultType)(
      node: YNode)(implicit ctx: RamlWebApiContext) = {
    val head = node.as[YMap].entries.head
    apply(head, adopt, isAnnotation, defaultType).parse()
  }
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
      case "datetime-only"     => ScalarShape().withDataType((Namespace.Shapes + "dateTimeOnly").iri())
      case "time-only"         => ScalarShape().withDataType((Namespace.Xsd + "time").iri())
      case "date-only"         => ScalarShape().withDataType((Namespace.Xsd + "date").iri())
      case "array"             => ArrayShape()
      case "object"            => NodeShape()
      case "union"             => UnionShape()
      case "file"              => FileShape()
    }
  }

  def wellKnownType(str: String): Boolean =
    if (str.indexOf("|") > -1 || str.indexOf("[") > -1 || str.indexOf("{") > -1 || str.indexOf("]") > -1 || str
          .indexOf("}") > -1 || (str.startsWith("<<") && str.endsWith(">>"))) {
      false
    } else RamlTypeDefMatcher.matchType(str, default = UndefinedType) != UndefinedType

  def isTypeExpression(str: String): Boolean = {
    try { RamlTypeDefMatcher.matchType(str, default = UndefinedType) == TypeExpressionType } catch {
      case _: Exception => false
    }
  }
}

// Default RAML types
abstract class DefaultType {
  val typeDef: TypeDef
}

// By default, string si the default type
object StringDefaultType extends DefaultType {
  override val typeDef: TypeDef = TypeDef.StrType
}

// In a body or body / application/json context it its any
object AnyDefaultType extends DefaultType {
  override val typeDef: TypeDef = TypeDef.AnyType
}

// In a body or body / application/json context raml 08 it its nil
object NilDefaultType extends DefaultType {
  override val typeDef: TypeDef = TypeDef.NilType
}

case class Raml10TypeParser(ast: YPart,
                            name: String,
                            node: YNode,
                            adopt: Shape => Shape,
                            isAnnotation: Boolean,
                            defaultType: DefaultType)(implicit override val ctx: RamlWebApiContext)
    extends RamlTypeParser(ast: YPart,
                           name: String,
                           node: YNode,
                           adopt: Shape => Shape,
                           isAnnotation: Boolean,
                           defaultType: DefaultType) {
  override def typeParser: (YPart, String, YNode, (Shape) => Shape, Boolean, DefaultType) => RamlTypeParser =
    Raml10TypeParser.apply

}

object Raml08TypeParser {
  def apply(ast: YMapEntry,
            adopt: Shape => Shape,
            isAnnotation: Boolean = false,
            defaultType: DefaultType = StringDefaultType)(implicit ctx: WebApiContext): Raml08TypeParser =
    new Raml08TypeParser(ast, ast.key.as[YScalar].text, ast.value, adopt, isAnnotation, defaultType)(
      new Raml08WebApiContext(ctx, Some(ctx.declarations)))
}

case class Raml08TypeParser(ast: YPart,
                            name: String,
                            node: YNode,
                            adopt: Shape => Shape,
                            isAnnotation: Boolean,
                            defaultType: DefaultType)(implicit override val ctx: RamlWebApiContext)
    extends RamlTypeParser(ast: YPart, name: String, node: YNode, adopt: Shape => Shape, isAnnotation, defaultType) {

  override def parse(): Option[Shape] = {
    val shape = ScalarShape(node).withName(name)
    adopt(shape)
    node.tagType match {
      case YType.Map =>
        // has schema or its simple raml type declaration
        val map = node.as[YMap]
        map
          .key("schema")
          .fold({
            Option(SimpleTypeParser(name, adopt, map, defaultType.typeDef).parse())
          })(_ => {
            val maybeShape = Raml08SchemaParser(map, adopt).parse()

            maybeShape.foreach(s => {
              RamlSingleExampleParser("example", map)
                .parse()
                .foreach(e => s.setArray(ScalarShapeModel.Examples, Seq(e)))
            })
            maybeShape
          })
      case YType.Seq =>
        Option(Raml08UnionTypeParser(UnionShape(node).withName(name), node.as[Seq[YNode]], node).parse())
      case _ => Raml08TextParser(node, adopt, name, defaultType).parse()
    }
  }

  override def typeParser: (YPart, String, YNode, (Shape) => Shape, Boolean, DefaultType) => RamlTypeParser =
    Raml08TypeParser.apply

  case class Raml08ReferenceParser(text: String, node: YNode, name: String)(implicit ctx: RamlWebApiContext) {
    def parse(): Some[Shape] = {
      val shape = ctx.declarations.findType(text, SearchScope.All) match {
        case Some(s: Shape) => s.link(text, Annotations(node.value)).asInstanceOf[Shape].withName(name)
        case None =>
          val shape = UnresolvedShape(text, node).withName(text)
          shape.withContext(ctx)
          adopt(shape)
          shape.unresolved(text, node)
          shape
      }
      Some(shape)
    }
  }

  case class Raml08TextParser(value: YNode, adopt: (Shape) => Shape, name: String, defaultType: DefaultType)(
      implicit ctx: RamlWebApiContext) {
    def parse(): Option[Shape] = {
      value.tagType match {
        case YType.Null =>
          Raml08DefaultTypeParser(defaultType.typeDef, name, adopt).parse().map(s => s.add(SourceAST(value)))
        case _ =>
          value.as[YScalar].text match {
            case XMLSchema(_)  => Option(parseXMLSchemaExpression(name, value, adopt))
            case JSONSchema(_) => Option(parseJSONSchemaExpression(name, value, adopt))
            case t if RamlTypeDefMatcher.match08Type(t).isDefined =>
              Option(
                SimpleTypeParser(name, adopt, YMap.empty, defaultType = RamlTypeDefMatcher.match08Type(t).get).parse())
            case t => Raml08ReferenceParser(t, node, name).parse()
          }
      }
    }
  }

  case class Raml08SchemaParser(map: YMap, adopt: (Shape) => Shape)(implicit ctx: RamlWebApiContext) {
    def parse(): Option[Shape] = {
      map.key("schema").flatMap { e =>
        e.value.tagType match {
          case YType.Map | YType.Seq =>
            Raml08TypeParser(e, "schema", e.value, adopt, isAnnotation = false, StringDefaultType).parse()
          case _ => Raml08TextParser(e.value, adopt, "schema", defaultType).parse()
        }
      }
    }
  }

}

case class Raml08DefaultTypeParser(defaultType: TypeDef, name: String, adopt: (Shape) => Shape)(
    implicit ctx: RamlWebApiContext) {
  def parse(): Option[Shape] = {
    val product = defaultType match {
      case NilType =>
        Option(NilShape().withName(name).add(Inferred()))
      case StrType =>
        Option(ScalarShape()
          .set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(defaultType)), Annotations() += Inferred()))
      case _ =>
        ctx.violation(s"Cannot set default type $defaultType in raml 08", None)
        None
    }
    product.map(adopt)
  }
}

case class Raml08UnionTypeParser(shape: Shape, types: Seq[YNode], ast: YPart)(implicit ctx: RamlWebApiContext) {
  def parse(): Shape = {

    val unionNodes = types.zipWithIndex
      .map {
        case (unionNode, index) =>
          Raml08TypeParser(unionNode,
                           s"item$index",
                           unionNode,
                           item => item.adopted(shape.id + "/items/" + index),
                           isAnnotation = false,
                           AnyDefaultType).parse()
      }
      .filter(_.isDefined)
      .map(_.get)
    shape.setArray(UnionShapeModel.AnyOf, unionNodes, Annotations(ast))

  }
}

case class SimpleTypeParser(name: String, adopt: Shape => Shape, map: YMap, defaultType: TypeDef)(
    implicit val ctx: RamlWebApiContext)
    extends SpecParserOps {

  def parse(): Shape = {

    if (map.key("repeat").exists(entry => entry.value.as[Boolean])) {
      val shape = ArrayShape(map).withName(name)
      adopt(shape)
      val items =
        SimpleTypeParser("items",
                         (s: Shape) => s.adopted(shape.id),
                         YMap(map.entries.filter(entry => !entry.key.as[YScalar].text.equals("repeat"))),
                         defaultType).parse()
      shape.withItems(items)
      shape
    } else {
      val shape = map
        .key("type")
        .flatMap(e => {
          e.value.tagType match {
            case YType.Null => None
            case _          => e.value.toOption[YScalar]
          }
        })
        .fold(Raml08DefaultTypeParser(defaultType, name, adopt).parse())(value => {
          XsdTypeDefMapping.xsdFromString(value.text) match {
            case (iri: String, format: Option[String]) =>
              val shape = ScalarShape(value).set(ScalarShapeModel.DataType, AmfScalar(iri), Annotations(value))
              format.foreach(f => shape.set(ScalarShapeModel.Format, AmfScalar(f), Annotations()))
              Some(shape.withName(name))
            case _ => None
          }
        })
        .getOrElse(ScalarShape(map).withName(name))

      adopt(shape)
      parseMap(shape)
      val str = shape match {
        case scalar: ScalarShape => scalar.dataType
        case _                   => "#shape"
      }

      val syntaxType = str.split("#").last match {
        case "integer" | "float" => "numberScalarShape"
        case "string"            => "stringScalarShape"
        case "dateTime"          => "dateScalarShape"
        case _                   => "shape"
      }
      ctx.closedShape(shape.id, map, syntaxType)
      shape.asInstanceOf[Shape]
    }
  }

  private def parseMap(shape: Shape): Unit = {

    map.key("displayName", ShapeModel.DisplayName in shape)
    map.key("description", ShapeModel.Description in shape)
    map.key("enum", ShapeModel.Values in shape)
    map.key("pattern", ScalarShapeModel.Pattern in shape)
    map.key("minLength", ScalarShapeModel.MinLength in shape)
    map.key("maxLength", ScalarShapeModel.MaxLength in shape)

    map.key("minimum", entry => { // todo pope
      val value = ScalarNode(entry.value)
      shape.set(ScalarShapeModel.Minimum, value.text(), Annotations(entry))
    })

    map.key("maximum", entry => { // todo pope
      val value = ScalarNode(entry.value)
      shape.set(ScalarShapeModel.Maximum, value.text(), Annotations(entry))
    })

    RamlSingleExampleParser("example", map).parse().foreach(e => shape.setArray(ScalarShapeModel.Examples, Seq(e)))

    map.key("required", ScalarShapeModel.RequiredShape in shape)
    map.key("default", ShapeModel.Default in shape)
  }
}

trait RamlExternalTypes {
  implicit val ctx: RamlWebApiContext

  protected def parseXMLSchemaExpression(name: String, value: YNode, adopt: Shape => Shape): Shape = {
    value.tagType match {
      case YType.Map =>
        val map = value.as[YMap]
        typeOrSchema(map) match {
          case Some(typeEntry: YMapEntry) if typeEntry.value.toOption[YScalar].isDefined =>
            val shape =
              SchemaShape().withRaw(typeEntry.value.as[YScalar].text).withMediaType("application/xml")
            shape.withName(name)
            adopt(shape)
            shape
          case _ =>
            val shape = SchemaShape()
            adopt(shape)
            ctx.violation(shape.id, "Cannot parse XML Schema expression out of a non string value", value)
            shape
        }
      case YType.Seq =>
        val shape = SchemaShape()
        adopt(shape)
        ctx.violation(shape.id, "Cannot parse XML Schema expression out of a non string value", value)
        shape
      case _ =>
        val shape = SchemaShape().withRaw(value.as[YScalar].text).withMediaType("application/xml")
        shape.withName(name)
        adopt(shape)
        shape
    }
  }

  protected def parseJSONSchemaExpression(name: String, value: YNode, adopt: Shape => Shape): Shape = {
    val (text, valueAST) = value.tagType match {
      case YType.Map =>
        val map = value.as[YMap]
        typeOrSchema(map) match {
          case Some(typeEntry: YMapEntry) if typeEntry.value.toOption[YScalar].isDefined =>
            (typeEntry.value.as[YScalar].text, typeEntry.value)
          case _ =>
            val shape = SchemaShape()
            adopt(shape)
            ctx.violation(shape.id, "Cannot parse XML Schema expression out of a non string value", value)
            ("", value)
        }
      case YType.Seq =>
        val shape = SchemaShape()
        adopt(shape)
        ctx.violation(shape.id, "Cannot parse XML Schema expression out of a non string value", value)
        ("", value)
      case _ => (value.as[YScalar].text, value)
    }

    val schemaAst = YamlParser(text)(ctx).withIncludeTag("!include").parse(keepTokens = true)
    val schemaEntry = schemaAst.head match {
      case d: YDocument => YMapEntry(name, d.node)
      case _ =>
        ctx.violation("invalid json schema expression", Some(valueAST))
        YMapEntry(name, YNode.Null)
    }

    OasTypeParser(schemaEntry, (shape) => adopt(shape))(toOas(ctx)).parse() match {
      case Some(shape) =>
        shape.annotations += ParsedJSONSchema(text)
        shape
      case None =>
        val shape = SchemaShape()
        adopt(shape)
        ctx.violation(shape.id, "Cannot parse JSON Schema", value)
        shape
    }
  }

  protected def typeOrSchema(map: YMap): Option[YMapEntry] = map.key("type").orElse(map.key("schema"))

}

sealed abstract class RamlTypeParser(ast: YPart,
                                     name: String,
                                     node: YNode,
                                     adopt: Shape => Shape,
                                     isAnnotation: Boolean,
                                     defaultType: DefaultType)(implicit val ctx: RamlWebApiContext)
    extends RamlSpecParser
    with RamlExternalTypes {

  def typeParser: (YPart, String, YNode, Shape => Shape, Boolean, DefaultType) => RamlTypeParser

  def parseDefaultType(defaultType: DefaultType): Shape = {
    val defaultShape = defaultType.typeDef match {
      case typeDef if typeDef.isScalar => parseScalarType(typeDef)
      case ObjectType                  => parseObjectType()
      case _                           => parseAnyType() // multiple matches, no disambiguation with default type => any
    }
    defaultShape.annotations += DefaultNode()
    defaultShape
  }

  def parse(): Option[Shape] = {

    val info: Option[TypeDef] =
      RamlTypeDetection(node,
                        "",
                        node.toOption[YMap].flatMap(m => m.key("(format)").map(_.value.toString())),
                        defaultType)
    val result = info.map {
      case XMLSchemaType                         => parseXMLSchemaExpression(name, node, adopt)
      case JSONSchemaType                        => parseJSONSchemaExpression(name, node, adopt)
      case TypeExpressionType                    => parseTypeExpression()
      case UnionType                             => parseUnionType()
      case ObjectType | FileType | UndefinedType => parseObjectType()
      case ArrayType                             => parseArrayType()
      case AnyType                               => parseAnyType()
      case typeDef if typeDef.isScalar           => parseScalarType(typeDef)
      case MultipleMatch =>
        parseDefaultType(defaultType) // Multiple match, we try to disambiguate using the default type info
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

    // parsing annotations
    node.value match {
      case map: YMap if result.isDefined => AnnotationParser(result.get, map).parse()
      case _                             => // ignore
    }

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
    ast match {
      case entry: YMapEntry if entry.value.value.isInstanceOf[YMap] =>
        AnyShapeParser(shape, entry.value.value.asInstanceOf[YMap]).parse()
      case map: YMap => AnyShapeParser(shape, map).parse()
      case _         => shape
    }
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
            case (text: String, Some(s)) =>
              s.link(text, Annotations(node.value))
                .asInstanceOf[Shape]
                .withName(name) // we setup the local reference in the name
                .withId(shape.id) // and the ID of the link at that position in the tree, not the ID of the linked element, tha goes in link-target
            case (text: String, _) if RamlTypeDefMatcher.matchType(text, default = UndefinedType) == ObjectType =>
              shape.annotations += ExplicitField()
              shape
            case (text: String, _) =>
              val shape = UnresolvedShape(text, node).withName(text)
              shape.withContext(ctx)
              adopt(shape)
              shape.unresolved(text, node)
              shape
          }
      }
    }
  }

  private def isFileType: Boolean = {
    node.to[YMap] match {
      case Right(map) =>
        typeOrSchema(map)
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
      map.key("pattern", (ScalarShapeModel.Pattern in shape).allowingAnnotations)
      map.key("minLength", (ScalarShapeModel.MinLength in shape).allowingAnnotations)
      map.key("maxLength", (ScalarShapeModel.MaxLength in shape).allowingAnnotations)
      map.key("(exclusiveMinimum)", ScalarShapeModel.ExclusiveMinimum in shape)
      map.key("(exclusiveMaximum)", ScalarShapeModel.ExclusiveMaximum in shape)
    }
  }

  case class AnyShapeParser(shape: AnyShape, map: YMap) extends ShapeParser {
    override def parse(): AnyShape = {
      super.parse()
      shape
    }
  }

  case class ScalarShapeParser(typeDef: TypeDef, shape: ScalarShape, map: YMap)
      extends ShapeParser
      with CommonScalarParsingLogic {

    override def parse(): ScalarShape = {
      super.parse()

      parseOASFields(map, shape)

      typeOrSchema(map)
        .fold(
          shape
            .set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), Annotations() += Inferred()))(
          entry => shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), Annotations(entry)))

      map.key("minimum", entry => { // todo pope
        val value = ScalarNode(entry.value)
        shape.set(ScalarShapeModel.Minimum, value.text(), Annotations(entry))
      })

      map.key("maximum", entry => { // todo pope
        val value = ScalarNode(entry.value)
        shape.set(ScalarShapeModel.Maximum, value.text(), Annotations(entry))
      })

      map.key("format", (ScalarShapeModel.Format in shape).allowingAnnotations)
      // We don't need to parse (format) extension because in oas must not be emitted, and in raml will be emitted.

      map.key("multipleOf", (ScalarShapeModel.MultipleOf in shape).allowingAnnotations)

      val syntaxType = Option(shape.dataType).getOrElse("#shape").split("#").last match {
        case "integer" | "float" => "numberScalarShape"
        case "string"            => "stringScalarShape"
        case "dateTime"          => "dateScalarShape"
        case _                   => "shape"
      }

      ctx.closedRamlTypeShape(shape, map, syntaxType, isAnnotation)

      // shape.set(ScalarShapeModel.Repeat, value = false) // 0.8 support, not exists in 1/.0, set default

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
                    typeParser(unionNode,
                               s"item$index",
                               unionNode,
                               item => item.adopted(shape.id + "/items/" + index),
                               isAnnotation,
                               AnyDefaultType).parse()
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

      map.key("fileTypes", FileShapeModel.FileTypes in shape)

      map.key("(minimum)", entry => { // todo pope
        val value = ScalarNode(entry.value)
        shape.set(ScalarShapeModel.Minimum, value.string(), Annotations(entry))
      })

      map.key("(maximum)", entry => { // todo pope
        val value = ScalarNode(entry.value)
        shape.set(ScalarShapeModel.Maximum, value.string(), Annotations(entry))
      })

      map.key("(format)", ScalarShapeModel.Format in shape)
      // We don't need to parse (format) extension because in oas must not be emitted, and in raml will be emitted.

      map.key("(multipleOf)", ScalarShapeModel.MultipleOf in shape)

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

      map.key("uniqueItems", (ArrayShapeModel.UniqueItems in shape).allowingAnnotations)
      map.key("(collectionFormat)", ArrayShapeModel.CollectionFormat in shape)

      val finalShape = (for {
        itemsEntry <- map.key("items")
        item <- Raml10TypeParser(itemsEntry, items => items.adopted(shape.id + "/items"), defaultType = defaultType)
          .parse()
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

      map.key("minItems", (ArrayShapeModel.MinItems in shape).allowingAnnotations)
      map.key("maxItems", (ArrayShapeModel.MaxItems in shape).allowingAnnotations)
      map.key("uniqueItems", (ArrayShapeModel.UniqueItems in shape).allowingAnnotations)

      map.key(
        "items",
        entry => {
          val items = entry.value
            .as[YMap]
            .entries
            .zipWithIndex
            .map {
              case (elem, index) =>
                Raml10TypeParser(elem, item => item.adopted(shape.id + "/items/" + index)).parse()
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
          val inherits: Seq[AmfElement] = entry.value.as[Seq[YNode]].map { node =>
            node.as[String] match {
              case RamlTypeDefMatcher.TypeExpression(s) =>
                RamlTypeExpressionParser(adopt, Some(node)).parse(s).get
              case s if wellKnownType(s) => parseWellKnownTypeRef(s)
              case s =>
                ctx.declarations.findType(s, SearchScope.All) match {
                  case Some(ancestor) => ancestor
                  case _              => unresolved(node)
                }
            }
          }
          shape.set(ShapeModel.Inherits, AmfArray(inherits, Annotations(entry.value)), Annotations(entry))

        case YType.Map =>
          Raml10TypeParser(entry, shape => shape.adopted(shape.id))
            .parse()
            .foreach(s =>
              shape.set(ShapeModel.Inherits, AmfArray(Seq(s), Annotations(entry.value)), Annotations(entry)))

        case _ if RamlTypeDefMatcher.TypeExpression.unapply(entry.value.as[YScalar].text).isDefined =>
          Raml10TypeParser(entry, shape => shape.adopted(shape.id))
            .parse()
            .foreach(s =>
              shape.set(ShapeModel.Inherits, AmfArray(Seq(s), Annotations(entry.value)), Annotations(entry)))

        case _ if !wellKnownType(entry.value.as[YScalar].text) =>
          val text = entry.value.as[YScalar].text
          // it might be a named type
          // only search for named ref, ex Person: !include. We dont handle inherits from an anonymous type like type: !include
          ctx.declarations.findType(text, SearchScope.All) match {
            case Some(ancestor) =>
              // set without ID!, we keep the ID of the referred element
              shape.fields.setWithoutId(ShapeModel.Inherits,
                                        AmfArray(Seq(ancestor), Annotations(entry.value)),
                                        Annotations(entry))
            case _ =>
              val u: UnresolvedShape = unresolved(entry.value)
              shape.set(ShapeModel.Inherits, AmfArray(Seq(u), Annotations(entry.value)), Annotations(entry))
          }

        case _ =>
          shape.add(ExplicitField()) // TODO store annotation in dataType field.
      }
    }

    private def unresolved(node: YNode): UnresolvedShape = {
      val reference = node.as[YScalar].text
      val shape     = UnresolvedShape(reference, node)
      shape.withContext(ctx)
      shape.unresolved(reference, node)
      adopt(shape)
      shape
    }
  }

  case class NodeShapeParser(shape: NodeShape, map: YMap) extends ShapeParser {
    override def parse(): NodeShape = {

      super.parse()

      map.key("minProperties", (NodeShapeModel.MinProperties in shape).allowingAnnotations)
      map.key("maxProperties", (NodeShapeModel.MaxProperties in shape).allowingAnnotations)

      shape.set(NodeShapeModel.Closed, value = false)
      map.key("additionalProperties", (NodeShapeModel.Closed in shape).negated.explicit)

      map.key("(additionalProperties)").foreach { entry =>
        OasTypeParser(entry, s => s.adopted(shape.id)).parse().foreach { s =>
          shape.set(NodeShapeModel.AdditionalPropertiesSchema, s, Annotations(entry))
        }
      }

      map.key("discriminator", (NodeShapeModel.Discriminator in shape).allowingAnnotations)
      map.key("discriminatorValue", (NodeShapeModel.DiscriminatorValue in shape).allowingAnnotations)

      map.key("(readOnly)", NodeShapeModel.ReadOnly in shape)

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
        .flatMap(entry => PropertyShapeParser(entry, producer).parse())
    }
  }

  case class PropertyShapeParser(entry: YMapEntry, producer: String => PropertyShape) {

    def parse(): Option[PropertyShape] = {

      entry.key.to[String] match {
        case Right(prop) =>
          val property = producer(prop).add(Annotations(entry))

          var explicitRequired: Option[Value] = None
          entry.value.to[YMap] match {
            case Right(map) =>
              map.key(
                "required",
                entry => {
                  val required = ScalarNode(entry.value).boolean().value.asInstanceOf[Boolean]
                  explicitRequired = Some(Value(AmfScalar(required), Annotations(entry) += ExplicitField()))
                  property.set(PropertyShapeModel.MinCount,
                               AmfScalar(if (required) 1 else 0),
                               Annotations(entry) += ExplicitField())
                }
              )
            case _ =>
          }

          if (property.fields.?(PropertyShapeModel.MinCount).isEmpty) {
            val required = !prop.endsWith("?")

            property.set(PropertyShapeModel.MinCount, if (required) 1 else 0)
            property.set(PropertyShapeModel.Name, if (required) prop else prop.stripSuffix("?")) // TODO property id is using a name that is not final.
          }

          property.set(PropertyShapeModel.Path, (Namespace.Data + entry.key.as[YScalar].text).iri())

          Raml10TypeParser(entry, shape => shape.adopted(property.id), isAnnotation = false, StringDefaultType)
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

          Some(property)

        case Left(error) =>
          ctx.violation(error.error, Some(entry.key))
          None
      }
    }
  }

  abstract class ShapeParser extends RamlTypeSyntax {

    val shape: Shape
    val map: YMap

    def parse(): Shape = {

      parseInheritance()

      map.key("displayName", (ShapeModel.DisplayName in shape).allowingAnnotations)
      map.key("description", (ShapeModel.Description in shape).allowingAnnotations)

      map.key(
        "default",
        entry => {
          entry.value.tagType match {
            case YType.Map | YType.Seq =>
              shape.set(ShapeModel.Default,
                        AmfScalar(YamlRender.render(entry.value), Annotations(entry.value)),
                        Annotations(entry))
            case _ =>
              val value = ScalarNode(entry.value)
              shape.set(ShapeModel.Default, value.text(), Annotations(entry))
          }
        }
      )

      map.key("enum", ShapeModel.Values in shape)
      map.key("minItems", (ArrayShapeModel.MinItems in shape).allowingAnnotations)
      map.key("maxItems", (ArrayShapeModel.MaxItems in shape).allowingAnnotations)
      map.key("(externalDocs)", AnyShapeModel.Documentation in shape using OasCreativeWorkParser.parse)

      map.key(
        "xml",
        entry => {
          val xmlSerializer: XMLSerializer =
            XMLSerializerParser(shape.name, entry.value.as[YMap]).parse()
          shape.set(AnyShapeModel.XMLSerialization, xmlSerializer, Annotations(entry))
        }
      )

      // Custom shape property definitions, not instances, those are parsed at the end of the parsing process
      map.key(
        "facets",
        entry => PropertiesParser(entry.value.as[YMap], shape.withCustomShapePropertyDefinition).parse()
      )

      val examples = RamlExamplesParser(map, "example", "examples").parse()
      if (examples.nonEmpty)
        shape.setArray(AnyShapeModel.Examples, examples)

      shape
    }

    protected def parseInheritance(): Unit = {
      typeOrSchema(map).foreach(entry => InheritanceParser(entry, shape).parse())
    }
  }

}
