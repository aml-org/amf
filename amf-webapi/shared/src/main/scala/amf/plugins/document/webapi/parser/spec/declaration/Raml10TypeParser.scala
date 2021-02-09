package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations._
import amf.core.metamodel.domain.common.{DisplayNameField, NameFieldSchema}
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.metamodel.domain.{LinkableElementModel, ShapeModel}
import amf.core.model.DataType
import amf.core.model.domain.{ScalarNode => DynamicDataNode, _}
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.{Annotations, _}
import amf.core.remote.Raml08
import amf.core.utils.{AmfStrings, IdCounter}
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.Shapes
import amf.plugins.document.webapi.annotations._
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.contexts.parser.raml.{Raml08WebApiContext, Raml10WebApiContext, RamlWebApiContext}
import amf.plugins.document.webapi.parser.RamlTypeDefMatcher.{JSONSchema, XMLSchema}
import amf.plugins.document.webapi.parser.spec.common._
import amf.plugins.document.webapi.parser.spec.declaration.RamlTypeDetection.parseFormat
import amf.plugins.document.webapi.parser.spec.declaration.external.raml.{
  RamlJsonSchemaExpression,
  RamlXmlSchemaExpression
}
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.raml.RamlSpecParser
import amf.plugins.document.webapi.parser.spec.raml.expression.RamlExpressionParser
import amf.plugins.document.webapi.parser.spec.toOas
import amf.plugins.document.webapi.parser.{RamlTypeDefMatcher, TypeName}
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.metamodel.common.{DocumentationField, ExamplesField}
import amf.plugins.domain.shapes.models.TypeDef._
import amf.plugins.domain.shapes.models.{ScalarType, _}
import amf.plugins.domain.shapes.parser.XsdTypeDefMapping
import amf.plugins.domain.webapi.annotations.TypePropertyLexicalInfo
import amf.validations.ParserSideValidations._
import org.yaml.model.{YPart, _}

import scala.language.postfixOps

object Raml10TypeParser {
  def apply(entry: YMapEntry,
            adopt: Shape => Unit,
            typeInfo: TypeInfo = TypeInfo(),
            defaultType: DefaultType = StringDefaultType)(implicit ctx: RamlWebApiContext): Raml10TypeParser = {
    val context = new Raml10WebApiContext(ctx.rootContextDocument,
                                          ctx.refs,
                                          ctx,
                                          Some(ctx.declarations),
                                          ctx.contextType,
                                          ctx.options)
    context.nodeRefIds ++= ctx.nodeRefIds
    new Raml10TypeParser(YMapEntryLike(entry), entry.key, adopt, typeInfo, defaultType)(context)
  }

  def apply(node: YNode, name: String, adopt: Shape => Unit, defaultType: DefaultType)(
      implicit ctx: RamlWebApiContext): Raml10TypeParser =
    new Raml10TypeParser(YMapEntryLike(node), name, adopt, TypeInfo(), defaultType)

  def apply(entryOrNode: YMapEntryLike,
            name: String,
            adopt: Shape => Unit,
            isAnnotation: Boolean,
            defaultType: DefaultType)(implicit ctx: RamlWebApiContext): Raml10TypeParser =
    new Raml10TypeParser(entryOrNode, name, adopt, TypeInfo(isAnnotation), defaultType)

  def parse(adopt: Shape => Unit, isAnnotation: Boolean = false, defaultType: DefaultType = StringDefaultType)(
      node: YNode)(implicit ctx: RamlWebApiContext): Option[Shape] = {
    val head = node.as[YMap].entries.head
    apply(head, adopt, TypeInfo(isAnnotation = isAnnotation), defaultType).parse()
  }
}

trait ExampleParser {
  def parseExamples(shape: AnyShape, map: YMap, options: ExampleOptions = DefaultExampleOptions)(
      implicit ctx: WebApiContext): Unit = {

    RamlExamplesParser(map, "example", "examples", shape, options.checkScalar(shape)).parse()
  }
}

trait RamlTypeSyntax {
  def parseWellKnownTypeRef(ramlType: String): Shape = {
    ramlType match {
      case "nil" | "" | "null" => NilShape()
      case "any"               => AnyShape()
      case "string" | "integer" | "number" | "boolean" | "datetime" | "datetime-only" | "time-only" | "date-only" =>
        ScalarShape().withDataType(DataType(ramlType))
      case "array"  => ArrayShape()
      case "object" => NodeShape()
      case "union"  => UnionShape()
      case "file"   => FileShape()
    }
  }

  def wellKnownType(str: String, isRef: Boolean = false): Boolean =
    if (str.indexOf("|") > -1 || str.indexOf("[") > -1 || str.indexOf("{") > -1 || str.indexOf("]") > -1 || str
          .indexOf("}") > -1 || (str.startsWith("<<") && str.endsWith(">>"))) {
      false
    } else
      RamlTypeDefMatcher.matchWellKnownType(TypeName(str), default = UndefinedType, isRef = isRef) != UndefinedType

