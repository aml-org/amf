package amf.maker

import amf.common.AMFToken.SequenceToken
import amf.model.DomainElement
import amf.parser.ASTNode
import amf.remote.Vendor
import amf.common.Strings.strings

/**
  * Maker class.
  */
abstract class Maker[T <: DomainElement[_, _]](val vendor: Vendor) {

  def make: T

  protected def findValues(node: ASTNode[_], path: String): List[String] = {
    findValues(node, path.split('/').toList)
  }

  protected def findValue(node: ASTNode[_], path: String): String = {
    findValues(node, path.split('/').toList).headOption.orNull
  }

  protected def findValues(node: ASTNode[_], remainingPath: List[String]): List[String] = remainingPath match {
    case Nil if node.`type` == SequenceToken => node.children.map(_.content).toList.map(e => e.unquote)
    case Nil                                 => List(node.content).map(e => e.unquote)
    case head :: tail =>
      node.children
        .find(entry => entry.head.content.unquote == head)
        .map(e => findValues(e.child(1), tail))
        .getOrElse(List())
  }
}
