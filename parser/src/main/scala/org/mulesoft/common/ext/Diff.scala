package org.mulesoft.common.ext

import java.io.File

import org.mulesoft.common.core._
import org.mulesoft.common.ext.Diff.{Delta, PathNode}

import scala.io.Source
import scala.reflect.ClassTag

/**
  * Diff class to compare objects.
  */
class Diff[T <: AnyRef : ClassTag](equalsComparator: (T, T) => Boolean) {

    private def equal(a: T, b: T): Boolean = (a eq b) || b != null && equalsComparator(a, b)

    /**
      * Computes the difference between the 2 sequences and returns it as a List of Delta objects.
      */
    def diff(at: Traversable[T], bt: Traversable[T]): List[Delta[T]] = {
        val a = at.toArray
        val b = bt.toArray
        buildRevision(buildPath(a, b), a, b)
    }

    protected def extractSubList(a: Array[T], from: Int, to: Int): Array[T] = a.slice(from, to)

    def buildPath(a: Array[T], b: Array[T]): PathNode = {
        val aSize = a.length
        val bSize = b.length

        val max = aSize + bSize + 1
        val size = 1 + 2 * max
        val middle = size / 2
        val diagonal = new Array[PathNode](size)

        diagonal(middle + 1) = PathNode.snake(0, -1, null)
        for {
            d <- 0 until max
            k <- -d to d by 2
        } {
            val kMiddle = middle + k
            val kPlus = kMiddle + 1
            val kMinus = kMiddle - 1
            var prev: PathNode = null

            var i = 0
            if ((k == -d) || (k != d && diagonal(kMinus).i < diagonal(kPlus).i)) {
                i = diagonal(kPlus).i
                prev = diagonal(kPlus)
            } else {
                i = diagonal(kMinus).i + 1
                prev = diagonal(kMinus)
            }

            var j = i - k

            var node = PathNode.diffNode(i, j, prev)

            // orig and rev are zero-based
            // but the algorithm is one-based
            // that's why there's no +1 when indexing the sequences
            while (i < aSize && j < bSize && equal(a(i), b(j))) {
                i = i + 1
                j = j + 1
            }
            if (i > node.i) node = PathNode.snake(i, j, node)

            diagonal(kMiddle) = node

            if (i >= aSize && j >= bSize) return diagonal(kMiddle)
        }
        null
    }

    /** Constructs a List of Deltas from a difference path. */
    private def buildRevision(originalPath: PathNode, a: Array[T], b: Array[T]): List[Delta[T]] = {
        var patch: List[Delta[T]] = Nil

        var path: PathNode =
            if (originalPath == null) null
            else if (originalPath.isSnake) originalPath.prev
            else originalPath

        while (path != null && path.prev != null && path.prev.j >= 0) {
            if (path.isSnake) throw new IllegalStateException()

            val i = path.i
            val j = path.j

            path = path.prev

            val delta =
                Delta[T](path.i, path.j, extractSubList(a, path.i, i), extractSubList(b, path.j, j))
            if (delta != null)
                patch = delta :: patch

            if (path.isSnake) path = path.prev
        }
        patch
    }
}

object Diff {

    def main(args: Array[String]): Unit = {
        val deltas = Diff[String]().diff(List("A", "B", "C"), List("X", "C"))
        println(deltas)
    }

    /** Creates a case insensitive diff for Strings. */
    def caseInsensitive: Diff.Str = new Str(_ equalsIgnoreCase _)

    /** Creates a case sensitive diff for Strings. */
    def caseSensitive: Diff.Str = new Str()

    /** Create a differ with the specified comparator. */
    def apply[T](comparator: (T, T) => Boolean = (a: T, b: T) => a == b) = new Diff(comparator)

    /** Creates a diff Strings that ignore all space. */
    def ignoreAllSpace: Diff.Str = new Str(_ equalsIgnoreSpaces _, true)

