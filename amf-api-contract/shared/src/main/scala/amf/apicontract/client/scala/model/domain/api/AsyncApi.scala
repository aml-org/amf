package amf.apicontract.client.scala.model.domain.api

import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.api.AsyncApiModel
import org.yaml.model.{YMap, YNode}

case class AsyncApi(fields: Fields, annotations: Annotations) extends Api(fields: Fields, annotations: Annotations) {
  override def meta: AsyncApiModel.type = AsyncApiModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/async-api"
}

object AsyncApi {

  def apply(): AsyncApi = apply(Annotations())

  def apply(ast: YMap): AsyncApi = apply(Annotations(ast))

  def apply(node: YNode): AsyncApi = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): AsyncApi = AsyncApi(Fields(), annotations)
}
