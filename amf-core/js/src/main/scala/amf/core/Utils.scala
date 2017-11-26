package amf.core

import scala.scalajs.js.{Dynamic, isUndefined}

/**
  *
  */
object Utils {

  /** Return true if js is running on client side. */
  def isClient: Boolean = !isUndefined(Dynamic.global.document)
}
