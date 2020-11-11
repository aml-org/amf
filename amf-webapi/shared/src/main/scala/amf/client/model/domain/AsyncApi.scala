package amf.client.model.domain

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.plugins.domain.webapi.models.api.{AsyncApi => InternalAsyncApi}

@JSExportAll
case class AsyncApi(override private[amf] val _internal: InternalAsyncApi) extends Api(_internal) {

  @JSExportTopLevel("model.domain.AsyncApi")
  def this() = this(InternalAsyncApi())
}
