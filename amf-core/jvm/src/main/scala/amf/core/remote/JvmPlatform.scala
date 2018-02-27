package amf.core.remote

import java.io.FileNotFoundException
import java.net.{HttpURLConnection, URI}

import amf.core.lexer.{CharArraySequence, CharSequenceStream, FileStream}
import amf.core.unsafe.PlatformBuilder
import org.mulesoft.common.io.{FileSystem, Fs}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JvmPlatform extends Platform {

  /** Underlying file system for platform. */
  override val fs: FileSystem = Fs

  /** Resolve specified url. */
  override protected def fetchHttp(url: String): Future[Content] = {
    val u          = new java.net.URL(url)
    val connection = u.openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")

    Future {
      connection.connect()
      connection.getResponseCode match {
        case 200 =>
          createContent(connection, url)
        case s => throw new Exception(s"Unhandled status code $s => $url")
      }
    }
  }

  private def createContent(connection: HttpURLConnection, url: String): Content = {
    Content(
      new CharSequenceStream(CharArraySequence(connection.getInputStream, connection.getContentLength, None)),
      url,
      Option(connection.getHeaderField("Content-Type"))
    )

  }

  /** Resolve specified file. */
  override protected def fetchFile(path: String): Future[Content] = Future {
    try {

      Content(new FileStream(path), ensureFileAuthority(path), extension(path).flatMap(mimeFromExtension))
    } catch {
      case e: FileNotFoundException => throw FileNotFound(e)
    }
  }

  /** Return temporary directory. */
  override def tmpdir(): String = System.getProperty("java.io.tmpdir")

  /** Location where the helper functions for custom validations must be retrieved */
  override def customValidationLibraryHelperLocation: String = "classpath:validations/amf_validation.js"

  override def resolvePath(path: String): String = {
    val res = new URI(path).normalize.toString
    if (res.startsWith("file://") || res.startsWith("file:///")) {
      res
    } else if (res.startsWith("file:/")) {
      res.replace("file:/", "file:///")
    } else {
      res
    }
  }
}

object JvmPlatform {
  private var singleton: Option[JvmPlatform] = None

  def instance(): JvmPlatform = singleton match {
    case Some(p) => p
    case None =>
      singleton = Some(PlatformBuilder())
      singleton.get
  }
}
