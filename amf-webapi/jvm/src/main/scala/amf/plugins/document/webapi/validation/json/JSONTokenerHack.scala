package amf.plugins.document.webapi.validation.json

import java.lang
import org.json.{JSONTokener}

import scala.util.matching.Regex

class JSONTokenerHack(text: String) extends JSONTokener(text) {

  override def nextValue(): Object = nextValueHack()

  /** This is extracted from JSONTokener class to replace the JSONObject.stringToValue method used in the superclass
    * so that we can fail when an unquoted value is processed (as all the other json parsers).
    */
  private def nextValueHack(): Object = hackDecimal {
    this.nextClean() match {
      case c @ ('"' | '\'') =>
        this.nextString(c)
      case '{' =>
        this.back()
        new JSONObject(this)
      case '[' =>
        this.back()
        new JSONArray(this)
      case c =>
        val sb      = new StringBuilder()
        var newChar = c
        while (shouldContinueParsing(newChar)) {
          sb.append(newChar)
          newChar = this.next()
        }
        this.back()

        val string = sb.toString.trim()
        if ("" == string) throw this.syntaxError("Missing value")
        JSONObject.stringToValue(string)
    }
  }

  protected def shouldContinueParsing(newChar: Char) = newChar >= ' ' && ",:]}/\\\"[{;=#".indexOf(newChar) < 0

  /**
    * numbers ending with .0 are converted to integer value. This is needed to maintain compatibility with js.
    * */
  private def hackDecimal(value: Object) =
    value match {
      case double: lang.Double      => removeRedundantDecimal(double.toString, double)
      case bd: java.math.BigDecimal => removeRedundantDecimal(bd.toString, bd)
      case _                        => value
    }

  private def removeRedundantDecimal(str: String, value: Object): Object = {
    val pattern = "[0-9]+(\\.0+)".r

    str match {
      case pattern(group) => JSONObject.stringToValue(str.stripSuffix(group))
      case _              => value
    }
  }

  private implicit class CaseInsensitiveRegex(sc: StringContext) {
    def ci: Regex = ("(?i)" + sc.parts.mkString).r
  }
}

class ScalarTokenerHack(text: String) extends JSONTokenerHack(text) {
  override protected def shouldContinueParsing(newChar: Char): Boolean = newChar != 0
}
