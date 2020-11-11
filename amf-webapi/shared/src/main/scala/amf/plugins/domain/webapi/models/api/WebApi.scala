package amf.plugins.domain.webapi.models.api

import amf.core.metamodel.Obj
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.api.WebApiModel
import org.yaml.model.{YMap, YNode}

case class WebApi(fields: Fields, annotations: Annotations) extends Api(fields: Fields, annotations: Annotations) {
  override def meta: Obj = WebApiModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "#/web-api"
}

object WebApi {

  def apply(): WebApi = apply(Annotations())

  def apply(ast: YMap): WebApi = apply(Annotations(ast))

  def apply(node: YNode): WebApi = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): WebApi = WebApi(Fields(), annotations)
}
