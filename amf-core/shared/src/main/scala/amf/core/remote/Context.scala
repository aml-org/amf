package amf.core.remote

/**
  * Context class for URL resolution.
  */
class Context protected (val platform: Platform,
                         val history: List[String],
                         val mappings: Map[String, String] = Map.empty) {

  def hasCycles: Boolean = history.count(_.equals(current)) == 2

  def current: String = if (history.isEmpty) "" else history.last
  def root: String    = if (history.isEmpty) "" else history.head

  def update(url: String): Context = Context(platform, history, resolve(url), mappings)

  def resolve(url: String): String =
    applyMapping(platform.resolvePath(url match {
      case Absolute(s)               => s
      case RelativeToRoot(s)         => Context.stripFile(root) + s
      case RelativeToIncludedFile(s) => Context.stripFile(current) + s
    }))

  private def applyMapping(path: String): String =
    mappings.find(m => path.startsWith(m._1)).fold(path)(m => path.replace(m._1, m._2))
}

object Context {
  private def apply(platform: Platform,
                    history: List[String],
                    currentResolved: String,
                    mapping: Map[String, String]): Context =
    new Context(platform, history :+ currentResolved, mapping)

  def apply(platform: Platform, root: String): Context = Context(platform, root, Map.empty)

  def apply(platform: Platform): Context = Context(platform, "", Map.empty)

  def apply(platform: Platform, root: String, mapping: Map[String, String]): Context =
    new Context(platform, Option(root).filter(_.nonEmpty).map(List(_)).getOrElse(Nil), mapping)

  def apply(platform: Platform, mapping: Map[String, String]): Context =
    new Context(platform, Nil, mapping)

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
