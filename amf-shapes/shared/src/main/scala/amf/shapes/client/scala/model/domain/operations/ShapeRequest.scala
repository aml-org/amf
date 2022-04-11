package amf.shapes.client.scala.model.domain.operations

import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.operations.ShapeRequestModel
import amf.shapes.internal.domain.metamodel.operations.ShapeRequestModel._
import org.yaml.model.YPart

case class ShapeRequest(override val fields: Fields, override val annotations: Annotations) extends AbstractRequest {
  override type ParameterType = ShapeParameter

  override private[amf] def buildQueryParameter: ShapeParameter = ShapeParameter()

  override def meta: ShapeRequestModel.type = ShapeRequestModel

  override def queryParameters: Seq[ShapeParameter]                            = fields.field(QueryParameters)
  override def withQueryParameters(parameters: Seq[ShapeParameter]): this.type = setArray(QueryParameters, parameters)
  override def withQueryParameter(name: String): ShapeParameter = {
    val result = ShapeParameter().withName(name)
    add(QueryParameters, result)
    result
  }

}

object ShapeRequest {
  def apply(): ShapeRequest = apply(Annotations())

  def apply(ast: YPart): ShapeRequest = apply(Annotations(ast))

  def apply(annotations: Annotations): ShapeRequest = new ShapeRequest(Fields(), annotations)
}
