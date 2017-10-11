package amf.common

package object core {

  /**
    * Common utility methods to deal with Strings.
    */
  implicit class Strings(val str: String) extends AnyVal {

    /** If the String is not null returns the String, else returns "". */
    def notNull: String = if (str == null) "" else str

    /** Add quotes to string. */
    def quote: String = {
      if (isQuoted) str
      else "\"" + str + "\"" // Should escape inner quotes if any...
    }

    private def isQuoted =
      Option(str).exists(s => (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))

    /** Url encoded string. */
    def urlEncoded: String = {
      str.replaceAll("/", "%2F").replaceAll("\\{", "%7B").replaceAll("\\}", "%7D") // TODO encode
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

  case class QName(qualification: String, name: String) {
    val isQualified: Boolean = qualification.nonEmpty
    val isEmpty: Boolean     = qualification.isEmpty && name.isEmpty
  }

  object QName {

    def apply(fqn: String): QName = {
      if (fqn.notNull.trim.nonEmpty) {
        fqn.lastIndexOf('.') match {
          case -1  => QName("", fqn)
          case dot => QName(fqn.substring(0, dot), fqn.substring(dot + 1))
        }
      } else Empty
    }

    val Empty = QName("", "")
  }
}
