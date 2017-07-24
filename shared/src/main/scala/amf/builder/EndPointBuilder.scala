package amf.builder

import amf.domain.{EndPoint, Fields, Operation}
import amf.metadata.domain.EndPointModel._

/**
  * EndPoint builder.
  */
class EndPointBuilder extends Builder {

  override type T = EndPoint

  def withName(name: String): this.type = set(Name, name)

  def withDescription(description: String): this.type = set(Description, description)

  def withPath(path: String): this.type = set(Path, path)

  def withOperations(operations: Seq[Operation]): this.type = set(Operations, operations)

  override def build: EndPoint = EndPoint(fields)
}

object EndPointBuilder {
  def apply(): EndPointBuilder = new EndPointBuilder()

  def apply(fields: Fields): EndPointBuilder = apply().copy(fields)
}
