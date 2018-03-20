package amf.client.handler

import scala.scalajs.js

/** Interface that needs to be implemented to handle a success result from writing a file, or an exception if something went wrong. */
@js.native
trait FileHandler extends js.Object {
  def success(): Unit                   = js.native
  def error(exception: Throwable): Unit = js.native
}
