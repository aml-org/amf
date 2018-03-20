package amf.client.handler

import scala.scalajs.js

/** Interface that needs to be implemented to handle a string result, or an exception if something went wrong. */
@js.native
trait Handler[T] extends js.Object {
  def success(result: T): Unit          = js.native
  def error(exception: Throwable): Unit = js.native
}
