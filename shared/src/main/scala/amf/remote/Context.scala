package amf.remote

import scala.Option.empty

/**
  * Context class for URL resolution.
  */
class Context protected (val platform: Platform,
                         val history: List[String],
                         val mapping: Option[(String, String)] = empty) {

  def hasCycles: Boolean = history.count(_.equals(current)) == 2

  def current: String = history.last
  def root: String    = history.head

  def update(url: String): Context = Context(platform, history, resolve(url), mapping)

  def resolve(url: String): String =
    applyMapping(platform.resolvePath(url match {
      case Absolute(s)               => s
      case RelativeToRoot(s)         => Context.stripFile(root) + s
      case RelativeToIncludedFile(s) => Context.stripFile(current) + s
    }))

  private def applyMapping(path: String): String = {
    mapping.fold(path)(mapping => path.replace(mapping._1, mapping._2))
  }
}

object Context {
  private def apply(platform: Platform,
                    history: List[String],
                    currentResolved: String,
                    mapping: Option[(String, String)]): Context =
    new Context(platform, history :+ currentResolved, mapping)

  def apply(platform: Platform, root: String): Context = Context(platform, root, empty)
  def apply(platform: Platform, root: String, mapping: Option[(String, String)]): Context =
    new Context(platform, List(root), mapping)

  private def stripFile(url: String): String =
    if (url.contains('/')) url.substring(0, url.lastIndexOf('/') + 1) else ""
}

private object Absolute {
  def unapply(url: String): Option[String] = url match {
    case s if s.startsWith("http://") || s.startsWith("https://") || s.startsWith("file://") => Some(s)
    case _                                                                                   => None
  }
}

private object RelativeToRoot {
  def unapply(url: String): Option[String] = url match {
    case s if s.startsWith("/") => Some(s)
    case _                      => None
  }
}

private object RelativeToIncludedFile {
  def unapply(url: String): Option[String] = Some(url)
}