    /** Convert all the list of deltas to a single string. */
    def makeString[T](deltas: Traversable[Delta[T]]): String = deltas.mkString

    /** Create a differ with the specified String comparator. */
    def stringDiffer(comparator: (String, String) => Boolean): Str = new Str(comparator, false)

    /** Creates a case sensitive diff that trim Strings, before comparing them. */
    def trimming: Str = new Str(_.trim() == _.trim(), false)

    class PathNode(val i: Int, val j: Int, val prev: PathNode, val isSnake: Boolean = false) {
        def previousSnake: PathNode =
            if (i < 0 || j < 0) null else if (!isSnake && prev != null) prev.previousSnake else this
    }

    object PathNode {
        def snake(i: Int, j: Int, prev: PathNode): PathNode = new PathNode(i, j, prev, true)

        def diffNode(i: Int, j: Int, prev: PathNode): PathNode = {
            new PathNode(i, j, if (prev == null) null else prev.previousSnake)
        }
    }

    class Str(equalsComparator: (String, String) => Boolean = _ == _,
              ignoreEmptyLine: Boolean = false)
        extends Diff[String](equalsComparator) {

        /**
          * Computes the difference between the 2 strings split by end of line and returns it as a
          * List of Delta objects.
          */
        def diff(a: String, b: String): List[Delta[String]] = diff(a.lines.toList, b.lines.toList)

        /**
          * Computes the difference between the 2 Files and returns it as a List of Delta objects.
          */
        def diff(a: File, b: File): List[Delta[String]] =
            diff(Source.fromFile(a).getLines().toList, Source.fromFile(b).getLines().toList)

        override protected def extractSubList(a: Array[String], from: Int, to: Int): Array[String] = {
            val ts = super.extractSubList(a, from, to)
            if (ignoreEmptyLine) {
                if (ts.exists(_.nonEmpty)) ts else Array()
            } else ts
        }
    }

    sealed abstract class Delta[T](aPosition: Int,
                                   bPosition: Int,
                                   aLines: Array[T],
                                   bLines: Array[T]) {

        /**
          * Print the Delta using the standard Diff format.
          */
        override def toString: String = {
            val s = new StringBuilder
            s ++= rangeAsStr + "\n"
            for (l <- aLines) s ++= "< " + l + "\n"
            if (aLines.nonEmpty && bLines.nonEmpty) s ++= "---\n"
            for (l <- bLines) s ++= "> " + l + "\n"
            s.toString()
        }

        protected def range(pos: Int, lines: Array[T]): String = {
            val from = pos + 1
            val to = pos + lines.length
            from + (if (from == to) "" else "," + to)
        }

        def rangeAsStr: String
    }

    object Delta {
        def apply[T](aPosition: Int, bPosition: Int, aLines: Array[T], bLines: Array[T]): Delta[T] =
            if (aLines.isEmpty) {
                if (bLines.isEmpty) null else Add(aPosition, bPosition, aLines, bLines)
            } else if (bLines.isEmpty) Delete(aPosition, bPosition, aLines, bLines)
            else Change(aPosition, bPosition, aLines, bLines)
    }

    case class Add[T](aPosition: Int, bPosition: Int, aLines: Array[T], bLines: Array[T])
        extends Delta[T](aPosition, bPosition, aLines, bLines) {
        override def rangeAsStr: String = aPosition + "a" + range(bPosition, bLines)

    }

    case class Delete[T](aPosition: Int, bPosition: Int, aLines: Array[T], bLines: Array[T])
        extends Delta[T](aPosition, bPosition, aLines, bLines) {
        override def rangeAsStr: String = range(aPosition, aLines) + "d" + bPosition
    }

    case class Change[T](aPosition: Int, bPosition: Int, aLines: Array[T], bLines: Array[T])
        extends Delta[T](aPosition, bPosition, aLines, bLines) {
        override def rangeAsStr: String = range(aPosition, aLines) + "c" + range(bPosition, bLines)

    }

}
