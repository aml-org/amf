package amf.spec.declaration

import amf.parser.{YMapOps, YScalarYRead}
import amf.shape.RamlTypeDefMatcher.matchType
import amf.shape.TypeDef._
import amf.shape._
import amf.spec.raml.{RamlSyntax, RamlTypeExpressionParser}
import amf.spec.{Declarations, ParserContext}
import amf.unsafe.PlatformSecrets
import org.yaml.model._

/**
  *
  */
object RamlTypeDetection {
  def apply(node: YNode, declarations: Declarations, fatherId: String, format: Option[String] = None)(
      implicit ctx: ParserContext): Option[TypeDef] =
    RamlTypeDetecter(declarations, ErrorReporter(fatherId), format).detect(node)
}

case class RamlTypeDetecter(declarations: Declarations,
                            errorReporter: ErrorReporter,
                            format: Option[String] = None,
                            recursive: Boolean = false)(implicit ctx: ParserContext)
    extends RamlTypeSyntax
    with PlatformSecrets {
  def detect(node: YNode): Option[TypeDef] = node.tagType match {
    case YType.Seq =>
      val sequence = node.as[Seq[YNode]]
      InheritsTypeDetecter(collectTypeDefs(sequence), errorReporter, node) // todo review with pedro
        .orElse(Some(ObjectType)) // type expression type?
    case YType.Map =>
      val map       = node.as[YMap]
      val filterMap = YMap(map.entries.filter(e => !e.key.toString().matches(".*/.*")))
      detectItems(filterMap)
        .orElse(detectProperties(filterMap))
        .orElse(detectAnyOf(filterMap))
        .orElse(detectTypeOrSchema(filterMap))
    case _ =>
      val scalar = node.as[YScalar]
      scalar.text match {
        case RamlTypeDefMatcher.TypeExpression(text) =>
          RamlTypeExpressionParser(shape => shape, declarations, Some(node.as[YScalar]))
            .parse(text)
            .flatMap(s => ShapeClassTypeDefMatcher(s, scalar, recursive))
            .map {
              case (TypeDef.UnionType | TypeDef.ArrayType) if !recursive => TypeExpressionType
              case other                                                 => other
            } // exceptionc ase when F: C|D (not type, not recursion, union but only have a typeexpression to parse de union
        case t: String if matchType(t, default = UndefinedType) == UndefinedType =>
          // it might be a named type
          declarations
            .findType(scalar.text) match {
            case Some(ancestor) if recursive => ShapeClassTypeDefMatcher(ancestor, scalar, recursive)
            case Some(_) if !recursive       => Some(ObjectType)
            case None                        => Some(UndefinedType)
          }
        case _ => // todo add if wellknowtype?
          val t = scalar.text
          //      val f = map.key("(format)").map(_.value.value.toScalar.text).getOrElse("")
          if (format.isDefined) format.map(f => matchType(t, f))
          else Some(matchType(t))
      }
  }

//  private def detectInPart
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
          RamlTypeDetecter(declarations, errorReporter, map.key("(format)").map(_.value.toString()), recursive = true)
            .detect(e.value)) match {
        case Some(t) if t == UndefinedType => ShapeClassTypeDefMatcher.fetchByRamlSyntax(map)
        case Some(other)                   => Some(other)
        case None                          => ShapeClassTypeDefMatcher.fetchByRamlSyntax(map)
      }).orElse(Some(ObjectType)) // this is for forward refferences.
    else None
  }

  private def collectTypeDefs(sequence: Seq[YNode]): Seq[TypeDef] =
    sequence
      .map(node => RamlTypeDetecter(declarations, errorReporter, recursive = true).detect(node))
      .collect({ case Some(typeDef) => typeDef })

}

object InheritsTypeDetecter {
  def apply(inheritsTypes: Seq[TypeDef], errorReporter: ErrorReporter, ast: YPart): Option[TypeDef] = {
    val head = inheritsTypes.headOption
    if (inheritsTypes.count(_.equals(head.get)) != inheritsTypes.size) {

      errorReporter.addError(" Cant inherits from more than one class type", ast)
      Some(UndefinedType)
    } else
      head
  }

  def apply(inherits: Seq[Shape], part: YPart, errorReporter: ErrorReporter)(
      implicit ctx: ParserContext): Option[TypeDef] =
    apply(inherits.flatMap(s => ShapeClassTypeDefMatcher(s, part, plainUnion = true)), errorReporter, part)
}

object ShapeClassTypeDefMatcher {
  def apply(shape: Shape, part: YPart, plainUnion: Boolean)(implicit ctx: ParserContext): Option[TypeDef] =
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
    def apply(union: UnionShape, part: YPart)(implicit ctx: ParserContext): Option[TypeDef] =
      new InheritsUnionMatcher(union).matchUnionFather(part)
  }

  case class InheritsUnionMatcher(union: UnionShape)(implicit ctx: ParserContext) extends PlatformSecrets {
    def matchUnionFather(part: YPart): Option[TypeDef] = {

      InheritsTypeDetecter(union.anyOf, part, ErrorReporter("")) match {
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
    val shapesNodes = RamlSyntax.nodes.filterKeys(k => k.endsWith("Shape"))

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

case class ErrorReporter(fatherId: String)(implicit ctx: ParserContext) {
  def addError(message: String, ast: YPart): Unit =
    ctx.violation(fatherId, " Cant inherits from more than one class type", ast)
}
