//package amf.common
//
//import org.mulesoft.common.core.decodeUnicodeChar
//
///**
//  *
//  */
//
//// Strings name? only one package object its allowed by each package
//package object strings {
//
//  implicit class Strings(val str: String) {
//
//    /** Remove quotes from string. */
//    def unquote: String = {
//      if (isQuoted) str.substring(1, str.length - 1)
//      else str
//    }
//
//    /** Add quotes to string. */
//    def quote: String = {
//      if (isQuoted) str
//      else "\"" + str + "\"" // Should escape inner quotes if any...
//    }
//
//    /** Url encoded string. */
//    def urlEncoded: String = {
//      str.replaceAll("/", "%2F") // TODO encode
//    }
//
//    private def isQuoted =
//      Option(str).exists(s => (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))
//
//    def escape: String = {
//      val result = new StringBuilder()
//      for {
//        c <- str
//      } {
//        result.append(c match {
//          case '\n'             => "\\n"
//          case '"'              => "\""
//          case _ if c.isControl => "\\u" + Integer.toHexString(c)
//          case _                => c
//        })
//      }
//      result.toString()
//    }
//
//
//    /** If the String is not null returns the String, else returns "". */
//    def notNull: String = if (str == null) "" else str
//
//    /** Returns the number of occurrences of a given char into an String. */
//    def count(c: Char): Int = {
//      if (str == null) return 0
//      var result = 0
//      for (i <- 0 until str.length)
//        if (str.charAt(i) == c) result += 1
//      result
//    }
//
//    /** Parse a String with escape sequences. */
//    def decode: String = {
//
//      if (str == null) return str
//      val length = str.length
//
//      if (length == 0) return str
//      val buffer = new StringBuilder(length)
//
//      var i = 0
//      while (i < length) {
//        val chr = str.charAt(i)
//        i += 1
//        if (chr != '\\' || i >= length) buffer.append(chr)
//        else {
//          val chr = str.charAt(i)
//          i += 1
//          buffer.append(chr match {
//            case 'U' =>
//              i += 8
//              decodeUnicodeChar(str, i - 8, i)
//            case 'u' =>
//              i += 4
//              decodeUnicodeChar(str, i - 4, i)
//            case 'x' =>
//              i += 2
//              decodeUnicodeChar(str, i - 2, i)
//            case 't' => "\t"
//            case 'r' => "\r"
//            case 'n' => "\n"
//            case 'f' => "\f"
//            case '_' => chr.toString
//          })
//        }
//      }
//      buffer.toString
//    }
//
//    def encode: String = ("" /: str) (_ + _.encodeChar)
//
//    /** Compare two Strings ignoring the spaces in each */
//    def equalsIgnoreSpaces(str2: String): Boolean = {
//      def charAt(s: String, i: Int) = if (i >= s.length) '\0' else s.charAt(i)
//
//      var i = 0
//      var j = 0
//      while (i < str.length || j < str2.length) {
//        val c1 = charAt(str, i)
//        if (c1.isWhitespace) i = i + 1
//        else {
//          val c2 = charAt(str2, j)
//          if (c2.isWhitespace) j = j + 1
//          else {
//            if (c1 != c2) return false
//            i = i + 1
//            j = j + 1
//          }
//        }
//      }
//      true
//    }
//
//  }
//}
