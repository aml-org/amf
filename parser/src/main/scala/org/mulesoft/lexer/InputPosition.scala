package org.mulesoft.lexer

/**
  * Defines a Position in the Lexer Input
  */
case class InputPosition(line: Int, column: Int) {
  override def toString: String = s"($line,$column)"
}

object InputPosition {
    final val Zero = new InputPosition(0, 0)
    def apply(l:Int=0, c:Int=0): InputPosition = if (l == 0 && c == 0) Zero else new InputPosition(l, c)
}
