package amf.client.handler

import scala.scalajs.js

/** Interface that needs to be implemented to handle a given result, or an exception if something went wrong. */
@js.native
trait JsHandler[T] extends js.Object
