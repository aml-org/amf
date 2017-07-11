package amf.builder

import amf.metadata.model.EndPointModel._
import amf.model.EndPoint

/**
  * EndPoint builder.
  */
class EndPointBuilder extends Builder[EndPoint] {
  private var children: List[EndPointBuilder] = Nil

  def withChildren(children: List[EndPointBuilder]): this.type = {
    this.children = children
    this
  }

  def withChildren(children: EndPointBuilder*): this.type = {
    this.children = children.toList
    this
  }

  def withName(name: String): this.type = set(Name, name)

  def withDescription(description: String): this.type = set(Description, description)

  def withPath(path: String): this.type = set(Path, path)

  override def build: EndPoint = EndPoint(fields, fields get Path, children.map(_.build(fields get Path)))

  def build(parentPath: String): EndPoint = {
    val nodePath: String = fields get Path
    fields set (Path, parentPath + nodePath)
    EndPoint(fields, nodePath, children.map(_.build(parentPath + nodePath)))
  }
}

object EndPointBuilder {
  def apply(): EndPointBuilder = new EndPointBuilder()
}
