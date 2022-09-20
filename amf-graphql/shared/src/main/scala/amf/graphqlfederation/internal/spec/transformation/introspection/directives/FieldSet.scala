package amf.graphqlfederation.internal.spec.transformation.introspection.directives

import amf.core.client.scala.model.domain.extensions.PropertyShapePath

case class FieldSet(root: Node) {
  override def toString: String = root.toString
}

object FieldSet {
  def parse(paths: Seq[PropertyShapePath]): FieldSet = {
    paths.map(parse) match {
      case parsed if parsed.size == 1 => FieldSet(parsed.head)
      case parsed if parsed.size > 1 =>
        val root = parsed.tail.foldLeft(parsed.head) { (this_, other) => this_.merge(other) }
        FieldSet(root)
      case parsed if parsed.isEmpty =>
        // throw validation
        // should be unreachable
        FieldSet(Node("", Nil))
    }
  }

  private def parse(path: PropertyShapePath): Node = {
    var children: Seq[Node] = Nil
    path.path.reverse.map { prop =>
      val name = prop.name.value()
      val node = Node(name, children)
      children = Seq(node)
      node
    }.last
  }
}

case class Node(name: String, children: Seq[Node]) {
  def merge(other: Node): Node = {
    val index: Map[String, Node] = this.children.map(c => c.name -> c).toMap
    val mergedChildren = other.children.map {
      case childFromOther if index.contains(childFromOther.name) =>
        val childFromThis = index(childFromOther.name)
        childFromThis merge childFromOther
      case other => other
    }
    Node(this.name, mergedChildren)
  }

  override def toString: String = {
    this.name match {
      case ""                        => children.map(_.toString).mkString(" ")
      case name if children.nonEmpty => s"$name { ${children.map(_.toString).mkString(" ")} }"
      case name                      => name
    }
  }

}
