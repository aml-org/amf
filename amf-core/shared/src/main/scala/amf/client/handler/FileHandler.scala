package amf.client.handler

import scala.scalajs.js.annotation.JSExportAll

/** Interface that needs to be implemented to handle a success result from writing a file, or an exception if something went wrong. */
@JSExportAll
trait FileHandler {
  def success()
  def error(exception: Throwable)
}
