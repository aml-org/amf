package amf.spec

import amf.parser.ASTNode
import amf.common.Strings.strings

import scala.util.matching.Regex

/**
  * Spec matchers.
  */
object Matcher {

  trait Matcher {
    def matches(node: ASTNode[_]): Boolean
  }

  case class KeyMatcher(key: String) extends Matcher {
    override def matches(entry: ASTNode[_]): Boolean = key == entry.head.content.unquote
  }

  case class RegExpMatcher(expr: String) extends Matcher {
    val path: Regex = expr.r

    override def matches(entry: ASTNode[_]): Boolean = entry.head.content.unquote match {
      case path() => true
      case _      => false
    }
  }
}
