package amf.client.plugins

import scala.scalajs.js.annotation.JSExportAll
import amf.client.convert.CoreClientConverters._

@JSExportAll
trait ClientAMFPlugin {
  val ID: String

  def dependencies(): ClientList[ClientAMFPlugin]
  def init(): ClientFuture[ClientAMFPlugin]
}
