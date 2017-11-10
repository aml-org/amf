package amf.spec.declaration

import amf.parser.YMapOps
import amf.shape.RamlTypeDefMatcher.matchType
import amf.shape.TypeDef._
import amf.shape._
import amf.spec.raml.RamlTypeExpressionParser
import amf.spec.{Declarations, ParserContext}
import amf.unsafe.PlatformSecrets
import org.yaml.model._

/**
  *
  */
object RamlTypeDetection {
  def apply(part: YPart, declarations: Declarations, parent: String, format: Option[String] = None)(
      implicit ctx: ParserContext): Option[TypeDef] =
    RamlTypeDetecter(declarations, parent, format).detect(part)
}

case class RamlTypeDetecter(declarations: Declarations,
                            parent: String,
                            format: Option[String] = None,
                            recursive: Boolean = false)(implicit val ctx: ParserContext)
    extends RamlTypeSyntax {
  def detect(part: YPart): Option[TypeDef] = part match {

    case scalar: YScalar if RamlTypeDefMatcher.TypeExpression.unapply(scalar.text).isDefined =>
      RamlTypeExpressionParser(shape => shape, declarations, Some(scalar))
        .parse(scalar.text)
        .flatMap(s => ShapeClassTypeDefMatcher(s, scalar, recursive))
        .map {
          case (TypeDef.UnionType | TypeDef.ArrayType) if !recursive => TypeExpressionType
          case other                                                 => other
        } // exceptionc ase when F: C|D (not type, not recursion, union but only have a typeexpression to parse de union

    case scalar: YScalar if matchType(scalar.text, default = UndefinedType) == UndefinedType =>
      // it might be a named type
      declarations
        .findType(scalar.text) match {
        case Some(ancestor) if recursive => ShapeClassTypeDefMatcher(ancestor, scalar, recursive)
        case Some(_) if !recursive       => Some(ObjectType)
        case None                        => Some(UndefinedType)
      }
    case scalar: YScalar => // todo add if wellknowtype?
      val t = scalar.text
      if (format.isDefined) format.map(f => matchType(t, f))
      else Some(matchType(t))
    case sequence: YSequence =>
      InheritsTypeDetecter(collectTypeDefs(sequence), sequence)
        .orElse(Some(ObjectType)) // type expression type?
    case map: YMap =>
      val filterMap = YMap(map.entries.filter(e => !e.key.toString().matches(".*/.*")))
      detectItems(filterMap)
        .orElse(detectProperties(filterMap))
        .orElse(detectAnyOf(filterMap))
        .orElse(detectTypeOrSchema(filterMap))

  }

  private def detectProperties(map: YMap): Option[TypeDef] = {
    map.key("properties").map(_ => ObjectType)
  }

  private def detectItems(map: YMap): Option[TypeDef] = {
    map.key("items") match {
      case (Some(_)) => Some(ArrayType)
      case None      => None
    }
  }

  private def detectAnyOf(map: YMap): Option[TypeDef] = {
    map.key("anyOf").map(_ => UnionType)
  }

  private def detectTypeOrSchema(map: YMap): Option[TypeDef] = {
    if (map.entries.nonEmpty)
      (map
        .key("type")
        .orElse(map.key("schema"))
        .flatMap(e =>
          RamlTypeDetecter(declarations, parent, map.key("(format)").map(_.value.toString()), recursive = true)
            .detect(e.value.value)) match {
        case Some(t) if t == UndefinedType => ShapeClassTypeDefMatcher.fetchByRamlSyntax(map)
        case Some(other)                   => Some(other)
        case None                          => ShapeClassTypeDefMatcher.fetchByRamlSyntax(map)
      }).orElse(Some(ObjectType)) // this is for forward refferences.
    else None
  }

  private def collectTypeDefs(sequence: YSequence): Seq[TypeDef] =
    sequence.nodes
      .map(node => RamlTypeDetecter(declarations, parent, recursive = true).detect(node.value))
      .collect({ case Some(typeDef) => typeDef })

  object InheritsTypeDetecter {
    def apply(inheritsTypes: Seq[TypeDef], ast: YPart): Option[TypeDef] = {
      val head = inheritsTypes.headOption
      if (inheritsTypes.count(_.equals(head.get)) != inheritsTypes.size) {
        ctx.violation("", parent, "Can't inherit from more than one class type", ast)
        Some(UndefinedType)
      } else
        head
    }

    def shapeToType(inherits: Seq[Shape], part: YPart): Option[TypeDef] =
      apply(inherits.flatMap(s => ShapeClassTypeDefMatcher(s, part, plainUnion = true)), part)
  }

  object ShapeClassTypeDefMatcher {
    def apply(shape: Shape, part: YPart, plainUnion: Boolean): Option[TypeDef] =
      shape match {
        case _: NilShape => Some(NilType)
        case _: AnyShape => Some(AnyType)
        case s: ScalarShape =>
          val (typeDef, format) = RamlTypeDefStringValueMatcher.matchType(TypeDefXsdMapping.typeDef(s.dataType))
          Some(matchType(typeDef, format))
        case union: UnionShape => if (plainUnion) InheritsUnionMatcher(union, part) else Some(UnionType)
        case _: NodeShape      => Some(ObjectType)
        case _: ArrayShape     => Some(ArrayType)
        case _                 => None
      }

    object InheritsUnionMatcher {
      def apply(union: UnionShape, part: YPart): Option[TypeDef] =
        new InheritsUnionMatcher(union).matchUnionFather(part)
    }

    case class InheritsUnionMatcher(union: UnionShape) extends PlatformSecrets {
      def matchUnionFather(part: YPart): Option[TypeDef] = {

        InheritsTypeDetecter.shapeToType(union.anyOf, part) match {
          case Some(UndefinedType) =>
            part match {
              case map: YMap => fetchByRamlSyntax(map)
              case _         => None
            }
          case other => other
        }
      }

    }

    private def findEventualShapes(map: YMap): Seq[String] = {
      val shapesNodes = ctx.syntax.nodes.filterKeys(k => k.endsWith("Shape"))

      var possibles: Seq[String] = Seq()
      map.entries.foreach { entry =>
        val locals = shapesNodes.filter(value => value._2(entry.key.toString()))
        if (possibles.isEmpty) possibles = locals.keys.toSeq
        else possibles = locals.keys.filter(k => possibles.contains(k)).toSeq
      }

      possibles.distinct
    }

    def fetchByRamlSyntax(map: YMap): Option[TypeDef] = {
      val defs = findEventualShapes(map).toList
      Option(defs.filter(!_.equals("shape")) match {
        case Nil if defs contains "shape"    => StrType
        case Nil if !(defs contains "shape") => UndefinedType
        case head :: Nil =>
          head match {
            case "nodeShape"         => ObjectType
            case "arrayShape"        => ArrayType
            case "stringScalarShape" => TypeDef.StrType
            case "numberScalarShape" => TypeDef.IntType
            case "fileShape"         => TypeDef.FileType
          }
        case _ :: tail if tail.nonEmpty => MultipleMatch
      })
    }

  }
}
