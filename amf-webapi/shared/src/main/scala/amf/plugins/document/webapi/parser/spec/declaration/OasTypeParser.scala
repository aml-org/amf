package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.{ExplicitField, NilUnion, SynthesizedField}
import amf.core.metamodel.Field
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.DataType
import amf.core.model.domain._
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.{Annotations, ScalarNode, _}
import amf.core.remote.Vendor
import amf.core.utils.{AmfStrings, IdCounter}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.annotations.{CollectionFormatFromItems, JSONSchemaId}
import amf.plugins.document.webapi.contexts._
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.async.Async20WebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.Oas3WebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, DataNodeParser, ScalarNodeParser, YMapEntryLike}
import amf.plugins.document.webapi.parser.spec.declaration.types.TypeDetector
import amf.plugins.document.webapi.parser.spec.domain.{ExampleOptions, ExamplesDataParser, NodeDataNodeParser, RamlExamplesParser}
import amf.plugins.document.webapi.parser.spec.common.{
  AnnotationParser,
  DataNodeParser,
  ScalarNodeParser,
  YMapEntryLike
}
import amf.plugins.document.webapi.parser.spec.domain.{
  ExampleOptions,
  ExamplesDataParser,
  NodeDataNodeParser,
  RamlExamplesParser
}
import amf.plugins.document.webapi.parser.spec.jsonschema.parser.{ContentParser, UnevaluatedParser}
import amf.plugins.document.webapi.parser.spec.oas.OasSpecParser
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models.TypeDef._
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.parser.XsdTypeDefMapping
import amf.plugins.domain.webapi.annotations.TypePropertyLexicalInfo
import amf.plugins.domain.webapi.metamodel.IriTemplateMappingModel.{LinkExpression, TemplateVariable}
import amf.plugins.domain.webapi.models.IriTemplateMapping
import amf.validation.DialectValidations.InvalidUnionType
import amf.validations.ParserSideValidations._
import org.yaml.model._

import scala.collection.mutable
import scala.util.Try

/**
  * OpenAPI Type Parser.
  */
/*
 * TODO: Refactor. We should have a proper JsonSchema parser and an OAS that overrides certain parts of it.
 *  Extract different shape parsers. Possibly implement a Chain of Responsibility for each key in type map?
 */
object OasTypeParser {

  def apply(entry: YMapEntry, adopt: Shape => Unit, version: SchemaVersion)(
      implicit ctx: OasLikeWebApiContext): OasTypeParser =
    new OasTypeParser(YMapEntryLike(entry), entry.key.as[String], entry.value.as[YMap], adopt, version)

  def apply(entry: YMapEntry, adopt: Shape => Unit)(implicit ctx: OasLikeWebApiContext): OasTypeParser =
    new OasTypeParser(
      YMapEntryLike(entry),
      entry.key.as[YScalar].text,
      entry.value.as[YMap],
      adopt,
      getSchemaVersion(ctx)
    )

  def buildDeclarationParser(entry: YMapEntry, adopt: Shape => Unit)(
      implicit ctx: OasLikeWebApiContext): OasTypeParser =
    new OasTypeParser(
      YMapEntryLike(entry),
      entry.key.as[YScalar].text,
      entry.value.as[YMap],
      adopt,
      getSchemaVersion(ctx),
      true
    )

  def apply(node: YMapEntryLike, name: String, adopt: Shape => Unit, version: SchemaVersion)(
      implicit ctx: OasLikeWebApiContext): OasTypeParser =
    new OasTypeParser(node, name, node.asMap, adopt, version)

  def apply(node: YNode, name: String, adopt: Shape => Unit)(implicit ctx: OasLikeWebApiContext): OasTypeParser =
    new OasTypeParser(YMapEntryLike(node), name, node.as[YMap], adopt, OAS20SchemaVersion("schema")(ctx.eh))

  private def getSchemaVersion(ctx: OasLikeWebApiContext) = {
    if (ctx.vendor == Vendor.OAS30) OAS30SchemaVersion("schema")(ctx.eh)
    else if (ctx.vendor == Vendor.ASYNC20) JSONSchemaDraft7SchemaVersion
    else OAS20SchemaVersion("schema")(ctx.eh)
  }
}

