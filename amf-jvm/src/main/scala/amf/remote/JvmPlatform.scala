package amf.remote

import java.io.FileWriter
import java.net.{HttpURLConnection, URI}

import amf.dialects.JVMDialectRegistry
import amf.lexer.{CharArraySequence, CharSequenceStream, FileStream}
import amf.validation.core.ValidationResult
import amf.validation.{JVMValidationResult, SHACLValidator}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JvmPlatform extends Platform {

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
        case s => throw new Exception(s"Unhandled status code $s")
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
    Content(new FileStream(path), ensureFileAuthority(path), extension(path).flatMap(mimeFromExtension))
  }

  /** Write specified content on specified file path. */
  override protected def writeFile(path: String, content: String): Future[String] = {
    Future {
      val file = new java.io.File(path)
      file.getParentFile.mkdirs()
      val writer: FileWriter = new FileWriter(file)
      writer.write(content)
      writer.flush()
      writer.close()
      path
    }
  }

  /** Return temporary directory. */
  override def tmpdir(): String = System.getProperty("java.io.tmpdir")

  override def resolvePath(path: String): String = new URI(path).normalize.toString

  override val dialectsRegistry = JVMDialectRegistry(this)
  override val validator = new SHACLValidator
}

object PlatformBuilder {
  def apply(): JvmPlatform = new JvmPlatform()
}
