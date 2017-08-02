package amf.builder

import amf.domain._
import amf.metadata.domain.EndPointModel._

/**
  * EndPoint builder.
  */
class EndPointBuilder extends Builder {

  override type T = EndPoint

  def withName(name: String): EndPointBuilder = set(Name, name)

  def withDescription(description: String): EndPointBuilder = set(Description, description)

  def withPath(path: String): EndPointBuilder = set(Path, path)

  def withOperations(operations: Seq[Operation]): EndPointBuilder = set(Operations, operations)

  def withParameters(parameters: Seq[Parameter]): EndPointBuilder = set(Parameters, parameters)

  override def build: EndPoint = EndPoint(fields, annotations)
}

object EndPointBuilder {
  def apply(): EndPointBuilder = apply(Nil)

  def apply(fields: Fields, annotations: List[Annotation] = Nil): EndPointBuilder = apply(annotations).copy(fields)

  def apply(annotations: List[Annotation]): EndPointBuilder = new EndPointBuilder().withAnnotations(annotations)
}
