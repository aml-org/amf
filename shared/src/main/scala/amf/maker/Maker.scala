package amf.maker

import amf.common.AMFToken.SequenceToken
import amf.model.DomainElement
import amf.parser.{AMFUnit, ASTNode}
import amf.remote.{Raml, Vendor}

/**
  * Maker class.
  */
abstract class Maker[T <: DomainElement](vendor: Vendor) {

  def make: T

  protected def findValues(doc: AMFUnit, path: String): List[String] = {
    findValues(doc.root.children.head, path.split('/').toList)
  }

  protected def findValue(doc: AMFUnit, path: String): String = {
    findValues(doc.root.children.head, path.split('/').toList).headOption.orNull
  }

  protected def findValues(node: ASTNode[_], remainingPath: List[String]): List[String] = remainingPath match {
    case Nil if node.`type` == SequenceToken => node.children.map(_.content).toList
    case Nil                                 => List(node.content)
    case head :: tail =>
      node.children.find(entry => entry.head.content == head).map(e => findValues(e.child(1), tail)).getOrElse(List())
  }
}
