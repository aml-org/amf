package amf.shapes.internal.document.apicontract.validation.json

import amf.shapes.internal.validation.payload.MaxNestingValueReached
import org.json.JSONTokener

import java.lang
import scala.util.matching.Regex

// Instance must be discarded after each use
class JSONTokenerHack(text: String, val nestingLimit: Long) extends JSONTokener(text) {

  private var currentNestingAmountReached: Long = 0

  def parseAll(): Object = {
    val parsed = nextValueHack()
    if (thereIsRemainingInput) throw syntaxError("There is invalid additional input  next to the last } or ]")
    else parsed
  }

  private def thereIsRemainingInput = {
    nextClean() // dump last character
    !end() && nextClean() != 0
  }

  override def nextValue(): Object = nextValueHack()

  /** This is extracted from JSONTokener class to replace the JSONObject.stringToValue method used in the superclass so
    * that we can fail when an unquoted value is processed (as all the other json parsers).
    */
  private def nextValueHack(): Object = hackDecimal {
    this.nextClean() match {
      case c @ ('"' | '\'') =>
        this.nextString(c)
      case '{' =>
        this.back()
        withoutReachingMaxNestedLevel {
          new JSONObject(this)
        }
      case '[' =>
        this.back()
        withoutReachingMaxNestedLevel {
          new JSONArray(this)
        }
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

  protected def withoutReachingMaxNestedLevel[T](thunk: => T): T = {
    currentNestingAmountReached += 1
    if (currentNestingAmountReached >= nestingLimit) throw MaxNestingValueReached(nestingLimit)
    else {
      val result = thunk
      currentNestingAmountReached -= 1
      result
    }
  }

  protected def shouldContinueParsing(newChar: Char) = newChar >= ' ' && ",:]}/\\\"[{;=#".indexOf(newChar) < 0

  /** numbers ending with .0 are converted to integer value. This is needed to maintain compatibility with js.
    */
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

class ScalarTokenerHack(text: String, nestingLimit: Long) extends JSONTokenerHack(text, nestingLimit: Long) {
  override protected def shouldContinueParsing(newChar: Char): Boolean = newChar != 0
}
