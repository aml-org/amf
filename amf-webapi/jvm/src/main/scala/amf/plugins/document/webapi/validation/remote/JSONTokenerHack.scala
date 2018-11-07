package amf.plugins.document.webapi.validation.remote
import java.lang

import org.json.JSONTokener

class JSONTokenerHack(text: String) extends JSONTokener(text) {

  override def nextValue(): Object = hack(super.nextValue())

  private def hack(value: Object) = {
    value match {
      case double: lang.Double => hackDouble(double)
      case _                   => value
    }
  }

  private def hackDouble(d: java.lang.Double): Object = {
    val pattern = "[0-9]+(\\.0+)".r
    d.toString match {
      case pattern(group) =>
        new lang.Integer(d.toString.stripSuffix(group))
      case _ => d
    }
  }

}
