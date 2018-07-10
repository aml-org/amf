package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.Root
import amf.core.annotations._
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{ScalarNode => DynamicDataNode, _}
import amf.core.parser.{Annotations, Value, _}
import amf.core.remote.Oas
import amf.core.utils.Strings
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.Shapes
import amf.plugins.document.webapi.annotations._
import amf.plugins.document.webapi.contexts.{
  Raml08WebApiContext,
  Raml10WebApiContext,
  RamlWebApiContext,
  WebApiContext,
  _
}
import amf.plugins.document.webapi.parser.RamlTypeDefMatcher
import amf.plugins.document.webapi.parser.RamlTypeDefMatcher.{JSONSchema, XMLSchema, matchType}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, ShapeExtensionParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.oas.Oas2DocumentParser
import amf.plugins.document.webapi.parser.spec.raml.{RamlSpecParser, RamlTypeExpressionParser}
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models.TypeDef._
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.parser.XsdTypeDefMapping
import amf.plugins.domain.webapi.annotations.TypePropertyLexicalInfo
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model.YNode.MutRef
import org.yaml.model.{YPart, _}
import org.yaml.parser.YamlParser
import org.yaml.render.YamlRender

import scala.collection.mutable
import scala.language.postfixOps

object Raml10TypeParser {
  def apply(entry: YMapEntry,
            adopt: Shape => Shape,
            isAnnotation: Boolean = false,
            defaultType: DefaultType = StringDefaultType)(implicit ctx: RamlWebApiContext): Raml10TypeParser =
    new Raml10TypeParser(Left(entry), entry.key.as[YScalar].text, adopt, isAnnotation, defaultType)(
      new Raml10WebApiContext(ctx.rootContextDocument, ctx.refs, ctx, Some(ctx.declarations)))

  def apply(node: YNode, name: String, adopt: Shape => Shape, defaultType: DefaultType)(
      implicit ctx: RamlWebApiContext): Raml10TypeParser =
    new Raml10TypeParser(Right(node), name, adopt, false, defaultType)
  def parse(adopt: Shape => Shape, isAnnotation: Boolean = false, defaultType: DefaultType = StringDefaultType)(
      node: YNode)(implicit ctx: RamlWebApiContext): Option[Shape] = {
    val head = node.as[YMap].entries.head
    apply(head, adopt, isAnnotation, defaultType).parse()
  }
}

trait ExampleParser {
  def parseExamples(shape: AnyShape, map: YMap, options: ExampleOptions = DefaultExampleOptions)(
      implicit ctx: WebApiContext): Unit = {
    val examples = RamlExamplesParser(map, "example", "examples", Option(shape.id), shape.withExample, options).parse()
    if (examples.nonEmpty)
      shape.setArray(AnyShapeModel.Examples, examples)
  }
}

trait RamlTypeSyntax {
  def parseWellKnownTypeRef(ramlType: String): Shape = {
    ramlType match {
      case "nil" | "" | "null" => NilShape()
      case "any"               => AnyShape()
      case "string"            => ScalarShape().withDataType((Namespace.Xsd + "string").iri())
      case "integer"           => ScalarShape().withDataType((Namespace.Xsd + "integer").iri())
      case "number"            => ScalarShape().withDataType((Namespace.Shapes + "number").iri())
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

case class Raml10TypeParser(entryOrNode: Either[YMapEntry, YNode],
                            name: String,
                            adopt: Shape => Shape,
                            isAnnotation: Boolean,
                            defaultType: DefaultType)(implicit override val ctx: RamlWebApiContext)
    extends RamlTypeParser(entryOrNode: Either[YMapEntry, YNode],
                           name: String,
                           adopt: Shape => Shape,
                           isAnnotation: Boolean,
                           defaultType: DefaultType) {
  override def typeParser: (Either[YMapEntry, YNode], String, Shape => Shape, Boolean, DefaultType) => RamlTypeParser =
    Raml10TypeParser.apply

}

object Raml08TypeParser {
  def apply(node: YNode, name: String, adopt: Shape => Shape, isAnnotation: Boolean, defaultType: DefaultType)(
      implicit ctx: RamlWebApiContext): Raml08TypeParser =
    new Raml08TypeParser(Right(node), name, adopt, isAnnotation, defaultType)(
      new Raml08WebApiContext(ctx.rootContextDocument, ctx.refs, ctx, Some(ctx.declarations)))

  def apply(entry: YMapEntry, adopt: Shape => Shape, isAnnotation: Boolean, defaultType: DefaultType)(
      implicit ctx: RamlWebApiContext): Raml08TypeParser =
    new Raml08TypeParser(Left(entry), entry.key.as[String], adopt, isAnnotation, defaultType)(
      new Raml08WebApiContext(ctx.rootContextDocument, ctx.refs, ctx, Some(ctx.declarations)))
}

case class Raml08TypeParser(entryOrNode: Either[YMapEntry, YNode],
                            name: String,
                            adopt: Shape => Shape,
                            isAnnotation: Boolean,
                            defaultType: DefaultType)(implicit override val ctx: RamlWebApiContext)
    extends RamlTypeParser(entryOrNode: Either[YMapEntry, YNode],
                           name: String,
                           adopt: Shape => Shape,
                           isAnnotation,
                           defaultType) {

  override def parse(): Option[AnyShape] = {
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

            maybeShape.map(s => {

              val inherits = s.meta.modelInstance.withName("inherits")
              adopt(inherits)

              RamlSingleExampleParser("example",
                                      map,
                                      inherits.withExample,
                                      ExampleOptions(strictDefault = true, quiet = true))
                .parse()
                .fold(s)(e => {
                  inherits.set(ShapeModel.Inherits, AmfArray(Seq(s)))
                  inherits.setArray(ScalarShapeModel.Examples, Seq(e))
                })
            })
          })
      case YType.Seq =>
        Option(
          Raml08UnionTypeParser(UnionShape(node).withName(name).adopted(shape.id), node.as[Seq[YNode]], node).parse())
      case _ => Raml08TextParser(node, adopt, name, defaultType).parse()
    }
  }

