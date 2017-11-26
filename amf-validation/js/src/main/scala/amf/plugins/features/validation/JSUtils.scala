package amf.plugins.features.validation

import scala.scalajs.js

object JSUtils {

  def maybe[T](obj: js.Dynamic): Option[T] = {
    if (js.isUndefined(obj)) {
      None
    } else if (Option(obj).isEmpty) {
      None
    } else {
      Some(obj).asInstanceOf[Some[T]]
    }
  }

  def default[T](x: js.Dynamic, default: T): T = {
    if (js.isUndefined(x)) {
      default
    } else if (Option(x).isEmpty) {
      default
    } else {
      x.asInstanceOf[T]
    }
  }
}
