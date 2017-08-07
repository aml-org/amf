package org.mulesoft.common

package object core {

    /**
      * Common utility methods to deal with Strings.
      */
    implicit class Strings(val str: String) extends AnyVal {

        /** If the String is not null returns the String, else returns "". */
        def notNull: String = if (str == null) "" else str

        /** Returns the number of occurrences of a given char into an String. */
        def count(c: Char): Int = {
            if (str == null) return 0
            var result = 0
            for (i <- 0 until str.length)
                if (str.charAt(i) == c) result += 1
            result
        }

        /** Parse a String with escape sequences. */
        def decode: String = {

            if (str == null) return str
            val length = str.length

            if (length == 0) return str
            val buffer = new StringBuilder(length)

            var i = 0
            while (i < length) {
                val chr = str.charAt(i)
                i += 1
                if (chr != '\\' || i >= length) buffer.append(chr)
                else {
                    val chr = str.charAt(i)
                    i += 1
                    buffer.append(chr match {
                        case 'U' =>
                            i += 8
                            decodeUnicodeChar(str, i - 8, i)
                        case 'u' =>
                            i += 4
                            decodeUnicodeChar(str, i - 4, i)
                        case 'x' =>
                            i += 2
                            decodeUnicodeChar(str, i - 2, i)
                        case 't' => "\t"
                        case 'r' => "\r"
                        case 'n' => "\n"
                        case 'f' => "\f"
                        case '_' => chr.toString
                    })
                }
            }
            buffer.toString
        }

        def encode: String = ("" /: str) (_ + _.encodeChar)

        /** Compare two Strings ignoring the spaces in each */
        def equalsIgnoreSpaces(str2: String): Boolean = {
            def charAt(s: String, i: Int) = if (i >= s.length) '\0' else s.charAt(i)

            var i = 0
            var j = 0
            while (i < str.length || j < str2.length) {
                val c1 = charAt(str, i)
                if (c1.isWhitespace) i = i + 1
                else {
                    val c2 = charAt(str2, j)
                    if (c2.isWhitespace) j = j + 1
                    else {
                        if (c1 != c2) return false
                        i = i + 1
                        j = j + 1
                    }
                }
            }
            true
        }
    }

    /**
      * Common utility methods to deal with Chars.
      */
    implicit class Chars(val chr: Char) extends AnyVal {
        def encodeChar: String =
        // If it is an ISO, no control character return it unchanged
            if (chr >= ' ' && chr < 0x7F || chr > 0x9F && chr < 0xFF) chr.toString
            else
                chr match {
                    case '\n' => "\\n"
                    case '\t' => "\\t"
                    case '\r' => "\\r"
                    case '\f' => "\\f"
                    case _ =>
                        val s = Integer.toHexString(chr)
                        "\\u" + "0" * (4 - s.length) + s
                }
    }

    private def decodeUnicodeChar(str: String, from: Int, to: Int):String = {
        var value = 0
        for (i <- from until to) {
            val n = Character.digit(str.charAt(i), 16)
            if (n == -1) throw new IllegalArgumentException("Malformed unicode encoding: " + str)
            value = (value << 4) | n
        }
        new String(Character.toChars(value))
    }

    /** Count the number of times a given predicate holds true The predicate receives an Int as a parameter */
    def countWhile(predicate: Int => Boolean): Int = {
        var i = 0
        while (predicate(i)) i = i+1
        i
    }
}
