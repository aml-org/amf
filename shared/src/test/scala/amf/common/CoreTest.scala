package amf.common

import org.scalatest.FunSuite
import org.scalatest.Matchers._
import amf.common.core.{QName, Strings}

/**
  * [[core]] test
  */
class CoreTest extends FunSuite {

  test("Null String") {
    val stringNotNull = "test"
    stringNotNull.notNull should be(stringNotNull)
    val stringNull: String = null
    stringNull.notNull should be("")
  }

  test("Quote String") {
    val testString       = "test"
    val quotedTestString = testString.quote
    quotedTestString should be("\"" + testString + "\"")
    quotedTestString.quote should be(quotedTestString)
  }

  test("Escape special chars") {
    val testStringNewline = "test\ntest"
    testStringNewline.escape should be("test\\ntest")
    val testStringQuotes = s""""test""""
    testStringQuotes.escape should be("\"test\"")
    val testStringIsoControl = '\u0013'.toString
    testStringIsoControl.escape should be("\\u13")
    val testStringNormal = "test"
    testStringNormal.escape should be(testStringNormal)
  }

  test("QName") {
    val qNameWithoutDot = "test"
    QName(qNameWithoutDot) should be(QName("", qNameWithoutDot))
    val qNameWithDot = "test.dot"
    QName(qNameWithDot) should be(QName("test", "dot"))
    val emptyString = ""
    QName(emptyString) should be(QName("", ""))
  }
}
