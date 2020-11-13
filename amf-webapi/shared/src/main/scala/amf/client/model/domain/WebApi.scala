package amf.client.model.domain

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.plugins.domain.webapi.models.api.{WebApi => InternalWebApi}

@JSExportAll
case class WebApi(override private[amf] val _internal: InternalWebApi) extends Api[WebApi](_internal) {

  @JSExportTopLevel("model.domain.WebApi")
  def this() = this(InternalWebApi())
}
