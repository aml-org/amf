package amf

import scala.scalajs.js.{Dynamic, isUndefined}

/**
  * Created by pedro.colunga on 5/28/17.
  */
object Utils {

  /** Return true if js is running on client side. */
  def isClient: Boolean = !isUndefined(Dynamic.global.document)
}
