package org.mulesoft.lexer

import java.lang.Integer.{MAX_VALUE=>IntMax}

/**
  * A Range in the Input
  */
case class InputRange(lineFrom:Int, columnFrom:Int, lineTo:Int, columnTo:Int) {
    /** Extent range */
    def extent(other: InputRange): InputRange = {
        val first = if (lineFrom < other.lineFrom || lineFrom == other.lineFrom && columnFrom < other.columnFrom)
            this
        else other
        val last = if (lineTo < other.lineTo || lineTo == other.lineTo && columnTo > other.columnTo)
            this
        else other
        InputRange(first.lineFrom, first.columnFrom, last.lineTo, last.columnTo)
    }
    override def toString: String = s"[$lineFrom,$columnFrom..$lineTo,$columnTo]"
}

object InputRange {
    final val Zero = new InputRange(0, 0, 0, 0)
    final val All = new InputRange(0, 0, IntMax, IntMax)

    def apply(lineFrom:Int, columnFrom:Int, lineTo:Int, columnTo:Int):InputRange =
        if (lineFrom == 0 && columnFrom == 0 && lineTo == 0 && lineFrom == 0) Zero
        else if (lineFrom == 0 && columnFrom == 0 && lineTo == IntMax && lineFrom == IntMax) All
        else new InputRange(lineFrom, columnFrom, lineTo, columnTo)
}

