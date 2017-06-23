package amf.remote

import scala.concurrent.Future

/**
  * Created by pedro.colunga on 5/19/17.
  */
trait Platform {

  /** Resolve remote url. */
  def resolve(url: String, context: Option[Context]): Future[Content] = {
    url match {
      case Http(_)                             => fetchHttp(url)
      case File(path)                          => fetchFile(path)
      case Relative(path) if context.isDefined => resolve(context.get resolve path, None)
      case _                                   => Future.failed(new Exception(s"Unsupported url: $url"))
    }
  }

  def resolvePath(path: String): String

  /** Resolve specified file. */
  protected def fetchFile(path: String): Future[Content]

  /** Resolve specified url. */
  protected def fetchHttp(url: String): Future[Content]

  /** Write specified content on given url. */
  def write(url: String, content: String): Future[Unit] = {
    url match {
      case File(path) => writeFile(path, content)
      case _          => Future.failed(new Exception(s"Unsupported write operation: $url"))
    }
  }

  /** Write specified content on specified file path. */
  protected def writeFile(path: String, content: String): Future[Unit]
}

object Platform {
  def base(url: String): Option[String] = Some(url.substring(0, url.lastIndexOf('/')))
}

private object Http {
  def unapply(url: String): Option[String] = {
    url match {
      case s if s.startsWith("http://") || s.startsWith("https://") => Some(s)
      case _                                                        => None
    }
  }
}

private object File {
  def unapply(url: String): Option[String] = {
    url match {
      case s if s.startsWith("file://") => Some(s.stripPrefix("file://"))
      case _                            => None
    }
  }
}

private object Relative {
  def unapply(url: String): Option[String] = {
    url match {
      case s if !s.contains(":") => Some(s)
      case _                     => None
    }
  }
}