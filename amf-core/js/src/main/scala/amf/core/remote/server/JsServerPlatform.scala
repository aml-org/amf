package amf.core.remote.server

import amf.core.interop.{OS, Path, ServerResponse}
import amf.core.lexer.CharSequenceStream
import amf.core.remote.File.FILE_PROTOCOL
import amf.core.remote.{Content, File, Http, Platform}
import org.mulesoft.common.io.{FileSystem, JsServerFileSystem}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  *
  */
class JsServerPlatform extends Platform {

  /** Underlying file system for platform. */
  override val fs: FileSystem = JsServerFileSystem

  override def exit(code: Int) = {
    js.Dynamic.global.process.exit(code)
  }

  /** Resolve specified file. */
  override protected def fetchFile(path: String): Future[Content] = {
    fs.asyncFile(path)
      .read()
      .map(content => {
        Content(new CharSequenceStream(path, content),
                ensureFileAuthority(path),
                extension(path).flatMap(mimeFromExtension))
      })
  }

  /** Resolve specified url. */
  override protected def fetchHttp(url: String): Future[Content] = {
    val promise: Promise[Content] = Promise()

    amf.core.interop.Http.get(
      url,
      (response: ServerResponse) => {
        var str = ""

        // Another chunk of data has been received, append it to `str`
        response.on("data", (s: String) => str += s)

        // The whole response has been received
        response.on("end", () => promise.success(Content(new CharSequenceStream(url, str), url)))
      }
    )

    promise.future
  }

  /** Return temporary directory. */
  override def tmpdir(): String = OS.tmpdir() + "/"

  override def resolvePath(uri: String): String = uri match {
    case File(path)                 => FILE_PROTOCOL + Path.resolve(withTrailingSlash(path)).substring(1)
    case Http(protocol, host, path) => protocol + host + Path.resolve(withTrailingSlash(path))
  }

  private def withTrailingSlash(path: String) = (if (!path.startsWith("/")) "/" else "") + path

  // TODO: Removed in modularization @modularization
  /*
  override val dialectsRegistry = JSDialectRegistry(this)
  override val validator        = new SHACLValidator()

  @JSExport
  def setupValidation(validation: Validation): js.Promise[Validation] = setupValidationBase(validation).toJSPromise
  */
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
}
