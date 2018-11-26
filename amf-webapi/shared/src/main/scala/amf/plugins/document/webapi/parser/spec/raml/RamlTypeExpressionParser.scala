package amf.plugins.document.webapi.parser.spec.raml

import amf.core.annotations.{LexicalInformation, SourceVendor}
import amf.core.model.domain.{AmfArray, Shape}
import amf.core.parser.{Annotations, Position, Range, SearchScope}
import amf.core.utils.Strings
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression
import amf.plugins.domain.shapes.metamodel.UnionShapeModel
import amf.plugins.domain.shapes.models._
import org.yaml.model.{YMapEntry, YNode, YPart, YScalar}
protected case class ParsingResult(result: Option[Shape], remaining: Seq[Char])

class RamlTypeExpressionParser(adopt: Shape => Shape, var i: Int = 0, ast: Option[YPart] = None, checking: Boolean)(
    implicit ctx: WebApiContext) {
  var parsedShape: Option[Shape] = None
  var acc: String                = ""
  var parsingArray               = false
  val part: Option[YPart] = ast.map {
    case e: YMapEntry => e.value.value
    case n: YNode     => n.value
    case s: YScalar   => s
  }

  def parse(expression: String): Option[Shape] = {
    val input: Seq[Char] = expression.replaceAll("\\s*", "").toCharArray.toSeq
    parseInput(input).result match {
      case Some(t) =>
        ensureNotEmptyArray(t)
        t.annotations += ParsedFromTypeExpression(expression)
        ast.foreach(p => t.annotations ++= Annotations(p))
        Some(t)
      case None => None
    }
  }

  protected def parseInput(input: Seq[Char]): ParsingResult = {
    if (input.isEmpty) {
      processChars()
      ParsingResult(parsedShape, Seq())
    } else {
      input.head match {
        case ')' =>
          processChars()
          ParsingResult(parsedShape, input.tail)
        case '(' =>
          processChars()
          val result = new RamlTypeExpressionParser(adopt, i + 1, ast, checking).parseInput(input.tail)
          acceptShape(result.result)
          parseInput(result.remaining)
        case '|' =>
          if (acc == "" && this.parsedShape.isEmpty) {
            ctx.violation("", "Syntax error, cannot parse Union with empty values", lexical, location)
          }
          processChars()
          parsedShape = Some(toUnion)
          val result = new RamlTypeExpressionParser(adopt, i + 1, ast, checking).parseInput(input.tail)
          acceptShape(result.result)
          parseInput(result.remaining)
        case '[' =>
          processChars()
          if (parsingArray) { ctx.violation("", "Syntax error, duplicated [", lexical, location) }
          parsingArray = true
          parseInput(input.tail)
        case ']' =>
          if (!parsingArray) {
            ctx.violation("", "Syntax error, Not matching ]", lexical, location)
          }
          parsingArray = false
          parsedShape = Some(toArray)
          parseInput(input.tail)
        case char =>
          acc += char
          parseInput(input.tail)
      }
    }
  }

  private def processChars() = {
    if (acc != "") {
      val shape = acc match {
        case "nil"           => NilShape()
        case "any"           => AnyShape()
        case "file"          => FileShape()
        case "object"        => NodeShape()
        case "string"        => ScalarShape().withDataType((Namespace.Xsd + "string").iri())
        case "integer"       => ScalarShape().withDataType((Namespace.Xsd + "integer").iri())
        case "number"        => ScalarShape().withDataType((Namespace.Shapes + "number").iri())
        case "boolean"       => ScalarShape().withDataType((Namespace.Xsd + "boolean").iri())
        case "datetime"      => ScalarShape().withDataType((Namespace.Xsd + "dateTime").iri())
        case "datetime-only" => ScalarShape().withDataType((Namespace.Xsd + "dateTime").iri())
        case "time-only"     => ScalarShape().withDataType((Namespace.Xsd + "time").iri())
        case "date-only"     => ScalarShape().withDataType((Namespace.Xsd + "date").iri())
        case other =>
          ctx.declarations
            .findType(other, SearchScope.Named) match { // i should not have a reference to fragment in a type expression.
            case Some(s) => s.link(other).asInstanceOf[Shape]
            case _ =>
              val shape = UnresolvedShape(other, part).withName(other)
              shape.withContext(ctx)
              adopt(shape)
              if (!checking) { // if we are just checking a raml type expression type, not parsing it we don't generate unresolved
                shape.unresolved(other, part.getOrElse(YNode.Null))
              }
              shape
          }
      }
      if (Option(shape.id).isEmpty) {
        shape.name.option() match {
          case Some(s) if s.equals("schema") =>
            shape.withName(s"schema-$i")
            adopt(shape)
            shape.withName("schema")
          case None =>
            shape.withName(s"scalar-expression-$i")
            adopt(shape)
            shape.name.remove()
          case _ => adopt(shape)
        }
        i = i + 1
      }
      acc = ""
      acceptShape(Some(shape))
    }
  }

  private def toUnion: Shape = {
    parsedShape match {
      case None =>
        val union = UnionShape()
        adopt(union)
        ctx.violation(union.id, "", None, "Syntax error, cannot create empty Union", lexical, location)
        union
      case Some(u: UnionShape) => u
      case Some(shape) =>
        val union = UnionShape()
        adopt(union)
        union.setArrayWithoutId(UnionShapeModel.AnyOf, Seq(shape))
    }
  }

  private def toArray: Shape = {
    val array = ArrayShape()
    adopt(array)
    parsedShape match {
      case None                 => array
      case Some(a: ArrayShape)  => MatrixShape().withId(array.id).withItems(a)
      case Some(a: MatrixShape) => MatrixShape().withId(array.id).withItems(a)
      case Some(other)          => array.withItems(other)
    }
  }

  private def acceptShape(maybeShape: Option[Shape]) = {
    maybeShape match {
      case None => // ignore
      case Some(array: ArrayShape) if isEmptyArray(array) =>
        parsedShape match {
          case None    => parsedShape = Some(array)
          case Some(_) => parsedShape = Some(fillEmptyArray(array))
        }
      case Some(shape) =>
        parsedShape match {
          case None => parsedShape = Some(shape)
          case Some(union: UnionShape) =>
            shape match {
              case otherUnion: UnionShape =>
                val newAnyOf = union.anyOf ++ otherUnion.anyOf
                union.fields.removeField(UnionShapeModel.AnyOf)
                union.fields.setWithoutId(UnionShapeModel.AnyOf, AmfArray(newAnyOf))
              case _ =>
                val newAnyOf = union.anyOf ++ Seq(shape)
                union.fields.removeField(UnionShapeModel.AnyOf)
                union.fields.setWithoutId(UnionShapeModel.AnyOf, AmfArray(newAnyOf))
            }
          case _ =>
            ctx.violation(shape.id, s"Error parsing type expression, cannot accept type $shape", lexical, location)
            Some(shape)
        }
    }
  }

  protected def isEmptyArray(shape: DataArrangementShape): Boolean = {
    shape match {
      case array: ArrayShape if array.isLink && array.effectiveLinkTarget.isInstanceOf[DataArrangementShape] =>
        isEmptyArray(array.effectiveLinkTarget.asInstanceOf[DataArrangementShape])
      case array: ArrayShape =>
        Option(array.items).isEmpty
      case matrix: MatrixShape if matrix.isLink && matrix.effectiveLinkTarget.isInstanceOf[DataArrangementShape] =>
        isEmptyArray(matrix.effectiveLinkTarget.asInstanceOf[DataArrangementShape])
      case matrix: MatrixShape =>
        isEmptyArray(matrix.items.asInstanceOf[DataArrangementShape])
      case _ =>
        false
    }
  }

  private def fillEmptyArray(shape: DataArrangementShape) = {
    shape match {
      case array: ArrayShape =>
        parsedShape match {
          case None                 => shape
          case Some(a: ArrayShape)  => array.toMatrixShape.withItems(a)
          case Some(m: MatrixShape) => array.toMatrixShape.withItems(m)
          case Some(other)          => array.withItems(other)
        }
      case matrix: MatrixShape =>
        parsedShape match {
          case None                 => shape
          case Some(a: ArrayShape)  => matrix.withItems(a)
          case Some(m: MatrixShape) => matrix.withItems(m)
          case Some(other)          => matrix.toArrayShape.withItems(other)
        }
    }
  }

  private def ensureNotEmptyArray(t: Shape): Unit = {
    val empty = t match {
      case a: ArrayShape  => isEmptyArray(a)
      case m: MatrixShape => isEmptyArray(m)
      case _              => false
    }
    if (empty) ctx.violation(t.id, "Syntax error, generating empty array", lexical, location)
  }

  private val (lexical, location) = part match {
    case p: YPart => (Some(new LexicalInformation(Range(p.range))), p.sourceName.option)
    case _        => (None, None)
  }
}

object RamlTypeExpressionParser {
  def apply(adopt: Shape => Shape, part: Option[YPart] = None, checking: Boolean = false)(
      implicit ctx: WebApiContext) =
    new RamlTypeExpressionParser(adopt, 0, part, checking)
}
