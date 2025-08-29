package amf.grpc.internal.spec.parser.domain

import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.grpc._
import org.mulesoft.antlrast.ast.{ASTNode, Node}

case class GrpcReservedValuesParser(ast: Node)(implicit ctx: GrpcWebApiContext) extends GrpcASTParserHelper {
  private val MAX_VALUE = 536870911

  def parse(setterFn: Seq[Reserved] => Unit): Seq[Reserved] = {
    val reservedValues = parseReserved()
    setterFn(reservedValues)
    reservedValues
  }

  private def parseReserved(): Seq[Reserved] = {
    val numbersReserved = parseNumbers()
    val fieldsReserved  = parseFieldNames()
    val reservedValues  = numbersReserved ++ fieldsReserved

    if (reservedValues.exists(r => r.fields.fields().isEmpty)) astError("Invalid reserved value", toAnnotations(ast))

    reservedValues
  }

  private def parseNumbers(): Seq[Reserved] = {
    collect(ast, Seq(RANGES, RANGE)).collect { case range: Node =>
      val numbers = find(range, INT_LITERAL).flatMap(getInt)
      processRangeNumbers(numbers, range)
    }
  }

  private def processRangeNumbers(numbers: Seq[Int], range: Node): Reserved = {
    val reserved = Reserved(toAnnotations(range))
    numbers match {
      case Seq(number) if range.children.size == 1 => reserved.withNumber(number)
      case Seq(minToMax) => reserved.withRange(Range(minToMax, MAX_VALUE, toAnnotations(range)))
      case Seq(min, max) => reserved.withRange(Range(min, max, toAnnotations(range)))
      case _             => reserved // return empty reserved as fallback
    }
  }

  private def parseFieldNames(): Seq[Reserved] = {
    collect(ast, Seq(RESERVED_FIELD_NAMES, STRING_LITERAL)).collect { case fieldNode: Node =>
      getString(fieldNode) match {
        case Some(fieldName) => Reserved(toAnnotations(fieldNode)).withFieldName(fieldName)
        case None            => Reserved(toAnnotations(fieldNode)) // return empty reserved as fallback
      }
    }
  }

  private def getString(astNode: ASTNode): Option[String] = extractTerminalValue(astNode).map(removeQuotes)

  private def removeQuotes(value: String): String =
    if (value.length >= 2 && value.startsWith("\"") && value.endsWith("\""))
      value.substring(1, value.length - 1)
    else value

  private def getInt(astNode: ASTNode): Option[Int] = extractTerminalValue(astNode).flatMap(parseIntValue)

  private def parseIntValue(value: String): Option[Int] = value match {
    case "max" => Some(MAX_VALUE)
    case s =>
      try Some(Integer.parseInt(s))
      catch {
        case _: NumberFormatException => None
      }
  }
}
