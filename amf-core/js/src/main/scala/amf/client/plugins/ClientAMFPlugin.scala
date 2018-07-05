package amf.client.plugins

import amf.client.convert.CoreClientConverters._

import scala.scalajs.js

@js.native
trait ClientAMFPlugin extends js.Object {

  val ID: String = js.native

  def dependencies(): ClientList[ClientAMFPlugin] = js.native
  def init(): ClientFuture[ClientAMFPlugin]       = js.native
}
