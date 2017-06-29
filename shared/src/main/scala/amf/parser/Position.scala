package amf.parser

import java.lang.Math.{max, min}

import amf.parser.Position.ZERO

/** Defines a position on an input */
case class Position(line: Int, column: Int) {
  /* Return min position between actual and given. */
  def min(other: Position): Position =
    if (line < other.line || line == other.line && column <= other.column) this else other

  /* Return max position between actual and given. */
  def max(other: Position): Position =
    if (line > other.line || line == other.line && column >= other.column) this else other

  override def toString: String = s"($line,$column)"
}

/** Defines a range on an input */
case class Range(start: Position, end: Position) {

  /** Extent range */
  def extent(other: Range): Range = Range(start.min(other.start), end.max(other.end))

  override def toString: String = s"[$start-$end]"
}

object Position {

  object ZERO extends Position(0, 0)

  def apply(lc: (Int, Int)): Position = Position(lc._1, lc._2)
}

object Range {

  object NONE extends Range(ZERO, ZERO)

  def apply(start: Position, delta: Int): Range = new Range(start, Position(start.line, start.column + delta))

  def apply(start: (Int, Int), end: (Int, Int)): Range = new Range(Position(start), Position(end))
}
