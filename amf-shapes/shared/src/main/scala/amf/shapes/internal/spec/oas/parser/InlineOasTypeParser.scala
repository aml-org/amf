package amf.shapes.internal.spec.oas.parser

import amf.aml.internal.annotations.DiscriminatorExtension
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain._
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.vocabulary.Namespace
import amf.core.internal.annotations.{ExplicitField, InferredProperty, NilUnion}
import amf.core.internal.datanode.{DataNodeParser, ScalarNodeParser}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.DomainElementModel.CustomDomainProperties
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.metamodel.domain.{LinkableElementModel, ShapeModel}
import amf.core.internal.parser._
import amf.core.internal.parser.domain.Annotations.{inferred, synthesized, virtual}
import amf.core.internal.parser.domain.{Annotations, Fields, FutureDeclarations, SearchScope}
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.utils._
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.annotations.{CollectionFormatFromItems, JSONSchemaId, TypePropertyLexicalInfo}
import amf.shapes.internal.domain.metamodel.DiscriminatorValueMappingModel.{
  DiscriminatorValue,
  DiscriminatorValueTarget
}
import amf.shapes.internal.domain.metamodel.IriTemplateMappingModel.{LinkExpression, TemplateVariable}
import amf.shapes.internal.domain.metamodel._
import amf.shapes.internal.domain.metamodel.common.ExamplesField
import amf.shapes.internal.domain.parser.XsdTypeDefMapping
import amf.shapes.internal.spec.SemanticContextParser
import amf.shapes.internal.spec.common.TypeDef._
import amf.shapes.internal.spec.common._
import amf.shapes.internal.spec.common.parser._
import amf.shapes.internal.spec.jsonschema.parser.{
  ContentParser,
  Draft2019ShapeDependenciesParser,
  Draft4ShapeDependenciesParser,
  UnevaluatedParser
}
import amf.shapes.internal.spec.oas.{OasShapeDefinitions, parser}
import amf.shapes.internal.spec.raml.parser.XMLSerializerParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations._
import org.mulesoft.common.collections._
import org.yaml.model._

import scala.collection.mutable
import scala.util.Try

