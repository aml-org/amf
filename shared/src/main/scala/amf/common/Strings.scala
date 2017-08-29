package amf.common

import scala.language.implicitConversions

/**
  *
  */
class Strings(val str: String) {

  /** Remove quotes from string. */
  def unquote: String = {
    if (isQuoted) str.substring(1, str.length - 1)
    else str
  }

  /** Add quotes to string. */
  def quote: String = {
    if (isQuoted) str
    else "\"" + str + "\"" // Should escape inner quotes if any...
  }

  /** Url encoded string. */
  def urlEncoded: String = {
    str.replaceAll("/", "%2F") // TODO encode
  }

  private def isQuoted =
    Option(str).exists(s => (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))
}

object Strings {

  implicit def strings(s: String): Strings = new Strings(s)

  def isNotEmpty(s: String): Boolean = Option(s).exists(_.trim.nonEmpty)

  def isEmpty(s: String): Boolean = !isNotEmpty(s)

  def escape(str: String): String = {
    val result = new StringBuilder()
    for {
      c <- str
    } {
      result.append(c match {
        case '\n'             => "\\n"
        case '"'              => "\""
        case _ if c.isControl => "\\u" + Integer.toHexString(c)
        case _                => c
      })
    }
    result.toString()
  }

  /** Split an String into lines. */
  def lines(str: String): List[String] = split(str, '\n')

  /** Split an String into chunks separated by the specified character. */
  def split(str: String, c: Char): List[String] = {
    val s = if (isNotEmpty(str)) str else ""

    if (s.isEmpty) return Nil

    val n = count(s, c) + 1
    if (n == 1) return List(s)

    var result = List[String]()

    var prev = 0
    for {
      i <- 0 until s.length
    } {
      val c1: Char = s.charAt(i)
      if (c1 == c) {
        result = result :+ s.substring(prev, i)
        prev = i + 1
      }
    }

    result :+ s.substring(prev)
  }

  def count(str: String, c: Char): Int = {
    val s: String = if (isNotEmpty(str)) str else ""
    s.count(_ == c)
  }
}
