package amf.client.model.domain

import amf.client.convert.WebApiClientConverters.ClientList

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.plugins.domain.webapi.models.api.{AsyncApi => InternalAsyncApi}
import amf.client.convert.WebApiClientConverters._

@JSExportAll
case class AsyncApi(override private[amf] val _internal: InternalAsyncApi) extends Api[AsyncApi](_internal) {

  @JSExportTopLevel("model.domain.AsyncApi")
  def this() = this(InternalAsyncApi())

  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

}
