package amf.core.utils

import amf.core.internal.utils.{AmfStrings, QName}
import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
  * [[amf.core.utils]] test
  */
class CoreTest extends FunSuite {

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
