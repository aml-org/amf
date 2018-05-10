package amf.core

import amf.core.unsafe.PlatformSecrets

import scala.annotation.tailrec

//noinspection ScalaFileName
package object utils {

  implicit class MediaTypeMatcher(val content: String) extends AnyVal {

    def guessMediaType(isScalar: Boolean): String = { // move to objects
      if (isXml && !isScalar) "application/xml"
      else if (isJson && !isScalar) "application/json"
      else "text/vnd.yaml" // by default, we will try to parse it as YAML
    }

    def isXml: Boolean = content.trim.startsWith("<")

    def isJson = content.trim.startsWith("{") || content.startsWith("[")
  }

//  implicit class UrlNormalizer(val rawUrl: String) extends AnyVal with PlatformSecrets{
//
////    def normalizePath: String = {
////      val escaped = escapeFileSystemPath(rawUrl)
////      if (escaped.contains("[") || escaped.contains("]")) // what else is incompatible
////        escaped
////      else new java.net.URI(escaped).normalize().toString
////    }
//
////    protected def escapeFileSystemPath(rawUrl: String): String = rawUrl.replace(" ", "%20") // all encodeds replaced?
//
//  }

  /**
    * Common utility methods to deal with Strings.
    */
  implicit class Strings(val str: String) extends PlatformSecrets {

    /** If the String is not null returns the String, else returns "". */
    def notNull: String = Option(str).getOrElse("")

    /** Add quotes to string. */
    def quote: String = {
      if (isQuoted) str
      else "\"" + str + "\"" // Should escape inner quotes if any...
    }

    def validReferencePath: Boolean = str.split("\\.").length < 3

    private def isQuoted =
      Option(str).exists(s => (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))

    /** Prepend correct protocol prefix, then encode */
    def normalizeUrl: String = {
      val url =
        if (str == null || str.isEmpty) ""
        else if (str.startsWith("http:") || str.startsWith("https:") || str.startsWith("file:")) str
        else if (str.startsWith("/")) "file:/" + str
        else "file://" + str

      platform.normalizeURL(url)
    }

    /** normalize and check that the string its a valid URI */
    def normalizePath: String = platform.normalizePath(str)

    /** Url encoded string. */
    def urlComponentEncoded: String = platform.encodeURIComponent(str)

    /** Url component dencoded string. */
    def urlComponentDecoded: String = platform.decodeURIComponent(str)

    def urlEncoded: String = platform.encodeURI(str)

    def urlDecoded: String = platform.decodeURI(str)

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

    def asRamlAnnotation = s"(amf-$str)"

    def asOasExtension = s"x-amf-$str"
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
