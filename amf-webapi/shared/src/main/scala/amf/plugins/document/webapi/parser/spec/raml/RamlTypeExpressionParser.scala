package amf.plugins.document.webapi.parser.spec.raml

import amf.core.annotations.LexicalInformation
import amf.core.model.domain.{AmfArray, Shape}
import amf.core.parser.{Range, SearchScope}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression
import amf.plugins.domain.shapes.metamodel.UnionShapeModel
import amf.plugins.domain.shapes.models._
import org.yaml.model.{YNode, YPart}

protected case class ParsingResult(result: Option[Shape], remaining: Seq[Char])

class RamlTypeExpressionParser(adopt: Shape => Shape, var i: Int = 0, part: Option[YPart] = None, checking: Boolean)(
    implicit ctx: WebApiContext) {
  var parsedShape: Option[Shape] = None
  var acc: String                = ""
  var parsingArray               = false

  def parse(expression: String): Option[Shape] = {
    val input: Seq[Char] = expression.replaceAll("\\s*", "").toCharArray.toSeq
    parseInput(input).result match {
      case Some(t) =>
        ensureNotEmptyArray(t)
        t.annotations += ParsedFromTypeExpression(expression)
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
          val result = new RamlTypeExpressionParser(adopt, i + 1, part, checking).parseInput(input.tail)
          acceptShape(result.result)
          parseInput(result.remaining)
        case '|' =>
          if (acc == "" && this.parsedShape.isEmpty) {
            throw new Exception("Syntax error, cannot parse Union with empty values")
          }
          processChars()
          parsedShape = Some(toUnion)
          val result = new RamlTypeExpressionParser(adopt, i + 1, part, checking).parseInput(input.tail)
          acceptShape(result.result)
          parseInput(result.remaining)
        case '[' =>
          processChars()
          if (parsingArray) { throw new Exception("Syntax error, duplicated [") }
          parsingArray = true
          parseInput(input.tail)
        case ']' =>
          if (!parsingArray) { throw new Exception("Syntax error, Not matching ]") }
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
            .findType(other, SearchScope.Named) match { //i should not have a reference to fragment in a type expression.
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
        adopt(shape)
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
        ctx.violation(union.id, "", None, "Syntax error, cannot create empty Union", lexical)
        union
      case Some(u: UnionShape) => u
      case Some(shape) =>
        val union = UnionShape()
        adopt(union)
        union.withAnyOf(Seq(shape))
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
                union.fields.remove(UnionShapeModel.AnyOf)
                union.fields.setWithoutId(UnionShapeModel.AnyOf, AmfArray(newAnyOf))
              case _ =>
                val newAnyOf = union.anyOf ++ Seq(shape)
                union.fields.remove(UnionShapeModel.AnyOf)
                union.fields.setWithoutId(UnionShapeModel.AnyOf, AmfArray(newAnyOf))
            }
          case _ =>
            ctx.violation(shape.id, "", None, s"Error parsing type expression, cannot accept type $shape", lexical)
            Some(shape)
        }
    }
  }

  protected def isEmptyArray(shape: DataArrangementShape): Boolean = {
    shape match {
      case array: ArrayShape   => Option(array.items).isEmpty
      case matrix: MatrixShape => isEmptyArray(matrix.items.asInstanceOf[DataArrangementShape])
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
    if (empty) {
      ctx.violation(t.id, "", None, "Syntax error, generating empty array", lexical)
    }
  }

  private val lexical = part.map(p => Range(p.range)).map(range => LexicalInformation(range))
}

object RamlTypeExpressionParser {
  def apply(adopt: Shape => Shape, part: Option[YPart] = None, checking: Boolean = false)(implicit ctx: WebApiContext) =
    new RamlTypeExpressionParser(adopt, 0, part, checking)
}
