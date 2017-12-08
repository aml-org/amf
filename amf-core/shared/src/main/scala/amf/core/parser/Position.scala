package amf.core.parser

import amf.core.parser.Position.ZERO
import org.mulesoft.lexer.InputRange

import scala.scalajs.js.annotation.JSExportAll

/** Defines a position on an input */
@JSExportAll
case class Position(line: Int, column: Int) extends Comparable[Position] {

  /** Return true if position is less than specified position. */
  def lt(o: Position): Boolean = compareTo(o) < 0

  /* Return min position between actual and given. */
  def min(other: Position): Position =
    if (line < other.line || line == other.line && column <= other.column) this else other

  /* Return max position between actual and given. */
  def max(other: Position): Position =
    if (line > other.line || line == other.line && column >= other.column) this else other

  override def compareTo(o: Position): Int = {
    val result = line - o.line
    if (result == 0) column - o.column else result
  }

  def isZero: Boolean = this == ZERO

  override def toString: String = s"($line,$column)"
}

/** Defines a range on an input */
@JSExportAll
case class Range(start: Position, end: Position) {

  /** Extent range */
  def extent(other: Range): Range = Range(start.min(other.start), end.max(other.end))

  override def toString: String = s"[$start-$end]"
}

object Position {

  object ZERO extends Position(0, 0) {
    override def compareTo(o: Position): Int = 1
  }

  def apply(lc: (Int, Int)): Position = Position(lc._1, lc._2)
}

object Range {

  object NONE extends Range(ZERO, ZERO)

  def apply(r: InputRange): Range = new Range(Position(r.lineFrom, r.columnFrom), Position(r.lineTo, r.columnTo))

  def apply(start: Position, delta: Int): Range = new Range(start, Position(start.line, start.column + delta))

  def apply(start: (Int, Int), end: (Int, Int)): Range = new Range(Position(start), Position(end))

  def apply(serialized: String): Range = {
    val Pattern = "\\[\\(([0-9]*),([0-9]*)\\)-\\(([0-9]*),([0-9]*)\\)\\]".r
    serialized match {
      case Pattern(l1, c1, l2, c2) => Range((l1.toInt, c1.toInt), (l2.toInt, c2.toInt))
    }
  }
}
