package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.model.domain.Shape
import amf.core.parser._
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.RamlTypeDefMatcher.{JSONSchema, XMLSchema, matchType}
import amf.plugins.document.webapi.parser.spec.raml.RamlTypeExpressionParser
import amf.plugins.document.webapi.parser.{RamlTypeDefMatcher, RamlTypeDefStringValueMatcher}
import amf.plugins.domain.shapes.models.TypeDef.{JSONSchemaType, _}
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.parser.TypeDefXsdMapping
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model._
import amf.core.utils.Strings

/**
  *
  */
object RamlTypeDetection {
  def apply(node: YNode, parent: String, format: Option[String] = None, defaultType: DefaultType = StringDefaultType)(
      implicit ctx: WebApiContext): Option[TypeDef] =
    RamlTypeDetector(parent, format, defaultType).detect(node)
}

case class RamlTypeDetector(parent: String,
                            format: Option[String] = None,
                            defaultType: DefaultType = StringDefaultType,
                            recursive: Boolean = false)(implicit ctx: WebApiContext)
    extends RamlTypeSyntax
    with PlatformSecrets {
  def detect(node: YNode): Option[TypeDef] = node.tagType match {

    case YType.Seq =>
      val sequence = node.as[Seq[YNode]]
      InheritsTypeDetecter(collectTypeDefs(sequence), node) // todo review with pedro
        .orElse(Some(ObjectType)) // type expression type?

    case YType.Map =>
      val map       = node.as[YMap]
      val filterMap = YMap(map.entries.filter(e => !e.key.as[YScalar].text.matches(".*/.*")))
      detectItems(filterMap)
        .orElse(detectFileTypes(filterMap))
        .orElse(detectProperties(filterMap))
        .orElse(detectAnyOf(filterMap))
        .orElse(detectTypeOrSchema(filterMap))

    // Default type as received from the parsing process
    case YType.Null => Some(defaultType.typeDef)

    case _ =>
      val scalar = node.as[YScalar]
      scalar.text match {
        case t: String if t.startsWith("<<") && t.endsWith(">>") =>
          ctx.violation(parent, "Trait/Resource Type parameter in type", node)
          None

        case t: String if t.endsWith("?") && wellKnownType(t.replace("?", "")) =>
          Some(NilUnionType)

        case XMLSchema(_) => Some(XMLSchemaType)

        case JSONSchema(_) => Some(JSONSchemaType)

        case RamlTypeDefMatcher.TypeExpression(text) =>
          RamlTypeExpressionParser(shape => shape.withId("/"), Some(node.as[YScalar]), checking = true)
            .parse(text)
            .flatMap(s => ShapeClassTypeDefMatcher(s, node, recursive))
            .map {
              case TypeDef.UnionType | TypeDef.ArrayType if !recursive => TypeExpressionType
              case other                                               => other
            } // exception case when F: C|D (not type, not recursion, union but only have a typeexpression to parse de union

        case t: String if matchType(t, default = UndefinedType) == UndefinedType =>
          // it might be a named type
          // its for identify the type, so i can search in all the scope, no need to difference between named ref and includes.
          ctx.declarations
            .findType(scalar.text, SearchScope.All) match {
            case Some(ancestor) if recursive => ShapeClassTypeDefMatcher(ancestor, node, recursive)
            case Some(_) if !recursive       => Some(ObjectType)
            case None                        => Some(UndefinedType)
          }
        case _ => // todo add if wellknowtype?
          val t = scalar.text
          //      val f = map.key("format".asRamlAnnotation).map(_.value.value.toScalar.text).getOrElse("")
          if (format.isDefined) format.map(f => matchType(t, f))
          else Some(matchType(t))
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
      val fromExplicitType = typeOrSchema(map).flatMap(
        e => {
          // let's call ourselves recursively with the value of type
          val result =
            RamlTypeDetector(parent,
                             map.key("format").orElse(map.key("format".asRamlAnnotation)).map(_.value.toString()),
                             recursive = true).detect(e.value)
          result match {
            case Some(t) if t == UndefinedType => None
            case Some(other)                   => Some(other)
            case None                          => result
          }
        }
      )

      fromExplicitType match {
        case None =>
          // implicit detection here
          ShapeClassTypeDefMatcher.fetchByRamlSyntax(map)
        case explicitType =>
          explicitType // we were able to find a shape looking into the 'type' property
      }
    } else Some(defaultType.typeDef)
  }

  /** Get type or schema facet. If both are available, default to type facet and throw a validation error. */
  def typeOrSchema(map: YMap): Option[YMapEntry] = {
    val `type` = map.key("type")
    val schema = map.key("schema")

    for {
      _ <- `type`
      s <- schema
    } {
      ctx.violation("'schema' and 'type' properties are mutually exclusive", s.key)
    }

    schema.foreach(s =>
      ctx.warning("'schema' keyword it's deprecated for 1.0 version, should use 'type' instead", s.key))

    `type`.orElse(schema)
  }

  private def collectTypeDefs(sequence: Seq[YNode]): Seq[TypeDef] =
    sequence
      .map(node => RamlTypeDetector(parent, recursive = true).detect(node))
      .collect({ case Some(typeDef) => typeDef })

  object InheritsTypeDetecter {
    def apply(inheritsTypes: Seq[TypeDef], ast: YPart): Option[TypeDef] = {
      val head = inheritsTypes.headOption
      if (inheritsTypes.count(_.equals(head.get)) != inheritsTypes.size) {
        ctx.violation(ParserSideValidations.ParsingErrorSpecification.id,
                      parent,
                      "Can't inherit from more than one class type",
                      ast)
        Some(UndefinedType)
      } else
        head
    }

    def shapeToType(inherits: Seq[Shape], part: YNode)(implicit ctx: ParserContext): Option[TypeDef] =
      apply(inherits.flatMap(s => ShapeClassTypeDefMatcher(s, part, plainUnion = true)), part)
  }

  object ShapeClassTypeDefMatcher {
    def apply(shape: Shape, part: YNode, plainUnion: Boolean)(implicit ctx: ParserContext): Option[TypeDef] =
      shape match {
        case _ if shape.isLink =>
          shape.linkTarget match {
            case Some(linkedShape: Shape) if linkedShape == shape => Some(AnyType)
            case Some(linkedShape: Shape)                         => apply(linkedShape, part, plainUnion)
            case _ =>
              ctx.violation(ParserSideValidations.ParsingErrorSpecification.id,
                            shape.id,
                            "Found reference to domain element different of Shape when shape was expected",
                            part)
              None
          }
        case _: NilShape => Some(NilType)
        case s: ScalarShape =>
          val (typeDef, format) =
            RamlTypeDefStringValueMatcher.matchType(TypeDefXsdMapping.typeDef(s.dataType.value()), s.format.option())
          Some(matchType(typeDef, format))
        case union: UnionShape => if (plainUnion) InheritsUnionMatcher(union, part) else Some(UnionType)
        case _: NodeShape      => Some(ObjectType)
        case _: ArrayShape     => Some(ArrayType)
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
      val shapesNodes = ctx.syntax.nodes.filterKeys(k => k.endsWith("Shape"))

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