  override def typeParser: (Either[YMapEntry, YNode], String, Shape => Shape, Boolean, DefaultType) => RamlTypeParser =
    Raml08TypeParser.apply

  case class Raml08ReferenceParser(text: String, node: YNode, name: String)(implicit ctx: RamlWebApiContext) {
    def parse(): Some[AnyShape] = {
      val shape: AnyShape = ctx.declarations.findType(text, SearchScope.All) match {
        case Some(s: AnyShape) => s.link(text, Annotations(node.value)).asInstanceOf[AnyShape].withName(name)
        case None =>
          val shape = UnresolvedShape(text, node).withName(text)
          shape.withContext(ctx)
          adopt(shape)
          if (!text.validReferencePath) {
            ctx.violation(
              ParserSideValidations.ChainedReferenceSpecification.id,
              shape.id,
              s"Chained reference '$text",
              node
            )
          } else {
            shape.unresolved(text, node)
          }
          shape
      }
      Some(shape)
    }
  }

  case class Raml08TextParser(value: YNode, adopt: Shape => Shape, name: String, defaultType: DefaultType)(
      implicit ctx: RamlWebApiContext) {
    def parse(): Option[AnyShape] = {
      value.tagType match {
        case YType.Null =>
          Raml08DefaultTypeParser(defaultType.typeDef, name, value, adopt)
            .parse()
            .map(s => s.add(SourceAST(value)).add(SourceLocation(value.sourceName)))
        case _ =>
          value.as[YScalar].text match {
            case XMLSchema(_)  => Option(RamlXmlSchemaExpression(name, value, adopt).parse())
            case JSONSchema(_) => Option(RamlJsonSchemaExpression(name, value, adopt).parse())
            case t if RamlTypeDefMatcher.match08Type(t).isDefined =>
              Option(
                SimpleTypeParser(name, adopt, YMap.empty, defaultType = RamlTypeDefMatcher.match08Type(t).get).parse())
            case t => Raml08ReferenceParser(t, node, name).parse()
          }
      }
    }
  }

  case class Raml08SchemaParser(map: YMap, adopt: Shape => Shape)(implicit ctx: RamlWebApiContext) {
    def parse(): Option[AnyShape] = {
      map.key("schema").flatMap { e =>
        e.value.tagType match {
          case YType.Map | YType.Seq =>
            Raml08TypeParser(e, adopt, isAnnotation = false, StringDefaultType).parse()
          case _ => Raml08TextParser(e.value, adopt, "schema", defaultType).parse()
        }
      }
    }
  }

}

case class Raml08DefaultTypeParser(defaultType: TypeDef, name: String, ast: YPart, adopt: Shape => Shape)(
    implicit ctx: RamlWebApiContext) {
  def parse(): Option[AnyShape] = {
    val product: Option[AnyShape] = defaultType match {
      case NilType =>
        Some(NilShape().withName(name).add(Inferred()))
      case StrType =>
        Some(ScalarShape()
          .withName(name)
          .set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(defaultType)), Annotations() += Inferred()))
      case AnyType =>
        Some(AnyShape().withName(name).add(Inferred()))
      case _ =>
        // TODO get parent id
        ctx.violation(s"Cannot set default type $defaultType in raml 08", ast)
        None
    }
    product.map(adopt)
    product
  }
}

case class Raml08UnionTypeParser(shape: UnionShape, types: Seq[YNode], ast: YPart)(implicit ctx: RamlWebApiContext) {
  def parse(): UnionShape = {

    val unionNodes = types.zipWithIndex
      .map {
        case (unionNode, index) =>
          Raml08TypeParser(unionNode,
                           s"item$index",
                           item => item.adopted(shape.id + "/items/" + index),
                           isAnnotation = false,
                           defaultType = AnyDefaultType).parse()
      }
      .filter(_.isDefined)
      .map(_.get)

    shape.setArray(UnionShapeModel.AnyOf, unionNodes, Annotations(ast))

  }
}

