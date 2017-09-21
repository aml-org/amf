package amf.remote.browser

import amf.dialects.JSDialectRegistry
import amf.lexer.CharSequenceStream
import amf.remote.{Content, Platform}
import amf.validation.SHACLValidator
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  *
  */
class JsBrowserPlatform extends Platform {

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

  /** Write specified content on specified file path. */
  override protected def writeFile(path: String, content: String): Future[String] = {
    // Accept in Node only
    Future.failed(new Exception(s"Unsupported write operation: $path"))
  }

  /** Return temporary directory. */
  override def tmpdir(): String = {
    // Accept in Node only
    throw new Exception(s"Unsupported tmpdir operation")
  }

  override def resolvePath(path: String): String = path

  override val dialectsRegistry = JSDialectRegistry(this)
  override val validator = new SHACLValidator()
}
