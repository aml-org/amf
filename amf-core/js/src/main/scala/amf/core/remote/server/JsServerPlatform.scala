package amf.core.remote.server

import java.io.IOException

import amf.core.lexer.CharSequenceStream
import amf.core.remote.File.FILE_PROTOCOL
import amf.core.remote._
import amf.core.remote.server.JsServerPlatform.OS
import org.mulesoft.common.io.{FileSystem, Fs}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportAll, JSImport}

/**
  *
  */
class JsServerPlatform extends Platform {

  /** Underlying file system for platform. */
  override val fs: FileSystem = Fs

  override def exit(code: Int): Unit = {
    js.Dynamic.global.process.exit(code)
  }

  /** Resolve specified file. */
  override protected def fetchFile(path: String): Future[Content] =
    fs.asyncFile(path)
      .read()
      .map(
        content =>
          Content(new CharSequenceStream(path, content),
                  ensureFileAuthority(path),
                  extension(path).flatMap(mimeFromExtension)))
      .recover {
        case io: IOException => throw FileNotFound(io)
      }

  /** Resolve specified url. */
  override protected def fetchHttp(url: String): Future[Content] = {
    val promise: Promise[Content] = Promise()

    if (url.startsWith("https:")) {
      Https.get(
        url,
        (response: js.Dynamic) => {
          var str = ""

          // CAREFUL!
          // this is required to avoid undefined behaviours
          val dataCb: js.Function1[Any, Any] = { (res: Any) =>
            str += res.toString
          }
          // Another chunk of data has been received, append it to `str`
          response.on("data", dataCb)

          val completedCb: js.Function = () => {
            val mediaType = try {
              Some(response.headers.asInstanceOf[js.Dictionary[String]]("content-type"))
            } catch {
              case e: Throwable => None
            }
            promise.success(Content(new CharSequenceStream(url, str), url, mediaType))
          }
          response.on("end", completedCb)
        }
      )

    } else {
      Http.get(
        url,
        (response: js.Dynamic) => {
          var str = ""

          val dataCb: js.Function1[Any, Any] = { (res: Any) =>
            str += res.toString
          }
          // Another chunk of data has been received, append it to `str`
          response.on("data", dataCb)

          val completedCb: js.Function = () => {
            val mediaType = try {
              Some(response.headers.asInstanceOf[js.Dictionary[String]]("content-type"))
            } catch {
              case e: Throwable => None
            }
            promise.success(Content(new CharSequenceStream(url, str), url, mediaType))
          }
          response.on("end", completedCb)
        }
      )
    }

    promise.future
  }

  /** Return temporary directory. */
  override def tmpdir(): String = OS.tmpdir() + "/"

  override def resolvePath(uri: String): String = {
    uri match {
      case File(path) =>
        if (path.startsWith("/")) {
          FILE_PROTOCOL + Path.resolve(path)
        } else {
          FILE_PROTOCOL + Path.resolve(withTrailingSlash(path)).substring(1)
        }

      case HttpParts(protocol, host, path) => protocol + host + Path.resolve(withTrailingSlash(path))
    }
  }

  private def withTrailingSlash(path: String) = (if (!path.startsWith("/")) "/" else "") + path

}

@JSExportAll
object JsServerPlatform {
  private var singleton: Option[JsServerPlatform] = None

  def instance(): JsServerPlatform = singleton match {
    case Some(p) => p
    case None =>
      singleton = Some(new JsServerPlatform())
      singleton.get
  }

  /** Operating System */
  @js.native
  @JSImport("os", JSImport.Namespace, "os")
  object OS extends js.Object {

    /** Returns the operating system's default directory for temporary files. */
    def tmpdir(): String = js.native
  }
}
