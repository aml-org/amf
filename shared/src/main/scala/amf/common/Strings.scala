package amf.common

/**
  * Created by pedro.colunga on 5/23/17.
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

    private def isQuoted = str != null && ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'")))
}

object Strings {
    implicit def strings(s: String): Strings = new Strings(s)

    def isNotEmpty(s: String): Boolean = s != null && s.trim.nonEmpty

    def escape(str: String): String = {
        val result = new StringBuilder()
        for (c <- str) {
            result.append(c match {
                case '\n' => "\\n"
                case '"' => "\""
                case _ if c.isControl => "\\u" + Integer.toHexString(c)
                case _ => c
            })
        }
        result.toString()
    }
}
