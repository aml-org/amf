package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.ExplicitField
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{AmfArray, AmfScalar, Shape}
import amf.core.parser.{Annotations, _}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.annotations.Inferred
import amf.plugins.document.webapi.contexts.{OasWebApiContext, WebApiContext}
import amf.plugins.document.webapi.parser.OasTypeDefMatcher.matchType
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.domain.RamlExamplesParser
import amf.plugins.document.webapi.parser.spec.oas.OasSpecParser
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models.TypeDef._
import amf.plugins.domain.shapes.models.{CreativeWork, Example, _}
import amf.plugins.domain.shapes.parser.XsdTypeDefMapping
import org.yaml.model._
import org.yaml.render.YamlRender

import scala.collection.mutable

/**
  * OpenAPI Type Parser.
  */
object OasTypeParser {
  def apply(entry: YMapEntry, adopt: Shape => Unit, oasNode: String = "schema")(
      implicit ctx: WebApiContext): OasTypeParser =
    OasTypeParser(entry, entry.key.as[YScalar].text, entry.value.as[YMap], adopt, oasNode)(
      new OasWebApiContext(ctx, Some(ctx.declarations)))
}

case class OasTypeParser(ast: YPart, name: String, map: YMap, adopt: Shape => Unit, oasNode: String)(
    implicit val ctx: WebApiContext)
    extends OasSpecParser {

  def parse(): Option[Shape] = {

    val parsedShape = detect(oasNode) match {
      case UnionType                   => Some(parseUnionType())
      case LinkType                    => parseLinkType()
      case ObjectType                  => Some(parseObjectType())
      case ArrayType                   => Some(parseArrayType())
      case AnyType                     => Some(parseAnyType())
      case typeDef if typeDef.isScalar => Some(parseScalarType(typeDef))
      case _                           => None
    }

    parsedShape match {
      case Some(shape: Shape) =>
        ctx.closedShape(shape.id, map, oasNode)
        Some(shape)
      case None => None
    }
  }

  private def detect(position: String): TypeDef = {
    val defaultType =
      if (position == "parameter") UndefinedType
      else AnyType

    detectDependency()
      .orElse(detectType())
      .orElse(detectProperties())
      .orElse(detectAnyOf())
      .getOrElse(defaultType)
  }

  private def detectProperties(): Option[TypeDef.ObjectType.type] =
    map.key("properties").orElse(map.key("allOf")).map(_ => ObjectType)

  private def detectDependency(): Option[TypeDef] = map.key("$ref").map(_ => LinkType)

  private def detectAnyOf(): Option[TypeDef.UnionType.type] = {
    map.key("anyOf").map(_ => UnionType)
  }

  private def detectType(): Option[TypeDef] = {
    map
      .key("type")
      .map(e => {
        val t = e.value.as[YScalar].text
        val f = map.key("format").flatMap(e => e.value.toOption[YScalar].map(_.text)).getOrElse("")
        matchType(t, f)
      })
  }

  private def parseScalarType(typeDef: TypeDef): Shape = {
    val parsed = typeDef match {
      case NilType => NilShape(ast).withName(name)
      case FileType =>
        val shape = FileShape(ast).withName(name)
        FileShapeParser(typeDef, shape, map).parse()
      case _ =>
        val shape = ScalarShape(ast).withName(name)
        ScalarShapeParser(typeDef, shape, map).parse()
    }
    adopt(parsed)
    parsed
  }

  private def parseAnyType(): Shape = {
    val shape = AnyShape(ast).withName(name)
    adopt(shape)
    AnyShapeParser(shape, map).parse()
  }

  private def parseArrayType(): Shape = {
    DataArrangementParser(name, ast, map, (shape: Shape) => adopt(shape)).parse()
  }

  private def parseLinkType(): Option[Shape] = {
    map
      .key("$ref")
      .map(e => OasDefinitions.stripDefinitionsPrefix(e.value))
      .map(text =>
        ctx.declarations.findType(text, SearchScope.All) match {
          case Some(s) =>
            val copied = s.link(text, Annotations(ast)).asInstanceOf[Shape].withName(name)
            adopt(copied)
            copied
          case None =>
            val shape = UnresolvedShape(text, map).withName(text)
            shape.withContext(ctx)
            shape.unresolved(text, map)
            adopt(shape)
            shape
      })
  }

  private def parseObjectType(): Shape = {
    if (map.key("x-schema").isDefined) {
      val shape = SchemaShape(ast).withName(name)
      adopt(shape)
      SchemaShapeParser(shape, map).parse()
    } else {
      val shape = NodeShape(ast).withName(name)
      adopt(shape)
      NodeShapeParser(shape, map).parse()
    }
  }

  private def parseUnionType(): Shape = {
    UnionShapeParser(map, name).parse()
  }

  trait CommonScalarParsingLogic {
    def parseScalar(map: YMap, shape: Shape): Unit = {
      map.key("pattern", ScalarShapeModel.Pattern in shape)
      map.key("minLength", ScalarShapeModel.MinLength in shape)
      map.key("maxLength", ScalarShapeModel.MaxLength in shape)

      map.key("minimum", entry => { // todo pope
        val value = ValueNode(entry.value)
        shape.set(ScalarShapeModel.Minimum, value.integer(), Annotations(entry))
      })

      map.key("maximum", entry => { // todo pope
        val value = ValueNode(entry.value)
        shape.set(ScalarShapeModel.Maximum, value.integer(), Annotations(entry))
      })

      map.key("exclusiveMinimum", ScalarShapeModel.ExclusiveMinimum in shape)
      map.key("exclusiveMaximum", ScalarShapeModel.ExclusiveMaximum in shape)
      map.key("format", ScalarShapeModel.Format in shape)
      map.key("multipleOf", ScalarShapeModel.MultipleOf in shape)

//      shape.set(ScalarShapeModel.Repeat, value = false)

    }
  }
  case class ScalarShapeParser(typeDef: TypeDef, shape: ScalarShape, map: YMap)
      extends ShapeParser()
      with CommonScalarParsingLogic {
    override def parse(): ScalarShape = {
      super.parse()
      map
        .key("type")
        .fold(shape
          .set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), Annotations() += Inferred()))(
          entry => shape.set(ScalarShapeModel.DataType, AmfScalar(XsdTypeDefMapping.xsd(typeDef)), Annotations(entry)))

      parseScalar(map, shape)

      shape
    }
  }

  case class UnionShapeParser(override val map: YMap, name: String) extends ShapeParser() {

    override val shape: UnionShape = UnionShape(Annotations(map)).withName(name)

    override def parse(): UnionShape = {
      super.parse()

      map.key(
        "anyOf", { entry =>
          entry.value.to[Seq[YNode]] match {
            case Right(seq) =>
              val unionNodes = seq.zipWithIndex
                .map {
                  case (node, index) =>
                    val entry = YMapEntry(YNode(s"item$index"), node)
                    OasTypeParser(entry, item => item.adopted(shape.id + "/items/" + index)).parse()
                }
                .filter(_.isDefined)
                .map(_.get)
              shape.setArray(UnionShapeModel.AnyOf, unionNodes, Annotations(entry.value))
            case _ =>
              ctx.violation(shape.id, "Unions are built from multiple shape nodes", entry.value)

          }
        }
      )

      shape
    }
  }

  case class DataArrangementParser(name: String, ast: YPart, map: YMap, adopt: Shape => Unit) {

    def lookAhead(): Option[Either[TupleShape, ArrayShape]] = {
      map.key("items") match {
        case Some(entry) =>
          entry.value.to[Seq[YNode]] match {
            // this is a sequence, we need to create a tuple
            case Right(_) => Some(Left(TupleShape(ast).withName(name)))
            // not an array regular array parsing
            case _ => Some(Right(ArrayShape(ast).withName(name)))

          }
        case None => None
      }
    }

    def parse(): Shape = {
      lookAhead() match {
        case None =>
          val arrayShape = ArrayShape()
          adopt(arrayShape)
          ctx.violation(arrayShape.id, "Cannot parse data arrangement shape", ast)
          arrayShape
        case Some(Left(tuple))  => TupleShapeParser(tuple, map, adopt).parse()
        case Some(Right(array)) => ArrayShapeParser(array, map, adopt).parse()
      }
    }

  }

  case class TupleShapeParser(shape: TupleShape, map: YMap, adopt: Shape => Unit) extends ShapeParser() {

    override def parse(): Shape = {
      adopt(shape)

      super.parse()

      map.key("minItems", ArrayShapeModel.MinItems in shape)
      map.key("maxItems", ArrayShapeModel.MaxItems in shape)
      map.key("uniqueItems", ArrayShapeModel.UniqueItems in shape)

      map.key(
        "items",
        entry => {
          val items = entry.value
            .as[YMap]
            .entries
            .zipWithIndex
            .map {
              case (elem, index) =>
                OasTypeParser(elem, item => item.adopted(item.id + "/items/" + index))
                  .parse()
            }
          shape.withItems(items.filter(_.isDefined).map(_.get))
        }
      )

      shape
    }
  }

  case class ArrayShapeParser(shape: ArrayShape, map: YMap, adopt: Shape => Unit) extends ShapeParser() {
    override def parse(): Shape = {
      adopt(shape)

      super.parse()

      map.key("minItems", ArrayShapeModel.MinItems in shape)
      map.key("maxItems", ArrayShapeModel.MaxItems in shape)
      map.key("uniqueItems", ArrayShapeModel.UniqueItems in shape)

      val finalShape = for {
        entry <- map.key("items")
        item <- OasTypeParser(entry, items => items.adopted(shape.id + "/items"))
          .parse()
      } yield {
        item match {
          case array: ArrayShape   => shape.withItems(array).toMatrixShape
          case matrix: MatrixShape => shape.withItems(matrix).toMatrixShape
          case other: Shape        => shape.withItems(other)
        }
      }

      finalShape match {
        case Some(parsed: Shape) => parsed
        case None =>
          val arrayShape = ArrayShape()
          adopt(arrayShape)
          ctx.violation(arrayShape.id, "Cannot parse data arrangement shape", map)
          arrayShape
      }
    }
  }

  case class AnyShapeParser(shape: AnyShape, map: YMap) extends ShapeParser() {
    override def parse(): AnyShape = {
      super.parse()

      map.key("type", _ => shape.add(ExplicitField())) // todo lexical of type?? new annotation?

      shape
    }
  }

  case class NodeShapeParser(shape: NodeShape, map: YMap) extends ShapeParser() {
    override def parse(): NodeShape = {

      super.parse()

      map.key("type", _ => shape.add(ExplicitField())) // todo lexical of type?? new annotation?

      map.key("minProperties", NodeShapeModel.MinProperties in shape)
      map.key("maxProperties", NodeShapeModel.MaxProperties in shape)

      shape.set(NodeShapeModel.Closed, value = false)
      map.key("additionalProperties", (NodeShapeModel.Closed in shape).negated.explicit)

      map.key("discriminator", NodeShapeModel.Discriminator in shape)
      map.key("x-discriminator-value", NodeShapeModel.DiscriminatorValue in shape)
      map.key("readOnly", NodeShapeModel.ReadOnly in shape)

      val requiredFields = map
        .key("required")
        .flatMap(_.value.toOption[Seq[String]])
        .getOrElse(Nil)

      map.key(
        "properties",
        entry => {
          entry.value.toOption[YMap] match {
            case Some(m) =>
              val properties: Seq[PropertyShape] =
                PropertiesParser(m, shape.withProperty, requiredFields).parse()
              shape.set(NodeShapeModel.Properties, AmfArray(properties, Annotations(entry.value)), Annotations(entry))
            case _ => // Empty properties node.
          }
        }
      )

      val properties = mutable.ListMap[String, PropertyShape]()
      shape.properties.foreach(p => properties += (p.name -> p))

      map.key(
        "dependencies",
        entry => {
          val dependencies: Seq[PropertyDependencies] =
            ShapeDependenciesParser(entry.value.as[YMap], properties).parse()
          shape.set(NodeShapeModel.Dependencies, AmfArray(dependencies, Annotations(entry.value)), Annotations(entry))
        }
      )

      map.key(
        "allOf",
        entry => {
          val inherits = AllOfParser(entry.value.as[Seq[YNode]], s => s.adopted(shape.id)).parse()
          shape.set(NodeShapeModel.Inherits, AmfArray(inherits, Annotations(entry.value)), Annotations(entry))
        }
      )

      shape
    }
  }

  case class AllOfParser(array: Seq[YNode], adopt: Shape => Unit) {
    def parse(): Seq[Shape] =
      array
        .flatMap(n => n.toOption[YMap])
        .flatMap(map =>
          declarationsRef(map)
            .orElse(OasTypeParser(map, "", map, adopt, "schema").parse()))

    private def declarationsRef(entries: YMap): Option[Shape] = {
      entries
        .key("$ref")
        .map(entry => ctx.declarations.shapes(entry.value.as[String].stripPrefix("#/definitions/")))
    }
  }

  case class PropertiesParser(map: YMap, producer: String => PropertyShape, requiredFields: Seq[String]) {
    def parse(): Seq[PropertyShape] = {
      map.entries.map(entry => PropertyShapeParser(entry, producer, requiredFields).parse())
    }
  }

  case class PropertyShapeParser(entry: YMapEntry, producer: String => PropertyShape, requiredFields: Seq[String]) {

    def parse(): PropertyShape = {

      val name     = entry.key.as[YScalar].text
      val required = requiredFields.contains(name)

      val property = producer(name)
        .add(Annotations(entry))
        .set(PropertyShapeModel.MinCount, AmfScalar(if (required) 1 else 0), Annotations() += ExplicitField())

      property.set(PropertyShapeModel.Path, (Namespace.Data + entry.key.as[YScalar].text).iri())

      OasTypeParser(entry, shape => shape.adopted(property.id))
        .parse()
        .foreach(property.set(PropertyShapeModel.Range, _))

      property
    }
  }

  case class Property(var typeDef: TypeDef = UndefinedType) {
    def withTypeDef(value: TypeDef): Unit = typeDef = value
  }

  abstract class ShapeParser(implicit ctx: WebApiContext) {

    val shape: Shape
    val map: YMap

    def parse(): Shape = {

      map.key("title", ShapeModel.DisplayName in shape)
      map.key("description", ShapeModel.Description in shape)

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
              shape.set(ShapeModel.Default, value.string(), Annotations(entry))
          }
        }
      )

      map.key("enum", entry => {
        val value = ArrayNode(entry.value)
        shape.set(ShapeModel.Values, value.strings(), Annotations(entry))
      })

      map.key(
        "externalDocs",
        entry => {
          val creativeWork: CreativeWork = OasCreativeWorkParser(entry.value.as[YMap]).parse()
          shape.set(AnyShapeModel.Documentation, creativeWork, Annotations(entry))
        }
      )

      map.key(
        "xml",
        entry => {
          val xmlSerializer: XMLSerializer =
            XMLSerializerParser(shape.name, entry.value.as[YMap]).parse()
          shape.set(AnyShapeModel.XMLSerialization, xmlSerializer, Annotations(entry))
        }
      )

      map.key(
        "x-facets",
        entry => {
          val properties: Seq[PropertyShape] =
            PropertiesParser(entry.value.as[YMap], shape.withCustomShapePropertyDefinition, Seq()).parse()
        }
      )

      val examples: Seq[Example] = RamlExamplesParser(map, "example", "x-examples").parse()
      if (examples.nonEmpty)
        shape.setArray(AnyShapeModel.Examples, examples)

      // normal annotations
      AnnotationParser(shape, map).parse()

      shape
    }
  }

  case class FileShapeParser(typeDef: TypeDef, shape: FileShape, map: YMap)
      extends ShapeParser()
      with CommonScalarParsingLogic {
    override def parse(): Shape = {
      super.parse()

      parseScalar(map, shape)

      map.key(
        "x-fileTypes", { entry =>
          entry.value.to[Seq[YNode]] match {
            case Right(_) =>
              val value = ArrayNode(entry.value)
              shape.set(FileShapeModel.FileTypes, value.strings(), Annotations(entry.value))
            case _ =>
          }
        }
      )

      shape
    }
  }

  case class SchemaShapeParser(shape: SchemaShape, map: YMap) extends ShapeParser() with CommonScalarParsingLogic {
    super.parse()

    override def parse(): Shape = {
      map.key(
        "x-schema", { entry =>
          entry.value.to[String] match {
            case Right(str) => shape.withRaw(str)
            case _ =>
              ctx.violation(shape.id, "Cannot parse non string schema shape", entry.value)
              shape.withRaw("")
          }
        }
      )

      map.key(
        "x-media-type", { entry =>
          entry.value.to[String] match {
            case Right(str) =>
              shape.withMediaType(str)
            case _ =>
              ctx.violation(shape.id, "Cannot parse non string schema shape", entry.value)
              shape.withMediaType("*/*")
          }
        }
      )

      shape
    }
  }

}
