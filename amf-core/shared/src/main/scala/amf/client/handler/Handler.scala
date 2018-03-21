package amf.client.handler

import scala.scalajs.js.annotation.JSExportAll

/** Interface that needs to be implemented to handle a given result, or an exception if something went wrong. */
@JSExportAll
trait Handler[T] {
  def success(result: T)
  def error(exception: Throwable)
}
