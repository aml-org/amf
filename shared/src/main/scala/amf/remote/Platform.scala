package amf.remote

import amf.lexer.CharStream
import amf.remote.Context.context

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
      case Relative(path) if context.isDefined => resolve(context.get qualify path, None)
      case _                                   => Future.failed(new Exception(s"Unsupported url: $url"))
    }
  }

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

case class Context(private val base: String) {
  def qualify(path: String): String = base + path

  def update(url: String): Context = {
    url match {
      case Relative(path) => context(base + path)
      case _              => Context(url)
    }
  }
}

object Context {
  def apply(url: String): Context = {
    url match {
      case Http(_)    => context(url)
      case File(path) => if (path.contains('/')) context(url) else new Context("file://")
      case _          => throw new Exception(s"Cannot create context for $url")
    }
  }

  private def context(url: String) = new Context(url.substring(0, url.lastIndexOf('/') + 1))
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