package amf.builder

import amf.common.Strings.strings
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

  override def resolveId(container: String): this.type = {
    val path: String = fields(Path)
    withId(container + "/end-points/" + path.urlEncoded)
  }

  override def build: EndPoint = null
}

object EndPointBuilder {
  def apply(): EndPointBuilder = apply(Nil)

  def apply(fields: Fields, annotations: List[Annotation] = Nil): EndPointBuilder = apply(annotations).copy(fields)

  def apply(annotations: List[Annotation]): EndPointBuilder = new EndPointBuilder().withAnnotations(annotations)
}