case class OasTypeParser(entryOrNode: YMapEntryLike,
                         name: String,
                         map: YMap,
                         adopt: Shape => Unit,
                         version: SchemaVersion,
                         isDeclaration: Boolean = false)(implicit val ctx: OasLikeWebApiContext)
    extends OasSpecParser {

  private val ast: YPart = entryOrNode.ast

  private val nameAnnotations: Annotations = entryOrNode.key.map(n => Annotations(n)).getOrElse(Annotations())

  def parse(): Option[AnyShape] = {

    if (detectDisjointUnion()) {
      validateUnionType()
      Some(parseDisjointUnionType())
    } else {
      val parsedShape = detect(version) match {
        case UnionType                   => Some(parseUnionType())
        case LinkType                    => parseLinkType()
        case ObjectType                  => Some(parseObjectType())
        case ArrayType                   => Some(parseArrayType())
        case AnyType                     => Some(parseAnyType())
        case typeDef if typeDef.isScalar => Some(parseScalarType(typeDef))
        case _                           => None
      }
      parsedShape match {
        case Some(shape: AnyShape) =>
          if (isOas) // external schemas can have any top level key
            ctx.closedShape(shape.id, map, version.asInstanceOf[OASSchemaVersion].position)
          if (isOas3) Some(checkNilUnion(shape))
          else Some(shape)
        case None => None
      }
    }
  }

  def validateUnionType(): Unit =
    if (version.isInstanceOf[OAS30SchemaVersion])
      ctx.eh.violation(InvalidJsonSchemaType,
                       "",
                       s"Value of field 'type' must be a string, multiple types are not supported",
                       map.key("type").get)

  protected def isOas: Boolean  = version.isInstanceOf[OASSchemaVersion]
  protected def isOas3: Boolean = version.isInstanceOf[OAS30SchemaVersion]

  def checkNilUnion(parsed: AnyShape): AnyShape = {
    map.key("nullable") match {
      case Some(nullableEntry) if nullableEntry.value.toOption[Boolean].getOrElse(false) =>
        val union = UnionShape().withName(name, nameAnnotations).withId(parsed.id + "/nilUnion")
        parsed.annotations += NilUnion(Range(nullableEntry.key.range).toString())
        union.withAnyOf(
          Seq(
            parsed,
            NilShape().withId(union.id + "_nil")
          )
        )
        union
      case _ =>
        parsed
    }
  }

  /**
    * JSON Schema allows to define multiple types for a shape.
    * In this case we can parse this as a union because properties
    * are going to be disjoint for each of them
    * @return
    */
  private def detectDisjointUnion(): Boolean = {
    map.key("type").isDefined && map.key("type").get.value.asOption[YSequence].isDefined
  }

  private def detect(version: SchemaVersion): TypeDef = {
    val defaultType = version match {
      case oasSchema: OASSchemaVersion if oasSchema.position == "parameter" => UndefinedType
      case _                                                                => AnyType
    }
    TypeDetector.detect(map)(ctx.eh).getOrElse(defaultType)
  }

  private def parseDisjointUnionType(): UnionShape = {

    // val detectedTypes = map.key("type").get.value.as[YSequence].nodes.map(_.as[String])
    val filtered = YMap(map.entries.filter(_.key.as[String] != "type"), map.sourceName)

    val parser = UnionShapeParser(YMapEntryLike(filtered), name)
    adopt(parser.shape) // We need to set the shape id before parsing to properly adopt nested nodes
    val union = parser.parse()

    val finals = filtered.entries.filter { entry =>
      val prop = entry.key.as[String]
      prop != "example" && prop != "examples".asOasExtension && prop != "title" &&
      prop != "description" && prop != "default" && prop != "enum" &&
      prop != "externalDocs" && prop != "xml" && prop != "facets".asOasExtension &&
      prop != "anyOf" && prop != "allOf" && prop != "oneOf" && prop != "not"
    }

    val exclusiveProps = YMap(finals, finals.headOption.map(_.sourceName).getOrElse(""))
    var i              = 0
    val parsedTypes: Seq[AmfElement] = map.key("type").get.value.as[YSequence].nodes map { node =>
      i += 1
      if (node.tagType == YType.Str) {
        node.as[String] match {
          case "object" =>
            Some(parseObjectType(name + i, exclusiveProps, s => s.withId(union.id + "/object")))
          case "array" =>
            Some(parseArrayType(name + i, exclusiveProps, s => s.withId(union.id + "/array")))
          case "number" =>
            Some(parseScalarType(TypeDef.NumberType, name + i, exclusiveProps, s => s.withId(union.id + "/number")))
          case "integer" =>
            Some(parseScalarType(TypeDef.IntType, name + i, exclusiveProps, s => s.withId(union.id + "/integer")))
          case "string" =>
            Some(parseScalarType(TypeDef.StrType, name + i, exclusiveProps, s => s.withId(union.id + "/string")))
          case "boolean" =>
            Some(parseScalarType(TypeDef.BoolType, name + i, exclusiveProps, s => s.withId(union.id + "/boolean")))
          case "null" =>
            Some(parseScalarType(TypeDef.NilType, name + i, exclusiveProps, s => s.withId(union.id + "/nil")))
          case "any" =>
            Some(parseAnyType(name + i, exclusiveProps, s => s.withId(union.id + "/any")))
          case other =>
            ctx.eh.violation(InvalidDisjointUnionType,
                             union.id,
                             s"Invalid type for disjointUnion $other",
                             map.key("type").get.value)
            None
        }
      } else if (node.tagType == YType.Map) {
        val entry = YMapEntry(s"union_member_$i", node)
        OasTypeParser(entry, shape => shape.adopted(union.id), version).parse()
      } else {
        ctx.eh.violation(InvalidDisjointUnionType,
                         union.id,
                         s"Invalid type for disjointUnion ${node.tagType}",
                         map.key("type").get.value)
        None
      }
    } collect { case Some(t: AmfElement) => t }

    if (parsedTypes.nonEmpty) union.setArrayWithoutId(UnionShapeModel.AnyOf, parsedTypes)

    union
  }

  private def parseScalarType(typeDef: TypeDef,
                              name: String = name,
                              map: YMap = map,
                              adopt: Shape => Unit = adopt): AnyShape = {
    val parsed = typeDef match {
      case NilType =>
        val shape = NilShape(ast).withName(name, nameAnnotations)
        adopt(shape)
        shape
      case FileType =>
        val shape = FileShape(ast).withName(name, nameAnnotations)
        adopt(shape)
        FileShapeParser(typeDef, shape, map).parse()
      case _ =>
        val shape = ScalarShape(ast).withName(name, nameAnnotations)
        adopt(shape)
        ScalarShapeParser(typeDef, shape, map).parse()
    }
    parsed
  }

  private def parseAnyType(name: String = name, map: YMap = map, adopt: Shape => Unit = adopt): AnyShape = {
    val shape = AnyShape(ast).withName(name, nameAnnotations)
    adopt(shape)
    AnyTypeShapeParser(shape, map).parse()
  }

  private def parseArrayType(name: String = name, map: YMap = map, adopt: Shape => Unit = adopt): AnyShape = {
    DataArrangementParser(name, ast, map, (shape: Shape) => adopt(shape)).parse()
  }

  private def parseLinkType(): Option[AnyShape] =
    new OasRefParser(map, name, nameAnnotations, ast, adopt, version).parse()

  private def parseObjectType(name: String = name, map: YMap = map, adopt: Shape => Unit = adopt): AnyShape = {
    if (map.key("schema".asOasExtension).isDefined) {
      val shape = SchemaShape(ast).withName(name, nameAnnotations)
      adopt(shape)
      SchemaShapeParser(shape, map).parse()
    } else {
      val shape = NodeShape(ast).withName(name, nameAnnotations)
      checkJsonIdentity(shape, map, adopt, ctx.declarations.futureDeclarations)
      NodeShapeParser(shape, map).parse()
    }
  }

  private def checkJsonIdentity(shape: AnyShape,
                                map: YMap,
                                adopt: Shape => Unit,
                                futureDeclarations: FutureDeclarations): Unit = {
    adopt(shape)
    if (!isOas && !isOas3) {
      map.map.get("id") foreach { f =>
        f.asOption[String].foreach { id =>
          futureDeclarations.resolveRef(id, shape)
          ctx.registerJsonSchema(id, shape)
        }
      }
    } else if (isOas && isDeclaration && ctx.isMainFileContext && shape.name.option().isDefined) {
      val localRef = buildLocalRef(shape.name.option().get)
      val fullRef  = ctx.loc + localRef
      ctx.registerJsonSchema(fullRef, shape)
    }

    def buildLocalRef(name: String) = ctx match {
      case _: Oas3WebApiContext    => s"#/components/schemas/$name"
      case _: Async20WebApiContext => s"#/components/schemas/$name"
      case _                       => s"#/definitions/$name"
    }
  }

  private def parseUnionType(): UnionShape = UnionShapeParser(entryOrNode, name).parse()

  trait CommonScalarParsingLogic {
    def parseScalar(map: YMap, shape: Shape, typeDef: TypeDef): TypeDef = {
      typeDef match {
        case TypeDef.StrType | TypeDef.FileType =>
          map.key("pattern", ScalarShapeModel.Pattern in shape)
          map.key("minLength", ScalarShapeModel.MinLength in shape)
          map.key("maxLength", ScalarShapeModel.MaxLength in shape)
        case n if n.isNumber =>
          setValue("minimum", map, ScalarShapeModel.Minimum, shape)
          setValue("maximum", map, ScalarShapeModel.Maximum, shape)
          map.key("multipleOf", ScalarShapeModel.MultipleOf in shape)
          if (version isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion) {
            parseNumericExclusive(map, shape)
          } else {
            map.key("exclusiveMinimum", ScalarShapeModel.ExclusiveMinimum in shape)
            map.key("exclusiveMaximum", ScalarShapeModel.ExclusiveMaximum in shape)
          }
        case _ => // Nothing to do
      }
      ScalarFormatType(shape, typeDef).parse(map)
    }

    private def parseNumericExclusive(map: YMap, shape: Shape): Unit = {
      if (map.key("exclusiveMinimum").isDefined) {
        setValue("exclusiveMinimum", map, ScalarShapeModel.Minimum, shape)
        shape.set(ScalarShapeModel.ExclusiveMinimum, AmfScalar(true), Annotations(SynthesizedField()))
      }
      if (map.key("exclusiveMaximum").isDefined) {
        setValue("exclusiveMaximum", map, ScalarShapeModel.Maximum, shape)
        shape.set(ScalarShapeModel.ExclusiveMaximum, AmfScalar(true), Annotations(SynthesizedField()))
      }
    }

    private def setValue(key: String, map: YMap, field: Field, shape: Shape): Unit =
      map.key(key, entry => {
        val value = ScalarNode(entry.value)
        shape.set(field, value.text(), Annotations(entry))
      })
  }

  case class ScalarShapeParser(typeDef: TypeDef, shape: ScalarShape, map: YMap)
      extends AnyShapeParser()
      with CommonScalarParsingLogic {

    override lazy val dataNodeParser: YNode => DataNode = ScalarNodeParser(parent = Some(shape.id)).parse
    override lazy val enumParser: YNode => DataNode     = CommonEnumParser(shape.id, enumType = EnumParsing.SCALAR_ENUM)

    override def parse(): ScalarShape = {
      super.parse()
      val validatedTypeDef = parseScalar(map, shape, typeDef)

      map
        .key("type")
        .fold(shape
          .set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(validatedTypeDef)), Annotations()))(entry =>
          shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(validatedTypeDef)), Annotations(entry)))

      if (isStringScalar(shape) && version.isBiggerThanOrEqualTo(JSONSchemaDraft7SchemaVersion)) {
        ContentParser(s => s.adopted(shape.id), version).parse(shape, map)
      }

      shape
    }

    private def isStringScalar(shape: ScalarShape) = shape.dataType.option().fold(false) { value => value == DataType.String}
  }

  case class UnionShapeParser(nodeOrEntry: YMapEntryLike, name: String) extends AnyShapeParser() {

    val node: YNode = nodeOrEntry.value

    private def nameAnnotations: Annotations = nodeOrEntry.key.map(n => Annotations(n)).getOrElse(Annotations())
    override val map: YMap                   = node.as[YMap]

    override val shape: UnionShape = {
      val union = UnionShape(Annotations.valueNode(node)).withName(name, nameAnnotations)
      adopt(union)
      union
    }

    override def parse(): UnionShape = {
      super.parse()

      map.key(
        "x-amf-union", { entry =>
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val unionNodes = seq.zipWithIndex
                .map {
                  case (unionNode, index) =>
                    val entry = YMapEntry(YNode(s"item$index"), unionNode)
                    OasTypeParser(entry, item => item.adopted(shape.id + "/items/" + index), version).parse()
                }
                .filter(_.isDefined)
                .map(_.get)
              shape.setArray(UnionShapeModel.AnyOf, unionNodes, Annotations(entry.value))
            case _ =>
              ctx.eh.violation(InvalidUnionType, shape.id, "Unions are built from multiple shape nodes", entry.value)

          }
        }
      )

      shape
    }
  }

  case class OrConstraintParser(map: YMap, shape: Shape) {

    def parse(): Unit = {
      map.key(
        "anyOf", { entry =>
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val unionNodes = seq.zipWithIndex
                .map {
                  case (node, index) =>
                    val entry = YMapEntry(YNode(s"item$index"), node)
                    OasTypeParser(entry, item => item.adopted(shape.id + "/or/" + index), version)
                      .parse()
                }
                .filter(_.isDefined)
                .map(_.get)
              shape.setArrayWithoutId(ShapeModel.Or, unionNodes, Annotations(entry.value))
            case _ =>
              ctx.eh.violation(InvalidOrType,
                               shape.id,
                               "Or constraints are built from multiple shape nodes",
                               entry.value)

          }
        }
      )
    }
  }

  case class AndConstraintParser(map: YMap, shape: Shape) {

    def parse(): Unit = {
      map.key(
        "allOf", { entry =>
          adopt(shape)
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val andNodes = seq.zipWithIndex
                .map {
                  case (node, index) =>
                    val entry = YMapEntry(YNode(s"item$index"), node)
                    OasTypeParser(entry, item => item.adopted(shape.id + "/and/" + index), version)
                      .parse()
                }
                .filter(_.isDefined)
                .map(_.get)
              shape.setArrayWithoutId(ShapeModel.And, andNodes, Annotations(entry.value))
            case _ =>
              ctx.eh.violation(InvalidAndType,
                               shape.id,
                               "And constraints are built from multiple shape nodes",
                               entry.value)

          }
        }
      )
    }
  }

  case class XoneConstraintParser(map: YMap, shape: Shape) {

    def parse(): Unit = {
      map.key(
        "oneOf", { entry =>
          adopt(shape)
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val nodes = seq.zipWithIndex
                .map {
                  case (node, index) =>
                    val entry = YMapEntry(YNode(s"item$index"), node)
                    OasTypeParser(entry, item => item.adopted(shape.id + "/xone/" + index), version).parse()
                }
                .filter(_.isDefined)
                .map(_.get)
              shape.setArrayWithoutId(ShapeModel.Xone, nodes, Annotations(entry.value))
            case _ =>
              ctx.eh.violation(InvalidXoneType,
                               shape.id,
                               "Xone constraints are built from multiple shape nodes",
                               entry.value)

          }
        }
      )
    }
  }

  case class InnerShapeParser(key: String, field: Field, map: YMap, shape: Shape) {

    def parse(): Unit = {
      map.key(
        key, { entry =>
          adopt(shape)
          OasTypeParser(entry, item => item.adopted(shape.id + s"/$key"), version).parse() match {
            case Some(parsedShape) =>
              shape.set(field, parsedShape)
            case _ => // ignore
          }
        }
      )
    }
  }

  case class DataArrangementParser(name: String, ast: YPart, map: YMap, adopt: Shape => Unit) {

    def lookAhead(): Option[Either[TupleShape, ArrayShape]] = {
      map.key("items") match {
        case Some(entry) =>
          entry.value.to[Seq[YNode]] match {
            // this is a sequence, we need to create a tuple
            case Right(_) => Some(Left(TupleShape(ast).withName(name, nameAnnotations)))
            // not an array regular array parsing
            case _ => Some(Right(ArrayShape(ast).withName(name, nameAnnotations)))

          }
        case None => None
      }
    }

    def parse(): AnyShape = {
      lookAhead() match {
        case None =>
          val array = ArrayShape(ast).withName(name, nameAnnotations)
          val shape = ArrayShapeParser(array, map, adopt).parse()
          validateMissingItemsField(shape)
          shape
        case Some(Left(tuple))  => TupleShapeParser(tuple, map, adopt).parse()
        case Some(Right(array)) => ArrayShapeParser(array, map, adopt).parse()
      }
    }

    private def validateMissingItemsField(shape: Shape): Unit = {
      if (version.isInstanceOf[OAS30SchemaVersion]) {
        ctx.eh.violation(ItemsFieldRequired, shape.id, "'items' field is required when schema type is array", map)
      }
    }

  }

  trait DataArrangementShapeParser extends AnyShapeParser {

    override def parse(): AnyShape = {
      super.parse()

      map.key("minItems", ArrayShapeModel.MinItems in shape)
      map.key("maxItems", ArrayShapeModel.MaxItems in shape)
      map.key("uniqueItems", ArrayShapeModel.UniqueItems in shape)

      if (version isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion)
        InnerShapeParser("contains", ArrayShapeModel.Contains, map, shape).parse()
      shape
    }

  }

  case class TupleShapeParser(shape: TupleShape, map: YMap, adopt: Shape => Unit)
      extends DataArrangementShapeParser() {

    override def parse(): AnyShape = {
      adopt(shape)
      super.parse()

      map.key("additionalItems").foreach { entry =>
        entry.value.tagType match {
          case YType.Bool =>
            (TupleShapeModel.ClosedItems in shape).negated(entry)
            if (version isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion)
              additionalItemViolation(entry, "Invalid part type for additional items node. Expected a map")
          case YType.Map =>
            OasTypeParser(entry, s => s.adopted(shape.id), version).parse().foreach { s =>
              shape.set(TupleShapeModel.AdditionalItemsSchema, s, Annotations(entry))
            }
          case _ =>
            additionalItemViolation(
              entry,
              if (version isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion)
                "Invalid part type for additional items node. Expected a map"
              else
                "Invalid part type for additional items node. Should be a boolean or a map"
            )
        }
      }
      map.key(
        "items",
        entry => {
          val items = entry.value
            .as[YSequence]
            .nodes
            .collect { case node if node.tagType == YType.Map => node }
            .zipWithIndex
            .map {
              case (elem, index) =>
                OasTypeParser(YMapEntryLike(elem), s"member$index", item => item.adopted(shape.id + "/items/" + index), version)
                  .parse()
            }
          shape.withItems(items.filter(_.isDefined).map(_.get))
        }
      )

      shape
    }

    private def additionalItemViolation(entry: YMapEntry, msg: String): Unit = {
      ctx.eh.violation(InvalidAdditionalItemsType, shape.id, msg, entry)
    }
  }

  case class ArrayShapeParser(shape: ArrayShape, map: YMap, adopt: Shape => Unit)
      extends DataArrangementShapeParser() {
    override def parse(): AnyShape = {
      checkJsonIdentity(shape, map, adopt, ctx.declarations.futureDeclarations)
      super.parse()

      map.key("collectionFormat", ArrayShapeModel.CollectionFormat in shape)

      map
        .key("items")
        .flatMap(_.value.toOption[YMap])
        .foreach(
          _.key("collectionFormat",
                (ArrayShapeModel.CollectionFormat in shape)
                  .withAnnotation(CollectionFormatFromItems())))

      val finalShape = for {
        entry <- map.key("items")
        item  <- OasTypeParser(entry, items => items.adopted(shape.id + "/items"), version).parse()
      } yield {
        item match {
          case array: ArrayShape   => shape.withItems(array).toMatrixShape
          case matrix: MatrixShape => shape.withItems(matrix).toMatrixShape
          case other: AnyShape     => shape.withItems(other)
        }
      }

      if (version.isBiggerThanOrEqualTo(JSONSchemaDraft201909SchemaVersion)) {
        new UnevaluatedParser(version, UnevaluatedParser.unevaluatedItemsInfo).parse(map, shape)
      }

      finalShape match {
        case Some(parsed: AnyShape) => parsed.withId(shape.id)
        case None                   => shape.withItems(AnyShape())
      }
    }
  }

  case class AnyTypeShapeParser(shape: AnyShape, map: YMap) extends AnyShapeParser {
    override val options: ExampleOptions = ExampleOptions(strictDefault = true, quiet = true)
  }

  abstract class AnyShapeParser() extends ShapeParser() {

    override val shape: AnyShape
    val options: ExampleOptions = ExampleOptions(strictDefault = true, quiet = false)
    override def parse(): AnyShape = {
      super.parse()
      parseExample()

      map.key("type", _ => shape.add(ExplicitField())) // todo lexical of type?? new annotation?

      if (version isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion)
        map.key("$comment", AnyShapeModel.Comment in shape)

      shape
    }

    private def parseExample(): Unit = {

      if (version isBiggerThanOrEqualTo JSONSchemaDraft6SchemaVersion) parseExamplesArray()
      else
        RamlExamplesParser(map, "example", "examples".asOasExtension, shape, options)
          .parse()
    }

    private def parseExamplesArray(): Unit =
      map
        .key("examples")
        .map { entry =>
          val sequence = entry.value.as[YSequence]
          val examples = ExamplesDataParser(sequence, options, shape.id).parse()
          shape.setArrayWithoutId(AnyShapeModel.Examples, examples, Annotations(entry))
        }
  }

  case class NodeShapeParser(shape: NodeShape, map: YMap)(implicit val ctx: OasLikeWebApiContext)
      extends AnyShapeParser() {
    override def parse(): NodeShape = {

      super.parse()

      map.key("type", _ => shape.add(ExplicitField()))

      map.key("minProperties", NodeShapeModel.MinProperties in shape)
      map.key("maxProperties", NodeShapeModel.MaxProperties in shape)

      shape.set(NodeShapeModel.Closed, value = false)

      map.key("additionalProperties").foreach { entry =>
        entry.value.tagType match {
          case YType.Bool => (NodeShapeModel.Closed in shape).negated.explicit(entry)
          case YType.Map =>
            OasTypeParser(entry, s => s.adopted(shape.id), version).parse().foreach { s =>
              shape.set(NodeShapeModel.AdditionalPropertiesSchema, s, Annotations(entry))
            }
          case _ =>
            ctx.eh.violation(InvalidAdditionalPropertiesType,
                             shape.id,
                             "Invalid part type for additional properties node. Should be a boolean or a map",
                             entry)
        }
      }

      if (version.isBiggerThanOrEqualTo(JSONSchemaDraft201909SchemaVersion)) {
        new UnevaluatedParser(version, UnevaluatedParser.unevaluatedPropertiesInfo).parse(map, shape)
      }

      if (isOas3) {
        map.key("discriminator", DiscriminatorParser(shape, _).parse())
      } else {
        map.key("discriminator", NodeShapeModel.Discriminator in shape)
        map.key("discriminatorValue".asOasExtension, NodeShapeModel.DiscriminatorValue in shape)
      }

      val requiredFields = parseRequiredFields(map, shape)

      val properties  = mutable.LinkedHashMap[String, PropertyShape]()
      val properEntry = map.key("properties")
      properEntry.foreach(entry => {
        Option(entry.value.as[YMap]) match {
          case Some(m) =>
            val props = PropertiesParser(m, shape.withProperty, requiredFields).parse()
            properties ++= props.map(p => p.name.value() -> p)
          case _ => // Empty properties node.
        }
      })
      if (version isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion)
        InnerShapeParser("propertyNames", NodeShapeModel.PropertyNames, map, shape).parse()

      val patternPropEntry = map.key("patternProperties")

      patternPropEntry.foreach(entry => {
        entry.value.toOption[YMap] match {
          case Some(m) =>
            properties ++=
              PropertiesParser(m, shape.withProperty, requiredFields, patterned = true)
                .parse()
                .map(p => p.name.value() -> p)
          case _ => // Empty properties node.
        }
      })
      val (entryAnnotations, valueAnnotations) = properEntry.map { pe =>
        Annotations(pe.value) -> Annotations(pe)
      } orElse {
        patternPropEntry.map { pp =>
          Annotations(pp.value) -> Annotations(pp)
        }
      } getOrElse { Annotations() -> Annotations() }

      if (properties.nonEmpty)
        shape.set(NodeShapeModel.Properties, AmfArray(properties.values.toSeq, entryAnnotations), valueAnnotations)
      shape.properties.foreach(p => properties += (p.name.value() -> p))

      parseShapeDependencies(shape, properties)

      map.key(
        "x-amf-merge",
        entry => {
          val inherits = AllOfParser(entry.value.as[Seq[YNode]], s => s.adopted(shape.id)).parse()
          shape.set(NodeShapeModel.Inherits, AmfArray(inherits, Annotations(entry.value)), Annotations(entry))
        }
      )

      shape
    }
  }

  private def parseShapeDependencies(shape: NodeShape, properties: mutable.LinkedHashMap[String, PropertyShape]): Unit = {
    if (version == JSONSchemaDraft201909SchemaVersion) {
      Draft2019ShapeDependenciesParser(shape, map, shape.id, properties.toMap, version).parse()
    } else {
      map.key(
        "dependencies",
        entry => {
          Draft4ShapeDependenciesParser(shape, entry.value.as[YMap], shape.id, properties.toMap, version).parse()
        }
      )
    }
  }

  private def parseRequiredFields(map: YMap, shape: NodeShape): Map[String, YNode] = {

    def parse(field: YMapEntry): Map[String, YNode] = {
      val defaultValue = Map[String, YNode]()
      val requiredSeq  = field.value.asOption[Seq[YNode]]
      requiredSeq match {
        case Some(required) =>
          val requiredGroup = required.groupBy(_.as[String])
          validateRequiredFields(requiredGroup, shape)
          requiredGroup.map {
            case (key, nodes) => key -> nodes.head
          }
        case None =>
          ctx.eh.violation(InvalidRequiredValue, shape.id, "'required' field has to be an array", loc = field.location)
          defaultValue
      }
    }

    val defaultValue = Map[String, YNode]()
    map
      .key("required")
      .map { field =>
        (field.value.tagType, version) match {
          case (YType.Seq, JSONSchemaDraft3SchemaVersion) =>
            ctx.eh.violation(InvalidRequiredArrayForSchemaVersion,
                             shape.id,
                             "Required arrays of properties not supported in JSON Schema below version draft-4",
                             field.value)
            defaultValue
          case (_, JSONSchemaDraft3SchemaVersion)        => defaultValue
          case (YType.Seq, JSONSchemaUnspecifiedVersion) => parse(field)
          case (_, JSONSchemaUnspecifiedVersion)         => defaultValue
          case (_, _)                                    => parse(field)
        }
      }
      .getOrElse(defaultValue)
  }

  private def validateRequiredFields(required: Map[String, Seq[YNode]], shape: NodeShape): Unit =
    required
      .foreach {
        case (name, nodes) if nodes.size > 1 =>
          ctx.eh.violation(DuplicateRequiredItem,
                           shape.id,
                           s"'$name' is duplicated in 'required' property",
                           nodes.last)
        case _ => // ignore
      }

  case class DiscriminatorParser(shape: NodeShape, entry: YMapEntry) {
    def parse(): Unit = {
      val map = entry.value.as[YMap]
      map.key("propertyName") match {
        case Some(entry) =>
          (NodeShapeModel.Discriminator in shape)(entry)
        case None =>
          ctx.eh.violation(DiscriminatorNameRequired, shape.id, s"Discriminator must have a propertyName defined", map)
      }
      map.key("mapping", parseMappings)
      ctx.closedShape(shape.id, map, "discriminator")
    }

    private def parseMappings(mappingEntry: YMapEntry): Unit = {
      val map = mappingEntry.value.as[YMap]
      val mappings = map.entries.map(entry => {
        val mapping  = IriTemplateMapping(Annotations(entry))
        val element  = ScalarNode(entry.key).string()
        val variable = ScalarNode(entry.value).string()
        mapping.set(TemplateVariable, element, Annotations(entry.key))
        mapping.set(LinkExpression, variable, Annotations(entry.value))
      })
      shape.setArray(NodeShapeModel.DiscriminatorMapping, mappings, Annotations(mappingEntry))
    }
  }

  case class AllOfParser(array: Seq[YNode], adopt: Shape => Unit) {
    def parse(): Seq[Shape] =
      array
        .flatMap(n => {
          n.toOption[YMap]
            .flatMap(declarationsRef)
            .orElse(OasTypeParser(YMapEntryLike(n), "", adopt, version).parse())
        })

    private def declarationsRef(entries: YMap): Option[Shape] = {
      entries
        .key("$ref")
        .flatMap { entry =>
          ctx.declarations.shapes.get(entry.value.as[String].stripPrefix("#/definitions/")) map { declaration =>
            declaration
              .link(entry.value.as[String], Annotations(entry.value))
              .asInstanceOf[AnyShape]
              .withName(declaration.name.option().getOrElse("schema"), Annotations())
          }
        }
    }
  }

  case class PropertiesParser(map: YMap,
                              producer: String => PropertyShape,
                              requiredFields: Map[String, YNode],
                              patterned: Boolean = false) {
    def parse(): Seq[PropertyShape] = {
      map.entries.map(entry => PropertyShapeParser(entry, producer, requiredFields, patterned).parse())
    }
  }

  case class PropertyShapeParser(entry: YMapEntry,
                                 producer: String => PropertyShape,
                                 requiredFields: Map[String, YNode],
                                 patterned: Boolean) {

    def parse(): PropertyShape = {

      val name                = entry.key.as[YScalar].text
      val required            = requiredFields.contains(name)
      val requiredAnnotations = requiredFields.get(name).map(node => Annotations(node)).getOrElse(Annotations())

      val property = producer(name)
        .add(Annotations(entry))
        .set(PropertyShapeModel.MinCount, AmfScalar(if (required) 1 else 0), requiredAnnotations += ExplicitField())

      property.set(
        PropertyShapeModel.Path,
        AmfScalar((Namespace.Data + entry.key.as[YScalar].text.urlComponentEncoded).iri(), Annotations(entry.key)))

      if (version.isInstanceOf[OAS20SchemaVersion])
        validateReadOnlyAndRequired(entry.value.toOption[YMap], property, required)

      // This comes from JSON Schema draft-3, we will parse it for backward compatibility but we will not generate it
      entry.value
        .toOption[YMap]
        .foreach(
          _.key(
            "required",
            entry => {
              if (entry.value.tagType == YType.Bool) {
                if (version != JSONSchemaDraft3SchemaVersion) {
                  ctx.eh.warning(InvalidRequiredBooleanForSchemaVersion,
                                 property.id,
                                 "Required property boolean value is only supported in JSON Schema draft-3",
                                 entry)
                }
                val required = ScalarNode(entry.value).boolean().value.asInstanceOf[Boolean]
                property.set(PropertyShapeModel.MinCount,
                             AmfScalar(if (required) 1 else 0),
                             Annotations(entry) += ExplicitField())
              }
            }
          )
        )

      OasTypeParser(entry, shape => shape.adopted(property.id), version)
        .parse()
        .foreach(property.set(PropertyShapeModel.Range, _))

      if (patterned) property.withPatternName(name)

      property
    }

    private def validateReadOnlyAndRequired(map: Option[YMap], property: PropertyShape, isRequired: Boolean): Unit = {
      map.foreach(
        _.key(
          "readOnly",
          readOnlyEntry => {
            val readOnly = Try(readOnlyEntry.value.as[YScalar].text.toBoolean).getOrElse(false)
            if (readOnly && isRequired) {
              ctx.eh.warning(ReadOnlyPropertyMarkedRequired,
                             property.id,
                             "Read only property should not be marked as required by a schema",
                             readOnlyEntry)
            }
          }
        ))
    }
  }

  case class Property(var typeDef: TypeDef = UndefinedType) {
    def withTypeDef(value: TypeDef): Unit = typeDef = value
  }

  abstract class ShapeParser(implicit ctx: WebApiContext) {

    val shape: Shape
    val map: YMap

    lazy val dataNodeParser: YNode => DataNode = DataNodeParser.parse(Some(shape.id), new IdCounter())
    lazy val enumParser: YNode => DataNode     = CommonEnumParser(shape.id)

    def parse(): Shape = {

      map.key("title", ShapeModel.DisplayName in shape)
      map.key("description", ShapeModel.Description in shape)

      map.key(
        "default",
        node => {
          shape.setDefaultStrValue(node)
          NodeDataNodeParser(node.value, shape.id, quiet = false).parse().dataNode.foreach { dn =>
            shape.set(ShapeModel.Default, dn, Annotations(node))
          }

        }
      )

      map.key("enum", ShapeModel.Values in shape using enumParser)
      map.key("externalDocs",
              AnyShapeModel.Documentation in shape using (OasLikeCreativeWorkParser.parse(_, shape.id)))
      map.key("xml", AnyShapeModel.XMLSerialization in shape using XMLSerializerParser.parse(shape.name.value()))

      map.key(
        "facets".asOasExtension,
        entry => PropertiesParser(entry.value.as[YMap], shape.withCustomShapePropertyDefinition, Map()).parse()
      )

      // Explicit annotation for the type property
      map.key("type", entry => shape.annotations += TypePropertyLexicalInfo(Range(entry.key.range)))

      // Logical constraints
      OrConstraintParser(map, shape).parse()
      AndConstraintParser(map, shape).parse()
      XoneConstraintParser(map, shape).parse()
      InnerShapeParser("not", ShapeModel.Not, map, shape).parse()

      map.key("readOnly", ShapeModel.ReadOnly in shape)

      if (version.isInstanceOf[OAS30SchemaVersion] || version.isBiggerThanOrEqualTo(JSONSchemaDraft7SchemaVersion)) {
        map.key("writeOnly", ShapeModel.WriteOnly in shape)
        map.key("deprecated", ShapeModel.Deprecated in shape)
      }

      if (version isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion) parseDraft7Fields()
      // normal annotations
      AnnotationParser(shape, map).parse()

      map.key("id", node => shape.annotations += JSONSchemaId(node.value.as[YScalar].text))
      shape
    }

    private def parseDraft7Fields(): Unit = {
      InnerShapeParser("if", ShapeModel.If, map, shape).parse()
      InnerShapeParser("then", ShapeModel.Then, map, shape).parse()
      InnerShapeParser("else", ShapeModel.Else, map, shape).parse()
      map.key("const", (ShapeModel.Values in shape using dataNodeParser).allowingSingleValue)
    }
  }

  case class FileShapeParser(typeDef: TypeDef, shape: FileShape, map: YMap)
      extends AnyShapeParser()
      with CommonScalarParsingLogic {
    override def parse(): FileShape = {
      super.parse()

      parseScalar(map, shape, typeDef)

      map.key("fileTypes".asOasExtension, FileShapeModel.FileTypes in shape)

      shape
    }
  }

  case class SchemaShapeParser(shape: SchemaShape, map: YMap) extends AnyShapeParser() with CommonScalarParsingLogic {
    super.parse()

    override def parse(): AnyShape = {
      map.key(
        "schema".asOasExtension, { entry =>
          entry.value.to[String] match {
            case Right(str) => shape.withRaw(str)
            case _ =>
              ctx.eh.violation(InvalidSchemaType, shape.id, "Cannot parse non string schema shape", entry.value)
              shape.withRaw("")
          }
        }
      )

      map.key(
        "mediaType".asOasExtension, { entry =>
          entry.value.to[String] match {
            case Right(str) =>
              shape.withMediaType(str)
            case _ =>
              ctx.eh.violation(InvalidMediaTypeType, shape.id, "Cannot parse non string schema shape", entry.value)
              shape.withMediaType("*/*")
          }
        }
      )

      shape
    }
  }
}
