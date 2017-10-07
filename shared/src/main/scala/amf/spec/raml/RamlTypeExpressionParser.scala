package amf.spec.raml

import amf.domain.Annotation.ParsedFromTypeExprssion
import amf.metadata.shape.UnionShapeModel
import amf.model.AmfArray
import amf.shape._
import amf.spec.Declarations
import amf.vocabulary.Namespace

protected case class ParsingResult(result: Option[Shape], remaining: Seq[Char])

class RamlTypeExpressionParser(id: String, declarations: Declarations, var i: Int = 0) {
  var parsedShape: Option[Shape] = None
  var acc: String = ""
  var parsingArray = false


  def parse(expression: String): Option[Shape] = {
    val input: Seq[Char] = expression.replaceAll("\\s*", "").toCharArray.toSeq
    parseInput(input).result match {
      case Some(t) =>
        ensureNotEmptyArray(t)
        t.annotations += ParsedFromTypeExprssion(expression)
        Some(t)
      case None => None
    }
  }

  protected def parseInput(input: Seq[Char]): ParsingResult = {
    if (input.isEmpty) {
      processChars()
      ParsingResult(parsedShape,Seq())
    } else {
      input.head match {
        case ')' =>
          processChars()
          ParsingResult(parsedShape, input.tail)
        case '(' =>
          processChars()
          val result = new RamlTypeExpressionParser(id, declarations, i + 1).parseInput(input.tail)
          acceptShape(result.result)
          parseInput(result.remaining)
        case '|' =>
          if (acc == "" && this.parsedShape.isEmpty) {
            throw new Exception("Syntax error, cannot parse Union with empty values")
          }
          processChars()
          toUnion()
          val result = new RamlTypeExpressionParser(id, declarations, i + 1).parseInput(input.tail)
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
          toArray()
          parseInput(input.tail)
        case char =>
          acc += char
          parseInput(input.tail)
      }
    }
  }

  def processChars() = {
    if (acc != "") {
      val shape = acc match {
        case "nil"           => NilShape()
        case "any"           => AnyShape()
        case "string"        => ScalarShape().withDataType((Namespace.Xsd + "string").iri())
        case "integer"       => ScalarShape().withDataType((Namespace.Xsd + "integer").iri())
        case "number"        => ScalarShape().withDataType((Namespace.Xsd + "float").iri())
        case "boolean"       => ScalarShape().withDataType((Namespace.Xsd + "boolean").iri())
        case "datetime"      => ScalarShape().withDataType((Namespace.Xsd + "dateTime").iri())
        case "datetime-only" => ScalarShape().withDataType((Namespace.Xsd + "dateTime").iri())
        case "time-only"     => ScalarShape().withDataType((Namespace.Xsd + "time").iri())
        case "date-only"     => ScalarShape().withDataType((Namespace.Xsd + "date").iri())
        case other           => declarations.findType(other) match {
          case Some(s) => s.link(Some(other)).asInstanceOf[Shape]
          case _       => throw new Exception("Reference not found")
        }
      }
      if (Option(shape.id).isDefined) {
        shape.id = id + s"/parsed$i"
        i += 1
      }
      acc = ""
      acceptShape(Some(shape))
    }
  }

  def toUnion() = {
    parsedShape match {
      case None                => throw new Exception("Syntax error, cannot create empty Union")
      case Some(_: UnionShape) => // ignore
      case Some(shape)         =>
        val union = UnionShape().withId(id + s"/parsed$i")
        i += 1
        parsedShape = Some(union.withAnyOf(Seq(shape)))
    }
  }

  def toArray() = {
    val array = ArrayShape().withId(id + s"/parsed$i")
    i += 1
    parsedShape match {
      case None =>
        parsedShape = Some(array)
      case Some(a: ArrayShape) =>
        val m = MatrixShape().withId(array.id)
        m.withItems(a)
        parsedShape = Some(m)
      case Some(a: MatrixShape) =>
        val m = MatrixShape().withId(array.id)
        m.withItems(a)
        parsedShape = Some(m)
      case Some(other) =>
        parsedShape = Some(array.withItems(other))
    }
  }

  def acceptShape(maybeShape: Option[Shape]) = {
    maybeShape match {
      case None => // ignore
      case Some(array: ArrayShape) if isEmptyArray(array) => {
        parsedShape match {
          case None => parsedShape = Some(array)
          case Some(shape) => parsedShape = Some(fillEmptyArray(array))
        }
      }
      case Some(shape) =>
        parsedShape match {
          case None                => parsedShape = Some(shape)
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
          case _ => throw new Exception(s"Error parsing type expression, cannot accept type ${shape}")
        }
    }
  }

  protected def isEmptyArray(shape: DataArrangementShape): Boolean = {
    shape match {
      case array: ArrayShape  => Option(array.items).isEmpty
      case matrix: MatrixShape => isEmptyArray(matrix.items.asInstanceOf[DataArrangementShape])
    }
  }

  protected def fillEmptyArray(shape: DataArrangementShape) = {
    shape match {
      case array: ArrayShape => {
        parsedShape match {
          case None => shape
          case Some(a: ArrayShape)  => array.toMatrixShape.withItems(a)
          case Some(m: MatrixShape) => array.toMatrixShape.withItems(m)
          case Some(other)          => array.withItems(other)
        }
      }
      case matrix: MatrixShape => {
        parsedShape match {
          case None => shape
          case Some(a: ArrayShape)  => matrix.withItems(a)
          case Some(m: MatrixShape) => matrix.withItems(m)
          case Some(other)          => matrix.toArrayShape.withItems(other)
        }
      }
    }
  }

  protected def ensureNotEmptyArray(t: Shape) = {
    val empty = t match {
      case a: ArrayShape  => isEmptyArray(a)
      case m: MatrixShape => isEmptyArray(m)
      case _              => false
    }
    if (empty) {
      throw new Exception("Syntax error, generating empty array")
    }
  }

}

object RamlTypeExpressionParser {
  def apply(id: String, declarations: Declarations) = new RamlTypeExpressionParser(id, declarations)
}
