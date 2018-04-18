package amf.core.remote.browser

import amf.internal.resource.{ResourceLoader, ResourceLoaderAdapter}
import amf.core.remote._
import org.mulesoft.common.io.FileSystem

import scala.scalajs.js.annotation.JSExportAll

/**
  *
  */
class JsBrowserPlatform extends JsPlatform {

  /** Underlying file system for platform. */
  override val fs: FileSystem = UnsupportedFileSystem

  override def loaders(): Seq[ResourceLoader] = Seq(ResourceLoaderAdapter(JsBrowserHttpResourceLoader()))

  /** Return temporary directory. */
  override def tmpdir(): String = {
    // Accept in Node only
    throw new Exception(s"Unsupported tmpdir operation")
  }

  override def resolvePath(path: String): String = path

}

@JSExportAll
object JsBrowserPlatform {
  private var singleton: Option[JsBrowserPlatform] = None

  def instance(): JsBrowserPlatform = singleton match {
    case Some(p) => p
    case None =>
      singleton = Some(new JsBrowserPlatform())
      singleton.get
  }
}
