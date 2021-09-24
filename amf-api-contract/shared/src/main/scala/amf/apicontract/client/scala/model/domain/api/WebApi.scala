package amf.apicontract.client.scala.model.domain.api

import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import org.yaml.model.{YMap, YNode}

case class WebApi(fields: Fields, annotations: Annotations) extends Api(fields: Fields, annotations: Annotations) {
  override def meta: WebApiModel.type = WebApiModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = "/web-api"
}

object WebApi {

  def apply(): WebApi = apply(Annotations())

  def apply(ast: YMap): WebApi = apply(Annotations(ast))

  def apply(node: YNode): WebApi = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): WebApi = WebApi(Fields(), annotations)
}