  def isTypeExpression(str: String): Boolean = {
    try { RamlTypeDefMatcher.matchWellKnownType(TypeName(str), default = UndefinedType) == TypeExpressionType } catch {
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

case class Raml10TypeParser(entryOrNode: YMapEntryLike,
                            key: YNode,
                            adopt: Shape => Unit,
                            typeInfo: TypeInfo,
                            defaultType: DefaultType)(implicit override val ctx: RamlWebApiContext)
    extends RamlTypeParser(entryOrNode: YMapEntryLike,
                           key: YNode,
                           adopt: Shape => Unit,
                           typeInfo: TypeInfo,
                           defaultType: DefaultType) {
  override def typeParser: (YMapEntryLike, String, Shape => Unit, Boolean, DefaultType) => RamlTypeParser =
    Raml10TypeParser.apply

}

object Raml08TypeParser {
  def apply(node: YNode, name: String, adopt: Shape => Unit, isAnnotation: Boolean, defaultType: DefaultType)(
      implicit ctx: RamlWebApiContext): Raml08TypeParser =
    new Raml08TypeParser(YMapEntryLike(node), name, adopt, TypeInfo(isAnnotation = isAnnotation), defaultType)(
      new Raml08WebApiContext(ctx.rootContextDocument,
                              ctx.refs,
                              ctx,
                              Some(ctx.declarations),
                              contextType = ctx.contextType,
                              options = ctx.options))

  def apply(entry: YMapEntry, adopt: Shape => Unit, isAnnotation: Boolean, defaultType: DefaultType)(
      implicit ctx: RamlWebApiContext): Raml08TypeParser = {
    val context =
      new Raml08WebApiContext(ctx.rootContextDocument, ctx.refs, ctx, Some(ctx.declarations), options = ctx.options)
    context.nodeRefIds ++= ctx.nodeRefIds
    new Raml08TypeParser(YMapEntryLike(entry), entry.key, adopt, TypeInfo(isAnnotation = isAnnotation), defaultType)(
      context)
  }

  def apply(entryOrNode: YMapEntryLike,
            name: String,
            adopt: Shape => Unit,
            isAnnotation: Boolean,
            defaultType: DefaultType)(implicit ctx: RamlWebApiContext): Raml08TypeParser =
    new Raml08TypeParser(entryOrNode, name, adopt, TypeInfo(isAnnotation = isAnnotation), defaultType)(
      new Raml08WebApiContext(ctx.rootContextDocument, ctx.refs, ctx, Some(ctx.declarations), options = ctx.options))
}

case class Raml08TypeParser(entryOrNode: YMapEntryLike,
                            key: YNode,
                            adopt: Shape => Unit,
                            typeInfo: TypeInfo,
                            defaultType: DefaultType)(implicit override val ctx: RamlWebApiContext)
    extends RamlTypeParser(entryOrNode: YMapEntryLike, key: YNode, adopt: Shape => Unit, typeInfo, defaultType) {

  override def parse(): Option[AnyShape] = {
    val shape = ScalarShape(node).withName(name, Annotations(key))
    adopt(shape)
    val optionalParsedShape = node.tagType match {
      case YType.Map => parseSchemaOrTypeDeclarationAndExamples
      case YType.Seq => parseUnion(shape)
      case _         => Raml08TextParser(node, adopt, name, defaultType).parse()
    }
    // TODO: Hack to fix lexical info for ALS. Should be done correctly on RAML08 Parser refactor
    optionalParsedShape.map(correctTypeAnnotations)
  }

  private def correctTypeAnnotations(result: AnyShape) = {
    result.name.option().foreach(_ => result.withName(name, Annotations(key)))
    result.annotations.reject(isLexical)
    result.add(Annotations(ast))
  }

  private def parseSchemaOrTypeDeclarationAndExamples = {
    // has schema or its simple raml type declaration
    val map = node.as[YMap]
    map
      .key("schema")
      .fold {
        Option(SimpleTypeParser(name, adopt, map, defaultType.typeDef).parse())
      } { _ =>
        val maybeShape = Raml08SchemaParser(map, adopt).parse()

        maybeShape.map { s =>
          val inherits = s.meta.modelInstance.withName("inherits", Annotations())
          adopt(inherits)

          RamlSingleExampleParser("example",
                                  map,
                                  inherits.withExample,
                                  ExampleOptions(strictDefault = true, quiet = true).checkScalar(s))
            .parse()
            .fold(s) { e =>
              inherits.set(ShapeModel.Inherits, AmfArray(Seq(s)))
              inherits.setArray(ScalarShapeModel.Examples, Seq(e))
            }
        }
      }
  }

  private def parseUnion(shape: ScalarShape) = {
    Option(
      Raml08UnionTypeParser(UnionShape(node).withName(name, Annotations(key)).adopted(shape.id),
                            node.as[Seq[YNode]],
                            node).parse())
  }

  override def typeParser: (YMapEntryLike, String, Shape => Unit, Boolean, DefaultType) => RamlTypeParser =
    Raml08TypeParser.apply

  case class Raml08ReferenceParser(text: String, node: YNode, name: String)(implicit ctx: RamlWebApiContext) {
    def parse(): Some[AnyShape] = {
      val shape: AnyShape = ctx.declarations.findType(text, SearchScope.All) match {
        case Some(s: AnyShape) =>
          s.link(text, Annotations(node.value)).asInstanceOf[AnyShape].withName(name, s.name.annotations())
        case None =>
          val shape = UnresolvedShape(text, node).withName(text, Annotations())
          shape.withContext(ctx)
          adopt(shape)
          if (!text.validReferencePath && ctx.declarations.libraries.keys.exists(_ == text.split("\\.").head)) {
            ctx.eh.violation(
              ChainedReferenceSpecification,
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

  case class Raml08TextParser(value: YNode, adopt: Shape => Unit, name: String, defaultType: DefaultType)(
      implicit ctx: RamlWebApiContext) {
    def parse(): Option[AnyShape] = {
      value.tagType match {
        case YType.Null =>
          Raml08DefaultTypeParser(defaultType.typeDef, name, value, adopt)
            .parse()
            .map(s => s.add(SourceAST(value)).add(SourceLocation(value.sourceName)))
        case _ =>
          value.as[YScalar].text match {
            case XMLSchema(_)  => Option(RamlXmlSchemaExpression(key, value, adopt).parse())
            case JSONSchema(_) => Option(RamlJsonSchemaExpression(key, value, adopt).parse())
            case t if RamlTypeDefMatcher.match08Type(t).isDefined =>
              Option(
                SimpleTypeParser(name, adopt, YMap.empty, defaultType = RamlTypeDefMatcher.match08Type(t).get).parse())
            case t => Raml08ReferenceParser(t, node, name).parse()
          }
      }
    }
  }

  case class Raml08SchemaParser(map: YMap, adopt: Shape => Unit)(implicit ctx: RamlWebApiContext) {
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

case class Raml08DefaultTypeParser(defaultType: TypeDef, name: String, ast: YPart, adopt: Shape => Unit)(
    implicit ctx: RamlWebApiContext) {
  def parse(): Option[AnyShape] = {
    val product: Option[AnyShape] = defaultType match {
      case NilType =>
        Some(NilShape(ast).withName(name, Annotations()).add(Inferred()))
      case FileType =>
        Some(FileShape(ast).withName(name, Annotations()))
      case _: ScalarType =>
        Some(ScalarShape(ast)
          .withName(name, Annotations())
          .set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(defaultType)), Annotations() += Inferred()))
      case AnyType =>
        Some(AnyShape(ast).withName(name, Annotations()).add(Inferred()))
      case _ =>
        // TODO get parent id
        ctx.eh.violation(UnableToSetDefaultType, "", s"Cannot set default type $defaultType in raml 08", ast)
        None
    }
    product.foreach(adopt)
    product
  }
}

case class Raml08UnionTypeParser(shape: UnionShape, types: Seq[YNode], ast: YPart)(implicit ctx: RamlWebApiContext) {
  def parse(): UnionShape = {

    val unionNodes = types.zipWithIndex
      .map {
        case (unionNode, index) =>
          val adopt: Shape => Unit = item => item.adopted(shape.id + "/items/" + index)
          Raml08TypeParser(unionNode, s"item$index", adopt, isAnnotation = false, defaultType = AnyDefaultType)
            .parse()
      }
      .filter(_.isDefined)
      .map(_.get)

    shape.setArray(UnionShapeModel.AnyOf, unionNodes, Annotations(ast))

  }
}

case class SimpleTypeParser(name: String, adopt: Shape => Unit, map: YMap, defaultType: TypeDef)(
    implicit val ctx: RamlWebApiContext)
    extends SpecParserOps {

  def parse(): AnyShape = {

    if (map.key("repeat").exists(entry => entry.value.as[Boolean])) {
      val shape = ArrayShape(map).withName(name, Annotations())
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
        .flatMap { e =>
          e.value.tagType match {
            case YType.Null => None
            case _          => e.value.toOption[YScalar]
          }
        }
        .fold {
          Raml08DefaultTypeParser(defaultType, name, map, adopt).parse()
        } { value =>
          XsdTypeDefMapping.xsdFromString(value.text) match {
            case (Some(iri: String), _: Option[String])
                if iri.equals((Shapes + "file").iri()) => // handle file type in 08 as FileShape for compatibility
              // (Applicable only to Form properties) ???
              val shape = FileShape(value)
              Some(shape.withName(name, Annotations()))
            case (Some(iri: String), format: Option[String]) =>
              val shape = ScalarShape(value).set(ScalarShapeModel.DataType, AmfScalar(iri), Annotations(value))
              format.foreach { f =>
                if (f != "") shape.set(ScalarShapeModel.Format, AmfScalar(f), Annotations())
              }
              Some(shape.withName(name, Annotations()))
            case _ =>
              ctx.eh.violation(InvalidTypeDefinition, "", s"Invalid type def ${value.text} for ${Raml08.name}", value)
              None
          }
        }
        .getOrElse(ScalarShape(map).withDataType(DataType.String).withName(name, Annotations()))

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

    val counter = new IdCounter
    map.key("enum", ShapeModel.Values in shape using DataNodeParser.parse(Some(s"${shape.id}/list"), counter))

    map.key(
      "pattern",
      entry => {
        var regex = entry.value.as[String]
        if (!regex.startsWith("^")) regex = "^" + regex
        if (!regex.endsWith("$")) regex = regex + "$"
        val pattern = ScalarNode(regex).text().copy(annotations = Annotations(entry))
        shape.set(ScalarShapeModel.Pattern, pattern, Annotations(entry))
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

    val isParamString = shape.isInstanceOf[ScalarShape] && shape
      .asInstanceOf[ScalarShape]
      .dataType
      .option()
      .getOrElse("") == DataType.String
    RamlSingleExampleParser("example",
                            map,
                            shape.withExample,
                            ExampleOptions(strictDefault = true, quiet = true).checkScalar(shape))
      .parse()
      .foreach(e => shape.setArray(ScalarShapeModel.Examples, Seq(e)))

    map.key(
      "default",
      entry => {
        entry.value.tagType match {
          case YType.Null =>
          case _ =>
            NodeDataNodeParser(entry.value, shape.id, quiet = false).parse().dataNode.foreach { dn =>
              shape.set(ShapeModel.Default, dn, Annotations(entry))
            }
            shape.setDefaultStrValue(entry)
        }
      }
    )
  }
}

case class TypeInfo(isAnnotation: Boolean = false, isPropertyOrParameter: Boolean = false)

sealed abstract class RamlTypeParser(entryOrNode: YMapEntryLike,
                                     key: YNode,
                                     adopt: Shape => Unit,
                                     typeInfo: TypeInfo,
                                     defaultType: DefaultType)(implicit val ctx: RamlWebApiContext)
    extends RamlSpecParser {

  val name: String = key.as[YScalar].text

  protected val (ast, node) = (entryOrNode.ast, entryOrNode.value)

  private val nameAnnotations: Annotations = entryOrNode.key.map(n => Annotations(n)).getOrElse(Annotations())

  def typeParser: (YMapEntryLike, String, Shape => Unit, Boolean, DefaultType) => RamlTypeParser

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
    val info: Option[TypeDef] = RamlTypeDetection(node, "", parseFormat(node), defaultType)
    val result = info.map {
      case XMLSchemaType                         => RamlXmlSchemaExpression(key, node, adopt, parseExample = true).parse()
      case JSONSchemaType                        => RamlJsonSchemaExpression(key, node, adopt, parseExample = true).parse()
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
      case map: YMap if result.isDefined =>
        AnnotationParser(
          result.get,
          map,
          if (typeInfo.isAnnotation) List(VocabularyMappings.customDomainProperty)
          else Nil ++ List(VocabularyMappings.shape, VocabularyMappings.payload, VocabularyMappings.request)
        ).parse()
      case _ => // ignore
    }

    result
  }

  private def parseNilUnion() = {
    val union = UnionShape().withName(name, nameAnnotations)
    adopt(union)

    val parsed = node.value match {
      case s: YScalar =>
        val toParse = YMapEntry(YNode(""), YNode(s.text.stripSuffix("?")))
        ctx.factory.typeParser(toParse, s => s.withId(union.id), typeInfo.isAnnotation, defaultType).parse().get
      case m: YMap =>
        val newEntries = m.entries.map { entry =>
          if (entry.key.as[YScalar].text == "type") {
            YMapEntry("type", entry.value.as[YScalar].text.stripSuffix("?"))
          } else {
            entry
          }
        }

        val toParse = YMapEntry(YNode(""), YMap(newEntries, newEntries.headOption.map(_.sourceName).getOrElse("")))
        ctx.factory.typeParser(toParse, s => s.withId(union.id), typeInfo.isAnnotation, defaultType).parse().get
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
      case map: YMap if shapeResult.isDefined => ShapeExtensionParser(shapeResult.get, map, ctx, typeInfo).parse()
      case _                                  => // ignore if it is not a map or we haven't been able to parse a shape
    }
  }

  private def parseTypeExpression(): Shape = {
    node.value match {
      case expression: YScalar =>
        val shape = RamlExpressionParser.parse(adopt, expression.text, ast).get
        if (name != "schema" && name != "type") adopt(shape.withName(name, nameAnnotations))
        shape
      case _: YMap => parseObjectType()
    }
  }

  private def parseScalarType(typeDef: TypeDef): Shape = {
    if (typeDef.isNil) {
      val nilShape = NilShape(ast).withName(name, nameAnnotations)
      adopt(nilShape)
      node.tagType match {
        case YType.Map => NilShapeParser(nilShape, node.as[YMap]).parse()
        case _         => nilShape
      }
    } else {
      val shape = ScalarShape(ast).withName(name, nameAnnotations)
      adopt(shape)
      node.tagType match {
        case YType.Map => ScalarShapeParser(typeDef, shape, node.as[YMap]).parse()
        case YType.Seq =>
          val entry = ast.asInstanceOf[YMapEntry]
          InheritanceParser(entry, shape, None).parse()
          shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), Annotations(entry))
          shape
        case _ =>
          val fieldAnnotations =
            if (node.isNull) Annotations() += Inferred()
            else Annotations()
          shape.set(ScalarShapeModel.DataType,
                    AmfScalar(XsdTypeDefMapping.xsd(typeDef), Annotations(node.value)),
                    fieldAnnotations)
      }
    }
  }

  private def parseAnyType(): Shape = {
    val shape = AnyShape(ast).withName(name, nameAnnotations)
    adopt(shape)
    ast match {
      case entry: YMapEntry if entry.value.value.isInstanceOf[YMap] =>
        AnyTypeShapeParser(shape, entry.value.value.asInstanceOf[YMap]).parse()
      case _ if node.tagType == YType.Map => AnyTypeShapeParser(shape, node.as[YMap]).parse()
      case _                              => shape
    }
  }

  def parseArrayType(): Shape = {
    val shape = node.to[YMap] match {
      case Right(map) => DataArrangementParser(name, ast, map, (shape: Shape) => adopt(shape)).parse()
      case Left(_) =>
        val toAdopt = ArrayShape(ast).withName(name, nameAnnotations)
        adopt(toAdopt)
        toAdopt
    }
    shape
  }

  private def parseUnionType(): UnionShape = {
    val shape = UnionShape(Annotations(node)).withName(name, nameAnnotations)
    adopt(shape)
    node.tagType match {
      case YType.Map =>
        UnionShapeParser(node.as[YMap], shape).parse()
      case YType.Seq =>
        InheritanceParser(ast.asInstanceOf[YMapEntry], shape, None).parse()
        shape
      case _ =>
        ctx.eh.violation(InvalidUnionType, shape.id, s"Invalid node for union shape '${node.toString()}", node)
        shape
    }
  }

  private def parseObjectType(): Shape = {
    if (isFileType) {
      val shape = FileShape(Annotations(node)).withName(name, nameAnnotations)
      node.tagType match {
        case YType.Str =>
          adopt(shape)
        case YType.Map =>
          FileShapeParser(node, shape, adopt).parse()
      }
      shape
    } else {
      val shape = NodeShape(ast).withName(name, nameAnnotations)
      adopt(shape)

      node.tagType match {
        case YType.Map =>
          NodeShapeParser(shape, node.as[YMap])
            .parse() // I have to do the adopt before parser children shapes. Other way the children will not have the father id
        case YType.Seq =>
          InheritanceParser(ast.asInstanceOf[YMapEntry], shape, None).parse()
          shape
        case _ if node.toOption[YScalar].isDefined =>
          val text = node.as[YScalar].text
          parseReference(node, shape.id, createLink(_, text, shape.id))
            .getOrElse {
              if (RamlTypeDefMatcher.matchWellKnownType(TypeName(text), default = UndefinedType) == ObjectType)
                shape.add(ExplicitField())
              else {
                val unresolve = UnresolvedShape(Fields(),
                                                Annotations(node),
                                                text,
                                                None,
                                                Some((k: String) => shape.set(LinkableElementModel.TargetId, k)),
                                                shouldLink = false).withName(text, nameAnnotations)
                unresolve.withContext(ctx)
                adopt(unresolve)
                if (!text.validReferencePath && ctx.declarations.libraries.keys.exists(_ == text.split("\\.").head)) {
                  ctx.eh.violation(
                    ChainedReferenceSpecification,
                    shape.id,
                    s"Chained reference '$text",
                    node
                  )
                } else {
                  unresolve.unresolved(text, node)
                }
                shape.annotations.reject(isLexical)
                shape.annotations ++= unresolve.annotations
                shape.withLinkTarget(unresolve).withLinkLabel(text)
              }
            }
      }
    }
  }

  private def createLink(shape: AnyShape, label: String, linkId: String): Shape = {
    shape
      .link(label, Annotations(node))
      .asInstanceOf[Shape]
      .withName(name, nameAnnotations) // we setup the local reference in the name
      .withId(linkId)
  }

  private def parseReference(node: YNode, parendId: String, createLink: AnyShape => Shape): Option[Shape] = {
    ctx.link(node) match {
      case Left(key) =>
        val referenced = ctx.declarations.findType(
          key,
          SearchScope.Fragments,
          Some((s: String) => ctx.eh.violation(InvalidFragmentType, parendId, s, node)))
        referenced.map(createLink(_).add(ExternalFragmentRef(key)))
      case _ =>
        val text       = node.as[YScalar].text
        val referenced = ctx.declarations.findType(text, SearchScope.Named)
        referenced.map(createLink(_))
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
          val pattern = ScalarNode(regex).text().copy(annotations = Annotations(entry))
          shape.set(ScalarShapeModel.Pattern, pattern, Annotations(entry))
        }
      )
      map.key("minLength", (ScalarShapeModel.MinLength in shape).allowingAnnotations)
      map.key("maxLength", (ScalarShapeModel.MaxLength in shape).allowingAnnotations)
      map.key("exclusiveMinimum".asRamlAnnotation, ScalarShapeModel.ExclusiveMinimum in shape)
      map.key("exclusiveMaximum".asRamlAnnotation, ScalarShapeModel.ExclusiveMaximum in shape)
    }
  }

  case class AnyTypeShapeParser(shape: AnyShape, map: YMap) extends AnyShapeParser {

    override val options: ExampleOptions = ExampleOptions(strictDefault = true, quiet = true).checkScalar(shape)

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

  case class NilShapeParser(shape: NilShape, map: YMap) extends AnyShapeParser

  case class ScalarShapeParser(typeDef: TypeDef, shape: ScalarShape, map: YMap)
      extends AnyShapeParser
      with CommonScalarParsingLogic {

    override lazy val dataNodeParser: YNode => DataNode = ScalarNodeParser(parent = Some(shape.id)).parse
    override lazy val enumParser: YNode => DataNode     = CommonEnumParser(shape.id, enumType = EnumParsing.SCALAR_ENUM)

    override def parse(): ScalarShape = {
      super.parse()

      parseOASFields(map, shape)

      val validatedTypeDef: TypeDef = ScalarFormatType(shape, typeDef).parse(map)
      ensureFormatInDateTime(validatedTypeDef)
      typeOrSchema(map)
        .fold(
          shape
            .set(ScalarShapeModel.DataType,
                 AmfScalar(XsdTypeDefMapping.xsd(validatedTypeDef)),
                 Annotations() += Inferred()))(entry =>
          shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(validatedTypeDef)), Annotations(entry)))

      // todo: should i parse double type values as value.double()? when emit it the values will appear with .0 (if they where ints)
      map.key(
        "minimum",
        entry => { // todo pope
          val value = ScalarNode(entry.value)
          if (ensurePrecision(shape.dataType.option(), entry.value.toString(), entry.value))
            shape.set(ScalarShapeModel.Minimum, value.text(), Annotations(entry))
        }
      )

      map.key(
        "maximum",
        entry => { // todo pope
          val value = ScalarNode(entry.value)
          if (ensurePrecision(shape.dataType.option(), entry.value.toString(), entry.value))
            shape.set(ScalarShapeModel.Maximum, value.text(), Annotations(entry))
        }
      )
      // We don't need to parse (format) extension because in oas must not be emitted, and in raml will be emitted.

      map.key(
        "multipleOf",
        entry => { // todo pope
          val value = ScalarNode(entry.value)
          if (ensurePrecision(shape.dataType.option(), entry.value.toString(), entry.value))
            shape.set(ScalarShapeModel.MultipleOf, value.text(), Annotations(entry))
        }
      )

      // shape.set(ScalarShapeModel.Repeat, value = false) // 0.8 support, not exists in 1/.0, set default

      shape
    }

    private def ensureFormatInDateTime(typeDef: TypeDef): Unit = {
      typeDef match {
        case TypeDef.DateTimeType =>
          shape.format
            .option()
            .foreach(format => {
              if (format != "rfc3339" && format != "rfc2616") {
                ctx.eh.violation(InvalidDatetimeFormat,
                                 shape.id,
                                 s"Invalid format value for datetime, must be 'rfc3339' or 'rfc2616'",
                                 ast)
              }
            })
        case _ =>
      }
    }

    protected def ensurePrecision(dataType: Option[String], value: String, ast: YNode): Boolean = {
      if (dataType.exists(_.endsWith("#integer")) && value.contains(".")) {
        ctx.eh.violation(
          InvalidDecimalPoint,
          shape.id,
          "Invalid decimal point for an integer: " + value,
          ast
        )
        false
      } else true
    }

  }

  case class UnionShapeParser(override val map: YMap, shape: UnionShape) extends AnyShapeParser {

    override def parse(): UnionShape = {
      super.parse()

      map.key(
        "anyOf", { entry =>
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val unionNodes = seq.zipWithIndex
                .map {
                  case (unionNode, index) =>
                    typeParser(YMapEntryLike(unionNode),
                               s"item$index",
                               item => item.adopted(shape.id + "/items/" + index),
                               typeInfo.isAnnotation,
                               AnyDefaultType).parse()
                }
                .filter(_.isDefined)
                .map(_.get)
              shape.setArray(UnionShapeModel.AnyOf, unionNodes, Annotations(entry.value))

            case _ =>
              ctx.eh.violation(InvalidUnionType, shape.id, "Unions are built from multiple shape nodes", entry)
          }
        }
      )

      shape
    }
  }

  case class OrConstraintParser(map: YMap, shape: Shape) {

    def parse(): Unit = {
      map.key(
        "or".asRamlAnnotation, { entry =>
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val nodes = seq.zipWithIndex
                .map {
                  case (unionNode, index) =>
                    typeParser(YMapEntryLike(unionNode),
                               s"item$index",
                               item => item.adopted(shape.id + "/or/" + index),
                               typeInfo.isAnnotation,
                               AnyDefaultType).parse()
                }
                .filter(_.isDefined)
                .map(_.get)
              shape.setArray(ShapeModel.Or, nodes, Annotations(entry.value))

            case _ =>
              ctx.eh.violation(InvalidOrType, shape.id, "Or constraints are built from multiple shape nodes", entry)
          }
        }
      )
    }
  }

  case class AndConstraintParser(map: YMap, shape: Shape) {

    def parse(): Unit = {
      map.key(
        "and".asRamlAnnotation, { entry =>
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val nodes = seq.zipWithIndex
                .map {
                  case (unionNode, index) =>
                    typeParser(YMapEntryLike(unionNode),
                               s"item$index",
                               item => item.adopted(shape.id + "/and/" + index),
                               typeInfo.isAnnotation,
                               AnyDefaultType).parse()
                }
                .filter(_.isDefined)
                .map(_.get)
              shape.setArray(ShapeModel.And, nodes, Annotations(entry.value))

            case _ =>
              ctx.eh.violation(InvalidAndType, shape.id, "And constraints are built from multiple shape nodes", entry)
          }
        }
      )
    }
  }

  case class XoneConstraintParser(map: YMap, shape: Shape) {

    def parse(): Unit = {
      adopt(shape)

      map.key(
        "xor".asRamlAnnotation, { entry =>
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val nodes = seq.zipWithIndex
                .map {
                  case (unionNode, index) =>
                    typeParser(YMapEntryLike(unionNode),
                               s"item$index",
                               item => item.adopted(shape.id + "/xor/" + index),
                               typeInfo.isAnnotation,
                               AnyDefaultType).parse()
                }
                .filter(_.isDefined)
                .map(_.get)
              shape.setArray(ShapeModel.Xone, nodes, Annotations(entry.value))

            case _ =>
              ctx.eh.violation(InvalidXoneType,
                               shape.id,
                               "Xone constraints are built from multiple shape nodes",
                               entry)
          }
        }
      )
    }
  }

  case class NotConstraintParser(map: YMap, shape: Shape) {

    def parse(): Unit = {
      map.key(
        "not".asRamlAnnotation, { entry =>
          typeParser(YMapEntryLike(entry), "not", (s: Shape) => s.withId(shape.id + "/not"), false, defaultType)
            .parse() match {
            case Some(negated) => shape.set(ShapeModel.Not, negated, Annotations(entry.value))
            case _             => // ignore
          }
        }
      )
    }
  }

  case class FileShapeParser(node: YNode, file: FileShape, adopt: Shape => Unit)
      extends AnyShapeParser
      with CommonScalarParsingLogic {
    override val map: YMap = node.as[YMap]
    override val shape: FileShape = {
      adopt(file)
      file
    }

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
          ctx.eh.violation(
            UnexpectedFileTypesSyntax,
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
            case Right(_) => Left(TupleShape(ast).withName(name, nameAnnotations))
            // not an array regular array parsing
            case _ =>
              val tuple = TupleShape(ast).withName(name, nameAnnotations)
              ctx.eh.violation(InvalidTupleType, tuple.id, "Tuples must have a list of types", ast)
              Left(tuple)
          }
        case None => Right(ArrayShape(ast).withName(name, nameAnnotations))
      }
    }

