package amf.remote

import java.io.FileWriter
import java.net.{HttpURLConnection, URI, URL}

import amf.lexer.{CharArraySequence, CharSequenceStream, FileStream}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JvmPlatform extends Platform {

  /** Resolve specified url. */
  override protected def fetchHttp(url: String): Future[Content] = {
    val u: URL     = new URL(url)
    val connection = u.openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")

    Future {
      connection.connect()
      connection.getResponseCode match {
        case 200 =>
          createContent(connection)
        case s => throw new Exception(s"Unhandled status code $s")
      }
    }
  }

  private def createContent(connection: HttpURLConnection): Content = {
    Content(
      new CharSequenceStream(CharArraySequence(connection.getInputStream, connection.getContentLength, None)),
      Option(connection.getHeaderField("Content-Type"))
    )

  }

  private def mimeFromExtension(extension: Option[String]): Option[String] =
    extension.flatMap({
      case "json"         => Option(Mimes.`APPLICATION/JSON`)
      case "yaml" | "yam" => Option(Mimes.`APPLICATION/YAML`)
      case "raml"         => Option(Mimes.`APPLICATION/RAML`)
      case _              => None
    })

  private def extractExtension(path: String): Option[String] = {
    val dot = path.lastIndexOf(".")
    if (dot > 0) {
      Option(path.substring(dot))
    } else None
  }

  /** Resolve specified file. */
  override protected def fetchFile(path: String): Future[Content] = Future {
    Content(new FileStream(path), mimeFromExtension(extractExtension(path)))
  }

  /** Write specified content on specified file path. */
  override protected def writeFile(path: String, content: String): Future[Unit] = {
    Future {
      val writer: FileWriter = new FileWriter(path)
      writer.write(content)
      writer.flush()
      writer.close()
    }
  }

  override def resolvePath(path: String): String = new URI(path).normalize.toString
}

object PlatformBuilder {
  def apply(): JvmPlatform = new JvmPlatform()
}