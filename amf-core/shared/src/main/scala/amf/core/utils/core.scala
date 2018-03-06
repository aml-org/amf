package amf.core

import scala.annotation.tailrec

package object utils {

  /**
    * Common utility methods to deal with Strings.
    */
  implicit class Strings(val str: String) extends AnyVal {

    /** If the String is not null returns the String, else returns "". */
    def notNull: String = Option(str).getOrElse("")

    /** Add quotes to string. */
    def quote: String = {
      if (isQuoted) str
      else "\"" + str + "\"" // Should escape inner quotes if any...
    }

    private def isQuoted =
      Option(str).exists(s => (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))

    /** Url encoded string. */
    def urlEncoded: String = {
      str
        .replaceAll("/", "%2F")
        .replaceAll(" ", "%20")
        .replaceAll("\\{", "%7B")
        .replaceAll("\\}", "%7D")
        .replaceAll("<", "%3C")
        .replaceAll(">", "%3E") // TODO encode
    }

    def urlDecoded: String = {
      str
        .replaceAll("%2F", "/")
        .replaceAll("%20", " ")
        .replaceAll("%7B", "\\{")
        .replaceAll("%7D", "\\}")
        .replaceAll("%3C", "<")
        .replaceAll("%3E", ">") // TODO encode
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

  object TemplateUri {
    def isValid(chars: String): Boolean = {
      @tailrec
      def isBalanced(cs: List[Char], level: Int): Boolean = cs match {
        case Nil                   => level == 0
        case '}' :: _ if level < 1 => false
        case '}' :: xs             => isBalanced(xs, level - 1)
        case '{' :: xs             => isBalanced(xs, level + 1)
        case _ :: xs               => isBalanced(xs, level)
      }
      isBalanced(chars.toList, 0)
    }

    def invalidMsg(uri: String): String = s"'$uri' is not a valid template uri."

    val varPattern = "\\{(.[^{]*)\\}".r
    def variables(path: String): Seq[String] =
      varPattern.findAllIn(path).toSeq.map(v => v.replace("{", "").replace("}", ""))
  }
}
