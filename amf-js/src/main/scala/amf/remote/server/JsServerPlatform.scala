package amf.remote.server

import amf.interop._
import amf.lexer.CharSequenceStream
import amf.remote.{Content, Platform}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js

/**
  * Created by pedro.colunga on 5/28/17.
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
          promise.success(Content(new CharSequenceStream(path, content.toString)))
        }
      }
    )

    promise.future
  }

  /** Resolve specified url. */
  override protected def fetchHttp(url: String): Future[Content] = {
    val promise: Promise[Content] = Promise()

    Http.get(
      url,
      (response: ServerResponse) => {
        var str = ""

        //Another chunk of data has been received, append it to `str`
        response.on("data", (data: Buffer) => {
          str += data
        })

        //The whole response has been received
        response.on("end", () => {
          promise.success(Content(new CharSequenceStream(url, str)))
        })
      }
    )

    promise.future
  }

  /** Write specified content on specified file path. */
  override protected def writeFile(path: String, content: String): Future[Unit] = {
    val promise: Promise[Unit] = Promise()

    Fs.writeFile(path, content, (error: Any) => {
      if (error == null) promise.success()
      else promise.failure(new Exception(s"Write failed on $path: " + error))
    })

    promise.future
  }

  override def resolvePath(path: String): String = { //TODO redo: file://include.yaml doesn't work..
    val url: ParsedURL = URL.parse(path)
    url.protocol.get + "//" + url.host.get + Path.resolve(url.path.get)
  }
}
