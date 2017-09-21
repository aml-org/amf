package amf.remote.server

import amf.dialects.JSDialectRegistry
import amf.interop.{Path, _}
import amf.lexer.CharSequenceStream
import amf.remote.File.FILE_PROTOCOL
import amf.remote.{Content, File, Http, Platform}
import amf.validation.SHACLValidator

import scala.concurrent.{Future, Promise}
import scala.scalajs.js

/**
  *
  */
class JsServerPlatform extends Platform {

  /** Resolve specified file. */
  override protected def fetchFile(path: String): Future[Content] = {
    val promise: Promise[Content] = Promise()

    Fs.readFile(
      path,
      (err: Any, content: js.Any) => {
        if (err != null) {
          promise.failure(new Exception(s"Could not load file $path from fs"))
        } else {
          promise.success(
            Content(new CharSequenceStream(path, content.toString), ensureFileAuthority(path), extension(path).flatMap(mimeFromExtension)))
        }
      }
    )

    promise.future
  }

  /** Resolve specified url. */
  override protected def fetchHttp(url: String): Future[Content] = {
    val promise: Promise[Content] = Promise()

    amf.interop.Http.get(
      url,
      (response: ServerResponse) => {
        var str = ""

        //Another chunk of data has been received, append it to `str`
        response.on("data", (data: Buffer) => {
          str += data
        })

        //The whole response has been received
        response.on("end", () => {
          promise.success(Content(new CharSequenceStream(url, str), url))
        })
      }
    )

    promise.future
  }

  /** Write specified content on specified file path. */
  override protected def writeFile(path: String, content: String): Future[String] = {
    val promise: Promise[String] = Promise()

    Fs.writeFile(path, content, (error: Any) => {
      if (error == null) promise.success(path)
      else promise.failure(new Exception(s"Write failed on $path: " + error))
    })

    promise.future
  }

  /** Return temporary directory. */
  override def tmpdir(): String = OS.tmpdir() + "/"

  override def resolvePath(uri: String): String = uri match {
    case File(path)                 => FILE_PROTOCOL + Path.resolve(withTrailingSlash(path)).substring(1)
    case Http(protocol, host, path) => protocol + host + Path.resolve(withTrailingSlash(path))
  }

  private def withTrailingSlash(path: String) = (if (!path.startsWith("/")) "/" else "") + path

  override val dialectsRegistry = JSDialectRegistry(this)
  override val validator = new SHACLValidator()
}