case class SimpleTypeParser(name: String, adopt: Shape => Shape, map: YMap, defaultType: TypeDef)(
    implicit val ctx: RamlWebApiContext)
    extends SpecParserOps {

  def parse(): AnyShape = {

    if (map.key("repeat").exists(entry => entry.value.as[Boolean])) {
      val shape = ArrayShape(map).withName(name)
      adopt(shape)
      val items =
        SimpleTypeParser(
          "items",
          (s: Shape) => s.adopted(shape.id),
          YMap(map.entries.filter(entry => !entry.key.as[YScalar].text.equals("repeat")),
               map.entries.headOption.map(_.sourceName).getOrElse("")),
          defaultType
        ).parse()
      shape.withItems(items)
      shape
    } else {
      val shape: AnyShape = map
        .key("type")
        .flatMap(e => {
          e.value.tagType match {
            case YType.Null => None
            case _          => e.value.toOption[YScalar]
          }
        })
        .fold(Raml08DefaultTypeParser(defaultType, name, map, adopt).parse())(value => {
          XsdTypeDefMapping.xsdFromString(value.text) match {
            case (Some(iri: String), format: Option[String])
                if iri.equals((Shapes + "file").iri()) => // handle file type in 08 as FileShape for compatibility
              // (Applicable only to Form properties) ???
              val shape = FileShape(value)
              Some(shape.withName(name))
            case (Some(iri: String), format: Option[String]) =>
              val shape = ScalarShape(value).set(ScalarShapeModel.DataType, AmfScalar(iri), Annotations(value))
              format.foreach { f =>
                if (f != "") shape.set(ScalarShapeModel.Format, AmfScalar(f), Annotations())
              }
              Some(shape.withName(name))
            case _ =>
              ctx.violation(s"Invalid type def ${value.text} for raml 08", value)
              None
          }
        })
        .getOrElse(ScalarShape(map).withDataType((Namespace.Xsd + "string").iri()).withName(name))

      map.key("type", e => { shape.annotations += TypePropertyLexicalInfo(Range(e.key.range)) })

      adopt(shape)
      parseMap(shape)
      val str = shape match {
        case scalar: ScalarShape => scalar.dataType.value()
        case _                   => "#shape"
      }

      val syntaxType = str.split("#").last match {
        case "integer" | "float" | "double" | "long" | "number" => "numberScalarShape"
        case "string"                                           => "stringScalarShape"
        case "dateTime"                                         => "dateScalarShape"
        case _                                                  => "shape"
      }

      ctx.closedShape(shape.id, map, syntaxType)
      shape
    }
  }

  private def parseMap(shape: AnyShape): Unit = {

    map.key("displayName", ShapeModel.DisplayName in shape)
    map.key("description", ShapeModel.Description in shape)
    map.key("enum", ShapeModel.Values in shape)
    map.key(
      "pattern",
      entry => {
        var regex = entry.value.as[String]
        if (!regex.startsWith("^")) regex = "^" + regex
        if (!regex.endsWith("$")) regex = regex + "$"
        shape.set(ScalarShapeModel.Pattern, ScalarNode(regex).text(), Annotations(entry))
      }
    )

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

    RamlSingleExampleParser("example", map, shape.withExample, ExampleOptions(strictDefault = true, quiet = true))
      .parse()
      .foreach(e => shape.setArray(ScalarShapeModel.Examples, Seq(e)))

    map.key(
      "default",
      entry => {
        NodeDataNodeParser(entry.value, shape.id, quiet = false).parse().dataNode.foreach { dn =>
          shape.set(ShapeModel.Default, dn, Annotations(entry))
        }
        val str = YamlRender.render(entry.value)
        shape.set(ShapeModel.DefaultValueString, AmfScalar(str), Annotations(entry))
      }
    )
  }
}

