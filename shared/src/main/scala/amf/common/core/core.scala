package amf.common

package object core {

  /**
    * Common utility methods to deal with Strings.
    */
  implicit class Strings(val str: String) extends AnyVal {

    /** Add quotes to string. */
    def quote: String = {
      if (isQuoted) str
      else "\"" + str + "\"" // Should escape inner quotes if any...
    }

    private def isQuoted =
      Option(str).exists(s => (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))

    /** Url encoded string. */
    def urlEncoded: String = {
      str.replaceAll("/", "%2F") // TODO encode
    }

    def escape: String = {
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
  }
}
