package amf.remote

import java.io.FileWriter
import java.net.{HttpURLConnection, URL}

import amf.lexer.{CharArraySequence, CharSequenceStream, CharStream, FileStream}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JvmPlatform extends Platform {

  /** Resolve specified url. */
  override protected def fetchHttp(url: String): Future[CharStream] = {
    val u: URL     = new URL(url)
    val connection = u.openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")

    Future {
      connection.connect()
      connection.getResponseCode match {
        case 200 =>
          new CharSequenceStream(CharArraySequence(connection.getInputStream, connection.getContentLength, None))
        case s => throw new Exception(s"Unhandled status code $s")
      }
    }
  }

  /** Resolve specified file. */
  override protected def fetchFile(path: String): Future[CharStream] = Future { new FileStream(path) }

  /** Write specified content on specified file path. */
  override protected def writeFile(path: String, content: String): Future[Unit] = {
    Future {
      val writer: FileWriter = new FileWriter(path)
      writer.write(content)
      writer.flush()
      writer.close()
    }
  }
}

object JvmPlatform {
  def apply(): JvmPlatform = new JvmPlatform()
}
