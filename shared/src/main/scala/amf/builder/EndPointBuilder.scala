package amf.builder

import amf.domain.{EndPoint, Fields}
import amf.metadata.domain.EndPointModel._

/**
  * EndPoint builder.
  */
class EndPointBuilder extends Builder[EndPoint] {
  def withName(name: String): this.type = set(Name, name)

  def withDescription(description: String): this.type = set(Description, description)

  def withPath(path: String): this.type = set(Path, path)

  override def build: EndPoint = EndPoint(fields)

  def build(parentPath: String): EndPoint = EndPoint(fields)
}

object EndPointBuilder {
  def apply(): EndPointBuilder = new EndPointBuilder()

  def apply(fields: Fields): EndPointBuilder = apply().copy(fields)
}