    def parse(): Shape = {
      lookAhead() match {
        case Left(tuple)  => TupleShapeParser(tuple, map, adopt).parse()
        case Right(array) => ArrayShapeParser(array, map, adopt).parse()
      }
    }

  }

  case class ArrayShapeParser(override val shape: ArrayShape, map: YMap, adopt: Shape => Unit)
      extends AnyShapeParser
      with SchemaUsageRestrictions {

    override def parse(): AnyShape = {
      adopt(shape)

      super.parse()

      map.key("uniqueItems", (ArrayShapeModel.UniqueItems in shape).allowingAnnotations)
      map.key("collectionFormat".asRamlAnnotation, ArrayShapeModel.CollectionFormat in shape)

      parseItems()

      val finalShape = Option(shape.items) match {
        case Some(_: ArrayShape)  => Some(shape.toMatrixShape)
        case Some(_: MatrixShape) => Some(shape.toMatrixShape)
        case Some(_: Shape)       => Some(shape)
        case None                 => arrayShapeTypeFromInherits().orElse(Some(shape))
      }

      finalShape match {
        case Some(parsed: AnyShape) =>
          adopt(parsed)
          parsed
        case _ =>
          ctx.eh.violation(UnableToParseArray, shape.id, "Cannot parse data arrangement shape", map)
          shape
      }
    }

    private def parseItems(): Unit =
      for {
        itemsEntry <- map.key("items")
        item       <- Raml10TypeParser(itemsEntry, items => items.adopted(shape.id), defaultType = defaultType).parse()
      } yield {
        // we check we are not using schemas for items
        checkSchemaInProperty(Seq(item), shape.location(), amf.core.parser.Range(itemsEntry.range))
        shape.withItems(item)
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

    override protected def parseInheritance(): Unit = {
      if (map.key("items").isDefined) super.parseInheritance()
      else {
        typeOrSchema(map)
          .map { typeEntry =>
            val isTypeExpression = isPlainArrayTypeExpression(typeEntry)
            if (isTypeExpression) {
              val typeExpression = typeEntry.value.toString.replaceFirst("\\[\\]", "")
              RamlExpressionParser
                .parse(items => items.adopted(shape.id), expression = typeExpression, part = typeEntry.value.value)
                .foreach { value =>
                  shape.withItems(value)
                }
            } else super.parseInheritance()
          }
          .getOrElse {
            super.parseInheritance()
          }
      }
    }
  }

  private def isPlainArrayTypeExpression(typeEntry: YMapEntry) = {
    val text = typeEntry.value.toString
    text.endsWith("[]") && !text.contains("|")
  }

  case class TupleShapeParser(shape: TupleShape, map: YMap, adopt: Shape => Unit) extends AnyShapeParser {

    override def parse(): TupleShape = {
      adopt(shape)

      super.parse()

      parseInheritance()

      map.key("minItems", (ArrayShapeModel.MinItems in shape).allowingAnnotations)
      map.key("maxItems", (ArrayShapeModel.MaxItems in shape).allowingAnnotations)
      map.key("uniqueItems", (ArrayShapeModel.UniqueItems in shape).allowingAnnotations)

      // The items of the tuple are emitted as an annotation
      val itemsField = map.key("tuple".asRamlAnnotation)
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

  trait SchemaUsageRestrictions {

    protected def checkSchemaInProperty(elements: Seq[AmfElement],
                                        location: Option[String] = None,
                                        entryRange: Range): Unit = {
      elements.foreach { checkForForeignTypeInheritance(_, location, entryRange) }
    }
    protected def checkSchemaInheritance(base: Shape, elements: Seq[AmfElement], entryRange: Range): Unit = {
      if (base.meta != AnyShapeModel && !emptyObjectType(base) && !emptyScalarType(base) && !emptyArrayType(base)) {
        elements.foreach { checkForForeignTypeInheritance(_, base.location(), entryRange) }
      }
    }

    private def checkForForeignTypeInheritance(element: AmfElement,
                                               location: Option[String],
                                               entryRange: Range): Unit = {
      element match {
        case shape: AnyShape if isParsedJsonSchema(shape) || isSchemaIsJsonSchema(shape) =>
          ctx.eh.warning(
            JsonSchemaInheritanceWarning,
            shape.id,
            Some(ShapeModel.Inherits.value.iri()),
            "Invalid reference to JSON Schema",
            Some(LexicalInformation(entryRange)),
            location.orElse(shape.location())
          )

        case xml: SchemaShape =>
          ctx.eh.violation(
            XmlSchemaInheritancceWarning,
            xml.id,
            Some(ShapeModel.Inherits.value.iri()),
            "Invalid reference to XML Schema",
            Some(LexicalInformation(entryRange)),
            location.orElse(xml.location())
          )
        case _ => // ignore
      }
    }

    protected def emptyObjectType(shape: Shape): Boolean = {
      shape match {
        case nodeShape: NodeShape => nodeShape.properties.isEmpty
        case _                    => false
      }
    }

    protected def emptyArrayType(shape: Shape): Boolean = {
      shape match {
        case arrayShape: ArrayShape => Option(arrayShape.items).isEmpty
        case _                      => false
      }
    }

    protected def emptyScalarType(shape: Shape): Boolean = {
      shape match {
        case scalarShape: ScalarShape => scalarShape.dataType.option().isEmpty
        case _                        => false
      }
    }
  }

  private def isSchemaIsJsonSchema(shape: AnyShape) = {
    shape.annotations.contains(classOf[SchemaIsJsonSchema])
  }

  private def isParsedJsonSchema(shape: AnyShape) = {
    shape.annotations.contains(classOf[ParsedJSONSchema])
  }

  case class InheritanceParser(entry: YMapEntry, shape: Shape, fatherMap: Option[YMap])(
      implicit val ctx: RamlWebApiContext)
      extends RamlSpecParser
      with RamlTypeSyntax
      with SchemaUsageRestrictions {

    def parse(): Unit = {
      entry.value.tagType match {

        case YType.Seq =>
          val superTypes            = entry.value.as[Seq[YNode]]
          val isMultipleInheritance = superTypes.size > 1
          val inherits: Seq[AmfElement] = superTypes.zipWithIndex.map {
            case (node, i) =>
              val id = if (isMultipleInheritance) shape.id + i else shape.id
              node.as[YScalar].text match {
                case RamlTypeDefMatcher.TypeExpression(s) =>
                  RamlExpressionParser.parse(adopt, s, node).get.adopted(id)
                case s if wellKnownType(s) =>
                  parseWellKnownTypeRef(s).withName(s, Annotations(entry.key)).adopted(id)
                case s =>
                  ctx.declarations.findType(s, SearchScope.All) match {
                    case Some(ancestor) => ancestor
                    case _              => unresolved(node, shape)
                  }
              }
          }
          checkSchemaInheritance(shape, inherits, Range(node.range))
          shape.fields.setWithoutId(ShapeModel.Inherits,
                                    AmfArray(inherits, Annotations(entry.value)),
                                    Annotations(entry))
        case YType.Map =>
          Raml10TypeParser(entry, s => s.adopted(shape.id))
            .parse()
            .foreach { s =>
              checkSchemaInheritance(shape, Seq(s), Range(entry.range))
              shape.set(ShapeModel.Inherits, AmfArray(Seq(s), Annotations(entry.value)), Annotations(entry))
            }

        case _ if RamlTypeDefMatcher.TypeExpression.unapply(entry.value.as[YScalar].text).isDefined =>
          Raml10TypeParser(entry, s => s.adopted(shape.id))
            .parse()
            .foreach { s =>
              checkSchemaInheritance(shape, Seq(s), Range(entry.range))
              shape.set(ShapeModel.Inherits, AmfArray(Seq(s), Annotations(entry.value)), Annotations(entry))
            }

        case YType.Str if XMLSchema.unapply(entry.value).isDefined =>
          val parsed =
            RamlXmlSchemaExpression("schema",
                                    entry.value,
                                    xmlSchemaShape => xmlSchemaShape.withId(shape.id + "/xmlSchema")).parse()
          checkSchemaInheritance(shape, Seq(parsed), Range(entry.range))
          shape.set(ShapeModel.Inherits, AmfArray(Seq(parsed), Annotations(entry.value)), Annotations(entry))

        case _ if !wellKnownType(entry.value.as[YScalar].text) =>
          val text = entry.value.as[YScalar].text
          val result = parseReference(
            entry.value,
            shape.id,
            target => {
              checkSchemaInheritance(shape, Seq(target), Range(entry.range))
              target
                .link(text, Annotations(entry.value))
                .asInstanceOf[AnyShape]
                .withName(target.name.option().getOrElse("schema"), Annotations(entry.key))
                .add(AutoGeneratedName())
            }
          )
          result match {
            case Some(link) =>
              shape.fields.setWithoutId(ShapeModel.Inherits,
                                        AmfArray(Seq(link), Annotations(entry.value)),
                                        Annotations(entry))
            case _ =>
              val baseClass = text match {
                case JSONSchema(_) =>
                  ctx.eh.warning(
                    JsonSchemaInheritanceWarning,
                    shape.id,
                    Some(ShapeModel.Inherits.value.iri()),
                    "Inheritance from JSON Schema",
                    entry.value
                  )
                  RamlJsonSchemaExpression("schema",
                                           entry.value,
                                           jsonSchemaShape => jsonSchemaShape.withId(shape.id + "/jsonSchema")).parse()
                case _ =>
                  unresolved(entry.value, shape)
              }
              if (!text.matches("<<.*>>")) {
                shape.set(ShapeModel.Inherits, AmfArray(Seq(baseClass), Annotations(entry.value)), Annotations(entry))
              }
          }

        case _ =>
          shape.add(ExplicitField()) // TODO store annotation in dataType field.
      }
    }

    private def unresolved(node: YNode, shape: Shape): UnresolvedShape = {
      val reference = node.as[YScalar].text
      // we need to pass the extension parser to the unresolved, in order to not only have the father at the moment of resolve the future ref in Value, also we need the parser itself, for modular dependency (We cannot create an instance of this parser in core)
      val unresolvedShape = UnresolvedShape(
        Fields(),
        Annotations(node),
        reference,
        fatherMap.map(m =>
          (resolvedKey: Option[String]) =>
            ShapeExtensionParser(shape, m, ctx, typeInfo, overrideSyntax = resolvedKey)),
        Some((k: String) =>
          if (shape.fields.exists(LinkableElementModel.TargetId)) shape.set(LinkableElementModel.TargetId, k))
      )
      unresolvedShape.withContext(ctx)
      if (!reference.validReferencePath && ctx.declarations.libraries.keys.exists(_ == reference.split("\\.").head)) {
        ctx.eh.violation(
          ChainedReferenceSpecification,
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

  case class NodeShapeParser(shape: NodeShape, map: YMap) extends AnyShapeParser with SchemaUsageRestrictions {
    override def parse(): AnyShape = {

      super.parse()

      checkExtendedUnionDiscriminator()

      map.key("minProperties", (NodeShapeModel.MinProperties in shape).allowingAnnotations)
      map.key("maxProperties", (NodeShapeModel.MaxProperties in shape).allowingAnnotations)

      // we set-up default values for closed
      if (shape.inherits.isEmpty)
        shape.set(NodeShapeModel.Closed, value = false)
      else if (map.key("additionalProperties").isEmpty) {
        val closedInInhertiance = shape.effectiveInherits.exists(
          s =>
            s.isInstanceOf[NodeShape] && s.asInstanceOf[NodeShape].closed.option().isDefined && s
              .asInstanceOf[NodeShape]
              .closed
              .value())
        shape.set(NodeShapeModel.Closed, value = closedInInhertiance)
      }
      map.key("additionalProperties", (NodeShapeModel.Closed in shape).negated.explicit)
      map.key("additionalProperties".asRamlAnnotation).foreach { entry =>
        ctx.factory
          .typeParser(entry, s => s.adopted(shape.id), true, defaultType)
          .parse()
          .foreach { parsed =>
            shape.set(NodeShapeModel.AdditionalPropertiesSchema, parsed, Annotations(entry))
          }
      }

      map.key("discriminator", (NodeShapeModel.Discriminator in shape).allowingAnnotations)
      map.key("discriminatorValue", (NodeShapeModel.DiscriminatorValue in shape).allowingAnnotations)

      map.key(
        "properties",
        entry => {
          entry.value.tagType match {
            case YType.Map =>
              val m = entry.value.as[YMap]
              val properties: Seq[PropertyShape] =
                PropertiesParser(m, shape.withProperty).parse()
              val hasPatternProperties = properties.exists(_.patternName.nonEmpty)
              if (hasPatternProperties && shape.closed.value()) {
                ctx.eh.violation(
                  PatternPropertiesOnClosedNodeSpecification,
                  shape.id,
                  s"Node without additional properties support cannot have pattern properties",
                  node
                )
              }
              // We check we are not using schemas in properties
              properties.foreach { prop =>
                checkSchemaInProperty(Seq(prop.range), prop.location(), Range(entry.range))
              }

              shape.discriminator
                .option()
                .foreach(discriminator => {
                  val containsDiscriminatorProp = properties.exists(_.name.value() == discriminator)
                  if (!containsDiscriminatorProp) {
                    ctx.violation(
                      MissingDiscriminatorProperty,
                      shape.id,
                      s"Property '$discriminator' marked as discriminator is missing in properties facet"
                    )
                  }
                })
              shape.set(NodeShapeModel.Properties, AmfArray(properties, Annotations(entry.value)), Annotations(entry))
            case YType.Null =>
            case _ =>
              ctx.eh.violation(
                InvalidValueInPropertiesFacet,
                shape.id,
                s"Properties facet must be a map of key and values",
                entry
              )
          }
        }
      )

      map.key(
        "dependencies".asRamlAnnotation,
        entry => {
          Draft4ShapeDependenciesParser(shape, entry.value.as[YMap], shape.id, JSONSchemaDraft4SchemaVersion)(
            toOas(ctx)).parse()
        }
      )

      shape
    }

    def checkExtendedUnionDiscriminator(): Unit = {
      if (shape.inherits.length == 1 && shape.inherits.head.isInstanceOf[UnionShape]) {
        map.key("discriminator").foreach {
          ctx.eh.violation(
            DiscriminatorOnExtendedUnionSpecification,
            shape.id,
            "Property discriminator forbidden in a node extending a unionShape",
            _
          )
        }

        map.key("discriminatorValue").foreach {
          ctx.eh.violation(
            DiscriminatorOnExtendedUnionSpecification,
            shape.id,
            "Property discriminatorValue forbidden in a node extending a unionShape",
            _
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

    private def setPathTo(shape: PropertyShape, name: String, annotations: Annotations) = {
      shape.set(
        PropertyShapeModel.Path,
        AmfScalar((Namespace.Data + name.urlComponentEncoded).iri(), annotations))
    }

    def parse(): Option[PropertyShape] = {

      entry.key.asScalar match {
        case Some(scalarKey) =>
          val propName = scalarKey.text
          val property = producer(propName).add(Annotations(entry))

          // we detect pattern properties here
          if (propName.startsWith("/") && propName.endsWith("/")) {
            if (propName == "//") {
              property.withPatternName("^.*$")
            } else {
              property.withPatternName(propName.drop(1).dropRight(1))
            }
          }

          // var explicitRequired: Option[Value] = None
          entry.value.toOption[YMap] match {
            case Some(map) =>
              map.key(
                "required",
                entry => {
                  val required = ScalarNode(entry.value).boolean().value.asInstanceOf[Boolean]
                  // explicitRequired = Some(Value(AmfScalar(required), Annotations(entry) += ExplicitField()))
                  property.set(PropertyShapeModel.MinCount,
                               AmfScalar(if (required) 1 else 0),
                               Annotations(entry) += ExplicitField())
                }
              )

            case _ =>
          }
          setPathTo(property, entry.key.as[YScalar].text, Annotations(entry.key))

          if (property.fields.?(PropertyShapeModel.MinCount).isEmpty) {
            if (property.patternName.option().isDefined) {
              property.set(PropertyShapeModel.MinCount, 0)
            } else {
              val required = !propName.endsWith("?")

              property.set(PropertyShapeModel.MinCount, if (required) 1 else 0)
              property.set(
                PropertyShapeModel.Name,
                if (required) propName else propName.stripSuffix("?").stripPrefix("/").stripSuffix("/")) // TODO property id is using a name that is not final.
              setPathTo(property, entry.key.as[YScalar].text.stripSuffix("?"), Annotations(entry.key))
            }
          }
          Raml10TypeParser(entry,
                           shape => shape.adopted(property.id),
                           TypeInfo(isPropertyOrParameter = true),
                           StringDefaultType)
            .parse()
            .foreach { range =>
              if (entry.value.tagType == YType.Null) {
                range.annotations += SynthesizedField()
              }

              property.set(PropertyShapeModel.Range, range)
            }

          Some(property)

        case None =>
          // TODO get parent id
          ctx.eh.violation(InvalidPropertyType, "", "Invalid property name", entry.key)
          None
      }
    }
  }

  abstract class ShapeParser extends RamlTypeSyntax {

    val shape: Shape
    val map: YMap
    lazy val dataNodeParser: YNode => DataNode = DataNodeParser.parse(Some(shape.id), new IdCounter)
    lazy val enumParser: YNode => DataNode     = CommonEnumParser(shape.id)

    def parse(): Shape = {

      parseInheritance()

      map.key("displayName", (ShapeModel.DisplayName in shape).allowingAnnotations)
      map.key("description", (ShapeModel.Description in shape).allowingAnnotations)

      map.key(
        "default",
        entry => {
          entry.value.tagType match {
            case YType.Null =>
            case _ =>
              val dataNodeResult = NodeDataNodeParser(entry.value, shape.id + "/default", quiet = false).parse()
              shape.setDefaultStrValue(entry)
              dataNodeResult.dataNode.foreach { dataNode =>
                shape.set(ShapeModel.Default, dataNode, Annotations(entry))
              }
          }
        }
      )

      map.key("enum", ShapeModel.Values in shape using enumParser)

      map.key("minItems", (ArrayShapeModel.MinItems in shape).allowingAnnotations)
      map.key("maxItems", (ArrayShapeModel.MaxItems in shape).allowingAnnotations)
      map.key("externalDocs".asRamlAnnotation,
              AnyShapeModel.Documentation in shape using (OasLikeCreativeWorkParser.parse(_, shape.id)))

      map.key(
        "xml",
        entry => {
          val xmlSerializer: XMLSerializer =
            XMLSerializerParser(shape.name.value(), entry.value).parse()
          shape.set(AnyShapeModel.XMLSerialization, xmlSerializer, Annotations(entry))
        }
      )

      // Logical constraints
      if (map.key("or".asRamlAnnotation).isDefined) OrConstraintParser(map, shape).parse()
      if (map.key("and".asRamlAnnotation).isDefined) AndConstraintParser(map, shape).parse()
      if (map.key("xone".asRamlAnnotation).isDefined) XoneConstraintParser(map, shape).parse()
      if (map.key("not".asRamlAnnotation).isDefined) NotConstraintParser(map, shape).parse()
      map.key("readOnly".asRamlAnnotation, PropertyShapeModel.ReadOnly in shape)

      // Custom shape property definitions, not instances, those are parsed at the end of the parsing process
      map.key(
        "facets",
        entry =>
          PropertiesParser(
            entry.value.as[YMap],
            name => {
              val propertyShape = shape.withCustomShapePropertyDefinition(name)
              if (name.startsWith("("))
                ctx.eh.violation(InvalidFragmentType,
                                 propertyShape.id,
                                 s"User defined facet name '$name' must not begin with open parenthesis",
                                 entry)
              propertyShape
            }
          ).parse()
      )

      // Explicit annotation for the type property
      map.key("type", entry => shape.annotations += TypePropertyLexicalInfo(Range(entry.key.range)))

      shape
    }

    protected def parseInheritance(): Unit = {
      typeOrSchema(map).foreach(entry => InheritanceParser(entry, shape, Some(map)).parse())
    }
  }

  def isLexical: Annotation => Boolean =
    (a: Annotation) =>
      a.isInstanceOf[SourceAST] || a.isInstanceOf[SourceNode] || a.isInstanceOf[SourceLocation] || a
        .isInstanceOf[LexicalInformation]

}
