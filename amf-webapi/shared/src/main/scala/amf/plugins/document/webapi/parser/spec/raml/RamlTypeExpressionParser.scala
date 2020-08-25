package amf.plugins.document.webapi.parser.spec.raml

import amf.core.annotations.LexicalInformation
import amf.core.model.DataType
import amf.core.model.domain.{AmfArray, Shape}
import amf.core.parser.{Annotations, Position, Range, SearchScope}
import amf.core.utils.{AmfStrings, SimpleCounter}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression
import amf.plugins.domain.shapes.metamodel.UnionShapeModel
import amf.plugins.domain.shapes.models._
import amf.validations.ParserSideValidations.InvalidTypeExpression
import org.yaml.model._
protected case class ParsingResult(result: Option[Shape], remaining: Seq[Char])

// TODO: needs simplification. Way to complicated to read for what it has to do.
// TODO: when simplifying check id generation.
class RamlTypeExpressionParser(adopt: Shape => Unit,
                               var i: Int = 0,
                               ast: Option[YPart] = None,
                               checking: Boolean,
                               expression: String)(implicit ctx: WebApiContext) {
  var parsedShape: Option[Shape] = None
  var acc: String                = ""
  var parsingArray               = false
  val part: Option[YPart] = ast.map {
    case e: YMapEntry => e.value.value
    case n: YNode     => n.value
    case s: YScalar   => s
  }
  private val (lexical, location) =
    part
      .map(p => (Some(new LexicalInformation(Range(p.range))), p.sourceName.option))
      .getOrElse((None, None))

  def parse(): Option[Shape] =
    parseInput(expression).result
      .ifDefinedDo(validateAndAnnotate(expression))

  protected def parseInput(input: Seq[Char]): ParsingResult = {
    input.headOption
      .map {
        case a if a.isWhitespace =>
          parseInput(input.tail)
        case ')' =>
          processParse(input.tail)
        case '(' =>
          processChars()
          parseNext(input)
        case '|' =>
          parseUnion(input)
        case '[' =>
          parseOpenArray(input)
        case ']' =>
          parseCloseArray(input)
        case char =>
          acc += char
          parseInput(input.tail)
      }
      .getOrElse(processParse(Seq.empty))
  }

  private def parseNext(input: Seq[Char]) = {
    val result = new RamlTypeExpressionParser(adopt, i + 1, ast, checking, input.tail.mkString).parseInput(input.tail)
    acceptShape(result.result)

    parseInput(result.remaining)
  }

  private def parseCloseArray(input: Seq[Char]) = {
    if (!parsingArray)
      ctx.eh.violation(InvalidTypeExpression, "", None, "Syntax error, Not matching ]", lexical, location)
    parsingArray = false
    parsedShape = Some(toArray)
    parseInput(input.tail)
  }

  private def parseOpenArray(input: Seq[Char]) = {
    processChars()
    if (parsingArray)
      ctx.eh.violation(InvalidTypeExpression, "", None, "Syntax error, duplicated [", lexical, location)
    parsingArray = true
    parseInput(input.tail)
  }

  private def parseUnion(input: Seq[Char]) = {
    checkEmptyValueViolation()
//    offset += 2
    processChars()
    parsedShape = Some(toUnion(parsedShape))
    val result = new RamlTypeExpressionParser(adopt, i + 1, ast, checking, input.tail.mkString).parseInput(input.tail)
    acceptShape(result.result)
    parseInput(result.remaining)
  }

  private def processParse(input: Seq[Char]): ParsingResult = {
    processChars()
    ParsingResult(parsedShape, input)
  }

  private def processChars(): Unit =
    if (acc.nonEmpty) {
      val shape = acc match {
        case "nil"    => NilShape()
        case "any"    => AnyShape()
        case "file"   => FileShape()
        case "object" => NodeShape()
        case "array"  => ArrayShape()
        case "string" | "integer" | "number" | "boolean" | "datetime" | "datetime-only" | "time-only" | "date-only" =>
          ScalarShape().withDataType(DataType(acc))
        case other =>
          val lexicalInformation: Annotations = extractNewLexicalAnnotations(other, expression, part)
          ctx.declarations
            .findType(other, SearchScope.Named) match { // i should not have a reference to fragment in a type expression.
            case Some(s) =>
              val newShape = s.link(other).asInstanceOf[Shape]
              newShape.annotations ++= lexicalInformation
              newShape
            case _ =>
              val shape = UnresolvedShape(other, part).withName(other)
              shape.withContext(ctx)
              shape.annotations.reject(_.isInstanceOf[LexicalInformation])
              shape.annotations ++= lexicalInformation
              adopt(shape)
              if (!checking) { // if we are just checking a raml type expression type, not parsing it we don't generate unresolved
                shape.unresolved(other, part.getOrElse(YNode.Null))
              }
              shape
          }
      }

      Option(shape.id).ifEmptyDo(() => adoptShape(shape))
      acc = ""
      acceptShape(Some(shape))
    }

  private def extractNewLexicalAnnotations(name: String, expression: String, part: Option[YPart]): Annotations =
    part match {
      case Some(p: YScalar) =>
        val wholeLine = p.text
        val prevLine  = wholeLine.substring(0, wholeLine.length - expression.length)

        val lineFrom   = p.range.lineFrom
        val columnFrom = p.range.columnFrom + prevLine.length + expression.prefixLength(_.isWhitespace)

        Annotations(LexicalInformation(Range(Position(lineFrom, columnFrom), name.length)))
      case _ => Annotations()
    }

  private def adoptShape(shape: Shape): Unit =
    shape.name.option() match {
      case Some(s) if s.equals("schema") =>
        shape.withName(useCounter(i => s"schema-$i"))
        adopt(shape)
        shape.withName("schema")
      case None =>
        shape.withName(useCounter(i => s"scalar-expression-$i"))
        adopt(shape)
        shape.name.remove()
      case _ => adopt(shape)
    }

  private def useCounter(fn: Int => String): String = {
    val result = fn(i)
    i += 1
    result
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

  private def toUnion(maybeShape: Option[Shape]): Shape =
    maybeShape match {
      case None =>
        val union = UnionShape()
        adopt(union)
        ctx.eh.violation(InvalidTypeExpression,
                         union.id,
                         None,
                         "Syntax error, cannot create empty Union",
                         lexical,
                         location)
        union
      case Some(u: UnionShape) => u
      case Some(shape) =>
        val union = UnionShape()
        adopt(union)
        union.setArrayWithoutId(UnionShapeModel.AnyOf, Seq(shape))
    }

  /**
    * updates the parsedShape var
    * @param maybeShape
    */
  private def acceptShape(maybeShape: Option[Shape]): Unit =
    maybeShape.foreach {
      case array: ArrayShape if isEmptyArray(array) =>
        parsedShape
          .ifEmptyDo(() => parsedShape = Some(array))
          .ifDefinedDo {
            case union: UnionShape => union.withAnyOf(union.anyOf :+ array)
            case _                 => parsedShape = Some(fillEmptyArray(array))
          }
      case shape =>
        parsedShape
          .ifEmptyDo(() => parsedShape = Some(shape))
          .ifDefinedDo {
            case union: UnionShape =>
              acceptUnionShape(shape, union)
            case _ => addParseTypeViolation(shape)
          }
    }

  private def acceptUnionShape(shape: Shape, union: UnionShape) =
    shape match {
      case otherUnion: UnionShape =>
        setUnionField(union, union.anyOf ++ otherUnion.anyOf)
      case _ =>
        setUnionField(union, union.anyOf ++ Seq(shape))
    }

  private def setUnionField(union: UnionShape, newAnyOf: Seq[Shape]) = {
    union.fields.removeField(UnionShapeModel.AnyOf)
    union.fields.setWithoutId(UnionShapeModel.AnyOf, AmfArray(newAnyOf))
  }

  protected def isEmptyArray(shape: DataArrangementShape): Boolean = shape match {
    case array: ArrayShape if array.isLink && array.effectiveLinkTarget().isInstanceOf[DataArrangementShape] =>
      isEmptyArray(array.effectiveLinkTarget().asInstanceOf[DataArrangementShape])
    case array: ArrayShape =>
      array.inherits.isEmpty && Option(array.items).isEmpty
    case matrix: MatrixShape if matrix.isLink && matrix.effectiveLinkTarget().isInstanceOf[DataArrangementShape] =>
      isEmptyArray(matrix.effectiveLinkTarget().asInstanceOf[DataArrangementShape])
    case matrix: MatrixShape =>
      isEmptyArray(matrix.items.asInstanceOf[DataArrangementShape])
    case _ =>
      false
  }

  @scala.annotation.tailrec
  private def addItem(arrangementShape: DataArrangementShape)(shape: Shape): Option[DataArrangementShape] =
    arrangementShape match {
      case a: ArrayShape =>
        shape match {
          case _ @(_: ArrayShape | _: MatrixShape) => addItem(a.toMatrixShape)(shape)
          case _                                   => Some(a.withItems(shape))
        }
      case m: MatrixShape =>
        shape match {
          case _ @(_: ArrayShape | _: MatrixShape) => Some(m.withItems(shape))
          case _                                   => None
        }
    }

  private def fillEmptyArray(arrangement: DataArrangementShape): DataArrangementShape =
    parsedShape.flatMap(addItem(arrangement)).getOrElse(arrangement)

  private def validateAndAnnotate(expression: String)(shape: Shape): Unit = {
    validateContentAndIds(shape)
    addAnnotations(expression, shape)
  }

  private def addAnnotations(expression: String, t: Shape): Unit = {
    t.annotations += ParsedFromTypeExpression(expression)
    ast.foreach(p => t.annotations ++= Annotations(p))
  }

  private def validateContentAndIds(t: Shape): Unit = {
    ensureNotEmptyArray(t)
    ensureAnyOfNamesAndIds(t)
  }

  private def ensureAnyOfNamesAndIds(t: Shape): Unit = t match {
    case u: UnionShape =>
      val arrayCounter = new SimpleCounter
      val idCounter    = new SimpleCounter
      u.anyOf.foreach { x =>
        x match {
          case array: ArrayShape if array.name.option().isEmpty =>
            array.withName(s"array_${arrayCounter.next()}")
          case _ => // Nothing
        }
        // TODO: evaluate if after the simplification this is still necessary
        if (!x.isLink) x.adopted(s"${u.id}_${idCounter.next()}")
      }
    case _ => // Nothing
  }

  private def isEmptyArrangement(t: Shape) = t match {
    case a: DataArrangementShape => isEmptyArray(a)
    case _                       => false
  }

  private def ensureNotEmptyArray(t: Shape): Unit =
    if (isEmptyArrangement(t))
      ctx.eh.violation(InvalidTypeExpression, t.id, None, "Syntax error, generating empty array", lexical, location)

  private def checkEmptyValueViolation(): Unit =
    if (acc.isEmpty && this.parsedShape.isEmpty)
      ctx.eh.violation(InvalidTypeExpression,
                       "",
                       None,
                       "Syntax error, cannot parse Union with empty values",
                       lexical,
                       location)

  private def addParseTypeViolation(shape: Shape): Unit =
    ctx.eh.violation(InvalidTypeExpression,
                     shape.id,
                     None,
                     s"Error parsing type expression, cannot accept type $shape",
                     lexical,
                     location)

  implicit class OptionImpl[T](op: Option[T]) {
    def ifEmptyDo(callback: () => Unit): Option[T] = {
      if (op.isEmpty)
        callback()
      op
    }

    def ifDefinedDo(callback: T => Unit): Option[T] = {
      op.foreach(callback)
      op
    }
  }
}

object RamlTypeExpressionParser {
  def apply(adopt: Shape => Unit, part: Option[YPart] = None, expression: String, checking: Boolean = false)(
      implicit ctx: WebApiContext) =
    new RamlTypeExpressionParser(adopt, 0, part, checking, expression)
}