case class InlineOasTypeParser(
    entryOrNode: YMapEntryLike,
    name: String,
    map: YMap,
    adopt: Shape => Unit,
    version: SchemaVersion,
    isDeclaration: Boolean = false
)(implicit val ctx: ShapeParserContext)
    extends QuickFieldParserOps {

  private val ast: YPart = entryOrNode.ast

  private val nameAnnotations: Annotations = entryOrNode.key.map(n => Annotations(n)).getOrElse(Annotations.virtual())

  def parse(): Option[AnyShape] = {

    val parsedShape = if (detectDisjointUnion()) {
      validateUnionType()
      Some(parseDisjointUnionType())
    } else {
      val parsedShape = detect(version) match {
        case UnionType                   => Some(parseUnionType())
        case ObjectType                  => Some(parseObjectType())
        case ArrayType                   => Some(parseArrayType())
        case AnyType                     => Some(parseAnyType())
        case typeDef if typeDef.isScalar => Some(parseScalarType(typeDef))
        case _                           => None
      }
      parsedShape match {
        case Some(shape: AnyShape) =>
          version match {
            case oas: OASSchemaVersion if oas.position != SchemaPosition.Other =>
              ctx.closedShape(shape, map, oas.position.toString)
            case _ => // Nothing to do
          }
          if (isOas3) Some(checkOas30Nullable(shape))
          else Some(shape)
        case None => None
      }
    }

    parsedShape
  }

  private def validateUnionType(): Unit =
    if (version.isInstanceOf[OAS30SchemaVersion])
      ctx.eh.violation(
        InvalidJsonSchemaType,
        "",
        s"Value of field 'type' must be a string, multiple types are not supported",
        map.key("type").get.location
      )

  protected def isOas: Boolean   = version.isInstanceOf[OASSchemaVersion]
  protected def isOas2: Boolean  = version.isInstanceOf[OAS20SchemaVersion]
  protected def isOas3: Boolean  = version.isInstanceOf[OAS30SchemaVersion]
  protected def isOas31: Boolean = version.isInstanceOf[OAS31SchemaVersion]

  protected def moveExamplesToUnion(parsed: AnyShape, union: UnionShape): Unit = {
    val AmfArray(values, annotations) = parsed.fields.get(ExamplesField.Examples)
    if (values.nonEmpty) {
      union.setWithoutId(ExamplesField.Examples, AmfArray(values, annotations), annotations ++= inferred())
      parsed.fields.removeField(ExamplesField.Examples)
    }
  }

  private def checkOas30Nullable(parsed: AnyShape): AnyShape = {
    map.key("nullable") match {
      case Some(nullableEntry) if nullableEntry.value.toOption[Boolean].getOrElse(false) =>
        val union = UnionShape().withName(name, nameAnnotations).withId(parsed.id + "/nilUnion")
        parsed.annotations += NilUnion(nullableEntry.key.range.toString())
        union.withAnyOf(
          Seq(
            parsed,
            NilShape().withId(union.id + "_nil")
          ),
          synthesized()
        )
        moveExamplesToUnion(parsed, union)
        union
      case _ =>
        parsed
    }
  }

  /** JSON Schema allows to define multiple types for a shape. In this case we can parse this as a union because
    * properties are going to be disjoint for each of them
    */
  private def detectDisjointUnion(): Boolean = {
    map.key("type").isDefined && map.key("type").get.value.asOption[YSequence].isDefined
  }

  private def detect(version: SchemaVersion): TypeDef = {
    val defaultType = version match {
      case oasSchema: OASSchemaVersion if oasSchema.position.toString == "parameter" => UndefinedType
      case _                                                                         => AnyType
    }
    TypeDetector.detect(map, version)(new SyamlAMFErrorHandler(ctx.eh)).getOrElse(defaultType)
  }

  private def parseDisjointUnionType(): UnionShape = {

    // val detectedTypes = map.key("type").get.value.as[YSequence].nodes.map(_.as[String])
    val allEntriesExceptType = YMap(map.entries.filter(_.key.as[String] != "type"), map.sourceName)

    val parser = UnionShapeParser(YMapEntryLike(allEntriesExceptType), name)
    adopt(parser.shape) // We need to set the shape id before parsing to properly adopt nested nodes
    val union = parser.parse()

    val filterKeys = Seq(
      "example",
      "examples",
      "examples".asOasExtension,
      "title",
      "description",
      "default",
      "enum",
      "externalDocs",
      "xml",
      "facets".asOasExtension,
      "anyOf",
      "allOf",
      "oneOf",
      "not"
    )
    val filteredEntries = allEntriesExceptType.entries.filter { entry =>
      !filterKeys.contains(entry.key.as[String])
    }
    val propsToPropagate = YMap(filteredEntries, filteredEntries.headOption.map(_.sourceName).getOrElse(""))
    val typesSeq         = map.key("type").get.value.as[YSequence]
    var index            = 0
    val parsedTypes: Seq[AmfElement] = typesSeq.nodes map { node =>
      index += 1
      if (node.tagType == YType.Str) {
        node.as[String] match {
          case "object" =>
            Some(parseObjectType(name + index, propsToPropagate, s => s.withId(union.id + "/object")))
          case "array" =>
            Some(parseArrayType(name + index, propsToPropagate, s => s.withId(union.id + "/array")))
          case "number" =>
            Some(
              parseScalarType(TypeDef.NumberType, name + index, propsToPropagate, s => s.withId(union.id + "/number"))
            )
          case "integer" =>
            Some(parseScalarType(TypeDef.IntType, name + index, propsToPropagate, s => s.withId(union.id + "/integer")))
          case "string" =>
            Some(parseScalarType(TypeDef.StrType, name + index, propsToPropagate, s => s.withId(union.id + "/string")))
          case "boolean" =>
            Some(
              parseScalarType(TypeDef.BoolType, name + index, propsToPropagate, s => s.withId(union.id + "/boolean"))
            )
          case "null" =>
            Some(parseScalarType(TypeDef.NilType, name + index, propsToPropagate, s => s.withId(union.id + "/nil")))
          case "any" =>
            Some(parseAnyType(name + index, propsToPropagate, s => s.withId(union.id + "/any")))
          case other =>
            ctx.eh.violation(
              InvalidDisjointUnionType,
              union,
              s"Invalid type for disjointUnion $other",
              map.key("type").get.value.location
            )
            None
        }
      } else if (node.tagType == YType.Map) {
        val entry = YMapEntry(s"union_member_$index", node)
        OasTypeParser(entry, shape => Unit, version).parse()
      } else {
        ctx.eh.violation(
          InvalidDisjointUnionType,
          union,
          s"Invalid type for disjointUnion ${node.tagType}",
          map.key("type").get.value.location
        )
        None
      }
    } collect { case Some(t: AmfElement) => t }

    if (parsedTypes.nonEmpty) union.setArrayWithoutId(UnionShapeModel.AnyOf, parsedTypes, Annotations(typesSeq))

    union
  }

  private def parseScalarType(
      typeDef: TypeDef,
      name: String = name,
      map: YMap = map,
      adopt: Shape => Unit = adopt
  ): AnyShape = {
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

  private def parseObjectType(name: String = name, map: YMap = map, adopt: Shape => Unit = adopt): AnyShape = {
    if (map.key("schema".asOasExtension).isDefined) {
      val shape = SchemaShape(ast).withName(name, nameAnnotations)
      adopt(shape)
      SchemaShapeParser(shape, map)(ctx.eh).parse()
    } else {
      val shape = NodeShape(ast).withName(name, nameAnnotations)
      checkJsonIdentity(shape, map, adopt, ctx.futureDeclarations)
      NodeShapeParser(shape, map).parse()
    }
  }

  private def checkJsonIdentity(
      shape: AnyShape,
      map: YMap,
      adopt: Shape => Unit,
      futureDeclarations: FutureDeclarations
  ): Unit = {
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
      case _ if ctx.isOas3Context || ctx.isOas31Context => s"#/components/schemas/$name"
      case _ if ctx.isAsyncContext                      => s"#/components/schemas/$name"
      case _                                            => s"#/definitions/$name"
    }
  }

  private def parseUnionType(): UnionShape = UnionShapeParser(entryOrNode, name).parse()

  def parseSemanticContext(shape: AnyShape): Option[SemanticContext] =
    SemanticContextParser(entryOrNode.asMap, shape)(ctx).parse()

  trait CommonScalarParsingLogic {
    def parseScalar(map: YMap, shape: Shape, typeDef: TypeDef): TypeDef = {
      checkPatternAndFormatCombination(map, shape)

      typeDef match {
        case TypeDef.StrType | TypeDef.FileType =>
          map.key("pattern", ScalarShapeModel.Pattern in shape)
          map.key("minLength", ScalarShapeModel.MinLength in shape)
          map.key("maxLength", ScalarShapeModel.MaxLength in shape)
        case n if n.isNumber =>
          setValue("minimum", map, ScalarShapeModel.Minimum, shape)
          setValue("maximum", map, ScalarShapeModel.Maximum, shape)
          map.key("multipleOf", ScalarShapeModel.MultipleOf in shape)
          if (version isBiggerThanOrEqualTo JSONSchemaDraft6SchemaVersion) {
            parseNumericExclusive(map, shape)
          } else {
            parseBooleanExclusive(map, shape)
          }
        case _ => // Nothing to do
      }
      ScalarFormatType(shape, typeDef).parse(map)
    }

    private def parseNumericExclusive(map: YMap, shape: Shape): Unit = {
      map.key("exclusiveMinimum", ScalarShapeModel.ExclusiveMinimumNumeric in shape)
      map.key("exclusiveMaximum", ScalarShapeModel.ExclusiveMaximumNumeric in shape)
    }

    private def parseBooleanExclusive(map: YMap, shape: Shape): Unit = {
      map.key("exclusiveMinimum", ScalarShapeModel.ExclusiveMinimum in shape)
      map.key("exclusiveMaximum", ScalarShapeModel.ExclusiveMaximum in shape)
    }

    private def setValue(key: String, map: YMap, field: Field, shape: Shape): Unit =
      map.key(
        key,
        entry => {
          val value = amf.core.internal.parser.domain.ScalarNode(entry.value)
          shape.setWithoutId(field, value.text(), Annotations(entry))
        }
      )
  }

  private def checkPatternAndFormatCombination(map: YMap, shape: Shape): Unit = {
    val pattern = map.key("pattern")
    val format  = map.key("format")

    if (pattern.isDefined && format.isDefined) {
      val formatName = format.get.value.as[YScalar].text
      ctx.eh.warning(
        PossiblyIgnoredPatternWarning,
        shape,
        s"Pattern property may be ignored if format '$formatName' already defines a standard pattern",
        pattern.get.location
      )
    }
  }

  case class ScalarShapeParser(typeDef: TypeDef, shape: ScalarShape, map: YMap)
      extends AnyShapeParser()
      with CommonScalarParsingLogic {

    override lazy val dataNodeParser: YNode => DataNode =
      ScalarNodeParser().parse
    override lazy val enumParser: YNode => DataNode = CommonEnumParser(shape.id, enumType = EnumParsing.SCALAR_ENUM)

    override def parse(): ScalarShape = {
      super.parse()
      val validatedTypeDef = parseScalar(map, shape, typeDef)

      map
        .key("type")
        .fold(
          shape
            .setWithoutId(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(validatedTypeDef)), synthesized())
        )(entry =>
          shape.setWithoutId(
            ScalarShapeModel.DataType,
            AmfScalar(XsdTypeDefMapping.xsd(validatedTypeDef), Annotations(entry.value)),
            Annotations(entry)
          )
        )

      if (isStringScalar(shape) && version.isBiggerThanOrEqualTo(JSONSchemaDraft7SchemaVersion)) {
        ContentParser(s => Unit, version).parse(shape, map)
      }

      shape
    }

    private def isStringScalar(shape: ScalarShape) = shape.dataType.option().fold(false) { value =>
      value == DataType.String
    }
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
        "x-amf-union",
        { entry =>
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val unionNodes = seq.zipWithIndex
                .map { case (unionNode, index) =>
                  val name  = s"item$index"
                  val entry = YMapEntryLike(name, unionNode)
                  parser
                    .OasTypeParser(entry, name, item => Unit, version)
                    .parse()
                }
                .filter(_.isDefined)
                .map(_.get)
              shape.setArray(UnionShapeModel.AnyOf, unionNodes, Annotations(entry.value))
            case _ =>
              ctx.eh
                .violation(InvalidUnionType, shape, "Unions are built from multiple shape nodes", entry.value.location)

          }
        }
      )

      shape
    }
  }

  case class OrConstraintParser(map: YMap, shape: Shape) {

    def parse(): Unit = {
      map.key(
        "anyOf",
        { entry =>
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val unionNodes = seq.zipWithIndex
                .flatMap { case (node, index) =>
                  val entry = YMapEntryLike(node)
                  OasTypeParser(entry, s"item$index", _ => Unit, version).parse()
                }
              shape.fields
                .setWithoutId(ShapeModel.Or, AmfArray(unionNodes, Annotations(entry.value)), Annotations(entry))
            case _ =>
              ctx.eh.violation(
                InvalidOrType,
                shape,
                "Or constraints are built from multiple shape nodes",
                entry.value.location
              )

          }
        }
      )
    }
  }

  case class XoneConstraintParser(map: YMap, shape: Shape) {

    def parse(): Unit = {
      map.key(
        "oneOf",
        { entry =>
          adopt(shape)
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val nodes = seq.zipWithIndex
                .flatMap { case (node, index) =>
                  val entry = YMapEntryLike(node)
                  OasTypeParser(entry, s"item$index", _ => Unit, version).parse()
                }
              shape.fields.setWithoutId(ShapeModel.Xone, AmfArray(nodes, Annotations(entry.value)), Annotations(entry))
            case _ =>
              ctx.eh.violation(
                InvalidXoneType,
                shape,
                "Xone constraints are built from multiple shape nodes",
                entry.value.location
              )

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
        case Some(Left(tuple)) =>
          val shape = TupleShapeParser(tuple, map, adopt).parse()
          invalidItemsFieldValidation(shape)
          shape
        case Some(Right(array)) =>
          ArrayShapeParser(array, map, adopt).parse()
      }
    }

    private def invalidItemsFieldValidation(shape: AnyShape): Unit = {
      if (isOas31) {
        ctx.eh.violation(InvalidOasItemsField, shape, Some(shape.id), "OAS Schemas 'items' field must be an object")
      } else if (isOas3 || isOas2)
        ctx.eh.warning(InvalidOasItemsField, shape, Some(shape.id), "OAS Schemas 'items' field must be an object")
    }

    private def validateMissingItemsField(shape: Shape): Unit = {
      if (isOas3)
        ctx.eh.violation(ItemsFieldRequired, shape, "'items' field is required when schema type is array", map.location)
    }
  }

  trait DataArrangementShapeParser extends AnyShapeParser {

    override def parse(): AnyShape = {
      super.parse()

      map.key("minItems", ArrayShapeModel.MinItems in shape)
      map.key("maxItems", ArrayShapeModel.MaxItems in shape)
      map.key("uniqueItems", ArrayShapeModel.UniqueItems in shape)

      if (version isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion)
        InnerShapeParser("contains", ArrayShapeModel.Contains, map, shape, adopt, version).parse()
      shape
    }

  }

  case class TupleShapeParser(shape: TupleShape, map: YMap, adopt: Shape => Unit) extends DataArrangementShapeParser() {

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
            parser.OasTypeParser(entry, s => Unit, version).parse().foreach { s =>
              shape.setWithoutId(TupleShapeModel.AdditionalItemsSchema, s, Annotations(entry))
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
            .map { case (elem, index) =>
              parser
                .OasTypeParser(YMapEntryLike(elem), s"member$index", item => Unit, version)
                .parse()
            }
          shape.withItems(items.filter(_.isDefined).map(_.get))
        }
      )

      shape
    }

    private def additionalItemViolation(entry: YMapEntry, msg: String): Unit = {
      ctx.eh.violation(InvalidAdditionalItemsType, shape, msg, entry.location)
    }
  }

  case class ArrayShapeParser(shape: ArrayShape, map: YMap, adopt: Shape => Unit) extends DataArrangementShapeParser() {
    override def parse(): AnyShape = {
      checkJsonIdentity(shape, map, adopt, ctx.futureDeclarations)
      super.parse()

      map.key("collectionFormat", ArrayShapeModel.CollectionFormat in shape)

      map
        .key("items")
        .flatMap(_.value.toOption[YMap])
        .foreach(
          _.key(
            "collectionFormat",
            (ArrayShapeModel.CollectionFormat in shape)
              .withAnnotation(CollectionFormatFromItems())
          )
        )

      val finalShape = for {
        entry <- map.key("items")
        item  <- parser.OasTypeParser(entry, items => Unit, version).parse()
      } yield {
        item match {
          case array: ArrayShape =>
            shape.setWithoutId(ArrayShapeModel.Items, array, Annotations(entry)).toMatrixShape
          case matrix: MatrixShape =>
            shape.setWithoutId(ArrayShapeModel.Items, matrix, Annotations(entry)).toMatrixShape
          case other: AnyShape =>
            shape.setWithoutId(ArrayShapeModel.Items, other, Annotations(entry))
        }
      }

      if (version.isBiggerThanOrEqualTo(JSONSchemaDraft201909SchemaVersion)) {
        new UnevaluatedParser(version, UnevaluatedParser.unevaluatedItemsInfo).parse(map, shape)
        map.key("minContains", ArrayShapeModel.MinContains in shape)
        map.key("maxContains", ArrayShapeModel.MaxContains in shape)
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

      val semanticContext = parseSemanticContext(shape)
      semanticContext.foreach(shape.withSemanticContext)
      if (ctx.getSemanticContext.isEmpty) ctx.withSemanticContext(semanticContext)

      super.parse()
      parseExample()

      map.key("type", _ => shape.add(ExplicitField())) // todo lexical of type?? new annotation?

      if (version isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion)
        map.key("$comment", AnyShapeModel.Comment in shape)

      if (version.isInstanceOf[OAS31SchemaVersion]) {
        map.key("$schema") match {
          case Some(entry) => Some(entry).foreach(AnyShapeModel.SchemaVersion in shape)
          case None =>
            shape.setWithoutId(
              AnyShapeModel.SchemaVersion,
              AmfScalar("https://spec.openapis.org/oas/3.1/dialect/base"),
              Annotations.inferred()
            )
        }
      }

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
          shape.fields.setWithoutId(
            AnyShapeModel.Examples,
            AmfArray(examples, Annotations(entry.value)),
            Annotations(entry)
          )
        }
  }

  case class NodeShapeParser(shape: NodeShape, map: YMap)(implicit val ctx: ShapeParserContext)
      extends AnyShapeParser() {
    override def parse(): NodeShape = {

      super.parse()

      map.key("type", _ => shape.add(ExplicitField()))

      map.key("minProperties", NodeShapeModel.MinProperties in shape)
      map.key("maxProperties", NodeShapeModel.MaxProperties in shape)

      shape.setWithoutId(NodeShapeModel.Closed, AmfScalar(value = false), synthesized())

      map.key("additionalProperties").foreach { entry =>
        entry.value.tagType match {
          case YType.Bool => (NodeShapeModel.Closed in shape).negated.explicit(entry)
          case YType.Map =>
            parser.OasTypeParser(entry, s => Unit, version).parse().foreach { s =>
              shape.setWithoutId(NodeShapeModel.AdditionalPropertiesSchema, s, synthesized())
            }
          case _ =>
            ctx.eh.violation(
              InvalidAdditionalPropertiesType,
              shape,
              "Invalid part type for additional properties node. Should be a boolean or a map",
              entry.location
            )
        }
      }

      if (version.isBiggerThanOrEqualTo(JSONSchemaDraft201909SchemaVersion)) {
        new UnevaluatedParser(version, UnevaluatedParser.unevaluatedPropertiesInfo).parse(map, shape)
      }

      if (isOas3 || isOas31) {
        map.key("discriminator", DiscriminatorParser(shape, _).parse())
      } else {
        map.key("discriminator", NodeShapeModel.Discriminator in shape)
        map.key(
          "discriminatorValue".asOasExtension,
          entry => {
            val setDiscriminatorValueField = NodeShapeModel.DiscriminatorValue in shape
            setDiscriminatorValueField(entry)

            val valueDataNode = DataNodeParser(entry.value).parse()
            shape.set(NodeShapeModel.DiscriminatorValueDataNode, valueDataNode, Annotations(entry.key))
          }
        )
      }

      val requiredFields = parseRequiredFields(map, shape)

      val properties      = mutable.LinkedHashMap[String, PropertyShape]()
      val propertiesEntry = map.key("properties")
      propertiesEntry.foreach(entry => {
        Option(entry.value.as[YMap]) match {
          case Some(m) =>
            val props = PropertiesParser(m, shape.withProperty, requiredFields).parse()
            properties ++= props.map(p => p.name.value() -> p)
          case _ => // Empty properties node.
        }
      })
      generateUndefinedRequiredProperties(requiredFields, properties)
      if (version isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion)
        InnerShapeParser("propertyNames", NodeShapeModel.PropertyNames, map, shape, adopt, version).parse()

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

      val (propertiesAnnotations, propertiesFieldAnnotations) = propertiesEntry.map { pe =>
        (Annotations(pe.value), Annotations(pe))
      } orElse {
        patternPropEntry.map { pp =>
          (Annotations(pp.value), Annotations(pp))
        }
      } getOrElse { (virtual(), inferred()) }

      if (properties.nonEmpty || propertiesEntry.nonEmpty)
        shape.setWithoutId(
          NodeShapeModel.Properties,
          AmfArray(properties.values.toSeq, propertiesAnnotations),
          propertiesFieldAnnotations
        )

      parseShapeDependencies(shape)

      map.key(
        "x-amf-merge",
        entry => {
          val inherits = AllOfParser(entry.value.as[Seq[YNode]], s => Unit, version).parse()
          shape.setWithoutId(NodeShapeModel.Inherits, AmfArray(inherits, Annotations(entry.value)), Annotations(entry))
        }
      )

      shape
    }
  }

  private def generateUndefinedRequiredProperties(
      requiredFields: Map[String, YNode],
      properties: mutable.LinkedHashMap[String, PropertyShape]
  ): Unit = {
    val undefinedRequiredProperties = requiredFields.keySet.filter(!properties.keySet.contains(_))
    val generatedRequiredProperties = undefinedRequiredProperties
      .map(propertyName => {
        PropertyShape(virtual() += InferredProperty())
          .withName(propertyName)
          .set(PropertyShapeModel.MinCount, AmfScalar(1), synthesized())
          .set(PropertyShapeModel.Range, AnyShape(), synthesized())
          .set(PropertyShapeModel.Path, AmfScalar((Namespace.Data + propertyName).iri()), synthesized())
      })
    properties ++= generatedRequiredProperties.map(p => p.name.value() -> p)
  }

  private def parseShapeDependencies(shape: NodeShape): Unit = {
    if (version == JSONSchemaDraft201909SchemaVersion) {
      Draft2019ShapeDependenciesParser(shape, map, shape.id, version).parse()
    } else {
      map.key(
        "dependencies",
        entry => {
          Draft4ShapeDependenciesParser(shape, entry.value.as[YMap], shape.id, version).parse()
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
          val requiredGroup = required.legacyGroupBy(_.as[String])
          validateRequiredFields(requiredGroup, shape)
          requiredGroup.map { case (key, nodes) =>
            key -> nodes.head
          }
        case None =>
          ctx.eh.violation(InvalidRequiredValue, shape, "'required' field has to be an array", loc = field.location)
          defaultValue
      }
    }

    val defaultValue = Map[String, YNode]()
    map
      .key("required")
      .map { field =>
        (field.value.tagType, version) match {
          case (YType.Seq, JSONSchemaDraft3SchemaVersion) =>
            ctx.eh.violation(
              InvalidRequiredArrayForSchemaVersion,
              shape,
              "Required arrays of properties not supported in JSON Schema below version draft-4",
              field.value.location
            )
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
          ctx.eh.violation(
            DuplicateRequiredItem,
            shape,
            s"'$name' is duplicated in 'required' property",
            nodes.last.location
          )
        case _ => // ignore
      }

  case class DiscriminatorParser(shape: NodeShape, entry: YMapEntry) {
    def parse(): Unit = {
      val map = entry.value.as[YMap]
      map.key("propertyName") match {
        case Some(entry) =>
          (NodeShapeModel.Discriminator in shape)(entry)
        case None =>
          ctx.eh.violation(
            DiscriminatorNameRequired,
            shape,
            s"Discriminator must have a propertyName defined",
            map.location
          )
      }
      map.key("mapping", parseMappings)
      if (isOas31) {
        val extensions = AnnotationParser.parseExtensions(None, map)
        extensions.foreach { ex =>
          ex.extension.annotations += DiscriminatorExtension()
          shape.add(CustomDomainProperties, ex)
        }
      }
      ctx.closedShape(shape, map, "discriminator")
    }

    private def parseMappings(mappingsEntry: YMapEntry): Unit = {
      val map = mappingsEntry.value.as[YMap]
      val mappings = map.entries.map(entry => {
        val mapping  = IriTemplateMapping(Annotations(entry))
        val element  = amf.core.internal.parser.domain.ScalarNode(entry.key).string()
        val variable = amf.core.internal.parser.domain.ScalarNode(entry.value).string()
        mapping.setWithoutId(TemplateVariable, element, Annotations(entry.key))
        mapping.setWithoutId(LinkExpression, variable, Annotations(entry.value))
      })
      shape.fields.setWithoutId(
        NodeShapeModel.DiscriminatorMapping,
        AmfArray(mappings, Annotations(mappingsEntry.value)),
        Annotations(mappingsEntry)
      )

      val discriminatorValueMapping = map.entries.map { entry =>
        val key: YNode         = entry.key
        val discriminatorValue = amf.core.internal.parser.domain.ScalarNode(key).string()
        val targetShape = {
          val rawRef: String = entry.value
          val definitionName = OasShapeDefinitions.stripDefinitionsPrefix(rawRef)
          ctx.findType(definitionName, SearchScope.All) match {
            case Some(s) =>
              s.link(AmfScalar(key.toString), Annotations(ast), synthesized())
                .asInstanceOf[AnyShape]
                .withName(name, nameAnnotations)
                .withSupportsRecursion(true)
            case _ =>
              val resultShape = AnyShape(ast).withName(key, Annotations(key))
              val tmpShape = UnresolvedShape(
                Fields(),
                Annotations(entry.value),
                entry.value,
                None,
                Some((k: String) => resultShape.set(LinkableElementModel.TargetId, k)),
                shouldLink = false
              )
                .withName(key, Annotations())
                .withSupportsRecursion(true)
              tmpShape.unresolved(definitionName, Nil, Some(entry.value.location), "warning")(ctx)
              tmpShape.withContext(ctx)
              val encodedKey = key.toString.urlComponentEncoded
              tmpShape.withId(s"${shape.id}/discriminator/$encodedKey/unresolved")
              resultShape.withId(s"${shape.id}/discriminator/$encodedKey")
              resultShape.withLinkTarget(tmpShape).withLinkLabel(key)
          }
        }

        val discriminatorMapping = DiscriminatorValueMapping(Annotations(entry))
        discriminatorMapping.setWithoutId(DiscriminatorValue, discriminatorValue, Annotations(key))
        discriminatorMapping.setWithoutId(DiscriminatorValueTarget, targetShape, Annotations(entry.value))
      }

      val fieldValue = AmfArray(discriminatorValueMapping, Annotations(mappingsEntry.value))
      shape.setWithoutId(NodeShapeModel.DiscriminatorValueMapping, fieldValue, Annotations(mappingsEntry))
    }
  }

  case class PropertiesParser(
      map: YMap,
      producer: (String, Annotations) => PropertyShape,
      requiredFields: Map[String, YNode],
      patterned: Boolean = false
  ) {
    def parse(): Seq[PropertyShape] = {
      map.entries.map(entry => PropertyShapeParser(entry, producer, requiredFields, patterned).parse())
    }
  }

  case class PropertyShapeParser(
      entry: YMapEntry,
      producer: (String, Annotations) => PropertyShape,
      requiredFields: Map[String, YNode],
      patterned: Boolean
  ) {

    def parse(): PropertyShape = {

      val name            = entry.key.as[YScalar].text
      val nameAnnotations = Annotations(entry.key)
      val required        = requiredFields.contains(name)
      val requiredAnnotations =
        requiredFields.get(name).map(node => Annotations(node)).getOrElse(synthesized())
      val property = producer(name, nameAnnotations)
        .add(Annotations(entry))
        .setWithoutId(
          PropertyShapeModel.MinCount,
          AmfScalar(if (required) 1 else 0, synthesized()),
          requiredAnnotations += ExplicitField()
        )

      property.setWithoutId(
        PropertyShapeModel.Path,
        AmfScalar((Namespace.Data + entry.key.as[YScalar].text.urlComponentEncoded).iri(), Annotations(entry.key)),
        inferred()
      )

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
                  ctx.eh.warning(
                    InvalidRequiredBooleanForSchemaVersion,
                    property,
                    InvalidRequiredBooleanForSchemaVersion.message,
                    entry.location
                  )
                }
                val required =
                  amf.core.internal.parser.domain.ScalarNode(entry.value).boolean().value.asInstanceOf[Boolean]
                property.setWithoutId(
                  PropertyShapeModel.MinCount,
                  AmfScalar(if (required) 1 else 0),
                  synthesized()
                )
              }
            }
          )
        )

      var shape = AnyShape()
      parser.OasTypeParser(entry, shape => Unit, version).parse().foreach(shape = _)
      property.setWithoutId(PropertyShapeModel.Range, shape, Annotations.inferred())

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
              ctx.eh.warning(
                ReadOnlyPropertyMarkedRequired,
                property,
                "Read only property should not be marked as required by a schema",
                readOnlyEntry.location
              )
            }
          }
        )
      )
    }
  }

  abstract class ShapeParser(implicit ctx: ShapeParserContext) {

    val shape: Shape
    val map: YMap

    lazy val dataNodeParser: YNode => DataNode = DataNodeParser.parse(new IdCounter())
    lazy val enumParser: YNode => DataNode     = CommonEnumParser(shape.id)

    def parse(): Shape = {

      map.key("title", ShapeModel.DisplayName in shape)
      map.key("description", ShapeModel.Description in shape)

      map.key(
        "default",
        node => {
          shape.setDefaultStrValue(node)
          NodeDataNodeParser(node.value, shape.id, quiet = false).parse().dataNode.foreach { dn =>
            shape.setWithoutId(ShapeModel.Default, dn, Annotations(node))
          }

        }
      )

      map.key("enum", ShapeModel.Values in shape using enumParser)
      map.key("externalDocs", AnyShapeModel.Documentation in shape using (OasLikeCreativeWorkParser.parse(_, shape.id)))
      map.key("xml", AnyShapeModel.XMLSerialization in shape using XMLSerializerParser.parse(shape.name.value()))

      map.key(
        "facets".asOasExtension,
        entry => PropertiesParser(entry.value.as[YMap], shape.withCustomShapePropertyDefinition, Map()).parse()
      )

      // Explicit annotation for the type property
      map.key("type", entry => shape.annotations += TypePropertyLexicalInfo(entry.key.range))

      // Logical constraints
      OrConstraintParser(map, shape).parse()
      AndConstraintParser(map, shape, adopt, version).parse()
      XoneConstraintParser(map, shape).parse()
      InnerShapeParser("not", ShapeModel.Not, map, shape, adopt, version).parse()

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
      InnerShapeParser("if", ShapeModel.If, map, shape, adopt, version).parse()
      InnerShapeParser("then", ShapeModel.Then, map, shape, adopt, version).parse()
      InnerShapeParser("else", ShapeModel.Else, map, shape, adopt, version).parse()
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

  case class SchemaShapeParser(shape: SchemaShape, map: YMap)(implicit errorHandler: AMFErrorHandler)
      extends AnyShapeParser()
      with CommonScalarParsingLogic {
    super.parse()

    override def parse(): AnyShape = {
      map.key(
        "schema".asOasExtension,
        { entry =>
          entry.value.to[String] match {
            case Right(str) => shape.withRaw(str)
            case _ =>
              errorHandler.violation(
                InvalidSchemaType,
                shape,
                "Cannot parse non string schema shape",
                entry.value.location
              )
              shape.withRaw("")
          }
        }
      )

      map.key(
        "mediaType".asOasExtension,
        { entry =>
          entry.value.to[String] match {
            case Right(str) => shape.withMediaType(str)
            case _ =>
              errorHandler.violation(
                InvalidMediaTypeType,
                shape,
                "Cannot parse non string schema shape",
                entry.value.location
              )
              shape.withMediaType("*/*")
          }
        }
      )

      shape
    }
  }
}
