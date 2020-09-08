package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.Shape
import amf.core.parser._
import amf.core.unsafe.PlatformSecrets
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.contexts.parser.raml.{RamlWebApiContext, RamlWebApiContextType}
import amf.plugins.document.webapi.parser.RamlTypeDefMatcher.{JSONSchema, XMLSchema, matchType}
import amf.plugins.document.webapi.parser.spec.raml.RamlTypeExpressionParser
import amf.plugins.document.webapi.parser.spec.raml.expression.RamlExpressionParser
import amf.plugins.document.webapi.parser.{RamlTypeDefMatcher, RamlTypeDefStringValueMatcher, TypeName}
import amf.plugins.domain.shapes.models.TypeDef.{JSONSchemaType, _}
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.parser.TypeDefXsdMapping
import amf.validations.ParserSideValidations._
import amf.validations.ResolutionSideValidations.InvalidTypeInheritanceErrorSpecification
import org.yaml.model._

/**
  *
  */
object RamlTypeDetection {
  def apply(node: YNode, parent: String, format: Option[String] = None, defaultType: DefaultType = StringDefaultType)(
      implicit ctx: RamlWebApiContext): Option[TypeDef] =
    RamlTypeDetector(parent, format, defaultType).detect(node)
}

case class RamlTypeDetector(parent: String,
                            format: Option[String] = None,
                            defaultType: DefaultType = StringDefaultType,
                            recursive: Boolean = false)(implicit ctx: RamlWebApiContext)
    extends RamlTypeSyntax
    with PlatformSecrets {
  def detect(node: YNode): Option[TypeDef] = node.tagType match {

    case YType.Seq =>
      val sequence = node.as[Seq[YNode]]
      InheritsTypeDetecter(collectTypeDefs(sequence), node) // todo review with pedro
        .orElse(Some(ObjectType)) // type expression type?

    case YType.Map =>
      val map          = node.as[YMap]
      val entries      = map.entries.filter(e => !e.key.as[YScalar].text.matches(".*/.*"))
      val filterMap    = YMap(entries, entries.headOption.map(_.sourceName).getOrElse(""))
      val typeExplicit = detectTypeOrSchema(filterMap)
      val infer = detectItems(filterMap)
        .orElse(detectFileTypes(filterMap))
        .orElse(detectProperties(filterMap))
        .orElse(detectAnyOf(filterMap))
      typeExplicit match {
        case Some(JSONSchemaType) if infer.isDefined =>
          ctx.eh.warning(
            JsonSchemaInheratinaceWarningSpecification,
            parent,
            Some(ShapeModel.Inherits.value.iri()),
            "Inheritance from JSON Schema",
            node.value
          )
          typeExplicit
        case Some(_)                 => typeExplicit
        case None if infer.isDefined => infer
        case _                       => inferType(map)
      }

    // Default type as received from the parsing process
    case YType.Null => Some(defaultType.typeDef)

    case _ =>
      val scalar = node.as[YScalar]
      scalar.text match {
        case t if t.startsWith("<<") && t.endsWith(">>") =>
          if (ctx.contextType == RamlWebApiContextType.DEFAULT) {
            ctx.eh.violation(InvalidAbstractDeclarationParameterInType,
                             parent,
                             s"Resource Type/Trait parameter $t in type",
                             node)
          }
          None

        case XMLSchema(_) => Some(XMLSchemaType)

        case JSONSchema(_) => Some(JSONSchemaType)

        case RamlTypeDefMatcher.TypeExpression(text) =>
          RamlExpressionParser
            .check(shape => shape.withId("/"), text)
            .flatMap(s => ShapeClassTypeDefMatcher(s, node, recursive))
            .map {
              case TypeDef.UnionType | TypeDef.ArrayType if !recursive => TypeExpressionType
              case other                                               => other
            } // exception case when F: C|D (not type, not recursion, union but only have a typeexpression to parse de union

        case t if t.endsWith("?") => Some(NilUnionType)

        case t: String if matchType(TypeName(t), default = UndefinedType) == UndefinedType =>
          // it might be a named type
          // its for identify the type, so i can search in all the scope, no need to difference between named ref and includes.

          ctx.declarations
            .findType(scalar.text, SearchScope.All) match {
            case Some(ancestor) if recursive => ShapeClassTypeDefMatcher(ancestor, node, recursive)
            case Some(_) if !recursive       => Some(ObjectType)
            case None                        => Some(UndefinedType)
          }
        case _ => // todo add if well known type?
          val t = scalar.text
          //      val f = map.key("format".asRamlAnnotation).map(_.value.value.toScalar.text).getOrElse("")
          if (format.isDefined) format.map(f => matchType(TypeName(t, f)))
          else Some(matchType(TypeName(t)))
      }
  }

  private def detectProperties(map: YMap): Option[TypeDef] = {
    map.key("properties").map(_ => ObjectType)
  }

  private def detectFileTypes(map: YMap): Option[TypeDef] = map.key("fileTypes").map(_ => FileType)

  private def detectItems(map: YMap): Option[TypeDef] = {
    map.key("items") match {
      case Some(_) => Some(ArrayType)
      case None    => None
    }
  }

  private def detectAnyOf(map: YMap): Option[TypeDef] = map.key("anyOf").map(_ => UnionType)

  private def detectTypeOrSchema(map: YMap): Option[TypeDef] = {
    if (map.entries.nonEmpty) {
      // let's try to detect based on the explicit value of 'type'
      typeOrSchema(map).flatMap(
        e => {
          // let's call ourselves recursively with the value of type
          val result =
            RamlTypeDetector(parent,
                             map.key("format").orElse(map.key("format".asRamlAnnotation)).map(_.value.toString()),
                             recursive = true).detect(e.value)
          result match {
            case Some(t) if t == UndefinedType || (t == AnyType && e.value.toString() != "any") => None
            case Some(other)                                                                    => Some(other)
            case None                                                                           => result
          }
        }
      )

    } else None
  }

  private def inferType(map: YMap) = ShapeClassTypeDefMatcher.fetchByRamlSyntax(map).orElse(Some(defaultType.typeDef))

  /** Get type or schema facet. If both are available, default to type facet and throw a validation error. */
  def typeOrSchema(map: YMap): Option[YMapEntry] = {
    val `type` = map.key("type")
    val schema = map.key("schema")

    for {
      _ <- `type`
      s <- schema
    } {
      ctx.eh.violation(ExclusiveSchemaType, parent, "'schema' and 'type' properties are mutually exclusive", s.key)
    }

    schema.foreach(
      s =>
        ctx.eh.warning(SchemaDeprecated,
                       parent,
                       "'schema' keyword it's deprecated for 1.0 version, should use 'type' instead",
                       s.key))

    `type`.orElse(schema)
  }

  private def collectTypeDefs(sequence: Seq[YNode]): List[TypeDef] =
    sequence
      .flatMap(node => RamlTypeDetector(parent, recursive = true).detect(node))
      .toList

  object InheritsTypeDetecter {
    def apply(inheritsTypes: List[TypeDef], ast: YPart): Option[TypeDef] = {
      inheritsTypes match {
        case Nil         => None
        case head :: Nil => Some(head)
        case _ =>
          val definedTypes = inheritsTypes.filter(_ != UndefinedType)
          if (definedTypes.isEmpty) Some(UndefinedType)
          else {
            val head = definedTypes.headOption
            if (definedTypes.count(_.equals(head.get)) != definedTypes.size) {
              ctx.eh.violation(InvalidTypeInheritanceErrorSpecification,
                               parent,
                               "Can't inherit from more than one class type",
                               ast)
              Some(UndefinedType)
            } else head
          }
      }
    }

    def shapeToType(inherits: Seq[Shape], part: YNode)(implicit ctx: ParserContext): Option[TypeDef] =
      apply(inherits.flatMap(s => ShapeClassTypeDefMatcher(s, part, plainUnion = true)).toList, part)
  }

  object ShapeClassTypeDefMatcher {
    def apply(shape: Shape, part: YNode, plainUnion: Boolean)(implicit ctx: ParserContext): Option[TypeDef] =
      shape match {
        case _ if shape.isLink =>
          shape.linkTarget match {
            case Some(linkedShape: Shape) if linkedShape == shape => Some(AnyType)
            case Some(linkedShape: Shape)                         => apply(linkedShape, part, plainUnion)
            case _ =>
              ctx.eh.violation(InvalidTypeDefinition,
                               shape.id,
                               "Found reference to domain element different of Shape when shape was expected",
                               part)
              None
          }
        case _: NilShape => Some(NilType)
        case s: ScalarShape =>
          val TypeName(typeDef, format) =
            RamlTypeDefStringValueMatcher.matchType(TypeDefXsdMapping.typeDef(s.dataType.value()), s.format.option())
          Some(matchType(TypeName(typeDef, format)))
        case union: UnionShape => if (plainUnion) InheritsUnionMatcher(union, part) else Some(UnionType)
        case _: NodeShape      => Some(ObjectType)
        case _: ArrayShape     => Some(ArrayType)
        case _: MatrixShape    => Some(ArrayType)
        case _: AnyShape       => Some(AnyType)
        case _                 => None
      }

    object InheritsUnionMatcher {
      def apply(union: UnionShape, part: YNode)(implicit ctx: ParserContext): Option[TypeDef] =
        new InheritsUnionMatcher(union).matchUnionFather(part)
    }

    case class InheritsUnionMatcher(union: UnionShape)(implicit ctx: ParserContext) extends PlatformSecrets {
      def matchUnionFather(part: YPart): Option[TypeDef] = {
        val typeSet =
          union.anyOf.flatMap(t => ShapeClassTypeDefMatcher(t, part.asInstanceOf[YNode], plainUnion = true)).toSet
        if (typeSet.size == 1) {
          Some(typeSet.head)
        } else {
          Some(UnionType)
        }
      }
    }

    private def findEventualShapes(map: YMap): Seq[String] = {
      val shapesNodes = ctx.syntax.nodes.filterKeys(k => k.endsWith("Shape") && k != "schemaShape")

      var possibles: Seq[String] = Seq()
      map.entries.foreach { entry =>
        val locals = shapesNodes.filter(value => value._2(entry.key.toString()))
        if (locals.nonEmpty) {
          if (possibles.isEmpty) possibles = locals.keys.toSeq
          else possibles = locals.keys.filter(k => possibles.contains(k)).toSeq
        }
      }

      possibles.distinct
    }

    def fetchByRamlSyntax(map: YMap): Option[TypeDef] = {
      val defs = findEventualShapes(map).toList
      Option(defs.filter(!_.equals("shape")) match {
        case Nil if defs contains "shape"    => StrType
        case Nil if !(defs contains "shape") => defaultType.typeDef
        case head :: Nil =>
          head match {
            case "nodeShape"         => ObjectType
            case "arrayShape"        => ArrayType
            case "stringScalarShape" => TypeDef.StrType
            case "numberScalarShape" => TypeDef.IntType
            case "fileShape"         => TypeDef.FileType
          }
        // explicit inheritance
        case _ :: tail if tail.nonEmpty && map.key("type").isDefined => TypeDef.AnyType
        // multiple matches without inheritance
        case _ :: tail if tail.nonEmpty => MultipleMatch
      })
    }

  }

}
