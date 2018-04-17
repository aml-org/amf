package amf.core.remote.server

import amf.internal.resource.{ResourceLoader, ResourceLoaderAdapter}
import amf.core.remote.File.FILE_PROTOCOL
import amf.core.remote._
import amf.core.remote.server.JsServerPlatform.OS
import org.mulesoft.common.io.{FileSystem, Fs}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportAll, JSImport}

/**
  *
  */
class JsServerPlatform extends JsPlatform {

  /** Underlying file system for platform. */
  override val fs: FileSystem = Fs

  override def exit(code: Int): Unit = {
    js.Dynamic.global.process.exit(code)
  }

  /** Platform out of the box [ResourceLoader]s */
  override def loaders(): Seq[ResourceLoader] = Seq(
    ResourceLoaderAdapter(JsServerFileResourceLoader()),
    ResourceLoaderAdapter(JsServerHttpResourceLoader())
  )

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