sealed abstract class RamlTypeParser(entryOrNode: Either[YMapEntry, YNode],
                                     name: String,
                                     adopt: Shape => Shape,
                                     isAnnotation: Boolean,
                                     defaultType: DefaultType)(implicit val ctx: RamlWebApiContext)
    extends RamlSpecParser {

  protected val (ast, node) = entryOrNode match {
    case Right(n) => (n, n)
    case Left(e)  => (e, e.value)
  }

  def typeParser: (Either[YMapEntry, YNode], String, Shape => Shape, Boolean, DefaultType) => RamlTypeParser

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
      RamlTypeDetection(
        node,
        "",
        node
          .toOption[YMap]
          .flatMap { m =>
            m.key("format").orElse(m.key("format".asRamlAnnotation)).map(_.value.toString())
          },
        defaultType)
    val result = info.map {
      case XMLSchemaType                         => RamlXmlSchemaExpression(name, node, adopt, parseExample = true).parse()
      case JSONSchemaType                        => RamlJsonSchemaExpression(name, node, adopt, parseExample = true).parse()
      case NilUnionType                          => parseNilUnion()
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
      .foreach(shape =>
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

  private def parseNilUnion() = {
    val union = UnionShape().withName(name)
    adopt(union)

    val parsed = node.value match {
      case s: YScalar =>
        val toParse = YMapEntry(YNode(""), YNode(s.text.replace("?", "")))
        ctx.factory.typeParser(toParse, s => s.withId(union.id), isAnnotation, defaultType).parse().get
      case m: YMap =>
        val newEntries = m.entries.map { entry =>
          if (entry.key.as[String] == "type") {
            YMapEntry("type", entry.value.as[String].replace("?", ""))
          } else {
            entry
          }
        }

        val toParse = YMapEntry(YNode(""), YMap(newEntries, newEntries.headOption.map(_.sourceName).getOrElse("")))
        ctx.factory.typeParser(toParse, s => s.withId(union.id), isAnnotation, defaultType).parse().get
    }
    union.withAnyOf(
      Seq(
        parsed,
        NilShape().withId(union.id)
      )
    )
  }

  // These are the actual custom facets, just regular properties in the AST map that have been
  // defined through the 'facets' properties in this shape or in base shape.
  // The shape definitions are parsed in the common parser of all shapes, not here.
  private def parseCustomShapeFacetInstances(shapeResult: Option[Shape]): Unit = {
    node.value match {
      case map: YMap if shapeResult.isDefined => ShapeExtensionParser(shapeResult.get, map, ctx, isAnnotation).parse()
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
        AnyTypeShapeParser(shape, entry.value.value.asInstanceOf[YMap]).parse()
//      case map: YMap => AnyTypeShapeParser(shape, map).parse()
      case _ => shape
    }
  }

  def parseArrayType(): Shape = {
    val shape = node.to[YMap] match {
      case Right(map) => DataArrangementParser(name, ast, map, (shape: Shape) => adopt(shape)).parse()
      case Left(_)    => adopt(ArrayShape(ast).withName(name))
    }
    shape
  }

  private def parseUnionType(): UnionShape = {
    UnionShapeParser(node.as[YMap], adopt).parse()
  }

  private def parseObjectType(): Shape = {
    if (isFileType) {
      val shape = node.tagType match {
        case YType.Str =>
          FileShape()
        case YType.Map =>
          FileShapeParser(node).parse()
      }
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
          InheritanceParser(ast.asInstanceOf[YMapEntry], shape, None).parse()
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
              s.link(text, Annotations.valueNode(node))
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
              if (!text.validReferencePath) {
                ctx.violation(
                  ParserSideValidations.ChainedReferenceSpecification.id,
                  shape.id,
                  s"Chained reference '$text",
                  node
                )
              } else {
                shape.unresolved(text, node)
              }
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
          } || map.key("fileTypes").isDefined
      case Left(_) =>
        node.to[YScalar] match {
          case Right(scalar) =>
            scalar.text == "file"
          case _ => false
        }
    }
  }

  trait CommonScalarParsingLogic {
    def parseOASFields(map: YMap, shape: Shape): Unit = {
      map.key(
        "pattern",
        entry => {
          var regex = entry.value.as[String]
          if (!regex.startsWith("^")) regex = "^" + regex
          if (!regex.endsWith("$")) regex = regex + "$"
          shape.set(ScalarShapeModel.Pattern, ScalarNode(regex).text(), Annotations(entry))
        }
      )
      map.key("minLength", (ScalarShapeModel.MinLength in shape).allowingAnnotations)
      map.key("maxLength", (ScalarShapeModel.MaxLength in shape).allowingAnnotations)
      map.key("exclusiveMinimum".asRamlAnnotation, ScalarShapeModel.ExclusiveMinimum in shape)
      map.key("exclusiveMaximum".asRamlAnnotation, ScalarShapeModel.ExclusiveMaximum in shape)
    }
  }

  case class AnyTypeShapeParser(shape: AnyShape, map: YMap) extends AnyShapeParser {

    override val options: ExampleOptions = ExampleOptions(strictDefault = true, quiet = true)

    override def parse(): AnyShape = {

      super.parse()
    }
  }

  abstract class AnyShapeParser() extends ShapeParser with ExampleParser {

    override val shape: AnyShape
    val options: ExampleOptions = DefaultExampleOptions

    override def parse(): AnyShape = {
      super.parse()
      parseExamples(shape, map, options)
      shape
    }

  }

  case class ScalarShapeParser(typeDef: TypeDef, shape: ScalarShape, map: YMap)
      extends AnyShapeParser
      with CommonScalarParsingLogic {

    override def parse(): ScalarShape = {
      super.parse()

      parseOASFields(map, shape)

      typeOrSchema(map)
        .fold(
          shape
            .set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), Annotations() += Inferred()))(
          entry => shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), Annotations(entry)))

      map.key(
        "minimum",
        entry => { // todo pope
          val value = ScalarNode(entry.value)
          ensurePrecision(shape.dataType.option(), entry.value.toString(), entry.value)
          shape.set(ScalarShapeModel.Minimum, value.text(), Annotations(entry))
        }
      )

      map.key(
        "maximum",
        entry => { // todo pope
          val value = ScalarNode(entry.value)
          ensurePrecision(shape.dataType.option(), entry.value.toString(), entry.value)
          shape.set(ScalarShapeModel.Maximum, value.text(), Annotations(entry))
        }
      )

      map.key("format", (ScalarShapeModel.Format in shape).allowingAnnotations)
      // We don't need to parse (format) extension because in oas must not be emitted, and in raml will be emitted.

      map.key(
        "multipleOf",
        entry => { // todo pope
          val value = ScalarNode(entry.value)
          ensurePrecision(shape.dataType.option(), entry.value.toString(), entry.value)
          shape.set(ScalarShapeModel.MultipleOf, value.text(), Annotations(entry))
        }
      )

      // shape.set(ScalarShapeModel.Repeat, value = false) // 0.8 support, not exists in 1/.0, set default

      shape
    }

    protected def ensurePrecision(dataType: Option[String], value: String, ast: YNode): Unit = {
      if (dataType.isDefined && dataType.get.endsWith("#integer")) {
        if (value.contains(".")) {
          ctx.violation(
            ParserSideValidations.ParsingErrorSpecification.id,
            shape.id,
            "Wrong precision for integer numeric facet value",
            ast
          )
        }
      }
    }
  }

  case class UnionShapeParser(override val map: YMap, adopt: Shape => Shape) extends AnyShapeParser {
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
                    typeParser(Right(unionNode),
                               s"item$index",
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

      shape
    }
  }

  case class OrConstraintParser(map: YMap, shape: Shape) {

    def parse(): Unit = {
      map.key(
        "(amf-or)", { entry =>
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val nodes = seq.zipWithIndex
                .map {
                  case (unionNode, index) =>
                    typeParser(Right(unionNode),
                               s"item$index",
                               item => item.adopted(shape.id + "/or/" + index),
                               isAnnotation,
                               AnyDefaultType).parse()
                }
                .filter(_.isDefined)
                .map(_.get)
              shape.setArray(ShapeModel.Or, nodes, Annotations(entry.value))

            case _ =>
              ctx.violation(shape.id, "Or constraints are built from multiple shape nodes", entry)
          }
        }
      )
    }
  }

  case class AndConstraintParser(map: YMap, shape: Shape) {

    def parse(): Unit = {
      map.key(
        "(amf-and)", { entry =>
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val nodes = seq.zipWithIndex
                .map {
                  case (unionNode, index) =>
                    typeParser(Right(unionNode),
                               s"item$index",
                               item => item.adopted(shape.id + "/and/" + index),
                               isAnnotation,
                               AnyDefaultType).parse()
                }
                .filter(_.isDefined)
                .map(_.get)
              shape.setArray(ShapeModel.And, nodes, Annotations(entry.value))

            case _ =>
              ctx.violation(shape.id, "And constraints are built from multiple shape nodes", entry)
          }
        }
      )
    }
  }

  case class XoneConstraintParser(map: YMap, shape: Shape) {

    def parse(): Unit = {
      adopt(shape)

      map.key(
        "(amf-xor)", { entry =>
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val nodes = seq.zipWithIndex
                .map {
                  case (unionNode, index) =>
                    typeParser(Right(unionNode),
                               s"item$index",
                               item => item.adopted(shape.id + "/xor/" + index),
                               isAnnotation,
                               AnyDefaultType).parse()
                }
                .filter(_.isDefined)
                .map(_.get)
              shape.setArray(ShapeModel.Xone, nodes, Annotations(entry.value))

            case _ =>
              ctx.violation(shape.id, "Xone constraints are built from multiple shape nodes", entry)
          }
        }
      )
    }
  }

  case class NotConstraintParser(map: YMap, shape: Shape) {

    def parse(): Unit = {
      map.key(
        "(amf-not)", { entry =>
          typeParser(Left(entry), "not", (s: Shape) => s.withId(shape.id + "/not"), false, defaultType).parse() match {
            case Some(negated) => shape.set(ShapeModel.Not, negated, Annotations(entry.value))
            case _             => // ignore
          }
        }
      )
    }
  }

  case class FileShapeParser(node: YNode) extends AnyShapeParser with CommonScalarParsingLogic {
    override val map: YMap = node.as[YMap]
    override val shape     = FileShape(Annotations(map))

    override def parse(): FileShape = {
      super.parse()
      parseOASFields(map, shape)

      map.key("fileTypes") match {
        case Some(entry) if entry.value.tagType == YType.Seq =>
          shape.setArray(FileShapeModel.FileTypes, entry.value.as[YSequence].nodes.map { n: YNode =>
            AmfScalar(n.as[YScalar].text)
          }, Annotations(entry.value))
        case Some(entry) if entry.value.tagType == YType.Str =>
          shape.setArray(FileShapeModel.FileTypes,
                         Seq(AmfScalar(entry.value.as[YScalar].text)),
                         Annotations(entry.value))
        case Some(entry) =>
          ctx.violation(
            ParserSideValidations.ParsingErrorSpecification.id,
            shape.id,
            Some(FileShapeModel.FileTypes.value.iri()),
            s"Unexpected syntax for the fileTypes property: ${entry.value.tagType}",
            entry.value
          )
        case _ => // ignore
      }

      map.key("minimum".asRamlAnnotation, entry => { // todo pope
        val value = ScalarNode(entry.value)
        shape.set(ScalarShapeModel.Minimum, value.text(), Annotations(entry))
      })

      map.key("maximum".asRamlAnnotation, entry => { // todo pope
        val value = ScalarNode(entry.value)
        shape.set(ScalarShapeModel.Maximum, value.text(), Annotations(entry))
      })

      map.key("format".asRamlAnnotation, ScalarShapeModel.Format in shape)
      // We don't need to parse (format) extension because in oas must not be emitted, and in raml will be emitted.

      map.key("multipleOf", entry => { // todo pope
        val value = ScalarNode(entry.value)
        shape.set(ScalarShapeModel.MultipleOf, value.text(), Annotations(entry))
      })

      shape
    }
  }

  case class DataArrangementParser(name: String, ast: YPart, map: YMap, adopt: Shape => Unit) {

    def lookAhead(): Either[TupleShape, ArrayShape] = {
      map.key("tuple".asRamlAnnotation) match {
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

  case class ArrayShapeParser(override val shape: ArrayShape, map: YMap, adopt: Shape => Unit) extends AnyShapeParser {

    override def parse(): AnyShape = {
      adopt(shape)

      super.parse()

      map.key("uniqueItems", (ArrayShapeModel.UniqueItems in shape).allowingAnnotations)
      map.key("collectionFormat".asRamlAnnotation, ArrayShapeModel.CollectionFormat in shape)

      val finalShape = (for {
        itemsEntry <- map.key("items")
        item <- Raml10TypeParser(itemsEntry, items => items.adopted(shape.id), defaultType = defaultType)
          .parse()
      } yield {
        item match {
          case array: ArrayShape   => shape.withItems(array).toMatrixShape
          case matrix: MatrixShape => shape.withItems(matrix).toMatrixShape
          case other: Shape        => shape.withItems(other)
        }
      }).orElse(arrayShapeTypeFromInherits()).orElse(Some(shape))

      finalShape match {
        case Some(parsed: AnyShape) =>
          adopt(parsed)
          parsed
        case _ =>
          ctx.violation(shape.id, "Cannot parse data arrangement shape", map)
          shape
      }
    }

    private def arrayShapeTypeFromInherits(): Option[Shape] = {
      val maybeShape = shape.inherits.headOption.map {
        case matrix: MatrixShape => matrix.items
        case tuple: TupleShape   => tuple.items.head
        case array: ArrayShape   => array.items
        case shape: Shape        => shape
      }
      maybeShape match {
        case Some(_: ArrayShape)  => Some(shape.toMatrixShape)
        case Some(_: MatrixShape) => Some(shape.toMatrixShape)
        case Some(_: TupleShape)  => Some(shape.toMatrixShape)
        case Some(_: Shape)       => Some(shape)
        case _                    => None
      }
    }
  }

  case class TupleShapeParser(shape: TupleShape, map: YMap, adopt: Shape => Unit) extends AnyShapeParser {

    override def parse(): TupleShape = {
      adopt(shape)

      super.parse()

      parseInheritance()

      map.key("minItems", (ArrayShapeModel.MinItems in shape).allowingAnnotations)
      map.key("maxItems", (ArrayShapeModel.MaxItems in shape).allowingAnnotations)
      map.key("uniqueItems", (ArrayShapeModel.UniqueItems in shape).allowingAnnotations)

      val itemsField = map.key("items")
      itemsField match {
        case None => // ignore
        case Some(entry) =>
          val items = entry.value
            .as[YSequence]
            .nodes
            .collect { case n if n.tagType == YType.Map => n }
            .zipWithIndex
            .map {
              case (elem, index) =>
                Raml10TypeParser(
                  elem.as[YMap],
                  s"member$index",
                  item => item.adopted(shape.id + "/items/" + index),
                  defaultType = AnyDefaultType
                ).parse()
            }
          shape.withItems(items.filter(_.isDefined).map(_.get))
      }

      shape
    }
  }

  case class InheritanceParser(entry: YMapEntry, shape: Shape, fatherMap: Option[YMap])(
      implicit val ctx: RamlWebApiContext)
      extends RamlSpecParser
      with RamlTypeSyntax {

    def parse(): Unit = {
      entry.value.tagType match {

        case YType.Seq =>
          val inherits: Seq[AmfElement] = entry.value.as[Seq[YNode]].map { node =>
            node.as[String] match {
              case RamlTypeDefMatcher.TypeExpression(s) =>
                RamlTypeExpressionParser(adopt, Some(node)).parse(s).get
              case s if wellKnownType(s) =>
                parseWellKnownTypeRef(s).withName(s).adopted(shape.id)
              case s =>
                ctx.declarations.findType(s, SearchScope.All) match {
                  case Some(ancestor) => ancestor
                  case _              => unresolved(node)
                }
            }
          }
          shape.fields.setWithoutId(ShapeModel.Inherits,
                                    AmfArray(inherits, Annotations(entry.value)),
                                    Annotations(entry))
        case YType.Map =>
          Raml10TypeParser(entry, s => s.adopted(shape.id))
            .parse()
            .foreach(s =>
              shape.set(ShapeModel.Inherits, AmfArray(Seq(s), Annotations(entry.value)), Annotations(entry)))

        case _ if RamlTypeDefMatcher.TypeExpression.unapply(entry.value.as[YScalar].text).isDefined =>
          Raml10TypeParser(entry, s => s.adopted(shape.id))
            .parse()
            .foreach(s =>
              shape.set(ShapeModel.Inherits, AmfArray(Seq(s), Annotations(entry.value)), Annotations(entry)))

        case YType.Str if XMLSchema.unapply(entry.value).isDefined =>
          val parsed =
            RamlXmlSchemaExpression("schema",
                                    entry.value,
                                    xmlSchemaShape => xmlSchemaShape.withId(shape.id + "/xmlSchema")).parse()
          shape.set(ShapeModel.Inherits, AmfArray(Seq(parsed), Annotations(entry.value)), Annotations(entry))

        case _ if !wellKnownType(entry.value.as[YScalar].text) =>
          val text = entry.value.as[YScalar].text
          // it might be a named type
          // only search for named ref, ex Person: !include. We dont handle inherits from an anonymous type like type: !include
          ctx.declarations.findType(text, SearchScope.All) match {
            case Some(ancestor) =>
              // set without ID!, we keep the ID of the referred element
              shape.fields.setWithoutId(
                ShapeModel.Inherits,
                AmfArray(
                  Seq(
                    ancestor
                      .link(text, Annotations(entry.value))
                      .asInstanceOf[AnyShape]
                      .withName(ancestor.name.option().getOrElse("schema"))),
                  Annotations(entry.value)
                ),
                Annotations(entry)
              )
            case _ =>
              val baseClass = text match {
                case JSONSchema(_) =>
                  ctx.warning(
                    ParserSideValidations.JsonSchemaInheratinaceWarningSpecification.id,
                    shape.id,
                    Some(ShapeModel.Inherits.value.iri()),
                    "Inheritance from JSON Schema",
                    entry.value
                  )
                  RamlJsonSchemaExpression("schema",
                                           entry.value,
                                           jsonSchemaShape => jsonSchemaShape.withId(shape.id + "/jsonSchema")).parse()
                case _ =>
                  unresolved(entry.value)
              }
              shape.set(ShapeModel.Inherits, AmfArray(Seq(baseClass), Annotations(entry.value)), Annotations(entry))
          }

        case _ =>
          shape.add(ExplicitField()) // TODO store annotation in dataType field.
      }
    }

    private def unresolved(node: YNode): UnresolvedShape = {
      val reference = node.as[YScalar].text
      // we need to pass the extension parser to the unresolved, in order to not only have the father at the moment of resolve the future ref in Value, also we need the parser itself, for modular dependency (We cannot create an instance of this parser in core)
      val unresolvedShape = UnresolvedShape(reference, node, fatherMap.map(ShapeExtensionParser(shape, _, ctx)))
      unresolvedShape.withContext(ctx)
      if (!reference.validReferencePath) {
        ctx.violation(
          ParserSideValidations.ChainedReferenceSpecification.id,
          unresolvedShape.id,
          s"Chained reference '$reference",
          node
        )
      } else {
        unresolvedShape.unresolved(reference, node)
      }
      adopt(unresolvedShape)
      unresolvedShape
    }
  }

  case class NodeShapeParser(shape: NodeShape, map: YMap) extends AnyShapeParser {
    override def parse(): AnyShape = {

      super.parse()

      checkExtendedUnionDiscriminator()

      map.key("minProperties", (NodeShapeModel.MinProperties in shape).allowingAnnotations)
      map.key("maxProperties", (NodeShapeModel.MaxProperties in shape).allowingAnnotations)

      shape.set(NodeShapeModel.Closed, value = false)
      map.key("additionalProperties", (NodeShapeModel.Closed in shape).negated.explicit)

      map.key("additionalProperties".asRamlAnnotation).foreach { entry =>
        OasTypeParser(entry, s => s.adopted(shape.id)).parse().foreach { s =>
          shape.set(NodeShapeModel.AdditionalPropertiesSchema, s, Annotations(entry))
        }
      }

      map.key("discriminator", (NodeShapeModel.Discriminator in shape).allowingAnnotations)
      map.key("discriminatorValue", (NodeShapeModel.DiscriminatorValue in shape).allowingAnnotations)

      map.key(
        "properties",
        entry => {
          entry.value.toOption[YMap] match {
            case Some(m) =>
              val properties: Seq[PropertyShape] =
                PropertiesParser(m, shape.withProperty).parse()
              if (properties.exists(_.patternName.nonEmpty) && shape.closed.option().getOrElse(false)) {
                ctx.violation(
                  ParserSideValidations.PatternPropertiesOnClosedNodeSpecification.id,
                  shape.id,
                  s"Node without additional properties support cannot have pattern properties",
                  node
                )
              }
              shape.set(NodeShapeModel.Properties, AmfArray(properties, Annotations(entry.value)), Annotations(entry))
            case _ => // Empty properties node.
          }
        }
      )

      val properties = mutable.ListMap[String, PropertyShape]()
      shape.properties.foreach(p => properties += (p.name.value() -> p))

      map.key(
        "dependencies".asRamlAnnotation,
        entry => {
          val dependencies: Seq[PropertyDependencies] =
            ShapeDependenciesParser(entry.value.as[YMap], properties).parse()
          shape.set(NodeShapeModel.Dependencies, AmfArray(dependencies, Annotations(entry.value)), Annotations(entry))
        }
      )

      shape
    }

    def checkExtendedUnionDiscriminator(): Unit = {
      if (shape.inherits.length == 1 && shape.inherits.head.isInstanceOf[UnionShape]) {
        if (map.key("discriminator").isDefined) {
          ctx.violation(
            ParserSideValidations.DiscriminatorOnExtendedUnionSpecification.id,
            "Property discriminator forbidden in a node extending a unionShape",
            shape.id,
            map.key("discriminator").get
          )
        }

        if (map.key("discriminatorValue").isDefined) {
          ctx.violation(
            ParserSideValidations.DiscriminatorOnExtendedUnionSpecification.id,
            "Property discriminatorValue forbidden in a node extending a unionShape",
            shape.id,
            map.key("discriminatorValue").get
          )
        }
      }
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

          // we detect pattern properties here
          if (prop.startsWith("/") && prop.endsWith("/")) {
            if (prop == "//") {
              property.withPatternName("^.*$")
            } else {
              property.withPatternName(prop.drop(1).dropRight(1))
            }
          }

          // var explicitRequired: Option[Value] = None
          entry.value.toOption[YMap] match {
            case Some(map) =>
              map.key(
                "required",
                entry => {
                  val required = ScalarNode(entry.value).boolean().value.asInstanceOf[Boolean]
                  //explicitRequired = Some(Value(AmfScalar(required), Annotations(entry) += ExplicitField()))
                  property.set(PropertyShapeModel.MinCount,
                               AmfScalar(if (required) 1 else 0),
                               Annotations(entry) += ExplicitField())
                }
              )

              map.key("readOnly".asRamlAnnotation, PropertyShapeModel.ReadOnly in property)
            case _ =>
          }

          property.set(PropertyShapeModel.Path,
                       (Namespace.Data + entry.key.as[YScalar].text.urlComponentEncoded).iri())

          if (property.fields.?(PropertyShapeModel.MinCount).isEmpty) {
            val required = !prop.endsWith("?")

            property.set(PropertyShapeModel.MinCount, if (required) 1 else 0)
            property.set(
              PropertyShapeModel.Name,
              if (required) prop else prop.stripSuffix("?").stripPrefix("/").stripSuffix("/")) // TODO property id is using a name that is not final.
            property.set(PropertyShapeModel.Path, (Namespace.Data + entry.key.as[YScalar].text.stripSuffix("?")).iri())
          }

          Raml10TypeParser(entry, shape => shape.adopted(property.id), isAnnotation = false, StringDefaultType)
            .parse()
            .foreach { range =>
              if (entry.value.tagType == YType.Null) {
                range.annotations += SynthesizedField()
              }

              property.set(PropertyShapeModel.Range, range)
            }

          Some(property)

        case Left(error) =>
          // TODO get parent id
          ctx.violation(error.error, entry.key)
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
          val dataNodeResult = NodeDataNodeParser(entry.value, shape.id, quiet = false).parse()
          val str            = YamlRender.render(entry.value)
          shape.set(ShapeModel.DefaultValueString, AmfScalar(str), Annotations(entry))
          dataNodeResult.dataNode.foreach { dataNode =>
            shape.set(ShapeModel.Default, dataNode, Annotations(entry))
          }
        }
      )

      map.key("enum", ShapeModel.Values in shape)
      map.key("minItems", (ArrayShapeModel.MinItems in shape).allowingAnnotations)
      map.key("maxItems", (ArrayShapeModel.MaxItems in shape).allowingAnnotations)
      map.key("externalDocs".asRamlAnnotation, AnyShapeModel.Documentation in shape using OasCreativeWorkParser.parse)

      map.key(
        "xml",
        entry => {
          val xmlSerializer: XMLSerializer =
            XMLSerializerParser(shape.name.value(), entry.value).parse()
          shape.set(AnyShapeModel.XMLSerialization, xmlSerializer, Annotations(entry))
        }
      )

      // Logical constraints
      if (map.key("(amf-or)").isDefined) OrConstraintParser(map, shape).parse()
      if (map.key("(amf-and)").isDefined) AndConstraintParser(map, shape).parse()
      if (map.key("(amf-xone)").isDefined) XoneConstraintParser(map, shape).parse()
      if (map.key("(amf-not)").isDefined) NotConstraintParser(map, shape).parse()

      // Custom shape property definitions, not instances, those are parsed at the end of the parsing process
      map.key(
        "facets",
        entry => PropertiesParser(entry.value.as[YMap], shape.withCustomShapePropertyDefinition).parse()
      )

      // Explicit annotation for the type property
      map.key("type", entry => shape.annotations += TypePropertyLexicalInfo(Range(entry.key.range)))

      shape
    }

    protected def parseInheritance(): Unit = {
      typeOrSchema(map).foreach(entry => InheritanceParser(entry, shape, Some(map)).parse())
    }
  }

}
