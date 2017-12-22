package amf.core.remote.browser

import amf.core.lexer.CharSequenceStream
import amf.core.remote.{Content, Platform, UnsupportedFileSystem}
import org.mulesoft.common.io.FileSystem
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.annotation.JSExportAll

/**
  *
  */
class JsBrowserPlatform extends Platform {

  /** Underlying file system for platform. */
  override val fs: FileSystem = UnsupportedFileSystem

  override protected def fetchHttp(url: String): Future[Content] = {
    Ajax
      .get(url)
      .flatMap(xhr =>
        xhr.status match {
          case 200 => Future { Content(new CharSequenceStream(xhr.responseText), url) }
          case s   => Future.failed(new Exception(s"Unhandled status code $s with ${xhr.statusText}"))
      })
  }

  override protected def fetchFile(url: String): Future[Content] = {
    // Accept in Node only
    Future.failed(new Exception(s"File protocol unsupported for: $url"))
  }

  /** Return temporary directory. */
  override def tmpdir(): String = {
    // Accept in Node only
    throw new Exception(s"Unsupported tmpdir operation")
  }

  override def resolvePath(path: String): String = path

}

@JSExportAll
object JsBrowserPlatform {
  private var singleton: Option[JsBrowserPlatform] = None

  def instance(): JsBrowserPlatform = singleton match {
    case Some(p) => p
    case None =>
      singleton = Some(new JsBrowserPlatform())
      singleton.get
  }
}
